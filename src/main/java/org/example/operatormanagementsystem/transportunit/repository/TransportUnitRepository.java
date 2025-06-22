package org.example.operatormanagementsystem.transportunit.repository;

import org.example.operatormanagementsystem.entity.TransportUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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

}
