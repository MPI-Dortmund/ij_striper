package de.mpi_dortmund.ij.mpitools.helicalPicker.FilamentDetector;

public class DetectionThresholdRange {
	
	private double lower_threshold;
	private double upper_threshold;
	
	public DetectionThresholdRange(double lower_threshold, double upper_threshold) {
		this.lower_threshold = lower_threshold;
		this.upper_threshold = upper_threshold;
	}
	
	public double getLowerThreshold(){
		return lower_threshold;
	}
	
	public double getUpperThreshold(){
		return upper_threshold;
	}

}
