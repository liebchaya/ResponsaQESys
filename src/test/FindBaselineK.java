package test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

public class FindBaselineK {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
//		for(int i=10;i<40;i++)
//		 System.out.println(i + "\t" + countKpositives(false,i));
		
		for(int i=38;i<46;i++)
			System.out.println(i+ "\t" +countKpositives(false,i));
	}
	
	public static int getTotalJudgments(File f, boolean isAncient) throws IOException{
		String judgeDir = "C:\\Documents and Settings\\HZ\\Desktop\\baseline\\judgments\\";
		File newF = new File(judgeDir+f.getName());
		int count = 0;
		if (newF.exists()){
			BufferedReader reader = new BufferedReader(new FileReader(newF));
			reader.readLine(); // skip the first line
			String line = reader.readLine();
			while (line != null){
				String [] tokens = line.split("\t");
				if (isAncient) {
				// ancient term
					if (Integer.parseInt(tokens[2])>0)
						count++;
				} else {
						// modern term
							if (Integer.parseInt(tokens[2])==0)
								count++;
						}
				line = reader.readLine();
			}
			reader.close();
		}
		return count;
	}
		
	public static void kDetails(boolean isAncient, int K) throws IOException {
		String fileName = "modern"+K+".txt";
		if (isAncient)
			fileName = "modern"+K+".txt";
		String annotationDir = "C:\\Documents and Settings\\HZ\\Desktop\\QEsys_baseline\\QEsys\\annotated";
		BufferedWriter writer = new BufferedWriter(new FileWriter("C:\\Documents and Settings\\HZ\\Desktop\\QEsys_baseline\\"+fileName));
		String headline = "target_term_id\t";
		headline += "total\tavailable\tloss\n";
		writer.write(headline);
		File annDir = new File(annotationDir);
		for(File f:annDir.listFiles()){
			int count = 0;
			int pos = 0;
			int total = 0;
			if(f.getName().endsWith(".dataGroups")){
				String dataLine = "";
				HashMap<Integer,Integer> negMap = new HashMap<Integer, Integer>();
				BufferedReader reader = new BufferedReader(new FileReader(f));
				reader.readLine(); // skip the first line
				String line = reader.readLine();
				while (line != null){
					String [] tokens = line.split("\t");
					if (isAncient) {
					// ancient term
						if (Integer.parseInt(tokens[2])>0){
							// negative term
							total ++;
							if(Integer.parseInt(tokens[3])==0){
								count ++;
								if (count==K){
									if (!negMap.containsKey(count)){
										negMap.put(count, pos);
									}
								}
							} else {
								count = 0;
							}
						}
					}
					else {
					// modern term
						if (Integer.parseInt(tokens[2])==0){ 
						// negative term
							total ++;
							if(Integer.parseInt(tokens[3])==0){
								count ++;
								if (count==K){
									if (!negMap.containsKey(count)){
										negMap.put(count, total);
									}
								}
							} else {
								count = 0;
							}
						}
					}
					line = reader.readLine();
				}
				reader.close();
				dataLine = f.getName().substring(0,f.getName().indexOf("."))+"\t";
				if(negMap.containsKey(K))
					dataLine += (total-negMap.get(K)) + "\t";
				else
					dataLine += "-1\t";
				
				dataLine += total + "\t" + getTotalJudgments(f,isAncient);
				writer.write(dataLine.trim()+"\n");
			}
		}
			writer.close();

	}

	public static void kOptions(boolean isAncient, int minK, int maxK) throws IOException {
		String fileName = "modernMore.txt";
		if (isAncient)
			fileName = "ancientMore.txt";
		String annotationDir = "C:\\Documents and Settings\\HZ\\Desktop\\QEsys_baseline\\QEsys\\annotated";
		BufferedWriter writer = new BufferedWriter(new FileWriter("C:\\Documents and Settings\\HZ\\Desktop\\QEsys_baseline\\"+fileName));
		String headline = "target_term_id\t";
		for(int i=minK;i<=maxK;i++)
			headline += i + "\t";
		headline += "total\tavailable";
		headline = headline.trim() + "\n";
		writer.write(headline);
		File annDir = new File(annotationDir);
		for(File f:annDir.listFiles()){
			int count = 0;
			int pos = 0;
			int total = 0;
			if(f.getName().endsWith(".dataGroups")){
				String dataLine = "";
				HashMap<Integer,Integer> negMap = new HashMap<Integer, Integer>();
				BufferedReader reader = new BufferedReader(new FileReader(f));
				reader.readLine(); // skip the first line
				String line = reader.readLine();
				while (line != null){
					String [] tokens = line.split("\t");
					if (isAncient) {
					// ancient term
						if (Integer.parseInt(tokens[2])>0){
							// negative term
							total ++;
							if(Integer.parseInt(tokens[3])==0){
								count ++;
								if (count>= minK && count<=maxK){
									if (!negMap.containsKey(count)){
										negMap.put(count, pos);
									}
								}
							} else {
								count = 0;
							}
						}
					}
					else {
					// modern term
						if (Integer.parseInt(tokens[2])==0){ 
						// negative term
							total ++;
							if(Integer.parseInt(tokens[3])==0){
								count ++;
								if (count>= minK && count<=maxK){
									if (!negMap.containsKey(count)){
										negMap.put(count, total);
									}
								}
							} else {
								count = 0;
							}
						}
					}
					line = reader.readLine();
				}
				reader.close();
				dataLine = f.getName().substring(0,f.getName().indexOf("."))+"\t";
				for(int i=minK;i<=maxK;i++){
					if(negMap.containsKey(i))
						dataLine += (total-negMap.get(i)) + "\t";
					else
						dataLine += "-1\t";
				}
				dataLine += total + "\t" + getTotalJudgments(f,isAncient);
				writer.write(dataLine.trim()+"\n");
			}
		}
			writer.close();

	}
	
	public static void printMaxK() throws IOException {
	
	String annotationDir = "C:\\Documents and Settings\\HZ\\Desktop\\QEsys_baseline\\QEsys\\annotated";
	BufferedWriter writer = new BufferedWriter(new FileWriter("C:\\Documents and Settings\\HZ\\Desktop\\QEsys_baseline\\bestKanal.txt"));
	writer.write("target_term_id\tmaxAncient\tlostAncient\tmaxModern\tlostModern\n");
	File annDir = new File(annotationDir);
	for(File f:annDir.listFiles()){
		int modernCount = 0;
		int longestModern = 0;
		int longestModernPos = 0;
		int totalModern = 0;
		int ancientCount = 0;
		int longestAncient = 0;
		int longestAcientPos = 0;
		int totalAncient = 0;
		if(f.getName().endsWith(".dataGroups")){
			BufferedReader reader = new BufferedReader(new FileReader(f));
			reader.readLine(); // skip the first line
			String line = reader.readLine();
			while (line != null){
				String [] tokens = line.split("\t");
				// ancient term
				if (Integer.parseInt(tokens[2])>0){
					// negative term
					totalAncient ++;
					if(Integer.parseInt(tokens[3])==0){
						ancientCount ++;
					} else {
						if (ancientCount > longestAncient){
							longestAncient = ancientCount;
							longestAcientPos = totalAncient;
						}
						ancientCount = 0;
					}
				// modern term
				} else {
					// negative term
					totalModern ++;
					if(Integer.parseInt(tokens[3])==0){
						modernCount ++;
					} else {
						if (modernCount > longestModern){
							longestModern = modernCount;
							longestModernPos = totalModern;
						}
						modernCount = 0;
					}
				}
				line = reader.readLine();
			}
			reader.close();
			writer.write(f.getName().substring(0,f.getName().indexOf("."))+"\t"+longestAncient+"\t"+(totalAncient-longestAcientPos)+"\t"+longestModern+"\t"+(totalModern-longestModernPos)+"\n");
		}
	}
		writer.close();
	}
	
	public static int countKjudgments(boolean isAncient, int K) throws IOException {
//		if (isAncient)
//			System.out.println("ancient");
//		else
//			System.out.println("modern");
		String annotationDir = "C:\\Documents and Settings\\HZ\\Desktop\\QEsys_baseline\\QEsys\\annotated";
		int sum = 0;
		File annDir = new File(annotationDir);
		for(File f:annDir.listFiles()){
			int count = 0;
			int positive = 0;
			boolean bFound = false;
			int total = 0;
			if(f.getName().endsWith(".dataGroups")){
				BufferedReader reader = new BufferedReader(new FileReader(f));
				reader.readLine(); // skip the first line
				String line = reader.readLine();
				while (line != null){
					String [] tokens = line.split("\t");
					if (isAncient) {
					// ancient term
						if (Integer.parseInt(tokens[2])>0){
							// negative term
							total ++;
							if(Integer.parseInt(tokens[3])==0){
								count ++;
								if (count==K){
									sum+=total;
									bFound = true;
									break;
								}
							} else {
								positive++;
								count = 0;
							}
						}
					}
					else {
					// modern term
						if (Integer.parseInt(tokens[2])==0){ 
						// negative term
							total ++;
							if(Integer.parseInt(tokens[3])==0){
								count ++;
								if (count==K){
									sum+=total;
									bFound = true;
									break;
									}
								}
							} else {
								count = 0;
							}
						}
					
					line = reader.readLine();
				}
				if (!bFound){
					int sumJudg = getTotalJudgments(f,isAncient);
					if (sumJudg == total)
						sum += total;
					else if (sumJudg > total+(K-count))
						sum += total + (K-count);
					else
						sum += sumJudg;
				}
			}
		}
//		System.out.println("Total sum of judgments with " + K + ": " + sum);
		return sum;
	}
		
		
		public static int countKpositives(boolean isAncient, int K) throws IOException {
//			if (isAncient)
//				System.out.println("ancient");
//			else
//				System.out.println("modern");
			String annotationDir = "C:\\Documents and Settings\\HZ\\Desktop\\QEsys_baseline\\QEsys\\annotated";
			int sum = 0;
			File annDir = new File(annotationDir);
			for(File f:annDir.listFiles()){
				int count = 0;
				if(f.getName().endsWith(".dataGroups")){
					BufferedReader reader = new BufferedReader(new FileReader(f));
					reader.readLine(); // skip the first line
					String line = reader.readLine();
					while (line != null){
						String [] tokens = line.split("\t");
						if (isAncient) {
						// ancient term
							if (Integer.parseInt(tokens[2])>0){
								// negative term
								if(Integer.parseInt(tokens[3])==0){
									count ++;
									if (count==K){
										break;
									}
								} else {
									sum++;
									count = 0;
								}
							}
						}
						else {
						// modern term
							if (Integer.parseInt(tokens[2])==0){ 
							// negative term
								if(Integer.parseInt(tokens[3])==0){
									count ++;
									if (count==K){
										break;
									}
								} else {
									sum++;
									count = 0;
								}
							}
						}
						line = reader.readLine();
					
					
					}
				}
			}
			return sum;
		}
	

	


}
