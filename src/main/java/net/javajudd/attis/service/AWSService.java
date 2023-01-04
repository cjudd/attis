package net.javajudd.attis.service;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import net.javajudd.attis.domain.Participant;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sfn.SfnClient;
import software.amazon.awssdk.services.sfn.model.ListStateMachinesResponse;
import software.amazon.awssdk.services.sfn.model.ListTagsForResourceRequest;
import software.amazon.awssdk.services.sfn.model.StartExecutionRequest;
import software.amazon.awssdk.services.sfn.model.StartExecutionResponse;
import software.amazon.awssdk.services.sfn.model.StateMachineListItem;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AWSService {
    String stepFunctionArn;

    public void setStepFunctionArn(String stepFunctionArn) { this.stepFunctionArn = stepFunctionArn; }
    public Boolean isStepFunctionArnInitialized() { return stepFunctionArn != null; }
    public List<StateMachineListItem> getStateMachines(String tagKey) {
        SfnClient client = SfnClient.builder()
                .region(Region.US_EAST_2)
                .build();

        ListStateMachinesResponse stateMachinesResponse = client.listStateMachines();

        return stateMachinesResponse.stateMachines().stream().filter(machine ->
                client.listTagsForResource(ListTagsForResourceRequest.builder().resourceArn(machine.stateMachineArn()).build()).tags().stream().anyMatch(tag ->
                        tag.key().equals(tagKey)
                )
        ).collect(Collectors.toList());
    }

    public void executeStepFunction(Participant participant) {
        SfnClient client = SfnClient.builder()
                .region(Region.US_EAST_2)
                .build();

        String participantJson = new Gson().toJson(participant);

        StartExecutionRequest request = StartExecutionRequest.builder()
                .name("AttisExecution-"+participant.getInitials()+"-"+UUID.randomUUID().toString().substring(0,7))
                .input("{\"Participant\": "+participantJson+"}")
                .stateMachineArn(stepFunctionArn)
                .build();

        StartExecutionResponse executionResponse = client.startExecution(request);
    }
}
