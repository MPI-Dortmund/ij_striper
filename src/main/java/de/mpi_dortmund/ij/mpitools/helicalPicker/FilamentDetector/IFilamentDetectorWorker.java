package de.mpi_dortmund.ij.mpitools.helicalPicker.FilamentDetector;

import java.awt.Polygon;
import java.util.ArrayList;

import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

public interface IFilamentDetectorWorker {

	public int getSliceFrom();
	
	public int getSliceTo();
	
	public ArrayList<ArrayList<Polygon>> getLines();
	
	
}
