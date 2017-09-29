package de.mpi_dortmund.ij.mpitools.FilamentEnhancer;

import java.awt.Color;
import java.lang.reflect.GenericArrayType;
import java.util.ArrayList;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.GenericDialog;
import ij.gui.Toolbar;
import ij.plugin.Histogram;
import ij.plugin.ZProjector;
import ij.plugin.filter.PlugInFilter;
import ij.plugin.filter.RankFilters;
import ij.process.ByteProcessor;
import ij.process.FHT;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;

public class FilamentEnhancer_ implements PlugInFilter {

	ArrayList<FHT> fftOfFilters = null;
	int filament_width;
	int mask_width;
	int angle_step;
	boolean show_mask;
	boolean show_response_stack;
	int last_fft_mask_width=0;
	int last_fft_filament_width=0;
	int type;
	boolean equalize;
	public int setup(String arg, ImagePlus imp) {
		GenericDialog gd = new GenericDialog("Mask creator");
		gd.addNumericField("Filament width", 16, 0);
		gd.addNumericField("Mask width", 128, 0);
		gd.addSlider("Angle step", 1, 180, 2);
		
		gd.addChoice("Type", new String[]{"First","Second"}, "Second");
		gd.addCheckbox("Show mask", false);
		gd.addCheckbox("Show reponse stack", false);
		gd.addCheckbox("Equalize", false);
	//	gd.addNumericField("Blurring radius", 10, 0);
		gd.showDialog();
		
		if(gd.wasCanceled()){
			return DONE;
		}
		
		filament_width = (int) gd.getNextNumber();
		mask_width = (int )gd.getNextNumber();
		angle_step = (int) gd.getNextNumber();
		type = gd.getNextChoiceIndex();
		show_mask = gd.getNextBoolean();
		show_response_stack = gd.getNextBoolean();
		equalize = gd.getNextBoolean();
		
		
	//	IJ.run(imp, "Invert", "stack");
		
		return IJ.setupDialog(imp, DOES_ALL+PARALLELIZE_STACKS);
	}
	
	public synchronized  void fillFFTFilters(int mask_size, int filament_width, int mask_width, int angle_step,boolean show_mask, int type){
		if(isPower2(mask_size)==false){
			throw new IllegalArgumentException("Mask size is not a power of 2");
		}
		
		if(fftOfFilters==null || (filament_width != last_fft_filament_width) || (mask_width != last_fft_mask_width)){
			
			MaskCreator_ maskCreator = new MaskCreator_();
			fftOfFilters = new ArrayList<FHT>();
			FloatProcessor fp = maskCreator.generateMask(mask_size,filament_width, mask_width,type);
			ImageStack maskStack = new ImageStack(mask_size, mask_size);
			maskStack.addSlice(fp);
			FHT h = new FHT(fp.duplicate());
			h.transform();
			fftOfFilters.add(h);
			
			int N = 180/angle_step;
			
			for(int i = 1; i < N; i++){
				FloatProcessor fpdub = (FloatProcessor) fp.duplicate();
				fpdub.setInterpolationMethod(FloatProcessor.BICUBIC);
				
				fpdub.rotate(i*angle_step);
				
				maskStack.addSlice(fpdub);
				h = new FHT(fpdub);
				h.transform();
				fftOfFilters.add(h);
			}
			
			if(show_mask){
				ImagePlus mask = new ImagePlus("Mask", maskStack);
				mask.show();
			}
			last_fft_filament_width = filament_width;
			last_fft_mask_width = mask_width;
		}
	}

	public void run(ImageProcessor ip) {
		enhance_filaments(ip, filament_width, mask_width, angle_step, show_mask, equalize, type);
	}
	
	public void enhance_filaments(ImageProcessor ip, int filament_width, int mask_width, int angle_step,boolean show_mask, boolean equalize, int type){
		
		// Adjust size to power of 2
		int max = ip.getWidth()>ip.getHeight()?ip.getWidth():ip.getHeight();
		int old_width = ip.getWidth();
		int old_height = ip.getHeight();
		int new_size = nextPower2(max);
		int mask_size = new_size;
		ImagePlus help = new ImagePlus("", ip);
		int mean = (int) ip.getStatistics().mean;
		Toolbar.setBackgroundColor(new Color(mean, mean, mean));
		IJ.run(help, "Canvas Size...", "width="+new_size+" height="+new_size+" position=Center");
		ImageProcessor ip_resize = ip.resize(new_size, new_size);
		
		for(int x = 0; x < ip_resize.getWidth(); x++){
			for(int y = 0; y < ip_resize.getHeight(); y++){
				ip_resize.set(x, y,help.getProcessor().get(x, y));
			}
		}	
	
		fillFFTFilters(mask_size, filament_width,mask_width,angle_step,show_mask,type);
		ip_resize.invert();
		FHT h1, h2=null;
		h1 = new FHT(ip_resize);
		h1.transform();
		ImageStack enhancedStack = new ImageStack(ip_resize.getWidth(), ip_resize.getHeight());
		for(int i = 0; i < fftOfFilters.size(); i++){
			h2 = fftOfFilters.get(i);
			
			FHT result = h1.multiply(h2);
			result.inverseTransform();
			result.swapQuadrants();
			result.resetMinAndMax();
			
			enhancedStack.addSlice(result);
		}
		
		ImagePlus imp = new ImagePlus("enhancedstack", enhancedStack);
		if(show_response_stack){
			imp.show();
		}
		
		ZProjector zproj = new ZProjector();
		zproj.setImage(imp);
		zproj.setMethod(ZProjector.MAX_METHOD);
		zproj.doProjection();
		ImagePlus maxProj = zproj.getProjection();
		
	//	zproj.setMethod(ZProjector.MIN_METHOD);
	//	zproj.doProjection();
	//	ImagePlus minProj = zproj.getProjection();
	//	minProj.show();
	//	maxProj.show();
		FloatProcessor maxProjProc = (FloatProcessor) maxProj.getProcessor();
		if(equalize){
			
			ImageStatistics stat = maxProjProc.getStatistics();
			maxProjProc.subtract(stat.mean);
			maxProjProc.multiply(1.0/stat.stdDev);
			stat = maxProjProc.getStatistics();
			maxProj = new ImagePlus("", maxProjProc);
			IJ.setMinAndMax(maxProj, stat.min, 5);
			
			
		}
		ByteProcessor bp = maxProj.getProcessor().convertToByteProcessor();
		
		//Resize back to old size
		help = new ImagePlus("", bp);
		IJ.run(help, "Canvas Size...", "width="+old_width+" height="+old_height+" position=Center zero");
		bp = (ByteProcessor) help.getProcessor();

		for(int x = 0; x < ip.getWidth(); x++){
			for(int y = 0; y < ip.getHeight(); y++){
				ip.set(x, y,bp.get(x, y));
			}
		}
		
		
		
	}
	
	public boolean isPower2(int n){
		return ((n & (n - 1)) == 0);
	}
	
	public int nextPower2(int n){
		int p = 2;
		int v = (int) Math.pow(2, p);
		while(v<n){
			p++;
			v = (int) Math.pow(2, p);
		}
		return v;
	}
	

}
