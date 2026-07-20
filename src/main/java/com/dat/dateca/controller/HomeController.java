package com.dat.dateca.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/")
    public Map<String, String> home() {
        return Map.of(
                "application", "Study Platform API",
                "status", "UP",
                "documentation", "/swagger-ui/index.html",
                "health", "/actuator/health"
        );
    }
}
