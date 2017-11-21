package de.mpi_dortmund.ij.mpitools.helicalPicker.gui;

import java.awt.Polygon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;

import de.mpi_dortmund.ij.mpitools.helicalPicker.Helical_Picker2_;
import de.mpi_dortmund.ij.mpitools.helicalPicker.FilamentDetector.FilamentDetector;
import de.mpi_dortmund.ij.mpitools.helicalPicker.logger.CentralLog;
import de.mpi_dortmund.ij.mpitools.userfilter.IUserFilter;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;

public class ApplyActionListener implements ActionListener {

	
	
	@Override
	public void actionPerformed(ActionEvent e) {
		HelicalPickerGUI gui = Helical_Picker2_.getGUI();
		
		
		PipelineRunner runner = new PipelineRunner();
		runner.run(1, Helical_Picker2_.getInstance().getImage().getStackSize());
		HashMap<Integer, ArrayList<Polygon>> filtered_lines = runner.getFilteredLines();
		int box_size = Integer.parseInt(gui.textfieldBoxSize.getText());
		int box_distance = Integer.parseInt(gui.textfieldBoxDistance.getText());
		/*
		 * Place boxes
		 */
		CentralLog.getInstance().info("Place boxes");
		Helical_Picker2_ picker_ = Helical_Picker2_.getInstance();
		boolean place_points = false;
		picker_.placeBoxes(filtered_lines, box_size, box_distance, place_points);
	}

}
