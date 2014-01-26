package run;

import lexicalResources.WikiSAXHandler;
import edu.jhu.nlp.wikipedia.*;

/**
 * Wiktionary file generator
 * @author HZ
 * @see <a href="http://dumps.wikimedia.org/hewiktionary/">Hebrew Wiktionary Dumps</a>
 *
 */
public class WikiParser {

	
	/**
	 * Parses relations from Wiktionary
	 * @param args args[0] Wiktionary dump file: hewiktionary-XXXXXXXX-pages-articles.xml.bz2 <br>
	 * 			   args[1] output file
	 */
	        public static void main(String[] args) {
	               
	                if(args.length != 2) {
	                        System.err.println("Usage: Parser <XML-FILE> <output-file>");
	                        System.exit(-1);
	                }
	               
	                System.out.println("input: "+args[1]);
	                PageCallbackHandler handler = new WikiSAXHandler(args[1]);
	               
	                System.out.println("output: "+args[0]);
	                WikiXMLParser wxsp = WikiXMLParserFactory.getSAXParser(args[0]);
	               
	                try {
	                        wxsp.setPageCallback(handler);
	                        wxsp.parse();
	                }catch(Exception e) {
	                        e.printStackTrace();
	                }
	        }
	




	

}
