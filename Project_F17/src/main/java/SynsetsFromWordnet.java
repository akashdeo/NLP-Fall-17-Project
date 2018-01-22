import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.ISynsetID;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.item.Pointer;

public class SynsetsFromWordnet {

	// keeps all the stop words
	private static ArrayList<String> stopWords;

	/**
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private static void getStopWords() throws FileNotFoundException, IOException {
		stopWords = new ArrayList<String>();
		File file = new File("StopWords.txt");
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			for (String line; (line = reader.readLine()) != null;) {
				stopWords.add(line.trim());
			}
		} catch (Exception e) {
			System.out.println("File not found or error parsing the file!");
		}
	}

	/**
	 * 
	 * @param signature
	 * @param context
	 * @return int
	 */
	private static int computeOverlap(ArrayList<String> signature, ArrayList<String> context) {
		int overlapCount = 0;

		for (String cs : context) {
			for (String ss : signature) {
				if (cs.equals(ss)) {
					overlapCount++;
				}
			}
		}

		return overlapCount;
	}

	/**
	 * 
	 * @param inputSentence
	 * @param inputWord
	 * @param pos
	 * @return String
	 * @throws IOException
	 */
	private static HashMap<String, ArrayList<String>> leskAlgorithm(String inputSentence, String inputWord, POS posTag)
			throws IOException {

		// get the most frequent sense of the word
		String correctSense;
		IWord correctWord = null;
		HashMap<String, ArrayList<String>> synsetForWord = new HashMap<>();

		String path = "./WordNetDict_3.1";
		URL url = new URL("file", null, path);
		IDictionary dict = new Dictionary(url);
		dict.open();

		IIndexWord indexWord = dict.getIndexWord(inputWord, posTag);

		if (indexWord != null) {

			// get the first sense (indicated by 0) as the correct/best sense
			IWordID wordID = indexWord.getWordIDs().get(0);
			IWord word = dict.getWord(wordID);

			correctSense = word.getSynset().getGloss().split(";")[0];
			correctWord = word;

			// max-overlap
			int maxOverlap = 0;

			// context is the set of words in the sentence, where only content words are
			// added
			ArrayList<String> context = new ArrayList<String>();

			StringTokenizer tokenizer = new StringTokenizer(inputSentence);
			while (tokenizer.hasMoreTokens()) {
				String token = tokenizer.nextToken().replace(".", "");
				if (!stopWords.contains(token.toLowerCase()) && !token.equals(inputWord)) {
					context.add(token);
				}
			}

			// loop through all the senses to find the correct/best sense
			for (int sense = 0; sense < indexWord.getWordIDs().size(); sense++) {

				// fill signature for the current sense
				ArrayList<String> signature = new ArrayList<String>();
				IWordID wordIDForSense = indexWord.getWordIDs().get(sense);
				IWord wordForSense = dict.getWord(wordIDForSense);

				String glossAndExamplesPruned = wordForSense.getSynset().getGloss().replaceAll("\\(", "")
						.replaceAll("\\)", "").replaceAll(";", "").replaceAll("\"", "");

				StringTokenizer senseTokenizer = new StringTokenizer(glossAndExamplesPruned);
				while (senseTokenizer.hasMoreTokens()) {
					String senseToken = senseTokenizer.nextToken();
					if (!stopWords.contains(senseToken.toLowerCase()) && !senseToken.equals(inputWord)) {
						signature.add(senseToken);
					}
				}

				// compute the overlap for the current sense
				int overlap = computeOverlap(signature, context);

				if (overlap > maxOverlap) {
					maxOverlap = overlap;
					correctSense = wordForSense.getSynset().getGloss().split(";")[0];
					correctWord = wordForSense;
				}
			}

			ISynset synset = correctWord.getSynset();

			// get hypernyms
			ArrayList<String> hypernyms = new ArrayList<>();
			List<ISynsetID> hypernymSynsetIds = synset.getRelatedSynsets(Pointer.HYPERNYM);
			if (!hypernymSynsetIds.isEmpty()) {
				for (ISynsetID iSynsetId : hypernymSynsetIds) {
					List<IWord> iWords = dict.getSynset(iSynsetId).getWords();
					for (IWord iWord : iWords) {
						String lemma = iWord.getLemma();
						hypernyms.add(lemma.replace(' ', '_')); // also get rid of spaces
					}
				}
			}

			synsetForWord.put("hypernyms", hypernyms);

			// get hyponyms
			ArrayList<String> hyponyms = new ArrayList<>();
			List<ISynsetID> hyponymSynsetIds = synset.getRelatedSynsets(Pointer.HYPONYM);
			if (!hyponymSynsetIds.isEmpty()) {
				for (ISynsetID iSynsetId : hyponymSynsetIds) {
					List<IWord> iWords = dict.getSynset(iSynsetId).getWords();
					for (IWord iWord : iWords) {
						String lemma = iWord.getLemma();
						hyponyms.add(lemma.replace(' ', '_')); // also get rid of spaces
					}
				}
			}

			synsetForWord.put("hyponyms", hyponyms);

			// get holonyms
			ArrayList<String> holonyms = new ArrayList<>();
			List<ISynsetID> holonymSynsetIds = synset.getRelatedSynsets(Pointer.HOLONYM_MEMBER);
			if (!holonymSynsetIds.isEmpty()) {
				for (ISynsetID iSynsetId : holonymSynsetIds) {
					List<IWord> iWords = dict.getSynset(iSynsetId).getWords();
					for (IWord iWord : iWords) {
						String lemma = iWord.getLemma();
						holonyms.add(lemma.replace(' ', '_')); // also get rid of spaces
					}
				}
			}

			synsetForWord.put("holonyms", holonyms);

			// get meronyms
			ArrayList<String> meronyms = new ArrayList<>();
			List<ISynsetID> meronymSynsetIds = synset.getRelatedSynsets(Pointer.MERONYM_MEMBER);
			if (!meronymSynsetIds.isEmpty()) {
				for (ISynsetID iSynsetId : meronymSynsetIds) {
					List<IWord> iWords = dict.getSynset(iSynsetId).getWords();
					for (IWord iWord : iWords) {
						String lemma = iWord.getLemma();
						meronyms.add(lemma.replace(' ', '_')); // also get rid of spaces
					}
				}
			}

			synsetForWord.put("meronyms", meronyms);

			dict.close();
		}

		return synsetForWord;
	}

	/**
	 * 
	 * @return ArrayList<ArrayList<String>>
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public static HashMap<String, ArrayList<String>> getSynsetsFromWordnet(String sentence, String word, String pos)
			throws FileNotFoundException, IOException {

		HashMap<String, ArrayList<String>> synset = new HashMap<>();

		getStopWords();

		Character posChar = pos.charAt(0);

		if (posChar == 'A' || posChar == 'N' || posChar == 'R' || posChar == 'V') {
			synset = leskAlgorithm(sentence, word, POS.getPartOfSpeech(posChar));
		}

		return synset;
	}
}