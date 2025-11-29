package serial;

import model.Cell;
import model.NotedSudoku;
import model.Sudoku;

public class ShortsTranscoder implements SudokuTranscoder<short[]> {

  @Override
  public short[] encodeNumbersExclusively(Sudoku sudoku) {
    var bytes = new short[CELLS];
    forEachCellIndices((i, coords) ->
        bytes[i] = encode(sudoku.model[coords.x][coords.y]));
    return bytes;
  }

  @Override
  public Sudoku decodeNumbersExclusively(short[] encoded) {
    if (encoded.length != CELLS) {
      throw new IllegalArgumentException(
          "Some cells are missing, expecting " + CELLS + " but got " + encoded.length);
    }
    var model = new int[Sudoku.SIZE][Sudoku.SIZE];
    forEachCellIndices((i, coords) ->
        model[coords.x][coords.y] = decode(encoded[i]));
    return new Sudoku(model);
  }

  @Override
  public short[] encodeAll(NotedSudoku sudoku) {
    var bytes = new short[CELLS];
    forEachCellIndices((i, coords) ->
        bytes[i] = encode(sudoku.getCell(coords.x, coords.y)));
    return bytes;
  }

  @Override
  public NotedSudoku decodeAll(short[] encoded) {
    if (encoded.length != CELLS) {
      throw new IllegalArgumentException(
          "Some cells are missing, expecting " + CELLS + " but got " + encoded.length);
    }
    var cells = new Cell[Sudoku.SIZE][Sudoku.SIZE];
    forEachCellIndices((i, coords) ->
        cells[coords.x][coords.y] = decode(i, encoded[i]));
    return new NotedSudoku(cells);
  }
}
