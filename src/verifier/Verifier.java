package verifier;

import model.Block;
import model.Point;
import model.Sudoku;
import model.Sudoku.Target;

public class Verifier {
	public static final int VERIFICATION_TOTAL = 45; 
	
	public static boolean verify(int[][] model) {
		for (int i=0; i<Sudoku.SIZE; i++)
			if (sum(Target.ROW, i, model) != VERIFICATION_TOTAL 
				|| sum(Target.COL, i, model) != VERIFICATION_TOTAL
				|| sum(Target.BLOCK, i, model) != VERIFICATION_TOTAL)
				return false;
		return true;
	}
	
	public static int sum(Target target, int x, int[][] model) {
		if (target == Target.BLOCK) 		
			return sumBlock(x, model);
		int sum = 0;
		for (int i=0; i<Sudoku.SIZE; i++)
			if (target == Target.ROW) 		
				sum += model[x][i];
			else if (target == Target.COL) 	
				sum += model[i][x];
		return sum;
	}
	
	private static int sumBlock(int i, int[][] model) {	// when blocks are indexed as such : 	012
		int sum = 0;
		Point start = Block.startOf(i);
		for (int dx=0; dx<Sudoku.BLOCK_SIZE; dx++) 
			for (int dy=0; dy<Sudoku.BLOCK_SIZE; dy++)
				sum += model[start.x+dx][start.y+dy];
		return sum;
	}	
}