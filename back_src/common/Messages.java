package common;

import enums.Language;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Internationalization utility — Java ResourceBundle approach.
 *
 * Loads properties files for EN, KZ, RU and serves localized strings
 * based on the current language setting.
 *
 * Usage:
 *   Messages.get("login.title")           → simple string
 *   Messages.fmt("login.welcome", name)   → parameterized string
 */
public final class Messages {

    private static Language currentLanguage = Language.ENGLISH;
    private static final Map<Language, Properties> bundles = new HashMap<>();

    static {
        bundles.put(Language.ENGLISH, load("messages_en.properties"));
        bundles.put(Language.RUSSIAN, load("messages_ru.properties"));
        bundles.put(Language.KAZAKH,  load("messages_kz.properties"));
    }

    private Messages() {}

    /** Load a properties file from the classpath (same package as this class). */
    private static Properties load(String filename) {
        Properties props = new Properties();
        try (InputStream is = Messages.class.getResourceAsStream(filename)) {
            if (is != null) {
                props.load(new InputStreamReader(is, StandardCharsets.UTF_8));
            } else {
                System.err.println("[i18n] Warning: " + filename + " not found on classpath.");
            }
        } catch (IOException e) {
            System.err.println("[i18n] Error loading " + filename + ": " + e.getMessage());
        }
        return props;
    }

    /** Set the active language for all subsequent get() / fmt() calls. */
    public static void setLanguage(Language lang) {
        currentLanguage = lang;
    }

    public static Language getLanguage() {
        return currentLanguage;
    }

    /**
     * Get a localized string by key.
     * Falls back to English if key is missing in current language.
     * Falls back to the key itself if missing everywhere.
     */
    public static String get(String key) {
        Properties props = bundles.get(currentLanguage);
        String value = props.getProperty(key);
        if (value == null && currentLanguage != Language.ENGLISH) {
            value = bundles.get(Language.ENGLISH).getProperty(key);
        }
        return value != null ? value : key;
    }

    /**
     * Get a localized string and format it with arguments.
     * Uses java.text.MessageFormat patterns: {0}, {1}, etc.
     */
    public static String fmt(String key, Object... args) {
        return MessageFormat.format(get(key), args);
    }
}
