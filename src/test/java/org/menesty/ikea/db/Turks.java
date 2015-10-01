package org.menesty.ikea.db;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.jxls.area.Area;
import org.jxls.builder.AreaBuilder;
import org.jxls.builder.xml.XmlAreaBuilder;
import org.jxls.common.CellRef;
import org.jxls.common.Context;
import org.jxls.transform.Transformer;
import org.jxls.util.TransformerFactory;
import org.menesty.ikea.domain.OrderItem;
import org.menesty.ikea.service.OrderService;
import org.menesty.ikea.util.NumberUtil;
import org.xml.sax.SAXException;

import javax.persistence.Persistence;
import java.io.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Menesty on
 * 9/23/15.
 * 12:01.
 */
public class Turks {
    public static void main(String... arg) throws IOException, InvalidFormatException, SAXException {
        DatabaseService.entityManagerFactory = Persistence.createEntityManagerFactory("ikea");
        DatabaseService.initialized = 1;

        BigDecimal rate = new BigDecimal(1.246);

        OrderService orderService = new OrderService();
        InputStream inputStream = new FileInputStream("/Users/andrewhome/Downloads/ikea215.xlsx");

        OrderService.OrderParseResult parseResult = orderService.parseOrder(inputStream, (l, l1) -> {
        });

        List<OrderItem> orderItems = orderService.reduce(null, parseResult.getRawOrderItems(), (l, l1) -> {
        });

        List<Item> items = orderItems.stream().map(orderItem -> {
            Item item = new Item();
            item.setArtNumber(orderItem.getArtNumber());
            item.setCount(new BigDecimal(orderItem.getCount()));
            item.setPlPrice(new BigDecimal(orderItem.getPrice()).setScale(2, BigDecimal.ROUND_HALF_UP));

            try {
                Document document = Jsoup.connect("http://www.ikea.com.tr/arama/?k=" + orderItem.getArtNumber()).get();
                Elements elements = document.select(".product-detail p.price");
                if(!elements.isEmpty()) {
                    double price = NumberUtil.parse(elements.get(0).text());
                    item.setTrPrice(new BigDecimal(price).multiply(rate).setScale(2, BigDecimal.ROUND_HALF_UP));
                }else {
                    System.out.println("No rice : "+orderItem.getArtNumber());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return item;
        }).collect(Collectors.toList());
        DatabaseService.close();

        String separator = ",";
        StringBuilder text = new StringBuilder();

        items.stream().forEach(item -> {
            text.append("'").append(item.getArtNumber()).append("'")
                    .append(separator)
                    .append(item.getPlPrice())
                    .append(separator)
                    .append(item.getTrPrice())
                    .append(separator)
                    .append(item.getCount()).append("\n");
        });

        FileOutputStream fos = new FileOutputStream("test.csv");
        fos.write(text.toString().getBytes());
        fos.close();
/*
        Context context = new Context();
        context.putVar("items", items);

        try (InputStream is = new FileInputStream("/Users/andrewhome/development/workspace/ikea/src/test/resources/templates/mismatch.xlsx")) {
            try (OutputStream os = new FileOutputStream("test.xlsx")) {
                Transformer transformer = TransformerFactory.createTransformer(is, os);
                try (InputStream configInputStream = new FileInputStream("/Users/andrewhome/development/workspace/ikea/src/test/resources/templates/mismatch.xml")) {
                    AreaBuilder areaBuilder = new XmlAreaBuilder(configInputStream, transformer);
                    List<Area> xlsAreaList = areaBuilder.build();
                    Area xlsArea = xlsAreaList.get(0);
                    xlsArea.applyAt(new CellRef("Template!A1"), context);
                    transformer.write();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }

}
