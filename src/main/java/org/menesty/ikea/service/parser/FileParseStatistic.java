package org.menesty.ikea.service.parser;

import java.math.BigDecimal;
import java.text.NumberFormat;

/**
 * Created by Menesty on
 * 9/9/15.
 * 00:42.
 */
public class FileParseStatistic {
    private final ParseResult parseResult;
    private BigDecimal sum;

    public FileParseStatistic(ParseResult parseResult) {
        this.parseResult = parseResult;
    }

    public BigDecimal getSum() {
        return sum;
    }

    public String getSumFormat() {
        return NumberFormat.getInstance().format(sum.doubleValue());
    }

    public void setSum(BigDecimal sum) {
        this.sum = sum;
    }

    public ParseResult getParseResult() {
        return parseResult;
    }

    public int getItemCount() {
        return parseResult.getRawOrderItems().size();
    }

    public int getWarningCount() {
        return parseResult.getParseWarnings().size();
    }

    public String getFileName() {
        return parseResult.getFileName();
    }

    public void setFileName(String finalName) {
        parseResult.setFileName(finalName);
    }
}
