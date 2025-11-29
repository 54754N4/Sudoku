package serial;

import java.util.BitSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import model.Cell;
import model.NotedSudoku;
import model.Sudoku;

public class BitSetTranscoder implements SudokuTranscoder<BitSet> {

  @Override
  public BitSet encodeNumbersExclusively(Sudoku sudoku) {
    var bitset = new BitSet(Storage.BITSET_NUM.sudokuBits());
    var offset = new AtomicInteger();
    var width = NUMBER_BIT_COUNT;
    forEachCellIndices((i, coords) ->
        writeIntoBitset(bitset, encode(sudoku.model[coords.x][coords.y]), width, offset.getAndAdd(width)));
    return bitset;
  }

  @Override
  public Sudoku decodeNumbersExclusively(BitSet encoded) {
    var expected = Storage.BITSET_NUM.sudokuBits();
    if (encoded.length() < expected) {
      throw new IllegalArgumentException(("Not enough bits, expected " + expected + " but got "  + encoded.length()));
    }
    var model = new int[Sudoku.SIZE][Sudoku.SIZE];
    var offset = new AtomicInteger();
    var width = NUMBER_BIT_COUNT;
    forEachCellIndices((i, coords) ->
        model[coords.x][coords.y] = decode(readFromBitset(encoded, width, offset.getAndAdd(width))));
    return new Sudoku(model);
  }

  @Override
  public BitSet encodeAll(NotedSudoku sudoku) {
    var bitset = new BitSet(Storage.BITSET_ALL.sudokuBits());
    var offset = new AtomicInteger();
    var width = MAX_BIT_COUNT;
    forEachCellIndices((i, coords) ->
        writeIntoBitset(bitset, encode(sudoku.getCell(coords.x, coords.y)), width, offset.getAndAdd(width)));
    return bitset;
  }

  @Override
  public NotedSudoku decodeAll(BitSet encoded) {
    var expected = Storage.BITSET_ALL.sudokuBits();
    if (encoded.length() < expected) {
      throw new IllegalArgumentException(("Not enough bits, expected " + expected + " but got "  + encoded.length()));
    }
    var cells = new Cell[Sudoku.SIZE][Sudoku.SIZE];
    var offset = new AtomicInteger();
    var width = MAX_BIT_COUNT;
    forEachCellIndices((i, coords) ->
        cells[coords.x][coords.y] = decode(i, readFromBitset(encoded, width, offset.getAndAdd(width))));
    return new NotedSudoku(cells);
  }

  public static void writeIntoBitset(BitSet sink, int number, int bits, int offset) {
    IntStream.range(0, bits)
        .forEach(i -> sink.set(i + offset, (number & (1 << i)) >> i == 1));
  }

  public static short readFromBitset(BitSet source, int bits, int offset) {
    short number = 0;
    for (int i=0; i<bits; ++i)
      if (source.get(i + offset))
        number |= (short) (1 << i);
    return number;
  }
}
