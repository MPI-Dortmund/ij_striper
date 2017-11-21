package de.mpi_dortmund.ij.mpitools.helicalPicker.gui;

import java.awt.Polygon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;

import de.mpi_dortmund.ij.mpitools.helicalPicker.Helical_Picker2_;
import de.mpi_dortmund.ij.mpitools.helicalPicker.logger.CentralLog;
import ij.IJ;

public class PreviewActionListener implements ActionListener {

	int last_filament_width = -1;
	int last_mask_width = -1;
	int last_slice_from = -1;
	int last_slice_to = -1;

	public final static int PREVIEW_BOXES = 0;
	public final static int PREVIEW_POINTS = 1;
	public final static int PREVIEW_LINES = 2;
 
	
	PipelineRunner runner;
	
	public PreviewActionListener() {
		runner = new PipelineRunner();
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		
		HelicalPickerGUI gui = Helical_Picker2_.getGUI();
		Helical_Picker2_ picker_ = Helical_Picker2_.getInstance();
		
		int filament_width = Integer.parseInt(gui.textfieldFilamentWidth.getText());
		int mask_width = Integer.parseInt(gui.textfieldMaskWidth.getText());
		int preview_mode = gui.comboboxPreviewOptions.getSelectedIndex();
		int slice_from = picker_.getImage().getCurrentSlice();
		int slice_to = picker_.getImage().getCurrentSlice();
		
		boolean update=false;
	
		
		if(
				last_filament_width != filament_width ||
				last_mask_width != mask_width ||
				last_slice_from != slice_from ||
				last_slice_to != slice_to
				){
			update = true;
			
			last_filament_width = filament_width;
			last_mask_width = mask_width;
			last_slice_from = slice_from;
			last_slice_to = slice_to;
		}
		
		boolean skip_line_filter = false;
		runner.run(slice_from, slice_to, update, skip_line_filter);
		
		HashMap<Integer, ArrayList<Polygon>> filtered_lines = runner.getFilteredLines();
		
		/*
		 * Place boxes
		 */
		int box_size = Integer.parseInt(gui.textfieldBoxSize.getText());
		int box_distance = Integer.parseInt(gui.textfieldBoxDistance.getText());
		IJ.log("Place");
		CentralLog.getInstance().info("info");
		
		boolean place_points = false;
		if(preview_mode==PREVIEW_POINTS){
			place_points=true;
		}
		picker_.placeBoxes(filtered_lines, box_size, box_distance, place_points);

	}

	

}
