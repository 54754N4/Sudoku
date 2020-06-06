package view;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class SudokuFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	private SudokuPanel panel;
	
	public SudokuFrame(int percent) {
		getContentPane().add(panel = new SudokuPanel(percent, this), BorderLayout.CENTER);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(getPreferredSize());
		setResizable(false);
		pack();
		setVisible(true);
	}
	
	@Override
	public Dimension getPreferredSize() {
		return new Dimension(panel.getWidth()+9, panel.getHeight()+30);
	}
	
	public void alert(String msg) {
		JOptionPane.showMessageDialog(this, msg);
	}

}
