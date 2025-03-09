package com.wharvex.nclos;

import java.util.Scanner;
import java.util.logging.Level;

public class App {
    public static void main(String[] args) {
        Thread.currentThread().setName("mainThread");
        OutputHelper.getInstance().getDebugLogger().log(Level.INFO, "begin execution trace");
        var bl = new Bootloader();
        bl.init();
        try (Scanner sc = new Scanner(System.in)) {
            do {
                System.out.println("Type x then enter to quit.");
                System.out.print("Type here: ");
            } while (!sc.nextLine().equals("x"));
        }
        OutputHelper.getInstance().getDebugLogger().log(Level.INFO, "end execution trace");
        System.exit(0);
    }

    public String getGreeting() {
        return "hey";
    }
}
