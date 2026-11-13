package io.github.edymihai.petowner.console;

import io.github.edymihai.petowner.PetCare;
import io.github.edymihai.petowner.account.InMemoryAccountDirectory;
import io.github.edymihai.petowner.pet.InMemoryPetRegister;

import java.time.LocalDate;
import java.util.Scanner;

/** Console entry point. The only place that reads the clock. */
public class ConsoleApp {

    public static void main(String[] args) {
        PetCare petCare = new PetCare(new InMemoryAccountDirectory(), new InMemoryPetRegister());
        LocalDate today = LocalDate.now();
        Input input = new Input(new Scanner(System.in), System.out);
        new Menu(input, petCare, System.out, today).run();
    }
}
