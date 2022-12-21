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
import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.sfn.SfnClient;
import software.amazon.awssdk.services.sfn.model.*;

import java.util.*;

import static java.lang.String.format;

@Service
@Slf4j
public class AWSService {

    @Value("${aws.dev.ami}")
    String awsDevAmi;

    String iamStepFunctionArn;
    String vmStepFunctionArn;

    @Autowired
    MailService mailService;

    public void setVmStepFunctionArn(String arn) { vmStepFunctionArn = arn; }
    public String getVmStepFunctionArn() { return iamStepFunctionArn; }
    public void setIamStepFunctionArn(String arn) { iamStepFunctionArn = arn; }
    public String getIamStepFunctionArn() { return iamStepFunctionArn; }

    public List<StateMachineListItem> getStateMachines(String tagKey) {
        SfnClient client = SfnClient.builder()
                .region(Region.US_EAST_2)
                .build();

        ListStateMachinesResponse stateMachinesResponse = client.listStateMachines();
        List<StateMachineListItem> attisFunctions = new ArrayList<>();
        for(StateMachineListItem function : stateMachinesResponse.stateMachines()) {
            List<software.amazon.awssdk.services.sfn.model.Tag> tags = client.listTagsForResource(ListTagsForResourceRequest.builder().resourceArn(function.stateMachineArn()).build()).tags();
            for(software.amazon.awssdk.services.sfn.model.Tag tag : tags) {
                if (tag.key().equals(tagKey)) {
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
        
        String participantJson = new Gson().toJson(participant);

        StartExecutionRequest request = StartExecutionRequest.builder()
                .name("AttisExecutionAttempt"+UUID.randomUUID().toString().substring(0,7))
                .input("{\"userData\": "+participantJson+"}")
                .stateMachineArn(iamStepFunctionArn)
                .build();

        StartExecutionResponse executionResponse = client.startExecution(request);

        DescribeExecutionRequest describeRequest = DescribeExecutionRequest.builder().executionArn(executionResponse.executionArn()).build();

        DescribeExecutionResponse describeResponse;
        do {
            Thread.sleep(1000);
            describeResponse = client.describeExecution(describeRequest);

        } while(describeResponse.status() == ExecutionStatus.RUNNING);
        //Possibly add some logic if ExecutionStatus is Failing, Timed-out, etc.

        client.close();

        JsonObject root = JsonParser.parseString(describeResponse.output()).getAsJsonObject();
        participant.setAccess(root.getAsJsonObject("AccessKey").get("AccessKeyId").getAsString());
        participant.setSecret(root.getAsJsonObject("AccessKey").get("SecretAccessKey").getAsString());
        participant.setPassword(root.get("RandomPassword").getAsString());

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
        SfnClient client = SfnClient.builder()
                .region(Region.US_EAST_2)
                .build();

        String participantJson = new Gson().toJson(participant);

        StartExecutionRequest request = StartExecutionRequest.builder()
                .name("AttisExecutionAttempt"+UUID.randomUUID().toString().substring(0,7))
                .input("{\"Participant\": "+participantJson+", \"DevAmi\": \""+awsDevAmi+"\"}")
                .stateMachineArn(vmStepFunctionArn)
                .build();

        StartExecutionResponse executionResponse = client.startExecution(request);

        //Could add describe request and do checks
    }
}
