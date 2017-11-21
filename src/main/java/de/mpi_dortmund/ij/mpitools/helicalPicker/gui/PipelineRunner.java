package de.mpi_dortmund.ij.mpitools.helicalPicker.gui;

import java.awt.Polygon;
import java.util.ArrayList;
import java.util.HashMap;

import de.mpi_dortmund.ij.mpitools.helicalPicker.Helical_Picker2_;
import de.mpi_dortmund.ij.mpitools.helicalPicker.logger.CentralLog;
import de.mpi_dortmund.ij.mpitools.userfilter.IUserFilter;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.process.ImageProcessor;

public class PipelineRunner {
	
	ImageStack enhanced_images = null;
	HashMap<Integer, ArrayList<Polygon>> filtered_lines = null;
	HashMap<Integer, ArrayList<Polygon>> lines = null;
	public void run(int slicefrom, int sliceto) {
		boolean update = true;
		boolean skip_line_filter = false;
		run(slicefrom, sliceto, update, skip_line_filter);
	}
	
	public void run(int slicefrom, int sliceto, boolean update, boolean skip_line_filter) {
		HelicalPickerGUI gui = Helical_Picker2_.getGUI();
		Helical_Picker2_ picker_ = Helical_Picker2_.getInstance();
		boolean is_single_frame = slicefrom==sliceto;
		/*
		 * Enhance images
		 */
		CentralLog.getInstance().info("Start enhancement:");
		int filament_width = Integer.parseInt(gui.textfieldFilamentWidth.getText());
		int mask_width = Integer.parseInt(gui.textfieldMaskWidth.getText());
		int angle_step = 2;
		boolean equalize = true;
		
		CentralLog.getInstance().info("Enhancement parameters - Filament width: " + filament_width + " mask_width: " + mask_width + " angle_step: " + angle_step);
		ImagePlus input_images = Helical_Picker2_.getInstance().getImage();
		ImageStack enhanced_images = null;
		
		//For preview mode: Save single slices. If parameters changes, update.
		if(this.enhanced_images == null || update==true){
			enhanced_images = picker_.enhanceImages(input_images.getStack(), filament_width, mask_width, angle_step, equalize,slicefrom,sliceto);
		}
		else if(update==false){
			enhanced_images = this.enhanced_images;
		}
		
		//In case of single frame, save the results
		if(is_single_frame){
			this.enhanced_images = enhanced_images;
		}
		
		
		/*
		 * Detect Filaments
		 */
		CentralLog.getInstance().info("Detect");
		double sigma = filament_width/(2*Math.sqrt(3)) + 0.5;
		double lower_threshold = Double.parseDouble(gui.textfieldLowerThreshold.getText());
		double upper_threshold = Double.parseDouble(gui.textfieldUpperThreshold.getText());
		lines = picker_.detectLines(enhanced_images, sigma, lower_threshold, upper_threshold,slicefrom,sliceto);
		
		
		/*
		 * Filter filaments
		 */
		CentralLog.getInstance().info("Filter");
		int box_size = Integer.parseInt(gui.textfieldBoxSize.getText());
		
		double overlapping_factor = (Double)gui.spinnerOverlappingFactor.getValue();
		double min_straightness = (Double) gui.spinnerMinStraightness.getValue();
		int straightness_windowsize = Integer.parseInt(gui.textfieldWindowSize.getText());
		int box_distance = Integer.parseInt(gui.textfieldBoxDistance.getText());
		int min_number_boxes = Integer.parseInt(gui.textfieldMinNumberBoxes.getText());
		int min_filament_length = (min_number_boxes-1)*box_distance+box_size;
		double sigma_max_response = (double) gui.spinnerSigmaMaxResponse.getValue();
		double sigma_min_response = (double) gui.spinnerSigmaMinResponse.getValue();
		double double_filament_detection_insensitivity = 1-(double) gui.spinnerSensitvity.getValue();
		String selected_mask = (String) gui.comboboxCustomMask.getSelectedItem();
		ImageStack masks = null;
		if(selected_mask.equals("None") == false){
			masks = WindowManager.getImage(selected_mask).getImageStack();
			
		}
		ArrayList<IUserFilter> userFilters = Helical_Picker2_.getUserFilter();
		filtered_lines = picker_.filterLines(lines, box_size, overlapping_factor, 
				min_straightness, straightness_windowsize, min_filament_length, sigma_max_response, sigma_min_response, 
				double_filament_detection_insensitivity, userFilters, input_images.getStack(), enhanced_images, masks);
		
	}
	
	public HashMap<Integer, ArrayList<Polygon>> getFilteredLines(){
		return filtered_lines;
	}
	
	public HashMap<Integer, ArrayList<Polygon>> getLines(){
		return lines;
	}
	
	public ImageProcessor getEnhancedImage(){
		return this.enhanced_images.getProcessor(0);
	}

}
