package de.mpi_dortmund.ij.mpitools.helicalPicker.gui;

public class SliceRange {
	private int slice_from;
	private int slice_to;
	public SliceRange(int slice_from, int slice_to) {
		this.slice_from = slice_from;
		this.slice_to = slice_to;
	}
	
	public int getSliceFrom(){
		return slice_from;
	}
	
	public int getSliceTo(){
		return slice_to;
	}

}
