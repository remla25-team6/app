package com.remla6.app.repository;

import com.remla6.app.model.SentimentModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SentimentRepository extends JpaRepository<SentimentModel, Long> {

    public SentimentModel save(SentimentModel sentimentModel);

    Optional<SentimentModel> findByInputString(String inputString);
}
