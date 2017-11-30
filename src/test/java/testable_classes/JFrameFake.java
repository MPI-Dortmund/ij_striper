package testable_classes;

import javax.swing.JFrame;

public class JFrameFake extends JFrame {
	
	boolean isvisible = false;
	
	@Override
	public void setVisible(boolean b) {
		isvisible = b;
	}
	
	@Override
	public boolean isVisible() {
		return isvisible;
	}

}
