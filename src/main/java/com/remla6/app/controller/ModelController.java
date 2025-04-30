package com.remla6.app.controller;

import com.remla6.app.service.ModelService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@AllArgsConstructor
@Controller
@RequestMapping("/")
public class ModelController {
    private final ModelService modelService;

    @GetMapping
    public String index(Model model) {
        return "index";
    }

}
