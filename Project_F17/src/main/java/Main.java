import java.io.IOException;
import org.apache.solr.client.solrj.SolrServerException;

public class Main {
	
	public static void main(String[] args) throws SolrServerException, IOException {
		String query = "Bananas remained one of the chain's top selling products because only high-quality specimens";
		String indexLabels = "WORD,LEMMA,STEM,POS,HYPERNYM,HYPONYM,HOLONYM,MERONYM,HEAD";
		String trainingFile = "training.txt"; 
		int maxIter = 4;
		double learningRate = 1.0;
		
		// Task 2
		System.out.println("**** Task 2 ****");
		Searcher task2 = new Searcher("task2",indexLabels);
		task2.query(query);
		System.out.println("**** End ****");
		
		// Task 3
		System.out.println("**** Task 3 ****");
		Searcher task3 = new Searcher("rural",indexLabels);
		task3.query(query,true);
		System.out.println("**** End ****");
		
		// Task 4
		/* System.out.println("**** Task 4 ****");
		Searcher task4 = new Searcher("rural",indexLabels);
		DataSet trainingSet = new DataSet(trainingFile);
		task4.train(trainingSet, maxIter, learningRate);
		System.out.println(task4);
		task4.query(query,true,true);
		System.out.println("**** End ****"); */
	}
}
