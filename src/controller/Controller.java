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
	public static final int SUDOKU_SIZE = SudokuPanel.SUDOKU_SIZE, 
			 BLOCK_SIZE = SudokuPanel.BLOCK_SIZE;
	private Random rand;
	private Sudoku sudoku;
	private Cell[][] cells;
	private Deque<Snapshot> backups, restores;
	
	public Controller(int percent) {
		sudoku = new Sudoku();
		rand = new Random();
		backups = new ArrayDeque<>();
		restores = new ArrayDeque<>();
		cells = new Cell[SUDOKU_SIZE][SUDOKU_SIZE];
		sudoku.generateModel();
		for (int i=0; i<SUDOKU_SIZE; i++)
			for (int j=0; j<SUDOKU_SIZE; j++)
				cells[i][j] = new Cell(i, j, 0, sudoku.model[i][j]);
		createBlocks(percent);
	}
	
	private void createBlocks(int percent) {
		int max = SUDOKU_SIZE*SUDOKU_SIZE,
			total = (int) (percent/100.0 * max);
		if (total == max) total--;
		Cell cell;
		while (total != 0) {
			if (!(cell = getRandomCell()).blocked) {
				cell.setBlocked(true);
				total--;
			}
		}
	}
	
	private Cell getRandomCell() {
		return getCell(
			new Point(
				rand.nextInt(SUDOKU_SIZE),
				rand.nextInt(SUDOKU_SIZE)));
	}
	
	private void clearNotesFrom(Point target, int n) {
		Cell[] cells;
		for (int i=0; i<SUDOKU_SIZE; i++) {
			cells = new Cell[]{ 
				getCell(new Point(target.x, i)), 
				getCell(new Point(i, target.y))
			};
			for (Cell cell : cells)
				if (cell.hasNote(n)) 
					cell.removeNote(n);
		}
	}
	
	public boolean isFinished() {
		for (int i=0; i<SUDOKU_SIZE; i++)
			for (int j=0; j<SUDOKU_SIZE; j++)
				if (cells[i][j].getState() != State.CORRECT)
					return false;
		return true;
	}
	
	public Cell[][] getCells() {
		return cells;
	}
	
	public Cell getCell(Point p) {
		return cells[p.x][p.y];
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
		if (cell.hasNote(n)) cell.removeNote(n);
		else cell.setNote(n);
	}
	
	public void deleteAll() {
		backups.add(makeSnapshot());
		for (int i=0; i<SUDOKU_SIZE; i++)
			for (int j=0; j<SUDOKU_SIZE; j++)
				delete(new Point(i,j));
	}
	
	public void delete(Point p) {
		backups.add(makeSnapshot());
		Cell cell = getCell(p);
		if (!cell.blocked)
			cell.clear();
	}
	
	private Snapshot makeSnapshot() {
		return new Snapshot();
	}
	
	public void redo() {
		if (restores.size() != 0) {
			Snapshot s = restores.removeLast(); 
			backups.add(s);
			cells = s.state;
		}
	}
	
	public void undo() {
		if (backups.size() != 0) {
			Snapshot s = backups.removeLast(); 
			restores.add(s);
			cells = s.state;
		}
	}
	
	public class Snapshot {
		public final Cell[][] state;
		
		protected Snapshot() {
			state = new Cell[SUDOKU_SIZE][SUDOKU_SIZE];
			for (int i=0; i<SUDOKU_SIZE; i++)
				for (int j=0; j<SUDOKU_SIZE; j++)
					state[i][j] = cells[i][j].clone();
		}
	}
}
