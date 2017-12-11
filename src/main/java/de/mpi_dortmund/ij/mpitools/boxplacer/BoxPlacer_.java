package de.mpi_dortmund.ij.mpitools.boxplacer;

import java.awt.Color;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import de.mpi_dortmund.ij.mpitools.helicalPicker.filamentFilter.LineTracer;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

public class BoxPlacer_ {
	
	private static int running_id = 1;
	int boxsize;
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
		

		Color[] colors = {Color.red,Color.PINK,Color.BLUE,Color.GREEN,Color.yellow,Color.CYAN,Color.ORANGE, Color.magenta};

		int boxsize = placing_context.getBoxSize();
		for (Polygon p : lines) {
			
			
			
			increaseID();
			BoxPositionIterator it = new BoxPositionIterator(p, boxsize, placing_context.getBoxDistance(), true);
			Line l = new Line(running_id);
			Color c = colors[running_id%colors.length];
			while(it.hasNext()){
				Point pos = it.next();
				if(placing_context.isPlacePoints()){
					boxsize=1;
				}
				Roi r = new Roi(pos.getX(), pos.getY(), boxsize, boxsize);
				r.setProperty("id", ""+running_id);
				
				r.setPosition(placing_context.getSlicePosition());
				r.setStrokeColor(c);
				ov.add(r);
				l.add(r);
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
