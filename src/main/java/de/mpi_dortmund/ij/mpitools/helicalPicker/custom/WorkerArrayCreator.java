package de.mpi_dortmund.ij.mpitools.helicalPicker.custom;

import java.lang.reflect.Array;

import de.mpi_dortmund.ij.mpitools.helicalPicker.gui.SliceRange;

public class WorkerArrayCreator {
	
	public <T> T[] createWorkerArray(int numberOfProcessors, SliceRange slice_range, IWorker clonable_worker){
		int nSlices = slice_range.getSliceTo()-slice_range.getSliceFrom()+1;
		int slicesPerThread = getNumberSlicesPerThreads(numberOfProcessors, nSlices);
		int Nmult = numberOfProcessors;
		int N = numberOfProcessors+(nSlices-numberOfProcessors*slicesPerThread);

		
		T[] workers =  (T[])Array.newInstance(clonable_worker.getClass(), N);// new IWorker[N];
		int from=0;
		int to=0;

		if(nSlices<numberOfProcessors){
			for(int i = slice_range.getSliceFrom()-1; i < slice_range.getSliceTo(); i++){
				from = i+1;
				to = (i+1);
			
				IWorker worker = (IWorker) clonable_worker.clone_worker();
				SliceRange range = new SliceRange(from, to);
				worker.setSliceRange(range);
		
				workers[i-slice_range.getSliceFrom()+1] = (T)worker;
			}
			return workers;
		}
		
		/*
		 * Add workers which process multiple frames
		 */
		for(int i = 0; i < Nmult; i++){
			from = i*slicesPerThread+1;
			to = (i+1)*slicesPerThread;
			SliceRange range = new SliceRange(from, to);
			IWorker worker = (IWorker) clonable_worker.clone_worker();
			worker.setSliceRange(range);
			workers[i] = (T)worker;
		}
		
		/*
		 * Add workers which process single frames
		 */
		int off = numberOfProcessors*slicesPerThread;
		for(int i = 0; i < (N-numberOfProcessors); i++){
			from =  off+i+1;
			to = off+i+1;
			SliceRange range = new SliceRange(from, to);
			IWorker worker = (IWorker) clonable_worker.clone_worker();
			worker.setSliceRange(range);
			workers[numberOfProcessors+i] = (T)worker;
		}
		
		return workers;
	}
	
	protected int getNumberSlicesPerThreads(int numberOfThreads, int numberOfSlices) {
		if(numberOfSlices<numberOfThreads){
			return 1;
		}
		
		return numberOfSlices/numberOfThreads;
	}
}
