package de.mpi_dortmund.ij.mpitools.helicalPicker;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.TextField;
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
import de.mpi_dortmund.ij.mpitools.userfilter.IUserFilter;
import fiji.util.gui.GenericDialogPlus;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.gui.ImageRoi;
import ij.gui.Overlay;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.plugin.filter.ExtendedPlugInFilter;
import ij.plugin.filter.PlugInFilterRunner;
import ij.process.ByteProcessor;
import ij.process.FloatPolygon;
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
	double min_straightness = 0.9;
	int straightness_windowsize = 25;
	FilamentEnhancer_ enhancer;
	ImagePlus input_imp;
	boolean isPreview = false;
	boolean updateResponseMap = false;
	boolean sliceChanged = false;
	boolean removeCarbonEdge = false;
	boolean applyUserFilter = true;
	boolean equalize = true;
	String previewMode = "Boxes";
	ImageProcessor lastResponseMap;
	HashMap<Integer,ImageProcessor> calculatedResponseMaps;
	private static ArrayList<IUserFilter> userFilters = new ArrayList<IUserFilter>();
	ProgressBar progressBar;
	int runPassed = 0;
	int nPasses;
	public int setup(String arg, ImagePlus imp) {
		if(arg.equals("final")){
			IJ.run("Helicon_Exporter", "");
			saveSettings(HeliconParticleExporter_.last_path + "/helica_picker_settings.txt");
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

			enhancer.enhance_filaments(response_map, filament_width, mask_width, angle_step, show_mask,equalize,1);
			calculatedResponseMaps.remove(input_imp.getCurrentSlice());
	
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
		
		if(isPreview && previewMode.equals("Enhanced+Ridges") ){
			
			response_map = calculatedResponseMaps.get(input_imp.getCurrentSlice());
			ImageRoi imgRoi = new ImageRoi(0, 0, response_map);
			imgRoi.setPosition(input_imp.getCurrentSlice());
			Overlay ov = input_imp.getOverlay();
			if(ov==null){
				ov = new Overlay();
				input_imp.setOverlay(ov);
			}
			ov.add(imgRoi);

			
			showLinesAsPreview(lines);
			//ip.setPixels(response_map.getPixels());
		}
		else{
			ImageProcessor line_image = generateBinaryImage(lines, ip.getWidth(), ip.getHeight());

			SkeletonFilter_ skeleton_filter = new SkeletonFilter_();
			int border_diameter =  box_size/2;
			int line_distance = (int) Math.sqrt(Math.pow(1.0*box_size/2,2)+Math.pow(1.0*box_size/2,2))/2;
			ArrayList<IUserFilter> filters = userFilters;
			if(applyUserFilter==false){
				filters = null;
			}
			ArrayList<Polygon> filteredLines = skeleton_filter.filterLineImage(line_image, ip, response_map, border_diameter, line_distance, removement_radius, min_straightness, straightness_windowsize, min_filament_length, sigma_max_response, sigma_min_response, double_filament_detection_insensitivity, removeCarbonEdge,filters);
			skeleton_filter.drawLines(filteredLines, line_image);

			BoxPlacer_ placer = new BoxPlacer_();
			int sliceNumber = ip.getSliceNumber();
			if(isPreview){
				sliceNumber = input_imp.getCurrentSlice();
			}
			if(isPreview && previewMode.equals("Points")){
				box_size = 1;
			}
			placer.placeBoxes(line_image, input_imp, sliceNumber, box_size, box_distance);
			input_imp.updateAndRepaintWindow();
			increaseRunPasseAndUpdateProgress();
		}
		
		
	}
	
	public synchronized void increaseRunPasseAndUpdateProgress(){
		runPassed++;

		if(isPreview==false){
			progressBar = ProgressBar.getInstance();
			int progress =  (int) (100.0*runPassed/nPasses);
			progressBar.updateProgress(progress, "% slices processed");
			if(progress==100){
				progressBar.setVisible(false);
			}
		}
	}
	
	public static void registerUserFilter(IUserFilter filter){
		if(userFilters==null){
			userFilters = new ArrayList<IUserFilter>();
		}
		for (IUserFilter iUserFilter : userFilters) {
			if(iUserFilter.getFilterName().equals(filter.getFilterName())){
				userFilters.remove(iUserFilter);
			}
		}
		userFilters.add(filter);
	}
	
	 
	public void showLinesAsPreview(Lines lines){

		Overlay ovpoly = input_imp.getOverlay();
		if(ovpoly==null){
			ovpoly = new Overlay();
			input_imp.setOverlay(ovpoly);
		}
		double px, py;

		// Print contour
		for (Line line : lines) {
			
		
				FloatPolygon polyMitte = new FloatPolygon();

				Line cont = line;
				
				int num_points =  cont.getNumber();

				float col[] = cont.getXCoordinates();
				float row[] = cont.getYCoordinates();
				for (int j = 0; j < num_points; j++) {
					
					px = col[j];
					py = row[j];					
					polyMitte.addPoint((px + 0.5), (py + 0.5));

				}
				
				
				PolygonRoi polyRoiMitte = new PolygonRoi(polyMitte,
						Roi.POLYLINE);
				
				polyRoiMitte.setStrokeColor(Color.red);
				int position = input_imp.getCurrentSlice();
			
			
				polyRoiMitte.setPosition(position);
				ovpoly.add(polyRoiMitte);
				
		}
		input_imp.updateAndRepaintWindow();

	}
	
	public void saveSettings(String path){
		try {
			CSVWriter writer = new CSVWriter(new FileWriter(path), '\t','\0');
			String[] next = {"IMAGE_NAME=" + input_imp.getTitle()};
			writer.writeNext(next);
			next = new String[]{"FILAMENT_WIDTH=" + filament_width};
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
			writer.close();
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
		
		final GenericDialogPlus gd = new GenericDialogPlus("Helical Picker V"+getClass().getPackage().getImplementationVersion());
		gd.addMessage("Line detection parameters:");
		gd.addNumericField("Filament width", filament_width, 0,4,"pixels");
		gd.addNumericField("Mask width", mask_width, 0,5,"pixels");
		gd.addNumericField("Lower threshold (RidgeDetection)", ridge_lt, 2);
		gd.addNumericField("Upper threshold (RidgeDetection)", ridge_ut, 2);
		gd.addMessage("Line filtering parameters: ");
		gd.addNumericField("Junction safety distance", 20, 0,5,"pixels");
		gd.addNumericField("Min line length", min_filament_length, 0,5,"pixels");
		gd.addNumericField("Sigma_min._response", 5, 0);
		gd.addNumericField("Sigma_max._response", 2, 0);
		gd.addSlider("Double_filament_detection_sensitivity", 0.01, 0.99, 0.9);
		gd.addNumericField("Min_straightness", 0.9, 2);
		gd.addNumericField("Straightness_window_size", 25, 0,5,"pixels");
		gd.addMessage("Box extraction parameters");
		gd.addNumericField("Box size", 64, 0,5,"pixels");
		gd.addNumericField("Box distance", 10, 0,5,"pixels");
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
		
		gd.addCheckbox("Remove carbon edge", removeCarbonEdge);
		gd.addCheckbox("Apply user filter", applyUserFilter);
		gd.addCheckbox("Equalize", equalize);
		gd.addChoice("Preview mode:", new String[]{"Boxes","Points","Enhanced+Ridges"}, "Boxes");
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
		min_straightness = gd.getNextNumber();
		straightness_windowsize = (int) gd.getNextNumber();
		box_size = (int) gd.getNextNumber();
		box_distance = (int) gd.getNextNumber();
		removeCarbonEdge = gd.getNextBoolean();
		applyUserFilter = gd.getNextBoolean();
		equalize = gd.getNextBoolean();
		previewMode = gd.getNextChoice();
		return IJ.setupDialog(imp, DOES_8G+PARALLELIZE_STACKS+FINAL_PROCESSING);
	}

	public void setNPasses(int nPasses) {
		this.nPasses = nPasses;
		if(isPreview==false){
			progressBar = ProgressBar.getInstance();
			int progress =  (int) (100.0*runPassed/nPasses);
			progressBar.updateProgress(progress, "% slices processed");
		}
		
	}

	public boolean dialogItemChanged(GenericDialog gd, AWTEvent e) {
		
		isPreview = gd.isPreviewActive();
		int new_filament_width = (int) gd.getNextNumber();
		boolean filamant_width_changed = false;
		if(new_filament_width != filament_width){
			updateResponseMap = true;
			filamant_width_changed = true;

		}
		filament_width = new_filament_width;
		
		int new_mask_width = (int) gd.getNextNumber();
		if(new_mask_width != mask_width){
			updateResponseMap = true;
		}
		mask_width = new_mask_width;
		
		/*
		 * Estimating parameters
		 */
		if(filamant_width_changed){
			int clow = 100;
			
			double estimatedSigma = filament_width/(2*Math.sqrt(3)) + 0.5;
			double estimatedLowerThresh = Math.floor(Math.abs(-2
					* clow
					* (filament_width / 2.0)
					/ (Math.sqrt(2 * Math.PI) * estimatedSigma * estimatedSigma * estimatedSigma)
					* Math.exp(-((filament_width / 2.0) * (filament_width / 2.0))
							/ (2 * estimatedSigma * estimatedSigma))));
			estimatedLowerThresh = estimatedLowerThresh*0.17;
			((TextField)gd.getNumericFields().get(2)).setText(""+estimatedLowerThresh);
			int chigh = 255;
			double estimatedUpperThresh = Math.floor(Math.abs(-2
					* chigh
					* (filament_width / 2.0)
					/ (Math.sqrt(2 * Math.PI) * estimatedSigma * estimatedSigma * estimatedSigma)
					* Math.exp(-((filament_width / 2.0) * (filament_width / 2.0))
							/ (2 * estimatedSigma * estimatedSigma))));
			estimatedUpperThresh = estimatedUpperThresh * 0.17;
			((TextField)gd.getNumericFields().get(3)).setText(""+estimatedUpperThresh);
		}
		
		
		ridge_lt = gd.getNextNumber();
		ridge_ut = gd.getNextNumber();
		removement_radius = (int) gd.getNextNumber();
		min_filament_length = (int) gd.getNextNumber();
		sigma_min_response = gd.getNextNumber();
		sigma_max_response = gd.getNextNumber();
		double_filament_detection_insensitivity = 1- gd.getNextNumber();
		min_straightness = gd.getNextNumber();
		straightness_windowsize = (int) gd.getNextNumber();
		box_size = (int) gd.getNextNumber();
		box_distance = (int) gd.getNextNumber();
		removeCarbonEdge = gd.getNextBoolean();
		applyUserFilter = gd.getNextBoolean();
		boolean new_equalize = gd.getNextBoolean();
		if(new_equalize != equalize){
			updateResponseMap = true;
			equalize = new_equalize;
		}
		previewMode = gd.getNextChoice();
		
		
		return true;
	}

}
