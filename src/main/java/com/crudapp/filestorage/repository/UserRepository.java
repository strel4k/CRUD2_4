package com.crudapp.filestorage.repository;

import com.crudapp.filestorage.model.User;

import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Long> {
    Optional<User> findByName(String name);
}
