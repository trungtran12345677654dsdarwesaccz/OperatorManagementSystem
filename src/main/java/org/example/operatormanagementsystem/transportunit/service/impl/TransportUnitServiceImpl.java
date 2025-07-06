package org.example.operatormanagementsystem.transportunit.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.operatormanagementsystem.entity.Booking;
import org.example.operatormanagementsystem.entity.TransportUnit;
import org.example.operatormanagementsystem.enumeration.UserStatus;
import org.example.operatormanagementsystem.transportunit.dto.request.TransportUnitRequest;
import org.example.operatormanagementsystem.transportunit.dto.request.TransportUnitSearchRequest;
import org.example.operatormanagementsystem.transportunit.dto.response.TransportUnitResponse;
import org.example.operatormanagementsystem.transportunit.repository.TransportUnitRepository;
import org.example.operatormanagementsystem.transportunit.service.TransportUnitService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransportUnitServiceImpl implements TransportUnitService {

    private final TransportUnitRepository repository;


    /* --------- NEW ---------- */
    @Override
    public Page<TransportUnitResponse> getAllPaged(int page, int size,
                                                   String sortBy,
                                                   String dir) {
        Sort sort = Sort.by(sortBy);
        if ("desc".equalsIgnoreCase(dir)) sort = sort.descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return repository.findAll(pageable)          // Page<TransportUnit>
                .map(this::toResponse);         // Page<TransportUnitResponse>
    }


    private TransportUnitResponse toResponse(TransportUnit entity) {
        return TransportUnitResponse.builder()
                .transportId(entity.getTransportId())
                .nameCompany(entity.getNameCompany())
                .namePersonContact(entity.getNamePersonContact())
                .phone(entity.getPhone())
                .licensePlate(entity.getLicensePlate())
                .numberOfVehicles(entity.getNumberOfVehicles())
                .capacityPerVehicle(entity.getCapacityPerVehicle())
                .availabilityStatus(entity.getAvailabilityStatus())
                .certificateFrontUrl(entity.getCertificateFrontUrl())
                .certificateBackUrl(entity.getCertificateBackUrl())
                .status(entity.getStatus())
                .note(entity.getNote())
                .build();
    }

    @Override
    public List<TransportUnitResponse> getAll() {
        return repository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TransportUnitResponse> search(String keyword) {
        return repository
                .findByNameCompanyContainingIgnoreCaseOrNamePersonContactContainingIgnoreCaseOrPhoneContainingIgnoreCaseOrLicensePlateContainingIgnoreCase(
                        keyword, keyword, keyword, keyword
                )
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }


    @Override
    public List<TransportUnitResponse> searchAdvanced(TransportUnitSearchRequest req) {
        return repository.findAll()
                .stream()
                .filter(t -> {
                    boolean match = true;
                    if (req.getTransportId() != null) match &= t.getTransportId().equals(req.getTransportId());
                    if (req.getStatus() != null) match &= t.getStatus() == req.getStatus();
                    if (req.getNumberOfVehicles() != null)
                        match &= t.getNumberOfVehicles().equals(req.getNumberOfVehicles());

                    if (req.getMinCapacityPerVehicle() != null)
                        match &= t.getCapacityPerVehicle() >= req.getMinCapacityPerVehicle();

                    if (req.getMaxCapacityPerVehicle() != null)
                        match &= t.getCapacityPerVehicle() <= req.getMaxCapacityPerVehicle();

                    if (req.getAvailabilityStatus() != null)
                        match &= t.getAvailabilityStatus() == req.getAvailabilityStatus();

                    if (StringUtils.hasText(req.getKeyword())) {
                        String kw = req.getKeyword().toLowerCase();
                        match &= (
                                t.getNameCompany().toLowerCase().contains(kw) ||
                                        t.getNamePersonContact().toLowerCase().contains(kw) ||
                                        t.getPhone().toLowerCase().contains(kw) ||
                                        t.getLicensePlate().toLowerCase().contains(kw)
                        );
                    }
                    return match;
                })
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public TransportUnitResponse update(Integer id, TransportUnitRequest req) {
        TransportUnit unit = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Not found"));

        if (unit.getStatus() != UserStatus.ACTIVE) {
            throw new IllegalStateException("Only ACTIVE transport units can be updated.");
        }

        // Cập nhật tất cả các trường từ TransportUnitRequest
        unit.setNameCompany(req.getNameCompany());
        unit.setNamePersonContact(req.getNamePersonContact());
        unit.setPhone(req.getPhone());
        unit.setLicensePlate(req.getLicensePlate());
        unit.setStatus(req.getStatus());
        unit.setNote(req.getNote());
        unit.setNumberOfVehicles(req.getNumberOfVehicles());
        unit.setCapacityPerVehicle(req.getCapacityPerVehicle());
        unit.setAvailabilityStatus(req.getAvailabilityStatus()); // <-- Trường mới thêm
        return toResponse(repository.save(unit));
    }


    @Override
    public TransportUnitResponse getById(Integer id) {
        return repository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new RuntimeException("Not found"));
    }



    @Override
    public List<TransportUnitResponse> getByStatus(UserStatus status) {
        return repository.findByStatus(status)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

}
