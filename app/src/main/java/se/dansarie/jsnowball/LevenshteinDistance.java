package se.dansarie.jsnowball;

public class LevenshteinDistance {
  private LevenshteinDistance() {
  }

  public static int levenshteinDistance(String stra, String strb) {
    if (stra == null || strb == null) {
      throw new NullPointerException();
    }
    if (stra == strb) {
      return 0;
    }
    char a[] = stra.toCharArray();
    char b[] = strb.toCharArray();
    if (a.length == 0) {
      return b.length;
    }
    if (b.length == 0) {
      return a.length;
    }

    int m = a.length;
    int n = b.length;
    int d[] = new int[(m + 1) * (n + 1)];

    for (int i = 0; i <= m; i++) {
      d[i] = i;
    }
    for (int j = 0; j <= n; j++) {
      d[j * (m + 1)] = j;
    }

    for (int j = 1; j <= n; j++) {
      for (int i = 1; i <= m; i++) {
        int cost = 1;
        if (a[i - 1] == b[j - 1]) {
          cost = 0;
        }
        d[i + j * (m + 1)] = Math.min(d[i - 1 + j * (m + 1)] + 1,
            Math.min(d[i + (j - 1) * (m + 1)] + 1,
            d[i - 1 + (j - 1) * (m + 1)] + cost));
      }
    }

    return d[d.length - 1];
  }
}
