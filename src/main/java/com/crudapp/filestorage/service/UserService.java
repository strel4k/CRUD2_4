package com.crudapp.filestorage.service;

import com.crudapp.filestorage.model.User;
import java.util.List;
import java.util.Optional;

public interface UserService {
    User create(String name);
    Optional<User> get(long id);
    List<User> list(int offset, int limit);
    User rename (long id, String newName);
    void delete(long id);
}
