package com.silvercare.companion_portal.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CommonViewController {

    @GetMapping("/common/navbar")
    public String navbar() {
        return "common/navbar"; 
    }
}
