package de.mpi_dortmund.ij.mpitools.FilamentEnhancer;

import java.util.ArrayList;

import ij.process.ImageProcessor;

public interface IFilamentEnhancerWorker {
	
	ArrayList<ImageProcessor> getMaps();
	
	public int getSliceFrom();
	
	public int getSliceTo();

}
