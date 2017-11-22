package ij_helical_picker;

import static org.junit.Assert.*;

import java.net.URL;
import java.util.ArrayList;

import org.junit.Test;

import de.mpi_dortmund.ij.mpitools.boxplacer.BoxPlacer_;
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
		int slice_position = 1;
		int box_size = 10;
		int box_distance = 5;
		boolean place_points = false;
		ArrayList<Line> lines = placer.placeBoxes(line_image.getProcessor(), target_image, slice_position, box_size, box_distance, place_points);
		
		assertEquals(lines.size(), 3);
		
	}

}
