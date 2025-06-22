package com.remla6.app.service;

import com.remla6.app.model.FeedbackModel;
import com.remla6.app.repository.FeedbackRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FeedbackService {
    private FeedbackRepository feedbackRepository;

    public FeedbackService(FeedbackRepository feedbackRepository) {
        this.feedbackRepository = feedbackRepository;
    }

    public void storeFeedback(List<FeedbackModel> feedback) {
        feedbackRepository.saveAll(feedback);
    }

    public List<FeedbackModel> fetchFeedback() {
        return feedbackRepository.findAll();
    }

}
