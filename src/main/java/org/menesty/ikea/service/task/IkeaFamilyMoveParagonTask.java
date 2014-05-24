package org.menesty.ikea.service.task;

import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.menesty.ikea.db.DatabaseService;
import org.menesty.ikea.domain.CustomerOrder;
import org.menesty.ikea.domain.IkeaParagon;
import org.menesty.ikea.domain.InvoicePdf;
import org.menesty.ikea.processor.invoice.RawInvoiceProductItem;
import org.menesty.ikea.service.InvoicePdfService;
import org.menesty.ikea.service.ServiceFacade;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Menesty on
 * 5/23/14.
 */
public class IkeaFamilyMoveParagonTask extends BaseIkeaFamilyTask<Boolean> {
    private final IkeaParagon paragon;

    private final CustomerOrder customerOrder;

    private static final Logger logger = Logger.getLogger(IkeaFamilyMoveParagonTask.class.getName());

    public IkeaFamilyMoveParagonTask(IkeaParagon paragon, CustomerOrder customerOrder) {
        this.paragon = paragon;
        this.customerOrder = customerOrder;
    }

    @Override
    protected Boolean call() throws Exception {
        boolean result;
        try {
            DatabaseService.begin();

            try (CloseableHttpClient httpClient = HttpClients.custom()
                    .setUserAgent("Mozilla/5.0 (Macintosh; U; PPC Max OS X Mach-O; en-US; rv:1.8.0.7) Gecko/200609211 Camino/1.0.3")
                    .build()) {

                if (result = login(httpClient))
                    if (start(httpClient)) {
                        paragon.setUploaded(true);
                        ServiceFacade.getIkeaParagonService().save(paragon);
                    }

            }
            DatabaseService.commit();
        } catch (Exception e) {
            DatabaseService.rollback();
            result = false;

            logger.log(Level.SEVERE, "Ikea family parse site problem", e);
        }

        return result;
    }

    private boolean start(CloseableHttpClient httpClient) throws IOException {
        InvoicePdf invoicePdf = new InvoicePdf(customerOrder);

        invoicePdf.setParagonName(paragon.getName());
        invoicePdf.setParagonDate(paragon.getCreatedDate());
        invoicePdf.setPrice(paragon.getPrice());
        invoicePdf.setName("Ikea Family " + paragon.getName());

        //invoicePdf.setInvoiceNumber();

        Collection<ProductDto> items = getParagonItems(httpClient, RequestBuilder.get().setUri(paragon.getDetailUrl()).build());

        List<RawInvoiceProductItem> products = new ArrayList<>(items.size());

        for (ProductDto item : items) {
            RawInvoiceProductItem product = new RawInvoiceProductItem();

            product.setOriginalArtNumber(item.artNumber);
            product.setPrice(item.price);
            product.setCount(item.count);
            product.setName(item.description);
            product.setProductInfo(ServiceFacade.getInvoicePdfService().loadProductInfo(product));
            product.invoicePdf = invoicePdf;

            products.add(product);
        }

        if (products.size() != 0) {
            ServiceFacade.getInvoicePdfService().save(invoicePdf);
            products = InvoicePdfService.reduce(products);
            ServiceFacade.getInvoicePdfService().save(products);
        }

        return products.size() != 0;
    }
}
