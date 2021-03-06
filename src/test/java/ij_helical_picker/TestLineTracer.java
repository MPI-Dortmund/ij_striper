package ij_helical_picker;

import static org.junit.Assert.assertEquals;

import java.awt.Polygon;
import java.net.URL;
import java.util.ArrayList;

import org.junit.Test;

import de.mpi_dortmund.ij.mpitools.helicalPicker.filamentFilter.LineTracer;
import ij.ImagePlus;
import ij.process.ByteProcessor;

public class TestLineTracer {

	@Test
	public void testNumberOfLines() {
		URL url = this.getClass().getClassLoader().getResource("line.tif");
		ImagePlus img = new ImagePlus(url.getPath());
		LineTracer tracer = new LineTracer();
		ArrayList<Polygon> lines = tracer.extractLines((ByteProcessor) img.getProcessor());
	
		assertEquals(1, lines.size());
	}
	
	@Test
	public void testNumberOfLines2() {
		URL url = this.getClass().getClassLoader().getResource("line_which_should_not_be_removed.tif");
		ImagePlus img = new ImagePlus(url.getPath());
		LineTracer tracer = new LineTracer();
		ArrayList<Polygon> lines = tracer.extractLines((ByteProcessor) img.getProcessor());
	
		assertEquals(1, lines.size());
	}
	
	@Test
	public void testNumberOfLines3() {
		URL url = this.getClass().getClassLoader().getResource("triangle.tif");
		ImagePlus img = new ImagePlus(url.getPath());
		LineTracer tracer = new LineTracer();
		ArrayList<Polygon> lines = tracer.extractLines((ByteProcessor) img.getProcessor());
	
		assertEquals(1, lines.size());
	}
	
	@Test
	public void testLengthOfLines() {
		URL url = this.getClass().getClassLoader().getResource("line.tif");
		ImagePlus img = new ImagePlus(url.getPath());
		LineTracer tracer = new LineTracer();
		ArrayList<Polygon> lines = tracer.extractLines((ByteProcessor) img.getProcessor());
		
		assertEquals(201, lines.get(0).npoints);
	}

}
