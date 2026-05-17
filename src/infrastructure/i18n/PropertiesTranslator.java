package infrastructure.i18n;

import domain.enums.Language;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public final class PropertiesTranslator implements Translator {
    public static final PropertiesTranslator INSTANCE = new PropertiesTranslator();
    private final Map<Language, Properties> bundles = new HashMap<>();
    private final ThreadLocal<Language> current = ThreadLocal.withInitial(() -> Language.ENGLISH);

    public PropertiesTranslator() {
        bundles.put(Language.ENGLISH, load("messages_en.properties"));
        bundles.put(Language.RUSSIAN, load("messages_ru.properties"));
        bundles.put(Language.KAZAKH,  load("messages_kz.properties"));
    }

    private Properties load(String filename) {
        Properties p = new Properties();
        try (InputStream is = PropertiesTranslator.class.getResourceAsStream("/" + filename)) {
            if (is != null) p.load(new InputStreamReader(is, StandardCharsets.UTF_8));
        } catch (IOException ignored) {}
        return p;
    }

    @Override public String get(String key) {
        String v = bundles.get(current.get()).getProperty(key);
        if (v == null && current.get() != Language.ENGLISH) v = bundles.get(Language.ENGLISH).getProperty(key);
        return v != null ? v : key;
    }
    @Override public String fmt(String key, Object... args) { return MessageFormat.format(get(key), args); }
    @Override public void switchTo(Language lang) { this.current.set(lang != null ? lang : Language.ENGLISH); }
    @Override public Language current() { return current.get(); }
    
    @Override public Map<String, String> getAll() {
        Properties p = bundles.get(current.get());
        if (p == null) p = bundles.get(Language.ENGLISH);
        Map<String, String> map = new HashMap<>();
        for (String key : p.stringPropertyNames()) {
            map.put(key, p.getProperty(key));
        }
        return map;
    }
}
