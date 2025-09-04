package com.crudapp.filestorage.repository.impl;

import com.crudapp.filestorage.config.HibernateUtil;
import com.crudapp.filestorage.model.User;
import com.crudapp.filestorage.repository.UserRepository;
import org.hibernate.Session;

import java.util.Optional;

public class UserRepositoryImpl extends AbstractHibernateRepository<User, Long> implements UserRepository {
    public UserRepositoryImpl() {
        super(User.class);
    }

    @Override
    public Optional<User> findByName(String name) {
        try (Session s = HibernateUtil.getSessionFactory().openSession()){
            return s.createQuery("from User u where u.name = :name", User.class)
                    .setParameter("name", name)
                    .uniqueResultOptional();
        }
    }
}
