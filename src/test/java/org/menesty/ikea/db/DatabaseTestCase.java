package org.menesty.ikea.db;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.menesty.ikea.domain.ProductInfo;
import org.menesty.ikea.service.ProductService;
import org.menesty.ikea.service.ServiceFacade;

import javax.persistence.Persistence;
import java.io.IOException;
import java.util.List;

/**
 * Created by Menesty
 * on 2/21/14.
 */
public class DatabaseTestCase {
    @Before
    public void setUpDatabase() {
        DatabaseService.entityManagerFactory = Persistence.createEntityManagerFactory("ikea");
        DatabaseService.initialized = 1;
    }

    @Test
    public void test() throws IOException {

        ProductService ps = ServiceFacade.getProductService();

      //  DatabaseService.begin();

        ProductInfo productInfo = ps.loadComboProduct("S29929230");
        System.out.println(productInfo);
        /*List<ProductInfo> items = DatabaseService.getEntityManager().createQuery("select entity from ProductInfo entity " +
                " where entity.originalArtNum LIKE 'S%' and entity.parts IS EMPTY", ProductInfo.class).getResultList();


        for (ProductInfo productInfo : items) {
            try {
                ProductInfo combo = ps.loadComboProduct(productInfo.getOriginalArtNum());

                if (combo != null) {
                    productInfo.setParts(combo.getParts());
                    productInfo.setPrice(combo.getPrice());
                    ps.save(productInfo);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/
        //DatabaseService.commit();
    }

    @After
    public void close() {
        DatabaseService.close();
    }
}
