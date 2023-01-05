package net.javajudd.attis.controller;

import net.javajudd.attis.domain.Participant;
import net.javajudd.attis.repository.ParticipantRepository;
import net.javajudd.attis.service.AWSService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;

@Controller
@RequestMapping("/participant")
public class ParticipantController {

    @Autowired
    ParticipantRepository participantRepository;

    @Autowired
    AWSService aws;

    @GetMapping({"","/"})
    public String index(Participant participant) {
        if(aws.isStepFunctionArnInitialized()) {
            return "participant/add-participant";
        }
        return "redirect:/init";
    }

    @PostMapping({"","/"})
    public String addParticipant(@Valid Participant participant, BindingResult result) throws InterruptedException {
        if (result.hasErrors()) {
            return "participant/add-participant";
        }

        participantRepository.save(participant);
        aws.executeStepFunction(participant);
        participantRepository.save(participant);
        return "redirect:/participant/registered";
    }

    @GetMapping({"registered"})
    public String registered() {
        return "participant/registered";
    }
}
