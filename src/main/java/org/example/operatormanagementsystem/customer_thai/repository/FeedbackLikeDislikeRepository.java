package org.example.operatormanagementsystem.customer_thai.repository;

import org.example.operatormanagementsystem.entity.FeedbackLikeDislike;
import org.example.operatormanagementsystem.entity.Feedback;
import org.example.operatormanagementsystem.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface FeedbackLikeDislikeRepository extends JpaRepository<FeedbackLikeDislike, Long> {
    Optional<FeedbackLikeDislike> findByFeedbackAndUser(Feedback feedback, Users user);
    List<FeedbackLikeDislike> findByUser(Users user);
    List<FeedbackLikeDislike> findByFeedback(Feedback feedback);
} 