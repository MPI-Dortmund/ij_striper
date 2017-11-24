package de.mpi_dortmund.ij.mpitools.FilamentEnhancer;

import de.mpi_dortmund.ij.mpitools.helicalPicker.custom.IContext;

public class FilamentEnhancerContext {
	private int filament_width; 
	private int mask_width; 
	private int angle_step;
	private boolean equalize;
	
	/**
	 * The filament width defines the how broad a filament is (measured in pixel).
	 * @return The filament width in pixel
	 */
	public int getFilamentWidth() {
		return filament_width;
	}
	
	/**
	 * The filament width defines the how broad a filament is (measured in pixel).
	 * @param filament_width width of a filament
	 */
	public void setFilamentWidth(int filament_width) {
		this.filament_width = filament_width;
	}
	
	/**
	 * 
	 * @return
	 */
	public int getMaskWidth() {
		return mask_width;
	}
	
	public void setMaskWidth(int mask_width) {
		this.mask_width = mask_width;
	}
	
	/**
	 * The angle step defines the degree how fine the directions will be enhanced. 
	 * @return angle in degree
	 */
	public int getAngleStep() {
		return angle_step;
	}
	
	/**
	 * The angle step defines the degree how fine the directions will be enhanced. 
	 * @param angle_step Angle step size in degree
	 */
	public void setAngleStep(int angle_step) {
		this.angle_step = angle_step;
	}
	
	
	/**
	 * If equalization is turned on, large high contrast contamination don't have less effect on the detection.
	 * @return true if equalization is turned on
	 */
	public boolean doEqualization() {
		return equalize;
	}
	
	/**
	 * If equalization is turned on, large high contrast contamination don't have less effect on the detection.
	 * @param equalize
	 */
	public void setEqualize(boolean equalize) {
		this.equalize = equalize;
	}
	
	@Override
	public String toString() {
		String str = "FilamentEnhancerContext ### Filament width: " + filament_width + " Mask width: " + mask_width + " Angle step: " + angle_step + " Equalize: " + equalize;
		return str;
	}
	
	
}
