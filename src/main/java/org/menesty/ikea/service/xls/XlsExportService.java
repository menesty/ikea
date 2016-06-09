package org.menesty.ikea.service.xls;

import org.jxls.area.Area;
import org.jxls.builder.AreaBuilder;
import org.jxls.builder.xml.XmlAreaBuilder;
import org.jxls.common.CellRef;
import org.jxls.common.Context;
import org.jxls.transform.Transformer;
import org.jxls.util.TransformerFactory;
import org.menesty.ikea.i18n.I18n;
import org.menesty.ikea.i18n.I18nKeys;
import org.menesty.ikea.lib.domain.ikea.logistic.resumption.ResumptionItem;
import org.menesty.ikea.lib.domain.ikea.logistic.stock.StockItemDto;
import org.menesty.ikea.lib.domain.report.SummaryOrderReport;
import org.menesty.ikea.lib.dto.ProductPriceMismatch;
import org.menesty.ikea.util.ColumnUtil;
import org.menesty.ikea.util.DateFormatter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Menesty on
 * 9/11/15.
 * 20:25.
 */
public class XlsExportService {
  private static final String PRODUCT_PRICE_MISMATCH_NOT_AVAILABLE_TEMPLATE = "mismatch_not_available";
  private static final String PRODUCT_BUY_RESULT_TEMPLATE = "buy_result";
  private static final String RESUMPTION_ITEM_TEMPLATE = "resumption_item";
  private static final String SUMMARY_ORDER_REPORT_TEMPLATE = "summary_order_report_template";

  public void exportProductPriceMismatchNotAvailable(File targetFile, List<String> items, List<ProductPriceMismatch> mismatches) {
    Context context = new Context();
    context.putVar("items", items);
    context.putVar("mismatches", mismatches);

    transformSingleSheet(targetFile, PRODUCT_PRICE_MISMATCH_NOT_AVAILABLE_TEMPLATE, context, Arrays.asList("NotExist!A1", "Mismatch!A1"));
  }

  public void exportSummaryOrderReport(File targetFile, SummaryOrderReport summaryOrderReport) {
    Context context = new Context();
    context.putVar("reportItemList", summaryOrderReport.getReportItemList());
    context.putVar("comboReportItems", summaryOrderReport.getComboReportItems());

    transformSingleSheet(targetFile, SUMMARY_ORDER_REPORT_TEMPLATE, context, Arrays.asList("OrderItems!A1", "OrderCombos!A1"));
  }

  private void transformSingleSheet(File targetFile, String templateName, Context context, List<String> cellRef) {
    try (InputStream is = getTemplate(templateName)) {
      try (OutputStream os = getOutputStream(targetFile)) {
        Transformer transformer = TransformerFactory.createTransformer(is, os);
        try (InputStream configInputStream = getTemplateConfig(templateName)) {
          AreaBuilder areaBuilder = new XmlAreaBuilder(configInputStream, transformer);
          List<Area> xlsAreaList = areaBuilder.build();

          for (int i = 0; i < xlsAreaList.size(); i++) {
            Area xlsArea = xlsAreaList.get(i);
            xlsArea.applyAt(new CellRef(cellRef.get(i)), context);
          }

          transformer.write();
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(I18n.UA.getString(I18nKeys.FAILED_GENERATE_XLS_REPORT, templateName), e);
    }
  }

  private InputStream getTemplate(String templateName) throws IOException {
    return XlsExportService.class.getResourceAsStream("/templates/xls/" + templateName + ".xlsx");
  }

  private InputStream getTemplateConfig(String templateName) throws IOException {
    return XlsExportService.class.getResourceAsStream("/templates/xls/config/" + templateName + ".xml");
  }

  private OutputStream getOutputStream(File file) throws IOException {
    StandardOpenOption operation = StandardOpenOption.CREATE_NEW;

    if (file.exists()) {
      operation = StandardOpenOption.TRUNCATE_EXISTING;
    }

    return Files.newOutputStream(file.toPath(), operation);
  }

  public void exportBuyResult(File targetFile, List<StockItemDto> overBough, List<StockItemDto> lack) {
    Context context = new Context();
    context.putVar("overBoughs", overBough);
    context.putVar("lacks", lack);

    transformSingleSheet(targetFile, PRODUCT_BUY_RESULT_TEMPLATE, context, Arrays.asList("OverBough!A1", "Lack!A1"));
  }

  public void exportResumptionItems(File targetFile, List<ResumptionItem> resumptionItems) {
    Context context = new Context();

    context.putVar("items", resumptionItems);
    context.putVar("dateFormatter", new DateFormatter(ColumnUtil.DEFAULT_DATE_FORMAT));
    transformSingleSheet(targetFile, RESUMPTION_ITEM_TEMPLATE, context, Collections.singletonList("Resumption!A1"));

  }
}
