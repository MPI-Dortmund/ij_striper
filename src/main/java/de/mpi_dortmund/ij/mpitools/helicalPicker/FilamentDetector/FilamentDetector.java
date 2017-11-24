package de.mpi_dortmund.ij.mpitools.helicalPicker.FilamentDetector;

import java.awt.Polygon;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import de.mpi_dortmund.ij.mpitools.helicalPicker.custom.WorkerArrayCreator;
import de.mpi_dortmund.ij.mpitools.helicalPicker.gui.SliceRange;
import ij.IJ;
import ij.ImageStack;

public class FilamentDetector {
	
	ImageStack ips;
	FilamentDetectorContext context;
	int mask_width;
	
	/**
	 * 
	 * @param ips Enhanced filament stack
	 * @param sigma parameter for detection. is proportional to the width
	 * @param lower_threshold lower detection threshold
	 * @param upper_threshold upper detection threshold
	 * @param equalize helps to have the same signal for filaments with different contrast
	 */
	public FilamentDetector(ImageStack ips, FilamentDetectorContext context) {
		this.ips = ips;
		this.context = context;
	}
	
	public HashMap<Integer, ArrayList<Polygon>> getFilaments(SliceRange slice_range){
		int numberOfProcessors = Runtime.getRuntime().availableProcessors();
		FilamentDetectorWorker[] workers = createWorkerArray(numberOfProcessors, slice_range);
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
			SliceRange range = worker.getSliceRange();
			for(int i = range.getSliceFrom(); i <= range.getSliceTo(); i++){
				lines.put(i, worker.getLines().get(i-range.getSliceFrom()));			
			}
		}
		return lines;
	}
	
	protected FilamentDetectorWorker[] createWorkerArray(int numberOfProcessors, SliceRange slice_range){
		WorkerArrayCreator creator = new WorkerArrayCreator();
		FilamentDetectorWorker worker = new FilamentDetectorWorker(ips, slice_range, context);
		FilamentDetectorWorker[] workers = creator.createWorkerArray(numberOfProcessors, slice_range, worker);
	
		return workers;
		
	}
	
	protected int getNumberSlicesPerThreads(int numberOfThreads, int numberOfSlices) {
		if(numberOfSlices<numberOfThreads){
			return 1;
		}
		
		return numberOfSlices/numberOfThreads;
	}

}
