package de.mpi_dortmund.ij.mpitools.helicalPicker.filamentFilter;

import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import de.mpi_dortmund.ij.mpitools.boxplacer.BoxPositionIterator;
import de.mpi_dortmund.ij.mpitools.userfilter.IUserFilter;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;
import ij.measure.CurveFitter;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

public class FilamentFilter {
	ImagePlus imp;
	ImagePlus response;
	ImagePlus input_image;
	FilamentFilterContext context;
	
	public FilamentFilter() {
		// TODO Auto-generated constructor stub
	}
	
	public FilamentFilter(FilamentFilterContext context) {
		this.context = context;
		
	}

	public HashMap<Integer, ArrayList<Polygon>> filterLines(HashMap<Integer, ArrayList<Polygon>> lines, FilamentFilterContext context, ImageStack input_images, ImageStack response_maps){
		
		HashMap<Integer, ArrayList<Polygon>> filtered_lines = new HashMap<Integer, ArrayList<Polygon>>();
		/*
		 * Process filter lines
		 */

		FilamentFilter filament_filter = new FilamentFilter();
		Iterator<Integer> slice_iterator = lines.keySet().iterator();
		ImagePlus masks = context.getBinaryMask();
		while(slice_iterator.hasNext()){
			int slice_position = slice_iterator.next();
			ArrayList<Polygon> lines_frame_i = lines.get(slice_position);
			
			ByteProcessor line_image = new ByteProcessor(input_images.getWidth(), input_images.getHeight()); 
			line_image.setLineWidth(1);
			line_image.setColor(255);
			FilamentFilter.drawLines(lines_frame_i, line_image);
			line_image.invert();
			line_image.skeletonize();
			line_image.invert();
			ImageProcessor maskImage = null;
			if(masks!=null){
				maskImage = masks.getStack().getProcessor(slice_position);
			}
			ImageProcessor ip = input_images.getProcessor(slice_position);
			ImageProcessor response_map = response_maps.getProcessor(slice_position);
	
			ArrayList<Polygon> filteredLines = filament_filter.filterLineImage(line_image, 
					ip, response_map, maskImage, context);
			
			filtered_lines.put(slice_position, filteredLines);
			
		}
		
		return filtered_lines;
	
		
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
	public ArrayList<Polygon> filterLineImage(ImageProcessor line_image, ImageProcessor input_image, ImageProcessor response_image, ImageProcessor mask, FilamentFilterContext context){
		this.context = context;
		int border_diameter = context.getBorderDiameter();
		setBorderToZero((ByteProcessor)line_image, border_diameter);
		//(new ImagePlus("after border to zero", line_image.duplicate())).show();
		int removement_radius = context.getJunctionRemovementRadius();
		removeJunctions((ByteProcessor) line_image, removement_radius);
		//(new ImagePlus("after remove junction", line_image.duplicate())).show();
		if(mask != null){
			applyMask(line_image, mask);
		}
		//(new ImagePlus("after carbon edge removed", line_image.duplicate())).show();
		LineTracer tracer = new LineTracer();
		ArrayList<Polygon> lines = tracer.extractLines((ByteProcessor) line_image);
		
		
		double min_line_straightness = context.getMinimumLineStraightness();
		int window_width_straightness = context.getWindowWidthStraightness();

		lines = splitByStraightness(lines,(ByteProcessor)line_image,min_line_straightness,window_width_straightness,removement_radius);
		//(new ImagePlus("after straightness ", line_image.duplicate())).show();
		ArrayList<IUserFilter> userFilters = context.getUserFilters();
		if(userFilters!=null){
			for (IUserFilter filter : userFilters) {
				lines = filter.apply(input_image, response_image, line_image);

				drawLines(lines, line_image);
			}
		}
		
		int minimum_number_boxes = context.getMinimumNumberBoxes();
		lines = filterByLength(lines, minimum_number_boxes);
		drawLines(lines, line_image);
		
		//lines = filterByResponseFixThresholds(lines, response.getProcessor(), min_response, max_response);
		boolean doFitDistr = context.isFitDistribution();
		double sigma_max_response = context.getSigmaMaxResponse();
		double sigma_min_response = context.getSigmaMinResponse();
		double double_filament_insensitivity = context.getDoubleFilamentInsensitivity();
		lines = filterByResponseMeanStd(lines, response_image,sigma_max_response, sigma_min_response,double_filament_insensitivity,doFitDistr);
		
		drawLines(lines, line_image);
		
		int minimum_filament_distance = context.getMinFilamentDistance();
		lines = removeParallelLines((ByteProcessor) line_image, lines, minimum_filament_distance);
		
		lines = filterByLength(lines, minimum_number_boxes);

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
	
	private ArrayList<Polygon> filterByLength(ArrayList<Polygon> lines, int minimum_number_boxes){
		ArrayList<Polygon> filtered = new ArrayList<Polygon>();
		for (Polygon polygon : lines) {
			int num_box = calcNumberOfBoxes(polygon);
			if(num_box >= minimum_number_boxes){
				filtered.add(polygon);
			}
		}
		return filtered;
	}
	
	private int calcNumberOfBoxes(Polygon p){
		BoxPositionIterator it = new BoxPositionIterator(p, context.getBoxSize(), context.getBoxDistance(), false);
		int N = 0;
		
		while(it.hasNext()){
			N++;
			it.next();
		}
		
		return N;
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


