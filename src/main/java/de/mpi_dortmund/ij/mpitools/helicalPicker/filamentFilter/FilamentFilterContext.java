package de.mpi_dortmund.ij.mpitools.helicalPicker.filamentFilter;

import java.util.ArrayList;

import de.mpi_dortmund.ij.mpitools.userfilter.IUserFilter;
import ij.IJ;
import ij.ImagePlus;

public class FilamentFilterContext {
	private int min_filament_length ;
	private double min_line_straightness; 
	private int window_width_straightness;
	private int removement_radius;
	private boolean fit_distribution;
	private double sigma_min_response;
	private double sigma_max_response;
	private int min_filament_distance;
	private int border_diameter;
	private double double_filament_insensitivity;
	
	
	ArrayList<IUserFilter> userFilters;
	ImagePlus mask;
	
	public FilamentFilterContext() {
		min_filament_length = 0;
		min_line_straightness = 0;
		window_width_straightness = 5;
		removement_radius = 0;
		fit_distribution = false;
		sigma_min_response = 0;
		sigma_max_response = 0;
		min_filament_distance = 0;
		border_diameter = 0;
		double_filament_insensitivity = 1;
		userFilters = null;
		mask = null;
	}
	
	/**
	 * @return Returns the minimum length of the filament 
	 */
	public int getMinimumFilamentLength() {
		
		return min_filament_length;
	}
	
	/**
	 * Sets the minimum length of a filament.
	 * @param min_filament_length Minimum length of the filament
	 */
	public void setMinimumFilamentLength(int min_filament_length) {
		this.min_filament_length = min_filament_length;
	}

	/**
	 * Returns the minimum line straightness. All line with a lower straightness will be removed.
	 * The line straightness measures the straightness of a line and take values 
	 * between 0 (very random movement) and 1 (straight line)
	 * @return Minimum value of line straightness.
	 */
	public double getMinimumLineStraightness() {
		return min_line_straightness;
	}

	/**
	 * Set the value of the minimum line straightness.The line straightness measures the straightness 
	 * of a line and take values between 0 (very random movement) and 1 (straight line).
	 * Parts of a line with a lower straightness will be removed and the line is splitted.
	 * @param min_line_straightness
	 */
	public void setMinimumLineStraightness(double min_line_straightness) {
		this.min_line_straightness = min_line_straightness;
	}

	/**
	 * Returns the running window size for local straightness evaluation. The local evaluation is done by
	 * a running window.
	 * @return Running window size for local straightness evaluation
	 */
	public int getWindowWidthStraightness() {
		return window_width_straightness;
	}

	/**
	 * Sets the running window size for local straightness evaluation. The local evaluation is done by
	 * a running window.
	 * @param window_width_straightness Running window size for local straightness evaluation
	 */
	public void setWindowWidthStraightness(int window_width_straightness) {
		this.window_width_straightness = window_width_straightness;
	}

	/**
	 * Returns the removement radius around a junction point. When a junction is removed, it will remove
	 * all line points within radius.
	 * @return The removement radius around a junction point.
	 */
	public int getJunctionRemovementRadius() {
		return removement_radius;
	}

	/**
	 * Sets the removement radius around a junction point. When a junction is removed, it will remove
	 * all line points within radius.
	 * @param removement_radius The removement radius around a junction point.
	 */
	public void setJunctionRemovementRadius(int removement_radius) {
		this.removement_radius = removement_radius;
	}

	/**
	 * Returns if the estimaton b of the mean / sd of the line response by model fitting  
	 * during response filtering.
	 * @return true if the estimation is done by model fitting.
	 */
	public boolean isFitDistribution() {
		return fit_distribution;
	}

	/**
	 * Activates/deactivates the estimaton b of the mean / sd of the line response by model fitting  
	 * during response filtering. Default values is false.
	 * @param fit_distribution true if the estimation is done by model fitting
	 */
	public void setFitDistribution(boolean fit_distribution) {
		this.fit_distribution = fit_distribution;
	}

	public double getSigmaMinResponse() {
		return sigma_min_response;
	}

	public void setSigmaMinResponse(double sigma_min_response) {
		this.sigma_min_response = sigma_min_response;
	}

	public double getSigmaMaxResponse() {
		return sigma_max_response;
	}

	public void setSigmaMaxResponse(double sigma_max_response) {
		this.sigma_max_response = sigma_max_response;
	}

	public int getMinFilamentDistance() {
		return min_filament_distance;
	}

	public void setMinFilamentDistance(int min_filament_distance) {
		this.min_filament_distance = min_filament_distance;
	}

	public int getBorderDiameter() {
		return border_diameter;
	}

	public void setBorderDiameter(int border_diameter) {
		this.border_diameter = border_diameter;
	}

	public double getDoubleFilamentInsensitivity() {
		return double_filament_insensitivity;
	}

	public void setDoubleFilamentInsensitivity(double double_filament_insensitivity) {
		this.double_filament_insensitivity = double_filament_insensitivity;
	}
	
	public ArrayList<IUserFilter> getUserFilters(){
		return userFilters;
	}
	
	public void setUserFilters(ArrayList<IUserFilter> userFilters){
		this.userFilters = userFilters;
	}
	
	public void setBinaryMask(ImagePlus mask){
		this.mask = mask;
	}
	
	public ImagePlus getBinaryMask(){
		return mask;
	}
	
	@Override
	public String toString() {
		min_filament_length = 0;
		min_line_straightness = 0;
		window_width_straightness = 5;
		removement_radius = 0;
		fit_distribution = false;
		sigma_min_response = 0;
		sigma_max_response = 0;
		min_filament_distance = 0;
		border_diameter = 0;
		double_filament_insensitivity = 1;
		userFilters = null;
		mask = null;
		return "FilamentFilterContext ### Min. Filament length: " + min_filament_length + " Min. straightness " + min_line_straightness + " Min. window width: " + window_width_straightness + ""
				+ " Removement radius: " + removement_radius + " Fit distr: " + fit_distribution + " Sigma min: " + sigma_min_response + " Sigma max: " + sigma_max_response + ""
						+ " Min. filament distance " + min_filament_distance +  " Min. border diameter " + border_diameter + " Double filament insens: " + double_filament_insensitivity + ""
								+ " UserFilters: " + !(userFilters==null) + " Mask: " + !(mask == null);
	}
	
	
}
