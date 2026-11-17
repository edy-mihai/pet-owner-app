package io.github.edymihai.petowner.console;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StyleTest {

    @Test
    void enabledErrorIsWrappedInRedAndReset() {
        assertEquals("\u001B[31mNot a number\u001B[0m", new Style(true).error("Not a number"));
    }

    @Test
    void enabledSuccessIsWrappedInGreenAndReset() {
        assertEquals("\u001B[32mAdded pet 1.\u001B[0m", new Style(true).success("Added pet 1."));
    }

    @Test
    void disabledReturnsTheTextUnchanged() {
        Style off = new Style(false);

        assertEquals("Not a number", off.error("Not a number"));
        assertEquals("Added pet 1.", off.success("Added pet 1."));
        assertEquals("", off.error(""));
    }
}
