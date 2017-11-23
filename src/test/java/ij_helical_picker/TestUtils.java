package ij_helical_picker;

import ij.ImageStack;
import ij.process.ImageProcessor;

public class TestUtils {
	
	public static boolean isEquals(ImageStack is1, ImageStack is2){
		for(int i = 0; i < is1.getSize(); i++){
			if(isEquals(is1.getProcessor(i+1), is2.getProcessor(i+1))==false){
				return false;
			}
		}
		return true;
	}
	
	 public static boolean isEquals(ImageProcessor ip, ImageProcessor ip2) {
		 if(ip.getWidth() != ip2.getWidth() || ip.getHeight() != ip2.getHeight()){
			 throw new IllegalAccessError("Dimensions are not the same");
		
		 }
		 
		 for(int x = 0; x < ip.getWidth(); x++){
			 for(int y = 0; y < ip.getHeight(); y++){
				 if( Math.abs(ip.getf(x, y)-ip2.getf(x, y))>Math.pow(10, -6)){
					 throw new IllegalAccessError("Image is at least different in position x: " + x + " y: " + y + ". Value1: " + ip.getf(x, y) + " Value2: " + ip2.getf(x, y));
				 }
			 }
		 }
		 
		 return true;
	 }

}
