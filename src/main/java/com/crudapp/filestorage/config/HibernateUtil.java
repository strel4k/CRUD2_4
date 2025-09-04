package com.crudapp.filestorage.config;

import com.crudapp.filestorage.model.Event;
import com.crudapp.filestorage.model.FileEntity;
import com.crudapp.filestorage.model.User;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

import java.util.Locale;
import java.util.Properties;

public final class HibernateUtil {
    private static volatile SessionFactory SESSION_FACTORY;

    private HibernateUtil() {}

    public static SessionFactory getSessionFactory() {
        SessionFactory local = SESSION_FACTORY;
        if (local == null) {
            synchronized (HibernateUtil.class) {
                local = SESSION_FACTORY;
                if (local == null) {
                    SESSION_FACTORY = local = buildSessionFactory();
                    Runtime.getRuntime().addShutdownHook(new Thread(HibernateUtil::shutdown, "hibernate-shutdown"));
                }
            }
        }
        return local;
    }

    private static SessionFactory buildSessionFactory() {
        String url  = getenv("DB_URL", "jdbc:mysql://localhost:3306/filestore?useSSL=false&serverTimezone=UTC");
        String user = getenv("DB_USER", "root");
        String pass = getenv("DB_PASS", "password");

        Properties p = new Properties();
        p.put("hibernate.connection.url", url);
        p.put("hibernate.connection.username", user);
        p.put("hibernate.connection.password", pass);

        p.put("hibernate.connection.driver_class", "com.mysql.cj.jdbc.Driver");

        p.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        p.put("hibernate.hbm2ddl.auto", "validate");

        p.put("hibernate.show_sql", "false");
        p.put("hibernate.format_sql", "true");
        p.put("hibernate.jdbc.time_zone", "UTC");
        p.put("hibernate.connection.autocommit", "false");

        StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .applySettings(p)
                .build();

        try {
            MetadataSources sources = new MetadataSources(registry)
                    .addAnnotatedClass(User.class)
                    .addAnnotatedClass(FileEntity.class)
                    .addAnnotatedClass(Event.class);

            return sources.buildMetadata().buildSessionFactory();
        } catch (Exception ex) {
            StandardServiceRegistryBuilder.destroy(registry);
            throw new RuntimeException("Failed to build SessionFactory", ex);
        }
    }

    public static void shutdown() {
        SessionFactory sf = SESSION_FACTORY;
        if (sf != null && !sf.isClosed()) {
            sf.close();
        }
    }

    private static String getenv(String key, String defVal) {
        String v = System.getenv(key);
        return (v == null || v.isBlank()) ? defVal : v.trim();
    }
}