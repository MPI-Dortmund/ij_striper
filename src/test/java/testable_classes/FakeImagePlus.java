package testable_classes;

import ij.ImagePlus;
import ij.gui.Overlay;

public class FakeImagePlus extends ImagePlus {
	
	FakeOverlay ov;
	
	public FakeImagePlus(FakeOverlay ov) {
		this.ov = ov;
	}
	
	@Override
	public Overlay getOverlay() {
		// TODO Auto-generated method stub
		return ov;
	}
	
	

}
