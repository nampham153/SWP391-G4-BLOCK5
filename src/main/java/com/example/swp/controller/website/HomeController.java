package com.example.swp.controller.website;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String root() {
        return "redirect:/SWP";
    }

    @GetMapping("/SWP")
    public String swpHome() {
        return "home-page"; // Trả về template home-page.html
    }

    @GetMapping("/home-page")
    public String home() {
        return "home-page"; // Trả về template home-page.html
    }
}