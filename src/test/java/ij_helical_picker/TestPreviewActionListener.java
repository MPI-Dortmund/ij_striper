package ij_helical_picker;

import static org.junit.Assert.*;

import java.awt.Polygon;
import java.util.ArrayList;

import org.junit.Test;

import de.mpi_dortmund.ij.mpitools.helicalPicker.gui.PreviewActionListener;
import testable_classes.FakeImagePlus;
import testable_classes.FakeOverlay;

public class TestPreviewActionListener {

	@Test
	public void test_showLinesAsPreview_checkAddedLinesToOverlay() {
		
		FakeOverlay fake_ov = new FakeOverlay();
		FakeImagePlus imp = new FakeImagePlus(fake_ov);
		
		PreviewActionListener list = new PreviewActionListener(imp);
		ArrayList<Polygon> lines = new ArrayList<Polygon>();
		Polygon p = new Polygon();
		p.addPoint(0, 0);
		p.addPoint(1, 1);
		p.addPoint(2, 2);
		p.addPoint(3, 3);
		lines.add(p);
		lines.add(p);
		lines.add(p);
		lines.add(p);
		list.showLinesAsPreview(lines);
		
		int n_added = fake_ov.getNumberLinesAdded();
		
		assertEquals(4, n_added);
		
				
	}

}
