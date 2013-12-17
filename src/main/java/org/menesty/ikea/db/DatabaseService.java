package org.menesty.ikea.db;


import org.menesty.ikea.domain.User;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.sql.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class DatabaseService {

    private static final Logger logger = Logger.getLogger(DatabaseService.class.getName());

    private static ExecutorService databaseExecutor;

    private static boolean initialized;

    public static void init() {
        databaseExecutor = Executors.newFixedThreadPool(1, new DatabaseThreadFactory());
        initialized = true;

        final EntityManagerFactory emf = Persistence.createEntityManagerFactory("ikea");
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        User test = new User();
        test.setLogin("Bla");
        em.persist(test);

        em.getTransaction().commit();
        em.close();
        emf.close();
    }

    public static void main(String... arg) {
        init();
    }

    public static boolean isInitialized() {
        return initialized;
    }

    public static void execute() {
        if (!isInitialized())
            throw new RuntimeException("Initialize database before run");

        try (Connection con = getConnection()) {


            Statement st = con.createStatement();

            ResultSet rs = st.executeQuery("select name from employee");

            //databaseExecutor.submit()
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
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

    private static Connection getConnection() throws ClassNotFoundException, SQLException {
        logger.info("Getting a database connection");
        Class.forName("org.h2.Driver");
        return DriverManager.getConnection("jdbc:h2:/data/db/ikea", "menesty", "ikea-desktop-1");
    }


}
/*
abstract class DBTask<T> extends Task<T> {
    DBTask() {
        setOnFailed(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent t) {
                logger.log(Level.SEVERE, null, getException());
            }
        });
    }
}

class DBSetupTask<Void> extends DBTask {
    @Override
    protected Void call() throws Exception {
        try (Connection con = getConnection()) {
            if (!schemaExists(con)) {
                createSchema(con);
                populateDatabase(con);
            }
        }
        return null;
    }

    private boolean schemaExists(Connection con) {
        logger.info("Checking for Schema existence");
        try {
            Statement st = con.createStatement();
            st.executeQuery("select count(*) from employee");
            logger.info("Schema exists");
        } catch (SQLException ex) {
            logger.info("Existing DB not found will create a new one");
            return false;
        }

        return true;
    }

    private void createSchema(Connection con) throws SQLException {
        logger.info("Creating schema");
        Statement st = con.createStatement();
        String table = "create table employee(id integer, name varchar(64))";
        st.executeUpdate(table);
        logger.info("Created schema");
    }

    private void populateDatabase(Connection con) throws SQLException {
        logger.info("Populating database");
        Statement st = con.createStatement();
        for (String name : SAMPLE_NAME_DATA) {
            st.executeUpdate("insert into employee values(1,'" + name + "')");
        }
        logger.info("Populated database");
    }
}*/
