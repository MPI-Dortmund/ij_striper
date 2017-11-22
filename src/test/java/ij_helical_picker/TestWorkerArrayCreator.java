package ij_helical_picker;

import static org.junit.Assert.*;

import org.junit.Test;

import de.mpi_dortmund.ij.mpitools.FilamentEnhancer.FilamentEnhancerWorker;
import de.mpi_dortmund.ij.mpitools.helicalPicker.FilamentDetector.FilamentDetectorWorker;
import de.mpi_dortmund.ij.mpitools.helicalPicker.custom.WorkerArrayCreator;
import de.mpi_dortmund.ij.mpitools.helicalPicker.gui.SliceRange;
import testable_classes.ClonableTestWorker;

public class TestWorkerArrayCreator {

	@Test
	public void testCreateWorkerArray() {
		WorkerArrayCreator creator = new WorkerArrayCreator();
		ClonableTestWorker clonable_worker = new ClonableTestWorker();
		int number_processors = 24;
		int slice_from = 1;
		int slice_to = 25;
		SliceRange slice_range = new SliceRange(slice_from, slice_to);
		ClonableTestWorker[] workers = creator.createWorkerArray(number_processors,slice_range, clonable_worker);
		
		assertEquals(25, workers.length);
		
	}

}
