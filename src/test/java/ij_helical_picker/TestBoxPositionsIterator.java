package ij_helical_picker;

import static org.junit.Assert.*;

import java.awt.Polygon;
import java.net.URL;
import java.util.ArrayList;

import org.junit.Test;

import de.mpi_dortmund.ij.mpitools.boxplacer.BoxPositionIterator;
import de.mpi_dortmund.ij.mpitools.helicalPicker.filamentFilter.LineTracer;
import ij.ImagePlus;
import ij.process.ByteProcessor;

public class TestBoxPositionsIterator {

	@Test
	public void check_number_of_boxes() {
		URL url = this.getClass().getClassLoader().getResource("line_length_134.tif");
		ImagePlus img = new ImagePlus(url.getPath());
		
		LineTracer tracer = new LineTracer();
		ArrayList<Polygon> pol = tracer.extractLines((ByteProcessor)img.getProcessor());
		
		int box_size = 64;
		int box_dist = 10;
		BoxPositionIterator it = new BoxPositionIterator(pol.get(0), box_size,box_dist,false);
		int N =0;
		while(it.hasNext()){
			it.next();
			N++;
		}
		
		assertEquals(7, N, 0);
	}

}
