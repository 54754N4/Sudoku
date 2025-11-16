package controller;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Random;

import model.Cell;
import model.Cell.State;
import model.Point;
import model.Sudoku;
import view.SudokuPanel;

public class Controller {
	public static final int
      SUDOKU_SIZE = SudokuPanel.SUDOKU_SIZE,
      BLOCK_SIZE = SudokuPanel.BLOCK_SIZE;
	private final Random rand;
	private final Sudoku sudoku;
	private Cell[][] cells;
	private final Deque<Snapshot> backups, restores;
	
	public Controller(float difficulty) {
		sudoku = new Sudoku();
		rand = new Random();
		backups = new ArrayDeque<>();
		restores = new ArrayDeque<>();
		cells = new Cell[SUDOKU_SIZE][SUDOKU_SIZE];
		sudoku.generateModel();
		for (int i=0; i<SUDOKU_SIZE; ++i)
			for (int j=0; j<SUDOKU_SIZE; ++j)
				cells[i][j] = new Cell(i, j, 0, sudoku.model[i][j]);
		createBlocks(difficulty);
	}
	
	private void createBlocks(float percent) {
		int max = SUDOKU_SIZE * SUDOKU_SIZE,
			total = (int) (percent * max);
		if (total == max)
      --total;
		Cell cell;
		while (total != 0) {
			if (!(cell = getRandomCell()).blocked) {
				cell.setBlocked(true);
        --total;
			}
		}
	}
	
	private Cell getRandomCell() {
		return getCell(
				rand.nextInt(SUDOKU_SIZE),
				rand.nextInt(SUDOKU_SIZE));
	}
	
	private void clearNotesFrom(Point target, int n) {
		Cell[] cells;
		for (int i=0; i<SUDOKU_SIZE; ++i) {
			cells = new Cell[]{ 
				getCell(target.x, i), 
				getCell(i, target.y)
			};
			for (Cell cell : cells)
				if (cell.hasNote(n)) 
					cell.removeNote(n);
		}
	}
	
	public boolean isFinished() {
		for (int i=0; i<SUDOKU_SIZE; ++i)
			for (int j=0; j<SUDOKU_SIZE; ++j)
				if (cells[i][j].getState() != State.CORRECT)
					return false;
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
		return new Point((x - x%BLOCK_SIZE)/BLOCK_SIZE, 
			(y - y%BLOCK_SIZE)/BLOCK_SIZE);
	}
	
	public void addNumber(Point p, int n) {
		backups.add(makeSnapshot());
		Cell cell = getCell(p); 
		if (!cell.blocked) {
			cell.setNumber(n);
			clearNotesFrom(p, n-1);	// note indices are always -1
		}
	}
	
	public void addNote(Point p, int n) {
		backups.add(makeSnapshot());
		Cell cell = getCell(p); 
		if (cell.hasNote(n))
      cell.removeNote(n);
		else
      cell.setNote(n);
	}
	
	public void deleteAll() {
		backups.add(makeSnapshot());
		for (int i=0; i<SUDOKU_SIZE; ++i)
			for (int j=0; j<SUDOKU_SIZE; ++j)
				delete(i, j);
	}
	
	public void delete(Point p) {
		delete(p.x, p.y);
	}
	
	public void delete(int x,  int y) {
		backups.add(makeSnapshot());
		Cell cell = getCell(x, y);
		if (!cell.blocked)
			cell.clear();
	}
	
	private Snapshot makeSnapshot() {
		return new Snapshot();
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
			for (int i=0; i<SUDOKU_SIZE; ++i)
				for (int j=0; j<SUDOKU_SIZE; ++j)
					state[i][j] = cells[i][j].clone();
		}
	}
}
