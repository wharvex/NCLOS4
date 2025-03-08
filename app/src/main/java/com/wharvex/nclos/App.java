package com.wharvex.nclos;

import java.util.Scanner;

public class App {
    public static void main(String[] args) {
        Thread.currentThread().setName("mainThread");
        OutputHelper.debugPrint("BEGIN EXECUTION TRACE");
        var bl = new Bootloader();
        bl.init();
        try (Scanner sc = new Scanner(System.in)) {
            do {
                System.out.println("press x then enter to quit");
            } while (!sc.nextLine().equals("x"));
        }
        OutputHelper.debugPrint("END EXECUTION TRACE");
        System.exit(0);
    }

    public String getGreeting() {
        return "hey";
    }
}
