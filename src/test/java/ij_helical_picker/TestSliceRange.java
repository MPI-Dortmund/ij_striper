package ij_helical_picker;

import static org.junit.Assert.*;

import org.junit.Test;

import de.mpi_dortmund.ij.mpitools.helicalPicker.gui.SliceRange;

public class TestSliceRange {

	@Test
	public void testSetterGetter() {
		SliceRange range = new SliceRange(5, 10);
		
		assertEquals(5, range.getSliceFrom());
		assertEquals(10, range.getSliceTo());
	}

}
