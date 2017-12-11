package de.mpi_dortmund.ij.mpitools.FilamentEnhancer;

import java.util.ArrayList;

import de.mpi_dortmund.ij.mpitools.helicalPicker.logger.CentralLog;
import ij.process.FHT;
import ij.process.FloatProcessor;

public class TransformedMaskProvider {
	
	private static ArrayList<FHT> fftOfFilters = null;
	static int last_fft_mask_width=0;
	static int last_fft_filament_width=0;
	static int last_angle_step = 2;
	static int last_mask_size = 0;
	
	private static synchronized void fillFFTFilters(int mask_size, int filament_width, int mask_width, int angle_step, int type){
		if(isPower2(mask_size)==false){
			throw new IllegalArgumentException("Mask size is not a power of 2");
		}
		
		if(fftOfFilters==null || (mask_size != last_mask_size) || (filament_width != last_fft_filament_width) || (mask_width != last_fft_mask_width) || (angle_step != last_angle_step)){

			last_fft_filament_width = filament_width;
			last_fft_mask_width = mask_width;
			last_angle_step = angle_step;
			last_mask_size = mask_size;
			MaskCreator_ maskCreator = new MaskCreator_();
			fftOfFilters = new ArrayList<FHT>();
			FloatProcessor fp = maskCreator.generateMask(mask_size,filament_width, mask_width,type);

			FHT h = new FHT(fp.duplicate());
			CentralLog.getInstance().info(CentralLog.m("Transform mask!"));
			h.transform();
			fftOfFilters.add(h);
			
			int N = 180/angle_step;
			
			for(int i = 1; i < N; i++){
				FloatProcessor fpdub = (FloatProcessor) fp.duplicate();
				fpdub.setInterpolationMethod(FloatProcessor.BICUBIC);
				fpdub.rotate(i*angle_step);
				
				h = new FHT(fpdub);
				CentralLog.getInstance().info(CentralLog.m("Transform mask rotation: " + i*angle_step));
				h.transform();
				fftOfFilters.add(h);
			}
			
			
		}
	}
	
	public synchronized ArrayList<FHT> getTransformedMasks(int mask_size, int filament_width, int mask_width, int angle_step, int type){
		
		fillFFTFilters(mask_size, filament_width, mask_width, angle_step, type);
		
		return fftOfFilters;
		
	}
	
	private static boolean isPower2(int n){
		return ((n & (n - 1)) == 0);
	}
	


}
