package de.mpi_dortmund.ij.mpitools.skeletonfilter;

import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;

import fiji.util.gui.GenericDialogPlus;
import ij.IJ;
import ij.ImagePlus;

import ij.gui.Roi;
import ij.plugin.FFTMath;
import ij.plugin.filter.PlugInFilter;
import ij.plugin.filter.RankFilters;
import ij.process.AutoThresholder;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
import ij.process.AutoThresholder.Method;

public class SkeletonFilter_ implements PlugInFilter {
	ImagePlus imp;
	ImagePlus response;
	ImagePlus input_image;
	int radius;
	int min_length;
	double min_response;
	double max_response;
	double min_straightness;
	int min_distance;
	int border_diameter;
	double double_filament_insensitivity;
	
	public int setup(String arg, ImagePlus imp) {
		this.imp = imp;
		
		GenericDialogPlus gd = new GenericDialogPlus("Skeleton Filter");
		
		gd.addMessage("Image choice:");
		gd.addImageChoice("Input image:", "");
		gd.addImageChoice("Response_map:", "");
		
		gd.addMessage("Preprocessing:");
		gd.addNumericField("Removement_radius:", 10, 0);
		gd.addNumericField("Border diameter:", 50, 0);
		
		gd.addMessage("Line filter:");
		gd.addNumericField("Minimum_length:", 10, 0);
		gd.addSlider("Minimum_straightness:", 0.0, 1.0, 0.90);
		gd.addNumericField("Minimum_line_distance:", 20, 0);
		gd.addNumericField("Minimum_response:", 0, 2);
		gd.addNumericField("Maximum_response:", 0, 2);
		gd.addSlider("Double_filament_detection_sensitivity:", 0.01, 0.99, 0.9); 
		
		gd.showDialog();
	
		
		if(gd.wasCanceled()){
			return DONE;
		}
		input_image = gd.getNextImage();
		response = gd.getNextImage();
		radius = (int) gd.getNextNumber();
		border_diameter = (int) gd.getNextNumber();
		min_length = (int) gd.getNextNumber();
		min_straightness = gd.getNextNumber();
		min_distance = (int) gd.getNextNumber();
		min_response =  gd.getNextNumber();
		max_response =  gd.getNextNumber();
		double_filament_insensitivity = 1-gd.getNextNumber();
		
		if(response.getImageStackSize() != input_image.getImageStackSize()){
			IJ.error("Response image and input image must have the same number of slices");
		}
		if(response.getHeight()!= input_image.getHeight() || response.getWidth() != input_image.getWidth()){
			IJ.error("Response image and input image must have the same dimensions");
		}
		return IJ.setupDialog(imp, DOES_8G);
	}

	public void run(ImageProcessor ip) {
		
		
		ArrayList<Polygon> lines = filterLineImage(ip, input_image.getStack().getProcessor(ip.getSliceNumber()),  response.getStack().getProcessor(ip.getSliceNumber()), border_diameter, min_distance, radius, min_straightness, min_length, max_response, min_response,double_filament_insensitivity);
		drawLines(lines, ip);
		
		
		
	}
	
	public ArrayList<Polygon> filterLineImage(ImageProcessor line_image, ImageProcessor input_image, ImageProcessor response_image, int border_diameter, int min_distance, int removementRadius, double min_straightness, int min_length, double max_response, double min_response, double double_filament_insensitivity){
		setBorderToZero((ByteProcessor)line_image,  border_diameter);
		removeJunctions((ByteProcessor) line_image,removementRadius);
		setCarbonEdgeToZero(input_image, line_image, 100);
		LineTracer tracer = new LineTracer();
		ArrayList<Polygon> lines = tracer.extractLines((ByteProcessor) line_image);
		removeParallelLines((ByteProcessor) line_image, lines, min_distance);
		lines = tracer.extractLines((ByteProcessor) line_image);
		lines = splitByStraightness(lines,min_straightness,25);

		lines = filterByLength(lines, min_length);

		line_image.setRoi(new Rectangle(0, 0, line_image.getWidth(), line_image.getHeight()));
		line_image.set(0);
		line_image.resetRoi();
		
		//lines = filterByResponseFixThresholds(lines, response.getProcessor(), min_response, max_response);
		lines = filterByResponseMeanStd(lines, response_image,max_response, min_response,double_filament_insensitivity);
		return lines;
	}
	
	
	private void setCarbonEdgeToZero(ImageProcessor inputImage, ImageProcessor lineImage, int filterSize){
		
		ImageProcessor mask = inputImage.duplicate().convertToByteProcessor();
		mask.resetRoi();

		mask.findEdges();
		RankFilters rf = new RankFilters();
		rf.rank(mask, filterSize, RankFilters.MEAN);
		ImagePlus help = new ImagePlus("carbon", mask);
		IJ.setAutoThreshold(help, "MaxEntropy");
	    IJ.run(help, "Convert to Mask", ""); 
	    mask = help.getProcessor();
	    int[] hist = mask.getHistogram();
	    if(hist[0]>hist[255]){
	    	mask.invert();
	    }
		//help.show();
		
		for(int x = 0; x < mask.getWidth(); x++){
			for(int y = 0; y < mask.getWidth(); y++){
				if(mask.getPixel(x, y)==0){
					lineImage.set(x, y, 0);
				}
			}
		}
		
	}
	
	private void setBorderToZero(ImageProcessor ip, int bordersize){
		Roi r = new Roi(0, 0, ip.getWidth(), bordersize);
		ip.setRoi(r);
		ip.set(0);
		ip.resetRoi();
		r = new Roi(0, ip.getHeight()-bordersize, ip.getWidth(), bordersize);
		ip.setRoi(r);
		ip.set(0);
		
		r = new Roi(0, 0, bordersize, ip.getWidth());
		ip.setRoi(r);
		ip.set(0);
		ip.resetRoi();
		r = new Roi(ip.getWidth()-bordersize, 0, bordersize, ip.getWidth());
		ip.setRoi(r);
		ip.set(0);
		
		ip.resetRoi();
	}
	
	private void removeParallelLines(ByteProcessor ip, ArrayList<Polygon> lines, int radius){
		
		for (Polygon p : lines) {
			
			for(int i = 0; i < p.npoints; i++){
				for(int x = p.xpoints[i]-radius; x < p.xpoints[i]+radius; x++){
					for(int y = p.ypoints[i]-radius; y < p.ypoints[i]+radius; y++){
			
						if(x<0 || x >= ip.getWidth() || y<0 || y>=ip.getHeight()){
							continue;
						}
						if(ip.get(x, y)>0 && isOnLine(x, y,p)==false){
							ip.set(x, y, 0);
							ip.set(p.xpoints[i], p.ypoints[i], 0);
						}
					}
				}
			}
			
		}
	}
	
	private boolean isOnLine(int x, int y, Polygon p){
		for(int i = 0; i < p.npoints; i++){
			if(p.xpoints[i]==x && p.ypoints[i]==y){
				return true;
			}
		}
		return false;
	}
	
	
	
	public void drawLines(ArrayList<Polygon> lines, ImageProcessor ip){
		for (Polygon p : lines) {
			for(int i = 0; i < p.npoints; i++){
				ip.putPixel(p.xpoints[i], p.ypoints[i], 255);
			}
		}
	}
	
	
	private ArrayList<Polygon> splitByStraightness(ArrayList<Polygon> lines, double min_straightness, int window_length){
		ArrayList<Polygon> filtered = new ArrayList<Polygon>();
		for (Polygon p : lines) {
			int[] keepPos = new int[p.npoints];
			Arrays.fill(keepPos, 1);
			
			for(int i = 0; i < p.npoints-window_length; i++){
				double s = getStraightness(p, i, i+window_length);
				if(s<min_straightness){
					int index = i + window_length/2 + 1;
					keepPos[index] = 0;
				}
			}
			Polygon newP = new Polygon();
			for(int i = 0; i < keepPos.length; i++){
				if(keepPos[i]==1){
					newP.addPoint(p.xpoints[i], p.ypoints[i]);
				}
				
				if(keepPos[i]==0){
					if(newP.npoints>2){
						filtered.add(newP);
					}
					newP = new Polygon();
				}
			}
			if(newP.npoints>2){
				filtered.add(newP);
			}
		}
		
		
		return filtered;
	}
	
	private double getStraightness(Polygon p, int from, int to ){
		double sum = 0;
		for(int i = from; i< to; i++){
			int dx = p.xpoints[i]-p.xpoints[i+1];
			int dy = p.ypoints[i]-p.ypoints[i+1];
			if( (dx==0) ^ (dy==0) ){
				sum += 1;
			}else{
				sum += Math.sqrt(2);
			}
		}
		double distance = Math.sqrt(Math.pow(p.xpoints[from]-p.xpoints[to], 2)+Math.pow(p.ypoints[from]-p.ypoints[to], 2));
		
		return distance/sum;
	}
	
	private ArrayList<Polygon> filterByLength(ArrayList<Polygon> lines, int minlength){
		ArrayList<Polygon> filtered = new ArrayList<Polygon>();
		for (Polygon polygon : lines) {
			if(polygon.npoints > minlength){
				filtered.add(polygon);
			}
		}
		return filtered;
	}
	
	private ArrayList<Polygon> filterByResponseFixThresholds(ArrayList<Polygon> lines, ImageProcessor responde_map, int min_response, int max_response){
		ArrayList<Polygon> filtered = new ArrayList<Polygon>();
		
		for (Polygon p : lines) {
			boolean add = true;
			double mean_response = meanResponse(p, responde_map);

			if(min_response>0 && mean_response<min_response){
				add = false;
			}
			if(max_response>0 && mean_response>max_response){
				add = false;
			}
			if(add){
				filtered.add(p);
			}
		}		
		return filtered;
	}
	
	
	private ArrayList<Polygon> filterByResponseMeanStd(ArrayList<Polygon> lines, ImageProcessor responde_map, double sigmafactor_max, double sigmafactor_min, double double_filament_insensitivity){
		ArrayList<Polygon> filtered = new ArrayList<Polygon>();
		double mean_response = 0;
		for (Polygon p : lines) {
			mean_response += meanResponse(p, responde_map);
		}
		mean_response = mean_response/lines.size();
		
		double sd = 0;
		int N = 0;
		for (Polygon p : lines) {
			for(int i = 0; i < p.npoints; i++){
				sd += Math.pow(responde_map.getf(p.xpoints[i], p.ypoints[i])-mean_response,2);
				N++;
			}
		}
		sd = Math.sqrt(sd/N);
		
		double threshold_max = mean_response + sd*sigmafactor_max;
		double threshold_min = mean_response - sd*sigmafactor_min;

		//IJ.log("MEAN: " + mean_response + " sigma: " + sd + " TMAX: " + threshold_max + " TMIN: " + threshold_min);
		
		for (Polygon p : lines) {
			int nOver = 0;
			int nUnder = 0;
			
			for(int i = 0; i < p.npoints; i++){
				if(responde_map.getf(p.xpoints[i], p.ypoints[i])>threshold_max){
					nOver++;
				}
				
				if(responde_map.getf(p.xpoints[i], p.ypoints[i])<threshold_min){
					nUnder++;
				}
			}
			int numberOfPointsToBeExcluded = (int) (p.npoints*double_filament_insensitivity);
			boolean add = nOver<numberOfPointsToBeExcluded && nUnder<numberOfPointsToBeExcluded;
			
			if(add){
				filtered.add(p);
			}
		}
		
		return filtered;
	}
	
	private double meanResponse(Polygon p, ImageProcessor responde_map){
		double sum = 0;
		for(int i = 0; i < p.npoints; i++){
			sum += responde_map.getf(p.xpoints[i], p.ypoints[i]);
		}
		double mean = sum / p.npoints;
		return mean;
	}
	
	
	
	
	private void removeJunctions(ByteProcessor ip, int removementRadius){
		ArrayList<Point> juncPos = new ArrayList<Point>();
		for(int x = 0; x < ip.getWidth(); x++){
			for(int y = 0; y < ip.getHeight(); y++){
				if( isJunction(x,y,(ByteProcessor)ip,true) ){
					juncPos.add(new Point(x, y));
				}
			}
		}
		
		for (Point point : juncPos) {
			setRegionToBlack(point.x, point.y, ip, removementRadius);
		}
	}
	
	private void setRegionToBlack(int x, int y, ByteProcessor ip, int radius){
		for(int i = -radius; i <= radius; i++){
			for(int j = -radius; j <= radius; j++){
				ip.putPixel(x+i, y+j, 0);
			}
		}
	}
	
	private boolean isJunction(int x, int y, ByteProcessor ip, boolean connected8){
		
		int n = countNeighbors(x, y, ip, connected8);
		
		return n>2;
		
	}
	
	private int countNeighbors(int x, int y, ByteProcessor ip, boolean connected8){
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
