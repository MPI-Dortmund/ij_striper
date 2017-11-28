package de.mpi_dortmund.ij.mpitools.RidgeDetectionOptimizer;

import java.util.Random;

import de.biomedical_imaging.ij.steger.Line;
import de.biomedical_imaging.ij.steger.LineDetector;
import de.biomedical_imaging.ij.steger.Lines;
import de.biomedical_imaging.ij.steger.OverlapOption;
import de.mpi_dortmund.ij.mpitools.FilamentEnhancer.IFilamentEnhancerWorker;
import de.mpi_dortmund.ij.mpitools.helicalPicker.FilamentDetector.DetectionThresholdRange;
import de.mpi_dortmund.ij.mpitools.helicalPicker.FilamentDetector.FilamentDetectorContext;
import de.mpi_dortmund.ij.mpitools.helicalPicker.custom.IWorker;
import de.mpi_dortmund.ij.mpitools.helicalPicker.gui.SliceRange;
import de.mpi_dortmund.ij.mpitools.helicalPicker.logger.CentralLog;
import ij.IJ;
import ij.ImageStack;
import ij.plugin.filter.RankFilters;
import ij.process.ImageProcessor;

public class RidgeOptimizerWorker extends Thread {
	
	ImageStack enhanced_substack;
	ImageStack binary_substack;
	DetectionThresholdRange detection_range_start;
	private static double best_goodness = Double.MIN_VALUE;
	private static DetectionThresholdRange best_detection_para = null;
	private RidgeOptimizerWorker precursor;
	int number_of_global_runs;
	int number_of_local_runs;
	int filament_width;
	private Random rand = null;
	private int seed_base = 11;
	private int seed;
	private static int object_counter;
	/**
	 * Constructor for RidgeOptimizerWorker
	 * @param enhanced_substack Imagestack with enhanced filaments
	 * @param binary_substack Ground truth binary stack
	 * @param detection_range_start Start parameter range for line detection
	 * @param filament_width Width of the filament
	 * @param number_global Number of global optimization runs
	 * @param number_local Number of local optimization runs
	 */
	public RidgeOptimizerWorker(ImageStack enhanced_substack, ImageStack binary_substack, DetectionThresholdRange detection_range_start, int filament_width, int number_global, int number_local, RidgeOptimizerWorker precursor) {
		this.detection_range_start = detection_range_start;
		this.enhanced_substack = enhanced_substack;
		this.binary_substack = binary_substack;
		this.number_of_global_runs = number_global;
		this.number_of_local_runs = number_local;
		this.filament_width = filament_width;
		this.precursor = precursor;
		seed = seed_base+object_counter;
		object_counter++;
	}
	
	/**
	 * Updates the best line detection parameter set if the goodness is higher than the last
	 * @param goodness Goodness for parameter set
	 * @param para Paramter set
	 * @return True if the parameter set was better than the previous set.
	 */
	public static synchronized boolean updateBestResult(double goodness, DetectionThresholdRange para){
		if(goodness > best_goodness){
			best_detection_para = para;
			best_goodness = goodness;
			//IJ.log("############ Goodness: " + best_goodness + " LT: " + para.getLowerThreshold() + " UT: " + para.getUpperThreshold());
			CentralLog.getInstance().info("############ Goodness: " + best_goodness + " LT: " + para.getLowerThreshold() + " UT: " + para.getUpperThreshold());
			return true;
		}
		return false;
	}
	
	@Override
	public void run() {
		int numberOfRuns = number_of_global_runs + number_of_local_runs;
		double sigma = FilamentDetectorContext.filamentWidthToSigma(filament_width);
		FilamentDetectorContext detection_context = new FilamentDetectorContext();
		detection_context.setSigma(sigma);
		for(int i = 0; i < numberOfRuns; i++){
			
			DetectionThresholdRange candidate_range = null;
			
			if(i<=number_of_global_runs){
				candidate_range = nextParamterSet(detection_range_start);
				//IJ.log("HIER " + i);
			}
			else{
				int LRUN = i-number_of_global_runs;
				double range = -0.0075*LRUN+0.5;
				DetectionThresholdRange local_range = new DetectionThresholdRange(best_detection_para.getLowerThreshold()*range, best_detection_para.getLowerThreshold()*(1+range));
				candidate_range = nextParamterSet(local_range);
			}
			detection_context.setThresholdRange(candidate_range);
			
			double goodness = getGoodness(enhanced_substack, binary_substack, detection_context);
			updateBestResult(goodness,candidate_range);
			
			this.interrupt();
			if(precursor!=null){
				
				precursor.notify();
			}
			
		}
		
	}
	
	/**
	 * Apply line detection to each image enhanced image, compare the detection with the binary ground truth image and calculate the the goodness value according
	 * 0.8*NUMBER_OF_CORRECT_LINES_POINTS - 0.2*NUMBER_OF_INCORRECT_LINES_POINTS
	 * @param enhanced_images Imagestack with enhanced filaments
	 * @param binary_stack Ground truth binary stack
	 * @param detection_context Parameter context for line detection
	 * @return Goodness value
	 */
	public double getGoodness(ImageStack enhanced_images, ImageStack binary_stack, FilamentDetectorContext detection_context){

		LineDetector detect = new LineDetector();
		int max_filament_length = 0;
		boolean isDarkLine = false;
		boolean doCorrectPosition = true;
		boolean doEstimateWidth = false;
		boolean doExtendLine = true;
		double sigma = detection_context.getSigma();
		
		int numberCorrectLinePoints=0;
		int numberIncorrectLinePoints=0;
		
		for(int k = 0; k < enhanced_images.size(); k++){
			
			ImageProcessor ip = enhanced_images.getProcessor(k+1);
			ImageProcessor selectionMap = binary_stack.getProcessor(k+1);
		
			Lines lines = detect.detectLines(ip, sigma, detection_context.getThresholdRange().getUpperThreshold(), detection_context.getThresholdRange().getLowerThreshold(), 0,max_filament_length, isDarkLine, doCorrectPosition, doEstimateWidth, doExtendLine, OverlapOption.NONE);
			
			for (Line line : lines) {
				float[] x  = line.getXCoordinates();
				float[] y  = line.getYCoordinates();
				for(int i = 0; i < x.length; i++){
					int v = selectionMap.get((int)x[i], (int)y[i]);
					if(v==255){
						numberCorrectLinePoints++;
					}else{
						numberIncorrectLinePoints++;
					}
				}
				
			}
		}
		
		return (0.8*numberCorrectLinePoints - 0.2*numberIncorrectLinePoints);
	}
	
	
	public DetectionThresholdRange nextParamterSet(DetectionThresholdRange min_max_threshold){
		
		DetectionThresholdRange range = nextParamterSet(min_max_threshold, min_max_threshold);
		return range;
	}
	
	public DetectionThresholdRange nextParamterSet(DetectionThresholdRange min_max_lower_threshold, DetectionThresholdRange min_max_upper_threshold){
	
		if(rand==null){
			rand = new Random(seed);
		}
	
		double cand_lt = 0;
		double cand_ut = 0;
		
		do {
			cand_lt = min_max_lower_threshold.getLowerThreshold() + rand.nextDouble()*min_max_lower_threshold.getUpperThreshold();
			cand_ut = min_max_upper_threshold.getLowerThreshold() + rand.nextDouble()*min_max_upper_threshold.getUpperThreshold();
		} while(cand_ut < cand_lt);
		
		DetectionThresholdRange range = new DetectionThresholdRange(cand_lt,cand_ut);
		return range;
	}
	
	public static double getBestGoodness(){
		return best_goodness;
	}
	
	public static DetectionThresholdRange getBestThresholds(){
		return best_detection_para;
	}


}
