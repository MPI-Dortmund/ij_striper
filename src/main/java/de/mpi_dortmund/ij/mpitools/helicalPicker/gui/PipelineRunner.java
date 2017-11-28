package de.mpi_dortmund.ij.mpitools.helicalPicker.gui;

import java.awt.Polygon;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import de.mpi_dortmund.ij.mpitools.FilamentEnhancer.FilamentEnhancer;
import de.mpi_dortmund.ij.mpitools.FilamentEnhancer.FilamentEnhancerContext;
import de.mpi_dortmund.ij.mpitools.helicalPicker.Helical_Picker_;
import de.mpi_dortmund.ij.mpitools.helicalPicker.FilamentDetector.DetectionThresholdRange;
import de.mpi_dortmund.ij.mpitools.helicalPicker.FilamentDetector.FilamentDetector;
import de.mpi_dortmund.ij.mpitools.helicalPicker.FilamentDetector.FilamentDetectorContext;
import de.mpi_dortmund.ij.mpitools.helicalPicker.filamentFilter.FilamentFilterContext;
import de.mpi_dortmund.ij.mpitools.helicalPicker.filamentFilter.FilamentFilter;
import de.mpi_dortmund.ij.mpitools.helicalPicker.logger.CentralLog;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageProcessor;

public class PipelineRunner {
	
	private ImageStack enhanced_images = null;
	HashMap<Integer, ArrayList<Polygon>> filtered_lines = null;
	HashMap<Integer, ArrayList<Polygon>> lines_in_enhanced_substack = null;
	
	/**
	 * Runs the complete pipeline
	 * @param input_images One or several images with filaments
	 * @param slice_range Slice range for evaluation
	 * @param filterContext Parameter context for filtering detected filaments
	 * @param enhancer_context Parameters for filaments enhancement
	 * @param detector_context Parameters for filaments detections
	 */
	public void run(ImagePlus input_images, SliceRange range, FilamentFilterContext line_filter_context, FilamentEnhancerContext enhancer_context, FilamentDetectorContext detector_context) {
		boolean update = true;
		boolean skip_line_filter = false;
		run(input_images, range, line_filter_context, enhancer_context, detector_context, update,skip_line_filter);
	}
	
	/**
	 * Runs the complete pipeline
	 * @param input_images One or several images with filaments
	 * @param slice_range Slice range for evaluation
	 * @param filterContext Parameter context for filtering detected filaments
	 * @param enhancer_context Parameters for filaments enhancement
	 * @param detector_context Parameters for filaments detections
	 * @param update if true, the enhanced filaments will be recalculated. Used for speeding up the preview.
	 * @param skip_line_filter if true, the line filtering will be skipped.
	 */
	public void run(ImagePlus input_images, SliceRange slice_range, FilamentFilterContext filterContext, FilamentEnhancerContext enhancer_context, FilamentDetectorContext detector_context, boolean update, boolean skip_line_filter) {
		if(Helical_Picker_.getGUI()!=null){
			Helical_Picker_.getGUI().showProgress(true, "Enhance filaments");
			
		}
		
		/*
		 * Enhance images
		 */
		CentralLog.getInstance().info("Start enhancement:");		
		CentralLog.getInstance().info("Enhancement parameters - Filament width: " + enhancer_context.getFilamentWidth() + " mask_width: " + enhancer_context.getMaskWidth() + " angle_step: " + enhancer_context.getAngleStep());
		
		ImageStack enhanced_images = null;
		
		//For preview mode: Save single slices. If parameters changes, update.
		if(this.enhanced_images == null || update==true){
			FilamentEnhancer enhancer = new FilamentEnhancer(input_images.getStack(), enhancer_context);		
			
			enhanced_images = enhancer.getEnhancedImages(slice_range);
			this.enhanced_images = enhanced_images;
		}
		else if(update==false){
			enhanced_images = this.enhanced_images;
		}
		
		if(Helical_Picker_.getGUI()!=null){
			Helical_Picker_.getGUI().showProgress(true, "Detect filaments ");
		}
		SliceRange enhanced_substack_slice_range = new SliceRange(1, slice_range.getSliceTo()-slice_range.getSliceFrom()+1);	
		
		/*
		 * Detect Filaments
		 */
		CentralLog.getInstance().info("Detect");
		
		FilamentDetector fdetect = new FilamentDetector(enhanced_images, detector_context);
		lines_in_enhanced_substack =  fdetect.getFilaments(enhanced_substack_slice_range);
		
		if(Helical_Picker_.getGUI()!=null){
			Helical_Picker_.getGUI().showProgress(true, "Filter filaments");
		}
		
		/*
		 * Filter filaments
		 */
		
		CentralLog.getInstance().info("Filter");
		FilamentFilter lineFilter = new FilamentFilter();
		filtered_lines = lineFilter.filterLines(lines_in_enhanced_substack, filterContext, getSubstack(input_images, slice_range).getStack(), enhanced_images);
		
		/*
		 * Correct slice positions for originak stack if only a substack was processed
		 * 
		 */
		HashMap<Integer, ArrayList<Polygon>> filtered_lines_corr = new HashMap<Integer, ArrayList<Polygon>>();
		Iterator<Integer> keyIter = filtered_lines.keySet().iterator();
		while(keyIter.hasNext()){
			int key = keyIter.next();
			ArrayList<Polygon> pol = filtered_lines.get(key);
			filtered_lines_corr.put(key+slice_range.getSliceFrom()-1, pol);
		}
		filtered_lines = filtered_lines_corr;
		
	
		if(Helical_Picker_.getGUI()!=null){
			Helical_Picker_.getGUI().showProgress(true, "Done!");
			IJ.wait(100);
			Helical_Picker_.getGUI().showProgress(false, "");
		}
		
	}
	
	/**
	 * Extracts a substack
	 * @param imp Original image stack
	 * @param range Range for substack
	 * @return Substack of imp
	 */
	public ImagePlus getSubstack(ImagePlus imp, SliceRange range){
		
		ImageStack substack = new ImageStack(imp.getWidth(), imp.getHeight());
		for(int i = range.getSliceFrom(); i <= range.getSliceTo(); i++){
			substack.addSlice(imp.getStack().getProcessor(i));
		}
		ImagePlus subStackImp = new ImagePlus("", substack);
		
		return subStackImp;
		
	}
	
	/**
	 * Returns a hashmap of filtered lines. The keys are the slice indicies and the values ArrayLists of polygons.
	 * @return Hashmap of filtered lines
	 */	
	public HashMap<Integer, ArrayList<Polygon>> getFilteredLines(){
		return filtered_lines;
	}
	
	/**
	 * Returns a hashmap of detected lines befor filtering. The keys are the slice indicies and the values ArrayLists of polygons.
	 */
	public HashMap<Integer, ArrayList<Polygon>> getLines(){
		return lines_in_enhanced_substack;
	}
	
	/**
	 * 
	 * @return Returns the enhanced imags.
	 */
	public ImageStack getEnhancedImage(){
		return this.enhanced_images;
	}

}
