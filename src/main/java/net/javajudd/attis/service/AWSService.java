package net.javajudd.attis.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import net.javajudd.attis.domain.Participant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;
import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.sfn.SfnClient;
import software.amazon.awssdk.services.sfn.model.*;

import java.util.*;

import static java.lang.String.format;
import static net.javajudd.attis.utils.PasswordUtil.generatePassword;

@Service
@Slf4j
public class AWSService {

    @Value("${aws.dev.ami}")
    String awsDevAmi;

    String stepFunctionArn;

    @Autowired
    MailService mailService;

    public void setStepFunctionArn(String arn) { stepFunctionArn = arn; }
    public String getStepFunctionArn() { return stepFunctionArn; }

    public List<StateMachineListItem> getStateMachines() {
        SfnClient client = SfnClient.builder()
                .region(Region.US_EAST_2)
                .build();

        ListStateMachinesResponse stateMachinesResponse = client.listStateMachines();
        List<StateMachineListItem> attisFunctions = new ArrayList<>();
        for(StateMachineListItem function : stateMachinesResponse.stateMachines()) {
            List<software.amazon.awssdk.services.sfn.model.Tag> tags = client.listTagsForResource(ListTagsForResourceRequest.builder().resourceArn(function.stateMachineArn()).build()).tags();
            for(software.amazon.awssdk.services.sfn.model.Tag tag : tags) {
                if (tag.key().equals("AttisFunction")) {
                    attisFunctions.add(function);
                }
            }
        }
        return attisFunctions;
    }

    public void createIamUser(Participant participant) throws InterruptedException {
        SfnClient client = SfnClient.builder()
                .region(Region.US_EAST_2)
                .build();

        String password = generatePassword();
        participant.setPassword(password);
        String participantJson = new Gson().toJson(participant);

        StartExecutionRequest request = StartExecutionRequest.builder()
                .name("AttisExecutionAttempt"+UUID.randomUUID().toString().substring(0,7))
                .input("{\"userData\": "+participantJson+"}")
                .stateMachineArn(stepFunctionArn)
                .build();

        StartExecutionResponse executionResponse = client.startExecution(request);

        DescribeExecutionRequest describeRequest = DescribeExecutionRequest.builder().executionArn(executionResponse.executionArn()).build();

        DescribeExecutionResponse describeResponse;
        do {
            Thread.sleep(1000);
            describeResponse = client.describeExecution(describeRequest);

        } while(describeResponse.status() == ExecutionStatus.RUNNING);
        //Possibly add some logic if ExecutionStatus is Failing, Timed-out, etc.

        JsonObject root = JsonParser.parseString(describeResponse.output()).getAsJsonObject();
        participant.setAccess(root.getAsJsonObject("AccessKey").get("AccessKeyId").getAsString());
        participant.setSecret(root.getAsJsonObject("AccessKey").get("SecretAccessKey").getAsString());

        sendEmail(participant);
    }

    public void sendEmail(Participant participant) {
        IamClient iam = IamClient.builder()
                .region(Region.AWS_GLOBAL)
                .build();

        String awsAlias = iam.listAccountAliases().accountAliases().get(0);
        String awsUrl = format("https://%s.signin.aws.amazon.com/console/", awsAlias);
        log.info("Participant {} ({}) and email {} created.", participant.getInitials(), participant.getName(), participant.getEmail());

        Map<String, Object> map = new HashMap<>();
        map.put("participant", participant);
        map.put("url", awsUrl);

        mailService.sendTemplateMessage(participant.getEmail(), "AWS Training Registration", "registration", map);
        log.info("Participant registration email sent to {} ({}) at {}", participant.getInitials(), participant.getName(), participant.getEmail());
    }

    public void createDevVM(Participant participant) {
        if(awsDevAmi != null && !awsDevAmi.isEmpty()) {
            Ec2Client ec2 = Ec2Client.builder()
                    .region(Region.US_EAST_2)
                    .build();

            RunInstancesRequest runRequest = RunInstancesRequest.builder()
                    .imageId(awsDevAmi)
                    .instanceType(InstanceType.T3_LARGE)
                    .maxCount(1)
                    .minCount(1)
                    .keyName("dev-key")
                    .blockDeviceMappings(BlockDeviceMapping.builder()
                            .deviceName("/dev/xvda")
                            .ebs(EbsBlockDevice.builder()
                                    .volumeSize(40)
                                    .deleteOnTermination(true)
                                    .encrypted(true)
                                    .build())
                            .build())
                    .hibernationOptions(HibernationOptionsRequest.builder().configured(true).build())
                    .securityGroups("devvm-default-sg")
                    .build();

            RunInstancesResponse response = ec2.runInstances(runRequest);
            String instanceId = response.instances().get(0).instanceId();
            log.info("Run EC2 Dev Instance {} for {} ({})", instanceId, participant.getInitials(), participant.getName());

            software.amazon.awssdk.services.ec2.model.Tag tag = software.amazon.awssdk.services.ec2.model.Tag.builder()
                    .key("Name").value(participant.getInitials() + "-dev")
                    .build();
            software.amazon.awssdk.services.ec2.model.Tag name = software.amazon.awssdk.services.ec2.model.Tag.builder()
                    .key("Participant").value(participant.getName())
                    .build();
            software.amazon.awssdk.services.ec2.model.Tag company = software.amazon.awssdk.services.ec2.model.Tag.builder()
                    .key("Company").value(participant.getCompany())
                    .build();
            software.amazon.awssdk.services.ec2.model.Tag email = software.amazon.awssdk.services.ec2.model.Tag.builder()
                    .key("Email").value(participant.getEmail())
                    .build();

            CreateTagsRequest tagRequest = CreateTagsRequest.builder()
                    .resources(instanceId)
                    .tags(tag, name, company, email)
                    .build();

            ec2.createTags(tagRequest);
        } else {
            log.info(format("No AMI specified for {}", participant.getInitials()));
        }
    }
}
