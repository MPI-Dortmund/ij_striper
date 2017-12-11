package ij_helical_picker;

import static org.junit.Assert.*;

import java.net.URL;

import org.junit.Test;

import de.mpi_dortmund.ij.mpitools.FilamentEnhancer.FilamentEnhancer;
import de.mpi_dortmund.ij.mpitools.FilamentEnhancer.FilamentEnhancerContext;
import de.mpi_dortmund.ij.mpitools.RidgeDetectionOptimizer.Parallel_Ridge_Optimizer;
import de.mpi_dortmund.ij.mpitools.RidgeDetectionOptimizer.RidgeOptimizerWorker;
import de.mpi_dortmund.ij.mpitools.helicalPicker.FilamentDetector.DetectionThresholdRange;
import de.mpi_dortmund.ij.mpitools.helicalPicker.gui.SliceRange;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.blob.ManyBlobs;
import testable_classes.TestableParallelRidgeOptimizer;

public class TestParallelRidgeDetectionOptimizer {

	@Test
	public void test_getSubstack_checkNumberFrames() {
		URL url = this.getClass().getClassLoader().getResource("substack_with_3_selections.tif");
		ImagePlus img = new ImagePlus(url.getPath());
		TestableParallelRidgeOptimizer optimizer = new TestableParallelRidgeOptimizer();
		ImagePlus subStack = optimizer.getSubStack(img, new Integer[]{1,3});
		
		
		assertEquals(2, subStack.getStackSize());
	}
	
	@Test
	public void test_getSubstack_checkNumberSelections() {
		URL url = this.getClass().getClassLoader().getResource("substack_with_3_selections.tif");
		ImagePlus img = new ImagePlus(url.getPath());
		TestableParallelRidgeOptimizer optimizer = new TestableParallelRidgeOptimizer();
		ImagePlus subStack = optimizer.getSubStack(img, new Integer[]{1,3});
		
		
		assertEquals(3, subStack.getOverlay().size());
	}
	
	@Test
	public void test_getBinaryStack_checkNumberComponents(){
		URL url = this.getClass().getClassLoader().getResource("substack_with_3_selections.tif");
		ImagePlus img = new ImagePlus(url.getPath());
		TestableParallelRidgeOptimizer optimizer = new TestableParallelRidgeOptimizer();
		ImageStack bin_stack = optimizer.getBinaryStack(img);
		ImagePlus bin_imp = new ImagePlus("",bin_stack);
		
		ManyBlobs mb = new ManyBlobs(bin_imp);
		mb.setBackground(0);
		mb.findConnectedComponents();
		int firstFrame =  mb.size();
		bin_imp.setSlice(3);
		mb = new ManyBlobs(bin_imp);
		mb.setBackground(0);
		mb.findConnectedComponents();
		int thirdFrame =  mb.size();
		assertEquals(3, firstFrame+thirdFrame);
		
	}
	
	@Test
	public void test_Optimize() {
		URL url = this.getClass().getClassLoader().getResource("3lines_with_selection.tif");
		ImagePlus input = new ImagePlus(url.getPath());
		/*
		FilamentEnhancerContext enhancer_context = new FilamentEnhancerContext();
		enhancer_context.setAngleStep(2);
		enhancer_context.setEqualize(true);
		enhancer_context.setMaskWidth(20);
		enhancer_context.setFilamentWidth(10);
		
		FilamentEnhancer enhancer = new FilamentEnhancer(input.getImageStack(), enhancer_context);
		ImageStack enhanced = enhancer.getEnhancedImages(new SliceRange(1, 1));
		
		url = this.getClass().getClassLoader().getResource("3lines_ground_truth.tif");
		ImagePlus ground_truth = new ImagePlus(url.getPath());
		*/
		Parallel_Ridge_Optimizer optim = new Parallel_Ridge_Optimizer();
		int GLOBAL_RUNS = 40;
		int LOCAL_RUNS = 40;
		int mask_width = 20;
		int filament_width = 20;
		DetectionThresholdRange start_params = null;//new DetectionThresholdRange(0.0, 9.228515625);
		DetectionThresholdRange range = optim.optimize(input, start_params, filament_width, mask_width, GLOBAL_RUNS, LOCAL_RUNS,false);
		

		assertEquals(0.607, range.getLowerThreshold(),0.01);
		assertEquals(0.929, range.getUpperThreshold(),0.01);
		
	}

}
