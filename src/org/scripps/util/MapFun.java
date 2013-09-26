/**
 * 
 */
package org.scripps.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;


/**
 * @author bgood
 *
 */
public class MapFun {

	public static void main(String[] args) {
		Map m = new HashMap();
		m.put("a", "some");
		m.put("b", "random");
		m.put("c", "words");
		m.put("d", "to");
		m.put("e", "be");
		m.put("f", "sorted");
		m.put("g", "by");
		m.put("h", "value");
		for (Object key : sortMapByValue(m)) {
			System.out.printf("key: %s, value: %s\n", key, m.get(key));
		}
	}

	/**
	 * return the keys as a list sorted according to the associated value
	 * @param m
	 * @return
	 */
	public static List sortMapByValue(final Map m) {
		List keys = new ArrayList();
		keys.addAll(m.keySet());
		Collections.sort(keys, new Comparator() {
			public int compare(Object o1, Object o2) {
				Object v1 = m.get(o1);
				Object v2 = m.get(o2);
				if (v1 == null) {
					return (v2 == null) ? 0 : 1;
				}
				else if (v1 instanceof Comparable) {
					return ((Comparable) v1).compareTo(v2);
				}
				else {
					return 0;
				}
			}
		});
		Collections.reverse(keys);
		return keys;
	}

	public static Map<Integer, Double> convertCountsToEmpiricalPtable(Map<Integer, Integer> count_map){
		Map<Integer, Double> count_p = new TreeMap<Integer, Double>();
		int runs = 0;
		for(Entry<Integer,Integer> countin : count_map.entrySet()){
			runs+= countin.getValue();
		}
		for(Entry<Integer,Integer> inmap : count_map.entrySet()){
			int k = inmap.getKey();
			int n_greater = 0;
			for(Entry<Integer,Integer> countin : count_map.entrySet()){
				int cvin = countin.getKey();
				if(cvin>=k){
					n_greater+=countin.getValue();
				}
			}
			count_p.put(k, n_greater/(double)runs);
		}
		return count_p;
	}
	
	public static Map<Object, Double> convertCountsToPercentagesGeneric(Map<Object, Integer> inmap){
		Map<Object, Double> outmap = new HashMap<Object,Double>();
		float sum = 0;
		for(Integer i : inmap.values()){
			sum+= i;
		}
		for(Object key : inmap.keySet()){
			outmap.put(key, (double)inmap.get(key)/sum);
		}
		return outmap;
	}
	
	public static Map<String, Float> convertCountsToPercentages(Map<String, Integer> inmap){
		Map<String, Float> outmap = new HashMap<String, Float>();
		float sum = 0;
		for(Integer i : inmap.values()){
			sum+= i;
		}
		for(String key : inmap.keySet()){
			outmap.put(key, (float)inmap.get(key)/sum);
		}
		return outmap;
	}

	public static Map<String, Set<String>> mergeStringSetMaps(Map<String, Set<String>> map1, Map<String, Set<String>> map2){
		for(Entry<String, Set<String>> in : map1.entrySet()){
			if(map2.keySet().contains(in.getKey())){
				map2.get(in).addAll(map1.get(in.getKey()));
			}else{
				map2.put(in.getKey(), in.getValue());
			}
		}
		return map2;
	}

	public static Map<String, Set<String>> flipMapStringSetStrings(Map<String, Set<String>> inmap){
		Map<String, Set<String>> outmap = new HashMap<String, Set<String>>();
		for(Entry<String, Set<String>> in : inmap.entrySet()){
			if(in!=null&&in.getValue()!=null){
				for(String f : in.getValue()){
					Set<String> fvals = outmap.get(f);
					if(fvals==null){
						fvals = new HashSet<String>();
					}
					fvals.add(in.getKey());
					outmap.put(f, fvals);
				}
			}
		}
		return outmap;
	}

	public static Map<String, Set<String>> flipMapStringListStrings(Map<String, List<String>> inmap){
		Map<String, Set<String>> outmap = new HashMap<String, Set<String>>();
		for(Entry<String, List<String>> in : inmap.entrySet()){
			for(String f : in.getValue()){
				Set<String> fvals = outmap.get(f);
				if(fvals==null){
					fvals = new HashSet<String>();
				}
				fvals.add(in.getKey());
				outmap.put(f, fvals);
			}
		}
		return outmap;
	}

	public static Map<String, String> flipMapStringString(Map<String, String> inmap){
		Map<String, String> outmap = new HashMap<String, String>();
		for(Entry<String, String> in : inmap.entrySet()){
			outmap.put(in.getValue(), in.getKey());
		}
		return outmap;
	}
	
	public static HashMap<String, String> read2columnMap(String file){
		HashMap<String, String> map = new HashMap<String, String>();
		try {
			BufferedReader f = new BufferedReader(new FileReader(file));
			String line = f.readLine().trim();
			while(line!=null){
				if(!line.startsWith("#")){
					String[] item = line.split("\t");
					if(item!=null&&item.length>1){
						map.put(item[0], item[1]);
					}
				}
				line = f.readLine();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return map;
	}
	
	public static String list2string(List<String> things, String delimiter){
		String list = "";
		for(String thing : things){
			list+=thing+delimiter;
		}
		return list;
	}
	
	public static List<String> string2list(String del, String delimiter){
		List<String> l = new ArrayList<String>();
		String[] split = del.split(delimiter);
		for(String s : split){
			l.add(s);
		}
		return l;
	}
}
