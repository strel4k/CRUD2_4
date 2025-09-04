package com.crudapp.filestorage.service.impl;

import com.crudapp.filestorage.config.HibernateUtil;
import com.crudapp.filestorage.config.StorageConfig;
import com.crudapp.filestorage.model.FileEntity;
import com.crudapp.filestorage.repository.FileRepository;
import com.crudapp.filestorage.repository.impl.FileRepositoryImpl;
import com.crudapp.filestorage.service.FileService;
import com.crudapp.filestorage.service.ServiceException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class FileServiceImpl implements FileService {
    private final FileRepository repo = new FileRepositoryImpl();
    private final Path root = Path.of(StorageConfig.storageRoot());

    @Override
    public FileEntity upload(String originalName, InputStream content) {
        if (originalName == null || originalName.isBlank()) throw new ServiceException("File name is required");
        try {
            Files.createDirectories(root);
            String safeName = originalName.strip();
            String ext = "";
            int dot = safeName.lastIndexOf('.');
            if (dot > 0 && dot < safeName.length() - 1) ext = safeName.substring(dot);
            String storedName = UUID.randomUUID() + ext;           // имя на диске
            Path target = root.resolve(storedName);

            try (InputStream in = content) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }

            FileEntity fe = new FileEntity(safeName, target.toString());
            return repo.save(fe);
        } catch (IOException e) {
            throw new ServiceException("Failed to store file", e);
        }
    }

    @Override
    public FileEntity rename(long id, String newName) {
        if(newName == null || newName.trim().isEmpty()) {
            throw new IllegalArgumentException("New name must not be empty");
        }
        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            var tx = session.beginTransaction();

            FileEntity entity = session.get(FileEntity.class, id);
            if (entity == null) {
                tx.rollback();
                throw  new IllegalArgumentException("File not found: " + id);
            }
            entity.setName(newName.trim());
            session.merge(entity);

            tx.commit();
            return entity;
        }
    }

    @Override public Optional<FileEntity> get(long id) { return repo.findById(id); }

    @Override public List<FileEntity> list(int offset, int limit) { return repo.findAll(offset, limit); }

    @Override public Path resolvePath(FileEntity file) { return Path.of(file.getFilePath()); }

    @Override
    public byte[] download(long id) {
        FileEntity fe = repo.findById(id).orElseThrow(() -> new ServiceException("File not found: " + id));
        Path p = resolvePath(fe);
        try {
            return Files.readAllBytes(p);
        } catch (IOException e) {
            throw new ServiceException("Failed to read file content", e);
        }
    }

    @Override
    public void delete(long id) {
        FileEntity fe = repo.findById(id).orElseThrow(() -> new ServiceException("File not found: " + id));
        Path p = resolvePath(fe);
        try { Files.deleteIfExists(p); } catch (IOException ignore) {}
        repo.deleteById(id);
    }
}
