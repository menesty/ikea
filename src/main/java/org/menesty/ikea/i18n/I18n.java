package org.menesty.ikea.i18n;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Created by Menesty on
 * 7/4/15.
 * 10:35.
 */
public enum I18n {
    UA("ua", "i18n.messages");

    private final ResourceBundle resourceBundle;
    private final Locale locale;

    I18n(String lang, String bundleFile) {
        locale = new Locale(lang);
        resourceBundle = ResourceBundle.getBundle(bundleFile, locale, new UTF8Control());

    }

    public String getString(String key) {
        try {
            return resourceBundle.getString(key);
        } catch (MissingResourceException e) {
            System.err.println(e);

            return "err#";
        }
    }

    public String getString(String key, Object... params) {
        return String.format(getString(key), params);
    }

}
