package org.example.operatormanagementsystem.transportunit.service;

import org.example.operatormanagementsystem.transportunit.dto.request.TransportUnitEmailRequest;
import org.example.operatormanagementsystem.transportunit.dto.response.TransportUnitResponse;

public interface TransportUnitOnboardingService {
    TransportUnitResponse onboardNewTransportUnit(TransportUnitEmailRequest request);
}