package org.example.operatormanagementsystem.service;

import org.example.operatormanagementsystem.entity.StorageUnit;
import org.example.operatormanagementsystem.repository.StorageUnitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StorageUnitService {
    @Autowired
    private StorageUnitRepository repository;

    public List<StorageUnit> search(String keyword) {
        return repository.findByNameContainingOrAddressContainingOrStatusContaining(keyword, keyword, keyword);
    }


    public List<StorageUnit> getAll() {
        return repository.findAll();
    }

    public StorageUnit getById(Long id) {
        return repository.findById(id).orElse(null);
    }

    public StorageUnit add(StorageUnit su) {
        su.setStatus("ACTIVE");
        return repository.save(su);
    }



    public StorageUnit update(Long id, StorageUnit su) {
        StorageUnit exist = repository.findById(id).orElse(null);
        if (exist != null) {
            exist.setName(su.getName());
            exist.setAddress(su.getAddress());
            exist.setStatus(su.getStatus());
            return repository.save(exist);
        }
        return null;
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public List<StorageUnit> search(String name, String address, String status) {
        return repository.findByNameContainingOrAddressContainingOrStatusContaining(name, address, status);
    }
}
