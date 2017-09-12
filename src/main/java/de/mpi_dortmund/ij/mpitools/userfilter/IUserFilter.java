package de.mpi_dortmund.ij.mpitools.userfilter;

import java.awt.Polygon;
import java.util.ArrayList;

import ij.process.ImageProcessor;

public interface IUserFilter {
	
	public ArrayList<Polygon> apply(ImageProcessor input_image, ImageProcessor response_map, ImageProcessor line_image);
	
	public String getFilterName();

}
