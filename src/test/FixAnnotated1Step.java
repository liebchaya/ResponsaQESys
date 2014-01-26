package test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class FixAnnotated1Step {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		String outputFolder = "C:\\Documents and Settings\\HZ\\QEsys\\fix\\";
		File annotatedFolder = new File("C:\\Documents and Settings\\HZ\\QEsys\\annotated\\");
		for(File f:annotatedFolder.listFiles()){
			if (f.getName().endsWith(".dataGroups")){
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFolder+f.getName()), "CP1255"));
				BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f.getAbsolutePath()), "CP1255"));
				String line = reader.readLine();
				while(line != null){
//					if (line.startsWith("Term"))
//						line = line + "\tGeneration";
//					else if(!line.trim().isEmpty())
//						line = line + "\t0";
//					writer.write(line+"\n");
					if(line.trim().isEmpty())
						writer.write("\n");
					else {
						line = line.substring(0,line.lastIndexOf("\t"));
						writer.write(line+"\t1\n");
					}
					line = reader.readLine();
					
				}
				writer.close();
				reader.close();
			}
		}

	}

}
