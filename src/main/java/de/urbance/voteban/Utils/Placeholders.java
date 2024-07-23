package de.urbance.voteban.Utils;

import org.apache.commons.text.StringSubstitutor;

import java.util.Map;

public class Placeholders {
    String message;
    Map<String, String> values;

    public Placeholders(String message, Map<String, String> values) {
        this.message = message;
        this.values = values;
    }

    public String set() {
        return StringSubstitutor.replace(message, values, "%", "%");
    }
}
