package org.menesty.ikea.service;

import org.jxls.common.Context;
import org.jxls.util.JxlsHelper;
import org.menesty.ikea.domain.CustomerOrder;
import org.menesty.ikea.processor.invoice.InvoiceItem;
import org.menesty.ikea.processor.invoice.RawInvoiceProductItem;
import org.mvel2.integration.VariableResolverFactory;
import org.mvel2.integration.impl.MapVariableResolverFactory;
import org.mvel2.templates.TemplateRuntime;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InvoiceService {

    private final static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

    private static final Logger logger = Logger.getLogger(InvoiceService.class.getName());

    public void exportToEpp(BigDecimal margin, String invoiceName, List<InvoiceItem> items, String fileName) {
        String templateFile = "/config/invoice-order.epp";
        StringBuilder text = new StringBuilder();
        String NL = System.getProperty("line.separator");

        try (Scanner scanner = new Scanner(getClass().getResourceAsStream(templateFile), "ISO-8859-2")) {
            while (scanner.hasNextLine())
                text.append(scanner.nextLine()).append(NL);

            String template = new String(text.toString().getBytes("utf8"));
            Map<String, Object> map = new HashMap<>();

            //calculate total
            BigDecimal totalPrice = BigDecimal.ZERO;

            double retailPercentage = 0.02;

            if (margin != null && !BigDecimal.ZERO.equals(margin)) {
                retailPercentage = margin.divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
            }

            for (InvoiceItem item : items) {
                item.setRetailPercentage(retailPercentage);
                totalPrice = totalPrice.add(item.getTotalWatPrice());
            }

            InvoiceItem totalItem = new InvoiceItem();
            totalItem.setCount(1);
            totalItem.setPrice(totalPrice.doubleValue());

            map.put("totalItem", totalItem);
            map.put("invoiceItems", items);
            map.put("createDate", sdf.format(new Date()));
            map.put("invoiceName", invoiceName);

            VariableResolverFactory vrf = new MapVariableResolverFactory(map);
            String result = (String) TemplateRuntime.eval(template, null, vrf);

            if (!fileName.endsWith(".epp"))
                fileName += ".epp";

            //new BufferedOutputStream(new ObjectOutputStream())
            new FileOutputStream(fileName).write(result.getBytes("ISO-8859-2"));

        } catch (IOException e) {
            logger.log(Level.SEVERE, "exportToEpp", e);
        }
    }

    public void exportToXls(List<RawInvoiceProductItem> items, String path) {
        Context bean = new Context();
        bean.putVar("items", items);

        if (!path.endsWith(".xlsx"))
            path = path.concat(".xlsx");

        try (InputStream in = getClass().getResourceAsStream("/config/invoice.xlsx")) {
            try (OutputStream os = Files.newOutputStream(FileSystems.getDefault().getPath(path))) {
                JxlsHelper.getInstance().processTemplate(in, os, bean);
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "exportToXls", e);
        }
    }

    public void exportToEpp(CustomerOrder order, String filePath) {
        List<InvoiceItem> items = ServiceFacade.getInvoiceItemService().loadBy(order);
        int index = 0;

        for (InvoiceItem item : items)
            item.setIndex(++index);

        exportToEpp(BigDecimal.valueOf(order.getMargin()), order.getName(), items, filePath);
        exportToXls(order.getName(), items, filePath);
    }

    private void exportToXls(String invoiceName, List<InvoiceItem> items, String path) {
        Context bean = new Context();
        bean.putVar("items", items);

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        bean.putVar("date", sdf.format(new Date()));

        if (!path.endsWith(".xls"))
            path = path.concat(".xls");

        try (InputStream in = getClass().getResourceAsStream("/config/outgoing-facture.xlsx")) {
            try (OutputStream os = Files.newOutputStream(FileSystems.getDefault().getPath(path), StandardOpenOption.CREATE_NEW)) {
                JxlsHelper.getInstance().processTemplate(in, os, bean);
            }

        } catch (IOException e) {
            logger.log(Level.SEVERE, "exportToXls", e);
        }
    }
}
