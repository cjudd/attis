package net.javajudd.attis.controller;

import net.javajudd.attis.service.AWSService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class IndexController {

    @Autowired
    AWSService awsService;

    @GetMapping()
    public String index(Model model) {
        if(awsService.getStepFunctionArn() != null) {
            return "redirect:/participant";
        }
        model.addAttribute("stepFunctions", awsService.getStateMachines());
        return "admin/initialization";
    }

    @PostMapping({"","/"})
    public String initialize(@RequestParam(value="stateMachineArn") String stateMachineArn) {
        awsService.setStepFunctionArn(stateMachineArn);
        return "redirect:/participant";
    }

}
