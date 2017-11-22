package ij_helical_picker;

import static org.junit.Assert.*;

import java.net.URL;
import java.util.ArrayList;

import org.junit.Test;

import de.mpi_dortmund.ij.mpitools.boxplacer.BoxPlacer_;
import de.mpi_dortmund.ij.mpitools.boxplacer.BoxPlacingContext;
import de.mpi_dortmund.ij.mpitools.boxplacer.Line;
import ij.ImagePlus;

public class TestBoxPlacer {

	@Test
	public void test() {
		
		URL url = this.getClass().getClassLoader().getResource("3lines_input.tif");
		ImagePlus target_image = new ImagePlus(url.getPath());
		
		url = this.getClass().getClassLoader().getResource("3lines.tif");
		ImagePlus line_image = new ImagePlus(url.getPath());
		
		BoxPlacer_ placer = new BoxPlacer_();

		BoxPlacingContext placing_context = new BoxPlacingContext();
		placing_context.setBoxDistance(5);
		placing_context.setBoxSize(10);
		placing_context.setPlacePoints(false);
		placing_context.setSlicePosition(1);

		ArrayList<Line> lines = placer.placeBoxes(line_image.getProcessor(), target_image, placing_context);
		assertEquals(lines.size(), 3);
		
	}

}
