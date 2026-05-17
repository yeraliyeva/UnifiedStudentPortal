package infrastructure.i18n;

import domain.enums.Language;

public interface Translator {
    String get(String key);
    String fmt(String key, Object... args);
    void switchTo(Language language);
    Language current();
    java.util.Map<String, String> getAll();
}
