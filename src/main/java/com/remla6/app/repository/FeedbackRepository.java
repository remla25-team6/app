package com.remla6.app.repository;

import com.remla6.app.model.FeedbackModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedbackRepository extends JpaRepository<FeedbackModel, Long> {
}
