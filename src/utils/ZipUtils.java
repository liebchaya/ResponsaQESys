package utils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;


/**
 * Utilities for zip treatment
 * @author HZ
 *
 */
public class ZipUtils {

	/**
	 * Unzips a folder
	 * @param source
	 * @param destination
	 */
	public static void unzip(String source, String destination){ 
		 String password = "password";  
		 try {  
		   ZipFile zipFile = new ZipFile(source);  
		   if (zipFile.isEncrypted()) {  
		     zipFile.setPassword(password);  
		   }  
		   zipFile.extractAll(destination);  
		 } catch (ZipException e) {  
		   e.printStackTrace();  
		 }  
	    }
	
	/**
	 * Creates a compressed archive of a folder, adding a timestamp in milliseconds to the archive name.  
	 * This allows your application to create multiple archives without overwriting previous archives.
	 * @param destination
	 * @return zip file path
	 */
	public static String zip(String destination){
		try {
			  Calendar calendar = Calendar.getInstance();
			  Date time = calendar.getTime();
			  long milliseconds = time.getTime();

			  // Initiate ZipFile object with the path/name of the zip file.
			  String zipPath = destination+"_"+milliseconds+".zip";
			  ZipFile zipFile = new ZipFile(zipPath);

			  // Folder to add
			  String folderToAdd = destination;

			  // Initiate Zip Parameters which define various properties such
			  // as compression method, etc.
			  ZipParameters parameters = new ZipParameters();

			  // set compression method to store compression
			  parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);

			  // Set the compression level
			  parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);

			  // Add folder to the zip file
			  zipFile.addFolder(folderToAdd, parameters);
			  
			  return zipPath;

			 } catch (ZipException e) {
			  e.printStackTrace();
			  return null;
			 }
	}
	
	/**
	 * Create a compressed archive of a folder, adding a timestamp in milliseconds to the archive name.  
	 * This allows your application to create multiple archives without overwriting previous archives.
	 * @param destination
	 * @param folders a set of sub-directories to zip
	 * @return zip file path
	 */
	public static String zip(String destination, HashSet<String> folders){
		try {
			  Calendar calendar = Calendar.getInstance();
			  long milliseconds = calendar.getTimeInMillis();
			  
			  // Convert from millisecs to a String with a defined format
		      SimpleDateFormat date_format = new SimpleDateFormat("ddMMyyyy_HHmm");
		      Date resultdate = new Date(milliseconds);

			  // Initiate ZipFile object with the path/name of the zip file.
			  String zipPath = destination+"_"+date_format.format(resultdate)+".zip";
			  ZipFile zipFile = new ZipFile(zipPath);

			  // Initiate Zip Parameters which define various properties such
			  // as compression method, etc.
			  ZipParameters parameters = new ZipParameters();

			  // set compression method to store compression
			  parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);

			  // Set the compression level
			  parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);

			  File dest = new File(destination);
			  for (File dir:dest.listFiles())
				  if (dir.isDirectory() && folders.contains(dir.getName()))
					  // Add folder to the zip file
					  zipFile.addFolder(dir.getAbsolutePath(), parameters);
			  
			  return zipPath;

			 } catch (ZipException e) {
			  e.printStackTrace();
			  return null;
			 }
	}

}

