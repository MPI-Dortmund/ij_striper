package de.mpi_dortmund.ij.mpitools.boxplacer;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import com.opencsv.CSVWriter;

import fiji.util.gui.GenericDialogPlus;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.plugin.PlugIn;

public class HeliconParticleExporter_ implements PlugIn {
	double scale = 0.25;
	ImagePlus imp;
	
	public static String last_path;
	
	public void run(String arg) {
		this.imp = IJ.getImage();
		ArrayList<Line> lines = getLinesFromOverlay(imp.getOverlay());
		
		String[] labels = imp.getStack().getSliceLabels();
		if(labels==null){
			// Bug fix for virtual stacks
			labels = new String[imp.getStackSize()];
			for(int i = 0; i < labels.length; i++){
				labels[i] = imp.getStack().getSliceLabel(i+1);
			}
		}
		
		GenericDialogPlus gd = new GenericDialogPlus("Export box files");
		gd.addNumericField("Resize factor", 0.25, 2);
		gd.addDirectoryField("Export path", "");
		gd.showDialog();
		if(gd.wasCanceled()){
			return;
		}
		scale = gd.getNextNumber();
		String path = gd.getNextString();
		last_path = path;
		
		
		export(lines, labels, path, imp.getStackSize(), imp.getHeight(), scale);
		
	}
	
	/**
	 * @param ov Overlay with lines
	 * @return return list of lines extracted from overlay
	 */
	public ArrayList<Line> getLinesFromOverlay(Overlay ov){
		
		HashMap<Integer, Line> lineMap = new HashMap<Integer, Line>();
	
		for(int i = 0; i < ov.size(); i++){
			Roi r = ov.get(i);
			
			int id = Integer.parseInt(r.getProperty("id"));
			
			Line l = null;
			if(lineMap.containsKey(id)){
				l = lineMap.get(id);
			} else {
				l = new Line(id);
				lineMap.put(id, l);
			}
			l.add(r);
		}
		ArrayList<Line> lines = new ArrayList<Line>(lineMap.values());
		return lines;
	}
	
	/**
	 * Export lines into boxfile in helicon format.
	 * @param lines List of detected lines
	 * @param labels Slice labels (basically the filenames)
	 * @param path Path where to save the file
	 * @param numberOfImages Number of images contained in lines
	 * @param imageHeight Image height for rescaling
	 * @param scale shrinking factor of the image
	 */
	public void export(ArrayList<Line> lines, String[] labels, String path, int numberOfImages, int imageHeight, double scale){
		
		for (int i = 0; i < numberOfImages; i++) {
			ArrayList<Line> sliceLines = getLinesFromSlice(lines, i+1);
			if(sliceLines != null && sliceLines.size()>1){
				Line oneLine = sliceLines.get(0);
				Roi oneRoi = oneLine.get(0);
				int boxsize = (int) (oneRoi.getBounds().getWidth());
				String filename = labels[i];
				CSVWriter writer;
				try {
					String filepath = path + "/" + filename.substring(0, filename.length()-4) + "_ptcl_coords.box";
					writer = new CSVWriter(new FileWriter(filepath), '\t','\0');
					String add = "#micrograph: " + filename;
					writer.writeNext(new String[]{add});
					add = "#segment length: " + boxsize/scale;
					writer.writeNext(new String[]{add});
					add = "#segment width: " + boxsize/scale;
					writer.writeNext(new String[]{add});
					
					
					for (Line l : sliceLines) {
						double[] startpoint = scale_coordinates((int)l.get(0).getBounds().getCenterX(),(int)l.get(0).getBounds().getCenterY(),imageHeight,scale);
						double[] endpoint = scale_coordinates((int)l.get(l.size()-1).getBounds().getCenterX(),(int)l.get(l.size()-1).getBounds().getCenterY(),imageHeight,scale);
						add = "#helix: (" + startpoint[0] + ", " + startpoint[1] + "),(" + endpoint[0] + ", " + endpoint[1] + "),"+boxsize/scale;
						writer.writeNext(new String[]{add});
						
						for (Roi box : l) {
							String[] values = new String[2];
							double[] pos = scale_coordinates((int)box.getBounds().x+boxsize/2, (int)(box.getBounds().y+boxsize/2),imageHeight,scale);
							values[0] = "" + pos[0];
							values[1] = "" + pos[1];
						    writer.writeNext(values);
						}
						
					}

					writer.close();

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			
		}
	}
	
	
	// Coordinate transform
	public double[] scale_coordinates(int x, int y, int imageHeight, double scale){
		
		double originalImageHeight = imageHeight/scale;
		
		double xscaled = x/scale;
		double yscaled = originalImageHeight-y/scale;
		
		return new double[]{xscaled,yscaled};
		
		
	}
	
	public ArrayList<Line> getLinesFromSlice(ArrayList<Line> lines, int slicenumber){
		ArrayList<Line> linesFromSlice = new ArrayList<Line>();
		
		for (Line line : lines) {
			int pos = line.get(0).getPosition();
			if(pos == slicenumber){
				linesFromSlice.add(line);
			}
		}
		
		return linesFromSlice;
	}

}
