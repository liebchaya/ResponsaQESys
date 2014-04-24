package test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashSet;

import utils.FileUtils;

public class GetNextTargetTerms {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		String fullTT = "C:\\Documents and Settings\\HZ\\Desktop\\thesaurus terms\\CleanTestSet.txt";
		String alreadyTT = "C:\\Documents and Settings\\HZ\\Desktop\\thesaurus terms\\targetTermsPrev.txt";
		String newTT = "C:\\Documents and Settings\\HZ\\Desktop\\thesaurus terms\\targetTerms4run.txt";
		HashSet<String> alreadySet = FileUtils.loadFileToSet(new File(alreadyTT));
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(newTT), "UTF-8"));
		BufferedReader reader = new BufferedReader(new FileReader(fullTT));
		String line = reader.readLine();
		while(line != null){
			String t = line.split("\t")[0];
			if(!alreadySet.contains(t))
				writer.write(t+"\n");
			line = reader.readLine();
		}
		reader.close();
		writer.close();
	}

}
