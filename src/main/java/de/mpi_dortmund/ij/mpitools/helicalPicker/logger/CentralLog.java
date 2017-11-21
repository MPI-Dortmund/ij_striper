package de.mpi_dortmund.ij.mpitools.helicalPicker.logger;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class CentralLog {
	
	private  static final Logger log = Logger.getLogger( Logger.class.getName() );
	private static Handler handler = null;
	public static synchronized Logger getInstance(){
		if(handler==null){
			try {
				
				log.setUseParentHandlers(false);
		
				String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format( new Date() );
				handler = new FileHandler( "log-"+timeStamp+".log" );
				handler.setFormatter(new SimpleFormatter());
				
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			log.addHandler( handler );
		}
		return log;
	}
	
	public static synchronized String m(String m){
		return "(TID " + Thread.currentThread().getId()+") " + m;
	}
	
	public static void setPath(String path){
		if(handler!=null){
			log.removeHandler(handler);
		}
		String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format( new Date() );
		try {
			log.setUseParentHandlers(false);
			handler = new FileHandler( path+ "log-"+timeStamp+".log" );
			handler.setFormatter(new SimpleFormatter());
			log.addHandler(handler);
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
