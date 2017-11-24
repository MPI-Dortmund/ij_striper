package de.mpi_dortmund.ij.mpitools.helicalPicker.FilamentDetector;

public class FilamentDetectorContext {
	
	private double sigma;
	private DetectionThresholdRange thresholdRange;
	
	
	public double getSigma() {
		return sigma;
	}
	public void setSigma(double sigma) {
		this.sigma = sigma;
	}
	public DetectionThresholdRange getThresholdRange() {
		return thresholdRange;
	}
	public void setThresholdRange(DetectionThresholdRange thresholdRange) {
		this.thresholdRange = thresholdRange;
	}
	
	public static double filamentWidthToSigma(int filament_width){
		double sigma = filament_width/(2*Math.sqrt(3)) + 0.5;
		return sigma;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "Sigma " + sigma + " Upper: " + thresholdRange.getUpperThreshold() + " Lower: " + thresholdRange.getLowerThreshold();
	}
	

}
