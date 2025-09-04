package com.crudapp.filestorage.service.impl;

import com.crudapp.filestorage.model.User;
import com.crudapp.filestorage.repository.UserRepository;
import com.crudapp.filestorage.repository.impl.UserRepositoryImpl;
import com.crudapp.filestorage.service.ServiceException;
import com.crudapp.filestorage.service.UserService;

import java.util.List;
import java.util.Optional;

public class UserServiceImpl implements UserService {
    private final UserRepository repo = new UserRepositoryImpl();

    @Override
    public User create(String name) {
        if (name == null || name.isBlank()) throw new ServiceException("User name is required");
        return repo.save(new User(name.trim()));
    }

    @Override
    public Optional<User> get(long id) {
        return repo.findById(id);
    }

    @Override
    public List<User> list(int offset, int limit) {
       return repo.findAll(offset, limit);
    }

    @Override
    public User rename(long id, String newName) {
        var u = repo.findById(id).orElseThrow(() -> new ServiceException("User not found: " + id));
        if (newName == null || newName.isBlank()) throw new ServiceException("New name is required");
        u.setName(newName.trim());
        return repo.update(u);
    }

    @Override
    public void delete(long id) {
        repo.deleteById(id);
    }
}
