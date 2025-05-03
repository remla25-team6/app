package com.remla6.app.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

@Entity
public class SentimentModel {

    @Id
    @GeneratedValue
    private Long id;

    private String inputString;
    private String sentiment;

    public SentimentModel() {}

    public SentimentModel(String inputString, String sentiment) {
        this.inputString = inputString;
        this.sentiment = sentiment;
    }

    public String getInputString() {
        return inputString;
    }

    public void setInputString(String inputString) {
        this.inputString = inputString;
    }

    public String getSentiment() {
        return sentiment;
    }

    public void setSentiment(String sentiment) {
        this.sentiment = sentiment;
    }
}
