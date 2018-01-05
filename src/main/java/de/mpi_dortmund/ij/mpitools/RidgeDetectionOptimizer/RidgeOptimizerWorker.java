package de.mpi_dortmund.ij.mpitools.RidgeDetectionOptimizer;

import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

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
import ij.process.ByteProcessor;
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
	boolean normalize;
	CyclicBarrier barr;
	/**
	 * Constructor for RidgeOptimizerWorker
	 * @param enhanced_substack Imagestack with enhanced filaments
	 * @param binary_substack Ground truth binary stack
	 * @param detection_range_start Start parameter range for line detection
	 * @param filament_width Width of the filament
	 * @param number_global Number of global optimization runs
	 * @param number_local Number of local optimization runs
	 */
	public RidgeOptimizerWorker(ImageStack enhanced_substack, ImageStack binary_substack, DetectionThresholdRange detection_range_start, int filament_width, int number_global, int number_local, boolean normalize, CyclicBarrier barr) {
		this.detection_range_start = detection_range_start;
		this.enhanced_substack = enhanced_substack;
		this.binary_substack = binary_substack;
		this.number_of_global_runs = number_global;
		this.number_of_local_runs = number_local;
		this.filament_width = filament_width;
		this.barr = barr;
		this.normalize = normalize;
		seed = seed_base+object_counter;
		object_counter++;
	}
	
	public void setPrecursor(RidgeOptimizerWorker precursor){
		this.precursor = precursor;
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
			if(RidgeDetectionOptimizerAssistant.getInstance()!= null){
				RidgeDetectionOptimizerAssistant.getInstance().updateBestResult(best_goodness, para);
			}
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
			}
			else{
				int LRUN = i-number_of_global_runs;
				double range = -0.0075*LRUN+0.5;
				DetectionThresholdRange local_range = new DetectionThresholdRange(best_detection_para.getLowerThreshold()*range, best_detection_para.getLowerThreshold()*(1+range));
				candidate_range = nextParamterSet(local_range);
			}
			detection_context.setThresholdRange(candidate_range);
			
			double goodness = getGoodness(enhanced_substack, binary_substack, detection_context,normalize);
			updateBestResult(goodness,candidate_range);
			try {
				barr.await();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		//	this.interrupt();
			//if(precursor!=null){
 catch (BrokenBarrierException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
				
			//	precursor.notify();
		//	}
			
		}
		
	}
	
	/**
	 * Apply line detection to each image enhanced image, compare the detection with the binary ground truth image and calculate the the goodness value according
	 * (0.8*NUMBER_OF_CORRECT_LINES_POINTS - 0.2*NUMBER_OF_INCORRECT_LINES_POINTS)*MEAN_CORRECT_LINE_LENGTH
	 * @param enhanced_images Imagestack with enhanced filaments
	 * @param binary_stack Ground truth binary stack
	 * @param detection_context Parameter context for line detection
	 * @return Goodness value
	 */
	public double getGoodness(ImageStack enhanced_images, ImageStack binary_stack, FilamentDetectorContext detection_context, boolean normalize){

		LineDetector detect = new LineDetector();
		int max_filament_length = 0;
		boolean isDarkLine = false;
		boolean doCorrectPosition = true;
		boolean doEstimateWidth = false;
		boolean doExtendLine = true;
		double sigma = detection_context.getSigma();
		
		double numberCorrectLinePoints=0;
		double numberIncorrectLinePoints=0;
		int currentCorrectInRow = 0;
		int sumCorrectInRow = 0;
		int correctSegements = 0;
		
		for(int k = 0; k < enhanced_images.size(); k++){
			
			ImageProcessor ip = enhanced_images.getProcessor(k+1);
			ImageProcessor selectionMap = binary_stack.getProcessor(k+1);
			int NORM_FACTOR =1 ;
			if(normalize){
			//	NORM_FACTOR = ((ByteProcessor)binary_stack.getProcessor(k+1)).getHistogram()[255];
			}
			Lines lines = detect.detectLines(ip, sigma, detection_context.getThresholdRange().getUpperThreshold(), detection_context.getThresholdRange().getLowerThreshold(), 0,max_filament_length, isDarkLine, doCorrectPosition, doEstimateWidth, doExtendLine, OverlapOption.NONE);
			double localNumberCorrectLinePoints = 0;
			double localNumberIncorrectLinePoints = 0;
			
			for (Line line : lines) {
				float[] x  = line.getXCoordinates();
				float[] y  = line.getYCoordinates();
				int lastv = 0;
				for(int i = 0; i < x.length; i++){
					int v = selectionMap.get((int)x[i], (int)y[i]);
					if(v==255){
						localNumberCorrectLinePoints++;
						if(i>1 && lastv==v){
							currentCorrectInRow++;
						}
					}else{
						localNumberIncorrectLinePoints++;
						if(currentCorrectInRow>0){
							sumCorrectInRow += currentCorrectInRow;
							correctSegements++;
						}
						currentCorrectInRow=0;
					}
					lastv = v;
				}
			}
			
			numberCorrectLinePoints += 1.0*localNumberCorrectLinePoints/NORM_FACTOR;
			numberIncorrectLinePoints += 1.0*localNumberIncorrectLinePoints/NORM_FACTOR;
		}
		double meanSegmentLength = 1.0*sumCorrectInRow/correctSegements;
		if(correctSegements == 0){
			meanSegmentLength = 0;
		}
		double segNorm = Math.sqrt(enhanced_images.getWidth()*enhanced_images.getWidth()+enhanced_images.getHeight()*enhanced_images.getHeight());
		meanSegmentLength = meanSegmentLength/segNorm;
		double goodness = (0.8*numberCorrectLinePoints - 0.2*numberIncorrectLinePoints)*meanSegmentLength;
		
		return goodness;
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
