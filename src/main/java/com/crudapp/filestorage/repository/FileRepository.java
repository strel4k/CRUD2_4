package com.crudapp.filestorage.repository;

import com.crudapp.filestorage.model.FileEntity;

import java.util.Optional;

public interface FileRepository extends CrudRepository<FileEntity, Long> {
    Optional<FileEntity> findByPath(String filePath);
}