package testable_classes;

import de.mpi_dortmund.ij.mpitools.RidgeDetectionOptimizer.Parallel_Ridge_Optimizer;
import ij.ImagePlus;
import ij.ImageStack;

public class TestableParallelRidgeOptimizer extends Parallel_Ridge_Optimizer {
	
	
	@Override
	public ImagePlus getSubStack(ImagePlus imp, Integer[] slicesWithSelection) {
		// TODO Auto-generated method stub
		return super.getSubStack(imp, slicesWithSelection);
	}
	
	@Override
	public ImageStack getBinaryStack(ImagePlus imp) {
		// TODO Auto-generated method stub
		return super.getBinaryStack(imp);
	}

}
