package de.mpi_dortmund.ij.mpitools.FilamentEnhancer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import de.mpi_dortmund.ij.mpitools.helicalPicker.custom.WorkerArrayCreator;
import ij.IJ;
import ij.ImageStack;
import ij.process.ImageProcessor;

public class FilamentEnhancer {
	ImageStack ips;
	int filament_width;
	int mask_width;
	int angle_step;
	boolean equalize;
	
	public FilamentEnhancer(ImageStack ips, int filament_width, int mask_width, int angle_step, boolean equalize) {
		this.ips = ips;
		this.filament_width = filament_width;
		this.mask_width = mask_width;
		this.angle_step = angle_step;
		this.equalize = equalize;
	}
	
	public ImageStack getEnhancedImages(int sliceFrom, int sliceTo){
		int numberOfProcessors = Runtime.getRuntime().availableProcessors();
		FilamentEnhancerWorker[] workers = createWorkerArray(numberOfProcessors, sliceFrom, sliceTo);

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
			for(int i = worker.getSliceFrom(); i <= worker.getSliceTo(); i++){
				ImageProcessor map = worker.getMaps().get(i-worker.getSliceFrom());
				enhanced.addSlice(map);
				//lines.add(worker.getLines().get(i-worker.getSliceFrom()));

				
			}
		}
		
		return enhanced;
	}
	
	
	protected FilamentEnhancerWorker[] createWorkerArray(int numberOfProcessors, int sliceFrom, int sliceTo){

		WorkerArrayCreator creator = new WorkerArrayCreator();		
		FilamentEnhancerWorker worker = new FilamentEnhancerWorker(ips, filament_width, mask_width, angle_step, equalize, sliceFrom, sliceTo);
		FilamentEnhancerWorker[] workers = creator.createWorkerArray(numberOfProcessors, sliceFrom, sliceTo, worker);
		
		
		return workers;
	}
	
	protected int getNumberSlicesPerThreads(int numberOfThreads, int numberOfSlices) {
		if(numberOfSlices<numberOfThreads){
			return 1;
		}
		
		return numberOfSlices/numberOfThreads;
	}

}
