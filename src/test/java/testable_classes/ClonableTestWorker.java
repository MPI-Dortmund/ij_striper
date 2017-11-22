package testable_classes;

import de.mpi_dortmund.ij.mpitools.helicalPicker.custom.IWorker;
import de.mpi_dortmund.ij.mpitools.helicalPicker.gui.SliceRange;

public class ClonableTestWorker implements IWorker {

	@Override
	public Object clone_worker() {
		// TODO Auto-generated method stub
		return new ClonableTestWorker();
	}

	@Override
	public void setSliceRange(SliceRange slice_range) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public SliceRange getSliceRange() {
		// TODO Auto-generated method stub
		return new SliceRange(1, 1);
	}

	

}
