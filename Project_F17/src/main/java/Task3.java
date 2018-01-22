import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.*;

public class Task3 {

	public static void main(String[] args) throws IOException, ParserConfigurationException, TransformerException {

		// get the articles from the 'rural' corpus
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		InputStream is = classloader.getResourceAsStream("rural.txt");
		BufferedReader br = new BufferedReader(new InputStreamReader(is));

		// Splitting the corpus into articles
		StringBuilder sb = new StringBuilder();
		for (String line; (line = br.readLine()) != null;) {
			if (line.equals("")) {
				sb.append("-1");
			} else {
				if (line.endsWith(".") || line.endsWith("?")) {
					sb.append(line.trim());
					sb.append(' ');
				} else if (!line.endsWith(".")) {
					sb.append(line.trim());
					sb.append('.');
					sb.append(' ');
				}
			}
		}

		String articles[] = sb.toString().split("-1");

		// creating the XML file here:
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

		// root elements
		Document doc = docBuilder.newDocument();
		Element rootElement = doc.createElement("add");
		doc.appendChild(rootElement);

		// creates a StanfordCoreNLP object, with tokenizer, sentence splitter, POS
		// tagging, lemmatization, and parsing
		Properties props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, pos, lemma, parse");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

		// use any one
		// CollinsHeadFinder headFinder = new CollinsHeadFinder();
		// SemanticHeadFinder headFinder = new SemanticHeadFinder();
		ModCollinsHeadFinder headFinder = new ModCollinsHeadFinder();

		// for (String article : articles) {
		// for (int artNumber = articles.length / 2; artNumber < articles.length; artNumber++) {
		for (int artNumber = 1; artNumber < articles.length; artNumber++) {

			// create an empty Annotation just with the given text
			Annotation document = new Annotation(articles[artNumber]);

			// run all Annotators on this text
			pipeline.annotate(document);

			// these are all the sentences in this document
			// a CoreMap is essentially a Map that uses class objects as keys and has values
			// with custom types
			List<CoreMap> sentences = document.get(SentencesAnnotation.class);

			for (int sentenceNumber = 1; sentenceNumber < sentences.size(); sentenceNumber++) {

				// traversing the words in the current sentence
				List<CoreLabel> tokens = sentences.get(sentenceNumber).get(TokensAnnotation.class);

				// keep each word/token in the sentence
				ArrayList<String> words = new ArrayList<>();

				// keep the lemmas for each word/token in the sentence
				ArrayList<String> lemmas = new ArrayList<>();

				// keep the stems for each word/token in the sentence
				ArrayList<String> stems = new ArrayList<>();

				// keep the part of speech tags for each word/token in the sentence
				ArrayList<String> partOfSpeechTags = new ArrayList<>();

				ArrayList<HashMap<String, ArrayList<String>>> synsetForWords = new ArrayList<>();

				// a CoreLabel is a CoreMap with additional token-specific methods
				for (CoreLabel token : tokens) {

					// Retrieve and add the text for each word/token into the list of words
					String word = token.get(TextAnnotation.class);
					words.add(word);

					// Retrieve and add the lemma for each word into the list of lemmas
					lemmas.add(token.get(LemmaAnnotation.class));

					// Retrieve and add the stem for each word into the list of stems
					Stemmer stemmer = new Stemmer();
					stems.add(stemmer.stem(word));

					// Retrieve and add the POS tag for each word into the list of tags
					String pos = token.get(PartOfSpeechAnnotation.class);
					partOfSpeechTags.add(pos);

					// Retrieve the hypernymns, hyponyms, meronyms, AND holonyms for each word into
					// the list of synsets

					// create the sentence with the lemmas (WordNet expects lemmas of each word in
					// the sentence)
					StringBuilder sbForLemmas = new StringBuilder();
					for (String l : lemmas) {
						sbForLemmas.append(l);
					}

					HashMap<String, ArrayList<String>> synset = SynsetsFromWordnet
							.getSynsetsFromWordnet(sbForLemmas.toString(), token.get(LemmaAnnotation.class), pos); // sentences.get(sentenceNumber).toString()

					synsetForWords.add(synset);
					/*
					 * if(!synset.isEmpty()) { System.out.println();
					 * System.out.println("Hypernyms of " + token.get(LemmaAnnotation.class) +
					 * " are:"); for(String w : synset.get("hypernyms")) { System.out.print(w +
					 * ","); }
					 * 
					 * System.out.println(); System.out.println("Hyponyms of " +
					 * token.get(LemmaAnnotation.class) + " are:"); for(String w :
					 * synset.get("hyponyms")) { System.out.print(w + ","); }
					 * 
					 * System.out.println(); System.out.println("Holonyms of " +
					 * token.get(LemmaAnnotation.class) + " are:"); for(String w :
					 * synset.get("holonyms")) { System.out.print(w + ","); }
					 * 
					 * System.out.println(); System.out.println("Meronyms of " +
					 * token.get(LemmaAnnotation.class) + " are:"); for(String w :
					 * synset.get("meronyms")) { System.out.print(w + ","); } }
					 */
				}

				// store either the head words or the dependency graph in appropriate data
				// structure

				// We can use either head words (USING this now) OR dependency graph

				// this is the parse tree of the current sentence
				Tree tree = sentences.get(sentenceNumber).get(TreeAnnotation.class);
				// head word of the sentence
				String head = tree.headTerminal(headFinder).toString();

				// this is the Stanford dependency graph of the current sentence
				// SemanticGraph dependencies = sentences.get(sentenceNumber)
				// .get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class);
				// System.out.println(dependencies.toString(SemanticGraph.OutputFormat.READABLE));

				/*
				 * System.out.println(); System.out.println("Words are:"); for (String w :
				 * words) { System.out.print(w + " "); }
				 * 
				 * System.out.println(); System.out.println("Lemmas are:"); for (String l :
				 * lemmas) { System.out.print(l + " "); }
				 * 
				 * System.out.println(); System.out.println("Stems are:"); for (String s :
				 * stems) { System.out.print(s + " "); }
				 * 
				 * System.out.println(); System.out.println("POS tags are:"); for (String p :
				 * partOfSpeechTags) { System.out.print(p + " "); }
				 */

				if (!words.isEmpty() && !lemmas.isEmpty() && !stems.isEmpty() && !partOfSpeechTags.isEmpty()
						&& !head.isEmpty() && !head.equals(".")) {
					// Sentence element
					Element sent = doc.createElement("doc");
					rootElement.appendChild(sent);

					// field1 elements
					Element field1 = doc.createElement("field");
					Attr attr1 = doc.createAttribute("name");
					attr1.setValue("ART_SENT");
					field1.setAttributeNode(attr1);

					field1.appendChild(doc.createTextNode((artNumber + 1) + "_" + (sentenceNumber)));
					sent.appendChild(field1);

					// field2 elements - all the words in the sentence
					Vector<String> vector = new Vector<>();
					for (String w : words) {
						if (!w.equals("") && !w.equals("\"") && !w.equals("'") && !w.equals("'\"") && !w.equals(",")
								&& !w.equals("``") && !w.equals(".") && !w.startsWith("'")) {
							vector.add(w.replace("\"", "").replace(";", "").replaceAll(":", "").replaceAll(",", "")
									.replaceAll("'", ""));
						}
					}

					if (!vector.isEmpty()) {
						for (String word : vector) {
							Element field2 = doc.createElement("field");
							Attr attr2 = doc.createAttribute("name");
							attr2.setValue("WORD");
							field2.setAttributeNode(attr2);
							field2.appendChild(doc.createTextNode(word));
							sent.appendChild(field2);
						}
					}

					vector.clear();

					// field3 elements - all the lemmas in the sentence
					for (String l : lemmas) {
						if (!l.equals("") && !l.equals("\"") && !l.equals("'") && !l.equals("'\"") && !l.equals(",")
								&& !l.equals("``") && !l.equals(".") && !l.startsWith("'")) {
							vector.add(l.replace("\"", "").replace(";", "").replaceAll(":", "").replaceAll(",", "")
									.replaceAll("'", ""));
						}
					}

					if (!vector.isEmpty()) {
						for (String lemma : vector) {
							Element field3 = doc.createElement("field");
							Attr attr3 = doc.createAttribute("name");
							attr3.setValue("LEMMA");
							field3.setAttributeNode(attr3);
							field3.appendChild(doc.createTextNode(lemma));
							sent.appendChild(field3);
						}
					}

					vector.clear();

					// field4 elements - all the stems in the sentence
					for (String s : stems) {
						if (!s.equals("") && !s.equals("\"") && !s.equals("'") && !s.equals("'\"") && !s.equals(",")
								&& !s.equals("``") && !s.equals(".") && !s.startsWith("'")) {
							vector.add(s.replace("\"", "").replace(";", "").replaceAll(":", "").replaceAll(",", "")
									.replaceAll("'", ""));
						}
					}

					if (!vector.isEmpty()) {
						for (String stem : vector) {
							Element field4 = doc.createElement("field");
							Attr attr4 = doc.createAttribute("name");
							attr4.setValue("STEM");
							field4.setAttributeNode(attr4);
							field4.appendChild(doc.createTextNode(stem));
							sent.appendChild(field4);
						}
					}

					vector.clear();

					// field5 elements - all the stems in the sentence
					for (String p : partOfSpeechTags) {
						if (!p.equals("") && !p.equals("\"") && !p.equals("'") && !p.equals("'\"") && !p.equals(",")
								&& !p.equals("``") && !p.equals(".") && !p.startsWith("'")) {
							vector.add(p.replace("\"", "").replace(";", "").replaceAll(":", "").replaceAll(",", "")
									.replaceAll("'", ""));
						}
					}

					if (!vector.isEmpty()) {
						for (String pos : vector) {
							Element field5 = doc.createElement("field");
							Attr attr5 = doc.createAttribute("name");
							attr5.setValue("POS");
							field5.setAttributeNode(attr5);
							field5.appendChild(doc.createTextNode(pos));
							sent.appendChild(field5);
						}
					}

					vector.clear();

					// field6 - the head word of the sentence
					Element field6 = doc.createElement("field");
					Attr attr6 = doc.createAttribute("name");
					attr6.setValue("HEAD");
					field6.setAttributeNode(attr6);
					field6.appendChild(doc.createTextNode(head));
					sent.appendChild(field6);

					for (HashMap<String, ArrayList<String>> synset : synsetForWords) {
						ArrayList<String> hypernyms = synset.get("hypernyms");
						ArrayList<String> hyponyms = synset.get("hyponyms");
						ArrayList<String> holonyms = synset.get("holonyms");
						ArrayList<String> meronyms = synset.get("meronyms");

						// field7 - all the hypernyms in the sentence
						if (hypernyms != null && !hypernyms.isEmpty()) {
							for (String hypernym : hypernyms) {
								Element field7 = doc.createElement("field");
								Attr attr7 = doc.createAttribute("name");
								attr7.setValue("HYPERNYM");
								field7.setAttributeNode(attr7);
								field7.appendChild(doc.createTextNode(hypernym));
								sent.appendChild(field7);
							}
						}

						// field8 - all the hyponyms in the sentence
						if (hyponyms != null && !hyponyms.isEmpty()) {
							for (String hyponym : hyponyms) {
								Element field8 = doc.createElement("field");
								Attr attr8 = doc.createAttribute("name");
								attr8.setValue("HYPONYM");
								field8.setAttributeNode(attr8);
								field8.appendChild(doc.createTextNode(hyponym));
								sent.appendChild(field8);
							}
						}

						// field9 - all the holonyms in the sentence
						if (holonyms != null && !holonyms.isEmpty()) {
							for (String holonym : holonyms) {
								Element field9 = doc.createElement("field");
								Attr attr9 = doc.createAttribute("name");
								attr9.setValue("HOLONYM");
								field9.setAttributeNode(attr9);
								field9.appendChild(doc.createTextNode(holonym));
								sent.appendChild(field9);
							}
						}

						// field10 - all the meronyms in the sentence
						if (meronyms != null && !meronyms.isEmpty()) {
							for (String meronym : meronyms) {
								Element field10 = doc.createElement("field");
								Attr attr10 = doc.createAttribute("name");
								attr10.setValue("MERONYM");
								field10.setAttributeNode(attr10);
								field10.appendChild(doc.createTextNode(meronym));
								sent.appendChild(field10);
							}
						}
					}
				}
			}

			// for (CoreMap sentence : sentences) {
			// }
		}

		// write the content into XML file
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		DOMSource source = new DOMSource(doc);

		// use your own file path to save the XML
		StreamResult result = new StreamResult(new File("file_rural_task3_final.xml"));

		transformer.transform(source, result);

		System.out.println("File saved!");

	}
}