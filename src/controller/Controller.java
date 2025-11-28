package controller;

import java.util.ArrayDeque;
import java.util.Deque;
import model.CachedPoint;
import model.Cell;
import model.Cell.State;
import model.NotedSudoku;
import model.NotedSudoku.Snapshot;
import view.SudokuPanel;

public class Controller {

  public static final int SUDOKU_SIZE = SudokuPanel.SUDOKU_SIZE;
  private final NotedSudoku sudoku;
  private final Deque<Snapshot> backups, restores;
  private boolean takeSnapshots;

  public Controller(float difficulty) {
    sudoku = new NotedSudoku(difficulty);
    backups = new ArrayDeque<>();
    restores = new ArrayDeque<>();
    takeSnapshots = true;
  }

  public NotedSudoku getSudoku() {
    return sudoku;
  }

  public void addNumber(CachedPoint p, int n) {
    makeSnapshot();
    var cell = sudoku.getCell(p);
    if (!cell.blocked) {
      cell.setNumber(n);
      sudoku.clearNotesFrom(p, n);
    }
  }

  public void addNote(CachedPoint p, int n) {
    makeSnapshot();
    var cell = sudoku.getCell(p);
    if (cell.hasNote(n)) {
      cell.removeNote(n);
    } else {
      cell.setNote(n);
    }
  }

  public void addAllNotes() {
    makeSnapshot();
    takeSnapshots = false;
    Cell cell;
    for (int x = 0; x < SUDOKU_SIZE; ++x) {
      for (int y = 0; y < SUDOKU_SIZE; ++y) {
        cell = sudoku.getCell(x, y);
        if (cell.getState() == State.EMPTY) {
          for (int n = 1; n <= SUDOKU_SIZE; ++n) {
            if (sudoku.countPeers(x, y, n, false) == 0 && !cell.hasNote(n)) {
              addNote(CachedPoint.from(x, y), n);
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
          cell = sudoku.getCell(i, j);
          // lonely note in cell
          if (cell.hasNoteExclusively(n)) {
            addNumber(CachedPoint.from(i, j), n);
            break;
          }
          // lonely note in block
          else if (cell.hasNote(n) && sudoku.countBlock(i, j, n, true) == 1
              && sudoku.countBlock(i, j, n, false) == 0) {
            addNumber(CachedPoint.from(i, j), n);
            break;
          }
        }
      }
    }
    // one note in any row or col
    for (int n = 1; n <= SUDOKU_SIZE; ++n) {
      for (int i = 0; i < SUDOKU_SIZE; ++i) {
        if (sudoku.countRow(i, n, true) == 1 && sudoku.countRow(i, n, false) == 0) {
          for (int j = 0; j < SUDOKU_SIZE; ++j) {
            if (sudoku.getCell(j, i).hasNote(n)) {
              addNumber(CachedPoint.from(j, i), n);
              break;
            }
          }
        }
        if (sudoku.countCol(i, n, true) == 1 && sudoku.countCol(i, n, false) == 0) {
          for (int j = 0; j < SUDOKU_SIZE; ++j) {
            if (sudoku.getCell(i, j).hasNote(n)) {
              addNumber(CachedPoint.from(i, j), n);
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

  public void delete(CachedPoint p) {
    delete(p.x, p.y);
  }

  public void delete(int x, int y) {
    makeSnapshot();
    var cell = sudoku.getCell(x, y);
    if (!cell.blocked) {
      cell.clear();
    }
  }

  private void makeSnapshot() {
    if (takeSnapshots) {
      backups.add(sudoku.snapshot());
    }
  }

  public void redo() {
    if (!restores.isEmpty()) {
      var state = restores.removeLast();
      backups.add(state);
      sudoku.revertTo(state);
    }
  }

  public void undo() {
    if (!backups.isEmpty()) {
      var state = backups.removeLast();
      restores.add(state);
      sudoku.revertTo(state);
    }
  }
}
