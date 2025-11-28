package model;

import view.SudokuPanel;

/**
 * Immutable point class. Cache note: Since we have the luxury of just needing points to map the
 * coordinates of a random sudoku, we know they're bounded. So we can take advantage of a 2D cache
 * of pre-constructed Points and make all constructors private.
 */
public class CachedPoint {

  public static final CachedPoint[][] CACHE;

  static {
    int MAX = SudokuPanel.SUDOKU_SIZE;
    CACHE = new CachedPoint[MAX][MAX];
    for (int x = 0; x < MAX; ++x) {
      for (int y = 0; y < MAX; ++y) {
        CACHE[x][y] = new CachedPoint(x, y);
      }
    }
  }

  public final int x, y;

  private CachedPoint(int x, int y) {
    this.x = x;
    this.y = y;
  }

  public static CachedPoint from(int x, int y) {
    return CACHE[x][y];
  }

  // starting block coords. based on block index is : B(i) = ((3*(i//3), (3*(i%3)))
  public static CachedPoint startOf(int i) {
    var coords = coordsOf(i);
    return from(
        Sudoku.BLOCK_SIZE * coords.x,
        Sudoku.BLOCK_SIZE * coords.y);
  }

  public static CachedPoint coordsOf(int i) {
    return from(
        i % Sudoku.BLOCK_SIZE,
        i / Sudoku.BLOCK_SIZE);
  }

  public static int indexOf(CachedPoint point) {
    return point.y * Sudoku.BLOCK_SIZE + point.x;
  }

  public CachedPoint plusEquals(int x, int y) {
    return plusEquals(x, y, Integer.MAX_VALUE);
  }

  public CachedPoint plusEquals(int i, int j, int wrap) {
    int x = this.x + i;
    int y = this.y + j;
    if (x >= wrap) {
      x = 0;
    } else if (x < 0) {
      x = wrap - 1;
    }
    if (y >= wrap) {
      y = 0;
    } else if (y < 0) {
      y = wrap - 1;
    }
    return from(x, y);
  }

  public CachedPoint plus(CachedPoint p) {
    return from(x + p.x, y + p.y);
  }

  public boolean equals(CachedPoint p) {
    if (p == null) {
      return false;
    }
    return x == p.x && y == p.y;
  }

  public String toString() {
    return String.format("(%d, %d)", x, y);
  }
}