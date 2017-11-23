package de.mpi_dortmund.ij.mpitools.helicalPicker;

import java.awt.Color;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import de.biomedical_imaging.ij.steger.Line;
import de.biomedical_imaging.ij.steger.Lines;
import de.mpi_dortmund.ij.mpitools.FilamentEnhancer.FilamentEnhancer;
import de.mpi_dortmund.ij.mpitools.FilamentEnhancer.FilamentEnhancerContext;
import de.mpi_dortmund.ij.mpitools.boxplacer.BoxPlacer_;
import de.mpi_dortmund.ij.mpitools.boxplacer.BoxPlacingContext;
import de.mpi_dortmund.ij.mpitools.helicalPicker.FilamentDetector.DetectionThresholdRange;
import de.mpi_dortmund.ij.mpitools.helicalPicker.FilamentDetector.FilamentDetector;
import de.mpi_dortmund.ij.mpitools.helicalPicker.gui.HelicalPickerGUI;
import de.mpi_dortmund.ij.mpitools.helicalPicker.gui.SliceRange;
import de.mpi_dortmund.ij.mpitools.skeletonfilter.SkeletonFilterContext;
import de.mpi_dortmund.ij.mpitools.skeletonfilter.SkeletonFilter_;
import de.mpi_dortmund.ij.mpitools.userfilter.IUserFilter;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Overlay;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.io.OpenDialog;
import ij.plugin.PlugIn;
import ij.process.ByteProcessor;
import ij.process.FloatPolygon;
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
	
	public void showLinesAsPreview(ArrayList<Polygon> lines){

		Overlay ovpoly = input_image.getOverlay();
		if(ovpoly==null){
			ovpoly = new Overlay();
			input_image.setOverlay(ovpoly);
		}
		// Print contour
		for (Polygon line : lines) {
			
				PolygonRoi polyRoiMitte = new PolygonRoi(line,
						Roi.POLYLINE);
				
				polyRoiMitte.setStrokeColor(Color.red);
				int position = input_image.getCurrentSlice();
			
			
				polyRoiMitte.setPosition(position);
				ovpoly.add(polyRoiMitte);
				
		}
		input_image.updateAndRepaintWindow();

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
