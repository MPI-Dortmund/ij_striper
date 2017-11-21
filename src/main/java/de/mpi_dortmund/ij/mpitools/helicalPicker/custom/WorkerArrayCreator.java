package de.mpi_dortmund.ij.mpitools.helicalPicker.custom;

import java.lang.reflect.Array;

import de.mpi_dortmund.ij.mpitools.helicalPicker.FilamentDetector.FilamentDetectorWorker;

public class WorkerArrayCreator {
	
	public <T> T[] createWorkerArray(int numberOfProcessors, int sliceFrom, int sliceTo, IWorker clonable_worker){
		int nSlices = sliceTo-sliceFrom+1;
		int slicesPerThread = getNumberSlicesPerThreads(numberOfProcessors, nSlices);
		int Nmult = numberOfProcessors;
		int N = numberOfProcessors+(nSlices-numberOfProcessors*slicesPerThread);

		
		T[] workers =  (T[])Array.newInstance(clonable_worker.getClass(), N);// new IWorker[N];
		int from=0;
		int to=0;

		if(nSlices<numberOfProcessors){
			for(int i = sliceFrom-1; i < sliceTo; i++){
				from = i+1;
				to = (i+1);				
				IWorker worker = (IWorker) clonable_worker.clone_worker();
				worker.setSliceFrom(from);
				worker.setSliceTo(to);
				workers[i-sliceFrom+1] = (T)worker;
			}
			return workers;
		}
		
		/*
		 * Add workers which process multiple frames
		 */
		for(int i = 0; i < Nmult; i++){
			from = i*slicesPerThread+1;
			to = (i+1)*slicesPerThread;
			IWorker worker = (IWorker) clonable_worker.clone_worker();
			worker.setSliceFrom(from);
			worker.setSliceTo(to);
			workers[i] = (T)worker;
		}
		
		/*
		 * Add workers which process single frames
		 */
		int off = numberOfProcessors*slicesPerThread;
		for(int i = 0; i < (N-numberOfProcessors); i++){
			from =  off+i+1;
			to = off+i+1;
			IWorker worker = (IWorker) clonable_worker.clone_worker();
			worker.setSliceFrom(from);
			worker.setSliceTo(to);
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
