package model;

import java.util.Random;
import model.Cell.State;
import verifier.Verifier;
import view.SudokuPanel;

public class NotedSudoku {

  public static final Random rand = Sudoku.rand;
  public static final int
      SUDOKU_SIZE = SudokuPanel.SUDOKU_SIZE,
      BLOCK_SIZE = SudokuPanel.BLOCK_SIZE;
  private Cell[][] cells;

  public NotedSudoku(float difficulty) {
    Sudoku sudoku = new Sudoku();
    cells = new Cell[SUDOKU_SIZE][SUDOKU_SIZE];
    sudoku.generateModel();
    for (int i = 0; i < SUDOKU_SIZE; ++i) {
      for (int j = 0; j < SUDOKU_SIZE; ++j) {
        cells[i][j] = new Cell(i, j, 0, sudoku.model[i][j]);
      }
    }
    createBlocks(difficulty);
  }

  public NotedSudoku(Cell[][] cells) {
    this.cells = cells;
    var model = new int[SUDOKU_SIZE][SUDOKU_SIZE];
    for (int x = 0; x < SUDOKU_SIZE; ++x) {
      for (int y = 0; y < SUDOKU_SIZE; ++y) {
        model[x][y] = cellCurrentValue(x, y);
      }
    }
    if (!Verifier.verify(model)) {
      throw new IllegalArgumentException("Invalid sudoku given");
    }
  }

  public void revertTo(Snapshot snapshot) {
    cells = snapshot.state;
  }

  private void createBlocks(float percent) {
    int max = SUDOKU_SIZE * SUDOKU_SIZE,
        total = (int) (percent * max);
    if (total == max) {
      --total;
    }
    Cell cell;
    while (total != 0) {
      if (!(cell = getRandomCell()).blocked) {
        cell.setBlocked(true);
        --total;
      }
    }
  }

  private Cell getRandomCell() {
    return getCell(rand.nextInt(SUDOKU_SIZE), rand.nextInt(SUDOKU_SIZE));
  }

  public boolean isFinished() {
    for (int i = 0; i < SUDOKU_SIZE; ++i) {
      for (int j = 0; j < SUDOKU_SIZE; ++j) {
        if (cells[i][j].getState() != State.CORRECT) {
          return false;
        }
      }
    }
    return true;
  }

  public Cell getCell(CachedPoint p) {
    return getCell(p.x, p.y);
  }

  public Cell getCell(int x, int y) {
    return cells[x][y];
  }

  public void clearNotesFrom(CachedPoint target, int n) {
    // Clear notes matching n in row/cols at the same time
    Cell[] cells;
    for (int i = 0; i < SUDOKU_SIZE; ++i) {
      cells = new Cell[]{
          getCell(target.x, i),
          getCell(i, target.y)
      };
      for (Cell cell : cells) {
        if (cell.hasNote(n)) {
          cell.removeNote(n);
        }
      }
    }
    // Clear also from the current block
    var blockIndex = Block.of(target);
    var block = Block.values()[blockIndex];
    Cell cell;
    for (var x : block.rows) {
      for (var y : block.cols) {
        cell = getCell(x, y);
        if (cell.hasNote(n)) {
          cell.removeNote(n);
        }
      }
    }
  }

  public CachedPoint pointFromCoords(int x, int y) {
    return CachedPoint.from((x - x % BLOCK_SIZE) / BLOCK_SIZE,
        (y - y % BLOCK_SIZE) / BLOCK_SIZE);
  }

  private boolean inRangeIndex(int n) {
    return 0 <= n && n < SUDOKU_SIZE;
  }

  private boolean inRange(int n) {
    return 0 < n && n <= SUDOKU_SIZE;
  }

  public int cellCurrentValue(int x, int y) {
    var cell = cells[x][y];
    return cell.blocked ? cell.actual : cell.number;
  }

  public int countCol(int col, int val, boolean notes) {
    if (!inRange(val) || !inRangeIndex(col)) {
      return 0;
    }
    var count = 0;
    for (int i = 0; i < SUDOKU_SIZE; ++i) {
      if ((!notes && cellCurrentValue(col, i) == val) || (notes && getCell(col, i).hasNote(val))) {
        ++count;
      }
    }
    return count;
  }

  public int countRow(int row, int val, boolean notes) {
    if (!inRange(val) || !inRangeIndex(row)) {
      return 0;
    }
    var count = 0;
    for (int i = 0; i < SUDOKU_SIZE; ++i) {
      if ((!notes && cellCurrentValue(i, row) == val) || (notes && getCell(i, row).hasNote(val))) {
        ++count;
      }
    }
    return count;
  }

  public int countBlock(int x, int y, int val, boolean notes) {
    if (!inRangeIndex(x) || !inRangeIndex(y) || !inRange(val)) {
      return 0;
    }
    var blockIndex = Block.of(CachedPoint.from(x, y));
    var block = Block.values()[blockIndex];
    var count = 0;
    for (var i : block.rows) {
      for (var j : block.cols) {
        if ((!notes && cellCurrentValue(i, j) == val) || (notes && getCell(i, j).hasNote(val))) {
          ++count;
        }
      }
    }
    return count;
  }

  public int countPeers(int x, int y, int val, boolean notes) {
    return countRow(y, val, notes) + countCol(x, val, notes) + countBlock(x, y, val, notes);
  }

  public Snapshot snapshot() {
    return new Snapshot();
  }

  public class Snapshot {

    public final Cell[][] state;

    protected Snapshot() {
      state = new Cell[SUDOKU_SIZE][SUDOKU_SIZE];
      for (int i = 0; i < SUDOKU_SIZE; ++i) {
        for (int j = 0; j < SUDOKU_SIZE; ++j) {
          state[i][j] = cells[i][j].clone();
        }
      }
    }
  }
}
