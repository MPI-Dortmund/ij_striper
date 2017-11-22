package de.mpi_dortmund.ij.mpitools.helicalPicker.custom;

import de.mpi_dortmund.ij.mpitools.helicalPicker.gui.SliceRange;

public interface IWorker {
	public Object clone_worker();
	
	public void setSliceRange(SliceRange slice_range);
	
	public SliceRange getSliceRange();
}
