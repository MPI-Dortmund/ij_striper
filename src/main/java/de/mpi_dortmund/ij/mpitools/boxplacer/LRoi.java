package de.mpi_dortmund.ij.mpitools.boxplacer;

import ij.gui.Roi;

public class LRoi extends Roi {
	/**
	 * 
	 */
	private static final long serialVersionUID = -322247483958866642L;
	int line_id;
	
	public LRoi(int x, int y, int width, int height, int line_id) {
		super(x, y, width, height);
		this.line_id = line_id;
	}
	
	public int getLineID(){
		return line_id;
	}

}
