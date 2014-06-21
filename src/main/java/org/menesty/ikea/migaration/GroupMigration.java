package org.menesty.ikea.migaration;

import org.menesty.ikea.db.DatabaseService;

import javax.persistence.Persistence;

/**
 * Created by Menesty on
 * 6/21/14.
 * 13:04.
 */
public class GroupMigration {

    public void startMigrate() {
       /* DatabaseService.runInTransaction(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                ServiceFacade.getProductService().changeGroup(ProductInfo.Group.Bathroom, ProductInfo.Group.BathroomStoring);
                ServiceFacade.getProductService().changeGroup(ProductInfo.Group.Storing, ProductInfo.Group.BathroomStoring);

                ServiceFacade.getProductService().changeGroup(ProductInfo.Group.Kids, ProductInfo.Group.FamilyKids);
                ServiceFacade.getProductService().changeGroup(ProductInfo.Group.Family, ProductInfo.Group.FamilyKids);

                return null;
            }
        });*/
    }

    public static void main(String... arg) {
        DatabaseService.setEntityManagerFactory(Persistence.createEntityManagerFactory("ikea"));
        new GroupMigration() {
        }.startMigrate();
    }
}
