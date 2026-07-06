package com.bob.pay.interfaces.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaForwardController {

    @GetMapping(value = {
            "/client",
            "/client/**",
            "/admin",
            "/admin/**",
            "/technician",
            "/technician/**"
    })
    public String forwardSpaRoutes() {
        return "forward:/index.html";
    }
}
