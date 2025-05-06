package com.remla6.app.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
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
}
