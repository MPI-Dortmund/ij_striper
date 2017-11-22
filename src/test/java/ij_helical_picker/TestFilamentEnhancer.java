package ij_helical_picker;

import static org.junit.Assert.*;

import java.net.URL;

import org.junit.Test;

import de.mpi_dortmund.ij.mpitools.FilamentEnhancer.FilamentEnhancer;
import de.mpi_dortmund.ij.mpitools.FilamentEnhancer.FilamentEnhancerContext;
import de.mpi_dortmund.ij.mpitools.helicalPicker.gui.SliceRange;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

public class TestFilamentEnhancer {

	@Test
	public void testGetEnhancedImages_imagesEqual() {
		URL url = this.getClass().getClassLoader().getResource("enhanced_noequalize_stack_5_images_12_lines.tif");
		ImagePlus result = new ImagePlus(url.getPath());
		
		url = this.getClass().getClassLoader().getResource("stack_5_images_12_lines.tif");
		ImagePlus input = new ImagePlus(url.getPath());
		
		FilamentEnhancerContext context = new FilamentEnhancerContext();
		context.setFilamentWidth(10);
		context.setMaskWidth(60);
		context.setAngleStep(2);
		context.setEqualize(false);
	
		FilamentEnhancer enhancer = new FilamentEnhancer(input.getImageStack(), context);
		SliceRange range = new SliceRange(1, 5);
		ImageStack ips = enhancer.getEnhancedImages(range);
	
	
		assertTrue(TestUtils.isEquals(result.getStack(),ips));
	}
	
	
	@Test
	public void testGetEnhancedImages_imagesUncommenFormat() {
		
		ImageProcessor ip = new ByteProcessor(500, 511);
	
		ImagePlus input = new ImagePlus("", ip);
		
		FilamentEnhancerContext context = new FilamentEnhancerContext();
		context.setFilamentWidth(10);
		context.setMaskWidth(60);
		context.setAngleStep(2);
		context.setEqualize(false);
		
		FilamentEnhancer enhancer = new FilamentEnhancer(input.getImageStack(), context);
		SliceRange range = new SliceRange(1, input.getStackSize());
		ImageStack ips = enhancer.getEnhancedImages(range);
	
	}

}
