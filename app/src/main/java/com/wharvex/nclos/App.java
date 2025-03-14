package com.wharvex.nclos;

import java.util.Scanner;

public class App {
  public static void main(String[] args) {
    Thread.currentThread().setName("mainThread");
    NclosLogger.logDebugThread(ThreadLifeStage.STARTING);

    // Try with resources.
    try (Scanner sc = new Scanner(System.in)) {
      // Print the options.
      System.out.println("Type 3 then enter to test messages.");
      System.out.println("Type 4 then enter to test paging.");
      System.out.println("Type 5 then enter to test virtual memory.");
      System.out.println("Type 9 then enter to quit.");

      // Get initial input and parse it as an int.
      var nexLin = sc.nextLine();
      int testChoice = Integer.parseInt(nexLin);
      System.out.println("You chose " + testChoice);

      // Start the bootloader with the choice.
      if (testChoice != 9) {
        var bl = new Bootloader(testChoice);
        bl.init();
      }

      // Loop until user enters 9.
      while (testChoice != 9) {
        System.out.println("Type 9 then enter to quit.");
        nexLin = sc.nextLine();
        testChoice = Integer.parseInt(nexLin);
      }
    }

    // The user has chosen to exit.
    NclosLogger.logDebugThread(ThreadLifeStage.STOPPING);
    System.exit(0);
  }

  public String getGreeting() {
    return "hey";
  }
}
