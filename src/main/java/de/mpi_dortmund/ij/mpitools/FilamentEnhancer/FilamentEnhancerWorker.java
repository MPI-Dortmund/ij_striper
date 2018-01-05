package de.mpi_dortmund.ij.mpitools.FilamentEnhancer;

import java.awt.Color;
import java.util.ArrayList;
import de.mpi_dortmund.ij.mpitools.helicalPicker.custom.IWorker;
import de.mpi_dortmund.ij.mpitools.helicalPicker.gui.SliceRange;
import de.mpi_dortmund.ij.mpitools.helicalPicker.logger.CentralLog;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Toolbar;
import ij.plugin.CanvasResizer;
import ij.plugin.ZProjector;
import ij.process.ByteProcessor;
import ij.process.FHT;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;

public class FilamentEnhancerWorker extends Thread implements IFilamentEnhancerWorker, IWorker {

	ArrayList<ImageProcessor> enhanced_maps;
	int type = 1;
	SliceRange slice_range;
	FilamentEnhancerContext context;
	ImageStack ips;
	private static Object lock_object = new Object();
	
	public FilamentEnhancerWorker(ImageStack ips, FilamentEnhancerContext context, SliceRange slice_range) {
		this.ips = ips;
		this.context = context;
		this.slice_range = slice_range;
	}
	public FilamentEnhancerWorker(FilamentEnhancerWorker a){
		this(a.ips,a.context,a.getSliceRange());
		
		
	}
	
	@Override
	public void run() {
		
		CentralLog.getInstance().info(CentralLog.m("Start enhancer - Filament width: " + context.getFilamentWidth() + " mask width: " + context.getMaskWidth() + " angle_step: " + context.getAngleStep() + " eq. " + context.doEqualization() + " slice from " + slice_range.getSliceFrom() + " slice to " + slice_range.getSliceTo()));
		int max = ips.getWidth()>ips.getHeight()?ips.getWidth():ips.getHeight();
		int old_width = ips.getWidth();
		int old_height = ips.getHeight();
		int new_size = nextPower2(max);
		int mask_size = new_size;
		
		
		CentralLog.getInstance().info(CentralLog.m("Calculate transformed masks"));
		TransformedMaskProvider mask_provider = new TransformedMaskProvider();
		CentralLog.getInstance().info(CentralLog.m("Transformed masks parameters - mask size: " + mask_size + " filament_width: " + context.getFilamentWidth() + " mask_width: " + context.getMaskWidth() + " angle_step " + context.getAngleStep() + " type " + type));
		ArrayList<FHT> transformed_masks = mask_provider.getTransformedMasks(mask_size, context.getFilamentWidth(), context.getMaskWidth(), context.getAngleStep(), type);
		CentralLog.getInstance().info(CentralLog.m("Transformed masks calculated"));
		
		enhanced_maps = new ArrayList<ImageProcessor>();
		for(int i = slice_range.getSliceFrom(); i <= slice_range.getSliceTo(); i++){
			CentralLog.getInstance().info(CentralLog.m("Enhance slice " + i));
			ImageProcessor ip = ips.getProcessor(i);
			/*
			 *  Adjust size to power of 2
			 */
			
			//ImagePlus help = new ImagePlus("", ip);
			CentralLog.getInstance().info(CentralLog.m("Calc mean " + i));
			int mean = (int) ip.getStatistics().mean;
			CentralLog.getInstance().info(CentralLog.m("Set col" + i));
			ImageProcessor ip_resize = null;
			int xoff = 0;
			int yoff = 0;
			CanvasResizer resizer = null;
			synchronized(lock_object){
				Toolbar.setBackgroundColor(new Color(mean, mean, mean));
				CentralLog.getInstance().info(CentralLog.m("Resize slice " + i + " new size: " + new_size));
			
				resizer = new CanvasResizer();
				xoff = (new_size-old_width)/2;
				yoff = (new_size-old_height)/2;
				ip = resizer.expandImage(ip, new_size, new_size, xoff, yoff);
			

				ip_resize = ip.resize(new_size, new_size);
				CentralLog.getInstance().info(CentralLog.m("Resize slice " + i + " done"));
			}
			
			// Fill new space with the mean value
			for(int x = 0; x < ip_resize.getWidth(); x++){
				for(int y = 0; y < ip_resize.getHeight(); y++){
					ip_resize.set(x, y,ip.get(x, y));
				}
			}
			
			/*
			 * Apply convolution in fourier space
			 */
			
			ip_resize.invert();
			FHT h1, h2=null;
			h1 = new FHT(ip_resize);
			CentralLog.getInstance().info(CentralLog.m("Transform slice " + i));
			h1.transform();
			ImageStack enhancedStack = new ImageStack(ip_resize.getWidth(), ip_resize.getHeight());
			
			for(int j = 0; j < transformed_masks.size(); j++){
			
				h2 = transformed_masks.get(j);
				CentralLog.getInstance().info(CentralLog.m("Multiply slice " + i + " Mask" + j));
			
				FHT result = h1.multiply(h2);
				
				result.inverseTransform();
				result.swapQuadrants();
				result.resetMinAndMax();
			
				enhancedStack.addSlice(result);
			}
			
			ImagePlus imp = new ImagePlus("enhancedstack", enhancedStack);		
			ZProjector zproj = new ZProjector();
			zproj.setImage(imp);
			zproj.setMethod(ZProjector.MAX_METHOD);
			CentralLog.getInstance().info(CentralLog.m("Z Project enhanced stack"));
			zproj.doProjection();
			ImagePlus maxProj = zproj.getProjection();
			
			FloatProcessor maxProjProc = (FloatProcessor) maxProj.getProcessor();
			if(context.doEqualization()){
				CentralLog.getInstance().info(CentralLog.m("Equalize"));
				ImageStatistics stat = maxProjProc.getStatistics();
				maxProjProc.subtract(stat.mean);
				maxProjProc.multiply(1.0/stat.stdDev);
				stat = maxProjProc.getStatistics();
				maxProj = new ImagePlus("", maxProjProc);
				IJ.setMinAndMax(maxProj, stat.min, 5);
				
				
			}
	
			ByteProcessor bp = maxProj.getProcessor().convertToByteProcessor();
			CentralLog.getInstance().info(CentralLog.m("Resize"));
			
			//Resize back to old size
			xoff = (old_width-new_size)/2;
			yoff = (old_height-new_size)/2;
			bp = (ByteProcessor) resizer.expandImage(bp, old_width, old_height, xoff, yoff);
			enhanced_maps.add(bp);
			CentralLog.getInstance().info(CentralLog.m("Enhancement slice " + i + " done"));			
		}
	}
	
	@Override
	public ArrayList<ImageProcessor> getMaps() {
		// TODO Auto-generated method stub
		return enhanced_maps;
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
	
	@Override
	public void setSliceRange(SliceRange slice_range){
		this.slice_range = slice_range;
	}


	@Override
	public FilamentEnhancerWorker clone_worker() {
		// TODO Auto-generated method stub
		return new FilamentEnhancerWorker(this);
	}
	
	@Override
	public SliceRange getSliceRange(){
		return slice_range;
	}
}
