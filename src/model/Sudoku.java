package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import verifier.Verifier;

public class Sudoku {

  public enum Target {ROW, COL, BLOCK}

  public static final Random rand = new Random();
  public static final int SIZE = 9, BLOCK_SIZE = 3;
  public final int[][] model;
  private final Marker[] markers;

  /**
   * This constructor is the lazy-loaded version that generates a valid configuration
   * later using Sudoku#generateModel
   */
  public Sudoku() {
    model = new int[SIZE][SIZE];
    markers = new Marker[9];
  }

  public Sudoku(int[][] model) {
    if (!Verifier.verify(model)) {
      throw new IllegalArgumentException("Invalid sudoku");
    }
    this.model = model;
    markers = new Marker[9];
  }

  private void init() {
    for (int i = 0; i < SIZE; ++i) {
      markers[i] = new Marker(i + 1);
      for (int j = 0; j < SIZE; ++j) {
        model[i][j] = 0;
      }
    }
  }

  private void erase(int num) {
    for (int i = 0; i < SIZE; ++i) {
      for (int j = 0; j < SIZE; ++j) {
        if (model[i][j] == num) {
          model[i][j] = 0;
        }
      }
    }
  }

  public void generateModel() {
    init();
    Marker marker;
    for (var value : markers) {
      marker = value.initAll();
      int c = 0;
      while (!marker.finished()) {
        marker.randomAvailablePoint();
        if (c++ > SIZE * SIZE) {
          generateModel();
          return;
        }
      }
    }
  }

  public String debug() {
    var sb = new StringBuilder();
    for (int i = 0; i < SIZE; ++i) {
      for (int j = 0; j < SIZE; ++j) {
        sb.append(model[i][j]);
      }
      sb.append("\n");
    }
    return sb.toString();
  }

  public static void main(String[] args) {
    Sudoku s = new Sudoku();
    long time = System.currentTimeMillis();
    s.generateModel();
    time = System.currentTimeMillis() - time;
    System.out.println(s.debug());
    System.out.printf("%s %sms%n", Verifier.verify(s.model), time);
  }

  class Marker {

    public final int number;
    private boolean[] rows, cols, blocks;  // marks which numbers we put
    private CachedPoint first;
    private final List<CachedPoint> invalids;

    public Marker(int number) {
      this.number = number;
      initAll();
      invalids = new ArrayList<>();
    }

    private Marker initAll() {
      var size = Sudoku.SIZE;
      rows = new boolean[size];
      cols = new boolean[size];
      blocks = new boolean[size];
      erase(number);
      return this;
    }

    private void setFirst(CachedPoint p) {
      // if we reset the first for this marker, then previous was invalid
      if (first != null) {
        invalids.add(first);
      }
      first = p;
    }

    private void randomAvailablePoint() {
      CachedPoint point = null, previous;
      int block, error = 0;
      do { // keep trying till we get a new point in an unmarked block
        previous = point;
        point = CachedPoint.from(randomAvailable(rows), randomAvailable(cols));
        if (previous == null) {
          setFirst(point);
        }
        block = Block.of(point);
        if (point.equals(previous)) {
          ++error;
        }
        if (error == 10) {  // Means initial point was placed wrong
          error = 0;    // since we keep getting same last point pos
          initAll();    // reset markers
          erase(number); // delete current number from model
        }
      } while (blocks[block]
          || model[point.x][point.y] != 0
          || invalids.contains(point));
      // mark new found available point
      blocks[block] = true;
      rows[point.x] = true;
      cols[point.y] = true;
      model[point.x][point.y] = number;
    }

    private boolean finished() {
      return allMarked(rows) && allMarked(cols) && allMarked(blocks);
    }

    private int randomAvailable(boolean[] arr) {
      var available = new ArrayList<Integer>();
      for (int i = 0; i < arr.length; i++) {
        if (!arr[i]) {
          available.add(i);
        }
      }
      if (available.size() == 1) {
        return available.getFirst();
      }
      return available.get(rand.nextInt(available.size()));
    }

    private boolean allMarked(boolean[] arr) {
      var marked = true;
      for (boolean b : arr) {
        marked &= b;
      }
      return marked;
    }
  }
}
