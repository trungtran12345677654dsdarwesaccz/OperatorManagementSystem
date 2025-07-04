package org.example.operatormanagementsystem.dashboardstaff.repository;

import org.example.operatormanagementsystem.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackRepository extends JpaRepository<Feedback, Integer> {
    long countByProcessStatusIsNull();
}