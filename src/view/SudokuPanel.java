package view;

import controller.Controller;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Line2D;
import java.io.Serial;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.IntStream;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import model.CachedPoint;
import model.Cell;
import model.Cell.State;

public class SudokuPanel extends JPanel implements MouseListener {

  @Serial
  private static final long serialVersionUID = -1281628670459625754L;
  public static final int BLOCK_SIZE = 70, SUDOKU_SIZE = 9, SLIM = 1, THICK = 3;
  public static final Color
      HIGHLIGHTED_COLOR = Color.RED,
      NORMAL_COLOR = Color.BLACK,
      BLOCKED_COLOR = Color.DARK_GRAY;
  public static final Font
      BIG_FONT = new Font("Verdana", Font.BOLD, 50),
      SMALL_FONT = new Font("Verdana", Font.PLAIN, 12);
  public static final CachedPoint POINT_MIDDLE = CachedPoint.from(SUDOKU_SIZE / 2, SUDOKU_SIZE / 2);
  private final float difficulty;
  private final SudokuFrame frame;
  private Controller controller;
  private ActionMap actionMap;
  private InputMap inputMap;
  private LocalTime start;
  private CachedPoint selected;
  private int highlight;

  public SudokuPanel(float difficulty, SudokuFrame frame) {
    this.frame = frame;
    this.difficulty = difficulty;
    setSize(getPreferredSize());
    setKeyBindings();
    addMouseListener(this);
    reset();
  }

  @Override
  public Dimension getPreferredSize() {
    var size = BLOCK_SIZE * SUDOKU_SIZE;
    return new Dimension(size, size);
  }

  private void setHighlighted(int num) {
    if (highlight == num) {
      highlight = 0;
    } else {
      highlight = num;
    }
  }

  private void reset() {
    setHighlighted(0);
    controller = new Controller(difficulty);
    selected = POINT_MIDDLE;
    start = LocalTime.now();
  }

  public void win() {
    var duration = Duration.between(LocalTime.now(), start);
    var output = String.format("Completed in %s",
        LocalTime.MIDNIGHT  // since it's equivalent to 00:00:00
            .minus(duration)
            .format(DateTimeFormatter.ofPattern("HH:mm:ss")));
    frame.setTitle(output);
    frame.alert(output);
  }

  // Drawing code

  @Override
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    drawGrid(g);
    drawCells(g);
    drawSelected(g);
  }

  private void drawSelected(Graphics g) {
    drawSquare(g, selected.x, selected.y, Color.blue);
  }

  private void drawCells(Graphics g) {
    var sudoku = controller.getSudoku();
    for (int x = 0; x < SUDOKU_SIZE; ++x) {
      for (int y = 0; y < SUDOKU_SIZE; ++y) {
        drawCell(g, sudoku.getCell(x, y));
      }
    }
  }

  private void drawCell(Graphics g, Cell cell) {
    if (cell.blocked) {
      g.setColor(BLOCKED_COLOR);
    } else {
      g.setColor(NORMAL_COLOR);
    }
    if (cell.number == highlight) {
      g.setColor(HIGHLIGHTED_COLOR);
    }
    if (cell.number == 0 && cell.hasNotes()) {
      drawNotes(g, cell);
    } else if (cell.number != 0) {
      writeNumber(g, cell.number, cell.coords.x, cell.coords.y);
    }
    if (cell.getState() == State.INCORRECT) {
      drawSquare(g, cell.coords.x, cell.coords.y, Color.red);
    }
  }

  private void drawNotes(Graphics g, Cell cell) {
    for (int i = 0; i < cell.notes.length; ++i) {
      if (cell.notes[i]) {
        writeNote(g, i, cell.coords.x, cell.coords.y);
      }
    }
  }

  private void writeNote(Graphics g, int n, int i, int j) {
    int x = i * BLOCK_SIZE,
        y = j * BLOCK_SIZE,
        SMALL_BLOCK = BLOCK_SIZE / 3;
    CachedPoint d = CachedPoint.coordsOf(n);
    x += d.x * SMALL_BLOCK + 8;
    y += d.y * SMALL_BLOCK + 16;
    if (n + 1 == highlight) {
      g.setColor(HIGHLIGHTED_COLOR);
    } else {
      g.setColor(NORMAL_COLOR);
    }
    g.setFont(SMALL_FONT);
    writeStr(g, "" + (n + 1), x, y);
  }

  private void writeNumber(Graphics g, int n, int i, int j) {
    int x = (int) ((i + 1.0 / 2.0 - 1.0 / 4.0) * BLOCK_SIZE),
        y = (int) ((j + 1.0 / 2.0 + 1.0 / 4.0) * BLOCK_SIZE);
    g.setFont(BIG_FONT);
    writeStr(g, "" + n, x, y);
  }

  private void writeStr(Graphics g, String s, int x, int y) {
    g.drawString(s, x, y);
  }

  private void drawSquare(Graphics g, int i, int j, Color color) {
    g.setColor(color);
    var g2 = (Graphics2D) g;
    g2.setStroke(new BasicStroke(THICK));
    int x = i * BLOCK_SIZE, y = j * BLOCK_SIZE;
    g2.draw(new Line2D.Float(x, y, x + BLOCK_SIZE, y));
    g2.draw(new Line2D.Float(x, y, x, y + BLOCK_SIZE));
    g2.draw(new Line2D.Float(x + BLOCK_SIZE, y, x + BLOCK_SIZE, y + BLOCK_SIZE));
    g2.draw(new Line2D.Float(x, y + BLOCK_SIZE, x + BLOCK_SIZE, y + BLOCK_SIZE));
  }

  private void drawGrid(Graphics g) {
    int stroke;
    for (int i = 1; i < SUDOKU_SIZE; i++) {
      if (i % 3 == 0) {
        stroke = THICK;
      } else {
        stroke = SLIM;
      }
      var g2 = (Graphics2D) g;
      g2.setStroke(new BasicStroke(stroke));
      g2.setColor(NORMAL_COLOR);
      g2.draw(new Line2D.Float(BLOCK_SIZE * i, 0, BLOCK_SIZE * i, BLOCK_SIZE * SUDOKU_SIZE));
      g2.draw(new Line2D.Float(0, BLOCK_SIZE * i, BLOCK_SIZE * SUDOKU_SIZE, BLOCK_SIZE * i));
    }
  }

  // Click handling

  @Override
  public void mouseClicked(MouseEvent e) {
    var mousePoint = e.getPoint();
    selected = controller.getSudoku()
        .pointFromCoords(mousePoint.x, mousePoint.y);
    repaint();
  }

  @Override
  public void mouseEntered(MouseEvent arg0) {
  }

  @Override
  public void mouseExited(MouseEvent arg0) {
  }

  @Override
  public void mousePressed(MouseEvent arg0) {
  }

  @Override
  public void mouseReleased(MouseEvent arg0) {
  }

  // Key handling

  private void setKeyBindings() {
    inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
    actionMap = getActionMap();
    setDirectionalBindings();
    setNumberBindings();
    setDeleteBindings();
    setActionBindings();
    setHighlightBindings();
    setNewGameBinding();
    setAutoBindings();
  }

  private void bind(String name, KeyStroke ks, AbstractAction aa) {
    inputMap.put(ks, name);
    actionMap.put(name, aa);
  }

  // Key binding names =v so fucking many

  public static final String
      LEFT = "LEFT", RIGHT = "RIGHT", UP = "UP", DOWN = "DOWN",
      ONE = "ONE", TWO = "TWO", THREE = "THREE", FOUR = "FOUR", FIVE = "FIVE",
      SIX = "SIX", SEVEN = "SEVEN", EIGHT = "EIGHT", NINE = "NINE",
      SHIFT = "SHIFT", ALT = "ALT", NUMPAD = "NUMPAD", BACKSPACE = "BACKSPACE",
      DELETE = "DELETE", REDO = "REDO", UNDO = "UNDO", RESET = "RESET", EXIT = "EXIT",
      AUTO_NOTES = "AUTO_NOTES", AUTO_SINGLES = "AUTO_SINGLES";

  public static final String[] NUMBERS = {ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE};
  public static final String[] DIRECTIONS = {LEFT, RIGHT, UP, DOWN};
  public static final String[] MODIFIERS = {SHIFT, NUMPAD, ALT};

  private void setNewGameBinding() {
    bind(RESET, KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, InputEvent.CTRL_DOWN_MASK),
        new ResetAction());
    bind(EXIT, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), new ExitAction());
  }

  private void setAutoBindings() {
    bind(AUTO_NOTES, KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK),
        new AutoNotesAction());
    bind(AUTO_SINGLES, KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK),
        new AutoSinglesAction());
  }

  private void setHighlightBindings() {
    for (int i = 0; i < NUMBERS.length; ++i) {
      var highlightAction = new HighlightAction(i + 1);
      bind(ALT.concat(NUMBERS[i]), KeyStroke.getKeyStroke("alt " + (i + 1)), highlightAction);
      bind(ALT.concat(NUMPAD).concat(NUMBERS[i]), KeyStroke.getKeyStroke("alt NUMPAD" + (i + 1)),
          highlightAction);
    }
  }

  private void setActionBindings() {
    bind(UNDO, KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK), new UndoAction());
    bind(REDO, KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK), new RedoAction());
  }

  private void setDeleteBindings() {
    var deleteSingle = new DeleteAction(false);
    var deleteAll = new DeleteAction(true);
    bind(DELETE, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), deleteSingle);
    bind(BACKSPACE, KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), deleteSingle);
    bind(SHIFT.concat(DELETE),
        KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, InputEvent.SHIFT_DOWN_MASK), deleteAll);
    bind(SHIFT.concat(BACKSPACE), KeyStroke.getKeyStroke("shift BACK_SPACE"), deleteAll);
  }

  private void setDirectionalBindings() {
    MoveAction left = new MoveAction(-1, 0),
        right = new MoveAction(1, 0),
        up = new MoveAction(0, -1),
        down = new MoveAction(0, 1);
    bind(LEFT, KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), left);
    bind(SHIFT.concat(LEFT), KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.SHIFT_DOWN_MASK),
        left);
    bind(RIGHT, KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), right);
    bind(SHIFT.concat(RIGHT), KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.SHIFT_DOWN_MASK),
        right);
    bind(UP, KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), up);
    bind(SHIFT.concat(UP), KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.SHIFT_DOWN_MASK), up);
    bind(DOWN, KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), down);
    bind(SHIFT.concat(DOWN), KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.SHIFT_DOWN_MASK),
        down);
  }

  private void setNumberBindings() {
    IntStream.range(0, NUMBERS.length)
        .forEach(i -> {
          var number = i + 1;
          var name = NUMBERS[i];
          var numAction = new NumberAction(number);
          var noteAction = new NoteAction(number);
          bind(name, KeyStroke.getKeyStroke(String.valueOf(number)), numAction);
          bind(NUMPAD.concat(name), KeyStroke.getKeyStroke("NUMPAD" + number), numAction);
          bind(SHIFT.concat(name), KeyStroke.getKeyStroke("shift " + number), noteAction);
          bind(SHIFT.concat(NUMPAD).concat(name), KeyStroke.getKeyStroke("shift NUMPAD" + number),
              noteAction);
        });
  }

  private abstract class SudokuAction extends AbstractAction {

    @Serial
    private static final long serialVersionUID = -7269093027030875306L;

    protected abstract void execute();

    @Override
    public void actionPerformed(ActionEvent arg0) {
      execute();
      repaint();
    }

  }

  private class ResetAction extends SudokuAction {

    @Serial
    private static final long serialVersionUID = -4587914074948045420L;

    @Override
    protected void execute() {
      reset();
    }

  }

  private class HighlightAction extends SudokuAction {

    @Serial
    private static final long serialVersionUID = 1042231741413338460L;

    private final int number;

    private HighlightAction(int number) {
      this.number = number;
    }

    @Override
    protected void execute() {
      setHighlighted(number);
    }

  }

  private class RedoAction extends SudokuAction {

    @Serial
    private static final long serialVersionUID = -3362384870309844128L;

    @Override
    public void execute() {
      controller.redo();
    }

  }

  private class UndoAction extends SudokuAction {

    @Serial
    private static final long serialVersionUID = -4999186335589061691L;

    @Override
    public void execute() {
      controller.undo();
    }

  }

  private class DeleteAction extends SudokuAction {

    @Serial
    private static final long serialVersionUID = -5114510976879561134L;
    private final boolean shift;

    private DeleteAction(boolean shift) {
      this.shift = shift;
    }

    @Override
    public void execute() {
      if (shift) {
        controller.deleteAll();
      } else {
        controller.delete(selected.x, selected.y);
      }
    }

  }

  private class NumberAction extends SudokuAction {

    @Serial
    private static final long serialVersionUID = 3491000804356446973L;

    protected int number;

    private NumberAction(int number) {
      this.number = number;
    }

    @Override
    public void execute() {
      controller.addNumber(selected, number);
      if (controller.getSudoku().isFinished()) {
        win();
      }
    }

  }

  private class NoteAction extends NumberAction {

    @Serial
    private static final long serialVersionUID = 4741555853672685832L;

    private NoteAction(int number) {
      super(number);
    }

    @Override
    public void execute() {
      controller.addNote(selected, number);
    }
  }

  private class MoveAction extends SudokuAction {

    @Serial
    private static final long serialVersionUID = 2038420213006370108L;
    private final int dx, dy;

    private MoveAction(int dx, int dy) {
      this.dx = dx;
      this.dy = dy;
    }

    @Override
    public void execute() {
      selected = selected.plusEquals(dx, dy, SUDOKU_SIZE);
    }

  }

  private class AutoNotesAction extends SudokuAction {

    @Override
    protected void execute() {
      controller.addAllNotes();
    }
  }

  private class AutoSinglesAction extends SudokuAction {

    @Override
    protected void execute() {
      controller.addAllSingles();
    }
  }

  private class ExitAction extends SudokuAction {

    @Override
    protected void execute() {
      System.exit(0);
    }
  }
}