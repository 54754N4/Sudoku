package model;

import view.SudokuPanel;

/**
 * Immutable point class.
 * Cache note:
 * Since we have the luxury of just needing points to map the coordinates
 * of a random sudoku, we know they're bounded. So we can take advantage of a
 * 2D cache of pre-constructed Points and make all constructors private.
 */
public class Point {
  public static final Point[][] CACHE;

  static {
    int MAX = SudokuPanel.SUDOKU_SIZE;
    CACHE = new Point[MAX][MAX];
    for (int x=0; x<MAX; ++x)
      for (int y=0; y<MAX; ++y)
        CACHE[x][y] = new Point(x,y);
  }

	public final int x, y;
	
	private Point(int x, int y) {
		this.x = x;
		this.y = y;
	}

  public static Point from(int x, int y) {
    return CACHE[x][y];
  }
	
	public Point plusEquals(int x, int y) {
		return plusEquals(x, y, Integer.MAX_VALUE);
	}
	
	public Point plusEquals(int i, int j, int wrap) {
		int x = this.x + i;
    int y = this.y + j;
		if (x >= wrap)
      x = 0;
		else if (x < 0)
      x = wrap-1;
		if (y >= wrap)
      y = 0;
		else if (y < 0)
      y = wrap-1;
		return from(x, y);
	}
	
	public Point plus(Point p) {
		return from(x + p.x, y + p.y);
	}
	
	public boolean equals(Point p) {
		if (p == null)
      return false;
		return x==p.x && y==p.y;
	}
	
	public String toString() {
		return String.format("(%d, %d)", x, y);
	}
}