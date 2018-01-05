package de.mpi_dortmund.ij.mpitools.helicalPicker;

import java.awt.Color;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.HashMap;

import de.biomedical_imaging.ij.steger.Line;
import de.biomedical_imaging.ij.steger.Lines;
import de.mpi_dortmund.ij.mpitools.helicalPicker.gui.HelicalPickerGUI;
import de.mpi_dortmund.ij.mpitools.userfilter.IUserFilter;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.Overlay;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.io.OpenDialog;
import ij.plugin.PlugIn;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import net.imagej.ImageJ;

public class Helical_Picker_ implements PlugIn {

	private static Helical_Picker_ instance;
	private static HelicalPickerGUI gui;
	private ImagePlus input_image;
	private static ArrayList<IUserFilter> userFilters;
	private HashMap<Integer, ArrayList<Polygon>> detected_lines;
	
	public static synchronized Helical_Picker_ getInstance() {
		if(instance==null){
			instance = new Helical_Picker_();
		}
		return instance;
	}
	
	public  HashMap<Integer, ArrayList<Polygon>> getDetectedLines(){
		return detected_lines;
	}
	
	public void setDetectedLines(HashMap<Integer, ArrayList<Polygon>> detected_lines){
		this.detected_lines = detected_lines;
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
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				gui.createAndShowGUI();
			}
		});
		
		
		
	}
	
	public ImagePlus getImage(){
		return input_image;
	}
	
	public static HelicalPickerGUI getGUI(){
		return gui;
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

		Class<?> clazz = Helical_Picker_.class;
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
