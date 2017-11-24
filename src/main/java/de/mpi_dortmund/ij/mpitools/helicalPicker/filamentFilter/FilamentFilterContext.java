package de.mpi_dortmund.ij.mpitools.helicalPicker.filamentFilter;

import java.util.ArrayList;

import de.mpi_dortmund.ij.mpitools.userfilter.IUserFilter;
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
	
	public int getMinimumFilamentLength() {
		return min_filament_length;
	}

	public void setMinimumFilamentLength(int min_filament_length) {
		this.min_filament_length = min_filament_length;
	}

	public double getMinimumLineStraightness() {
		return min_line_straightness;
	}

	public void setMinimumLineStraightness(double min_line_straightness) {
		this.min_line_straightness = min_line_straightness;
	}

	public int getWindowWidthStraightness() {
		return window_width_straightness;
	}

	public void setWindowWidthStraightness(int window_width_straightness) {
		this.window_width_straightness = window_width_straightness;
	}

	public int getRemovementRadius() {
		return removement_radius;
	}

	public void setRemovementRadius(int removement_radius) {
		this.removement_radius = removement_radius;
	}

	public boolean isFitDistribution() {
		return fit_distribution;
	}

	public void setFitDistribution(boolean fit_distribution) {
		this.fit_distribution = fit_distribution;
	}

	public double getMinimumSigmaResponse() {
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
		return "Min. Filament length: " + min_filament_length + " Min. straightness " + min_line_straightness + " Min. window width: " + window_width_straightness + ""
				+ " Removement radius: " + removement_radius + " Fit distr: " + fit_distribution + " Sigma min: " + sigma_min_response + " Sigma max: " + sigma_max_response + ""
						+ " Min. filament distance " + min_filament_distance +  " Min. border diameter " + border_diameter + " Double filament insens: " + double_filament_insensitivity + ""
								+ " UserFilters: " + !(userFilters==null) + " Mask: " + !(mask == null);
	}
	
	
}
