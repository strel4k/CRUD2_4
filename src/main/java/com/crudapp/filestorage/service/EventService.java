package com.crudapp.filestorage.service;

import com.crudapp.filestorage.model.Event;

import java.util.List;

public interface EventService {
    Event record(long userId, long fileId, String type);
    List<Event> forUser(long userId, int offset, int limit);
    List<Event> forFile(long fileId, int offset, int limit);
    long purgeForUser(long userId);
    long purgeForFile(long fileId);
    Event get(long id);
    Event create(long userId, long fileId, String eventType);
    Event updateType(long id, String eventType);
    boolean delete(long id);
}
