package ij_helical_picker;

import static org.junit.Assert.*;

import java.net.URL;

import org.junit.Test;

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

}
