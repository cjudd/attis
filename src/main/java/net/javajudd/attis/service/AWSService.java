package net.javajudd.attis.service;

import net.javajudd.attis.domain.Participant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.waiters.WaiterResponse;
import software.amazon.awssdk.regions.Region;
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
}
