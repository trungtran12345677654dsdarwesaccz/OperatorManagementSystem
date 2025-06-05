package org.example.operatormanagementsystem.controller;

import org.example.operatormanagementsystem.entity.StorageUnit;
import org.example.operatormanagementsystem.service.StorageUnitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/storage-units")
public class StorageUnitController {
    @Autowired
    private StorageUnitService service;



    @GetMapping
    public List<StorageUnit> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public StorageUnit getById(@PathVariable Long id) {
        return service.getById(id);
    }




    @PostMapping
    public StorageUnit add(@RequestBody StorageUnit su) {
        return service.add(su);
    }

    @PutMapping("/{id}")
    public StorageUnit update(@PathVariable Long id, @RequestBody StorageUnit su) {
        return service.update(id, su);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @GetMapping("/search")
    public List<StorageUnit> search(@RequestParam String keyword) {
        return service.search(keyword, keyword, keyword);
    }

}
