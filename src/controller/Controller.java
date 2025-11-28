package controller;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Random;
import model.Block;
import model.Cell;
import model.Cell.State;
import model.Point;
import model.Sudoku;
import view.SudokuPanel;

public class Controller {

  public static final int
      SUDOKU_SIZE = SudokuPanel.SUDOKU_SIZE,
      BLOCK_SIZE = SudokuPanel.BLOCK_SIZE;
  public static final Random rand = Sudoku.rand;
  private final Sudoku sudoku;
  private Cell[][] cells;
  private final Deque<Snapshot> backups, restores;
  private boolean takeSnapshots;

  public Controller(float difficulty) {
    sudoku = new Sudoku();
    backups = new ArrayDeque<>();
    restores = new ArrayDeque<>();
    cells = new Cell[SUDOKU_SIZE][SUDOKU_SIZE];
    sudoku.generateModel();
    takeSnapshots = true;
    for (int i = 0; i < SUDOKU_SIZE; ++i) {
      for (int j = 0; j < SUDOKU_SIZE; ++j) {
        cells[i][j] = new Cell(i, j, 0, sudoku.model[i][j]);
      }
    }
    createBlocks(difficulty);
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

  private void clearNotesFrom(Point target, int n) {
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

  public Cell[][] getCells() {
    return cells;
  }

  public Cell getCell(Point p) {
    return getCell(p.x, p.y);
  }

  public Cell getCell(int x, int y) {
    return cells[x][y];
  }

  public Point pointFromCoords(int x, int y) {
    return Point.from((x - x % BLOCK_SIZE) / BLOCK_SIZE,
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
    var blockIndex = Block.of(Point.from(x, y));
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

  public void addNumber(Point p, int n) {
    makeSnapshot();
    Cell cell = getCell(p);
    if (!cell.blocked) {
      cell.setNumber(n);
      clearNotesFrom(p, n);
    }
  }

  public void addNote(Point p, int n) {
    makeSnapshot();
    Cell cell = getCell(p);
    if (cell.hasNote(n)) {
      cell.removeNote(n);
    } else {
      cell.setNote(n);
    }
  }

  public void addAllNotes() {
    makeSnapshot();
    takeSnapshots = false;
    for (int x = 0; x < SUDOKU_SIZE; ++x) {
      for (int y = 0; y < SUDOKU_SIZE; ++y) {
        if (cells[x][y].getState() == State.EMPTY) {
          for (int n = 1; n <= SUDOKU_SIZE; ++n) {
            if (countPeers(x, y, n, false) == 0 && !cells[x][y].hasNote(n)) {
              addNote(Point.from(x, y), n);
            }
          }
        }
      }
    }
    takeSnapshots = true;
  }

  public void addAllSingles() {
    makeSnapshot();
    takeSnapshots = false;
    Cell cell;
    for (int i = 0; i < SUDOKU_SIZE; ++i) {
      for (int j = 0; j < SUDOKU_SIZE; ++j) {
        for (int n = 1; n <= SUDOKU_SIZE; ++n) {
          cell = getCell(i, j);
          // lonely note in cell
          if (cell.hasNoteExclusively(n)) {
            addNumber(Point.from(i, j), n);
            break;
          }
          // lonely note in block
          else if (cell.hasNote(n) && countBlock(i, j, n, true) == 1
              && countBlock(i, j, n, false) == 0) {
            addNumber(Point.from(i, j), n);
            break;
          }
        }
      }
    }
    // one note in any row or col
    for (int n = 1; n <= SUDOKU_SIZE; ++n) {
      for (int i = 0; i < SUDOKU_SIZE; ++i) {
        if (countRow(i, n, true) == 1 && countRow(i, n, false) == 0) {
          for (int j = 0; j < SUDOKU_SIZE; ++j) {
            if (getCell(j, i).hasNote(n)) {
              addNumber(Point.from(j, i), n);
              break;
            }
          }
        }
        if (countCol(i, n, true) == 1 && countCol(i, n, false) == 0) {
          for (int j = 0; j < SUDOKU_SIZE; ++j) {
            if (getCell(i, j).hasNote(n)) {
              addNumber(Point.from(i, j), n);
              break;
            }
          }
        }
      }
    }
    takeSnapshots = true;
  }

  public void deleteAll() {
    makeSnapshot();
    for (int i = 0; i < SUDOKU_SIZE; ++i) {
      for (int j = 0; j < SUDOKU_SIZE; ++j) {
        delete(i, j);
      }
    }
  }

  public void delete(Point p) {
    delete(p.x, p.y);
  }

  public void delete(int x, int y) {
    makeSnapshot();
    Cell cell = getCell(x, y);
    if (!cell.blocked) {
      cell.clear();
    }
  }

  private void makeSnapshot() {
    if (takeSnapshots) {
      backups.add(new Snapshot());
    }
  }

  public void redo() {
    if (!restores.isEmpty()) {
      Snapshot s = restores.removeLast();
      backups.add(s);
      cells = s.state;
    }
  }

  public void undo() {
    if (!backups.isEmpty()) {
      Snapshot s = backups.removeLast();
      restores.add(s);
      cells = s.state;
    }
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
