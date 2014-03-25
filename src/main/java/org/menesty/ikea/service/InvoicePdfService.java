package org.menesty.ikea.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import org.menesty.ikea.domain.CustomerOrder;
import org.menesty.ikea.domain.InvoicePdf;
import org.menesty.ikea.domain.ProductInfo;
import org.menesty.ikea.exception.ProductFetchException;
import org.menesty.ikea.processor.invoice.RawInvoiceProductItem;
import org.menesty.ikea.util.NumberUtil;

import javax.persistence.TypedQuery;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: Menesty
 * Date: 10/6/13
 * Time: 11:42 AM
 */
public class InvoicePdfService extends Repository<InvoicePdf> {
    private static Logger logger = Logger.getLogger(InvoicePdfService.class.getName());

    private final static Pattern linePattern = Pattern.compile("(\\d+) (\\d{3}-\\d{3}-\\d{2}) (.*) (SZT\\.|CM\\.) (\\d{0,},{0,}\\d+) (\\S+) (\\d+,\\d+%) (\\S+,\\d{2})");

    private final static String INVOICE_NAME_PATTERN = "(\\d+)\\s+(\\d+)/\\s+(\\w+)\\s+/F A K T U R A.*";

    private final static Pattern totalPattern = Pattern.compile("DO ZAPŁATY:(.*)");

    private ProductService productService;

    public InvoicePdfService() {
        productService = new ProductService();
    }

    private List<RawInvoiceProductItem> parseInvoiceItems(final InvoicePdf invoicePdf, final InputStream stream) throws IOException {

        PDDocument p = PDDocument.load(stream);

        PDFTextStripper t = new PDFTextStripper();
        String content = t.getText(p);
        p.close();

        Scanner scanner = new Scanner(content);

        List<RawInvoiceProductItem> products = new ArrayList<>();


        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            Matcher m = linePattern.matcher(line);
            if (m.find()) {
                RawInvoiceProductItem product = new RawInvoiceProductItem();
                product.setOriginalArtNumber(m.group(2).replace("-", ""));
                product.setName(m.group(3));

                BigDecimal count = BigDecimal.valueOf(NumberUtil.parse(m.group(5)));

                if ("CM.".equals(m.group(4)))
                    count = count.divide(BigDecimal.valueOf(100));

                product.setCount(count.doubleValue());

                double price = Double.valueOf(m.group(6).trim().replaceAll("[\\s\\u00A0]+", "").replace(",", "."));

                product.setPrice(price);
                product.setWat(m.group(7));
                product.setProductInfo(loadProductInfo(product));
                product.invoicePdf = invoicePdf;
                products.add(product);
            } else {
                if (line.matches(INVOICE_NAME_PATTERN))
                    invoicePdf.setInvoiceNumber(line.replaceAll(INVOICE_NAME_PATTERN, "$1/$3/$2"));
                else
                    System.out.println("!!!!!" + line);
            }
        }

        Matcher m = totalPattern.matcher(content);

        if (m.find()) {
            double price = Double.valueOf(m.group(1).trim().replaceAll("[\\s\\u00A0]+", "").replace(",", "."));
            invoicePdf.setPrice(price);
        }
        return products;
    }

    public static void main(String... arg) throws IOException, InterruptedException, ExecutionException {
        InvoicePdfService service = new InvoicePdfService();
        try {
            service.parseInvoiceItems(new InvoicePdf(), new FileInputStream("D:\\tmp\\33654 katow.pdf"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private InvoicePdf parseInvoice(final CustomerOrder order, final String name, final InputStream stream) throws IOException {
        try {
            begin();
            InvoicePdf result = new InvoicePdf(name);
            result.customerOrder = order;

            List<RawInvoiceProductItem> products = parseInvoiceItems(result, stream);

            result = save(result);

            List<RawInvoiceProductItem> items = reduce(products);

            result.setProducts(save(items));

            return result;
        } catch (Exception e) {
            rollback();
            logger.log(Level.SEVERE, "Parse Invoice pdf problem", e);
            return null;
        } finally {
            commit();
        }

    }

    public static List<RawInvoiceProductItem> reduce(List<RawInvoiceProductItem> items) {
        Map<String, RawInvoiceProductItem> filtered = new HashMap<>();

        for (RawInvoiceProductItem item : items) {
            RawInvoiceProductItem current = filtered.get(item.getOriginalArtNumber());
            if (current == null)
                filtered.put(item.getOriginalArtNumber(), item);
            else
                current.setCount(NumberUtil.round(current.getCount() + item.getCount()));
        }
        return new ArrayList<>(filtered.values());

    }

    private ProductInfo loadProductInfo(RawInvoiceProductItem product) {
        ProductInfo productInfo = null;
        try {
            productInfo = productService.loadOrCreate(product.getOriginalArtNumber());
        } catch (ProductFetchException e) {
            System.out.println("Problem with open product : " + product.getPrepareArtNumber());
        }
        if (productInfo == null) {
            productInfo = new ProductInfo();
            productInfo.setOriginalArtNum(product.getOriginalArtNumber());
            productInfo.setName(product.getName());
            productInfo.setShortName(ProductService.generateShortName(product.getName(), product.getPrepareArtNumber(), 1));
            productInfo.getPackageInfo().setBoxCount(1);
            productInfo = save(productInfo);
        }
        productInfo.setWat(product.getIntWat());
        productInfo.setPrice(product.getPrice());
        return productInfo;
    }

    public InvoicePdf createInvoicePdf(final CustomerOrder order, String orderName, InputStream is) {
        try {
            return parseInvoice(order, orderName, is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<InvoicePdf> createInvoicePdf(final CustomerOrder order, List<File> files) {
        List<InvoicePdf> result = new ArrayList<>();
        for (File file : files)
            try {
                result.add(createInvoicePdf(order, file.getName(), Files.newInputStream(file.toPath())));
            } catch (IOException e) {
                e.printStackTrace();
            }
        return result;
    }

    public void remove(InvoicePdf entity) {
        boolean started = isActive();
        try {
            if (!started)
                begin();

            ServiceFacade.getInvoiceItemService().deleteBy(entity);
            super.remove(entity.getProducts(), RawInvoiceProductItem.class);
            super.remove(entity);
            if (!started)
                commit();
        } catch (Exception e) {
            rollback();
            e.printStackTrace();
        }
    }

    public void removeAll(List<InvoicePdf> entities) {
        boolean started = isActive();
        if (!started)
            begin();

        for (InvoicePdf item : entities)
            this.remove(item);

        if (!started)
            commit();
    }


    public List<InvoicePdf> loadBy(CustomerOrder order) {
        boolean started = isActive();
        try {
            if (!started)
                begin();

            TypedQuery<InvoicePdf> query = getEm().createQuery("select entity from " + entityClass.getName() + " entity where entity.customerOrder.id = ?1", entityClass);
            query.setParameter(1, order.getId());
            return query.getResultList();

        } finally {
            if (!started)
                commit();
        }

    }

    public void removeBy(CustomerOrder order) {
        boolean started = isActive();
        if (!started)
            begin();
        removeAll(loadBy(order));
        if (!started)
            commit();
    }

    public List<RawInvoiceProductItem> loadRawInvoiceItemBy(CustomerOrder order) {
        boolean started = isActive();
        try {
            if (!started)
                begin();
            TypedQuery<RawInvoiceProductItem> query = getEm().createQuery("select entity from " + RawInvoiceProductItem.class.getName() + " entity left join entity.invoicePdf invoice  where invoice.customerOrder.id = ?1", RawInvoiceProductItem.class);
            query.setParameter(1, order.getId());
            return query.getResultList();
        } finally {
            if (!started)
                commit();
        }
    }
}


