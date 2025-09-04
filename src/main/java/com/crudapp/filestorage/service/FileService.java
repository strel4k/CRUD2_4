package com.crudapp.filestorage.service;

import com.crudapp.filestorage.model.FileEntity;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public interface FileService {
    FileEntity upload(String originalName, InputStream content);
    FileEntity rename(long id, String newName);
    Optional<FileEntity> get(long id);
    List<FileEntity> list(int offset, int limit);
    Path resolvePath(FileEntity file);
    byte[] download(long id);
    void delete(long id);
}
