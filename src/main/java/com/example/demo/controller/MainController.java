package com.example.demo.controller;

import com.example.demo.model.Login;
import com.example.demo.model.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @GetMapping("/login")
    public String showLoginPage(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("login", new Login());
        return "login_register";
    }

    @GetMapping("/index")
    public String showIndexPage() {
        return "index";
    }
}
