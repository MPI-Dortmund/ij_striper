package de.mpi_dortmund.ij.mpitools.helicalPicker.gui;

import java.awt.Polygon;
import java.util.ArrayList;
import java.util.HashMap;

import de.mpi_dortmund.ij.mpitools.FilamentEnhancer.FilamentEnhancer;
import de.mpi_dortmund.ij.mpitools.FilamentEnhancer.FilamentEnhancerContext;
import de.mpi_dortmund.ij.mpitools.helicalPicker.Helical_Picker2_;
import de.mpi_dortmund.ij.mpitools.helicalPicker.FilamentDetector.DetectionThresholdRange;
import de.mpi_dortmund.ij.mpitools.helicalPicker.FilamentDetector.FilamentDetector;
import de.mpi_dortmund.ij.mpitools.helicalPicker.logger.CentralLog;
import de.mpi_dortmund.ij.mpitools.skeletonfilter.SkeletonFilterContext;
import de.mpi_dortmund.ij.mpitools.skeletonfilter.SkeletonFilter_;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageProcessor;

public class PipelineRunner {
	
	ImageStack enhanced_images = null;
	HashMap<Integer, ArrayList<Polygon>> filtered_lines = null;
	HashMap<Integer, ArrayList<Polygon>> lines = null;
	public void run(ImagePlus input_images, SliceRange range, SkeletonFilterContext line_filter_context, DetectionThresholdRange thresh_rang, FilamentEnhancerContext enhancer_context) {
		boolean update = true;
		boolean skip_line_filter = false;
		run(input_images, range, line_filter_context, thresh_rang, enhancer_context, update,skip_line_filter);
	}
	
	public void run(ImagePlus input_images, SliceRange slice_range, SkeletonFilterContext filterContext, DetectionThresholdRange thresh_range, FilamentEnhancerContext enhancer_context,  boolean update, boolean skip_line_filter) {

		Helical_Picker2_ picker_ = Helical_Picker2_.getInstance();
		boolean is_single_frame = slice_range.getSliceFrom()==slice_range.getSliceTo();
		/*
		 * Enhance images
		 */
		CentralLog.getInstance().info("Start enhancement:");
		//int filament_width = Integer.parseInt(gui.textfieldFilamentWidth.getText());
		//int mask_width = Integer.parseInt(gui.textfieldMaskWidth.getText());
		
		CentralLog.getInstance().info("Enhancement parameters - Filament width: " + enhancer_context.getFilamentWidth() + " mask_width: " + enhancer_context.getMaskWidth() + " angle_step: " + enhancer_context.getAngleStep());

		ImageStack enhanced_images = null;
		
		//For preview mode: Save single slices. If parameters changes, update.
		if(this.enhanced_images == null || update==true){
			FilamentEnhancer enhancer = new FilamentEnhancer(input_images.getStack(), enhancer_context);		
			enhanced_images = enhancer.getEnhancedImages(slice_range);
	
			this.enhanced_images = enhanced_images;
		}
		else if(update==false){
			IJ.log("Use previous");
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
		double sigma = enhancer_context.getFilamentWidth()/(2*Math.sqrt(3)) + 0.5;
		FilamentDetector fdetect = new FilamentDetector(enhanced_images, sigma, thresh_range);
		lines =  fdetect.getFilaments(slice_range);
		
		
		/*
		 * Filter filaments
		 */
		CentralLog.getInstance().info("Filter");
	//	double overlapping_factor = (Double)gui.spinnerOverlappingFactor.getValue();
		//int box_size = Integer.parseInt(gui.textfieldBoxSize.getText());
		/*
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
		*/
		SkeletonFilter_ lineFilter = new SkeletonFilter_();
		filtered_lines = lineFilter.filterLines(lines, filterContext, input_images.getImageStack(), enhanced_images);
		//filtered_lines = picker_.filterLines(lines, filterContext, input_images.getStack(), enhanced_images);
		
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
