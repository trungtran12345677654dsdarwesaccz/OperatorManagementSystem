package org.example.operatormanagementsystem.managestaff_yen.repository.DashboardRepo;

import org.example.operatormanagementsystem.entity.IssueLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IssueLogRepository extends JpaRepository<IssueLog, Integer> {

    List<IssueLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
