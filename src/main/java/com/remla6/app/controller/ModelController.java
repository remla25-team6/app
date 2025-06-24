package com.remla6.app.controller;

import com.remla6.app.exception.InferenceFailedException;
import com.remla6.app.metric.WebMetrics;
import com.remla6.app.model.FeedbackModel;
import com.remla6.app.model.SentimentModel;
import com.remla6.app.service.FeedbackService;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Controller
@RequestMapping("/")
public class ModelController {
    private final ModelService modelService;
    private final FeedbackService feedbackService;
    private final WebMetrics metrics;

    /**
     * REST endpoint serving GET /
     *
     * @param model Spring MVC Model
     * @return Thymleaf page
     */
    @GetMapping
    public String index(Model model) {
        List<SentimentModel> responses = modelService.getAllPreviousResults();
        metrics.updateStoredResponsesCount(responses.size());
        updateSentimentGauges(responses); // Update sentiment labeled gauges

        model.addAttribute("responses", responses);
        model.addAttribute("version", VersionUtil.getVersion());
        return "index";
    }

    /**
     * Receive and persist user inference disagreements.
     * @param disagreeIds The respective id's
     * @param model The MVC Model
     * @param redirectAttributes MVC parameter
     * @return A redirect to the homepage, accompanied by succes or error message
     */
    @PostMapping("/train")
    public String handleDisagreements(@RequestParam(value = "disagreeIds", required = false) List<Long> disagreeIds,
                                      Model model,
                                      RedirectAttributes redirectAttributes) {
        try {
            if (disagreeIds == null || disagreeIds.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "No disagreements selected");
                return "redirect:/";
            }

            // Collect the disagreed predictions for feedback
            List<FeedbackModel> feedbackEntries = new ArrayList<>();

            for (Long id : disagreeIds) {
                // Retrieve the response from your storage (e.g., in-memory cache, database)
                SentimentModel response = modelService.findById(id);

                if (response != null) {
                    // Create feedback entry with flipped sentiment
                    String correctedSentiment = response.getSentiment().equals("pos") ? "neg" : "pos";

                    FeedbackModel feedback = new FeedbackModel(
                            id,
                            response.getInputString(),
                            correctedSentiment
                    );

                    feedbackEntries.add(feedback);
                }
            }

            // Send feedback to model-service
            if (!feedbackEntries.isEmpty()) {
                feedbackService.storeFeedback(feedbackEntries);

                redirectAttributes.addFlashAttribute("successMessage",
                        String.format("Successfully submitted %d corrections for training", feedbackEntries.size()));
            }

            return "redirect:/";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Failed to submit feedback. Please try again later.");
            return "redirect:/";
        }
    }

    /**
     * Rest endpoint serving as an API for broadcasting user corrected sentiment
     * @return List of all FeedbackModel objects in database
     */
    @GetMapping("/train")
    @ResponseBody // Without this it needs HTML templating. Now it serves similarly to an api.
    public List<FeedbackModel> getUserFeedback() {
        return feedbackService.fetchFeedback();
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
        SentimentModel sentiment;

        // Process and persist inference
        try {
            sentiment = modelService.processSentiment(review);
            metrics.recordSuccessfulInference(sentiment.getSentiment());
            metrics.stopInferenceTimer(sample, sentiment.getSentiment());
        } catch (Exception ex) {
            // We need to stop the timer and then rethrow because spring handles it in the end.
            metrics.recordInferenceFailure();
            metrics.stopInferenceTimer(sample, "failed"); // Return failed outcome when inference fails
            throw ex;
        }
        

        // Retrieve all previous inferences.
        List<SentimentModel> responses = modelService.getAllPreviousResults();
        metrics.updateStoredResponsesCount(responses.size());
        updateSentimentGauges(responses);

        // MVC
        model.addAttribute("responses", responses);
        model.addAttribute("version", VersionUtil.getVersion());

        return "index";
    }


     // Helper method to count sentiments and update the labeled gauges.
    private void updateSentimentGauges(List<SentimentModel> responses) {
        long positiveCount = responses.stream().filter(r -> "pos".equals(r.getSentiment())).count();
        long negativeCount = responses.stream().filter(r -> "neg".equals(r.getSentiment())).count();
        metrics.updateStoredResponsesBySentiment(positiveCount, negativeCount);
    }

}