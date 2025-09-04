package com.crudapp.filestorage.service.impl;

import com.crudapp.filestorage.config.HibernateUtil;
import com.crudapp.filestorage.model.Event;
import com.crudapp.filestorage.model.FileEntity;
import com.crudapp.filestorage.model.User;
import com.crudapp.filestorage.repository.EventRepository;
import com.crudapp.filestorage.repository.FileRepository;
import com.crudapp.filestorage.repository.UserRepository;
import com.crudapp.filestorage.repository.impl.EventRepositoryImpl;
import com.crudapp.filestorage.repository.impl.FileRepositoryImpl;
import com.crudapp.filestorage.repository.impl.UserRepositoryImpl;
import com.crudapp.filestorage.service.EventService;
import com.crudapp.filestorage.service.ServiceException;

import java.util.List;

public class EventServiceImpl implements EventService {
    private final EventRepository events = new EventRepositoryImpl();
    private final UserRepository users = new UserRepositoryImpl();
    private final FileRepository files = new FileRepositoryImpl();
    private String normalizeAndValidateType(String t) {
        if (t == null) throw new IllegalArgumentException("eventType is required");
        String up = t.trim().toUpperCase();
        if (!up.equals("UPLOAD") && !up.equals("DOWNLOAD")) {
            throw new IllegalArgumentException("Invalid eventType: " + t + " (allowed: UPLOAD, DOWNLOAD)");
        }
        return up;
    }

    @Override
    public Event record(long userId, long fileId, String type) {
        User u = users.findById(userId).orElseThrow(() -> new ServiceException("User not found: " + userId));
        FileEntity f = files.findById(fileId).orElseThrow(() -> new ServiceException("File not found: " + fileId));
        String t = (type == null || type.isBlank()) ? null : type.trim();
        return events.save(new Event(u, f, t));
    }

    @Override
    public List<Event> forUser(long userId, int offset, int limit) {
        return events.findByUserId(userId, offset, limit);
    }

    @Override
    public List<Event> forFile(long fileId, int offset, int limit) {
        return events.findByFileId(fileId, offset, limit);
    }

    @Override
    public long purgeForUser(long userId) {
        return events.deleteByUserId(userId);
    }
    @Override public long purgeForFile(long fileId) {
        return events.deleteByFileId(fileId);
    }
    @Override
    public Event get(long id) {
        Event e = events.findById(id);
        if (e == null) throw new IllegalArgumentException("Event not found: " + id);
        return e;
    }

    @Override
    public Event create(long userId, long fileId, String eventType) {
        if (userId <= 0 || fileId <= 0) throw new IllegalArgumentException("userId and fileId must be positive");

        try (var s = HibernateUtil.getSessionFactory().openSession()) {
            var user = s.get(User.class, userId);
            if (user == null) throw new IllegalArgumentException("User not found: " + userId);
            var file = s.get(FileEntity.class, fileId);
            if (file == null) throw new IllegalArgumentException("File not found: " + fileId);

            String type = normalizeAndValidateType(eventType);

            Event e = new Event(user, file, type);
            return events.save(e);
        }
    }

    @Override
    public Event updateType(long id, String eventType) {
        String type = normalizeAndValidateType(eventType);
        Event e = events.findById(id);
        if (e == null) throw new IllegalArgumentException("Event not found: " + id);
        e.setEventType(type);
        return events.update(e);
    }

    @Override
    public boolean delete(long id) {
        return events.deleteById(id);
    }
}
