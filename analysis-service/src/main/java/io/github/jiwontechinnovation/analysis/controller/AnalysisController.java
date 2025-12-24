package io.github.jiwontechinnovation.analysis.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AnalysisController {

    @GetMapping("/test")
    public String test() {
        return "Analysis Service is working!";
    }
}
