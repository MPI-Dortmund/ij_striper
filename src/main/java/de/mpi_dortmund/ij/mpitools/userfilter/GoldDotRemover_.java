package de.mpi_dortmund.ij.mpitools.userfilter;

import java.awt.Polygon;
import java.util.ArrayList;

import de.mpi_dortmund.ij.mpitools.helicalPicker.Helical_Picker_;
import de.mpi_dortmund.ij.mpitools.skeletonfilter.LineTracer;
import ij.IJ;
import ij.ImagePlus;
import ij.blob.Blob;
import ij.blob.ManyBlobs;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

public class GoldDotRemover_ implements PlugIn, IUserFilter {
	double toleranceDia = 5.0;
	boolean doScalingDia = true;
	boolean saturateDia = true;
	double minDistanceToGold = 32;
	private int filterLargeDia = 9;
	private int filterSmallDia = 5;
	private int minBlobArea = 15;
	int choiceIndex = 0;

	public ArrayList<Polygon> apply(ImageProcessor input_image, ImageProcessor response_map,
			ImageProcessor line_image) {
		ImageProcessor input_image_dup = input_image.duplicate();
		ImagePlus help = new ImagePlus("",input_image_dup);
		IJ.run(help, "Bandpass Filter...", "filter_large="+filterLargeDia+" filter_small="+filterSmallDia+" suppress=None tolerance=5 autoscale saturate");
		input_image_dup = help.getProcessor();
		input_image_dup.invert();
		input_image_dup.threshold(254);
		ManyBlobs mb = new ManyBlobs(new ImagePlus("", input_image_dup));
		mb.setBackground(0);
		mb.findConnectedComponents();
	
		mb = mb.filterBlobs(minBlobArea, Blob.GETENCLOSEDAREA);
		
		ImageProcessor line_image_dup = line_image.duplicate();
		distanceToBlobFilter(mb,line_image_dup);
		LineTracer tracer = new LineTracer();

		return tracer.extractLines((ByteProcessor) line_image_dup);
	}
	
	public void distanceToBlobFilter(ManyBlobs mb, ImageProcessor line_image){
		
		for(int x = 0; x < line_image.getWidth(); x++){
			for(int y = 0; y < line_image.getHeight(); y++){
				if(line_image.get(x, y)==255){
					for (Blob blob : mb) {
						if(distanceToBlob(x, y, blob)<minDistanceToGold){
							line_image.set(x, y, 0);
							continue;
						}
					}
				}
			}
		}
	}
	
	public double distanceToBlob(int x, int y, Blob b){
		return b.getCenterOfGravity().distance(x, y);
	}



	
	public void run(String arg) {
		GenericDialog gd = new GenericDialog("Gold Dot Remover");
		gd.addNumericField("Filter_large structures down to", filterLargeDia, 0, 4, "pixels");
		gd.addNumericField("Filter_small structures up to", filterSmallDia, 0, 4, "pixels");
		gd.addNumericField("Min. distance to gold blob", minDistanceToGold, 0);
		gd.addNumericField("Min. blob area", minBlobArea, 0);
		gd.showDialog();

		if(gd.wasCanceled()){
			return;
		}

		filterLargeDia = (int) gd.getNextNumber();
		filterSmallDia = (int) gd.getNextNumber();
		minDistanceToGold = gd.getNextNumber();
		minBlobArea = (int) gd.getNextNumber();
		Helical_Picker_.registerUserFilter(this);

	}

	public String getFilterName() {
		// TODO Auto-generated method stub
		return "GOLD-DOT-LINE-FILTER";
	}





}
