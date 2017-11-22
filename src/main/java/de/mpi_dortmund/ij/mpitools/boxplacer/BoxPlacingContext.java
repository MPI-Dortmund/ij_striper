package de.mpi_dortmund.ij.mpitools.boxplacer;

public class BoxPlacingContext {
	private int slicePosition;
	private int box_size;
	private int box_distance;
	private boolean place_points;
	
	public BoxPlacingContext() {
		slicePosition =1;
		box_size = 2;
		box_distance = 4;
		place_points = false;
	}
	
	public int getSlicePosition() {
		return slicePosition;
	}
	public void setSlicePosition(int slicePosition) {
		this.slicePosition = slicePosition;
	}
	public int getBoxSize() {
		return box_size;
	}
	public void setBoxSize(int box_size) {
		this.box_size = box_size;
	}
	public int getBoxDistance() {
		return box_distance;
	}
	public void setBoxDistance(int box_distance) {
		this.box_distance = box_distance;
	}
	public boolean isPlacePoints() {
		return place_points;
	}
	public void setPlacePoints(boolean place_points) {
		this.place_points = place_points;
	}
	
	
	
}
