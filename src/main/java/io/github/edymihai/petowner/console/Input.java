package io.github.edymihai.petowner.console;

import io.github.edymihai.petowner.EndOfInputException;

import java.io.PrintStream;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.Scanner;

/** Reads typed answers. Checks type and format only; the domain checks meaning. Re-prompts on bad input. */
public class Input {

    private final Scanner in;
    private final PrintStream out;

    public Input(Scanner in, PrintStream out) {
        this.in = in;
        this.out = out;
    }

    public String readNonBlank(String prompt) {
        while (true) {
            String line = readLine(prompt).strip();
            if (!line.isEmpty()) {
                return line;
            }
            out.println("Please type something.");
        }
    }

    public int readInt(String prompt, int min, int max) {
        while (true) {
            String line = readLine(prompt).strip();
            try {
                int value = Integer.parseInt(line);
                if (value >= min && value <= max) {
                    return value;
                }
                out.println("Please type a number from " + min + " to " + max + ".");
            } catch (NumberFormatException e) {
                out.println("That is not a number.");
            }
        }
    }

    public String readEmail(String prompt) {
        while (true) {
            String line = readLine(prompt).strip();
            if (line.contains("@")) {
                return line;
            }
            out.println("An email must contain @.");
        }
    }

    public LocalDate readDate(String prompt) {
        while (true) {
            String line = readLine(prompt).strip();
            try {
                return LocalDate.parse(line);
            } catch (DateTimeParseException e) {
                out.println("Please type a date as yyyy-MM-dd.");
            }
        }
    }

    /** A blank answer means "none". */
    public Optional<LocalDate> readOptionalDate(String prompt) {
        while (true) {
            String line = readLine(prompt).strip();
            if (line.isEmpty()) {
                return Optional.empty();
            }
            try {
                return Optional.of(LocalDate.parse(line));
            } catch (DateTimeParseException e) {
                out.println("Please type a date as yyyy-MM-dd, or leave blank.");
            }
        }
    }

    /** A blank answer means "none". */
    public Optional<String> readOptionalText(String prompt) {
        String line = readLine(prompt).strip();
        return line.isEmpty() ? Optional.empty() : Optional.of(line);
    }

    /** Shows the constants of the enum; accepts the name (any case) or its number. */
    public <E extends Enum<E>> E readChoice(String prompt, Class<E> type) {
        E[] values = type.getEnumConstants();
        StringBuilder options = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            options.append(i + 1).append(") ").append(values[i]).append("  ");
        }
        while (true) {
            String line = readLine(prompt + " [" + options.toString().strip() + "]: ").strip();
            for (int i = 0; i < values.length; i++) {
                if (values[i].name().equalsIgnoreCase(line) || line.equals(String.valueOf(i + 1))) {
                    return values[i];
                }
            }
            out.println("Not a valid choice.");
        }
    }

    private String readLine(String prompt) {
        out.print(prompt);
        out.flush();
        if (!in.hasNextLine()) {
            throw new EndOfInputException();
        }
        return in.nextLine();
    }
}
