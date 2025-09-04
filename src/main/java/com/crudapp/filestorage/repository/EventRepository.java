package com.crudapp.filestorage.repository;

import com.crudapp.filestorage.model.Event;

import java.util.List;

public interface EventRepository extends CrudRepository<Event, Long> {
    List<Event> findByUserId(long userId, int offset, int limit);
    List<Event> findByFileId(long fileId, int offset, int limit);
    long deleteByUserId(long userId);
    long deleteByFileId(long fileId);
    Event findById(long id);
    Event save(Event e);
    Event update(Event e);
    boolean deleteById(long id);
}
