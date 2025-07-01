    package org.example.operatormanagementsystem.transportunit.repository;

    import org.example.operatormanagementsystem.entity.TransportUnit;
    import org.example.operatormanagementsystem.enumeration.UserStatus;
    import org.example.operatormanagementsystem.transportunit.dto.response.HistoricalDataResponse;
    import org.springframework.data.jpa.repository.JpaRepository;
    import org.springframework.data.jpa.repository.Query;
    import org.springframework.data.repository.query.Param;
    import org.springframework.stereotype.Repository;

    import java.time.LocalDate;
    import java.util.List;

    @Repository
    public interface TransportUnitRepository extends JpaRepository<TransportUnit, Integer> {

        List<TransportUnit> findByNameCompanyContainingIgnoreCaseOrNamePersonContactContainingIgnoreCaseOrPhoneContainingIgnoreCaseOrLicensePlateContainingIgnoreCase(
                String nameCompany,
                String namePersonContact,
                String phone,
                String licensePlate
        );


        List<TransportUnit> findByStatus(org.example.operatormanagementsystem.enumeration.UserStatus status);
        int countByStatus(UserStatus status);

        @Query("SELECT COUNT(t) FROM TransportUnit t")
        int countAll();

        @Query("SELECT t.status, COUNT(t) FROM TransportUnit t GROUP BY t.status")
        List<Object[]> getStatusCounts();

        @Query(
                value = "SELECT " +
                        "DATE_FORMAT(tua.processed_at, '%Y-%m') AS period, " +
                        "SUM(CASE WHEN tu.status = 'PENDING_APPROVAL' THEN 1 ELSE 0 END) AS pending, " +
                        "SUM(CASE WHEN tu.status = 'ACTIVE' THEN 1 ELSE 0 END) AS active, " +
                        "SUM(CASE WHEN tu.status = 'INACTIVE' THEN 1 ELSE 0 END) AS inactive, " +
                        "SUM(CASE WHEN tua.status = 'APPROVED' THEN 1 ELSE 0 END) AS totalApprovals, " +
                        "SUM(CASE WHEN tua.status = 'REJECTED' THEN 1 ELSE 0 END) AS totalRejections " +
                        "FROM transport_unit tu " +
                        "LEFT JOIN transport_unit_approval tua ON tua.transport_unit_id = tu.transport_id " +
                        "WHERE tua.processed_at BETWEEN :start AND :end " +
                        "GROUP BY DATE_FORMAT(tua.processed_at, '%Y-%m') " +
                        "ORDER BY period ASC",
                nativeQuery = true
        )
        List<HistoricalDataResponse> getHistoricalDataMonthly(
                @Param("start") LocalDate start,
                @Param("end") LocalDate end
        );


        @Query(
                value = "SELECT " +
                        "DATE_FORMAT(t.created_at, '%x-W%v') AS period, " +
                        "SUM(CASE WHEN t.status = 'PENDING_APPROVAL' THEN 1 ELSE 0 END) AS pending, " +
                        "SUM(CASE WHEN t.status = 'ACTIVE' THEN 1 ELSE 0 END) AS active, " +
                        "SUM(CASE WHEN t.status = 'INACTIVE' THEN 1 ELSE 0 END) AS inactive, " +
                        "0 AS totalApprovals, " +
                        "0 AS totalRejections " +
                        "FROM transport_unit t " +
                        "WHERE t.created_at BETWEEN :start AND :end " +
                        "GROUP BY DATE_FORMAT(t.created_at, '%x-W%v') " +
                        "ORDER BY DATE_FORMAT(t.created_at, '%x-W%v') ASC",
                nativeQuery = true
        )
        List<HistoricalDataResponse> getHistoricalDataWeekly(
                @Param("start") LocalDate start,
                @Param("end") LocalDate end
        );
    }

