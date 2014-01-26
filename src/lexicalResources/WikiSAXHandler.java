package lexicalResources;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import edu.jhu.nlp.wikipedia.PageCallbackHandler;
import edu.jhu.nlp.wikipedia.WikiPage;

/**
 * PageCallbackHandler for Wiktionary pages
 * @author HZ
 *
 */
public class WikiSAXHandler implements PageCallbackHandler {

	
	private BufferedWriter m_writer = null;

	/**
	 * @param output
	 */
	public WikiSAXHandler(String output){
		if (m_writer == null)
	try {
		m_writer = new BufferedWriter(new FileWriter("C:\\wiki\\test"));
	} catch (IOException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
	}
	
	/**
	 * Processes a WikiPage, takes out synonyms, categories and links
	 */
	public void process(WikiPage page) {
		String text = page.getWikiText();
		String[] relations = new String[] { "===מילים נרדפות===",
				"===ראו גם===", "===ראה גם===" };
		for (String rel : relations) {
			String lst = getSpecificRelation(text, rel);
			if (!lst.equals("")) {
				// if(page.getTitle().trim().equals("זחון")){
				String[] split = lst.split(",|\n");
				HashSet<String> relList = new HashSet<String>();
				for (String s : split) {
					for (String str : s.split("\\||#")) {
						if (!str.trim().equals(""))
							relList.add(str.trim());
					}
				}
				for (String s : relList) {
					// add node if necessary
					try {
						if (rel.contains("גם"))
							m_writer.write(page.getTitle().trim() + "\t" + s
									+ "\t2\n");
						else
							m_writer.write(page.getTitle().trim() + "\t" + s
									+ "\t1\n");
						m_writer.flush();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		List<String> catList = getCategories(text);
		for (String cat : catList) {
			try {
				m_writer.write(page.getTitle().trim() + "\t" + cat + "\t3\n");
				m_writer.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

/**
 * Gets a relation from a page
 * @param text page's text
 * @param rel relation name
 * @return string of relations
 */
	public String getSpecificRelation(String text, String rel) {
		String synList = "";
		int index = text.indexOf(rel);
		if (index != -1) {
			synList = text.substring(index + rel.length());
			int endIndex = synList.indexOf("\n\n");
			int endIndex2 = synList.indexOf("===");
			if (endIndex != -1 && endIndex2 != -1)
				synList = synList.substring(0, Math.min(endIndex, endIndex2));
			else {
				if (endIndex2 != -1)
					synList = synList.substring(0, synList.indexOf("==="));
				else if (endIndex != -1)
					synList = synList.substring(0, synList.indexOf("\n\n"));
				else {
					synList = synList.trim();
					endIndex = synList.indexOf("\n");
					if (endIndex != -1) {
						if (synList.charAt(endIndex + 1) == '['
								|| synList.charAt(endIndex + 1) == '{')
							synList = synList.substring(0, synList
									.indexOf("\n"));
					} else {
						endIndex = synList.indexOf("</text>");
						if (endIndex != -1)
							synList.substring(0, synList.indexOf("</text>"));
					}
				}
			}
			synList = synList.replaceAll("\\([0-9]\\)", "");
			synList = synList.replaceAll("\\[|\\]|\\(|\\)|\\*", "").trim();
		}
		return synList;
	}

	/**
	 * Gets categories
	 * @param text page's text
	 * @return list of categories
	 */
	public List<String> getCategories(String text) {
		LinkedList<String> catList = new LinkedList<String>();
		String category = text;
		int index = category.indexOf("[קטגוריה:");
		while (index != -1) { // there is a least one category
			category = category.substring(index + 9);
			int endIndex = category.indexOf(']');
			if (endIndex != -1)
				catList.add(category.substring(0, endIndex).replace(']', ' ').trim());
			else
				catList.add(category.substring(0).replace(']', ' ').trim());
			if (category.length() < endIndex + 2)
				index = -1;
			else {
				category = category.substring(endIndex + 2);
				index = category.indexOf("[קטגוריה:");
			}
		}
		return catList;
	}

}
