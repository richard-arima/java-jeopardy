package com.kenzie.app;

import java.util.NoSuchElementException;
import java.util.Scanner;

public class Main {
    public static final String WELCOME_MESSAGE = "Welcome to the Kenzie Quiz bowl!";
    public static final String GOODBYE_MESSAGE = "Thank you for playing with us today :D\nGoodbye!";

    public static void main(String[] args)  {
        Scanner inputScanner = new Scanner(System.in);
        GameDaemon gameDaemon = GameDaemon.getInstance();
        boolean continueGame = true;

        // welcome message
        System.out.println(WELCOME_MESSAGE);

        String inputResponse = "";
        do {
            try {
                inputResponse = inputScanner.nextLine();
            } catch (IllegalStateException illegalStateException) {
                System.out.println("Fatal Error: Could not open Scanner.");
                return;
            } catch (java.util.NoSuchElementException noSuchElementException) {
                System.out.println("Error: No line was found.");
            }

            continueGame = gameDaemon.reportInput(inputResponse);
        } while (continueGame);

        // output goodbye
        System.out.println(GOODBYE_MESSAGE);
    }
}

