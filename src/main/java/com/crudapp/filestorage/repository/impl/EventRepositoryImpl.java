package com.crudapp.filestorage.repository.impl;

import com.crudapp.filestorage.config.HibernateUtil;
import com.crudapp.filestorage.model.Event;
import com.crudapp.filestorage.repository.EventRepository;
import org.hibernate.Session;

import java.util.List;

public class EventRepositoryImpl extends AbstractHibernateRepository<Event, Long> implements EventRepository {
    public EventRepositoryImpl() {
        super(Event.class);
    }

    @Override
    public List<Event> findByUserId(long userId, int offset, int limit) {
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            return s.createQuery("from Event e where e.user.id = :uid order by e.createdAt desc", Event.class)
                    .setParameter("uid", userId)
                    .setFirstResult(Math.max(0, offset))
                    .setMaxResults(Math.max(1, limit))
                    .list();
        }
    }

    @Override
    public List<Event> findByFileId(long fileId, int offset, int limit) {
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            return s.createQuery("from Event e where e.file.id = :fid order by e.createdAt desc", Event.class)
                    .setParameter("fid", fileId)
                    .setFirstResult(Math.max(0, offset))
                    .setMaxResults(Math.max(1, limit))
                    .list();
        }
    }
    @Override
    public long deleteByUserId(long userId) {
        try (var s = HibernateUtil.getSessionFactory().openSession()) {
            var tx = s.beginTransaction();
            int n = s.createMutationQuery("delete from Event e where e.user.id = :uid")
                    .setParameter("uid", userId)
                    .executeUpdate();
            tx.commit();
            return n;
        }
    }

    @Override
    public long deleteByFileId(long fileId) {
        try (var s = HibernateUtil.getSessionFactory().openSession()) {
            var tx = s.beginTransaction();
            int n = s.createMutationQuery("delete from Event e where e.file.id = :fid")
                    .setParameter("fid", fileId)
                    .executeUpdate();
            tx.commit();
            return n;
        }
    }

    @Override
    public Event findById(long id) {
        try (var s = HibernateUtil.getSessionFactory().openSession()) {
            return s.get(Event.class, id);
        }
    }

    @Override
    public Event save(Event e) {
        try (var s = HibernateUtil.getSessionFactory().openSession()) {
            var tx = s.beginTransaction();
            s.persist(e);
            tx.commit();
            return e;
        }
    }

    @Override
    public Event update(Event e) {
        try (var s = HibernateUtil.getSessionFactory().openSession()) {
            var tx = s.beginTransaction();
            s.merge(e);
            tx.commit();
            return e;
        }
    }

    @Override
    public boolean deleteById(long id) {
        try (var s = HibernateUtil.getSessionFactory().openSession()) {
            var tx = s.beginTransaction();
            Event e = s.get(Event.class, id);
            if (e == null) {
                tx.rollback();
                return false;
            }
            s.remove(e);
            tx.commit();
            return true;
        }
    }
}
