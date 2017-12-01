package ij_helical_picker;

import static org.junit.Assert.*;

import java.awt.HeadlessException;

import org.junit.Test;

import de.mpi_dortmund.ij.mpitools.FilamentEnhancer.FilamentEnhancerContext;
import de.mpi_dortmund.ij.mpitools.boxplacer.BoxPlacingContext;
import de.mpi_dortmund.ij.mpitools.helicalPicker.FilamentDetector.DetectionThresholdRange;
import de.mpi_dortmund.ij.mpitools.helicalPicker.FilamentDetector.FilamentDetectorContext;
import de.mpi_dortmund.ij.mpitools.helicalPicker.filamentFilter.FilamentFilterContext;
import de.mpi_dortmund.ij.mpitools.helicalPicker.gui.HelicalPickerGUI;
import testable_classes.JFrameFake;

public class TestHelicalPickerGUI {

	@Test
	public void builds_without_exception() {
		HelicalPickerGUI gui = new HelicalPickerGUI();
		JFrameFake frm = new JFrameFake();
		gui.setMainFrame(frm);
		try{
		gui.createAndShowGUI();
		gui.toggleAdvanced();
		}catch (HeadlessException e) {
			// IGnore
		}
		
		assertEquals(true, frm.isVisible());
	}
	
	@Test
	public void test_update_parameters() {
		HelicalPickerGUI gui = new HelicalPickerGUI();
		JFrameFake frm = new JFrameFake();
		gui.setMainFrame(frm);
		try{
		gui.createAndShowGUI();
		gui.toggleAdvanced();
		
		
		}catch (HeadlessException e) {
			// IGnore
		}
		int lower = 5;
		int upper = 10 ;
		DetectionThresholdRange range = new DetectionThresholdRange(lower, upper);
		gui.updateDetectionParameters(range);
		FilamentDetectorContext context = gui.getFilamentDetectorContext();
		
		assertEquals(lower, context.getThresholdRange().getLowerThreshold(),0);
		assertEquals(upper, context.getThresholdRange().getUpperThreshold(),0);
	}
	
	
	@Test
	public void test_get_box_placing_context() {
		HelicalPickerGUI gui = new HelicalPickerGUI();
		JFrameFake frm = new JFrameFake();
		gui.setMainFrame(frm);
		try{
		gui.createAndShowGUI();
		gui.toggleAdvanced();
		
		
		}catch (HeadlessException e) {
			// IGnore
		}
		
		BoxPlacingContext context = gui.getBoxPlacingContext();

		assertEquals(HelicalPickerGUI.DEFAULT_BOX_DISTANCE, context.getBoxDistance(),0);
		assertEquals(HelicalPickerGUI.DEFAULT_BOX_SIZE, context.getBoxSize(),0);
	}
	
	
	@Test
	public void test_getFilamentEnhancerContext() {
		HelicalPickerGUI gui = new HelicalPickerGUI();
		JFrameFake frm = new JFrameFake();
		gui.setMainFrame(frm);
		try{
		gui.createAndShowGUI();
		gui.toggleAdvanced();
		
		
		}catch (HeadlessException e) {
			// IGnore
		}
		
		FilamentEnhancerContext context = gui.getFilamentEnhancerContext();

		assertEquals(HelicalPickerGUI.DEFAULT_FILAMENT_WIDTH, context.getFilamentWidth(),0);
		assertEquals(HelicalPickerGUI.DEFAULT_MASK_WIDTH, context.getMaskWidth(),0);
	}
	
	
	@Test
	public void test_getFilamentFilterContext() {
		HelicalPickerGUI gui = new HelicalPickerGUI();
		JFrameFake frm = new JFrameFake();
		gui.setMainFrame(frm);
		try{
		gui.createAndShowGUI();
		gui.toggleAdvanced();
		
		
		}catch (HeadlessException e) {
			// IGnore
		}
		
		FilamentFilterContext context = gui.getLineFilterContext();

		assertEquals(1-HelicalPickerGUI.DEFAULT_MIN_SENSITIVITY, context.getDoubleFilamentInsensitivity(),0);
		assertEquals(HelicalPickerGUI.DEFAULT_SIGMA_MAX_RESPONSE, context.getSigmaMaxResponse(),0);
		assertEquals(HelicalPickerGUI.DEFAULT_SIGMA_MIN_RESPONSE, context.getSigmaMinResponse(),0);
		assertEquals(HelicalPickerGUI.DEFAULT_LOCAL_MIN_STRAIGHTNESS, context.getMinimumLineStraightness(),0);
		assertEquals(HelicalPickerGUI.DEFAULT_WINDOW_STRAIGHTNESS_SIZE, context.getWindowWidthStraightness(),0);
	}

}
