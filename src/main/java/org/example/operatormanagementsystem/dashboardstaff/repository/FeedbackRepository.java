package org.example.operatormanagementsystem.dashboardstaff.repository;

import org.example.operatormanagementsystem.entity.Feedback;
import org.example.operatormanagementsystem.entity.OperatorStaff;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Integer> {
    List<Feedback> findByOperatorStaff(OperatorStaff operatorStaff);
}