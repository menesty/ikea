package org.menesty.ikea.service;

import net.sf.jxls.transformer.XLSTransformer;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.menesty.ikea.processor.invoice.InvoiceItem;
import org.menesty.ikea.processor.invoice.RawInvoiceProductItem;
import org.mvel.integration.VariableResolverFactory;
import org.mvel.integration.impl.MapVariableResolverFactory;
import org.mvel.templates.TemplateRuntime;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.*;

public class InvoiceService {
    private final static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
    public void exportToEpp(List<InvoiceItem> items, String fileName) {
        String templateFile = "/config/invoice-order.epp";

        StringBuilder text = new StringBuilder();
        String NL = System.getProperty("line.separator");
        try (Scanner scanner = new Scanner(getClass().getResourceAsStream(templateFile), "ISO-8859-2")) {
            while (scanner.hasNextLine())
                text.append(scanner.nextLine() + NL);

            String template = new String(text.toString().getBytes("utf8"));

            Map<String, Object> map = new HashMap<>();
            map.put("invoiceItems", items);
            map.put("createDate", sdf.format(new Date()));
            VariableResolverFactory vrf = new MapVariableResolverFactory(map);
            String result = (String) TemplateRuntime.eval(template, null, vrf, null);

            if (!fileName.endsWith(".epp"))
                fileName += ".epp";
            new FileOutputStream(fileName).write(result.getBytes("ISO-8859-2"));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void exportToXls(List<RawInvoiceProductItem> items, String path) {
        XLSTransformer transformer = new XLSTransformer();
        Map<String, Object> bean = new HashMap<>();
        bean.put("items", items);

        if (!path.endsWith(".xlsx"))
            path = path.concat(".xlsx");

        try {
            Workbook workbook = transformer.transformXLS(getClass().getResourceAsStream("/config/invoice.xlsx"), bean);

            workbook.write(Files.newOutputStream(FileSystems.getDefault().getPath(path), StandardOpenOption.CREATE_NEW));

        } catch (InvalidFormatException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
