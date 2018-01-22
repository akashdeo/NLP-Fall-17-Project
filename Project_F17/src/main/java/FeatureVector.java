import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.TreeMap;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.ModCollinsHeadFinder;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;

public class FeatureVector {
	private TreeMap<String,String> userLabels;
	private HashMap<String,ArrayList<String>> labels;
	private AssociativeVector weights;
	private ArrayList<String> words;
	private ArrayList<String> lemmas;
	private ArrayList<String> stems;
	private ArrayList<String> partOfSpeechTags;
	private ArrayList<String> hypernyms;
	private ArrayList<String> hyponyms;
	private ArrayList<String> holonyms;
	private ArrayList<String> meronyms;
	private ArrayList<String> head;
	
	// constructors
	public FeatureVector() {
		setLabels("words","lemmas","stems","partOfSpeechTags","hypernyms","hyponyms","holonyms","meronyms", "head");
		reset();
	}
	
	public FeatureVector(String sentence) throws FileNotFoundException, IOException {
		this();
		this.setFeatures(sentence);
	}
	
	public FeatureVector(String sentence, String weights) throws FileNotFoundException, IOException {
		this();
		this.setFeatures(sentence);
		this.weights = new AssociativeVector("words,lemmas,stems,partOfSpeechTags,hypernyms,hyponyms,holonyms,meronyms,head",
				weights,",");
	}
	
	public FeatureVector(String sentence, AssociativeVector v) throws FileNotFoundException, IOException {
		this();
		this.setFeatures(sentence);
		this.setWeights(v);
	}
	
	// getters
	public int getSize() {
		return labels.size();
	}
	
	public List<String> getLabels() {
		return new ArrayList<String>(userLabels.keySet());
	}
	
	public AssociativeVector getWeights() {
		AssociativeVector w = new AssociativeVector();
		for(String label: userLabels.keySet()) {
			String actualLabel = userLabels.get(label);
			w.put(label, weights.get(actualLabel));
		}
		return w;
	}
	
	public AssociativeVector getWeightsRaw() {
		return new AssociativeVector(weights);
	}
	
	public ArrayList<String> get(String label) {
		String actualLabel = userLabels.get(label);
		if(actualLabel == null)
			return null;
		
		ArrayList<String> feature = labels.get(actualLabel);
		if(feature != null)
			return new ArrayList<String>(feature);
		else
			return null;
	}
	
	public double getWeight(String label) {
		String actualLabel = userLabels.get(label);
		if(actualLabel == null)
			return 0.0;
		
		return weights.get(actualLabel);
	}
	
	// setters
	public void setLabels(String labelString) {
		String[] labels = labelString.split(",");
		if(labels.length!=9)
			return;
		setLabels(labels[0],labels[1],labels[2],labels[3],labels[4],labels[5],labels[6],labels[7],labels[8]);
	}
	
	public void setLabels(String lWords,
							String lLemmas, 
							String lStems, 
							String lPartOfSpeechTags,
							String lHypernyms,
							String lHyponyms,
							String lHolonyms,
							String lMeronyms,
							String lHead) {
		userLabels =  new TreeMap<String,String>();
		userLabels.put(lWords, "words");
		userLabels.put(lLemmas, "lemmas");
		userLabels.put(lStems, "stems");
		userLabels.put(lPartOfSpeechTags, "partOfSpeechTags");
		userLabels.put(lHypernyms, "hypernyms");
		userLabels.put(lHyponyms, "hyponyms");
		userLabels.put(lHolonyms, "holonyms");
		userLabels.put(lMeronyms, "meronyms");
		userLabels.put(lHead, "head");
	}
	
	public void setFeatures(String query) throws FileNotFoundException, IOException {
		reset();
		Properties props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, pos, lemma, parse");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		
		Annotation document = new Annotation(query);
		pipeline.annotate(document);
		
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
		
		for(int sIndex=0; sIndex < sentences.size(); sIndex ++ ) {
			CoreMap sentence = sentences.get(sIndex);
			String rawSentence = toRawSentence(sentence);
			List<CoreLabel> tokens = sentence.get(TokensAnnotation.class);
			for(CoreLabel token: tokens) {
				// get basic features
				words.add(token.get(TextAnnotation.class));
				lemmas.add(token.get(LemmaAnnotation.class));
				stems.add(new Stemmer().stem(token.get(TextAnnotation.class)));
				partOfSpeechTags.add(token.get(PartOfSpeechAnnotation.class));
			
				// get wordnet features
				HashMap<String, ArrayList<String>> synset = SynsetsFromWordnet
					.getSynsetsFromWordnet(rawSentence, token.get(LemmaAnnotation.class), token.get(PartOfSpeechAnnotation.class));
			
				if(synset.get("hypernyms") != null)
					hypernyms.addAll(synset.get("hypernyms"));
				if(synset.get("hyponyms") != null)
					hyponyms.addAll(synset.get("hyponyms"));
				if(synset.get("holonyms") != null)
					holonyms.addAll(synset.get("holonyms"));
				if(synset.get("meronyms") != null)
					meronyms.addAll(synset.get("meronyms"));
			}
		
			// get head word
			ModCollinsHeadFinder headFinder = new ModCollinsHeadFinder();
			Tree parseTree = sentence.get(TreeAnnotation.class);
			head.add(parseTree.headTerminal(headFinder).toString());
		}
	}
	
	public void setWeights(AssociativeVector w) {
		for(String label: w.keySet()) {
			String actualLabel = userLabels.get(label);
			if(actualLabel == null)
				continue;
			weights.put(actualLabel, w.get(label));
		}
	}
	
	public void setWeightsRaw(AssociativeVector w) {
		weights = new AssociativeVector(w);
	}
	
	public void setWeights(String w) {
		weights = new AssociativeVector("words,lemmas,stems,partOfSpeechTags,hypernyms,hyponyms,holonyms,meronyms,head",
				w,",");
	}
	
	// utils
	private void reset() {
		words = new ArrayList<String>();
		lemmas = new ArrayList<String>();
		stems = new ArrayList<String>();
		partOfSpeechTags = new ArrayList<String>();
		hypernyms = new ArrayList<String>();
		hyponyms = new ArrayList<String>();
		holonyms = new ArrayList<String>();
		meronyms = new ArrayList<String>();
		head = new ArrayList<String>();
		resetLabels();
		resetWeights();
	}
	
	private void resetWeights() {
		weights = new AssociativeVector("words,lemmas,stems,partOfSpeechTags,hypernyms,hyponyms,holonyms,meronyms,head",
										"1,1,1,1,1,1,1,1,1",
										",");
	}
	
	private void resetLabels() {
		labels =  new HashMap<String,ArrayList<String>>();
		labels.put("words", words);
		labels.put("lemmas", lemmas);
		labels.put("stems", stems);
		labels.put("partOfSpeechTags", partOfSpeechTags);
		labels.put("hypernyms", hypernyms);
		labels.put("hyponyms", hyponyms);
		labels.put("holonyms", holonyms);
		labels.put("meronyms", meronyms);
		labels.put("head", head);
	}
	
	private String toRawSentence(CoreMap sentence) {
		String rawSentence = "";
		List<CoreLabel> tokens = sentence.get(TokensAnnotation.class);
		
		for(CoreLabel token: tokens) {
			rawSentence += token.get(LemmaAnnotation.class) + " ";
		}
		
		return rawSentence;
	}
	
	public String toString() {
		return "\nWords: " + words + 
				"\nLemmas: " + lemmas +
				"\nStems: " + stems +
				"\nPart-of-Speech Tags: " + partOfSpeechTags +
				"\nHypernyms: " + hypernyms +
				"\nHyponyms: " + hyponyms +
				"\nHolonyms: " + holonyms +
				"\nMeronyms: " + meronyms +
				"\nHead: " + head +
				"\n";
	}
}
