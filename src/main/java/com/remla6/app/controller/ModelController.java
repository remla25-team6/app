package com.remla6.app.controller;

import com.remla6.app.exception.InferenceFailedException;
import com.remla6.app.metric.WebMetrics;
import com.remla6.app.model.SentimentModel;
import com.remla6.app.service.ModelService;
import io.micrometer.core.instrument.Timer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.remla25team6.libversion.VersionUtil;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@AllArgsConstructor
@Controller
@RequestMapping("/")
public class ModelController {
    private final ModelService modelService;
    private final WebMetrics metrics;

    /**
     * REST endpoint serving GET /
     *
     * @param model Spring MVC Model
     * @return Thymleaf page
     */
    @GetMapping
    public String index(Model model) {
        model.addAttribute("version", VersionUtil.getVersion());
        return "index";
    }

    /**
     * Rest endpoint serving POST /
     * @param review RequestParameter representing user-submitted review.
     * @param model Spring MVC Model
     * @return Thymleaf page
     * @throws InferenceFailedException if model-service inference fails. Caught & handled by Spring
     */
    @PostMapping
    public String postReview(@RequestParam("review") String review, Model model) throws InferenceFailedException {
        // Inference metric start timer:
        Timer.Sample sample = metrics.startInferenceTimer();

        // Process and persist inference
        try {
            modelService.processSentiment(review);
        } catch (Exception ex) {
            // We need to stop the timer and then rethrow because spring handles it in the end.
            metrics.recordInferenceFailure();
            metrics.stopInferenceTimer(sample);
            throw ex;
        }

        metrics.stopInferenceTimer(sample);

        // Retrieve all previous inferences.
        List<SentimentModel> responses = modelService.getAllPreviousResults();
        metrics.updateStoredResponsesCount(responses.size());

        // MVC
        model.addAttribute("responses", responses);
        model.addAttribute("version", VersionUtil.getVersion());
        return "index";
    }

}
