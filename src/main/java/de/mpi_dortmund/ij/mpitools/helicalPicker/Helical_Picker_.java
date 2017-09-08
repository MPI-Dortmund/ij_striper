package de.mpi_dortmund.ij.mpitools.helicalPicker;

import java.awt.AWTEvent;
import java.awt.Polygon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import com.opencsv.CSVWriter;

import de.biomedical_imaging.ij.steger.Line;
import de.biomedical_imaging.ij.steger.LineDetector;
import de.biomedical_imaging.ij.steger.Lines;
import de.biomedical_imaging.ij.steger.Lines_;
import de.biomedical_imaging.ij.steger.OverlapOption;
import de.mpi_dortmund.ij.mpitools.FilamentEnhancer.FilamentEnhancer_;
import de.mpi_dortmund.ij.mpitools.boxplacer.BoxPlacer_;
import de.mpi_dortmund.ij.mpitools.boxplacer.HeliconParticleExporter_;
import de.mpi_dortmund.ij.mpitools.skeletonfilter.SkeletonFilter_;
import fiji.util.gui.GenericDialogPlus;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.gui.Roi;
import ij.plugin.filter.ExtendedPlugInFilter;
import ij.plugin.filter.PlugInFilterRunner;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

public class Helical_Picker_ implements ExtendedPlugInFilter, DialogListener {
	
	int filament_width = 13;
	int mask_width = 100;
	double ridge_lt = 0.7;
	double ridge_ut = 1.2;
	int min_filament_length = 100;
	double sigma_min_response = 5;
	double sigma_max_response = 2;
	double double_filament_detection_insensitivity = 0.1;
	int box_size = 64;
	int box_distance = 10;
	int removement_radius = 20;
	FilamentEnhancer_ enhancer;
	ImagePlus input_imp;
	boolean isPreview = false;
	boolean updateResponseMap = false;
	boolean sliceChanged = false;
	ImageProcessor lastResponseMap;
	HashMap<Integer,ImageProcessor> calculatedResponseMaps;
	
	public int setup(String arg, ImagePlus imp) {
		if(arg.equals("final")){
			IJ.run("Helicon_Exporter", "");
			saveSettings(HeliconParticleExporter_.last_path);
			return DONE;
		}
		calculatedResponseMaps = new HashMap<Integer, ImageProcessor>();
		this.input_imp = imp;
		return DOES_8G;
	}

	public void run(ImageProcessor ip) {
		if(enhancer == null){
			enhancer = new FilamentEnhancer_();
		}
		ImageProcessor response_map = null;
		if( isPreview && calculatedResponseMaps.get(input_imp.getCurrentSlice()) != null && updateResponseMap==false ){
			this.input_imp.setOverlay(null);
			this.input_imp.updateAndRepaintWindow();
			response_map = calculatedResponseMaps.get(input_imp.getCurrentSlice());
		}
		else {
			response_map = ip.duplicate();
			int angle_step =2;
			boolean show_mask = false;
			if(updateResponseMap){
				calculatedResponseMaps = new HashMap<Integer, ImageProcessor>();
			}
			enhancer.enhance_filaments(response_map, filament_width, mask_width, angle_step, show_mask);
			calculatedResponseMaps.put(input_imp.getCurrentSlice(), response_map.duplicate());
			updateResponseMap=false;
		}
		LineDetector detect = new LineDetector();
		int max_filament_length = 0;
		boolean isDarkLine = false;
		boolean doCorrectPosition = true;
		boolean doEstimateWidth = false;
		boolean doExtendLine = true;
		double sigma = filament_width/(2*Math.sqrt(3)) + 0.5;
		Lines lines = detect.detectLines(response_map, sigma, ridge_ut, ridge_lt, min_filament_length,max_filament_length, isDarkLine, doCorrectPosition, doEstimateWidth, doExtendLine, OverlapOption.NONE);
		ImageProcessor line_image = generateBinaryImage(lines, ip.getWidth(), ip.getHeight());

		SkeletonFilter_ skeleton_filter = new SkeletonFilter_();
		int border_diameter = box_size/2;
		int line_distance = (int) Math.sqrt(Math.pow(box_size/2,2)+Math.pow(box_size/2,2));
		double min_straightness = 0.9;

		ArrayList<Polygon> filteredLines = skeleton_filter.filterLineImage(line_image, ip, response_map, border_diameter, line_distance, removement_radius, min_straightness, min_filament_length, sigma_max_response, sigma_min_response,double_filament_detection_insensitivity);
		skeleton_filter.drawLines(filteredLines, line_image);

		BoxPlacer_ placer = new BoxPlacer_();
		int sliceNumber = ip.getSliceNumber();
		if(isPreview){
			sliceNumber = input_imp.getCurrentSlice();
		}
		placer.placeBoxes(line_image, input_imp, sliceNumber, box_size, box_distance);
		input_imp.updateAndRepaintWindow();
	}
	
	public void saveSettings(String path){
		try {
			CSVWriter writer = new CSVWriter(new FileWriter(path), '\t','\0');
			String[] next = {"FILAMENT_WIDTH=" + filament_width};
			writer.writeNext(next);
			next = new String[]{"MASK_WIDTH=" + mask_width};
			writer.writeNext(next);
			next = new String[]{"RIDGE_LT=" + ridge_lt};
			writer.writeNext(next);
			next = new String[]{"RIDGE_UT=" + ridge_ut};
			writer.writeNext(next);
			next = new String[]{"REMOVEMENT_RADIUS=" + removement_radius};
			writer.writeNext(next);
			next = new String[]{"MIN_FILAMENT_LENGTH=" + min_filament_length};
			writer.writeNext(next);
			next = new String[]{"SIGMA_MIN_RESPONSE=" + sigma_min_response};
			writer.writeNext(next);
			next = new String[]{"SIGMA_MAX_RESPONSE=" + sigma_max_response};
			writer.writeNext(next);
			next = new String[]{"DOUBLE_FILAMENT_DETECTION_INSENSITIVITY=" + double_filament_detection_insensitivity};
			writer.writeNext(next);
			next = new String[]{"BOX_SIZE=" + box_size};
			writer.writeNext(next);
			next = new String[]{"BOX_DISTANCE=" + box_distance};
			writer.writeNext(next);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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

	public int showDialog(ImagePlus imp, String command, PlugInFilterRunner pfr) {
		
		final GenericDialogPlus gd = new GenericDialogPlus("Helical Picker");
		gd.addMessage("Line detection parameters:");
		gd.addNumericField("Filament width", filament_width, 0);
		gd.addNumericField("Mask width", mask_width, 0);
		gd.addNumericField("Lower threshold (RidgeDetection)", 0.7, 2);
		gd.addNumericField("Upper threshold (RidgeDetection)", 1.2, 2);
		gd.addMessage("Line filtering parameters: ");
		gd.addNumericField("Junction safety distance", 20, 0);
		gd.addNumericField("Min line length", min_filament_length, 0);
		gd.addNumericField("Sigma_min._response", 5, 0);
		gd.addNumericField("Sigma_max._response", 2, 0);
		gd.addSlider("Double_filament_detection_sensitivity", 0.01, 0.99, 0.9);
		gd.addMessage("Box extraction parameters");
		gd.addNumericField("Box size", 64, 0);
		gd.addNumericField("Box distance", 10, 0);
		gd.addButton("Previous slice", new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				
				if(input_imp.getCurrentSlice()!=1){
					input_imp.setSlice(input_imp.getCurrentSlice()-1);
					input_imp.setOverlay(null);
					sliceChanged = true;
					gd.getPreviewCheckbox().setState(false);
					isPreview = false;
				}
				
			}
		});
		gd.addButton("Next slice", new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				
				if(input_imp.getCurrentSlice()!=input_imp.getStackSize()){
					input_imp.setSlice(input_imp.getCurrentSlice()+1);
					input_imp.setOverlay(null);
					sliceChanged = true;
					gd.getPreviewCheckbox().setState(false);
					isPreview = false;
				}
				
			}
		});
		gd.addPreviewCheckbox(pfr);
		gd.addDialogListener(this);
		
		gd.showDialog();
		
		if(gd.wasCanceled()){
			return DONE;
		}
		isPreview = false;
		filament_width = (int) gd.getNextNumber();
		mask_width = (int) gd.getNextNumber();
		ridge_lt = gd.getNextNumber();
		ridge_ut = gd.getNextNumber();
		removement_radius = (int) gd.getNextNumber();
		min_filament_length = (int) gd.getNextNumber();
		sigma_min_response = gd.getNextNumber();
		sigma_max_response = gd.getNextNumber();
		double_filament_detection_insensitivity = 1- gd.getNextNumber();
		box_size = (int) gd.getNextNumber();
		box_distance = (int) gd.getNextNumber();
		
		return IJ.setupDialog(imp, DOES_8G+PARALLELIZE_STACKS+FINAL_PROCESSING);
	}

	public void setNPasses(int nPasses) {
		// TODO Auto-generated method stub
		
	}

	public boolean dialogItemChanged(GenericDialog gd, AWTEvent e) {
		
		isPreview = gd.isPreviewActive();
		int new_filament_width = (int) gd.getNextNumber();
		if(new_filament_width != filament_width){
			updateResponseMap = true;
		}
		filament_width = new_filament_width;
		
		int new_mask_width = (int) gd.getNextNumber();
		if(new_mask_width != mask_width){
			updateResponseMap = true;
		}
		mask_width = new_mask_width;
		ridge_lt = gd.getNextNumber();
		ridge_ut = gd.getNextNumber();
		removement_radius = (int) gd.getNextNumber();
		min_filament_length = (int) gd.getNextNumber();
		sigma_min_response = gd.getNextNumber();
		sigma_max_response = gd.getNextNumber();
		double_filament_detection_insensitivity = 1- gd.getNextNumber();
		box_size = (int) gd.getNextNumber();
		box_distance = (int) gd.getNextNumber();
		
		return true;
	}

}
