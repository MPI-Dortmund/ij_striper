package de.mpi_dortmund.ij.mpitools.helicalPicker.gui;

import java.awt.Polygon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;

import de.mpi_dortmund.ij.mpitools.FilamentEnhancer.FilamentEnhancerContext;
import de.mpi_dortmund.ij.mpitools.boxplacer.BoxPlacer_;
import de.mpi_dortmund.ij.mpitools.boxplacer.BoxPlacingContext;
import de.mpi_dortmund.ij.mpitools.helicalPicker.Helical_Picker_;
import de.mpi_dortmund.ij.mpitools.helicalPicker.FilamentDetector.DetectionThresholdRange;
import de.mpi_dortmund.ij.mpitools.helicalPicker.FilamentDetector.FilamentDetectorContext;
import de.mpi_dortmund.ij.mpitools.helicalPicker.filamentFilter.FilamentFilterContext;
import de.mpi_dortmund.ij.mpitools.helicalPicker.logger.CentralLog;
import ij.ImagePlus;

public class ApplyActionListener implements ActionListener {

	
	
	@Override
	public void actionPerformed(ActionEvent e) {
		HelicalPickerGUI gui = Helical_Picker_.getGUI();
		
		ImagePlus input_image = Helical_Picker_.getInstance().getImage();
		
		PipelineRunner runner = new PipelineRunner();
		int sliceFrom = 1;
		int sliceTo = input_image.getStackSize();
		SliceRange slice_range = new SliceRange(sliceFrom, sliceTo);
		FilamentFilterContext skeleton_filter_context = gui.getLineFilterContext();
		FilamentDetectorContext detector_context = gui.getFilamentDetectorContext();
		FilamentEnhancerContext enhancer_context = gui.getFilamentEnhancerContext();

		runner.run(input_image,slice_range, skeleton_filter_context,enhancer_context,detector_context);
	
		HashMap<Integer, ArrayList<Polygon>> filtered_lines = runner.getFilteredLines();
		int box_size = Integer.parseInt(gui.textfieldBoxSize.getText());
		int box_distance = Integer.parseInt(gui.textfieldBoxDistance.getText());
		/*
		 * Place boxes
		 */
		CentralLog.getInstance().info("Place boxes");
		boolean place_points = false;
		BoxPlacingContext placing_context = new BoxPlacingContext();
		placing_context.setBoxSize(box_size);
		placing_context.setBoxDistance(box_distance);
		placing_context.setPlacePoints(place_points);
		
		BoxPlacer_ boxPlacer = new BoxPlacer_();
		boxPlacer.placeBoxes(filtered_lines, input_image, placing_context);
		
		//picker_.placeBoxes(filtered_lines, placing_context);
	}

}
