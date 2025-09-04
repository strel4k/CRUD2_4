package com.crudapp.filestorage.repository.impl;

import com.crudapp.filestorage.config.HibernateUtil;
import com.crudapp.filestorage.model.FileEntity;
import com.crudapp.filestorage.repository.FileRepository;
import org.hibernate.Session;

import java.util.Optional;

public class FileRepositoryImpl extends AbstractHibernateRepository<FileEntity, Long> implements FileRepository {
    public FileRepositoryImpl() {
        super(FileEntity.class);
    }
    @Override
    public Optional<FileEntity> findByPath(String filePath) {
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            return s.createQuery("from FileEntity f where f.filePath = :p", FileEntity.class)
                    .setParameter("p", filePath)
                    .uniqueResultOptional();
        }
    }
}
