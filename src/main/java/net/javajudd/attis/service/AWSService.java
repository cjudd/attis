package net.javajudd.attis.service;

import net.javajudd.attis.domain.Participant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.waiters.WaiterResponse;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.CreateTagsRequest;
import software.amazon.awssdk.services.ec2.model.InstanceType;
import software.amazon.awssdk.services.ec2.model.RunInstancesRequest;
import software.amazon.awssdk.services.ec2.model.RunInstancesResponse;
import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.model.AddUserToGroupRequest;
import software.amazon.awssdk.services.iam.model.CreateAccessKeyRequest;
import software.amazon.awssdk.services.iam.model.CreateAccessKeyResponse;
import software.amazon.awssdk.services.iam.model.CreateLoginProfileRequest;
import software.amazon.awssdk.services.iam.model.CreateUserRequest;
import software.amazon.awssdk.services.iam.model.CreateUserResponse;
import software.amazon.awssdk.services.iam.model.GetUserRequest;
import software.amazon.awssdk.services.iam.model.GetUserResponse;
import software.amazon.awssdk.services.iam.model.Tag;
import software.amazon.awssdk.services.iam.waiters.IamWaiter;

import java.util.HashMap;
import java.util.Map;

import static net.javajudd.attis.utils.PasswordUtil.generatePassword;

@Service
public class AWSService {

    @Value("${aws.account.url}")
    String awsUrl;

    @Autowired
    MailService mailService;

    public void createIamUser(Participant participant) {
        IamClient iam = IamClient.builder()
                .region(Region.AWS_GLOBAL)
                .build();

        IamWaiter iamWaiter = iam.waiter();

        Tag name = Tag.builder().key("Participant").value(participant.getName()).build();
        Tag company = Tag.builder().key("Company").value(participant.getCompany()).build();
        Tag email = Tag.builder().key("Email").value(participant.getEmail()).build();

        CreateUserRequest request = CreateUserRequest.builder()
                .userName(participant.getInitials())
                .tags(name, company, email)
                .build();

        CreateUserResponse response = iam.createUser(request);

        // Wait until the user is created
        GetUserRequest userRequest = GetUserRequest.builder()
                .userName(response.user().userName())
                .build();

        WaiterResponse<GetUserResponse> waitUntilUserExists = iamWaiter.waitUntilUserExists(userRequest);
        waitUntilUserExists.matched().response().ifPresent(System.out::println);

        CreateAccessKeyRequest keyRequest = CreateAccessKeyRequest.builder().userName(participant.getInitials()).build();
        CreateAccessKeyResponse accessKeyResponse = iam.createAccessKey(keyRequest);

        participant.setAccess(accessKeyResponse.accessKey().accessKeyId());
        participant.setSecret(accessKeyResponse.accessKey().secretAccessKey());

        String password = generatePassword();
        CreateLoginProfileRequest loginProfileRequest = CreateLoginProfileRequest.builder()
                .userName(participant.getInitials())
                .password(password)
                .passwordResetRequired(false).build();

        iam.createLoginProfile(loginProfileRequest);

        participant.setPassword(password);

        AddUserToGroupRequest groupRequest = AddUserToGroupRequest.builder().groupName("developers").userName(participant.getInitials()).build();
        iam.addUserToGroup(groupRequest);

        Map<String, Object> map = new HashMap<>();
        map.put("participant", participant);
        map.put("url", awsUrl);
        mailService.sendTemplateMessage(participant.getEmail(), "AWS Training Registration", "registration", map);
    }

    public void createDevVM(Participant participant) {
        String amiId = "ami-0b59d313feafc34cf"; // dev-base image
        Ec2Client ec2 = Ec2Client.builder()
                .region(Region.US_EAST_2)
                .build();

        RunInstancesRequest runRequest = RunInstancesRequest.builder()
                .imageId(amiId)
                .instanceType(InstanceType.T3_LARGE)
                .maxCount(1)
                .minCount(1)
                .keyName("dev-key")
                .securityGroups("devvm-default-sg")
                .build();

        RunInstancesResponse response = ec2.runInstances(runRequest);
        String instanceId = response.instances().get(0).instanceId();

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
    }
}
