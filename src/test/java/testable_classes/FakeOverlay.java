package testable_classes;

import ij.gui.Overlay;
import ij.gui.Roi;

public class FakeOverlay extends Overlay {
	int rois_added = 0;
	@Override
	public void add(Roi roi) {
		// TODO Auto-generated method stub
		rois_added++;
	}
	
	public int getNumberLinesAdded(){
		return rois_added;
	}

}
