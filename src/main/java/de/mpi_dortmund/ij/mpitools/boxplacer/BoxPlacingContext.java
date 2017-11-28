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
	
	/**
	 * @return Position where the boxes will be placed
	 */
	public int getSlicePosition() {
		return slicePosition;
	}
	
	/**
	 * Set the position where the boxes will be placed
	 */
	public void setSlicePosition(int slicePosition) {
		this.slicePosition = slicePosition;
	}
	
	/**
	 * 
	 * @return The side length of the boxes to be placed.
	 */
	public int getBoxSize() {
		return box_size;
	}
	
	/** 
	 * @param box_size The side length of the boxes to be placed.
	 */
	public void setBoxSize(int box_size) {
		this.box_size = box_size;
	}
	
	/**
	 * @return The distance between two boxes.
	 */
	public int getBoxDistance() {
		return box_distance;
	}
	
	/**
	 * @param box_distance The distance between two boxes.
	 */
	public void setBoxDistance(int box_distance) {
		this.box_distance = box_distance;
	}
	
	/**
	 * @return If true, points instead of boxes will be placed.
	 */
	public boolean isPlacePoints() {
		return place_points;
	}
	
	/**
	 * @param place_points True, if points instead of boxes should be placed.
	 */
	public void setPlacePoints(boolean place_points) {
		this.place_points = place_points;
	}
	
	
	
}
