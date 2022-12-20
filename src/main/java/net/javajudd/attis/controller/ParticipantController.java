package net.javajudd.attis.controller;

import net.javajudd.attis.domain.Participant;
import net.javajudd.attis.repository.ParticipantRepository;
import net.javajudd.attis.service.AWSService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
        return "participant/add-participant";
    }

    @PostMapping({"","/"})
    public String addParticipant(@Valid Participant participant, BindingResult result, Model model) throws InterruptedException {
        if (result.hasErrors()) {
            return "participant/add-participant";
        }

        participantRepository.save(participant);
        aws.createIamUser(participant);
        //aws.createDevVM(participant);
        participantRepository.save(participant);
        return "redirect:/participant/registered";
    }

    @GetMapping({"registered"})
    public String registered() {
        return "participant/registered";
    }
}
