package ij_helical_picker;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import de.mpi_dortmund.ij.mpitools.boxplacer.HeliconParticleExporter_;
import de.mpi_dortmund.ij.mpitools.boxplacer.Line;
import ij.IJ;
import ij.ImagePlus;

public class TestHeliconExporter {

	@Test
	public void test_getLinesFromOverlay_AllLinesWereDetected() {
		URL url = this.getClass().getClassLoader().getResource("stack1_3.tif");
		ImagePlus img = new ImagePlus(url.getPath());
		
		HeliconParticleExporter_ exporter = new HeliconParticleExporter_();
		ArrayList<Line> lines = exporter.getLinesFromOverlay(img.getOverlay());
		System.out.println("s: "+ lines.get(0).get(0));
		assertEquals(64, lines.size());
		
	}
	
	@Test
	public void test_getLinesFromSlice_AllLinesWereDetected() {
		URL url = this.getClass().getClassLoader().getResource("stack1_3.tif");
		ImagePlus img = new ImagePlus(url.getPath());
		
		HeliconParticleExporter_ exporter = new HeliconParticleExporter_();
		ArrayList<Line> lines = exporter.getLinesFromOverlay(img.getOverlay());
		ArrayList<Line> sliceLines = exporter.getLinesFromSlice(lines, 1);
		assertEquals(18, sliceLines.size());
		
		sliceLines = exporter.getLinesFromSlice(lines, 2);
		assertEquals(20, sliceLines.size());
		
		sliceLines = exporter.getLinesFromSlice(lines, 3);
		assertEquals(26, sliceLines.size());
		
	}
	
	@Test
	public void test_exportfiles_countNumberOfFiles() {
		URL url = this.getClass().getClassLoader().getResource("stack1_3.tif");
		ImagePlus img = new ImagePlus(url.getPath());
		
		HeliconParticleExporter_ exporter = new HeliconParticleExporter_();
		ArrayList<Line> lines = exporter.getLinesFromOverlay(img.getOverlay());
		
		String path = IJ.getDirectory("home")+".exptest/";

		File dir = new File(path);
		if(dir.exists()){
			try {
				FileUtils.deleteDirectory(dir);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		dir.mkdir();
		System.out.println(""+path);
		exporter.export(lines, img.getStack().getSliceLabels(), path, img.getStackSize(), img.getHeight(), 0.25);
		
		int numberOfFiles = dir.listFiles().length;
		assertEquals(3, numberOfFiles);
		if(dir.exists()){
			
			try {
				FileUtils.deleteDirectory(dir);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
	}
	


}
