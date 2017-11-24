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
import ij.IJ;
import ij.ImagePlus;
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
 
	ImagePlus target_image;
	
	PipelineRunner runner;
	
	public PreviewActionListener(ImagePlus target_image) {
		runner = new PipelineRunner();
		this.target_image = target_image;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		
		HelicalPickerGUI gui = Helical_Picker_.getGUI();
		FilamentEnhancerContext enhancer_context = gui.getFilamentEnhancerContext();

		int preview_mode = gui.comboboxPreviewOptions.getSelectedIndex();
		int slice_from = target_image.getCurrentSlice();
		int slice_to = target_image.getCurrentSlice();
		IJ.log("From " + slice_from + " To: " + slice_to);
		SliceRange slice_range = new SliceRange(slice_from, slice_to);
		boolean update=false;
	
		
		if(
				last_filament_width != enhancer_context.getFilamentWidth() ||
				last_mask_width != enhancer_context.getMaskWidth() ||
				last_slice_from != slice_range.getSliceFrom() ||
				last_slice_to != slice_range.getSliceTo()
				){
			update = true;
			
			last_filament_width = enhancer_context.getFilamentWidth();
			last_mask_width = enhancer_context.getMaskWidth();
			last_slice_from = slice_range.getSliceFrom();
			last_slice_to = slice_range.getSliceTo();
		}
		
		boolean skip_line_filter = false;
		FilamentFilterContext skeleton_filter_context = gui.getLineFilterContext();
		FilamentDetectorContext detector_context = gui.getFilamentDetectorContext();
	
		
		runner.run(target_image, slice_range, skeleton_filter_context,enhancer_context, detector_context, update, skip_line_filter);
	
		HashMap<Integer, ArrayList<Polygon>> filtered_lines = runner.getFilteredLines();
		
		/*
		 * Place boxes
		 */

		CentralLog.getInstance().info("info");
		BoxPlacingContext placing_context = gui.getBoxPlacingContext();

		if(preview_mode==PREVIEW_POINTS){
			placing_context.setPlacePoints(true);
			
		}
		if(preview_mode==PREVIEW_LINES){
			/*
			 * Show detected ridges without any filtering
			 */
			ImageStack enhanced_images = runner.enhanced_images;
			ImageProcessor response_map = enhanced_images.getProcessor(1);
			ImageRoi imgRoi = new ImageRoi(0, 0, response_map);
			imgRoi.setPosition(slice_from);
			Overlay ov = Helical_Picker_.getInstance().getImage().getOverlay();
			if(ov==null){
				ov = new Overlay();
				Helical_Picker_.getInstance().getImage().setOverlay(ov);
			}
			ov.add(imgRoi);

			HashMap<Integer, ArrayList<Polygon>> unfiltered_lines = runner.getLines();
			Helical_Picker_.getInstance().showLinesAsPreview(unfiltered_lines.get(slice_from));
		}
		else{
			BoxPlacer_ placer = new BoxPlacer_();
			ImagePlus input_image = Helical_Picker_.getInstance().getImage();
			placer.placeBoxes(filtered_lines, input_image, placing_context);
		//	picker_.placeBoxes(filtered_lines, placing_context);
		}
		

	}
	
	
	

	

}
