package io.github.edymihai.petowner.console;

/** Wraps text in ANSI color codes when colors are enabled; returns it unchanged otherwise. */
public class Style {

    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String RESET = "\u001B[0m";

    private final boolean enabled;

    public Style(boolean enabled) {
        this.enabled = enabled;
    }

    public String error(String text) {
        return wrap(RED, text);
    }

    public String success(String text) {
        return wrap(GREEN, text);
    }

    private String wrap(String code, String text) {
        return enabled ? code + text + RESET : text;
    }
}
