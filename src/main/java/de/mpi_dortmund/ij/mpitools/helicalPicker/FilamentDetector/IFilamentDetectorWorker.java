package de.mpi_dortmund.ij.mpitools.helicalPicker.FilamentDetector;

import java.awt.Polygon;
import java.util.ArrayList;

import de.mpi_dortmund.ij.mpitools.helicalPicker.gui.SliceRange;

public interface IFilamentDetectorWorker {

	public SliceRange getSliceRange();

	public ArrayList<ArrayList<Polygon>> getLines();
	
	
}
