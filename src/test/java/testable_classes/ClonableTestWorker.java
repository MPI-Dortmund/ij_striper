package testable_classes;

import de.mpi_dortmund.ij.mpitools.helicalPicker.custom.IWorker;

public class ClonableTestWorker implements IWorker {

	@Override
	public Object clone_worker() {
		// TODO Auto-generated method stub
		return new ClonableTestWorker();
	}

	@Override
	public void setSliceFrom(int i) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setSliceTo(int i) {
		// TODO Auto-generated method stub
		
	}

}
