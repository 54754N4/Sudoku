package serial;

import model.NotedSudoku;
import model.Sudoku;

/**
 * We need to differentiate between serializing normal sudokus and serializing the one with notes to
 * be able to better implement the undo stack.
 * <p>
 * Sudoku grid = 9 * 9 = 81 cells 1 cell = 1 marker bit to say if it's: - a number: 4 bits [0-12] -
 * or a note: 9 bits, 1 for each note digit We can represent each cell with a short (16 bits) since
 * we'd need at most 10 bits. Using this approach, each sudoku would take 81 * 16 bits = 1296 bits =
 * 162 bytes
 * <p>
 * > Alternative: Look into using a bitset to not have lost/reserved bits space (e.g in the case
 * above we use shorts [16 bits] to store for each cell [when only 10 are needed]). At best, a full
 * sudoku would require 81 * 10 bits = 101.25 bytes; however, if we only want to store numbers then
 * we'd only need 81 * 4 bits = 40.5 bytes.
 */
public interface SudokuTranscoder<T> {

  int CELLS = Sudoku.SIZE * Sudoku.SIZE;

  enum Storage {
    SHORTS(16), BITSET_ALL(10), BITSET_NUM(4);
    final int bits;

    Storage(int bits) {
      this.bits = bits;
    }

    int sudokuBits() {
      return CELLS * bits;
    }

    double sudokuBytes() {
      return sudokuBits() / 8d;
    }
  }

  T encodeNumbersExclusively(Sudoku sudoku);

  Sudoku decodeNumbersExclusively(T encoded);

  T encodeAll(NotedSudoku sudoku);

  NotedSudoku decodeAll(T encoded);

  static int upperBoundPowerOf2(int max) {
    var pow = 0;
    while (1 << pow < max) {
      ++pow;
    }
    return pow;
  }

  static void main(String[] args) {
    System.out.println(Storage.SHORTS.sudokuBytes());
    System.out.println(Storage.BITSET_ALL.sudokuBytes());
    System.out.println(Storage.BITSET_NUM.sudokuBytes());
  }
}