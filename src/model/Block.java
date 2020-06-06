package model;

import model.Point;

/** Allows us to retrieve block number index from coordinates (x,y) */
public enum Block {	
	ZERO(new int[]{0,1,2}, new int[]{0,1,2}),
	ONE(new int[]{3,4,5}, new int[]{0,1,2}),
	TWO(new int[]{6,7,8}, new int[]{0,1,2}),
	THREE(new int[]{0,1,2}, new int[]{3,4,5}),
	FOUR(new int[]{3,4,5}, new int[]{3,4,5}),
	FIVE(new int[]{6,7,8}, new int[]{3,4,5}),
	SIX(new int[]{0,1,2}, new int[]{6,7,8}),
	SEVEN(new int[]{3,4,5}, new int[]{6,7,8}),
	EIGHT(new int[]{6,7,8}, new int[]{6,7,8});
	
	public final int[] cols, rows;
	
	Block(int[] cols, int[] rows) {
		this.cols = cols;
		this.rows = rows;
	}
	
	public static int of(Point p) {
		for (Block block : values()) 
			if (contains(p.x, block.rows) && contains(p.y, block.cols))
				return block.ordinal();
		return -1;
	}
	
	// starting block coords. based on block index is : B(i) = ((3*(i//3), (3*(i%3)))
	public static Point startOf(int i) {
		Point coords = coordsOf(i);
		return new Point(
			Sudoku.BLOCK_SIZE*coords.x, 
			Sudoku.BLOCK_SIZE*coords.y);
	}
	
	public static Point coordsOf(int i) {
		return new Point(
			i%Sudoku.BLOCK_SIZE,
			i/Sudoku.BLOCK_SIZE);
	}
	
	private static boolean contains(Number x, int[] arr) {
		for (int i : arr)
			if (i == x.intValue()) 
				return true;
		return false;
	}
}