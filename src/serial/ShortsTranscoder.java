package serial;

import java.util.stream.IntStream;
import model.CachedPoint;
import model.Cell;
import model.NotedSudoku;
import model.Sudoku;

/**
 * The bit format for each cell is as follows 'buuuuooooooooo' where: - b: Marker bit to say if cell
 * is blocked or not. - u: Number bits ([0, 2^4-1]) - o: Notes bits (1 for each digit present) Each
 * cell can take up to 14 bits
 */
public class ShortsTranscoder implements SudokuTranscoder<short[]> {

  public static final int NOTES_BIT_COUNT = 9;
  public static final int NUMBER_BIT_COUNT = 4;
  public static final int BLOCKED_BIT_COUNT = 1;
  public static final int MAX_BIT_COUNT = BLOCKED_BIT_COUNT + NUMBER_BIT_COUNT + NOTES_BIT_COUNT;

  public static final int NOTES_OFFSET = 0;
  public static final int NUMBER_OFFSET = NOTES_BIT_COUNT;
  public static final int BLOCKED_OFFSET = NUMBER_BIT_COUNT + NUMBER_OFFSET;

  public static final int NOTES_MASK = ((1 << NOTES_BIT_COUNT) - 1) << NOTES_OFFSET;
  public static final int NUMBER_MASK = ((1 << NUMBER_BIT_COUNT) - 1) << NUMBER_OFFSET;
  public static final int BLOCKED_MASK = ((1 << BLOCKED_BIT_COUNT) - 1) << BLOCKED_OFFSET;
  public static final int MASK =
      BLOCKED_MASK | NUMBER_MASK | NOTES_MASK;  // e.g. 1 << MAX_BIT_COUNT - 1;

  @Override
  public short[] encodeNumbersExclusively(Sudoku sudoku) {
    var bytes = new short[CELLS];
    for (int i = 0; i < CELLS; ++i) {
      var coords = CachedPoint.coordsOf(i);
      bytes[i] = (short) (1 << BLOCKED_OFFSET | sudoku.model[coords.x][coords.y] << NUMBER_OFFSET);
    }
    return bytes;
  }

  @Override
  public Sudoku decodeNumbersExclusively(short[] encoded) {
    if (encoded.length != CELLS) {
      throw new IllegalArgumentException(
          "Some cells are missing, expecting " + CELLS + " but got " + encoded.length);
    }
    var model = new int[Sudoku.SIZE][Sudoku.SIZE];
    for (int i = 0; i < CELLS; ++i) {
      var point = CachedPoint.coordsOf(i);
      model[point.x][point.y] = (encoded[i] & NUMBER_MASK) >> NUMBER_OFFSET;
    }
    return new Sudoku(model);
  }

  @Override
  public short[] encodeAll(NotedSudoku sudoku) {
    var bytes = new short[CELLS];
    for (int i = 0; i < CELLS; ++i) {
      var coords = CachedPoint.coordsOf(i);
      bytes[i] = encode(sudoku.getCell(coords.x, coords.y));
    }
    return bytes;
  }

  @Override
  public NotedSudoku decodeAll(short[] encoded) {
    if (encoded.length != CELLS) {
      throw new IllegalArgumentException(
          "Some cells are missing, expecting " + CELLS + " but got " + encoded.length);
    }
    var cells = new Cell[Sudoku.SIZE][Sudoku.SIZE];
    for (int i = 0; i < CELLS; ++i) {
      var coords = CachedPoint.coordsOf(i);
      cells[coords.x][coords.y] = decode(i, encoded[i]);
    }
    return new NotedSudoku(cells);
  }

  private short encode(Cell cell) {
    var isBlocked = cell.blocked ? 1 : 0;
    var data = (short) (isBlocked << BLOCKED_OFFSET);
    var number = cell.blocked ? cell.actual : cell.number;
    data |= (short) (number << NUMBER_OFFSET);
    var notes = IntStream.range(0, NOTES_BIT_COUNT)
        .reduce(0, (res, i) -> cell.hasNote(i) ?
            res | 1 << (NOTES_BIT_COUNT - i) :
            res);
    data |= (short) (notes << NOTES_OFFSET);
    return data;
  }

  private Cell decode(int index, short encoded) {
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
}
