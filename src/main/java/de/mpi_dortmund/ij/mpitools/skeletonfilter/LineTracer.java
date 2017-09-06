package de.mpi_dortmund.ij.mpitools.skeletonfilter;

import java.awt.Point;
import java.awt.Polygon;
import java.util.ArrayList;

import org.omg.CORBA.IMP_LIMIT;

import ij.IJ;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

public class LineTracer {
	
	
	public ArrayList<Polygon> extractLines(ByteProcessor ip){
		
		ByteProcessor map = new ByteProcessor(ip.getWidth(), ip.getHeight());
		LineTracer tracer = new LineTracer();
		ArrayList<Polygon> lines = new ArrayList<Polygon>();
		
		for(int x = 0; x < ip.getWidth(); x++){
			for(int y = 0; y < ip.getHeight(); y++){
				if( isStartPoint(x,y,(ByteProcessor)ip,true) && map.getPixel(x, y)==0 ){
					Polygon p = tracer.traceLine(x, y, ip, map);
					
					lines.add(p);
				}
			}
		}
		
		
		return lines;
	}
	
	public boolean isStartPoint(int x, int y, ByteProcessor ip, boolean connected8){
		int n = countNeighbors(x, y, ip, connected8);
		
		return n==1;
	}

	
	public Polygon traceLine(int x, int y, ByteProcessor ip, ByteProcessor map){
	
		Point next = null;
		Polygon pol = new Polygon();
		int x_pos = x;
		int y_pos = y;
		map.putPixel(x_pos, y_pos, 1);
		pol.addPoint(x_pos, y_pos);
		while( (next=getNext(x_pos, y_pos, ip, map)) != null){
			x_pos = next.x;
			y_pos = next.y;
			
			pol.addPoint(next.x,next.y);
			map.putPixel(next.x, next.y, 1);
		}
		return pol;
		
		
	}
	
	public Point getNext(int x, int y, ByteProcessor ip, ByteProcessor map){
		for(int i = -1; i <= 1; i++){
			for(int j = -1; j <= 1; j++){
				if(j ==0 && i==0 ){
					continue;
				}
				
				if(isInside(x+i, y+i, ip)==false){
					continue;
				}
								
				if(ip.getPixel(x+i, y+j)>0 && map.getPixel(x+i, y+j)==0 ){
					
					
					return new Point(x+i, y+j);
				}
			}
		}
		return null;
	}
	
	private boolean isInside(int x, int y, ImageProcessor ip){
		
		if(x< 0 || y < 0){
			return false;
		}
		
		if(x >= ip.getWidth() || y >= ip.getHeight()){
			return false;
		}
		
		return true;
	}
	
	
	
	public int countNeighbors(int x, int y, ByteProcessor ip, boolean connected8){
		int n = 0;
		if(ip.getPixel(x, y)>0){
			if(connected8){
				for(int i = -1; i <= 1; i++){
					for(int j = -1; j <= 1; j++){
						if(j ==0 && i==0){
							continue;
						}
						if(ip.getPixel(x+i, y+j)>0){
							n++;
						}
					}
				}
			}
			else{
				if(ip.getPixel(x+1, y)>0){
					n++;
				}
				if(ip.getPixel(x-1, y)>0){
					n++;
				}
				if(ip.getPixel(x, y+1)>0){
					n++;
				}
				if(ip.getPixel(x, y-1)>0){
					n++;
				}
			}
			
		}
		
		return n;
		
	}

}
