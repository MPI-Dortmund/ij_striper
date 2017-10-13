package de.mpi_dortmund.ij.mpitools.helicalPicker.gui;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;

import javax.swing.JTextArea;
import javax.swing.JTextPane;

import ij.IJ;

public class UpdateInformationListener implements MouseListener {
	
	HashMap<Object, String> descriptionMap = new HashMap<Object, String>();
	JTextArea informationPane;
	public UpdateInformationListener(JTextArea informationPane) {
		this.informationPane = informationPane;
	}
	
	public void addDescription(Object guiElement, String description){
		descriptionMap.put(guiElement, description);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		informationPane.setText(descriptionMap.get(e.getSource()));
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

}
