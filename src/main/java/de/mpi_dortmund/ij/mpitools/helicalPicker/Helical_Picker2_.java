package de.mpi_dortmund.ij.mpitools.helicalPicker;

import java.awt.Polygon;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import de.biomedical_imaging.ij.steger.Line;
import de.biomedical_imaging.ij.steger.Lines;
import de.mpi_dortmund.ij.mpitools.FilamentEnhancer.FilamentEnhancer;
import de.mpi_dortmund.ij.mpitools.boxplacer.BoxPlacer_;
import de.mpi_dortmund.ij.mpitools.helicalPicker.FilamentDetector.FilamentDetector;
import de.mpi_dortmund.ij.mpitools.helicalPicker.gui.HelicalPickerGUI;
import de.mpi_dortmund.ij.mpitools.skeletonfilter.SkeletonFilter_;
import de.mpi_dortmund.ij.mpitools.userfilter.IUserFilter;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Overlay;
import ij.io.OpenDialog;
import ij.plugin.PlugIn;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import net.imagej.ImageJ;

public class Helical_Picker2_ implements PlugIn {

	private static Helical_Picker2_ instance;
	private static HelicalPickerGUI gui;
	private ImagePlus input_image;
	private static ArrayList<IUserFilter> userFilters;
	
	public static synchronized Helical_Picker2_ getInstance() {
		if(instance==null){
			instance = new Helical_Picker2_();
		}
		return instance;
	}
	
	@Override
	public void run(String arg) {
		instance = this;
		input_image = IJ.getImage();
		if(input_image.getType() != ImagePlus.GRAY8){
			IJ.error("STRIPPER only supports 8 bit greyscal images");
		}
		if(input_image == null){
			IJ.error("No image open");
			return;
		}
		gui = new HelicalPickerGUI();
		gui.createAndShowGUI();
		
		
	}
	
	public ImagePlus getImage(){
		return input_image;
	}
	
	public static HelicalPickerGUI getGUI(){
		return gui;
	}
	
	public ImageStack enhanceImages(ImageStack ips, int filament_width, int mask_width, int angle_step, boolean equalize){
		ImageStack enhanced_stack = enhanceImages(ips, filament_width, mask_width, angle_step, equalize, 1, ips.size());
		return enhanced_stack;
	}
	
	public ImageStack enhanceImages(ImageStack ips, int filament_width, int mask_width, int angle_step, boolean equalize, int sliceFrom, int sliceTo){
		FilamentEnhancer enhancer = new FilamentEnhancer(ips, filament_width, mask_width, angle_step, equalize);
		ImageStack enhanced_stack = enhancer.getEnhancedImages(sliceFrom, sliceTo);
		return enhanced_stack;
	}
	
	/**
	 * 
	 * @param ips The enhanced images
	 * @param sigma Parameter for ridge detection. It depends on the filament width.
	 * @param lower_threshold Lower threshold parameter for ridge detection
	 * @param upper_threshold Upper threshold parameter for ridge detection
	 * @param equalize Try to equalize the greyvalue for the filaments.
	 * @return List of lines (polygone)
	 */
	public HashMap<Integer, ArrayList<Polygon>> detectLines(ImageStack ips, double sigma, double lower_threshold, double upper_threshold){
		HashMap<Integer, ArrayList<Polygon>> lines_map = detectLines(ips, sigma, lower_threshold, upper_threshold, 1, ips.getSize());
		return lines_map;
	}
	
	/**
	 * @param ips The enhanced images
	 * @param sigma Parameter for ridge detection. It depends on the filament width.
	 * @param lower_threshold Lower threshold parameter for ridge detection
	 * @param upper_threshold Upper threshold parameter for ridge detection
	 * @param equalize Try to equalize the greyvalue for the filaments.
	 * @return List of lines (polygone)
	 */
	public HashMap<Integer, ArrayList<Polygon>> detectLines(ImageStack ips, double sigma, double lower_threshold, double upper_threshold, int sliceFrom, int sliceTo){
		FilamentDetector fdetect = new FilamentDetector(ips, sigma, lower_threshold, upper_threshold);
		HashMap<Integer, ArrayList<Polygon>> lines_map =  fdetect.getFilaments(sliceFrom, sliceTo);
		
		return lines_map;
	}
	

	
	public HashMap<Integer, ArrayList<Polygon>> filterLines(HashMap<Integer, ArrayList<Polygon>> lines, int box_size, double overlapping_factor, double min_straightness,
			int straightness_windowsize, int min_filament_length, double sigma_max_response, double sigma_min_response, double double_filament_detection_insensitivity,
			ArrayList<IUserFilter> userFilters, ImageStack input_images, ImageStack response_maps, ImageStack masks){
		
		HashMap<Integer, ArrayList<Polygon>> filtered_lines = new HashMap<Integer, ArrayList<Polygon>>();
		/*
		 * Process filter lines
		 */
		int border_diameter =  box_size/2;
		int line_distance = (int) Math.sqrt(Math.pow(overlapping_factor*box_size,2)+Math.pow(overlapping_factor*box_size,2))/2;
		int removement_radius = box_size/2;
		SkeletonFilter_ skeleton_filter = new SkeletonFilter_();
		Iterator<Integer> slice_iterator = lines.keySet().iterator();
		while(slice_iterator.hasNext()){
			int slice_position = slice_iterator.next();
			ArrayList<Polygon> lines_frame_i = lines.get(slice_position);
			
			ByteProcessor line_image = new ByteProcessor(input_images.getWidth(), input_images.getHeight()); 
			line_image.setLineWidth(1);
			line_image.setColor(255);
			SkeletonFilter_.drawLines(lines_frame_i, line_image);
			line_image.invert();
			line_image.skeletonize();
			line_image.invert();
			
			ImageProcessor maskImage = null;
			if(masks!=null){
				maskImage = masks.getProcessor(slice_position);
			}
			ImageProcessor ip = input_images.getProcessor(slice_position);
			ImageProcessor response_map = response_maps.getProcessor(slice_position);
			boolean fitDistr = false;
			
			ArrayList<Polygon> filteredLines = skeleton_filter.filterLineImage(line_image, 
					ip, response_map, maskImage, border_diameter, line_distance, removement_radius, 
					min_straightness, straightness_windowsize, min_filament_length, sigma_max_response,
					sigma_min_response, fitDistr, double_filament_detection_insensitivity, userFilters);
			
			filtered_lines.put(slice_position, filteredLines);
			
		}
		
		return filtered_lines;
	
		
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
	
	public void placeBoxes(HashMap<Integer, ArrayList<Polygon>> lines, int box_size, int box_distance, boolean place_points){
		BoxPlacer_ placer = new BoxPlacer_();

		Overlay ov = new Overlay();
		input_image.setOverlay(ov);
		input_image.repaintWindow();
		
		Iterator<Integer> image_iterator = lines.keySet().iterator();
		while(image_iterator.hasNext()){
			int slice_position = image_iterator.next();
			ArrayList<Polygon> lines_in_image = lines.get(slice_position);
			if(lines_in_image.size()>0){
				placer.placeBoxes(lines_in_image, input_image, slice_position, box_size, box_distance, place_points);
			}
			
		}

	}
	
	public void generatePreview(){
		
	}
		
	public static ArrayList<IUserFilter> getUserFilter(){
		return userFilters;
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
		IJ.showMessage(filter.getFilterName()+" was registered as user filter for the helical picker.");
	}
	
	
	public static void main(String[] args) {

		Class<?> clazz = Helical_Picker2_.class;
		String url = clazz.getResource("/" + clazz.getName().replace('.', '/') + ".class").toString();
		String pluginsDir = url.substring("file:".length(), url.length() - clazz.getName().length() - ".class".length());
		System.setProperty("plugins.dir", pluginsDir);
		// start ImageJ
		ImageJ ij = new ImageJ();
		ij.ui().showUI();
				// open the Clown sample
		OpenDialog od = new OpenDialog("Select image");
		ImagePlus image = IJ.openImage(od.getPath());
		image.show();

		// run the plugin
		IJ.runPlugIn(clazz.getName(), "");
	}


}
