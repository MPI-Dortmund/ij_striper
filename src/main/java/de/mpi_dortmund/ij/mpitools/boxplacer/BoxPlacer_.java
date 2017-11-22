package de.mpi_dortmund.ij.mpitools.boxplacer;

import java.awt.Color;
import java.awt.Polygon;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import de.mpi_dortmund.ij.mpitools.skeletonfilter.LineTracer;
import fiji.util.gui.GenericDialogPlus;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.plugin.filter.PlugInFilter;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

public class BoxPlacer_ implements PlugInFilter {
	
	private static int running_id = 1;
	int boxsize;
	int box_distance;
	ImagePlus targetImage;
	

	public int setup(String arg, ImagePlus imp) {
		if(arg.equals("final")){
			targetImage.repaintWindow();
			return DONE;
		}
		GenericDialogPlus gd = new GenericDialogPlus("Box placer");
		gd.addImageChoice("Target image", "");
		gd.addNumericField("Box_size", 50, 0);
		gd.addNumericField("Box_distance", 5, 0);
		gd.showDialog();
		
		targetImage = gd.getNextImage();
		boxsize = (int) gd.getNextNumber();
		box_distance = (int) gd.getNextNumber();
		
		if(gd.wasCanceled()){
			return DONE;
		}
		return IJ.setupDialog(imp, DOES_8G+PARALLELIZE_STACKS+FINAL_PROCESSING);
	}

	public void run(ImageProcessor ip) {
		BoxPlacingContext context = new BoxPlacingContext();
		context.setSlicePosition(ip.getSliceNumber());
		context.setPlacePoints(false);
		placeBoxes(ip, targetImage, context);
	}
	
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
	
	

}
