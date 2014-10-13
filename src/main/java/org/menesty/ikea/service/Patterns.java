package org.menesty.ikea.service;

import java.util.regex.Pattern;

/**
 * User: Menesty
 * Date: 10/13/13
 * Time: 5:14 PM
 */
public class Patterns {
    public static final Pattern WEIGHT_PATTERN = Pattern.compile("(\\d{0,},{0,}\\d{1,})kg");

    public static final Pattern ART_NUMBER_PART_PATTERN = Pattern.compile("(\\d{3}\\.\\d{3}\\.\\d{2})");

    public static final Pattern PACKAGE_INFO_PATTERN = Pattern.compile("\"quantity\":\"(\\d+)\",\"length\":(\\d+),\"width\":(\\d+),\"articleNumber\":\"\\d+\",\"weight\":(\\d+),\"height\":(\\d+)");

    public static final Pattern HEIGHT_PATTERN = Pattern.compile("Wysokość: (\\d+{0,}\\.{0,}\\d+)");

    public static final Pattern WIDTH_PATTERN = Pattern.compile("Szerokość: (\\d+{0,}\\.{0,}\\d+)");

    public static final Pattern LENGHT_PATTERN = Pattern.compile("(Długość|Głębokość): (\\d+{0,}\\.{0,}\\d+)");
}
