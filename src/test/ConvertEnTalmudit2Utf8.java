package test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import ac.biu.nlp.nlp.general.Utils;
import ac.biu.nlp.nlp.general.file.RecursiveFileListIterator;
import utils.FileFilters;
import utils.FileUtils;

public class ConvertEnTalmudit2Utf8 {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws InstantiationException 
	 */
	public static void main(String[] args) throws IOException, InstantiationException {
		String inputDir = "C:\\Documents and Settings\\HZ\\Desktop\\ModernJewish\\Rambam";
		String outputDir = "C:\\Documents and Settings\\HZ\\Desktop\\RambamUtf8";
		RecursiveFileListIterator iter = new RecursiveFileListIterator(new File(inputDir), new FileFilters.TextNoFileFilter());
		BufferedWriter writer = null;
		while(iter.hasNext())
		{
			File currFile = iter.next();
			String encoding = FileUtils.getFileEncoding(currFile);
			String currDocText = FileUtils.loadFileToString(currFile,encoding);
			currDocText = FileUtils.loadFileToString(currFile,encoding);
//			currDocText = currDocText.replaceAll("[\\x07\t\f\n-]", " ");
			currDocText = currDocText.replaceAll("[\\x07\t\f-]", " ");
			currDocText = currDocText.replaceAll("[^אבגדהלוזחטיכגמנסעפצקרשתץףךםןת\'\\s\"]", "");
			writer = new BufferedWriter(new FileWriter(outputDir+"\\"+currFile.getName().replace(".org", ".txt")));
			writer.write(currDocText+"\n");
			writer.close();
		}

	}

}
