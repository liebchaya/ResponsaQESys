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
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

import obj.Pair;

public class BaselineGeneration {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		File judgmentsDir = new File("C:\\Documents and Settings\\HZ\\Desktop\\baseline\\judgments");
		String annotatedDir = "C:\\Documents and Settings\\HZ\\Desktop\\baseline\\annotated";
		File baselineDir = new File("C:\\Documents and Settings\\HZ\\Desktop\\baseline\\baseline");
		HashMap<String, Pair<Integer,Integer>> annotatedMap;
		HashMap<Integer, Pair<Integer,String>> groupsMap;
		BufferedReader reader;
		int totalNew = 0, switches = 0, modernCount=0, ancientCount=0,modernCountOld=0, ancientCountOld=0;
		for(File f:judgmentsDir.listFiles()){
			int modernC=0, ancientC=0;
			// load annotations to map
			annotatedMap = new HashMap<String, Pair<Integer,Integer>>();
			File annotatedFile = new File(annotatedDir+"\\"+f.getName());
			if(!annotatedFile.exists())
				System.out.println("Missing annotated file: " + annotatedFile.getAbsolutePath());
			else {
				reader = new BufferedReader(new InputStreamReader(new FileInputStream(annotatedFile), "CP1255"));
				reader.readLine(); // skip the first line
				String line = reader.readLine();
				while(line != null){
					String[] tokens = line.split("\t");
					annotatedMap.put(tokens[0], new Pair<Integer,Integer>(Integer.parseInt(tokens[3]),Integer.parseInt(tokens[4])));
					line = reader.readLine();
				}
				reader.close();
			}
			// load groups to map
			groupsMap = new HashMap<Integer, Pair<Integer,String>>();
			File groupsFile = new File(annotatedDir+"\\"+f.getName().substring(0,f.getName().indexOf("."))+".groups");
			if(!groupsFile.exists())
				System.out.println("Missing groups file: " + groupsFile.getAbsolutePath());
			else {
				reader = new BufferedReader(new InputStreamReader(new FileInputStream(groupsFile), "CP1255"));
				String line = reader.readLine();
				while(line != null){
					String[] tokens = line.split("\t");
					groupsMap.put(Integer.parseInt(tokens[0]),new Pair<Integer,String>(Integer.parseInt(tokens[2]),tokens[1]));
					line = reader.readLine();
				}
				reader.close();
			}
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
				    new FileOutputStream(baselineDir + "\\" + f.getName()), "CP1255"));
			writer.write("\n");
			int counter = 0;
			// read judgment file
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), "CP1255"));
			reader.readLine(); // skip the first line
			String line = reader.readLine();
			while(line != null){
				String[] tokens = line.split("\t");
				// if judgment in annotations' map
				if (!annotatedMap.containsKey(tokens[0])){
					writer.write(line + "\t0\n");
					totalNew++;
					if(Integer.parseInt(tokens[2])==0){
						modernCount++;
						modernC++;
					}
					else{
						ancientCount++;
						ancientC++;
					}
				}
				else {
					if(Integer.parseInt(tokens[2])==0)
						modernCountOld++;
					else
						ancientCountOld++;
					// re-write the line to include the annotation
					Pair<Integer,Integer> judgment = annotatedMap.get(tokens[0]);
					writer.write(tokens[0]+"\t"+tokens[1]+"\t"+tokens[2]+"\t"+judgment.key()+"\t"+judgment.value()+"\t"+tokens[5]+"\t0\n");
					if (judgment.value()>-1){
						int period = groupsMap.get(judgment.value()).key();
						String str = groupsMap.get(judgment.value()).value();
						if(period>0&&Integer.parseInt(tokens[2])==0){
							groupsMap.put(judgment.value(),new Pair<Integer,String>(0,str));
							switches++;
//							System.out.println(str);
						}
					}
					// increase the counter
					counter++;
				}
				line = reader.readLine();
			}
			reader.close();
			writer.close();
			// write groups file
			groupsFile = new File(baselineDir+"\\"+f.getName().substring(0,f.getName().indexOf("."))+".groups");
			writer = new BufferedWriter(new OutputStreamWriter(
				    new FileOutputStream(groupsFile), "CP1255"));
			SortedSet<Integer> keys = new TreeSet<Integer>(groupsMap.keySet());
			for(int i:keys)
				writer.write(i + "\t" + groupsMap.get(i).value() + "\t" + groupsMap.get(i).key() + "\n");
			writer.close();
			// verify that all the annotations were recognized, while scanning the judgment file
			if(counter!=annotatedMap.size()){
				System.out.println("Matching problem in: " + f.getAbsolutePath());
			}
			System.out.println(f.getName().substring(0,f.getName().indexOf("."))+"\t"+ancientC+"\t"+modernC);
				
		}
		System.out.println("Total new annotations: " + totalNew);	
		System.out.println("Modern new annotations: " + modernCount);
		System.out.println("Ancient new annotations: " + ancientCount);
		System.out.println("Modern old annotations: " + modernCountOld);
		System.out.println("Ancient old annotations: " + ancientCountOld);
		System.out.println("Switched groups: " + switches);
	}

}
