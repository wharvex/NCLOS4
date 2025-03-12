package com.wharvex.nclos;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MiscHelperTest {
  @Test
  void splitString_splitsCorrectly() {
    String[] result =
        MiscHelper.splitString("1234567890123456789012345678901234567890",
            38);
    assertArrayEquals(
        new String[]{"12345678901234567890123456789012345678", "90"},
        result);
  }

  @Test
  void splitString_handlesEmptyString() {
    String[] result = MiscHelper.splitString("", 38);
    assertArrayEquals(new String[]{""}, result);
  }
}