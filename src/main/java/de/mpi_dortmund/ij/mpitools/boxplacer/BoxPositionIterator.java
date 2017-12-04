package de.mpi_dortmund.ij.mpitools.boxplacer;

import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.Point2D;
import java.util.Iterator;

import ij.IJ;

public class BoxPositionIterator implements Iterator<Point> {
	Polygon p;
	int boxsize;
	int curr = 0;
	double boxToBoxDistSq;
	double distToEndSq;
	boolean topleft = false;
	public BoxPositionIterator(Polygon p, int boxsize, int boxdista, boolean topleft) {
		this.p = p;
		boxToBoxDistSq = boxdista*boxdista;
		distToEndSq = (boxsize*boxsize)/4.0; 
		this.topleft= topleft;
		this.boxsize = boxsize;
	}
	
	@Override
	public boolean hasNext() {
		
		int i = nextPointPos();
		return (i!=-1);
	}
	
	
	private int nextPointPos(){
		for(int i = curr+1; i < p.npoints; i++){
			int x = p.xpoints[i];
			int y = p.ypoints[i];
			double distsq_to_start = Point2D.distanceSq(p.xpoints[0], p.ypoints[0], x, y);
			double distsq_to_prev = Point2D.distanceSq(p.xpoints[curr], p.ypoints[curr], x, y);
			double distsq_to_end = Point2D.distanceSq(p.xpoints[p.npoints-1], p.ypoints[p.npoints-1], x, y);
			
			if(distsq_to_start>distToEndSq &&
					distsq_to_prev>=boxToBoxDistSq &&
					distsq_to_end>distToEndSq){
				return i;
			}
		}
		return -1;
	}
	

	@Override
	public Point next() {
		int i = nextPointPos();
		if(i!=-1){
			curr = i;
			int x = p.xpoints[i];
			int y = p.ypoints[i];
			if(topleft){
				x = x - boxsize/2;
				y = y - boxsize/2;
			}
			return new Point(x, y);
		}
		return null;
	}

}
