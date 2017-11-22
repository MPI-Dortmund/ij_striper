package de.mpi_dortmund.ij.mpitools.FilamentEnhancer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import de.mpi_dortmund.ij.mpitools.helicalPicker.custom.WorkerArrayCreator;
import de.mpi_dortmund.ij.mpitools.helicalPicker.gui.SliceRange;
import ij.IJ;
import ij.ImageStack;
import ij.process.ImageProcessor;

public class FilamentEnhancer {
	ImageStack ips;
	FilamentEnhancerContext context;
	
	public FilamentEnhancer(ImageStack ips, FilamentEnhancerContext context) {
		this.ips = ips;
		this.context = context;
	}
	
	public ImageStack getEnhancedImages(SliceRange slice_range){
		int numberOfProcessors = Runtime.getRuntime().availableProcessors();
		FilamentEnhancerWorker[] workers = createWorkerArray(numberOfProcessors, slice_range);
		ExecutorService pool = Executors.newFixedThreadPool(numberOfProcessors);
		for (FilamentEnhancerWorker worker : workers) {
			pool.submit(worker);
		}
		pool.shutdown();
		
		try {
			
			pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		ImageStack enhanced = new ImageStack(ips.getWidth(), ips.getHeight());
		for (FilamentEnhancerWorker worker : workers) {
			SliceRange range = worker.getSliceRange();
			for(int i = range.getSliceFrom(); i <= range.getSliceTo(); i++){
				
				ImageProcessor map = worker.getMaps().get(i-range.getSliceFrom());
				enhanced.addSlice(map);
				//lines.add(worker.getLines().get(i-worker.getSliceFrom()));

				
			}
		}
		
		return enhanced;
	}
	
	
	protected FilamentEnhancerWorker[] createWorkerArray(int numberOfProcessors, SliceRange slice_range){

		WorkerArrayCreator creator = new WorkerArrayCreator();		
		FilamentEnhancerWorker worker = new FilamentEnhancerWorker(ips, context, slice_range);
		FilamentEnhancerWorker[] workers = creator.createWorkerArray(numberOfProcessors, slice_range, worker);
		
		return workers;
	}
	
	protected int getNumberSlicesPerThreads(int numberOfThreads, int numberOfSlices) {
		if(numberOfSlices<numberOfThreads){
			return 1;
		}
		
		return numberOfSlices/numberOfThreads;
	}

}
