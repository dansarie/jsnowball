/* Copyright (c) 2021 Marcus Dansarie

   This program is free software: you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
   GNU General Public License for more details.
   You should have received a copy of the GNU General Public License
   along with this program. If not, see <http://www.gnu.org/licenses/>. */
package se.dansarie.jsnowball;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class TestLevenshteinDistance {
  @Test
  public void testLevenshteinDistance() {
    assertEquals(0, LevenshteinDistance.levenshteinDistance("", ""));
    assertEquals(6, LevenshteinDistance.levenshteinDistance("", "kitten"));
    assertEquals(6, LevenshteinDistance.levenshteinDistance("kitten", ""));
    assertEquals(6, LevenshteinDistance.levenshteinDistance("kitten", "_"));
    assertEquals(6, LevenshteinDistance.levenshteinDistance("kitten", "______"));
    assertEquals(4, LevenshteinDistance.levenshteinDistance("", "Rock"));
    assertEquals(3, LevenshteinDistance.levenshteinDistance("kitten", "sitting"));
    assertEquals(1, LevenshteinDistance.levenshteinDistance("rock", "Rock"));
    assertEquals(0, LevenshteinDistance.levenshteinDistance("kitten", "kitten"));
    assertThrows(NullPointerException.class,
        () -> LevenshteinDistance.levenshteinDistance(null, "kitten"));
    assertThrows(NullPointerException.class,
        () -> LevenshteinDistance.levenshteinDistance("kitten", null));
    assertThrows(NullPointerException.class,
        () -> LevenshteinDistance.levenshteinDistance(null, null));
  }
}
