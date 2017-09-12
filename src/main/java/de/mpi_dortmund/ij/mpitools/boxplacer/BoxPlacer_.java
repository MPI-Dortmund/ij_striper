package de.mpi_dortmund.ij.mpitools.boxplacer;

import java.awt.Color;
import java.awt.Polygon;
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
		
		placeBoxes(ip, targetImage, ip.getSliceNumber(), boxsize, box_distance);
	}
	
	public synchronized void increaseID(){
		running_id++;
	}
	
	public ArrayList<Line> placeBoxes(ImageProcessor lineImage, ImagePlus targetImage, int slicePosition, int boxsize, int box_distance){
		ArrayList<Line> allLines = new ArrayList<Line>();
		Overlay ov = targetImage.getOverlay();
		if(ov==null){
			ov = new Overlay();
			targetImage.setOverlay(ov);
		}
		targetImage.setProperty("line_image", lineImage.convertToByteProcessor());
		
		LineTracer tracer = new LineTracer();
		
		ArrayList<Polygon> lines = tracer.extractLines((ByteProcessor) lineImage);
		int distancesq = box_distance*box_distance;
		Color[] colors = {Color.red,Color.BLUE,Color.GREEN,Color.yellow,Color.CYAN,Color.ORANGE, Color.magenta};
		
		for (Polygon p : lines) {
			increaseID();
			Line l = new Line(running_id);
			Color c = colors[running_id%colors.length];
			int x = p.xpoints[0]-boxsize/2;
			int y = p.ypoints[0]-boxsize/2;
			
			Roi r = new Roi(x, y, boxsize, boxsize);
			r.setProperty("id", ""+running_id);
			
			r.setPosition(slicePosition);
			r.setStrokeColor(c);
			ov.add(r);
			l.add(r);
			for(int i = 1; i < p.npoints; i++){
				int xc = p.xpoints[i]-boxsize/2;
				int yc = p.ypoints[i]-boxsize/2;
				
				if(distsq(x, y, xc, yc)>=distancesq){
					x = xc;
					y = yc;
				
					r = new Roi(x, y, boxsize, boxsize);
					r.setProperty("id", ""+running_id);
					r.setStrokeColor(c);
					
					r.setPosition(slicePosition);
					ov.add(r);
					l.add(r);
				}
			}
			allLines.add(l);
		}
		
		return allLines;
	}
	
	
	
	public double distsq(int x1, int y1, int x2, int y2){
		return Math.pow(x1-x2, 2)+Math.pow(y1-y2, 2);
	}
	
	

}
