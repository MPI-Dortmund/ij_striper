package ij_helical_picker;

import static org.junit.Assert.*;

import java.awt.Polygon;
import java.net.URL;
import java.util.ArrayList;

import org.junit.Test;

import de.mpi_dortmund.ij.mpitools.skeletonfilter.SkeletonFilter_;
import de.mpi_dortmund.ij.mpitools.userfilter.IUserFilter;
import ij.ImagePlus;
import ij.process.ImageProcessor;

public class TestSkeletonFilter {
	
	ImagePlus input;
	ImagePlus lines;
	ImagePlus response;
	ImagePlus mask;
	
	public TestSkeletonFilter() {
		//3 lines
		/* Line		 1		 2		 3
		 * Length 	200		150		100
		 * Response	150		100		50
		 * Distance     ~96     ~90
		 */
		URL url = this.getClass().getClassLoader().getResource("3lines_input.tif");
		input = new ImagePlus(url.getPath());
		
		url = this.getClass().getClassLoader().getResource("3lines.tif");
		lines = new ImagePlus(url.getPath());
		
		url = this.getClass().getClassLoader().getResource("3lines_response.tif");
		response = new ImagePlus(url.getPath());
		
		url = this.getClass().getClassLoader().getResource("3lines_mask.tif");
		mask = new ImagePlus(url.getPath());
	}
	
	@Test
	public void test_filterLineImage_noLinesFiltered() {
		//3 lines
		/* Line		 1		 2		 3
		 * Length 	200		150		100
		 * Response	150		100		50
		 * Distance     ~96     ~90
		 */
		
		int min_length = 90;
		double min_straightness = 0;
		int window_straightness = 10;
		int radius = 10;
		int border_diameter = 5;
		double sigma_min_reponse = 5;
		double sigma_max_response = 5;
		boolean fitDistr = false;
		int min_distance = 80;
		double double_filament_insensitivity = 1;
		ArrayList<IUserFilter> userFilters = null;
		SkeletonFilter_ filter = new SkeletonFilter_(min_length, min_straightness, window_straightness, radius, 
				border_diameter, sigma_min_reponse, sigma_max_response, fitDistr, min_distance, double_filament_insensitivity, userFilters);
		
		ArrayList<Polygon> filteredLines = filter.filterLineImage(lines.getProcessor(), input.getProcessor(), response.getProcessor(), null);
		
		assertEquals(3, filteredLines.size());
	}
	
	@Test
	public void test_filterLineImage_onlyFirstLine_ByLength() {
		//3 lines
		/* Line		 1		 2		 3
		 * Length 	200		150		100
		 * Response	150		100		50
		 * Distance     ~96     ~90
		 */
		
		int min_length = 151;
		double min_straightness = 0;
		int window_straightness = 10;
		int radius = 10;
		int border_diameter = 5;
		double sigma_min_reponse = 5;
		double sigma_max_response = 5;
		boolean fitDistr = false;
		int min_distance = 80;
		double double_filament_insensitivity = 1;
		ArrayList<IUserFilter> userFilters = null;
		SkeletonFilter_ filter = new SkeletonFilter_(min_length, min_straightness, window_straightness, radius, 
				border_diameter, sigma_min_reponse, sigma_max_response, fitDistr, min_distance, double_filament_insensitivity, userFilters);
		
		ArrayList<Polygon> filteredLines = filter.filterLineImage(lines.getProcessor(), input.getProcessor(), response.getProcessor(), null);
		
		assertEquals(1, filteredLines.size());
		assertEquals(200, filteredLines.get(0).npoints,1);
	}
	
	@Test
	public void test_filterLineImage_onlySecondLine_ByResponse() {
		//3 lines
		/* Line		 1		 2		 3
		 * Length 	200		150		100
		 * Response	150		100		50
		 * Distance     ~96     ~90
		 */
		/*
		 * Known mean:99.36547672756798
		 * Known SD: 41.65841610647696
		 */
		int min_length = 90;
		double min_straightness = 0;
		int window_straightness = 10;
		int radius = 10;
		int border_diameter = 5;
		double sigma_min_reponse = 1;
		double sigma_max_response = 1;
		boolean fitDistr = false;
		int min_distance = 80;
		double double_filament_insensitivity = 0.3;
		ArrayList<IUserFilter> userFilters = null;
		SkeletonFilter_ filter = new SkeletonFilter_(min_length, min_straightness, window_straightness, radius, 
				border_diameter, sigma_min_reponse, sigma_max_response, fitDistr, min_distance, double_filament_insensitivity, userFilters);
		
		ArrayList<Polygon> filteredLines = filter.filterLineImage(lines.getProcessor(), input.getProcessor(), response.getProcessor(), null);
		
		assertEquals(1, filteredLines.size());
		assertEquals(150, filteredLines.get(0).npoints,1);
	}
	
	@Test
	public void test_filterLineImage_removeSecondLine_ByMask() {
		//3 lines
		/* Line		 1		 2		 3
		 * Length 	200		150		100
		 * Response	150		100		50
		 * Distance     ~96     ~90
		 */
		
		int min_length = 90;
		double min_straightness = 0;
		int window_straightness = 10;
		int radius = 10;
		int border_diameter = 5;
		double sigma_min_reponse = 5;
		double sigma_max_response = 5;
		boolean fitDistr = false;
		int min_distance = 80;
		double double_filament_insensitivity = 1;
		ArrayList<IUserFilter> userFilters = null;
		ImageProcessor mask = this.mask.getProcessor();
		SkeletonFilter_ filter = new SkeletonFilter_(min_length, min_straightness, window_straightness, radius, 
				border_diameter, sigma_min_reponse, sigma_max_response, fitDistr, min_distance, double_filament_insensitivity, userFilters);
		
		ArrayList<Polygon> filteredLines = filter.filterLineImage(lines.getProcessor(), input.getProcessor(), response.getProcessor(), mask);
		
		assertEquals(2, filteredLines.size());
	}

}
