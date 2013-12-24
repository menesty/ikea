package org.menesty.ikea.db;


import javafx.concurrent.Task;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseService {

    private static final Logger logger = Logger.getLogger(DatabaseService.class.getName());

    private static ExecutorService databaseExecutor;

    private static int initialized = -1;

    private static EntityManagerFactory entityManagerFactory;

    private static EntityManager entityManager;

    public static Task<Void> init() {
        if (initialized >= 0)
            throw new RuntimeException("Already initialized or initialization in progress");
        initialized = 0;
        databaseExecutor = Executors.newFixedThreadPool(2, new DatabaseThreadFactory());

        final DBSetupTask setupTask = new DBSetupTask();
        databaseExecutor.submit(setupTask);
        return setupTask;
    }

    public static void begin() {
        entityManager.getTransaction().begin();
    }

    public static synchronized void commit() {
        entityManager.getTransaction().commit();
    }

    public static void rollback() {
        entityManager.getTransaction().rollback();
    }

    public static EntityManager getEntityManager() {
        return entityManager;
    }

    public static void close() {
        if (!isInitialized())
            throw new RuntimeException("Database not initialized");
        entityManager.close();
        entityManagerFactory.close();
        databaseExecutor.shutdown();
        logger.info("close db connections");
    }


    public static boolean isInitialized() {
        return initialized == 1;
    }

    public static boolean isActive() {
        return entityManager.getTransaction().isActive();
    }


    static class DatabaseThreadFactory implements ThreadFactory {
        static final AtomicInteger poolNumber = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable, "Database-Connection-" + poolNumber.getAndIncrement() + "-thread");
            thread.setDaemon(true);
            return thread;
        }
    }


    static class DBSetupTask extends Task<Void> {

        @Override
        protected Void call() throws Exception {
            logger.info("start initialize EntityManagerFactory ...");
            try {
                entityManagerFactory = Persistence.createEntityManagerFactory("ikea");
                entityManager = entityManagerFactory.createEntityManager();
                initialized = 1;
                logger.info("finish initialization EntityManagerFactory.");
            } catch (Exception e) {
                logger.log(Level.SEVERE, "db initialization problem", e);
            }
            return null;
        }
    }
}
