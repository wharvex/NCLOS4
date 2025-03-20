package com.wharvex.nclos;

import java.util.Objects;
import java.util.function.IntBinaryOperator;
import java.util.function.IntFunction;
import java.util.function.IntUnaryOperator;
import java.util.logging.Level;
import java.util.stream.IntStream;

public class MiscHelper {
  public static void enforceNonNullNonEmptyNonBlankString(String s) {
    if (s == null || s.isBlank()) {
      throw new RuntimeException(
          NclosLogger.logError("NULL_OR_BLANK_STRING").get());
    }
  }

  public static void enforceArrayLength(Object[] arr, int len) {
    if (arr.length != len) {
      throw new RuntimeException(NclosLogger.logError(
              "array len expected -> " + len + ", actual -> " + arr.length)
          .get());
    }
  }

  public static int findNullIndex(IntFunction<Object> f, int size) {
    return IntStream.range(0, size).filter(i -> Objects.isNull(f.apply(i)))
        .findFirst().orElse(-1);
  }

  public static int findNegativeIndex(IntUnaryOperator f, int size) {
    return IntStream.range(0, size).filter(i -> f.applyAsInt(i) < 0)
        .findFirst().orElse(-1);
  }

  public static int findNegativeIndex(IntBinaryOperator f, int size,
                                      int src) {
    return IntStream.range(0, size).filter(i -> f.applyAsInt(src, i) < 0)
        .findFirst().orElse(-1);
  }

  public static int[] makeIntArr(int size) {
    return IntStream.generate(() -> -1).limit(size).toArray();
  }

  // Split an input string into n substrings and store them in an array.
  // Their order in the array equals their order in the input.
  // No substring is to exceed m characters in length.
  public static String[] splitString(String s, int m) {
    // Get the length of the input string.
    int len = s.length();

    // Get the number of splits to make:
    // 1. Round the input string length up by m - 1.
    // (We do this because integer division floors the result.)
    // 2. Divide the result by the maximum length of each split.
    int n = (len + m - 1) / m;

    // Create an array to store the substrings.
    String[] result = new String[n];

    // Populate the array.
    for (int i = 0; i < n; i++) {
      // Start is (the index of) the first character of the input, or the
      // next one after m characters, or the next one after m more, etc.
      int start = i * m;

      // End is m characters after start, or the length of the input,
      // whichever comes first.
      int end = Math.min(start + m, len);
      result[i] = s.substring(start, end);
    }
    return result;
  }

  public static String getLogStringLower(Object o) {
    return String.valueOf(o).toLowerCase();
  }
}
