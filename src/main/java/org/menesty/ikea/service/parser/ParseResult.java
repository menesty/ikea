package org.menesty.ikea.service.parser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Menesty on
 * 9/7/15.
 * 20:16.
 */
public class ParseResult {
    private String fileName;
    private List<RawItem> rawOrderItems;
    private List<ErrorMessage> parseWarnings;

    public ParseResult() {
        rawOrderItems = new ArrayList<>();
        parseWarnings = new ArrayList<>();
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public List<RawItem> getRawOrderItems() {
        return rawOrderItems;
    }

    public void setRawOrderItems(List<RawItem> rawOrderItems) {
        this.rawOrderItems = rawOrderItems;
    }

    public List<ErrorMessage> getParseWarnings() {
        return parseWarnings;
    }

    public void setParseWarnings(List<ErrorMessage> parseWarnings) {
        this.parseWarnings = parseWarnings;
    }

    public void addParseWarning(String message, String exception) {
        parseWarnings.add(new ErrorMessage(message, exception));
    }

    public void addRawOrderItem(RawItem rawItem) {
        rawOrderItems.add(rawItem);
    }
}
