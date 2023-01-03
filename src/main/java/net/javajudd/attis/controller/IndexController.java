package net.javajudd.attis.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    @GetMapping()
    public String index() {
        return "redirect:/participant";
    }
}
