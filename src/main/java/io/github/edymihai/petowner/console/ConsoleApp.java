package io.github.edymihai.petowner.console;

import io.github.edymihai.petowner.PetCare;
import io.github.edymihai.petowner.account.InMemoryAccountDirectory;
import io.github.edymihai.petowner.careplan.InMemoryCarePlanBook;
import io.github.edymihai.petowner.pet.InMemoryPetRegister;

import java.io.Console;
import java.time.LocalDate;
import java.util.Scanner;

/** Console entry point. The only place that reads the clock, the terminal and the environment. */
public class ConsoleApp {

    public static void main(String[] args) {
        Options options;
        try {
            options = Options.parse(args);
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            System.err.println("Usage: ConsoleApp [--export-dir=PATH] [--demo] [--color | --no-color]");
            System.exit(2);
            return;
        }
        Console console = System.console();
        boolean terminal = console != null && console.isTerminal();
        String noColorEnv = System.getenv("NO_COLOR");
        boolean noColorEnvSet = noColorEnv != null && !noColorEnv.isEmpty();
        Style style = new Style(options.colorsEnabled(terminal, noColorEnvSet));

        PetCare petCare = new PetCare(new InMemoryAccountDirectory(), new InMemoryPetRegister(),
                new InMemoryCarePlanBook());
        LocalDate today = LocalDate.now();
        if (options.demo()) {
            DemoData.Summary loaded = DemoData.load(petCare, today);
            System.out.println(style.success("Demo data loaded: " + loaded.accounts() + " accounts, "
                    + loaded.pets() + " pets, " + loaded.plans() + " care plans."));
        }
        Input input = new Input(new Scanner(System.in), System.out, style);
        new Menu(input, petCare, System.out, today, options.exportDir(), style).run();
    }
}
