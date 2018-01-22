import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

import edu.stanford.nlp.util.StringUtils;

public class AssociativeVector {
	private TreeMap<String,Double> vector;
	
	// constructors
	public AssociativeVector() {
		vector = new TreeMap<String,Double>();
	}
	
	public AssociativeVector(HashMap<String,Double> v) {
		vector = new TreeMap<String,Double>(v);
	}
	
	public AssociativeVector(AssociativeVector v) {
		vector = new TreeMap<String,Double>(v.vector);
	}
	
	public AssociativeVector(String keys) {
		this(keys,",");
	}
	
	public AssociativeVector(String keys, String delim) {		
		this();
		String[] keyList = keys.split(delim);
		
		for(int i=0;i<keyList.length;i++) {
			vector.put(keyList[i], 0.0);
		}
	}
	
	public AssociativeVector(String keys, String values, String delim) {
		this(keys,delim);
		
		String[] keyList = keys.split(delim);
		String[] valueList = values.split(delim);
		
		int endIndex = keyList.length;
		if(valueList.length < endIndex)
			endIndex = valueList.length;
		
		for(int i=0;i<endIndex;i++) {
			vector.put(keyList[i], Double.parseDouble(valueList[i]));
		}
	}
	
	// getters
	public double get(String key) {
		if(vector.get(key)==null)
			return 0.0;
		else
			return vector.get(key);
	}
	
	public Set<String> keySet() {
		return vector.keySet();
	}
	
	public String keyString() {
		return StringUtils.join(keySet(),",");
	}
	
	public ArrayList<Double> valueList() {
		ArrayList<Double> valueList = new ArrayList<Double>();
		for(String key: keySet()) {
			valueList.add(get(key));
		}
		return valueList;
	}
	
	public String valueString() {
		return StringUtils.join(valueList(),",");
	}
	
	public int size() {
		return vector.size();
	}
	
	// setters
	public void put(String key, double value) {
		vector.put(key, value);
	}	
	
	public void plusOne(String key) {
		increment(key,1);
	}
	
	public void minusOne(String key) {
		decrement(key,1);
	}
	
	public void increment(String key, double value) {
		double oldValue = get(key);
		put(key,oldValue + value);
	}
	
	public void decrement(String key,int value) {
		double oldValue = get(key);
		put(key,oldValue - value);
	}
	
	// removers
	public double remove(String key) {
		return vector.remove(key);
	}
	
	// utils
	public boolean isEmpty() {
		return vector.isEmpty();
	}
	
	public String maxKey() {
		return maxOrMinKey(true);
	}
	
	public String minKey() {
		return maxOrMinKey(false);
	}
	
	private String maxOrMinKey(boolean max) {
		if(isEmpty())
			return "";
		String maxOrMinKey = vector.keySet().iterator().next();
		for(String key: vector.keySet()) {
			if( (max && vector.get(key)>vector.get(maxOrMinKey)) || (!max && vector.get(key)<vector.get(maxOrMinKey)) )
				maxOrMinKey = key;
		}
		return maxOrMinKey;
	}
	
	public double sum() {
		double sum = 0.0;
		
		for(String key: vector.keySet()) {
			sum += vector.get(key);
		}
		
		return sum;
	}
	
	public double mean() {
		if(isEmpty())
			return 0.0;
		
		return sum()/size();
	}
	
	public double absMax() {
		double max = vector.get(maxKey());
		double min = vector.get(minKey());
		if(Math.abs(min)>Math.abs(max))
			return Math.abs(min);
		return Math.abs(max);
	}
	
	public static AssociativeVector add(AssociativeVector v1, AssociativeVector v2) {
		return addOrSubtract(v1,v2,true);
	}
	
	public static AssociativeVector subtract(AssociativeVector v1, AssociativeVector v2) {
		return addOrSubtract(v1,v2,false);
	}
	
	private static AssociativeVector addOrSubtract(AssociativeVector v1, AssociativeVector v2, boolean add) {
		Set<String> keys = new HashSet<String>(v1.keySet());
		keys.addAll(v2.keySet());
		AssociativeVector v3 = new AssociativeVector();
		
		for(String key: keys) {
			if(add)
				v3.put(key, v1.get(key) + v2.get(key));
			else
				v3.put(key, Math.abs(v1.get(key) - v2.get(key)));
		}
		
		return v3;
	}
	
	public void multiplyScalar(double scalar) {
		for(String key: vector.keySet()) {
			double value = vector.get(key);
			vector.put(key, value*scalar);
		}
	}
	
	public void divideScalar(double scalar) {
		multiplyScalar(1/scalar);
	}
	
	public void normalize() {
		double absMax = absMax();
		if(absMax!=0.0)
			divideScalar(absMax);
	}
	
	// toString
	public String toString() {
		String vectorString = "";
		for(String key: vector.keySet()) {
			vectorString += key + " : " + vector.get(key) + " , ";
		}
		return vectorString.substring(0,vectorString.length()-1);
	}
}
