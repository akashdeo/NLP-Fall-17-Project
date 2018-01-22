import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.math3.util.Pair;

public class DataSet {
	private ArrayList<Pair<String,String>> data;
	
	// constructors
	public DataSet() {
		data = new ArrayList<Pair<String,String>>(); 
	}
	
	public DataSet(String filename) throws IOException {
		this();
		readData(filename);
	}
	
	// getters
	public String getFeature(int index) {
		return data.get(index).getSecond();
	}
	
	public String getClass(int index) {
		return data.get(index).getFirst();
	}
	
	// main methods
	public void readData(String filename) throws IOException {
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		InputStream is = classloader.getResourceAsStream(filename);
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		Matcher m;
		
		for (String line; (line = br.readLine()) != null;) {
			m = Pattern.compile("^([0-9]+_[0-9]+)\\s+([^$]+)$").matcher(line);
			if(m.find()) {
				Pair<String,String> temp = new Pair<String,String> (m.group(1), m.group(2));
				data.add(temp);
			}
		}	
	}
	
	// utils
	public int size() {
		return data.size();
	}
	
	public boolean isEmpty() {
		if(size()==0)
			return true;
		return false;
	}
	
	public String toString() {
		String toString = "";
		for(Pair<String,String> p: data) {
			System.out.println(p.getFirst()+"\t\t"+p.getSecond());
		}
		return toString;
	}
}
