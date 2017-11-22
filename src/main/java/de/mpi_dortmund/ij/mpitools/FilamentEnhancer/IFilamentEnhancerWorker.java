package de.mpi_dortmund.ij.mpitools.FilamentEnhancer;

import java.util.ArrayList;

import de.mpi_dortmund.ij.mpitools.helicalPicker.gui.SliceRange;
import ij.process.ImageProcessor;

public interface IFilamentEnhancerWorker {
	
	ArrayList<ImageProcessor> getMaps();
	
	public void setSliceRange(SliceRange slice_range);
	
	public SliceRange getSliceRange();

}
