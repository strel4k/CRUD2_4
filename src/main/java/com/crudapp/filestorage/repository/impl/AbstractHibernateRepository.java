package com.crudapp.filestorage.repository.impl;

import com.crudapp.filestorage.config.HibernateUtil;
import com.crudapp.filestorage.repository.CrudRepository;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;
import java.util.Optional;

public abstract class AbstractHibernateRepository <T, ID> implements CrudRepository <T, ID> {
    private final Class<T> entityClass;
    protected AbstractHibernateRepository(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    @Override
    public Optional<T> findById(ID id) {
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            return Optional.ofNullable(s.get(entityClass, id));
        }
    }

    @Override
    public List<T> findAll(int offset, int limit) {
        try (Session s = HibernateUtil.getSessionFactory().openSession()){
            return  s.createQuery("from " + entityClass.getSimpleName(), entityClass)
                    .setFirstResult(Math.max(0, offset))
                    .setMaxResults(Math.max(1, limit))
                    .list();
        }
    }

    @Override
    public T save(T entity) {
        Transaction tx = null;
        try (Session s = HibernateUtil.getSessionFactory().openSession()){
            tx = s.beginTransaction();
            s.persist(entity);
            tx.commit();
            return entity;
        } catch (RuntimeException e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    @Override
    public T update(T entity) {
        Transaction tx = null;
        try (Session s = HibernateUtil.getSessionFactory().openSession()){
            tx = s.beginTransaction();
            T merged = (T) s.merge(entity);
            tx.commit();
            return merged;
        } catch (RuntimeException e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    @Override
    public void deleteById(ID id) {
        Transaction tx = null;
        try (Session s = HibernateUtil.getSessionFactory().openSession()){
            tx = s.beginTransaction();
            T ref = s.get(entityClass, id);
            if (ref != null) s.remove(ref);
            tx.commit();
        } catch (RuntimeException e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    @Override
    public long count() {
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            return s.createQuery("select count(e) from " + entityClass.getSimpleName() + " e", Long.class)
                    .getSingleResult();
        }
    }
}
