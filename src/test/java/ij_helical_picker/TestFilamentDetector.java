package ij_helical_picker;

import static org.junit.Assert.*;

import java.awt.Polygon;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.junit.Test;

import de.mpi_dortmund.ij.mpitools.helicalPicker.FilamentDetector.FilamentDetector;
import ij.ImagePlus;
import ij.ImageStack;

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
		boolean equalize = false;
	
		FilamentDetector detector = new FilamentDetector(img.getImageStack(), sigma, lower_threshold, upper_threshold);
		HashMap<Integer,ArrayList<Polygon>> lines = detector.getFilaments(1, 5);
		
		Iterator<Integer> slice_iterator = lines.keySet().iterator();
		int N = 0;
		while(slice_iterator.hasNext()){
			int slice_pos = slice_iterator.next();
			N += lines.get(slice_pos).size();
		}

		assertEquals(12, N);
	}

}
