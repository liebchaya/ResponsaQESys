package test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import obj.Pair;

public class EvalAddData {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		HashMap<Integer,HashMap<Integer,Pair<Integer,Integer>>> dataMap = new HashMap<Integer, HashMap<Integer,Pair<Integer,Integer>>>();
		String f1 = "C:\\Documents and Settings\\HZ\\Desktop\\AnalJournalIterative\\groups4filtering\\groupsData4Manual.txt";
		String f2 = "C:\\Documents and Settings\\HZ\\Desktop\\AnalJournalIterative\\groups4filtering\\groupsData4ManualBaseline.txt";
		String f3 = "C:\\Documents and Settings\\HZ\\Desktop\\finalK.txt";
		
		BufferedReader reader = new BufferedReader(new FileReader(f1));
		String line = reader.readLine();
		line = reader.readLine();
		while (line != null){
			String []tokens = line.split("\t");
			int id = Integer.parseInt(tokens[0]);
			if (!dataMap.containsKey(id)){
				HashMap<Integer,Pair<Integer,Integer>> groups = new HashMap<Integer, Pair<Integer,Integer>>();
				groups.put(Integer.parseInt(tokens[2]), new Pair<Integer,Integer>(Integer.parseInt(tokens[3]),Integer.parseInt(tokens[4])));
				dataMap.put(id, groups);
			} else {
				int group = Integer.parseInt(tokens[2]);
				if (!dataMap.get(id).containsKey(group))
					dataMap.get(id).put(group, new Pair<Integer,Integer>(Integer.parseInt(tokens[3]),Integer.parseInt(tokens[4])));
				else {
					Pair<Integer, Integer> g = dataMap.get(id).get(group);
					if (g.key() == 0 && Integer.parseInt(tokens[3]) > 0)
						dataMap.get(id).put(group, new Pair<Integer,Integer>(Integer.parseInt(tokens[3]),Integer.parseInt(tokens[4])));
				}
			}
			line = reader.readLine();
		}
		reader.close();
		
		reader = new BufferedReader(new FileReader(f2));
		line = reader.readLine();
		line = reader.readLine();
		while (line != null){
			String []tokens = line.split("\t");
			int id = Integer.parseInt(tokens[0]);
			if (!dataMap.containsKey(id)){
				HashMap<Integer,Pair<Integer,Integer>> groups = new HashMap<Integer, Pair<Integer,Integer>>();
				groups.put(Integer.parseInt(tokens[2]), new Pair<Integer,Integer>(Integer.parseInt(tokens[3]),Integer.parseInt(tokens[4])));
				dataMap.put(id, groups);
			} else {
				int group = Integer.parseInt(tokens[2]);
				if (!dataMap.get(id).containsKey(group))
					dataMap.get(id).put(group, new Pair<Integer,Integer>(Integer.parseInt(tokens[3]),Integer.parseInt(tokens[4])));
				else {
					Pair<Integer, Integer> g = dataMap.get(id).get(group);
					if (g.key() == 0 && Integer.parseInt(tokens[3]) > 0)
						dataMap.get(id).put(group, new Pair<Integer,Integer>(Integer.parseInt(tokens[3]),Integer.parseInt(tokens[4])));
				}
			}
			line = reader.readLine();
		}
		reader.close();
		
		int prevId = -1;
		int sum=0;
		int[] arrOld = new int[4];
		int[] arrNew = new int[4];
		reader = new BufferedReader(new FileReader(f3));
		line = reader.readLine();
		line = reader.readLine();
		while (line != null){
			String []tokens = line.split("\t");
			int id = Integer.parseInt(tokens[0]);
			if (Integer.parseInt(tokens[2])==-99){
				line = reader.readLine();
				continue;
			}
			Pair<Integer, Integer> p = dataMap.get(id).get(Integer.parseInt(tokens[2]));
			int period = p.key();
			int generation = p.value();
			if(id != prevId){
				if (prevId != -1)
					System.out.println(prevId + "\t" + sum + "\t"+ arrOld[0] + "\t" + arrNew[0] + "\t"+ arrOld[1] + "\t" + arrNew[1] + "\t"+ arrOld[2] + "\t" + arrNew[2] + "\t"+ arrOld[3] + "\t" + arrNew[3]);
				sum=1;
				arrNew = new int[4];
				arrOld = new int[4];
				if (period>0){
					arrOld[generation] = 1;
				} else{
					arrNew[generation] = 1;
				}
			} else {
				sum +=1;
				if (period>0){
					int prevOld = arrOld[generation];
					arrOld[generation] = prevOld+1;
				}
				else{
					int prevNew = arrNew[generation];
					arrNew[generation] = prevNew +1;
				}
				
			}
			prevId = id;
			line = reader.readLine();
		}
		System.out.println(prevId + "\t" + sum + "\t"+ arrOld[0] + "\t" + arrNew[0] + "\t"+ arrOld[1] + "\t" + arrNew[1] + "\t"+ arrOld[2] + "\t" + arrNew[2] + "\t"+ arrOld[3] + "\t" + arrNew[3]);
		reader.close();
		
		
//		modernAncientRelatedTerms();
	}

	
	public static void modernAncientRelatedTerms() throws IOException {
		HashMap<Integer,HashMap<Integer,Integer>> dataMap = new HashMap<Integer, HashMap<Integer,Integer>>();
		String f1 = "C:\\Documents and Settings\\HZ\\Desktop\\AnalJournalIterative\\groups4filtering\\groupsData4Manual.txt";
		String f2 = "C:\\Documents and Settings\\HZ\\Desktop\\AnalJournalIterative\\groups4filtering\\groupsData4ManualBaseline.txt";
		String f3 = "C:\\Documents and Settings\\HZ\\Desktop\\finalK.txt";
		
		BufferedReader reader = new BufferedReader(new FileReader(f1));
		String line = reader.readLine();
		line = reader.readLine();
		while (line != null){
			String []tokens = line.split("\t");
			int id = Integer.parseInt(tokens[0]);
			if (!dataMap.containsKey(id)){
				HashMap<Integer,Integer> groups = new HashMap<Integer, Integer>();
				groups.put(Integer.parseInt(tokens[2]), Integer.parseInt(tokens[3]));
				dataMap.put(id, groups);
			} else {
				int group = Integer.parseInt(tokens[2]);
				if (!dataMap.get(id).containsKey(group))
					dataMap.get(id).put(group, Integer.parseInt(tokens[3]));
			}
			line = reader.readLine();
		}
		reader.close();
		
		reader = new BufferedReader(new FileReader(f2));
		line = reader.readLine();
		line = reader.readLine();
		while (line != null){
			String []tokens = line.split("\t");
			int id = Integer.parseInt(tokens[0]);
			if (!dataMap.containsKey(id)){
				HashMap<Integer,Integer> groups = new HashMap<Integer, Integer>();
				groups.put(Integer.parseInt(tokens[2]), Integer.parseInt(tokens[3]));
				dataMap.put(id, groups);
			} else {
				int group = Integer.parseInt(tokens[2]);
				if (!dataMap.get(id).containsKey(group))
					dataMap.get(id).put(group, Integer.parseInt(tokens[3]));
			}
			line = reader.readLine();
		}
		reader.close();
		
		int prevId = -1;
		int sum=0, oldR=0, newR=0;
		reader = new BufferedReader(new FileReader(f3));
		line = reader.readLine();
		line = reader.readLine();
		while (line != null){
			String []tokens = line.split("\t");
			int id = Integer.parseInt(tokens[0]);
			if (Integer.parseInt(tokens[2])==-99){
				line = reader.readLine();
				continue;
			}
			int period = dataMap.get(id).get(Integer.parseInt(tokens[2]));
			if(id != prevId){
				if (prevId != -1)
					System.out.println(prevId + "\t" + sum + "\t"+ oldR + "\t" + newR);
				sum=1;
				if (period>0){
					oldR = 1;
					newR = 0;
				}
				else{
					newR = 1;
					oldR = 0;
				}
			} else {
				sum +=1;
				if (period>0)
					oldR += 1;
				else
					newR += 1;
				
			}
			prevId = id;
			line = reader.readLine();
		}
		System.out.println(prevId + "\t" + sum + "\t"+ oldR + "\t" + newR);
		reader.close();
		
	}

}
