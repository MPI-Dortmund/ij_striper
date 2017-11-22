package de.mpi_dortmund.ij.mpitools.helicalPicker.FilamentDetector;

import java.awt.Polygon;
import java.util.ArrayList;

import de.mpi_dortmund.ij.mpitools.helicalPicker.gui.SliceRange;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

public interface IFilamentDetectorWorker {

	public SliceRange getSliceRange();

	public ArrayList<ArrayList<Polygon>> getLines();
	
	
}
