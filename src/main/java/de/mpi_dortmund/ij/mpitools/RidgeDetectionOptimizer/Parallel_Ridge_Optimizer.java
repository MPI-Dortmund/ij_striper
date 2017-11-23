package de.mpi_dortmund.ij.mpitools.RidgeDetectionOptimizer;

import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import de.biomedical_imaging.ij.steger.LineDetector;
import de.biomedical_imaging.ij.steger.Lines;
import de.biomedical_imaging.ij.steger.OverlapOption;
import de.mpi_dortmund.ij.mpitools.FilamentEnhancer.FilamentEnhancer;
import de.mpi_dortmund.ij.mpitools.FilamentEnhancer.FilamentEnhancerContext;
import de.mpi_dortmund.ij.mpitools.FilamentEnhancer.FilamentEnhancerWorker;
import de.mpi_dortmund.ij.mpitools.helicalPicker.Helical_Picker_;
import de.mpi_dortmund.ij.mpitools.helicalPicker.FilamentDetector.DetectionThresholdRange;
import de.mpi_dortmund.ij.mpitools.helicalPicker.custom.WorkerArrayCreator;
import de.mpi_dortmund.ij.mpitools.helicalPicker.gui.HelicalPickerGUI;
import de.mpi_dortmund.ij.mpitools.helicalPicker.gui.SliceRange;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Overlay;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.plugin.Selection;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

public class Parallel_Ridge_Optimizer {	
	
	public DetectionThresholdRange optimize(ImagePlus imp, int filament_width, int mask_width, int GLOBAL_RUNS, int LOCAL_RUNS){
		Overlay ov = imp.getOverlay();
		HashSet<Integer> slicesWithSelectionSet = new HashSet<Integer>();
		for(int i = 0; i < ov.size(); i++){
			slicesWithSelectionSet.add(ov.get(i).getPosition());
		}
		Integer[] slicesWithSelection = slicesWithSelectionSet.toArray(new Integer[slicesWithSelectionSet.size()]);
		ImagePlus substack = getSubStack(imp, slicesWithSelection);
		ImageStack enhanced_substack = getEnhancedFilaments(substack, filament_width, mask_width);
		ImageStack binary_substack = getBinaryStack(imp);
		DetectionThresholdRange start_params = searchRange(enhanced_substack, filament_width);
		
		int numberOfProcessors = Runtime.getRuntime().availableProcessors();
		
		RidgeOptimizerWorker[] workers = new RidgeOptimizerWorker[numberOfProcessors];
		for(int i = 0; i < workers.length; i++){
			workers[i] = new RidgeOptimizerWorker(enhanced_substack, binary_substack, start_params, filament_width, GLOBAL_RUNS, LOCAL_RUNS);
		}
		
		ExecutorService pool = Executors.newFixedThreadPool(numberOfProcessors);
		for (RidgeOptimizerWorker worker : workers) {
			pool.submit(worker);
		}
		pool.shutdown();
		
		try {
			
			pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		return RidgeOptimizerWorker.getBestThresholds();
		
	}
	
	protected ImagePlus getSubStack(ImagePlus imp, Integer[] slicesWithSelection){
		ImageStack subStack = new ImageStack(imp.getWidth(), imp.getHeight());
		Overlay ov = imp.getOverlay();
		Overlay newOv = new Overlay();
		for(int i = 0; i < slicesWithSelection.length; i++){
			subStack.addSlice(imp.getStack().getProcessor(slicesWithSelection[i]));
			if(ov!=null){
				for(int j = 0; j < ov.size(); j++){
					if(ov.get(j).getPosition()==slicesWithSelection[i]){
						Roi r = ov.get(j);
						r.setPosition(subStack.getSize());
						newOv.add(r);
					}
				}
			}
			
		}
		ImagePlus impSubStack = new ImagePlus("Substack", subStack);
		impSubStack.setOverlay(newOv);
		
		return impSubStack;
	}
	
	protected ImageStack getBinaryStack(ImagePlus imp){
		ImageProcessor[] selectionMaps = new ImageProcessor[imp.getStackSize()];
		Overlay ov = imp.getOverlay();
		
		for(int i = 0; i < imp.getStackSize(); i++){
			
			if(selectionMaps[i] == null){
				selectionMaps[i] = new ByteProcessor(imp.getWidth(), imp.getHeight());
				selectionMaps[i].setValue(0);
			}
			
			for(int j = 0; j < ov.size(); j++){
				if( (i+1) == ov.get(j).getPosition()){
					PolygonRoi p =  (PolygonRoi) ov.get(j);
					Roi r = Selection.lineToArea(p);
			
					selectionMaps[i].setColor(255);
					selectionMaps[i].fill(r);
				}
			}
		}
		
		ImageStack stack = new ImageStack(imp.getWidth(), imp.getHeight());
		for(int i = 0; i< selectionMaps.length; i++){
			stack.addSlice(selectionMaps[i]);
		}
		return stack;
	}
	
	protected ImageStack getEnhancedFilaments(ImagePlus imp, int filament_width, int mask_width){
		//ImageProcessor[] ips = new ImageProcessor[slices.length];
		
		FilamentEnhancerContext enhance_context = new FilamentEnhancerContext();
		enhance_context.setAngleStep(2);
		enhance_context.setEqualize(true);
		enhance_context.setFilamentWidth(filament_width);
		enhance_context.setMaskWidth(mask_width);
		FilamentEnhancer enhancer = new FilamentEnhancer(imp.getStack(), enhance_context);
		SliceRange slice_range = new SliceRange(1, imp.getStackSize());
		ImageStack enhanced = enhancer.getEnhancedImages(slice_range);
		
		
		return enhanced;
		
	}
	
	protected DetectionThresholdRange searchRange(ImageStack ips,double filament_width){
		LineDetector detect = new LineDetector();
		double lb = 0;
		double ub = 20;
		double last_ub_without_lines = 20;
		int max_filament_length = 0;
		boolean isDarkLine = false;
		boolean doCorrectPosition = true;
		boolean doEstimateWidth = false;
		boolean doExtendLine = true;
		double sigma = filament_width/(2*Math.sqrt(3)) + 0.5;
		
		do{
			int N_FOUND = 0;
			for(int k = 0; k < ips.getSize(); k++){
				ImageProcessor ip = ips.getProcessor(k+1);			
				Lines lines = detect.detectLines(ip, sigma, ub, lb, 0,max_filament_length, isDarkLine, doCorrectPosition, doEstimateWidth, doExtendLine, OverlapOption.NONE);
				N_FOUND += lines.size();
			}
			if(N_FOUND==0){
				last_ub_without_lines = ub;
				ub = ub/2;
			}
			else{
				ub = ub + (last_ub_without_lines -ub)/2;
				
			}
			
		}while(Math.abs(ub-last_ub_without_lines)>0.01);
		
		DetectionThresholdRange range = new DetectionThresholdRange(0, last_ub_without_lines);
		return range;
		
	}

}
