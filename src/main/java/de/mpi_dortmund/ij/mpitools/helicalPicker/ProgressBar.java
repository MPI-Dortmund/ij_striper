package de.mpi_dortmund.ij.mpitools.helicalPicker;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import ij.IJ;

public class ProgressBar  
{
	
	JProgressBar bar;
	JFrame frame;
	boolean runs = false;
	private static ProgressBar instance;
	private ProgressBar() {
		 frame = new JFrame();
		 frame.setSize(300, 40);
		 frame.setTitle("Progress");
		 JPanel pane = new JPanel();
		 pane.setPreferredSize(new Dimension(300, 40));
		 if(bar==null){
			 bar = new JProgressBar(0, 100);
			 bar.setPreferredSize(new Dimension(300, 40));
			 bar.setVisible(true);
			 bar.setStringPainted(true);
		 }
		 pane.setLayout(new BorderLayout());
		 pane.add(bar, BorderLayout.CENTER);
		 frame.setLayout(new BorderLayout());
		 frame.add(pane, BorderLayout.CENTER);
		 if(IJ.getInstance()!=null){
			 frame.setLocationRelativeTo(IJ.getImage().getCanvas());
		 }
		 frame.setAlwaysOnTop( true );
		 frame.setVisible(true);
		
	}
	
	public static ProgressBar getInstance(){
		if(instance==null){
			instance = new ProgressBar();
			return instance;
		}
		instance.updateProgress(0, "");
		instance.setVisible(true);
		return instance;
	}
	
	
	
	public void updateProgress(int n, String s){
	
		bar.setValue(n);
		bar.setString(s);
	}
	
	public JProgressBar getProgressBar(){
		return bar;
	}
	
	public void setVisible(boolean visible){
		frame.setVisible(visible);
	}

}
