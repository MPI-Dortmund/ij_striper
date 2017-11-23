package de.mpi_dortmund.ij.mpitools.FilamentEnhancer;

import ij.process.FloatProcessor;

public class MaskCreator_ {
	
	public FloatProcessor generateMask(int mask_size, int filamentwidth, int maskwidth, int type){
		FloatProcessor fp = new FloatProcessor(mask_size, mask_size);
		double x0 = fp.getWidth()/2 + 0.5;
		double y0 = fp.getHeight()/2 + 0.5;
		double sigmax = maskwidth/2.355; //Full width at half maximum
		double varx = sigmax*sigmax;
		double sigmay = filamentwidth/2.355;
		double vary = sigmay*sigmay;
		
		for(int i = 0; i < mask_size; i++){
			for(int j = 0; j < mask_size; j++){
				double value = 0;
				double y = j+0.5;
				double x = i+0.5;
				if(type==1){
					value= -1.0*Math.PI*sigmax*(vary- Math.pow(y-y0,2))/(2*vary*sigmay) * Math.exp( -1.0*( Math.pow(x-x0, 2)/(2*varx) + Math.pow(y-y0, 2)/(2*vary)  ));
				}
				else if(type==0){
					value = (i-x0)*Math.exp( -1.0 * ( Math.pow(i-x0,2)/(2*varx) + Math.pow(j-y0,2)/(2*vary) ) );
				}
				fp.setf(i, j, (float) (value));
				
			}
		}
		fp.invert();
		fp.add(-1*fp.getf(0, 0));
		
		// Normalize
		double sum = 0;
		for(int x = 0; x < fp.getWidth(); x++){
			for(int y = 0; y < fp.getHeight(); y++){
				sum += fp.getf(x, y);
			}
		}
		double scale = 1.0/sum;
		//fp.multiply(scale);
		
		return fp;
	}



}
