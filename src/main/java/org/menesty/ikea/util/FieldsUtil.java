package org.menesty.ikea.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * Created by Menesty on
 * 7/21/15.
 * 20:00.
 */
public class FieldsUtil {
    public static String build(String... fields) {
        return Arrays.asList(fields).stream().collect(Collectors.joining("."));
    }
}
