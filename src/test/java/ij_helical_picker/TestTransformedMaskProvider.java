package ij_helical_picker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.util.ArrayList;

import org.junit.Test;

import de.mpi_dortmund.ij.mpitools.FilamentEnhancer.TransformedMaskProvider;
import ij.ImagePlus;
import ij.process.FHT;

public class TestTransformedMaskProvider {

	@Test
	public void testGetTransformedMask_checkNumberOfMasks() {
		int mask_size = 256;
		int filament_width = 10;
		int mask_width = 60;
		int angle_step = 2;
		int type = 1;  
		TransformedMaskProvider provider = new TransformedMaskProvider();

		ArrayList<FHT> transformedMasks = provider.getTransformedMasks(mask_size, filament_width, mask_width, angle_step, type);
		
		assertEquals(180/angle_step, transformedMasks.size());
	}
	
	@Test
	public void testGetTransformedMask_checkPowerSpectrum_Frame1() {
		int mask_size = 256;
		int filament_width = 10;
		int mask_width = 60;
		int angle_step = 2;
		int type = 1;  
		TransformedMaskProvider provider = new TransformedMaskProvider();

		ArrayList<FHT> transformedMasks = provider.getTransformedMasks(mask_size, filament_width, mask_width, angle_step, type);
		
		URL url = this.getClass().getClassLoader().getResource("fft_mask_ms256_mw60_as2_frame1.tif");
		ImagePlus img = new ImagePlus(url.getPath());
		
		assertTrue(TestUtils.isEquals(img.getProcessor(), transformedMasks.get(0).getPowerSpectrum()));
	}
	
	@Test
	public void testGetTransformedMask_checkPowerSpectrum_Frame40() {
		int mask_size = 256;
		int filament_width = 10;
		int mask_width = 60;
		int angle_step = 2;
		int type = 1;  
		TransformedMaskProvider provider = new TransformedMaskProvider();

		ArrayList<FHT> transformedMasks = provider.getTransformedMasks(mask_size, filament_width, mask_width, angle_step, type);
		
		URL url = this.getClass().getClassLoader().getResource("fft_mask_ms256_mw60_as2_frame45.tif");
		ImagePlus img = new ImagePlus(url.getPath());
		
		assertTrue(TestUtils.isEquals(img.getProcessor(), transformedMasks.get(44).getPowerSpectrum()));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testGetTransformedMask_notPowerOf2Exception() {
		int mask_size = 200;
		int filament_width = 10;
		int mask_width = 60;
		int angle_step = 2;
		int type = 1;  
		TransformedMaskProvider provider = new TransformedMaskProvider();

		ArrayList<FHT> transformedMasks = provider.getTransformedMasks(mask_size, filament_width, mask_width, angle_step, type);
		
	}
	

}
