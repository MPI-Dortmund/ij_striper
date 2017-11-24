package ij_helical_picker;

import static org.junit.Assert.assertEquals;

import java.awt.Polygon;
import java.net.URL;
import java.util.ArrayList;

import org.junit.Test;

import de.mpi_dortmund.ij.mpitools.helicalPicker.filamentFilter.FilamentFilterContext;
import de.mpi_dortmund.ij.mpitools.helicalPicker.filamentFilter.FilamentFilter_;
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
		
		FilamentFilterContext context = new FilamentFilterContext();
		context.setMinimumFilamentLength(90);
		context.setMinimumLineStraightness(0);
		context.setWindowWidthStraightness(10);
		context.setRemovementRadius(10);
		context.setBorderDiameter(5);
		context.setSigmaMinResponse(5);
		context.setSigmaMaxResponse(5);
		context.setFitDistribution(false);
		context.setMinFilamentDistance(80);
		context.setDoubleFilamentInsensitivity(1);
		context.setUserFilters(null);

		FilamentFilter_ filter = new FilamentFilter_();
		
		ArrayList<Polygon> filteredLines = filter.filterLineImage(lines.getProcessor(), input.getProcessor(), response.getProcessor(), null,context);
		
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
		
		
		FilamentFilterContext context = new FilamentFilterContext();
		context.setMinimumFilamentLength(151);
		context.setMinimumLineStraightness(0);
		context.setWindowWidthStraightness(10);
		context.setRemovementRadius(10);
		context.setBorderDiameter(5);
		context.setSigmaMinResponse(5);
		context.setSigmaMaxResponse(5);
		context.setFitDistribution(false);
		context.setMinFilamentDistance(80);
		context.setDoubleFilamentInsensitivity(1);
		context.setUserFilters(null);

		FilamentFilter_ filter = new FilamentFilter_();
		
		ArrayList<Polygon> filteredLines = filter.filterLineImage(lines.getProcessor(), input.getProcessor(), response.getProcessor(), null,context);
		
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
		
		FilamentFilterContext context = new FilamentFilterContext();
		context.setMinimumFilamentLength(90);
		context.setMinimumLineStraightness(0);
		context.setWindowWidthStraightness(10);
		context.setRemovementRadius(10);
		context.setBorderDiameter(5);
		context.setSigmaMinResponse(1);
		context.setSigmaMaxResponse(1);
		context.setFitDistribution(false);
		context.setMinFilamentDistance(80);
		context.setDoubleFilamentInsensitivity(0.3);
		context.setUserFilters(null);

		FilamentFilter_ filter = new FilamentFilter_();
		
		ArrayList<Polygon> filteredLines = filter.filterLineImage(lines.getProcessor(), input.getProcessor(), response.getProcessor(), null, context);
		
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
		
		FilamentFilterContext context = new FilamentFilterContext();
		context.setMinimumFilamentLength(90);
		context.setMinimumLineStraightness(0);
		context.setWindowWidthStraightness(10);
		context.setRemovementRadius(10);
		context.setBorderDiameter(5);
		context.setSigmaMinResponse(5);
		context.setSigmaMaxResponse(5);
		context.setFitDistribution(false);
		context.setMinFilamentDistance(80);
		context.setDoubleFilamentInsensitivity(1);
		context.setUserFilters(null);
	
		ImageProcessor mask = this.mask.getProcessor();
		FilamentFilter_ filter = new FilamentFilter_();
		
		ArrayList<Polygon> filteredLines = filter.filterLineImage(lines.getProcessor(), input.getProcessor(), response.getProcessor(), mask, context);
		
		assertEquals(2, filteredLines.size());
	}

}
