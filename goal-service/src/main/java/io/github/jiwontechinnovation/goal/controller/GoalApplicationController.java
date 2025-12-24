package io.github.jiwontechinnovation.goal.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GoalApplicationController {

    @GetMapping("/test")
    public String test() {
        return "Goal Service is working!";
    }
}
