package net.javajudd.attis.service;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import net.javajudd.attis.domain.Participant;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sfn.SfnClient;
import software.amazon.awssdk.services.sfn.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class AWSService {
    String StepFunctionArn;

    public void setStepFunctionArn(String stepFunctionArn) { this.StepFunctionArn = stepFunctionArn; }
    public String getStepFunctionArn() { return StepFunctionArn; }

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

    public void createUserAndVM(Participant participant) {
        SfnClient client = SfnClient.builder()
                .region(Region.US_EAST_2)
                .build();

        String participantJson = new Gson().toJson(participant);

        StartExecutionRequest request = StartExecutionRequest.builder()
                .name("AttisExecution"+UUID.randomUUID().toString().substring(0,7))
                .input("{\"Participant\": "+participantJson+"}")
                .stateMachineArn(StepFunctionArn)
                .build();

        StartExecutionResponse executionResponse = client.startExecution(request);
    }
}
