package de.mpi_dortmund.ij.mpitools.helicalPicker.gui;

import java.awt.Polygon;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import de.mpi_dortmund.ij.mpitools.FilamentEnhancer.FilamentEnhancer;
import de.mpi_dortmund.ij.mpitools.FilamentEnhancer.FilamentEnhancerContext;
import de.mpi_dortmund.ij.mpitools.helicalPicker.FilamentDetector.DetectionThresholdRange;
import de.mpi_dortmund.ij.mpitools.helicalPicker.FilamentDetector.FilamentDetector;
import de.mpi_dortmund.ij.mpitools.helicalPicker.FilamentDetector.FilamentDetectorContext;
import de.mpi_dortmund.ij.mpitools.helicalPicker.filamentFilter.FilamentFilterContext;
import de.mpi_dortmund.ij.mpitools.helicalPicker.filamentFilter.FilamentFilter_;
import de.mpi_dortmund.ij.mpitools.helicalPicker.logger.CentralLog;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageProcessor;

public class PipelineRunner {
	
	private ImageStack enhanced_images = null;
	HashMap<Integer, ArrayList<Polygon>> filtered_lines = null;
	HashMap<Integer, ArrayList<Polygon>> lines_in_enhanced_substack = null;
	public void run(ImagePlus input_images, SliceRange range, FilamentFilterContext line_filter_context, FilamentEnhancerContext enhancer_context, FilamentDetectorContext detector_context) {
		boolean update = true;
		boolean skip_line_filter = false;
		run(input_images, range, line_filter_context, enhancer_context, detector_context, update,skip_line_filter);
	}
	
	public void run(ImagePlus input_images, SliceRange slice_range, FilamentFilterContext filterContext, FilamentEnhancerContext enhancer_context, FilamentDetectorContext detector_context, boolean update, boolean skip_line_filter) {

		
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
		
		SliceRange enhanced_substack_slice_range = new SliceRange(1, slice_range.getSliceTo()-slice_range.getSliceFrom()+1);	
		
		/*
		 * Detect Filaments
		 */
		CentralLog.getInstance().info("Detect");
		
		FilamentDetector fdetect = new FilamentDetector(enhanced_images, detector_context);
		lines_in_enhanced_substack =  fdetect.getFilaments(enhanced_substack_slice_range);
		
		
		
		/*
		 * Filter filaments
		 */
		
		CentralLog.getInstance().info("Filter");
		FilamentFilter_ lineFilter = new FilamentFilter_();
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
		
	
		
	}
	
	
	public ImagePlus getSubstack(ImagePlus imp, SliceRange range){
		
		ImageStack substack = new ImageStack(imp.getWidth(), imp.getHeight());
		for(int i = range.getSliceFrom(); i <= range.getSliceTo(); i++){
			substack.addSlice(imp.getStack().getProcessor(i));
		}
		ImagePlus subStackImp = new ImagePlus("", substack);
		
		return subStackImp;
		
	}
	
	public HashMap<Integer, ArrayList<Polygon>> getFilteredLines(){
		return filtered_lines;
	}
	
	public HashMap<Integer, ArrayList<Polygon>> getLines(){
		return lines_in_enhanced_substack;
	}
	
	public ImageStack getEnhancedImage(){
		return this.enhanced_images;
	}

}
