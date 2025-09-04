package com.crudapp.filestorage.repository;

import java.util.List;
import java.util.Optional;

public interface CrudRepository<T, ID> {
    Optional<T> findById(ID id);
    List<T> findAll(int offset, int limit);
    T save(T entity);
    T update(T entity);
    void deleteById(ID id);
    long count();
}
