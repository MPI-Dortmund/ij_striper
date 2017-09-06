package de.mpi_dortmund.ij.mpitools.FilamentEnhancer;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.gui.Roi;
import ij.plugin.PlugIn;
import ij.plugin.filter.GaussianBlur;
import ij.process.FloatProcessor;

public class MaskCreator_ implements PlugIn {

	public void run(String arg) {
		GenericDialog gd = new GenericDialog("Mask creator");
		gd.addNumericField("Filament width", 16, 0);
		gd.addNumericField("Mask width", 128, 0);
	//	gd.addNumericField("Blurring radius", 10, 0);
		gd.showDialog();
		
		if(gd.wasCanceled()){
			return;
		}
		
		int filament_width = (int) gd.getNextNumber();
		int mask_width = (int )gd.getNextNumber();
	//	int blurring_radius = (int) gd.getNextNumber();
		FloatProcessor fp = generateMask(filament_width,mask_width);//generateMask(filament_width, mask_width, blurring_radius);

		ImagePlus mask = new ImagePlus("Mask", fp);
		mask.show();

		
	}
	
	public FloatProcessor generateMask2(int filament_width, int mask_width, double blurring_radius){
		
		FloatProcessor fp = new FloatProcessor(1024, 1024);
		
		Roi r = new Roi(512-mask_width/2, 512-filament_width/2, mask_width, filament_width);
		fp.setRoi(r);
		fp.set(1);
		fp.resetRoi();
		
		r = new Roi(512-mask_width/2, 512-filament_width/2+filament_width, mask_width, filament_width);
		fp.setRoi(r);
		fp.set(-0.5);
		fp.resetRoi();
		
		r = new Roi(512-mask_width/2, 512-filament_width/2-filament_width, mask_width, filament_width);
		fp.setRoi(r);
		fp.set(-0.5);
		fp.resetRoi();
		
		GaussianBlur blur = new GaussianBlur();

		blur.blurGaussian(fp, 0.4*blurring_radius, 0.4*blurring_radius, 0.001);
		return fp;
		
	}
	
	public FloatProcessor generateMask(int filamentwidth, int maskwidth){
		FloatProcessor fp = new FloatProcessor(1024, 1024);
		int x0 = fp.getWidth()/2;
		int y0 = fp.getHeight()/2;
		double sigmax = maskwidth/2.355; //Full width at half maximum
		double varx = sigmax*sigmax;
		double sigmay = filamentwidth/2.355;
		double vary = sigmay*sigmay;
		double normConstant = 1.0/(2*Math.PI*sigmax*sigmay);

		for(int i = 0; i < 1024; i++){
			for(int j = 0; j < 1024; j++){
			//	float value = (float) (normConstant*Math.exp( -1.0*( Math.pow(i-x0, 2)/(2*varx) + Math.pow(j-y0, 2)/(2*vary)  )));
			//	double value =  (Math.PI*(varx-Math.pow(x0-i,2))*(vary-Math.pow(y0-j,2)))/(2*varx*sigmax*vary*sigmay) * Math.exp( -1.0*( Math.pow(i-x0, 2)/(2*varx) + Math.pow(j-y0, 2)/(2*vary)  )) ;
				double value = -1.0*Math.PI*sigmax*(vary- Math.pow(j-y0,2))/(2*vary*sigmay) * Math.exp( -1.0*( Math.pow(i-x0, 2)/(2*varx) + Math.pow(j-y0, 2)/(2*vary)  ));
				
				fp.setf(i, j, (float) (1000*value));
			}
		}
		fp.invert();
		fp.add(-1*fp.getf(0, 0));
		
		
		return fp;
	}



}
