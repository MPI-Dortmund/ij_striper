package de.mpi_dortmund.ij.mpitools.FilamentEnhancer;

public class FilamentEnhancerContext {
	private int filament_width; 
	private int mask_width; 
	private int angle_step;
	private boolean equalize;
	
	public int getFilamentWidth() {
		return filament_width;
	}
	public void setFilamentWidth(int filament_width) {
		this.filament_width = filament_width;
	}
	public int getMaskWidth() {
		return mask_width;
	}
	public void setMaskWidth(int mask_width) {
		this.mask_width = mask_width;
	}
	public int getAngleStep() {
		return angle_step;
	}
	public void setAngleStep(int angle_step) {
		this.angle_step = angle_step;
	}
	public boolean isEqualize() {
		return equalize;
	}
	public void setEqualize(boolean equalize) {
		this.equalize = equalize;
	}
	
	
}
