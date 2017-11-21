package de.mpi_dortmund.ij.mpitools.helicalPicker.gui;

import java.awt.Polygon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;

import de.mpi_dortmund.ij.mpitools.helicalPicker.Helical_Picker2_;
import de.mpi_dortmund.ij.mpitools.helicalPicker.logger.CentralLog;
import ij.IJ;
import ij.ImageStack;
import ij.gui.ImageRoi;
import ij.gui.Overlay;
import ij.process.ImageProcessor;

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
		if(preview_mode==PREVIEW_POINTS || preview_mode == PREVIEW_BOXES){
			place_points=true;
			picker_.placeBoxes(filtered_lines, box_size, box_distance, place_points);
		}
		if(preview_mode==PREVIEW_LINES){
			/*
			 * Show detected ridges without any filtering
			 */
			ImageStack enhanced_images = runner.enhanced_images;
			ImageProcessor response_map = enhanced_images.getProcessor(1);
			ImageRoi imgRoi = new ImageRoi(0, 0, response_map);
			imgRoi.setPosition(slice_from);
			Overlay ov = Helical_Picker2_.getInstance().getImage().getOverlay();
			if(ov==null){
				ov = new Overlay();
				Helical_Picker2_.getInstance().getImage().setOverlay(ov);
			}
			ov.add(imgRoi);

			HashMap<Integer, ArrayList<Polygon>> unfiltered_lines = runner.getLines();
			Helical_Picker2_.getInstance().showLinesAsPreview(unfiltered_lines.get(slice_from));
		}
		

	}

	

}
