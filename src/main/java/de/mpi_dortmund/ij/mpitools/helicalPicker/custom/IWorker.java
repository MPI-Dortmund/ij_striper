package de.mpi_dortmund.ij.mpitools.helicalPicker.custom;

public interface IWorker {
	public Object clone_worker();
	
	public void setSliceFrom(int i);
	public void setSliceTo(int i);
}
