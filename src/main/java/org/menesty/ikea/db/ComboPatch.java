package org.menesty.ikea.db;

import org.menesty.ikea.domain.ProductInfo;
import org.menesty.ikea.domain.ProductPart;
import org.menesty.ikea.service.ProductService;

import javax.persistence.Persistence;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;

public class ComboPatch {
    public static void main(String... arg) throws IOException {
        DatabaseService.entityManagerFactory = Persistence.createEntityManagerFactory("ikea");
        DatabaseService.initialized = 1;

        final ProductService productService = new ProductService();


        List<ProductInfo> combos = productService.loadCombos();

        for (final ProductInfo combo : combos) {
            DatabaseService.runInTransaction(new Callable<Object>() {
                @Override
                public Object call() throws Exception {

                    ProductInfo cleanCombo = productService.loadComboProduct(combo.getOriginalArtNum());
                    boolean changed = false;

                    for (ProductPart part : cleanCombo.getParts()) {

                        for (ProductPart targetPart : combo.getParts())
                            if (part.getProductInfo().getOriginalArtNum().equals(targetPart.getProductInfo().getOriginalArtNum())) {
                                if (targetPart.getCount() != part.getCount()) {
                                    System.out.println(combo.getOriginalArtNum() + " " + targetPart.getCount() + " = " + part.getCount());
                                    targetPart.setCount(part.getCount());
                                    changed = true;
                                }
                                break;
                            }
                    }

                    if (changed)
                        productService.save(combo);

                    return null;
                }
            });
        }

        DatabaseService.close();
    }
}
