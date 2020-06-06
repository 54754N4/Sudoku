package view;

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
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import controller.Controller;
import model.Block;
import model.Cell;
import model.Cell.State;
import model.Point;

public class SudokuPanel extends JPanel implements MouseListener {
	private static final long serialVersionUID = -1281628670459625754L;
	
	public static final Font BIG_FONT = new Font("Verdana", Font.BOLD, 50), SMALL_FONT = new Font("Verdana", Font.PLAIN, 12);
	public static final Color HIGHLIGHTED_COLOR = Color.red, NORMAL_COLOR = Color.black, BLOCKED_COLOR = Color.gray;
	public static final int BLOCK_SIZE = 70, SUDOKU_SIZE = 9, SLIM = 1, THICK = 3;
	
	private Controller controller;
	private ActionMap actionMap;
	private InputMap inputMap;
	private SudokuFrame frame;
	private LocalTime start;
	private Point selected;
	private int highlight, percent;
	
	public SudokuPanel(int percent, SudokuFrame frame) {
		this.frame = frame;
		this.percent = percent;	// difficulty
		setSize(getPreferredSize());
		setKeyBindings();
		addMouseListener(this);
		reset();
	}
	
	@Override
	public Dimension getPreferredSize() {
		return new Dimension(BLOCK_SIZE*SUDOKU_SIZE, BLOCK_SIZE*SUDOKU_SIZE);
	}
	
	private void setHighlighted(int num) {
		if (highlight == num) highlight = 0;
		else highlight = num;
	}
	
	private void reset() {
		setHighlighted(0);
		controller = new Controller(percent);
		selected = new Point(4, 4);
		start = LocalTime.now();
	}
	
	public void win() {
		Duration duration = Duration.between(LocalTime.now(), start); 
		String output = String.format("Completed in %s", 
			LocalTime.MIDNIGHT	// since it's equivalent to 00:00:00
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
		for (int x=0; x<SUDOKU_SIZE; x++) 
			for (int y=0; y<SUDOKU_SIZE; y++)
				drawCell(g, controller.getCell(new Point(x, y)));
	}

	private void drawCell(Graphics g, Cell cell) {
		if (cell.blocked) g.setColor(BLOCKED_COLOR); 
		else g.setColor(NORMAL_COLOR);
		if (cell.number == highlight) g.setColor(HIGHLIGHTED_COLOR);
		if (cell.number == 0 && cell.hasNotes()) 
			drawNotes(g, cell);
		else if (cell.number != 0) 
			writeNumber(g, cell.number, cell.coords.x, cell.coords.y);
		if (cell.getState() == State.INCORRECT)
			drawSquare(g, cell.coords.x, cell.coords.y, Color.red);
	}
	
	private void drawNotes(Graphics g, Cell cell) {
		for (int i=0; i<cell.notes.length; i++)
			if (cell.notes[i])
				writeNote(g, i, cell.coords.x, cell.coords.y);
	}
	
	private void writeNote(Graphics g, int n, int i, int j) {
		int x = (int) ((i)*BLOCK_SIZE),
			y = (int) ((j)*BLOCK_SIZE),
			SMALL_BLOCK = BLOCK_SIZE/3;
		Point d = Block.coordsOf(n);
		x += d.x * SMALL_BLOCK + 8;
		y += d.y * SMALL_BLOCK + 16;
		if (n+1 == highlight) g.setColor(HIGHLIGHTED_COLOR); 
		else g.setColor(NORMAL_COLOR);
		g.setFont(SMALL_FONT);
		writeStr(g, ""+(n+1), x, y);
	}

	private void writeNumber(Graphics g, int n, int i, int j) {
		int x = (int) ((i+1.0/2.0-1.0/4.0)*BLOCK_SIZE),
			y = (int) ((j+1.0/2.0+1.0/4.0)*BLOCK_SIZE);
		g.setFont(BIG_FONT);
		writeStr(g, ""+n, x, y);
	}

	private void writeStr(Graphics g, String s, int x, int y) {
		g.drawString(s, x, y);
	}
	
	private void drawSquare(Graphics g, int i, int j, Color color) {
		g.setColor(color);
		Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(THICK));
        int x = i*BLOCK_SIZE, y = j*BLOCK_SIZE;
        g2.draw(new Line2D.Float(x, y, x+BLOCK_SIZE, y));
        g2.draw(new Line2D.Float(x, y, x, y+BLOCK_SIZE));
        g2.draw(new Line2D.Float(x+BLOCK_SIZE, y, x+BLOCK_SIZE, y+BLOCK_SIZE));
        g2.draw(new Line2D.Float(x, y+BLOCK_SIZE, x+BLOCK_SIZE, y+BLOCK_SIZE));
	}
	
	private void drawGrid(Graphics g) {
		int stroke;
		for (int i=1; i<SUDOKU_SIZE; i++) {
			if (i%3==0) stroke = THICK;
			else stroke = SLIM;
			Graphics2D g2 = (Graphics2D) g;
            g2.setStroke(new BasicStroke(stroke));
            g2.setColor(NORMAL_COLOR);
            g2.draw(new Line2D.Float(BLOCK_SIZE*i, 0, BLOCK_SIZE*i, BLOCK_SIZE*SUDOKU_SIZE));
            g2.draw(new Line2D.Float(0, BLOCK_SIZE*i, BLOCK_SIZE*SUDOKU_SIZE, BLOCK_SIZE*i));
		}
	}
	
	// Click handling

	@Override
	public void mouseClicked(MouseEvent e) {
		Point at = new Point(e.getPoint());
		selected = controller.pointFromCoords(at.x, at.y);
		repaint();
	}
	
	@Override public void mouseEntered(MouseEvent arg0) {}
	@Override public void mouseExited(MouseEvent arg0) {}
	@Override public void mousePressed(MouseEvent arg0) {}
	@Override public void mouseReleased(MouseEvent arg0) {}
	
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
	}
	
	private void bind(String name, KeyStroke ks, AbstractAction aa) {
		inputMap.put(ks, name);
		actionMap.put(name, aa);
	}
	
	private void setNewGameBinding() {
		bind(RESET, KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0), new ResetAction());
	}

	private void setHighlightBindings() {
		String[] names = {ALT_ONE, ALT_TWO, ALT_THREE, ALT_FOUR, ALT_FIVE, ALT_SIX, ALT_SEVEN, ALT_EIGHT, ALT_NINE};
		for (int i=0; i<names.length; i++) 
			bind(names[i], KeyStroke.getKeyStroke("alt pressed "+(i+1)), new HighlightAction(i+1));
	}
	
	private void setActionBindings() {
		bind(REDO, KeyStroke.getKeyStroke(KeyEvent.VK_X, 0), new RedoAction());
		bind(UNDO, KeyStroke.getKeyStroke(KeyEvent.VK_Z, 0), new UndoAction());
	}
	
	private void setDeleteBindings() {
		bind(DELETE, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), new DeleteAction(false));
		bind(SHIFT_DELETE, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, InputEvent.SHIFT_MASK), new DeleteAction(true));
	}

	private void setDirectionalBindings() {
		bind(LEFT, KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), new MoveAction(-1, 0));
		bind(SHIFT_LEFT, KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.SHIFT_MASK), new MoveAction(-1, 0));
		bind(RIGHT, KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), new MoveAction(1, 0));
		bind(SHIFT_RIGHT, KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.SHIFT_MASK), new MoveAction(1, 0));
		bind(UP, KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), new MoveAction(0, -1));
		bind(SHIFT_UP, KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.SHIFT_MASK), new MoveAction(0, -1));
		bind(DOWN, KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), new MoveAction(0, 1));
		bind(SHIFT_DOWN, KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.SHIFT_MASK), new MoveAction(0, 1));
	}
	
	private void setNumberBindings() {
		String[] names = {
			ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE,
			SHIFT_ONE, SHIFT_TWO, SHIFT_THREE, SHIFT_FOUR, SHIFT_FIVE, SHIFT_SIX, SHIFT_SEVEN, SHIFT_EIGHT, SHIFT_NINE,
			NUMPAD_ONE, NUMPAD_TWO, NUMPAD_THREE, NUMPAD_FOUR, NUMPAD_FIVE, NUMPAD_SIX, NUMPAD_SEVEN, NUMPAD_EIGHT, NUMPAD_NINE,
			SHIFT_NUMPAD_ONE, SHIFT_NUMPAD_TWO, SHIFT_NUMPAD_THREE, SHIFT_NUMPAD_FOUR, SHIFT_NUMPAD_FIVE, SHIFT_NUMPAD_SIX, SHIFT_NUMPAD_SEVEN, SHIFT_NUMPAD_EIGHT, SHIFT_NUMPAD_NINE
		}, keystrokes = {
			"pressed 1", "pressed 2", "pressed 3", "pressed 4", "pressed 5", "pressed 6", "pressed 7", "pressed 8", "pressed 9",
			"shift pressed 1", "shift pressed 2", "shift pressed 3", "shift pressed 4", "shift pressed 5", "shift pressed 6", "shift pressed 7", "shift pressed 8", "shift pressed 9",
			"pressed NUMPAD1", "pressed NUMPAD2", "pressed NUMPAD3", "pressed NUMPAD4", "pressed NUMPAD5", "pressed NUMPAD6", "pressed NUMPAD7", "pressed NUMPAD8", "pressed NUMPAD9",
			"shift pressed NUMPAD1", "shift pressed NUMPAD2", "shift pressed NUMPAD3", "shift pressed NUMPAD4", "shift pressed NUMPAD5", "shift pressed NUMPAD6", "shift pressed NUMPAD7", "shift pressed NUMPAD8", "shift pressed NUMPAD9"
		};
		AbstractAction[] actions = {
			new NumberAction(1), new NumberAction(2), new NumberAction(3), new NumberAction(4), new NumberAction(5), new NumberAction(6), new NumberAction(7), new NumberAction(8), new NumberAction(9),
			new NoteAction(1), new NoteAction(2), new NoteAction(3), new NoteAction(4), new NoteAction(5), new NoteAction(6), new NoteAction(7), new NoteAction(8), new NoteAction(9),
			new NumberAction(1), new NumberAction(2), new NumberAction(3), new NumberAction(4), new NumberAction(5), new NumberAction(6), new NumberAction(7), new NumberAction(8), new NumberAction(9),
			new NoteAction(1), new NoteAction(2), new NoteAction(3), new NoteAction(4), new NoteAction(5), new NoteAction(6), new NoteAction(7), new NoteAction(8), new NoteAction(9)
		}; 
		for (int i=0; i<actions.length; i++) 
			bind(names[i], KeyStroke.getKeyStroke(keystrokes[i]), actions[i]);
	}
	
	// Key binding names =v so fucking many
	private static final String LEFT = "LEFT", RIGHT = "RIGHT", UP = "UP", DOWN = "DOWN", 
			SHIFT_LEFT = "SHIFT_LEFT", SHIFT_RIGHT = "SHIFT_RIGHT", SHIFT_UP = "SHIFT_UP", SHIFT_DOWN = "SHIFT_DOWN",
			ONE = "ONE", TWO = "TWO", THREE = "THREE", FOUR = "FOUR", FIVE = "FIVE", 
			SIX = "SIX", SEVEN = "SEVEN", EIGHT = "EIGHT", NINE = "NINE",
			NUMPAD_ONE = "NUMPAD_ONE", NUMPAD_TWO = "NUMPAD_TWO", NUMPAD_THREE = "NUMPAD_THREE", NUMPAD_FOUR = "NUMPAD_FOUR", NUMPAD_FIVE = "NUMPAD_FIVE", 
			NUMPAD_SIX = "NUMPAD_SIX", NUMPAD_SEVEN = "NUMPAD_SEVEN", NUMPAD_EIGHT = "NUMPAD_EIGHT", NUMPAD_NINE = "NUMPAD_NINE",
			SHIFT_ONE = "SHIFT_ONE", SHIFT_TWO = "SHIFT_TWO", SHIFT_THREE = "SHIFT_THREE", SHIFT_FOUR = "SHIFT_FOUR", SHIFT_FIVE = "SHIFT_FIVE", 
			SHIFT_SIX = "SHIFT_SIX", SHIFT_SEVEN = "SHIFT_SEVEN", SHIFT_EIGHT = "SHIFT_EIGHT", SHIFT_NINE = "SHIFT_NINE",
			SHIFT_NUMPAD_ONE = "SHIFT_NUMPAD_ONE", SHIFT_NUMPAD_TWO = "SHIFT_NUMPAD_TWO", SHIFT_NUMPAD_THREE = "SHIFT_NUMPAD_THREE", SHIFT_NUMPAD_FOUR = "SHIFT_NUMPAD_FOUR", SHIFT_NUMPAD_FIVE = "SHIFT_NUMPAD_FIVE", 
			SHIFT_NUMPAD_SIX = "SHIFT_NUMPAD_SIX", SHIFT_NUMPAD_SEVEN = "SHIFT_NUMPAD_SEVEN", SHIFT_NUMPAD_EIGHT = "SHIFT_NUMPAD_EIGHT", SHIFT_NUMPAD_NINE = "SHIFT_NUMPAD_NINE",
			ALT_ONE = "ALT_ONE", ALT_TWO = "ALT_TWO", ALT_THREE = "ALT_THREE", ALT_FOUR = "ALT_FOUR", ALT_FIVE = "ALT_FIVE", 
			ALT_SIX = "ALT_SIX", ALT_SEVEN = "ALT_SEVEN", ALT_EIGHT = "ALT_EIGHT", ALT_NINE = "ALT_NINE",
			DELETE = "DELETE", SHIFT_DELETE = "SHIFT_DELETE", REDO = "REDO", UNDO = "UNDO", RESET = "RESET";
	
	private abstract class SudokuAction extends AbstractAction {
		private static final long serialVersionUID = -7269093027030875306L;
		
		protected abstract void execute();
		
		@Override
		public void actionPerformed(ActionEvent arg0) {
			execute();
			repaint();
		}
		
	}
	
	private class ResetAction extends SudokuAction {
		private static final long serialVersionUID = -4587914074948045420L;

		@Override
		protected void execute() {
			reset();
		}
		
	}
	
	private class HighlightAction extends SudokuAction {
		private static final long serialVersionUID = 1042231741413338460L;

		private int number;
		
		private HighlightAction(int number) {
			this.number = number;
		}
		
		@Override
		protected void execute() {
			setHighlighted(number);
		}
		
	}
	
	private class RedoAction extends SudokuAction {
		private static final long serialVersionUID = -3362384870309844128L;
		
		@Override
		public void execute() {
			controller.redo();
		}
		
	}
	
	private class UndoAction extends SudokuAction {
		private static final long serialVersionUID = -4999186335589061691L;

		@Override
		public void execute() {
			controller.undo();
		}
		
	}
	
	private class DeleteAction extends SudokuAction {
		private static final long serialVersionUID = -5114510976879561134L;
		private boolean shift;
		
		private DeleteAction(boolean shift) {
			this.shift = shift;
		}
		
		@Override
		public void execute() {
			if (shift) controller.deleteAll();
			else controller.delete(selected);
		}
		
	}
	
	private class NumberAction extends SudokuAction {
		private static final long serialVersionUID = 3491000804356446973L;

		protected int number;
		
		private NumberAction(int number) {
			this.number = number;
		}
		
		@Override
		public void execute() {
			controller.addNumber(selected, number);
			if (controller.isFinished())
				win();
		}
		
	}
	
	private class NoteAction extends NumberAction {
		private static final long serialVersionUID = 4741555853672685832L;

		private NoteAction(int number) {
			super(number);
		}
		
		@Override
		public void execute() {
			controller.addNote(selected, number-1);
		}
	}
	
	private class MoveAction extends SudokuAction {
		private static final long serialVersionUID = 2038420213006370108L;
		private int dx,dy;
		
		private MoveAction(int dx, int dy) {
			this.dx = dx;
			this.dy = dy;
		}
		
		@Override
		public void execute() {
			selected.plusEquals(dx, dy, SUDOKU_SIZE);
		}
		
	}

}