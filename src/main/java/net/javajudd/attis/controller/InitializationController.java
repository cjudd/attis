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
        if(awsService.getIamStepFunctionArn() != null && awsService.getVmStepFunctionArn() != null) {
            return "redirect:/participant";
        }
        model.addAttribute("iamStepFunctions", awsService.getStateMachines("AttisIam"));
        model.addAttribute("vmStepFunctions", awsService.getStateMachines("AttisVM"));
        return "admin/initialization";
    }

    @PostMapping()
    public String initialize(@RequestParam(value="iamStepFunctionArn") String iamStepFunctionArn,
                             @RequestParam(value="vmStepFunctionArn") String vmStepFunctionArn) {
        awsService.setIamStepFunctionArn(iamStepFunctionArn);
        awsService.setVmStepFunctionArn(vmStepFunctionArn);
        return "redirect:/participant";
    }
}
