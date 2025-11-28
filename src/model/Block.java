package model;

/**
 * Allows us to retrieve block number index from coordinates (x,y)
 */
public enum Block {
  ZERO(new int[]{0, 1, 2}, new int[]{0, 1, 2}),
  ONE(new int[]{3, 4, 5}, new int[]{0, 1, 2}),
  TWO(new int[]{6, 7, 8}, new int[]{0, 1, 2}),
  THREE(new int[]{0, 1, 2}, new int[]{3, 4, 5}),
  FOUR(new int[]{3, 4, 5}, new int[]{3, 4, 5}),
  FIVE(new int[]{6, 7, 8}, new int[]{3, 4, 5}),
  SIX(new int[]{0, 1, 2}, new int[]{6, 7, 8}),
  SEVEN(new int[]{3, 4, 5}, new int[]{6, 7, 8}),
  EIGHT(new int[]{6, 7, 8}, new int[]{6, 7, 8});

  public final int[] cols, rows;

  Block(int[] cols, int[] rows) {
    this.cols = cols;
    this.rows = rows;
  }

  public static int of(CachedPoint p) {
    for (var block : values()) {
      if (contains(p.x, block.rows) && contains(p.y, block.cols)) {
        return block.ordinal();
      }
    }
    return -1;
  }

  private static boolean contains(Number n, int[] arr) {
    for (var i : arr) {
      if (i == n.intValue()) {
        return true;
      }
    }
    return false;
  }
}