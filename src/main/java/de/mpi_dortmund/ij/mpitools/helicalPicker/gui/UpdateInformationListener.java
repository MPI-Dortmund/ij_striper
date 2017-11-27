package de.mpi_dortmund.ij.mpitools.helicalPicker.gui;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextPane;

import ij.IJ;

public class UpdateInformationListener implements MouseListener {
	
	HashMap<Object, String> descriptionMap = new HashMap<Object, String>();
	JTextPane informationPane;
	public UpdateInformationListener(JTextPane informationPane) {
		this.informationPane = informationPane;
	}
	
	public void addDescription(JComponent guiElement, String description){
		if(guiElement instanceof JComboBox){
			guiElement.addMouseListener(this);
			for(final Component component : guiElement.getComponents()) {
		        component.addMouseListener(this);
		        descriptionMap.put(component, description);
		    }
			
		//	((JComboBox<String>)guiElement).getEditor().getEditorComponent().addMouseListener(this);
			
	
		}else if(guiElement instanceof JSpinner){
			((JSpinner.DefaultEditor)((JSpinner)guiElement).getEditor()).getTextField().addMouseListener(this);
			descriptionMap.put(((JSpinner.DefaultEditor)((JSpinner)guiElement).getEditor()).getTextField(), description);
	
		}
		else{
			guiElement.addMouseListener(this);
			descriptionMap.put(guiElement, description);
		}
		
		
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		informationPane.setText(descriptionMap.get(e.getSource()));
		
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
