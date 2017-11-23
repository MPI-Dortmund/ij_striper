package ij_helical_picker;

import static org.junit.Assert.assertEquals;

import java.awt.Polygon;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Test;

import de.mpi_dortmund.ij.mpitools.FilamentEnhancer.FilamentEnhancerContext;
import de.mpi_dortmund.ij.mpitools.helicalPicker.FilamentDetector.DetectionThresholdRange;
import de.mpi_dortmund.ij.mpitools.helicalPicker.gui.PipelineRunner;
import de.mpi_dortmund.ij.mpitools.helicalPicker.gui.SliceRange;
import de.mpi_dortmund.ij.mpitools.skeletonfilter.SkeletonFilterContext;
import ij.ImagePlus;

public class TestPipelineRunner {

	@Test
	public void test() {
		URL url = this.getClass().getClassLoader().getResource("smallimage_2lines_w10_h100.tif");
		ImagePlus img = new ImagePlus(url.getPath());
		double overlapping_factor = 0.5;
		int box_size = 10;
		PipelineRunner run = new PipelineRunner();
		SliceRange slice_range = new SliceRange(1, 1);
		SkeletonFilterContext skeleton_filter_context = new SkeletonFilterContext();
		skeleton_filter_context.setBorderDiameter(box_size/2);
		skeleton_filter_context.setMinFilamentDistance(1);
		FilamentEnhancerContext enhancer_context = new FilamentEnhancerContext();
		enhancer_context.setEqualize(true);
		enhancer_context.setFilamentWidth(10);
		enhancer_context.setMaskWidth(10);
		enhancer_context.setAngleStep(2);
		DetectionThresholdRange thresh_range = new DetectionThresholdRange(4, 5);
		

		run.run(img,slice_range, skeleton_filter_context, thresh_range, enhancer_context);
		HashMap<Integer, ArrayList<Polygon>> filtered_lines_map=  run.getFilteredLines();
		ArrayList<Polygon> lines = filtered_lines_map.get(1);
		assertEquals(2, lines.size());
	}

}
