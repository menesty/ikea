package org.menesty.ikea.service;

import org.menesty.ikea.processor.invoice.InvoiceItem;
import org.menesty.ikea.processor.invoice.RawInvoiceProductItem;
import org.mvel.integration.VariableResolverFactory;
import org.mvel.integration.impl.MapVariableResolverFactory;
import org.mvel.templates.TemplateRuntime;

import java.io.*;
import java.util.*;

public class InvoiceService {

    public void exportToEpp(List<RawInvoiceProductItem> items, String fileName) {
        String templateFile = "/config/invoice-order.epp";

        StringBuilder text = new StringBuilder();
        String NL = System.getProperty("line.separator");
        try (Scanner scanner = new Scanner(getClass().getResourceAsStream(templateFile), "ISO-8859-2")) {
            while (scanner.hasNextLine())
                text.append(scanner.nextLine() + NL);

            String template = new String(text.toString().getBytes("utf8"));

            Map<String, Object> map = new HashMap<>();
            map.put("invoiceItems", prepareData(items));
            VariableResolverFactory vrf = new MapVariableResolverFactory(map);
            String result = (String) TemplateRuntime.eval(template, null, vrf, null);

            if (!fileName.endsWith(".epp"))
                fileName += ".epp";
            new FileOutputStream(fileName).write(result.getBytes("ISO-8859-2"));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private List<InvoiceItem> prepareData(List<RawInvoiceProductItem> items) {
        List<InvoiceItem> invoiceItems = new ArrayList<>();

        for (RawInvoiceProductItem item : items)
            invoiceItems.addAll(InvoiceItem.get(item.getProductInfo(), item.getCount()));

        int index = 0;
        for (InvoiceItem invoiceItem : invoiceItems)
            invoiceItem.setIndex(++index);

        return invoiceItems;
    }
}
