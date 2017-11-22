package de.mpi_dortmund.ij.mpitools.helicalPicker;

import java.util.HashSet;
import java.util.Random;

import de.biomedical_imaging.ij.steger.Line;
import de.biomedical_imaging.ij.steger.LineDetector;
import de.biomedical_imaging.ij.steger.Lines;
import de.biomedical_imaging.ij.steger.OverlapOption;
import de.mpi_dortmund.ij.mpitools.FilamentEnhancer.FilamentEnhancer;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.GenericDialog;
import ij.gui.Overlay;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.plugin.Selection;
import ij.plugin.filter.PlugInFilter;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

public class Ridge_Optimizer implements PlugInFilter {
	ImagePlus imp;
	Random rand;
	int filament_width;
	int GLOBAL_RUNS;
	int LOCAL_RUNS;
	int mask_width;
	boolean verbose;
	FilamentEnhancer enhancer;
	@Override
	public int setup(String arg, ImagePlus imp) {
		this.imp = imp;
		rand = new Random();
		rand.setSeed(1234);
		
		GenericDialog gd = new GenericDialog("Optimizer");
		gd.addNumericField("Number of global runs", 40, 0);
		gd.addNumericField("Number of local runs", 40, 0);
		gd.addNumericField("Filament width", 25, 0);
		gd.addNumericField("Mask width", 100, 0);
		gd.addCheckbox("Verbose", false);
		gd.showDialog();
		
		if(gd.wasCanceled()){
			return DONE;
		}
		GLOBAL_RUNS = (int) gd.getNextNumber();
		LOCAL_RUNS = (int) gd.getNextNumber();
		filament_width = (int) gd.getNextNumber();
		mask_width = (int) gd.getNextNumber();
		verbose = gd.getNextBoolean();
		return DOES_8G;
	}
	
	@Override
	public void run(ImageProcessor ip) {
		// TODO Auto-generated method stub
		
	}
	/*
	@Override
	public void run(ImageProcessor ip) {
		
		double[] res = optimize(imp, filament_width, mask_width, GLOBAL_RUNS, LOCAL_RUNS, verbose);
		IJ.log("Settings ### Filament width: " + filament_width + " #GLOBAL: " + GLOBAL_RUNS + " #LOCAL: " + LOCAL_RUNS + "###");
		IJ.log("OPTIMIZATION RESULT ### Lower threshold: " + res[0] + " Upper threshold: " + res[1] + " Goodness: " + res[2] + "###");
		
	}
	

	public double[] optimize(ImagePlus imp, int filament_width, int mask_width, int GLOBAL_RUNS, int LOCAL_RUNS, boolean verbose){
		Overlay ov = imp.getOverlay();
		
		
		HashSet<Integer> slicesWithSelectionSet = new HashSet<Integer>();
		for(int i = 0; i < ov.size(); i++){
			slicesWithSelectionSet.add(ov.get(i).getPosition());
		}
		Integer[] slicesWithSelection = slicesWithSelectionSet.toArray(new Integer[slicesWithSelectionSet.size()]);

		ImageProcessor[] ips = getArrayOfEnhancedFilaments(imp, slicesWithSelection, filament_width, mask_width);
		ImageProcessor[] selectionMaps = getArrayOfSelectionMaps(imp, slicesWithSelection);
		double[] r  = searchRange(ips, filament_width);
		IJ.log("Range: " + r[0] + " " + r[1] );
		double max_goodness = Integer.MIN_VALUE;
		double[] max_goodness_para = null;
		int N_RUNS = GLOBAL_RUNS+LOCAL_RUNS;
		for(int i = 0; i < N_RUNS; i++){
			
			double[] para = null;
			
			if(i<=GLOBAL_RUNS){
				para = nextParamterSet(r[0], r[1], r[0], r[1]);
			}
			else{
				int LRUN = i-GLOBAL_RUNS;
				double range = -0.0075*LRUN+0.5;
				para = nextParamterSet(max_goodness_para[0]*range, max_goodness_para[0]*(1+range), max_goodness_para[1]*range, max_goodness_para[1]*(1+range));
			}
			if(verbose){IJ.log("RUN: " + (i+1) + " LT: " + para[0] + " UT: " + para[1]);}
			double goodness = getGoodness(ips, selectionMaps, para[1], para[0], filament_width);
			if(goodness>max_goodness){
				max_goodness = goodness;
				max_goodness_para = para;
				if(verbose){IJ.log("Goodness: " + max_goodness + " LT: " + para[0] + " UT: " + para[1]);};
			}
		}
		
		double[] result = {max_goodness_para[0],max_goodness_para[1],max_goodness};
		
		return result;
	}
	
	public double[] optimize2(ImagePlus imp, int filament_width, int mask_width, int GLOBAL_RUNS, int LOCAL_RUNS, boolean verbose){
		Overlay ov = imp.getOverlay();
		
		
		HashSet<Integer> slicesWithSelectionSet = new HashSet<Integer>();
		for(int i = 0; i < ov.size(); i++){
			slicesWithSelectionSet.add(ov.get(i).getPosition());
		}
		Integer[] slicesWithSelection = slicesWithSelectionSet.toArray(new Integer[slicesWithSelectionSet.size()]);

		ImageProcessor[] ips = getArrayOfEnhancedFilaments(imp, slicesWithSelection, filament_width, mask_width);
		ImageProcessor[] selectionMaps = getArrayOfSelectionMaps(imp, slicesWithSelection);
		
		double max_goodness = Integer.MIN_VALUE;
		double[] max_goodness_para = null;
		int N_RUNS = GLOBAL_RUNS+LOCAL_RUNS;
		boolean NEW_MAX = false;
		int i_LOC = 0;
		for(int i = 0; i < N_RUNS; i++){
			
			double[] para = null;
			if(NEW_MAX==false){
				
				para = nextParamterSet(0, 3, 0, 3);
				i_LOC = 0;
						
			}
			else{
				i_LOC++;
				i--;
				para = nextParamterSet(max_goodness_para[0]*0.5, max_goodness_para[0]*1.5, max_goodness_para[1]*0.5, max_goodness_para[1]*1.5);
				IJ.log("LOCAL");
				if(i_LOC == LOCAL_RUNS){
					NEW_MAX=false;
				}
				
			}
			if(verbose){IJ.log("RUN: " + (i+1) + " LT: " + para[0] + " UT: " + para[1]);}

			double goodness = getGoodness(ips, selectionMaps, para[1], para[0], filament_width);
			if(goodness>max_goodness){
				max_goodness = goodness;
				max_goodness_para = para;
				if(verbose){IJ.log("Goodness: " + max_goodness + " LT: " + para[0] + " UT: " + para[1]);};
				NEW_MAX=true;
			}
			
		}
		
		double[] result = {max_goodness_para[0],max_goodness_para[1],max_goodness};
		
		return result;
	}
	
	public ImageProcessor[] getArrayOfSelectionMaps(ImagePlus imp, Integer[] slices){
		ImageProcessor[] selectionMaps = new ImageProcessor[slices.length];
		Overlay ov = imp.getOverlay();
		
		for(int i = 0; i < slices.length; i++){
			int slice = slices[i];
			if(selectionMaps[i] == null){
				selectionMaps[i] = new ByteProcessor(imp.getWidth(), imp.getHeight());
				selectionMaps[i].setValue(255);
			}
			
			for(int j = 0; j < ov.size(); j++){
				if(slice == ov.get(j).getPosition()){
					PolygonRoi p =  (PolygonRoi) ov.get(j);
					Roi r = Selection.lineToArea(p);
					
					selectionMaps[i].fill(r);
				}
			}
		}
		
		return selectionMaps;
	}
	
	public ImageStack getArrayOfEnhancedFilaments(ImagePlus imp, Integer[] slices, int filament_width, int mask_width){
		ImageProcessor[] ips = new ImageProcessor[slices.length];
		int angle_step = 2;
		boolean show_mask = false;
		boolean equalize = true;
		int type = 1;		
		
		if(enhancer==null){
			enhancer = new FilamentEnhancer(imp.getImageStack(), filament_width, mask_width, angle_step, equalize);
		}

		ImageStack enhanced = enhancer.getEnhancedImages(1, imp.getImageStackSize());
		
		
		return ips;
		
	}
	
	public double[] searchRange(ImageProcessor[] ips,double filament_width){
		LineDetector detect = new LineDetector();
		boolean found = false;
		double lb = 0;
		double ub = 20;
		double last_lb = 0;
		double last_ub_without_lines = 20;
		int max_filament_length = 0;
		boolean isDarkLine = false;
		boolean doCorrectPosition = true;
		boolean doEstimateWidth = false;
		boolean doExtendLine = true;
		double sigma = filament_width/(2*Math.sqrt(3)) + 0.5;
		
		do{
			IJ.log("Search range!");
			int N_FOUND = 0;
			for(int k = 0; k < ips.length; k++){
				ImageProcessor ip = ips[k];			
				Lines lines = detect.detectLines(ip, sigma, ub, lb, 0,max_filament_length, isDarkLine, doCorrectPosition, doEstimateWidth, doExtendLine, OverlapOption.NONE);
				N_FOUND += lines.size();
			}
			if(N_FOUND==0){
				last_ub_without_lines = ub;
				ub = ub/2;
				IJ.log("No Line New UB: " + ub);
			}
			else{
				ub = ub + (last_ub_without_lines -ub)/2;
				IJ.log("Lines found New UB: " + ub);
				
			}
			
		}while(Math.abs(ub-last_ub_without_lines)>0.01);
		
		return new double[]{0,last_ub_without_lines};
		
	}
	
	public double getGoodness(ImageProcessor[] ips, ImageProcessor[] selectionMaps, double ridge_ut,  double ridge_lt, double filament_width){
		LineDetector detect = new LineDetector();
		int max_filament_length = 0;
		boolean isDarkLine = false;
		boolean doCorrectPosition = true;
		boolean doEstimateWidth = false;
		boolean doExtendLine = true;
		double sigma = filament_width/(2*Math.sqrt(3)) + 0.5;
		
		int numberInSelection=0;
		int numberOutSelection=0;
		
		for(int k = 0; k < ips.length; k++){
			
			ImageProcessor ip = ips[k];
			ImageProcessor selectionMap = selectionMaps[k];
		
			Lines lines = detect.detectLines(ip, sigma, ridge_ut, ridge_lt, 0,max_filament_length, isDarkLine, doCorrectPosition, doEstimateWidth, doExtendLine, OverlapOption.NONE);
			
			for (Line line : lines) {
				float[] x  = line.getXCoordinates();
				float[] y  = line.getYCoordinates();
				for(int i = 0; i < x.length; i++){
					int v = selectionMap.get((int)x[i], (int)y[i]);
					if(v==255){
						numberInSelection++;
					}else{
						numberOutSelection++;
					}
				}
				
			}
		}
		return (0.8*numberInSelection - 0.2*numberOutSelection);
	}
	
	public double[] nextParamterSet(double min_lt,  double max_lt, double min_ut, double max_ut){
		
		double cand_lt = 0;
		double cand_ut = 0;
		
		do {
			cand_lt = min_lt + rand.nextDouble()*max_lt;
			cand_ut = min_ut + rand.nextDouble()*max_ut;
		} while(cand_ut < cand_lt);
		
		return new double[]{cand_lt,cand_ut};
	}
*/
}
