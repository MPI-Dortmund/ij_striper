package ij_helical_picker;

import static org.junit.Assert.*;

import java.awt.HeadlessException;

import org.junit.Test;

import de.mpi_dortmund.ij.mpitools.RidgeDetectionOptimizer.RidgeDetectionOptimizerAssistant;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ByteProcessor;
import testable_classes.JFrameFake;

public class TestRidgeDetectionOptimizerAssistant {

	@Test
	public void test_builds_without_exception() {
		ImagePlus target_image = new ImagePlus("", new ByteProcessor(1024, 1024));
		RidgeDetectionOptimizerAssistant ass = new RidgeDetectionOptimizerAssistant(target_image);
		JFrameFake fake_frame = new JFrameFake();
		
		ass.setMainFrame(fake_frame);
		try{
		ass.showGUI();
		}catch (HeadlessException e) {
			// IGnore
		}
		
		
		assertTrue(fake_frame.isVisible());
	}
	
	@Test
	public void test_skip_through_without_exception() {
		ImagePlus target_image = new ImagePlus("", new ByteProcessor(1024, 1024));
		RidgeDetectionOptimizerAssistant ass = new RidgeDetectionOptimizerAssistant(target_image);
		JFrameFake fake_frame = new JFrameFake();
		
		ass.setMainFrame(fake_frame);
		try{
		ass.showGUI();
		}catch (HeadlessException e) {
			// IGnore
		}
		
		
		ass.getMeasureWidthPanelButtonNext().doClick();
		ass.getSelectFilamentsPanelButtonNext().doClick();
	}

}
