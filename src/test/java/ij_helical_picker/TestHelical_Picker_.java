package ij_helical_picker;

import static org.junit.Assert.*;

import java.awt.HeadlessException;

import org.junit.Test;

import de.mpi_dortmund.ij.mpitools.helicalPicker.gui.HelicalPickerGUI;
import testable_classes.JFrameFake;

public class TestHelical_Picker_ {

	@Test
	public void builds_without_exception() {
		HelicalPickerGUI gui = new HelicalPickerGUI();
		JFrameFake frm = new JFrameFake();
		gui.setMainFrame(frm);
		try{
		gui.createAndShowGUI();
		}catch (HeadlessException e) {
			// IGnore
		}
		
		assertEquals(true, frm.isVisible());
	}

}
