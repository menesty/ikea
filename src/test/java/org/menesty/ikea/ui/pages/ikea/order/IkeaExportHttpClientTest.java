package org.menesty.ikea.ui.pages.ikea.order;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.menesty.ikea.domain.User;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by Menesty on
 * 10/4/15.
 * 18:51.
 */
public class IkeaExportHttpClientTest {
    private User user;
    private IkeaExportHttpClient client;

    @Before
    public void setUp() throws IOException {
        user = new User("kor1@gmail.com", "Mature65");
        client = new IkeaExportHttpClient();
        client.login(user);
        client.deleteCategories();
        client.logout();

    }

    @After
    public void close() throws IOException {
        client.close();
    }

    @Test
    public void testLogin() throws IOException {
        client.login(user);
        client.logout();
    }

    @Test
    public void testDelete() throws IOException {
        try (IkeaExportHttpClient client = new IkeaExportHttpClient()) {
            client.login(user);

            List<String> categoryIds = client.getCategories();

            assertEquals(0, categoryIds.size());
        }
    }

}