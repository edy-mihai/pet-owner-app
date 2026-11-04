package io.github.edymihai.petowner;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class GreeterTest {

    @Test
    void greetIncludesTheName() {
        Greeter greeter = new Greeter();
        String Result = greeter.greet("Rex");
        assertEquals("Hello, Rex!", Result);
    }
}