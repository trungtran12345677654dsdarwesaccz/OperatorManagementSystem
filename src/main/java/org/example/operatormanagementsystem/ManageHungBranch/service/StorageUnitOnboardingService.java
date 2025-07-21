package org.example.operatormanagementsystem.ManageHungBranch.service;

import org.example.operatormanagementsystem.ManageHungBranch.dto.request.StorageUnitEmailRequest;
import org.example.operatormanagementsystem.ManageHungBranch.dto.response.StorageUnitResponse;

public interface StorageUnitOnboardingService {
    StorageUnitResponse onboardNewStorageUnit(StorageUnitEmailRequest request);

}
