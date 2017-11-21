package de.mpi_dortmund.ij.mpitools.helicalPicker.FilamentDetector;

import java.awt.Polygon;
import java.util.ArrayList;

import de.biomedical_imaging.ij.steger.Line;
import de.biomedical_imaging.ij.steger.LineDetector;
import de.biomedical_imaging.ij.steger.Lines;
import de.biomedical_imaging.ij.steger.OverlapOption;
import de.mpi_dortmund.ij.mpitools.FilamentEnhancer.FilamentEnhancer_;
import de.mpi_dortmund.ij.mpitools.helicalPicker.custom.IWorker;
import de.mpi_dortmund.ij.mpitools.helicalPicker.logger.CentralLog;
import de.mpi_dortmund.ij.mpitools.skeletonfilter.LineTracer;
import de.mpi_dortmund.ij.mpitools.skeletonfilter.SkeletonFilter_;
import ij.ImageStack;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

public class FilamentDetectorWorker extends Thread implements IFilamentDetectorWorker, IWorker {
	
	private ImageStack input_images;
	private int sliceFrom;
	private int sliceTo;
	private double sigma;
	private double lower_threshold;
	private double upper_threshold;
	ArrayList<ArrayList<Polygon>> lines; // Contains a list of all images for every image
	
	public FilamentDetectorWorker(ImageStack ips, int from, int to, double sigma, double lower_threshold, double upper_threshold) {
		
		this.input_images = ips;
		this.sliceFrom = from;
		this.sliceTo = to;
		this.sigma = sigma;
		this.lower_threshold = lower_threshold;
		this.upper_threshold = upper_threshold;
		
	}
	
	public FilamentDetectorWorker(FilamentDetectorWorker a) {
		
		this.input_images = a.input_images;
		this.sliceFrom = a.sliceFrom;
		this.sliceTo = a.sliceTo;
		this.sigma = a.sigma;
		this.lower_threshold = a.lower_threshold;
		this.upper_threshold = a.upper_threshold;
		
	}
	
	@Override
	public void run() {
		/*
		 *  For slices from-to in ImageStack ips do the following:
		 *  1. Detect lines in enhanced image using steger's method
		 */
		lines = new ArrayList<ArrayList<Polygon>>();
		int N = sliceTo - sliceFrom + 1;
		
		for(int i = sliceFrom; i <= sliceTo; i++){
			CentralLog.getInstance().info("Process lines of frame: " + i);
			ImageProcessor input_image = input_images.getProcessor(i);
			
			/*
			 * Detect filaments
			 */
			LineDetector detect = new LineDetector();
			int max_filament_length = 0;
			int min_filament_length = 0;
			boolean isDarkLine = false;
			boolean doCorrectPosition = true;
			boolean doEstimateWidth = false;
			boolean doExtendLine = true;
			Lines detected_lines = detect.detectLines(input_image, sigma, upper_threshold, lower_threshold, min_filament_length,max_filament_length, isDarkLine, doCorrectPosition, doEstimateWidth, doExtendLine, OverlapOption.NONE);
			CentralLog.getInstance().info("Line detection frame " + i + " successfull");
			ImageProcessor line_image = generateBinaryImage(detected_lines, input_image.getWidth(), input_image.getHeight());
			LineTracer tracer = new LineTracer();
			ArrayList<Polygon> lines_current_image = tracer.extractLines((ByteProcessor) line_image);
			lines.add(lines_current_image);
			/*
			 * Filter filaments
			 */
			/*
			
			unfiltered_lines.add(unfilteredLines);
			ImageProcessor maskImage = null;
			if(mask!=null){
				maskImage = mask.getProcessor(i);
			}
			ArrayList<Polygon> filteredLines = lineFilter.filterLineImage(line_image, input_image, response_map, maskImage);
			lines.add(filteredLines);
			CentralLog.getInstance().addLine("Line filtering frame " + i + " successfull");
			*/
		}
			
		
	}
	
	public ImageProcessor generateBinaryImage(Lines lines, int imageWdith, int imageHeight){
		ByteProcessor binary = new ByteProcessor(imageWdith, imageHeight);
		for (Line contour : lines) {
				
				float[] x = contour.getXCoordinates();
				float[] y = contour.getYCoordinates();

				binary.setLineWidth(1);
				binary.setColor(255);
				
				for(int j = 1; j < x.length; j++){
					// this draws the identified line
					
					
					binary.drawLine((int) Math.round(x[j-1]), (int) Math.round(y[j-1]),(int) Math.round(x[j]), (int) Math.round(y[j]));
					
				}
			
		}
	
		binary.invert();
		binary.skeletonize();
		binary.invert();
		return binary;
	}

	@Override
	public ArrayList<ArrayList<Polygon>> getLines() {
		// TODO Auto-generated method stub
		return lines;
	}

	@Override
	public Object clone_worker() {
		// TODO Auto-generated method stub
		return new FilamentDetectorWorker(this);
	}

	@Override
	public void setSliceFrom(int i) {
		this.sliceFrom = i;
	}

	@Override
	public void setSliceTo(int i) {
		this.sliceTo = i;
		
	}

	@Override
	public int getSliceFrom() {
		// TODO Auto-generated method stub
		return sliceFrom;
	}

	@Override
	public int getSliceTo() {
		// TODO Auto-generated method stub
		return sliceTo;
	}


}