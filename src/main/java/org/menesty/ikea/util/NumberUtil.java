package org.menesty.ikea.util;

import javafx.beans.property.SimpleStringProperty;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;

public class NumberUtil {

    public static double parse(String value) {
        return parse(value, 0);
    }

    public static double parse(String value, double defaultValue) {
        try {
            return Double.valueOf(value.replaceAll("[a-zA-z\\u00A0\\s#€\\-.]", "").replace(",", "."));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static double round(double value, int roundingMode) {
        return round(value, 2, roundingMode);
    }

    public static double round(double value) {
        return round(value, 2, BigDecimal.ROUND_CEILING);
    }

    public static double round(double value, int scale, int roundingMode) {
        return BigDecimal.valueOf(value).setScale(scale, roundingMode).doubleValue();
    }

    public static String toString(double value) {
        value = round(value);

        if (value % 1 == 0) {
            return NumberFormat.getNumberInstance().format((int) value);
        }

        return NumberFormat.getNumberInstance().format(value);
    }

    public static SimpleStringProperty preparePackInfo(int value, int dive, String prefix) {
        return new SimpleStringProperty((value != 0 ? BigDecimal.valueOf((double) value / dive).setScale(2, RoundingMode.CEILING).doubleValue() + "" : "0") + " " + prefix);
    }


    public static double convertToCm(int value) {
        return BigDecimal.valueOf(value).divide(BigDecimal.TEN).setScale(2).doubleValue();
    }

    public static int convertToMm(double value) {
        return BigDecimal.valueOf(value).multiply(BigDecimal.TEN).intValue();
    }

    public static double convertToKg(int value) {
        return BigDecimal.valueOf(value).divide(BigDecimal.valueOf(1000)).setScale(3).doubleValue();
    }

    public static double convertToKg(double value) {
        return BigDecimal.valueOf(value).divide(BigDecimal.valueOf(1000)).setScale(3).doubleValue();
    }

    public static int convertToGr(double value) {
        return BigDecimal.valueOf(value).multiply(BigDecimal.valueOf(1000)).intValue();
    }
}
