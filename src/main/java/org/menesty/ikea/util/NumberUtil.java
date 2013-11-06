package org.menesty.ikea.util;

import java.math.BigDecimal;

public class NumberUtil {

    public static double parse(String value) {
        return parse(value, 0);
    }

    public static double parse(String value, double defaultValue) {
        try {
            return Double.valueOf(value.replaceAll("[a-zA-z ]", "").replace(",", "."));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static double round(double value) {
        return round(value, 2);
    }

    public static double round(double value, int scale) {
        return BigDecimal.valueOf(value).setScale(scale, BigDecimal.ROUND_CEILING).doubleValue();
    }
}
