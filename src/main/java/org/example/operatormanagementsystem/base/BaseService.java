package org.example.operatormanagementsystem.base;

import java.util.List;
import java.util.Optional;

public interface BaseService<T, ID> {
    List<T> findAll();
    Optional<T> findById(ID id);
    T save(T entity);
    void deleteById(ID id);
}