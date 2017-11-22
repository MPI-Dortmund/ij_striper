package de.mpi_dortmund.ij.mpitools.skeletonfilter;

import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;

import de.mpi_dortmund.ij.mpitools.userfilter.IUserFilter;
import fiji.util.gui.GenericDialogPlus;
import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.Roi;
import ij.measure.CurveFitter;
import ij.plugin.FFTMath;
import ij.plugin.filter.PlugInFilter;
import ij.plugin.filter.RankFilters;
import ij.process.AutoThresholder;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
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
	boolean fitDistr;
	double min_straightness;
	int window_straightness;
	int min_distance;
	int border_diameter;
	double double_filament_insensitivity;
	ImagePlus mask;
	ArrayList<IUserFilter> userFilters;
	
	public SkeletonFilter_() {
		// TODO Auto-generated constructor stub
	}
	
	public SkeletonFilter_(int min_length, double min_straightness, int window_straightness, int radius, int border_diameter, double min_reponse, double max_response, boolean fitDistr, 
			int min_distance, double double_filament_insensitivity, ArrayList<IUserFilter> userFilters) {
		this.min_length = min_length;
		this.min_straightness = min_straightness;
		this.window_straightness = window_straightness;
		this.radius = radius;
		this.border_diameter = border_diameter;
		this.min_response = min_reponse;
		this.max_response = max_response;
		this.fitDistr = fitDistr;
		this.min_distance = min_distance;
		this.double_filament_insensitivity = double_filament_insensitivity;
		this.userFilters = userFilters;
	}
	
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
		gd.addNumericField("Straightness window size", 25, 0);
		gd.addNumericField("Minimum_line_distance:", 20, 0);
		gd.addNumericField("Minimum_response:", 0, 2);
		gd.addNumericField("Maximum_response:", 0, 2);
		gd.addCheckbox("Fit Resp. Distribution", false);
		gd.addSlider("Double_filament_detection_sensitivity:", 0.01, 0.99, 0.9); 
		String[] imageTitles = WindowManager.getImageTitles();
		String[] choices = new String[imageTitles.length];
		choices[0] = "None";
		for(int i = 1; i< choices.length; i++){
			choices[i] = imageTitles[i-1];
		}
		gd.addChoice("Mask", choices, choices[0]);
		
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
		window_straightness = (int) gd.getNextNumber();
		min_distance = (int) gd.getNextNumber();
		min_response =  gd.getNextNumber();
		max_response =  gd.getNextNumber();
		fitDistr = gd.getNextBoolean();
		double_filament_insensitivity = 1-gd.getNextNumber();
		mask = WindowManager.getImage(gd.getNextChoice());

		if(response.getImageStackSize() != input_image.getImageStackSize()){
			IJ.error("Response image and input image must have the same number of slices");
		}
		if(response.getHeight()!= input_image.getHeight() || response.getWidth() != input_image.getWidth()){
			IJ.error("Response image and input image must have the same dimensions");
		}
		return IJ.setupDialog(imp, DOES_8G);
	}

	public void run(ImageProcessor ip) {
		
		ImageProcessor maskImage = null;
		if(mask!=null){
			maskImage = mask.getStack().getProcessor(ip.getSliceNumber());
		}

		ArrayList<Polygon> lines = filterLineImage(ip, 
				input_image.getStack().getProcessor(ip.getSliceNumber()), 
				response.getStack().getProcessor(ip.getSliceNumber()), 
				maskImage,
				border_diameter, min_distance, radius, min_straightness, window_straightness, 
				min_length, max_response, min_response,fitDistr,double_filament_insensitivity, null);
		drawLines(lines, ip);
		
		
		
	}
	public ArrayList<Polygon> filterLineImage(ImageProcessor line_image, ImageProcessor input_image, ImageProcessor response_image, ImageProcessor mask){
		
		return filterLineImage(line_image, input_image, response_image, mask, border_diameter, min_distance, radius, min_straightness, window_straightness, min_length, max_response, min_response, fitDistr, double_filament_insensitivity, userFilters);
	}
	
	
	/*
	 *  1. Set border to zero
	 *  2. Remove junctions
	 *  3. (Apply mask)
	 *  4. Straightness
	 *  5. User filter
	 *  6. length filter
	 *  7. Filter by response
	 *  8. Remove parallel lines
	 *  9. Length filter 2
	 */
	public ArrayList<Polygon> filterLineImage(ImageProcessor line_image, ImageProcessor input_image, ImageProcessor response_image, ImageProcessor mask, int border_diameter, int min_distance, int removementRadius, double min_straightness, int window_straightness, int min_length, double max_response, double min_response, boolean fitDist, double double_filament_insensitivity, ArrayList<IUserFilter> userFilters){
		
		setBorderToZero((ByteProcessor)line_image,  border_diameter);
		//(new ImagePlus("after border to zero", line_image.duplicate())).show();
		removeJunctions((ByteProcessor) line_image,removementRadius);
		//(new ImagePlus("after remove junction", line_image.duplicate())).show();
		if(mask != null){
			applyMask(line_image, mask);
		}
		//(new ImagePlus("after carbon edge removed", line_image.duplicate())).show();
		LineTracer tracer = new LineTracer();
		ArrayList<Polygon> lines = tracer.extractLines((ByteProcessor) line_image);
		
		
		
		lines = splitByStraightness(lines,(ByteProcessor)line_image,min_straightness,window_straightness,removementRadius);
		//(new ImagePlus("after straightness ", line_image.duplicate())).show();
		
		if(userFilters!=null){
			for (IUserFilter filter : userFilters) {
				lines = filter.apply(input_image, response_image, line_image);

				drawLines(lines, line_image);
			}
		}
		
		lines = filterByLength(lines, min_length);
		drawLines(lines, line_image);
		
		//lines = filterByResponseFixThresholds(lines, response.getProcessor(), min_response, max_response);
		boolean doFitDistr = false;
		lines = filterByResponseMeanStd(lines, response_image,max_response, min_response,double_filament_insensitivity,doFitDistr);
		
		drawLines(lines, line_image);
		
		lines = removeParallelLines((ByteProcessor) line_image, lines, min_distance);
		
		lines = filterByLength(lines, min_length);

		return lines;
	}
	
	private void applyMask(ImageProcessor lineImage, ImageProcessor mask){
		for(int x = 0; x < mask.getWidth(); x++){
			for(int y = 0; y < mask.getHeight(); y++){
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
	
	private ArrayList<Polygon> removeParallelLines(ByteProcessor ip, ArrayList<Polygon> lines, int radius){
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
		LineTracer tracer = new LineTracer();
		ArrayList<Polygon> filtered_lines = tracer.extractLines((ByteProcessor) ip);
		return filtered_lines;
	}
	
	private boolean isOnLine(int x, int y, Polygon p){
		for(int i = 0; i < p.npoints; i++){
			if(p.xpoints[i]==x && p.ypoints[i]==y){
				return true;
			}
		}
		return false;
	}
	
	
	
	public static void drawLines(ArrayList<Polygon> lines, ImageProcessor ip){
		ip.setRoi(new Rectangle(0, 0, ip.getWidth(), ip.getHeight()));
		ip.set(0);
		ip.resetRoi();
		for (Polygon p : lines) {
			for(int i = 0; i < p.npoints; i++){
				ip.putPixel(p.xpoints[i], p.ypoints[i], 255);
			}
		}
	}
	
	private ArrayList<Polygon> splitByStraightness(ArrayList<Polygon> lines, ByteProcessor line_image, double min_straightness, int window_length, int radius){
		for (Polygon p : lines) {
			
			for(int i = 0; i < p.npoints-window_length; i++){
				double s = getStraightness(p, i, i+window_length);

				if(s<min_straightness){
					int index = i + window_length/2 + 1;
					setRegionToBlack(p.xpoints[index], p.ypoints[index], line_image, radius);
				}
			}
		}
		LineTracer tracer = new LineTracer();
		ArrayList<Polygon> filtered = tracer.extractLines((ByteProcessor) line_image);
		
		return filtered;
	}
	
	private double getStraightness(Polygon p, int from, int to ){
	
		double sum = 0;
		for(int i = from; i< to; i++){

			sum += Point2D.distance(p.xpoints[i], p.ypoints[i], p.xpoints[i+1], p.ypoints[i+1]);
			/*
			if( (dx==0) ^ (dy==0) ){
				sum += 1;
			}else{
				sum += Math.sqrt(2);
			}
			*/
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
	
	
	
	private ArrayList<Polygon> filterByResponseMeanStd(ArrayList<Polygon> lines, ImageProcessor response_map, double sigmafactor_max, double sigmafactor_min, double double_filament_insensitivity, boolean fitDistr){
		ArrayList<Polygon> filtered = new ArrayList<Polygon>();
		
		//Calculate mean response over all lines
		double sum_mean_line_response = 0;
		for (Polygon p : lines) {
			sum_mean_line_response += meanResponse(p, response_map);
		}
		double mean_response = sum_mean_line_response/lines.size();
		
		// Calculate standard deviation of the response
		double sd = 0;
		int N = 0;
		for (Polygon p : lines) {
			for(int i = 0; i < p.npoints; i++){
				sd += Math.pow(response_map.getf(p.xpoints[i], p.ypoints[i])-mean_response,2);
				N++;
			}
		}
		sd = Math.sqrt(sd/N);

		// Fit a distribution to get better estimates for mean response and the standard deviation
		if(fitDistr){
			double[] fitted = fitNormalDistributionToHist(getResponseHistogram(lines, response_map),mean_response,sd);
			
			mean_response = fitted[2];
			sd = fitted[3];
		}
		
		// Calculate the thresholds
		double threshold_max = mean_response + sd*sigmafactor_max;
		double threshold_min = mean_response - sd*sigmafactor_min;
		
		if(sigmafactor_max<Math.pow(10, -6)){
			// No max threshold!
			threshold_max = Double.MAX_VALUE;
		}
		else if(threshold_max>255){
			threshold_max = 254.9;
		}
		if(sigmafactor_min<Math.pow(10, -6)){
			threshold_min = Double.MIN_VALUE;
		}
		
	
		/*
		 *  For each line: Count the number of positions (pixel) which has a response below threshold_min or above threshold_max.
		 *  If the number if higher then a the number of positions times some factor (0-1) the filament will be excluded.
		 */
		for (Polygon p : lines) {
			int nOver = 0;
			int nUnder = 0;
			
			for(int i = 0; i < p.npoints; i++){
				if(response_map.getf(p.xpoints[i], p.ypoints[i])>threshold_max){
					nOver++;
				}
				
				if(response_map.getf(p.xpoints[i], p.ypoints[i])<threshold_min){
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
	
	private int[] getResponseHistogram(ArrayList<Polygon> lines, ImageProcessor responde_map){
		
		int[] hist = new int[256];
		
		for (Polygon p : lines) {
			for(int i = 0; i < p.npoints; i++){
				int v = responde_map.get(p.xpoints[i], p.ypoints[i]);
				hist[v] = hist[v]+1;
			}
		}
		
		return hist;
	}
	
	private double[] fitNormalDistributionToHist(int[] hist, double initMean, double initSD){
		
		double[] xData = new double[254]; // 254 -> Ignore saturated values
		double[] yData = new double[254];
		for(int i = 0; i < xData.length; i++){
			xData[i] = i;
			yData[i] = hist[i];
		}
		
		CurveFitter cfit = new CurveFitter(xData, yData);
		double[] init = {150,100,initMean,initSD};
		cfit.setInitialParameters(init);
		cfit.doFit(CurveFitter.GAUSSIAN);

		double[] fitted = cfit.getParams();
		
		return fitted;
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
