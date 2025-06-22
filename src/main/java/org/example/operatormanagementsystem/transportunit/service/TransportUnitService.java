package org.example.operatormanagementsystem.transportunit.service;

import org.example.operatormanagementsystem.enumeration.UserStatus;
import org.example.operatormanagementsystem.transportunit.dto.request.TransportUnitRequest;
import org.example.operatormanagementsystem.transportunit.dto.request.TransportUnitSearchRequest;
import org.example.operatormanagementsystem.transportunit.dto.response.TransportUnitResponse;
import org.springframework.stereotype.Service;

import java.util.List;

public interface TransportUnitService {
    List<TransportUnitResponse> getAll();
    List<TransportUnitResponse> search(String keyword);
    List<TransportUnitResponse> searchAdvanced(TransportUnitSearchRequest request);
    TransportUnitResponse update(Integer id, TransportUnitRequest request);
    TransportUnitResponse getById(Integer id);
    List<TransportUnitResponse> getByStatus(UserStatus status);
}
