package de.mpi_dortmund.ij.mpitools.helicalPicker.FilamentDetector;

import java.awt.Polygon;
import java.util.ArrayList;

import de.biomedical_imaging.ij.steger.Line;
import de.biomedical_imaging.ij.steger.LineDetector;
import de.biomedical_imaging.ij.steger.Lines;
import de.biomedical_imaging.ij.steger.OverlapOption;
import de.mpi_dortmund.ij.mpitools.helicalPicker.custom.IWorker;
import de.mpi_dortmund.ij.mpitools.helicalPicker.gui.SliceRange;
import de.mpi_dortmund.ij.mpitools.helicalPicker.logger.CentralLog;
import de.mpi_dortmund.ij.mpitools.skeletonfilter.LineTracer;
import ij.ImageStack;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

public class FilamentDetectorWorker extends Thread implements IFilamentDetectorWorker, IWorker {
	
	private ImageStack input_images;
	private SliceRange slice_range;
	FilamentDetectorContext context;
	ArrayList<ArrayList<Polygon>> lines; // Contains a list of all images for every image
	
	public FilamentDetectorWorker(ImageStack ips, SliceRange slice_range, FilamentDetectorContext context) {
		
		this.input_images = ips;
		this.slice_range = slice_range;
		this.context = context;
		
	}
	
	public FilamentDetectorWorker(FilamentDetectorWorker a) {
		
		this.input_images = a.input_images;
		this.slice_range = a.slice_range;
		this.context = a.context;
		
	}
	
	@Override
	public void run() {
		/*
		 *  For slices from-to in ImageStack ips do the following:
		 *  1. Detect lines in enhanced image using steger's method
		 */
		lines = new ArrayList<ArrayList<Polygon>>();
		
		for(int i = slice_range.getSliceFrom(); i <= slice_range.getSliceTo(); i++){
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
			Lines detected_lines = detect.detectLines(input_image, context.getSigma(), context.getThresholdRange().getUpperThreshold(), context.getThresholdRange().getLowerThreshold(), min_filament_length,max_filament_length, isDarkLine, doCorrectPosition, doEstimateWidth, doExtendLine, OverlapOption.NONE);
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
	public void setSliceRange(SliceRange slice_range) {
		this.slice_range = slice_range;
		
	}

	@Override
	public SliceRange getSliceRange() {
		// TODO Auto-generated method stub
		return slice_range;
	}



}
