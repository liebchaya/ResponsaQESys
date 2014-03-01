package test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import mwe.MWEGrouping;
import net.lingala.zip4j.model.ZipParameters;
import obj.Pair;

import org.apache.lucene.search.ScoreDoc;

import period.NgramsDataGeneration;

import clustering.ClusteringGeneration;

import representation.FeatureRepresentation;
import representation.NgramsFeatureRepresentation;
import representation.NgramsTargetTermRepresentation;
import representation.TargetTermRepresentation;
import representation.FeatureRepresentation.FeatureType;
import representation.TargetTermRepresentation.TargetTermType;
import utils.FileUtils;
import utils.TargetTerm2Id;
import utils.ZipUtils;
import fo.scorers.StatScorer;
import fo.similarity.FeatureVectorExtractor;

import ac.biu.nlp.nlp.general.configuration.ConfigurationFile;
import ac.biu.nlp.nlp.general.configuration.ConfigurationParams;
import annotation.JudgmentsLoader;

public class TestQESys {
	public static void main(String[] args) throws Exception{
		
		String mail = args[2];
		
		ConfigurationFile conf = new ConfigurationFile(new File(args[0]));
		ConfigurationParams params = conf.getModuleConfiguration("Experiment");
		
		String usr = mail.substring(0,mail.indexOf("@"));
		
		String mainDir = params.get("main-dir")+"/"+usr;
		String rawTermFile = params.get("raw-terms-file");
		String targetTermFile = rawTermFile.replace(".txt", "_orig.txt");
		String wiktionaryFile = params.get("wiktionary-file");
		String databaseName = params.get("database-name");
		
		String oldIndex = params.get("old-index");
		
		
		String inputFolder = mainDir+"/input/";
		String outputFolder = mainDir+"/output/";
		String judgmentsFolder = mainDir+"/judgments/";
		String annotationsFolder = mainDir+"/annotated/";
		String thesaurusFolder = mainDir+"/thesaurus/";
		
		File outputDir = new File(outputFolder);
		if (outputDir.exists())
			FileUtils.deleteRecursive(outputDir);
		outputDir.mkdir();
		
		File inputDir = new File(inputFolder);
		if (inputDir.exists())
			FileUtils.deleteRecursive(inputDir);
		inputDir.mkdir();
		
		File judgmentsDir = new File(judgmentsFolder);
		if (judgmentsDir.exists())
			FileUtils.deleteRecursive(judgmentsDir);
		judgmentsDir.mkdir();
		
		File annotatedDir = new File(annotationsFolder);
		if (annotatedDir.exists())
			FileUtils.deleteRecursive(annotatedDir);
		annotatedDir.mkdir();
		
		
		ZipUtils.unzip(args[1], mainDir);
		
		ConfigurationParams foParams = conf.getModuleConfiguration("FO");
		String ngramsIndex = foParams.get("ngrams-index");
		TargetTermType targetType = TargetTermType.Surface;
		String scorerClass =foParams.get("stat-scorer");
		Class<?> cls = Class.forName(scorerClass);
		StatScorer scorer  = (StatScorer) cls.newInstance();
		FeatureType featureType = FeatureType.valueOf(foParams.get("feature-type"));
		FeatureRepresentation featureRp = new NgramsFeatureRepresentation(featureType, ngramsIndex);
		FeatureVectorExtractor vectorExtractor = new FeatureVectorExtractor(scorer,featureRp);
		TargetTermRepresentation targetRp;
		HashMap<String, ArrayList<ScoreDoc>> targetDocs;
		
		String fileType = "_Dice.txt";
		ConfigurationParams clusterParam = conf.getModuleConfiguration("Clustering");
		String taggerDir = clusterParam.get("tagger-dir");
		int topNum = clusterParam.getInt("top-num");
		ClusteringGeneration clsGen;
		
		ConfigurationParams genParam = conf.getModuleConfiguration("DataGeneration");
		String modernIndex = genParam.get("modern-index");
		String oldNgramsFileName = genParam.get("old-ngrams-file");
		String modernJewishNgramsFileName = genParam.get("modernJewish-ngrams-file");
		// index contains up tp 4-grams
		int maxN = 4;
		NgramsDataGeneration ngramsGenerator = new NgramsDataGeneration(oldIndex,ngramsIndex,modernIndex);
		ConfigurationParams ngramParam = conf.getModuleConfiguration("NgramsGeneration");
		double mweThreshold = ngramParam.getDouble("mwe-threshold");
		boolean filterFreqModern = ngramParam.getBoolean("filter-freq-modern");
		
		JudgmentsLoader jLoader = new JudgmentsLoader(databaseName,wiktionaryFile,oldIndex);
		File newInputFile = new File(inputFolder+rawTermFile);
		// Statistics extraction for new terms
		if (newInputFile.exists()) {
			/**
			 *  step 1 - load target terms to database
			 */
			// first we have to process the input file for new target terms
			// insert the new terms to the database and get their ids in the system
			int firstId = jLoader.generateInputFile(new File(inputFolder+rawTermFile), new File(inputFolder+targetTermFile));
			System.out.println("Finish loading new target terms");
			
			/**
			 *  step 2 - run statistical extraction over new target terms
			 */
			
			TargetTerm2Id.loadTargetTerm2IdMapping(new File(inputFolder+targetTermFile));
			// expand target terms with morphology prefixes
			String expTargetTermFile = morphology.Morphology4TargetTermExp.generateMorphExpFile(inputFolder+targetTermFile, ngramsIndex, firstId);
	
			// FO n-grams extraction - from ngrams index
			targetRp = new NgramsTargetTermRepresentation(targetType, expTargetTermFile, ngramsIndex);
			targetDocs = targetRp.extractDocsByRepresentation();
			String termsDirName = vectorExtractor.extractTargetTermVectors(targetDocs, targetType, new File(outputFolder));
			System.out.println("Finish statistics extraction");
			System.out.println("statDir: "+termsDirName);
			
			// cluster FO results
			TargetTerm2Id.loadTargetTerm2IdMapping(new File(expTargetTermFile));
			
			clsGen = new ClusteringGeneration(taggerDir,termsDirName,topNum);
			String clustersDirName = clsGen.clusterDir(fileType, topNum);
			System.out.println("Finish clustring");
			System.out.println("clustersDirName: "+clustersDirName);
			
			// generate clusters data
			ngramsGenerator.generateDataFiles(clustersDirName, expTargetTermFile, oldNgramsFileName, modernJewishNgramsFileName, maxN, true);
			System.out.println("Finish generating clusters data");
			
			// group MWE
			MWEGrouping grouping = new MWEGrouping();
			File clusterFolder = new File(clustersDirName);
			for(File f:clusterFolder.listFiles())
				if (f.getName().endsWith(".dataClusters.txt")){
					grouping.groupMWEfile(f.getAbsolutePath(), mweThreshold, filterFreqModern);
				}
			
			/*
			 * step 3 - load initial results for new target terms
			 */
			TargetTerm2Id.loadTargetTerm2IdMapping(new File(inputFolder+targetTermFile));
	//		String clustersDirName = "C:\\QESys\\output\\Surface_Surface\\clusters50_SUMSCORE_2";
			File statDir = new File(clustersDirName);
			for(File f:statDir.listFiles())
				if (f.getName().endsWith(".dataGroups.txt"))
					jLoader.generateIntialJudgmentFile(f, judgmentsFolder);
			// add wiktionary expansions when there isn't any statistical information
			jLoader.addWikiIntialJudgmentFiles(judgmentsFolder);
			System.out.println("Finish generating new annotation files");
		}
		/*
		 * step 4 -  load annotations
		 */
//		File annotatedDir = new File(annotationsFolder);
		if (annotatedDir.exists()) {
			for(File f:annotatedDir.listFiles())
				if(f.getAbsolutePath().endsWith(".dataGroups"))
					jLoader.loadAnnotations(f);
			System.out.println("Finish loading annotations");
			
			// insert expansions to database
			// generate expansion input file
			
			File expInputFile = new File(inputFolder+"exp_raw.txt");
			if(expInputFile.exists()) {
				File expOutputFile = new File(expInputFile.getAbsolutePath().replace("_raw.txt", "_orig.txt"));
				HashMap<Integer, Pair<Integer, String>> expMap = jLoader.generateExpansionsFile(expInputFile, expOutputFile);
				System.out.println("Finish inserting expansions to the database");
		//		
				/**
				 * step 5 - run statistical extraction over expansions
				 */
				
				TargetTerm2Id.loadTargetTerm2IdMapping(new File(expOutputFile.getAbsolutePath()));
				// expand target terms with morphology prefixes
				String expExpFile = morphology.Morphology4TargetTermExp.generateMorphExpFile(expOutputFile.getAbsolutePath(), ngramsIndex, 0);
		
				// FO n-grams extraction - from ngrams index
				targetRp = new NgramsTargetTermRepresentation(targetType, expExpFile, ngramsIndex);
				targetDocs = targetRp.extractDocsByRepresentation();
				
				String expOutputFolder = outputFolder+"exp/";
				String expDirName = vectorExtractor.extractTargetTermVectors(targetDocs, targetType, new File(expOutputFolder));
				
				// cluster FO results
				TargetTerm2Id.loadTargetTerm2IdMapping(new File(expExpFile));
				
				clsGen = new ClusteringGeneration(taggerDir,expDirName,topNum);
				String expClustersDirName = clsGen.clusterDir(fileType, topNum);
				
				// generate clusters data
				ngramsGenerator.generateDataFiles(expClustersDirName, expExpFile, oldNgramsFileName, modernJewishNgramsFileName, maxN, false);
				
				
				/**
				 * step 6 - merge files
				 */
				// merge generation results
				HashMap<Integer,ArrayList<Integer>> mergingMap = new HashMap<Integer, ArrayList<Integer>>();
				for(int id:expMap.keySet()){
					int targetTermId = expMap.get(id).key();
					if(mergingMap.containsKey(targetTermId))
						mergingMap.get(targetTermId).add(id);
					else {
						ArrayList<Integer> idList = new ArrayList<Integer>();
						idList.add(id);
						mergingMap.put(targetTermId, idList);
					}
				}
				System.out.println(mergingMap);
				String stepInputFolder = expClustersDirName;
				String stepOutputFolder = expOutputFolder;
				for (int id:mergingMap.keySet())
					jLoader.mergeFiles(id,mergingMap.get(id),stepInputFolder,stepOutputFolder);
				
				// group MWE
				MWEGrouping grouping = new MWEGrouping();
				File clusterFolder = new File(stepOutputFolder);
				for(File f:clusterFolder.listFiles())
					if (f.getName().endsWith(".dataClusters.txt")){
						grouping.groupMWEfile(f.getAbsolutePath(),mweThreshold, filterFreqModern);
					}
			
				
				File stepOutputDir = new File(stepOutputFolder);
				// generate judgment file
				for(File f:stepOutputDir.listFiles())
					if (f.getName().endsWith(".dataGroups.txt")){
						jLoader.exportRunData(f, judgmentsFolder);
						jLoader.printUpdatedGroupsData(judgmentsFolder, f.getName());
					}
		////		
		////		
		////		
		////		
		////	
				jLoader.printThesaurus(thesaurusFolder);
			}
		}	
		HashSet<String> folders = new HashSet<String>();
		folders.add("judgments");
		folders.add("input");
		if (annotatedDir.exists())
			folders.add("thesaurus");
		String zipPath = ZipUtils.zip(mainDir, folders);
//		String command = "mailx -s \"Judgments System\" -a " + zipPath + " liebchaya@gmail.com";
		sendMail(zipPath, mail);
//		System.out.println(command);
//		Runtime.getRuntime().exec(command);
		
	}

	
	private static void sendMail(String attachment, String mail){
	
	// Recipient's email ID needs to be mentioned.
    String to = mail;

    // Sender's email ID needs to be mentioned
    String from = "liebchaya@gmail.com";

    // Assuming you are sending email from localhost
    String host = "localhost";
    

    // Get system properties
    Properties properties = System.getProperties();

    // Setup mail server
    properties.setProperty("mail.smtp.host", host);

    // Get the default Session object.
    Session session = Session.getDefaultInstance(properties);

    try{
       // Create a default MimeMessage object.
       MimeMessage message = new MimeMessage(session);

       // Set From: header field of the header.
       message.setFrom(new InternetAddress(from));

       // Set To: header field of the header.
       message.addRecipient(Message.RecipientType.TO,
                                new InternetAddress(to));

       // Set Subject: header field
       message.setSubject("ממערכת לבניית תזארוס");

       // Create the message part 
       BodyPart messageBodyPart = new MimeBodyPart();

       // Fill the message
       messageBodyPart.setText("מצורף קובץ");
       
       // Create a multipar message
       Multipart multipart = new MimeMultipart();

       // Set text message part
       multipart.addBodyPart(messageBodyPart);

       // Part two is attachment
       messageBodyPart = new MimeBodyPart();
       DataSource source = new FileDataSource(attachment);
       messageBodyPart.setDataHandler(new DataHandler(source));
       messageBodyPart.setFileName(attachment);
       multipart.addBodyPart(messageBodyPart);

       // Send the complete message parts
       message.setContent(multipart );

       // Send message
       Transport.send(message);
       System.out.println("Sent message successfully....");
    }catch (MessagingException mex) {
       mex.printStackTrace();
    }
	}

}
