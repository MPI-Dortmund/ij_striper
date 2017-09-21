package de.mpi_dortmund.ij.mpitools.userfilter;

import java.awt.AWTEvent;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import com.sun.tools.classfile.Dependency.Finder;

import de.mpi_dortmund.ij.mpitools.helicalPicker.Helical_Picker_;
import de.mpi_dortmund.ij.mpitools.skeletonfilter.LineTracer;
import fiji.util.gui.GenericDialogPlus;
import ij.IJ;
import ij.ImagePlus;
import ij.blob.Blob;
import ij.blob.ManyBlobs;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.gui.NonBlockingGenericDialog;
import ij.gui.Overlay;
import ij.gui.PointRoi;
import ij.gui.PolygonRoi;
import ij.plugin.ImageCalculator;
import ij.plugin.PlugIn;
import ij.plugin.filter.ExtendedPlugInFilter;
import ij.plugin.filter.MaximumFinder;
import ij.plugin.filter.PlugInFilter;
import ij.plugin.filter.PlugInFilterRunner;
import ij.plugin.filter.RankFilters;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

public class GoldDotRemover2_ implements ExtendedPlugInFilter, IUserFilter, DialogListener {

	int radius1;
	int radius2;
	int tolerance;
	int minDistanceToGold;
	boolean isPreview;
	ImagePlus input_imp;
	@Override
	public ArrayList<Polygon> apply(ImageProcessor input_image, ImageProcessor response_map,
			ImageProcessor line_image) {

		Polygon max = findMaxima(input_image);
		ImageProcessor line_image_dup = line_image.duplicate();
		distanceToPointFilter(max, line_image_dup);
		LineTracer tracer = new LineTracer();

		return tracer.extractLines((ByteProcessor) line_image_dup);

	}
	
	
	public Polygon findMaxima(ImageProcessor input_image){
		FloatProcessor fp = input_image.duplicate().convertToFloatProcessor();
		RankFilters rnk = new RankFilters();

		FloatProcessor fpMeanR1 = (FloatProcessor) fp.duplicate();
		rnk.rank(fpMeanR1, radius1, RankFilters.MEAN);
		FloatProcessor fpMeanR2 = (FloatProcessor) fp.duplicate();
		rnk.rank(fpMeanR2, radius2, RankFilters.MEAN);

		ImagePlus impR1 = new ImagePlus("", fpMeanR1);
		ImagePlus impR2 = new ImagePlus("", fpMeanR2);
		ImageCalculator icalc = new ImageCalculator();
		ImagePlus result = icalc.run("32-bit create subtract", impR1, impR2);

		MaximumFinder maxFind = new MaximumFinder();

		Polygon max = maxFind.getMaxima(result.getProcessor(), tolerance, true);
		
		return max;
	}

	public void distanceToPointFilter(Polygon points, ImageProcessor line_image){

		for(int x = 0; x < line_image.getWidth(); x++){
			for(int y = 0; y < line_image.getHeight(); y++){
				if(line_image.get(x, y)==255){

					for (int i = 0; i < points.npoints; i++) {
						if(distanceToPoint(x, y, points.xpoints[i],points.ypoints[i])<minDistanceToGold){
							line_image.set(x, y, 0);
							continue;
						}
					}
				}
			}
		}
	}

	public double distanceToPoint(int x, int y, int px, int py){
		return (new Point(px, py)).distance(x, y);
	}

	@Override
	public String getFilterName() {
		// TODO Auto-generated method stub
		return "Gold-Dot-Remover-2";
	}

	@Override
	public int setup(String arg, ImagePlus imp) {
		if(arg.equals("final")){
			isPreview = false;
			Helical_Picker_.registerUserFilter(this);
			imp.setOverlay(null);
			return DONE;
		}
		this.input_imp = imp;
		return DOES_8G+FINAL_PROCESSING;
	}

	@Override
	public void run(ImageProcessor ip) {
		if(isPreview){
			Polygon p =  findMaxima(ip);
			
			PointRoi proi = new PointRoi(p);
			proi.setPosition(input_imp.getCurrentSlice());
			Overlay ov = input_imp.getOverlay();
			if(ov == null){
				ov = new Overlay();
				input_imp.setOverlay(ov);
			}
			ov.add(proi);
			
		}

	}


	@Override
	public boolean dialogItemChanged(GenericDialog gd, AWTEvent e) {
		input_imp = IJ.getImage();
		isPreview = gd.isPreviewActive();
		radius1 = (int) gd.getNextNumber();
		radius2 = (int) gd.getNextNumber();
		tolerance = (int) gd.getNextNumber();
		minDistanceToGold = (int) gd.getNextNumber();
		return true;
	}


	@Override
	public int showDialog(ImagePlus imp, String command, PlugInFilterRunner pfr) {

		final GenericDialogPlus gd = new GenericDialogPlus("Gold do remover");
		gd.addNumericField("Radius 1", 5, 0);
		gd.addNumericField("Radius 2", 3, 0);
		gd.addNumericField("Tolerance", 8, 0);
		gd.addNumericField("Gold distance", 32, 0);
		gd.addButton("Previous slice", new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				
				if(input_imp.getCurrentSlice()!=1){
					input_imp.setSlice(input_imp.getCurrentSlice()-1);
					input_imp.setOverlay(null);
		
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

		radius1 = (int) gd.getNextNumber();
		radius2 = (int) gd.getNextNumber();
		tolerance = (int) gd.getNextNumber();
		minDistanceToGold = (int) gd.getNextNumber();
		isPreview = gd.isPreviewActive();
		return DOES_8G+FINAL_PROCESSING;
	}


	@Override
	public void setNPasses(int nPasses) {
		// TODO Auto-generated method stub
		
	}

}
