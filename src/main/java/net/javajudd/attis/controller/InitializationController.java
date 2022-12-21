package net.javajudd.attis.controller;

import net.javajudd.attis.service.AWSService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/init")
public class InitializationController {

    @Autowired
    AWSService awsService;

    @GetMapping
    public String initForm(Model model) {
        if(awsService.getStepFunctionArn() != null) {
            return "redirect:/participant";
        }
        model.addAttribute("StepFunctions", awsService.getStateMachines("Cybele"));
        return "admin/initialization";
    }

    @PostMapping()
    public String initialize(@RequestParam(value="StepFunctionArn") String StepFunctionArn) {
        awsService.setStepFunctionArn(StepFunctionArn);
        return "redirect:/participant";
    }
}
