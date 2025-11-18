package view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.Serial;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class SudokuFrame extends JFrame {
	@Serial
  private static final long serialVersionUID = 1L;
	private final SudokuPanel panel;
	
	public SudokuFrame(float difficulty) {
		getContentPane().add(panel = new SudokuPanel(difficulty, this), BorderLayout.CENTER);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(getPreferredSize());
		setResizable(false);
		pack();
		setVisible(true);
	}
	
	@Override
	public Dimension getPreferredSize() {
		return new Dimension(panel.getWidth()+9, panel.getHeight()+35);
	}
	
	public void alert(String msg) {
		JOptionPane.showMessageDialog(this, msg);
	}

}
