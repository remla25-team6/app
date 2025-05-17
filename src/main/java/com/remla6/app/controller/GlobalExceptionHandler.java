package com.remla6.app.controller;

import com.remla6.app.exception.InferenceFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(InferenceFailedException.class)
    public String inferenceFailed(InferenceFailedException ex,
                                  RedirectAttributes redirectAttributes) {
        logger.error("An unexpected exception occurred: {}", ex.getMessage(), ex);
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/";
    }


    @ExceptionHandler(Exception.class)
    public String blanketExceptionHandler(Exception ex,
                                          RedirectAttributes redirectAttributes) {
        logger.error("An unexpected exception occurred: {}", ex.getMessage(), ex);
        redirectAttributes.addFlashAttribute("errorMessage", "Something unexpected went wrong. Try again later");
        return "redirect:/";
    }

}
