package model;

import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.stream.IntStream;
import view.SudokuPanel;

public class Cell implements Serializable {

  @Serial
  private static final long serialVersionUID = 1446121313411739282L;
  public static final int SUDOKU_SIZE = SudokuPanel.SUDOKU_SIZE;

  public static enum State {EMPTY, CORRECT, INCORRECT}

  ;
  public final Point coords;
  public final boolean[] notes;
  public boolean blocked;
  public final int actual;
  public int number;

  public Cell(int x, int y, int number, int actual) {
    this.actual = actual;
    this.number = number;
    notes = new boolean[SUDOKU_SIZE];
    clearNotes();
    coords = Point.from(x, y);
  }

  public Cell clone() {
    Cell cell = new Cell(coords.x, coords.y, number, actual);
    cell.setBlocked(blocked);
    for (int i = 1; i <= notes.length; ++i) {
      if (hasNote(i)) {
        cell.setNote(i);
      }
    }
    return cell;
  }

  public void setBlocked(boolean blocked) {
    this.blocked = blocked;
    if (blocked) {
      this.number = actual;
    }
  }

  public void clear() {
    clearNotes();
    number = 0;
  }

  private void clearNotes() {
    Arrays.fill(notes, false);
  }

  public State getState() {
    if (number == 0) {
      return State.EMPTY;
    } else if (number != actual) {
      return State.INCORRECT;
    } else {
      return State.CORRECT;
    }
  }

  public boolean hasNotes() {
    boolean hasNote = false;
    for (boolean note : notes) {
      hasNote |= note;
    }
    return hasNote;
  }

  public boolean hasNote(int i) {
    if (i <= 0 || i > notes.length) {
      throw new IllegalArgumentException("Invalid note index : " + i);
    }
    return notes[i - 1];
  }

  public boolean hasNoteExclusively(int i) {
    if (i <= 0 || i > notes.length) {
      throw new IllegalArgumentException("Invalid note index : " + i);
    }
    return notes[i - 1] && IntStream.range(0, notes.length)
        .filter(j -> j == i - 1)
        .mapToObj(j -> notes[j])
        .noneMatch(b -> b);
  }

  private void setNote(int i, boolean value) {
    if (i <= 0 || i > notes.length) {
      throw new IllegalArgumentException("Invalid note index : " + i);
    }
    if (blocked) {
      return;
    }
    notes[i - 1] = value;
  }

  public void setNote(int i) {
    setNote(i, true);
  }

  public void removeNote(int i) {
    setNote(i, false);
  }

  public void setNumber(int n) {
    if (!blocked) {
      this.number = n;
      if (hasNotes()) {
        clearNotes();
      }
    }
  }

  public String toString() {
    return String.format("(%s)[%d-%d]", coords, actual, number);
  }
}
