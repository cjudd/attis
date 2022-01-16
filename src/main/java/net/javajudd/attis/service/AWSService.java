package net.javajudd.attis.service;

import net.javajudd.attis.domain.Participant;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.waiters.WaiterResponse;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.model.AddUserToGroupRequest;
import software.amazon.awssdk.services.iam.model.CreateAccessKeyRequest;
import software.amazon.awssdk.services.iam.model.CreateAccessKeyResponse;
import software.amazon.awssdk.services.iam.model.CreateUserRequest;
import software.amazon.awssdk.services.iam.model.CreateUserResponse;
import software.amazon.awssdk.services.iam.model.GetUserRequest;
import software.amazon.awssdk.services.iam.model.GetUserResponse;
import software.amazon.awssdk.services.iam.model.Tag;
import software.amazon.awssdk.services.iam.waiters.IamWaiter;

@Service
public class AWSService {

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

        AddUserToGroupRequest groupRequest = AddUserToGroupRequest.builder().groupName("developers").userName(participant.getInitials()).build();
        iam.addUserToGroup(groupRequest);
    }
}
