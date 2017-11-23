package de.mpi_dortmund.ij.mpitools.RidgeDetectionOptimizer;

import java.util.Random;

import de.biomedical_imaging.ij.steger.Line;
import de.biomedical_imaging.ij.steger.LineDetector;
import de.biomedical_imaging.ij.steger.Lines;
import de.biomedical_imaging.ij.steger.OverlapOption;
import de.mpi_dortmund.ij.mpitools.FilamentEnhancer.IFilamentEnhancerWorker;
import de.mpi_dortmund.ij.mpitools.helicalPicker.FilamentDetector.DetectionThresholdRange;
import de.mpi_dortmund.ij.mpitools.helicalPicker.custom.IWorker;
import de.mpi_dortmund.ij.mpitools.helicalPicker.gui.SliceRange;
import ij.IJ;
import ij.ImageStack;
import ij.process.ImageProcessor;

public class RidgeOptimizerWorker extends Thread {
	
	ImageStack enhanced_substack;
	ImageStack binary_substack;
	DetectionThresholdRange start_params;
	private static double best_goodness = Double.MIN_VALUE;
	private static DetectionThresholdRange best_para = null;
	int number_of_global_runs;
	int number_of_local_runs;
	int filament_width;
	private static Random rand = null;
	private static int seed = 11;
	
	public RidgeOptimizerWorker(ImageStack enhanced_substack, ImageStack binary_substack, DetectionThresholdRange startParams, int filament_width, int number_global, int number_local) {
		this.start_params = startParams;
		this.enhanced_substack = enhanced_substack;
		this.binary_substack = binary_substack;
		this.number_of_global_runs = number_global;
		this.number_of_local_runs = number_local;
		this.filament_width = filament_width;
	}
	
	public static synchronized boolean updateBestResult(double goodness, DetectionThresholdRange para){
		if(goodness > best_goodness){
			best_para = para;
			best_goodness = goodness;
			IJ.log("############ Goodness: " + best_goodness + " LT: " + para.getLowerThreshold() + " UT: " + para.getUpperThreshold());
			return true;
		}
		return false;
	}
	
	@Override
	public void run() {
		int numberOfRuns = number_of_global_runs + number_of_local_runs;
		
		
		for(int i = 0; i < numberOfRuns; i++){
			DetectionThresholdRange para = null;
			
			if(i<=number_of_global_runs){
				para = nextParamterSet(start_params.getLowerThreshold(), start_params.getUpperThreshold(), start_params.getLowerThreshold(), start_params.getUpperThreshold());
			}
			else{
				int LRUN = i-number_of_global_runs;
				double range = -0.0075*LRUN+0.5;
				para = nextParamterSet(best_para.getLowerThreshold()*range, best_para.getLowerThreshold()*(1+range), best_para.getUpperThreshold()*range, best_para.getUpperThreshold()*(1+range));
			}
			double goodness = getGoodness(enhanced_substack, binary_substack, para, filament_width);
			updateBestResult(goodness,para);
		}
		
	}
	
	public double getGoodness(ImageStack ips, ImageStack selectionMaps, DetectionThresholdRange thresh_range, double filament_width){
		LineDetector detect = new LineDetector();
		int max_filament_length = 0;
		boolean isDarkLine = false;
		boolean doCorrectPosition = true;
		boolean doEstimateWidth = false;
		boolean doExtendLine = true;
		double sigma = filament_width/(2*Math.sqrt(3)) + 0.5;
		
		int numberInSelection=0;
		int numberOutSelection=0;
		
		for(int k = 0; k < ips.size(); k++){
			
			ImageProcessor ip = ips.getProcessor(k+1);
			ImageProcessor selectionMap = selectionMaps.getProcessor(k+1);
		
			Lines lines = detect.detectLines(ip, sigma, thresh_range.getUpperThreshold(), thresh_range.getLowerThreshold(), 0,max_filament_length, isDarkLine, doCorrectPosition, doEstimateWidth, doExtendLine, OverlapOption.NONE);
			
			for (Line line : lines) {
				float[] x  = line.getXCoordinates();
				float[] y  = line.getYCoordinates();
				for(int i = 0; i < x.length; i++){
					int v = selectionMap.get((int)x[i], (int)y[i]);
					if(v==255){
						numberInSelection++;
					}else{
						numberOutSelection++;
					}
				}
				
			}
		}
		return (0.8*numberInSelection - 0.2*numberOutSelection);
	}
	
	public static synchronized DetectionThresholdRange nextParamterSet(double min_lt,  double max_lt, double min_ut, double max_ut){
	
		if(rand==null){
			rand = new Random(seed);
		}
	
		double cand_lt = 0;
		double cand_ut = 0;
		
		do {
			cand_lt = min_lt + rand.nextDouble()*max_lt;
			cand_ut = min_ut + rand.nextDouble()*max_ut;
		} while(cand_ut < cand_lt);
		
		DetectionThresholdRange range = new DetectionThresholdRange(cand_lt,cand_ut);
		return range;
	}
	
	public static double getBestGoodness(){
		return best_goodness;
	}
	
	public static DetectionThresholdRange getBestThresholds(){
		return best_para;
	}


}
