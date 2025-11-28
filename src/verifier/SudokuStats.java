package verifier;

import model.Sudoku;

public class SudokuStats {

  private int n;
  public final String[] sudoku;
  public final long[] times;
  public final boolean[] correct;

  public static void main(String[] args) {
    SudokuStats.test(1000).stats();
  }

  public static SudokuStats test(int n) {
    return new SudokuStats(n);
  }

  private SudokuStats(int n) {
    this.n = n;
    sudoku = new String[n];
    times = new long[n];
    correct = new boolean[n];
    generate();
  }

  private void generate() {
    Sudoku s = new Sudoku();
    long time;
    for (int i = 0; i < n; i++) {
      time = System.currentTimeMillis();
      s.generateModel();
      times[i] = System.currentTimeMillis() - time;
      sudoku[i] = s.debug();
      correct[i] = Verifier.verify(s.model);
    }
  }

  private boolean testValidity() {
    boolean valid = true;
    for (boolean b : correct) {
      valid &= b;
    }
    return valid;
  }

  public long average() {
    long sum = 0;
    for (long time : times) {
      sum += time;
    }
    return sum / n;
  }

  public double std() {
    double std = 0;
    long mean = average();
    for (int i = 0; i < n; i++) {
      std += (times[i] - mean) * (times[i] - mean);
    }
    return Math.sqrt(std / n);
  }

  private long max() {
    long max = 0;
    for (long time : times) {
      if (time > max) {
        max = time;
      }
    }
    return max;
  }

  private long min() {
    long min = Long.MAX_VALUE;
    for (long time : times) {
      if (time < min) {
        min = time;
      }
    }
    return min;
  }

  private long totalTime() {
    long sum = 0;
    for (long time : times) {
      sum += time;
    }
    return sum;
  }

  private void debug() {
    int c = 1, every = 5;
    for (long time : times) {
      if (c++ % every == 0) {
        System.out.println(time);
      } else {
        System.out.print(time + "\t");
      }
    }
  }

  public void stats() {
    System.out.println("Data count : " + n);
    System.out.println("All valid : " + testValidity());
    System.out.println("Mean : " + average() + " ms");
    System.out.println("STD : " + std() + "ms");
    System.out.println("MIN : " + min() + "ms");
    System.out.println("MAX : " + max() + "ms");
    System.out.println("TOTAL : " + totalTime() + "ms");
    System.out.println();
    debug();
  }
}