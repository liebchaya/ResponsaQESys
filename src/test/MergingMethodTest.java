package test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import obj.Pair;
import utils.StringUtils;

import mwe.MWEDistance;
import mwe.MWEGrouping;

public class MergingMethodTest {

	private static HashMap<Integer, int[]> m_mergeMap;
//	private static String m_mergingFolder = "C:\\Documents and Settings\\HZ\\Desktop\\Merging\\";
//	private static String m_mergingFolder ="/home/ir/liebesc/QEsys/Merging";
	private static String m_mergingFolder ="/home/ir/liebesc/QEsys/MergingBefore";
	private static void initMergeMap() {
		// {2=[27, 28], 19=[43, 44], 3=[34, 35, 32, 33, 36, 29, 31, 30],
		// 21=[51, 50, 52], 4=[38, 39, 37, 42, 40, 41], 20=[46, 47, 45, 49, 48]}
		m_mergeMap = new HashMap<Integer, int[]>();
		m_mergeMap.put(2, new int[] { 27, 28 });
		m_mergeMap.put(3, new int[] { 29, 30, 31, 32, 33, 34, 35, 36 });
		m_mergeMap.put(4, new int[] { 37, 38, 39, 40, 41, 42 });
		m_mergeMap.put(19, new int[] { 43, 44 });
		m_mergeMap.put(20, new int[] { 45, 46, 47, 48, 49 });
		m_mergeMap.put(21, new int[] { 50, 51, 52 });
	}

	public static void mergeFiles(int targetTermId, int[] idList,String inputFolder, String outpuFolder) throws IOException {
		HashSet<String> seenResults = new HashSet<String>();
		System.out.println("writing merged file: " + outpuFolder + "/"
				+ targetTermId + "_Dice.dataGroups.txt");
		BufferedWriter writer = new BufferedWriter(new FileWriter(outpuFolder
				+ "/" + targetTermId + "_Dice.dataGroups.txt"));
		ArrayList<Pair<Integer, BufferedReader>> readersList = new ArrayList<Pair<Integer, BufferedReader>>();
		for (int id : idList) {
			File idFile = new File(inputFolder + "/" + id
					+ "_Dice.dataGroups.txt");
			if (idFile.exists()) {
				BufferedReader reader = new BufferedReader(new FileReader(
						inputFolder + "/" + id + "_Dice.dataGroups.txt"));
				readersList.add(new Pair<Integer, BufferedReader>(id, reader));
			}
		}
		MWEDistance dist = new MWEDistance();
		// merge a line from each file, avoid duplicate lines
		int activeCounter = readersList.size();
		while (activeCounter > 0) {
			activeCounter = readersList.size();
			for (int i = 0; i < readersList.size(); i++) {
				String line = readersList.get(i).value().readLine();
				if (line == null)
					activeCounter--;
				else {
					String result = line.split("\t")[0];
					HashSet<String> resultSet = StringUtils
							.convertStringToSet(result);
					boolean bfound = false;
					for (String l : resultSet){
						for( String seen:seenResults)
							if (dist.distance(seen, l) == 0){
								bfound = true;
								break;
							}
						if(bfound)
							break;
					}
					if (!bfound){
						seenResults.add(result);
						writer.write(line + "\t" + readersList.get(i).key()
								+ "\n");
				}
				}
			}
		}
		for (Pair<Integer, BufferedReader> r : readersList)
			r.value().close();
		writer.close();
	}

	public static void mergeFilesWithFreq(int targetTermId, int[] idList,String inputFolder, String outpuFolder) throws IOException {
		Vector<String> seenResults = new Vector<String>();
		Vector<HashSet<Integer>> idsVector = new Vector<HashSet<Integer>>();
			System.out.println("writing merged file: " + outpuFolder + "/"
					+ targetTermId + "_Dice.dataGroups.txt");
			BufferedWriter writer = new BufferedWriter(new FileWriter(outpuFolder
					+ "/" + targetTermId + "_Dice.dataGroups.txt"));
			ArrayList<Pair<Integer, BufferedReader>> readersList = new ArrayList<Pair<Integer, BufferedReader>>();
			for (int id : idList) {
				File idFile = new File(inputFolder + "/" + id
						+ "_Dice.dataGroups.txt");
				if (idFile.exists()) {
					BufferedReader reader = new BufferedReader(new FileReader(
							inputFolder + "/" + id + "_Dice.dataGroups.txt"));
					readersList.add(new Pair<Integer, BufferedReader>(id, reader));
				}
			}
			MWEDistance dist = new MWEDistance();
			// merge a line from each file, avoid duplicate lines
			int activeCounter = readersList.size();
			while (activeCounter > 0) {
				activeCounter = readersList.size();
				for (int i = 0; i < readersList.size(); i++) {
					String line = readersList.get(i).value().readLine();
					if (line == null)
						activeCounter--;
					else {
						String result = line.split("\t")[0];
						HashSet<String> resultSet = StringUtils
								.convertStringToSet(result);
						boolean bfound = false;
						int foundIndex = -1;
						for (String l : resultSet){
							for( int index=0;index<seenResults.size();index++){
								String seen = seenResults.get(index);
								if (dist.distance(seen, l) == 0){
									bfound = true;
									foundIndex = index;
									break;
								}
							}
							if(bfound)
								break;
						}
						if (!bfound){
							seenResults.add(result);
							HashSet<Integer> idsSet = new HashSet<Integer>();
							idsSet.add(readersList.get(i).key());
							idsVector.add(idsSet);
						} else{
							String res = seenResults.get(foundIndex);
							HashSet<String> resSet = StringUtils.convertStringToSet(res);
							resSet.addAll(StringUtils.convertStringToSet(result));
							seenResults.set(foundIndex, StringUtils.convertSetToString(resSet));
							idsVector.get(foundIndex).add(readersList.get(i).key());
					}
				}
			}
			}
			for(int index=0; index<idsVector.size();index++)
				writer.write(seenResults.get(index)+"\t"+idsVector.get(index).size()+"\n");
			for (Pair<Integer, BufferedReader> r : readersList)
				r.value().close();
			writer.close();
		}


	
	public static void testMWEgroupingBeforeMerging() throws IOException {
		// group MWE
//		MWEGrouping grouping = new MWEGrouping();
//		File clusterFolder = new File(m_mergingFolder);
//		for (File f : clusterFolder.listFiles())
//			if (f.getName().endsWith(".dataClusters.txt")) {
//				grouping.groupMWEfile(f.getAbsolutePath(), 0.0001, true);
//			}

		// merge files
		for (int id : m_mergeMap.keySet())
//			mergeFiles(id, m_mergeMap.get(id), m_mergingFolder,
//					"C:\\Documents and Settings\\HZ\\Desktop\\afterMerging\\");
//		mergeFiles(id, m_mergeMap.get(id), m_mergingFolder,
//		"/home/ir/liebesc/QEsys/afterMerging/");
		mergeFilesWithFreq(id, m_mergeMap.get(id), m_mergingFolder,
				"/home/ir/liebesc/QEsys/afterMergingFreq/");
	}

	public static void testMWEgroupingAfterMerging() throws IOException {
		// group MWE
		MWEGroupingExpCount grouping = new MWEGroupingExpCount();
		File clusterFolder = new File(m_mergingFolder);
		for (File f : clusterFolder.listFiles())
			if (f.getName().endsWith(".dataClusters.txt")) {
				grouping.groupMWEfile(f.getAbsolutePath(), 0.0001, true);
			}
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			System.out.println("Init merging map");
			initMergeMap();
			System.out.println("Start testMWEgroupingBeforeMerging ");
//			testMWEgroupingBeforeMerging();
			testMWEgroupingAfterMerging();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
