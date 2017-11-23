package de.mpi_dortmund.ij.mpitools.boxplacer;

import java.awt.Color;
import java.awt.Polygon;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import de.mpi_dortmund.ij.mpitools.skeletonfilter.LineTracer;
import ij.ImagePlus;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

public class BoxPlacer_ {
	
	private static int running_id = 1;
	int boxsize;
	int box_distance;
	ImagePlus targetImage;
	
	public synchronized void increaseID(){
		running_id++;
	}
	
	public ArrayList<Line> placeBoxes(ImageProcessor lineImage, ImagePlus targetImage, BoxPlacingContext placing_context){

		targetImage.setProperty("line_image", lineImage.convertToByteProcessor());
		
		LineTracer tracer = new LineTracer();
		
		ArrayList<Polygon> lines = tracer.extractLines((ByteProcessor) lineImage);
		ArrayList<Line> allLines = placeBoxes(lines, targetImage,placing_context);
		
		return allLines;
	}
	
	public ArrayList<Line> placeBoxes(ArrayList<Polygon> lines, ImagePlus targetImage, BoxPlacingContext placing_context){
		ArrayList<Line> allLines = new ArrayList<Line>();
		Overlay ov = targetImage.getOverlay();
		if(ov==null){
			ov = new Overlay();
			targetImage.setOverlay(ov);
		}
		
		int distancesq = box_distance*box_distance;
		Color[] colors = {Color.red,Color.BLUE,Color.GREEN,Color.yellow,Color.CYAN,Color.ORANGE, Color.magenta};
		int boxsize = placing_context.getBoxSize();
		for (Polygon p : lines) {
			increaseID();
			Line l = new Line(running_id);
			Color c = colors[running_id%colors.length];
			int start_index = 0;
			double d = 0;
			do {
				if(start_index+1<p.npoints){
					start_index++;
					d = Point2D.distanceSq(p.xpoints[0], p.ypoints[0], p.xpoints[start_index], p.ypoints[start_index]);
				}else{
					d = Double.POSITIVE_INFINITY;
				}
				
			}while(d<Math.pow(boxsize/2, 2));
			if(placing_context.isPlacePoints()){
				boxsize=1;
			}
			//IJ.log("START: " + start_index);
			int x = p.xpoints[start_index]-boxsize/2;
			int y = p.ypoints[start_index]-boxsize/2;
			
			
			
			Roi r = new Roi(x, y, boxsize, boxsize);
			r.setProperty("id", ""+running_id);
			
			r.setPosition(placing_context.getSlicePosition());
			r.setStrokeColor(c);
			ov.add(r);
			l.add(r);
			for(int i = start_index+1; i < p.npoints; i++){
				int xc = p.xpoints[i]-boxsize/2;
				int yc = p.ypoints[i]-boxsize/2;
				
				if(
						Point2D.distanceSq(x, y, xc, yc)>=distancesq && 
								Point2D.distanceSq(xc, yc, p.xpoints[p.npoints-1], p.ypoints[p.npoints-1])>Math.pow(placing_context.getBoxSize()/2, 2)
						){
					x = xc;
					y = yc;
				
					r = new Roi(x, y, boxsize, boxsize);
					r.setProperty("id", ""+running_id);
					r.setStrokeColor(c);
					
					r.setPosition(placing_context.getSlicePosition());
					ov.add(r);
					l.add(r);
				}
			}
			allLines.add(l);
		}
		
		return allLines;
	}
	
	public void placeBoxes(HashMap<Integer, ArrayList<Polygon>> lines, ImagePlus target_image, BoxPlacingContext placing_context){
		
		BoxPlacer_ placer = new BoxPlacer_();
	
		Overlay ov = new Overlay();
		target_image.setOverlay(ov);
		target_image.repaintWindow();
		
		Iterator<Integer> image_iterator = lines.keySet().iterator();
		while(image_iterator.hasNext()){
			int slice_position = image_iterator.next();
			placing_context.setSlicePosition(slice_position);
			ArrayList<Polygon> lines_in_image = lines.get(slice_position);
			if(lines_in_image.size()>0){
				placer.placeBoxes(lines_in_image, target_image, placing_context);
			}
			
		}

	}
	
	

}
