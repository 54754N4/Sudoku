package serial;

import java.util.function.BiConsumer;
import java.util.stream.IntStream;
import model.CachedPoint;
import model.Cell;
import model.NotedSudoku;
import model.Sudoku;

/**
 * The bit format for each cell is as follows <b>buuuuooooooooo</b> where:
 * <p>
*  <pre>
 * - b: Marker bit to say if cell is blocked or not.
 * - u: Number bits ([0, 2^4-1])
 * - o: Notes bits (1 for each digit present)
 * Each cell can take up to 14 bits out of the 16 bits from a short.
 * </pre>
 */
public interface SudokuTranscoder<T> {
  int CELLS = Sudoku.SIZE * Sudoku.SIZE;

  int NOTES_BIT_COUNT = 9;
  int NUMBER_BIT_COUNT = 4;
  int BLOCKED_BIT_COUNT = 1;
  int MAX_BIT_COUNT = BLOCKED_BIT_COUNT + NUMBER_BIT_COUNT + NOTES_BIT_COUNT;

  int NOTES_OFFSET = 0;
  int NUMBER_OFFSET = NOTES_BIT_COUNT;
  int BLOCKED_OFFSET = NUMBER_BIT_COUNT + NUMBER_OFFSET;

  int NOTES_MASK = ((1 << NOTES_BIT_COUNT) - 1) << NOTES_OFFSET;
  int NUMBER_MASK = ((1 << NUMBER_BIT_COUNT) - 1) << NUMBER_OFFSET;
  int BLOCKED_MASK = ((1 << BLOCKED_BIT_COUNT) - 1) << BLOCKED_OFFSET;
  int MASK = BLOCKED_MASK | NUMBER_MASK | NOTES_MASK;  // e.g. 1 << MAX_BIT_COUNT - 1;

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

  default short encode(int number) {
    return (short) (1 << BLOCKED_OFFSET | number << NUMBER_OFFSET);
  }

  default int decode(short encoded) {
    return (encoded & NUMBER_MASK) >> NUMBER_OFFSET;
  }

  default short encode(Cell cell) {
    var isBlocked = cell.blocked ? 1 : 0;
    var number = cell.blocked ? cell.actual : cell.number;
    var notes = IntStream.range(0, NOTES_BIT_COUNT)
        .reduce(0, (res, i) -> cell.hasNote(i) ?
            res | 1 << (NOTES_BIT_COUNT - i) :
            res);
    var data = (short) (isBlocked << BLOCKED_OFFSET);
    data |= (short) (number << NUMBER_OFFSET);
    data |= (short) (notes << NOTES_OFFSET);
    return data;
  }

  default Cell decode(int index, short encoded) {
    var isBlocked = (encoded & BLOCKED_MASK) >> BLOCKED_OFFSET > 0;
    var number = ((encoded & NUMBER_MASK) >> NUMBER_OFFSET);
    var notes = new boolean[NOTES_BIT_COUNT];
    var noteBits = (encoded & NOTES_MASK) >> NOTES_OFFSET;
    for (int i = 0; i < NOTES_BIT_COUNT; ++i) {
      var offset = NOTES_BIT_COUNT - i;
      notes[i] = (noteBits & (1 << offset)) >> offset == 1;
    }
    var coords = CachedPoint.coordsOf(index);
    var cell = new Cell(coords.x, coords.y, 0, number);
    if (isBlocked) {
      cell.setBlocked(true);
    }
    for (int i = 0; i < NOTES_BIT_COUNT; ++i) {
      if (notes[i]) {
        cell.setNote(i);
      }
    }
    return cell;
  }

  default void forEachCellIndices(BiConsumer<Integer, CachedPoint> consumer) {
    IntStream.range(0, CELLS)
        .forEach(i -> consumer.accept(i, CachedPoint.coordsOf(i)));
  }

  static int upperBoundPowerOf2(int max) {
    var pow = 0;
    while (1 << pow < max) {
      ++pow;
    }
    return pow;
  }
}
