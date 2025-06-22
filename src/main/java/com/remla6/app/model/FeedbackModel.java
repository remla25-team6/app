package com.remla6.app.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.SQLInsert;

@Entity
public class FeedbackModel {
    @Id
    private long id;

    private String review;
    private String correctSentiment;

    public FeedbackModel() {}

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public String getCorrectSentiment() {
        return correctSentiment;
    }

    public void setCorrectSentiment(String correctSentiment) {
        this.correctSentiment = correctSentiment;
    }

    public FeedbackModel(Long id, String review, String correctSentiment) {
        this.review = review;
        this.correctSentiment = correctSentiment;
        this.id = id;
    }
}
