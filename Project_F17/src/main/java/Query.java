import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;

public class Query {
	protected String queryString;
	protected SolrDocumentList results;
	protected int rows;

	public Query() {
		this(10);
	}
	
	public Query(int r) {
		this("",r);
	}

	public Query(String query, int r) {
		queryString = query;
		results = null;
		rows = r;
	}
	
	public void setNumRows(int r) {
		rows = r;
	}

	public void setQueryString(String query) {
		queryString = query;
	}

	public String getQueryString() {
		return queryString;
	}

	public SolrDocumentList getResults() {
		return results;
	}
	
	public int getNumRows() {
		return rows;
	}

	public boolean isEmptyQuery() {
		return queryString.equals("");
	}

	public boolean isEmptyResults() {
		return (results == null);
	}

	protected String formatter(List<String> list, int[] formats) {
		String result = "";

		for (int i = 0; i < list.size(); i++) {
			result = result + String.format("%" + formats[i] + "s", list.get(i));
		}

		return result;
	}

	protected static Vector<String> tokenize(String sentence) {
		String[] words = sentence.split(" ");
		Vector<String> vector = new Vector<String>();
		for (String w : words) {
			if (!w.equals("")) {
				vector.add(w.replace("\"", "").replace(";", "").replaceAll(":", "").replaceAll(",", "").replaceAll("'",
						""));
			}

		}
		return vector;
	}

	public static String generateQueryString(String field, String sentence) {
		Vector<String> tokens = tokenize(sentence);
		String query = field + ":(";
		String values = "";

		for (String token : tokens) {
			// values = values + token + " and ";
			values = values + token + " ";
		}

		// return query + values.substring(0, values.length() - 4) + ")";
		return query + values + ")";
	}
	
	public static String generateQueryString(FeatureVector f, String feature) {
		String query = "";
		
		ArrayList<String> tokens = f.get(feature);
		
		if(tokens.size()==0)
			return query;
		
		query = "(" + feature + ":(";
		for (String token: tokens) {
			// query = query + token + " and ";
			query = query + token + " ";
		}
		
		// return query.substring(0, query.length() - 4) + "))^" + f.getWeight(feature) +" ";
		return query + "))^" + f.getWeight(feature) +" ";
	}
	
	public static String generateQueryString(FeatureVector f) {
		String query = "";
		List<String> features = f.getLabels();

		for(String feature: features) {
			query += generateQueryString(f,feature);
		}
		return query;
	}
	
	public void executeQuery(String core,String fl,int[] formats) throws SolrServerException, IOException {
		SolrDocumentList results = executeQueryRaw(core,fl);
		if(results!=null)
			printResponse(results,fl,formats);
	}
	
	public SolrDocumentList executeQueryRaw(String core, String fl) throws SolrServerException, IOException {
		return executeQueryRaw(core,fl,"");
	}
	
	public SolrDocumentList executeQueryRaw(String core, String fl, String fq) throws SolrServerException, IOException {
		if (isEmptyQuery()) {
			System.out.println("Error! Query field is empty!");
		}
		
		String urlString = "http://localhost:8983/solr/" + core;
		SolrClient solr = new HttpSolrClient.Builder(urlString).build();
		SolrQuery solrQuery = new SolrQuery().setQuery(queryString);
		
		if(!fq.isEmpty())
			solrQuery.set("fq", fq);
		solrQuery.set("fl", fl);
		solrQuery.set("rows", rows);
		
		QueryResponse rsp = solr.query(solrQuery);
		SolrDocumentList list = rsp.getResults();
		return list;
	}

	public void printResponse(SolrDocumentList results, String fl, int[] formats) {
		System.out.println(formatter(Arrays.asList(fl.split(",")), formats));
		for (int i = 0; i < results.size(); i++) {
			ArrayList<String> fieldVals = new ArrayList<String>();
			for (String field : fl.split(",")) {
				fieldVals.add(results.get(i).getFieldValues(field).toString());
			}
			System.out.println(formatter(fieldVals, formats));
		}
	}
}
