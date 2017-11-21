package ij_helical_picker;

import static org.junit.Assert.*;

import java.net.URL;

import org.junit.Test;

import de.mpi_dortmund.ij.mpitools.FilamentEnhancer.FilamentEnhancer;
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
		
		int filament_width = 10;
		int mask_width = 60;
		int angle_step = 2;
		boolean equalize = false;
		FilamentEnhancer enhancer = new FilamentEnhancer(input.getImageStack(), filament_width, mask_width, angle_step, equalize);
		ImageStack ips = enhancer.getEnhancedImages(1, 5);
		assertTrue(TestUtils.isEquals(result.getStack(),ips));
	}
	
	
	@Test
	public void testGetEnhancedImages_imagesUncommenFormat() {
		
		ImageProcessor ip = new ByteProcessor(500, 511);
	
		ImagePlus input = new ImagePlus("", ip);
		
		int filament_width = 10;
		int mask_width = 60;
		int angle_step = 2;
		boolean equalize = false;
		FilamentEnhancer enhancer = new FilamentEnhancer(input.getImageStack(), filament_width, mask_width, angle_step, equalize);
		ImageStack ips = enhancer.getEnhancedImages(1, input.getStackSize());
	
	}

}
