package ij_helical_picker;

import static org.junit.Assert.assertEquals;

import java.awt.Polygon;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.junit.Test;

import de.mpi_dortmund.ij.mpitools.helicalPicker.FilamentDetector.DetectionThresholdRange;
import de.mpi_dortmund.ij.mpitools.helicalPicker.FilamentDetector.FilamentDetector;
import de.mpi_dortmund.ij.mpitools.helicalPicker.FilamentDetector.FilamentDetectorContext;
import de.mpi_dortmund.ij.mpitools.helicalPicker.gui.SliceRange;
import ij.ImagePlus;

public class TestFilamentDetector {

	@Test
	public void testGetFilaments_NumberOfLines_Expected12() {
		/*
		 * Manual optimized settings:
		 * Sigma: 3.39
		 * LT: 0.68
		 * UT: 1.36
		 */
		URL url = this.getClass().getClassLoader().getResource("enhanced_noequalize_stack_5_images_12_lines.tif");
		ImagePlus img = new ImagePlus(url.getPath());
		
		double sigma = 3.39;
		double lower_threshold = 0.68;
		double upper_threshold = 1.36;

		DetectionThresholdRange thresh_range = new DetectionThresholdRange(lower_threshold, upper_threshold);
		FilamentDetectorContext context = new FilamentDetectorContext();
		context.setThresholdRange(thresh_range);
		context.setSigma(sigma);
		FilamentDetector detector = new FilamentDetector(img.getImageStack(), context);
		SliceRange range = new SliceRange(1, 5);
		HashMap<Integer,ArrayList<Polygon>> lines = detector.getFilaments(range);
		
		Iterator<Integer> slice_iterator = lines.keySet().iterator();
		int N = 0;
		while(slice_iterator.hasNext()){
			int slice_pos = slice_iterator.next();
			N += lines.get(slice_pos).size();
		}

		assertEquals(12, N);
	}
	
	
	@Test
	public void testGetFilaments_Only5thSlice_NumberOfLines_Expected2() {
		/*
		 * Manual optimized settings:
		 * Sigma: 3.39
		 * LT: 0.68
		 * UT: 1.36
		 */
		URL url = this.getClass().getClassLoader().getResource("enhanced_noequalize_stack_5_images_12_lines.tif");
		ImagePlus img = new ImagePlus(url.getPath());
		
		double sigma = 3.39;
		double lower_threshold = 0.68;
		double upper_threshold = 1.36;

		DetectionThresholdRange thresh_range = new DetectionThresholdRange(lower_threshold, upper_threshold);
		FilamentDetectorContext context = new FilamentDetectorContext();
		context.setThresholdRange(thresh_range);
		context.setSigma(sigma);
		
		FilamentDetector detector = new FilamentDetector(img.getImageStack(), context);
		SliceRange range = new SliceRange(5, 5);
		HashMap<Integer,ArrayList<Polygon>> lines = detector.getFilaments(range);
		
		Iterator<Integer> slice_iterator = lines.keySet().iterator();
		int N = 0;
		while(slice_iterator.hasNext()){
			int slice_pos = slice_iterator.next();
			N += lines.get(slice_pos).size();
		}

		assertEquals(2, N);
	}

}
