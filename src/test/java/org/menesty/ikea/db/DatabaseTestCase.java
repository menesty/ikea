package org.menesty.ikea.db;

import org.junit.After;
import org.junit.Before;

import javax.persistence.Persistence;

/**
 * Created by Menesty
 * on 2/21/14.
 */
public class DatabaseTestCase {
    @Before
    public void setUpDatabase() {
        DatabaseService.entityManagerFactory = Persistence.createEntityManagerFactory("ikea-test");
        DatabaseService.initialized = 1;
    }

    @After
    public void close() {
        DatabaseService.close();
    }
}
