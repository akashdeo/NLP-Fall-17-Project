import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocumentList;

public class Searcher {
	private String core;
	private String labels;
	private AssociativeVector weights;
	private int numRows;
	private static String systemLabels = "words,lemmas,stems,partOfSpeechTags,hypernyms,hyponyms,holonyms,meronyms,head";
	private static String defaultWeights = "1,1,1,1,1,1,1,1,1";
	private static String fl = "ART_SENT,score,WORD";
	private static int formats[] = { 20, 20, 100 };
	
	// constructors
	public Searcher(String core) {
		this(core,systemLabels);
	}
	
	public Searcher(String core, String labels) {
		this(core,labels,new AssociativeVector(labels,defaultWeights,","));
	}
	
	public Searcher(String core, String labels, AssociativeVector weights) {
		this(core,labels,weights,10);
	}
	
	public Searcher(String core, String labels, int rows) {
		this(core,labels,new AssociativeVector(labels,defaultWeights,","),rows);
	}
	
	public Searcher(String core, String labels, AssociativeVector weights, int numRows) {
		this.core = core;
		this.labels = labels;
		this.weights = new AssociativeVector(weights);
		this.numRows = numRows;
	}
	
	// getters
	public String getCore() {
		return core;
	}
	
	// setters
	public void setCore(String core) {
		this.core = core;
	}
	
	// core methods
	public void query(String sentence) throws FileNotFoundException, IOException, SolrServerException {
		query(sentence,false);
	}
	
	public void query(String sentence, boolean advanced) throws FileNotFoundException, IOException, SolrServerException {
		query(sentence,advanced,false);
	}
	
	public void query(String sentence, boolean advanced, boolean useWeights) throws FileNotFoundException, IOException, SolrServerException {
		Query q = new Query(numRows);
		String queryString;
		
		if(!advanced)
			queryString = Query.generateQueryString("WORD",sentence);
		else {
			FeatureVector f = new FeatureVector(sentence);
			f.setLabels(labels);
			if(useWeights)
				f.setWeights(weights);
			queryString = Query.generateQueryString(f);
		}
		
		System.out.println("Query String: \n"+queryString);
		q.setQueryString(queryString);
		q.executeQuery(core, fl, formats);		
	}
	
	// machine learning	
	public void train(DataSet trainingSet, int maxIter, double learningRate) throws FileNotFoundException, IOException, SolrServerException {
		Query q = new Query();
		FeatureVector f = new FeatureVector("");
		AssociativeVector weights = f.getWeightsRaw();
		String fl = "ART_SENT,score";
		
		for(int j=1;j<=maxIter;j++) {
			System.out.println("Iteration "+j);
			boolean converge = true;
			
			for(int i=0;i<trainingSet.size();i++) {
				String sentence = trainingSet.getFeature(i);
				String correctLabel = trainingSet.getClass(i);
			
				// create a new feature vector for the new query
				// System.out.println("Setting weights for f: "+weights);
				f = new FeatureVector(sentence);
				f.setLabels(labels);
				f.setWeights(weights);
				// System.out.println("Set weights for f: "+f.getWeights());
			
				// check if SolR fetches the same article
				String queryString = Query.generateQueryString(f);
				q.setQueryString(queryString);
				// System.out.println("Query String in train: "+queryString);
				SolrDocumentList results = q.executeQueryRaw(core,fl);
				if(results==null)
					continue;
				String label = trimBrackets(results.get(0).getFieldValues("ART_SENT").toString());
			
				// if the labels match, then continue
				if(label.equals(correctLabel))
					continue;
				
				// if not, then re-adjust the weights
				converge = false;
				// System.out.println("**** Readjusting weights");
				// System.out.println(f.getWeights());
				weights = readjustWeights(f,label,correctLabel,1.0,core);
				// System.out.println("**** Readjusted weights");
				// System.out.println(weights);
			}
			
			if(converge)
				break;
		}
		
		this.weights = weights;
		System.out.println("**** Training complete ****");
		// return weights;
	}
	
	private AssociativeVector readjustWeights(FeatureVector f, String label, String correctLabel, double learningRate, String core) throws SolrServerException, IOException {
		// System.out.println("**** Calculating scores for correct label");
		AssociativeVector score1 = getScoreVector(f,"ART_SENT",correctLabel,core);
		// System.out.println(score1);
		// System.out.println("**** Calculating scores for current label");
		AssociativeVector score2 = getScoreVector(f,"ART_SENT",label,core);
		// System.out.println(score2);
		AssociativeVector newWeights;
		
		// normalize by largest value
		// score1.normalize();
		// score2.normalize();
		
		// get difference and multiply by learning rate;
		newWeights = AssociativeVector.subtract(score1, score2);
		newWeights.multiplyScalar(learningRate);
		newWeights = AssociativeVector.add(f.getWeights(), newWeights);
		
		return newWeights;
	}
	
	private AssociativeVector getScoreVector(FeatureVector f,String idLabel,String id,String core) throws SolrServerException, IOException {
		AssociativeVector score = new AssociativeVector();
		Query q = new Query();
		String queryString;
		SolrDocumentList results;

		for(String feature: f.getLabels()) {
			queryString = Query.generateQueryString(f,feature);
			
			if(queryString.equals("")) {
				score.put(feature, 0.0);
				continue;
			}
			
			q.setQueryString(queryString);
			results = q.executeQueryRaw(core,idLabel+",score",idLabel+":"+id);
			
			if( results==null || results.size()==0 )
				score.put(feature, 0.0);
			else
				score.put(feature, Double.parseDouble(trimBrackets(results.get(0).getFieldValue("score").toString())));
		}
		
		return score;
	}
	
	// utils
	private static String trimBrackets(String str) {
		Matcher m = Pattern.compile("^\\s*\\[([^\\[]+)\\s*\\]$").matcher(str);
		if(m.find())
			return m.group(1);
		else
			return str;
	}
	
	// toString
	public String toString() {
		return "Core: " + core + "\nLabels: " + labels + "\nWeights: " + weights + "\n";
	}
}
