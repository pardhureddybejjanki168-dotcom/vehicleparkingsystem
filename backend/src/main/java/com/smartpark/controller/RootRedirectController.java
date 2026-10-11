package com.smartpark.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Redirects browser requests to the backend root URL over to the React frontend.
 * Without this, Spring Security would intercept GET / with no JWT and return a 401 JSON error.
 */
@Controller
public class RootRedirectController {

    @GetMapping("/")
    public String redirectToFrontend() {
        return "redirect:http://localhost:5173/";
    }
}
