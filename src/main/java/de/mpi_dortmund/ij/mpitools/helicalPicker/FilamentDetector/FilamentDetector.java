package de.mpi_dortmund.ij.mpitools.helicalPicker.FilamentDetector;

import java.awt.Polygon;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import de.mpi_dortmund.ij.mpitools.helicalPicker.custom.WorkerArrayCreator;
import ij.ImageStack;

public class FilamentDetector {
	
	ImageStack ips;
	double sigma; 
	double lower_threshold; 
	double upper_threshold;
	int mask_width;
	
	/**
	 * 
	 * @param ips Enhanced filament stack
	 * @param sigma parameter for detection. is proportional to the width
	 * @param lower_threshold lower detection threshold
	 * @param upper_threshold upper detection threshold
	 * @param equalize helps to have the same signal for filaments with different contrast
	 */
	public FilamentDetector(ImageStack ips, double sigma, double lower_threshold, double upper_threshold) {
		this.ips = ips;
		this.sigma = sigma;
		this.lower_threshold = lower_threshold;
		this.upper_threshold = upper_threshold;
	}
	
	public HashMap<Integer, ArrayList<Polygon>> getFilaments( int sliceFrom, int sliceTo){
		int numberOfProcessors = Runtime.getRuntime().availableProcessors();
		FilamentDetectorWorker[] workers = createWorkerArray(numberOfProcessors, sliceFrom, sliceTo);
		ExecutorService pool = Executors.newFixedThreadPool(numberOfProcessors);
		for (FilamentDetectorWorker worker : workers) {
			pool.submit(worker);
		}
		pool.shutdown();
		
		try {
			
			pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		HashMap<Integer, ArrayList<Polygon>> lines = new HashMap<Integer, ArrayList<Polygon>>();
		for (IFilamentDetectorWorker worker : workers) {
			for(int i = worker.getSliceFrom(); i <= worker.getSliceTo(); i++){
				lines.put(i, worker.getLines().get(i-worker.getSliceFrom()));			
			}
		}
		return lines;
	}
	
	protected FilamentDetectorWorker[] createWorkerArray(int numberOfProcessors, int sliceFrom, int sliceTo){
		WorkerArrayCreator creator = new WorkerArrayCreator();
		FilamentDetectorWorker worker = new FilamentDetectorWorker(ips, sliceFrom, sliceTo, sigma, lower_threshold, upper_threshold);
		FilamentDetectorWorker[] workers = creator.createWorkerArray(numberOfProcessors, sliceFrom, sliceTo, worker);
	
		return workers;
		
	}
	
	protected int getNumberSlicesPerThreads(int numberOfThreads, int numberOfSlices) {
		if(numberOfSlices<numberOfThreads){
			return 1;
		}
		
		return numberOfSlices/numberOfThreads;
	}

}
