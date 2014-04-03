/**
 * 
 */
package org.scripps.combo.model;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.jasper.tagplugins.jstl.core.Set;
import org.scripps.util.JdbcConnection;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hp.hpl.jena.mem.ArrayBunch;

import java.security.MessageDigest;
import java.sql.ResultSetMetaData;
import java.text.SimpleDateFormat;

public class Badge {

	int id;
	String badgehash;
	float score;
	float accuracy;
	float size;
	float novelty;
	int genenumber;
	int cfnumber;
	float leafnodeacc;
	float leafnodesize;
	int treeno;
	int collaborators;
	int globaltreeno;
	
	public String createHash(String raw_string) throws Exception{
		StringBuffer hexString = new StringBuffer();
		MessageDigest md = MessageDigest.getInstance("MD5");
		byte[] hash = md.digest(raw_string.getBytes());

		        for (int i = 0; i < hash.length; i++) {
		            if ((0xff & hash[i]) < 0x10) {
		                hexString.append("0"
		                        + Integer.toHexString((0xFF & hash[i])));
		            } else {
		                hexString.append(Integer.toHexString(0xFF & hash[i]));
		            }
		        }
		return hexString.toString();
	}
	
	public String[] getListofAttributes() throws Exception{
		JdbcConnection conn = new JdbcConnection();
		String getattr = "select * from badge limit 1";
		ResultSet resultSet = conn.executeQuery(getattr);
		ResultSetMetaData metaData = resultSet.getMetaData();
		int count = metaData.getColumnCount();
		String attributes[] = new String[count];
		int counter = 0;
		for (int i = 1; i <= count; i++)
		{
			if(!metaData.getColumnLabel(i).equals("id") && !metaData.getColumnLabel(i).equals("level_id") && !metaData.getColumnLabel(i).equals("badgehash") && !metaData.getColumnLabel(i).equals("description")){
				attributes[counter] = metaData.getColumnLabel(i);
				counter++;
			}
		}
		return attributes;
	}
	
	public void insert(Map<String, String> mp, int level, String desc) throws Exception{
		JdbcConnection conn = new JdbcConnection();
		String getcount = "select count(*) as n from badge";
		ResultSet rslt = conn.executeQuery(getcount);
		double count = 0;
		try {
			if(rslt.next()){
				count = rslt.getDouble("n");
			}
			rslt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}	
		count++;
		String salt = "barney";
		String badgehash = createHash(salt+String.valueOf(level+count));
		String row = "";
		String value="";
		for (Map.Entry<String, String> entry : mp.entrySet()) {
			if(entry.getValue()!=""){
			    row += entry.getKey() + ",";
		        value += entry.getValue() + ",";	
			}
		}
		row = row.substring(0,row.length()-1);
		value = value.substring(0,value.length()-1);
		String insert = "insert into badge(level_id,badgehash,description,"+row+") values("+level+",'"+badgehash+"','"+desc+"',"+value+")";
		try {			
			conn.executeUpdate(insert);
		} finally {
			if (conn.connection != null) try { conn.connection.close(); } catch (SQLException logOrIgnore) {}
		}
	}	
	
	public String getBadgesofUser(int user_id) throws Exception{
		JdbcConnection conn = new JdbcConnection();
		String getbadge = "select * from badge";
		ResultSet rslt = conn.executeQuery(getbadge);
		ResultSetMetaData md = rslt.getMetaData();
		int columns = md.getColumnCount();
		ArrayList list = new ArrayList(50);
		while (rslt.next()){
			HashMap row = new HashMap(columns);
		    for(int i=columns; i>=1; --i){           
		    	if(!md.getColumnName(i).equals("badgehash") && !md.getColumnName(i).equals("id") && rslt.getObject(i)!=null){
			    	row.put(md.getColumnName(i),rslt.getObject(i));	
		    	}
		    }
		    list.add(row);
		}
		final OutputStream out = new ByteArrayOutputStream();
		final ObjectMapper mapper = new ObjectMapper();
		mapper.writeValue(out, list);
		final byte[] data = ((ByteArrayOutputStream) out).toByteArray();
		return new String(data);
	}
	
	public ArrayList getEarnedBadges(int tree_id, int player_id) throws Exception{
		ArrayList listOfBadges = new ArrayList();
		HashMap map = new HashMap();
		int prev_tree_id = tree_id;
		float score = 0, accuracy = 0, novwlty = 0, size = 0;
		JdbcConnection conn = new JdbcConnection();
		List<String> collaborators = new ArrayList<String>();
		List<Double> leafnodeacc = new ArrayList<Double>();
		List<Integer> leafnodesize = new ArrayList<Integer>();
		List<String> geneid = new ArrayList<String>();
		List<String> cfid = new ArrayList<String>();
		int treeno = 0, globaltreeno = 0, acctreeno = 0, sizetreeno = 0, countglobaltreeno = 0;
		HashMap<String,Integer> hm = new HashMap<String,Integer>();
		int tempCount = 0;
		
		ResultSet rslt;
		ObjectMapper mapper = new ObjectMapper();
		JsonNode treenode;
		String query = "";
		
		/*
		 * Badge table
			+---------------+-------------+------+-----+---------+----------------+
			| Field         | Type        | Null | Key | Default | Extra          |
			+---------------+-------------+------+-----+---------+----------------+
			| id            | int(11)     | NO   | PRI | NULL    | auto_increment |
			| badgehash     | varchar(50) | NO   |     | NULL    |                |
			| level_id      | int(11)     | YES  |     | NULL    |                |
			| globaltreeno  | int(11)     | YES  |     | NULL    |                |
			| treeno        | int(11)     | YES  |     | NULL    |                |
			| genenumber    | int(11)     | YES  |     | NULL    |                |
			| cfnumber      | int(11)     | YES  |     | NULL    |                |
			| leafnodeacc   | float       | YES  |     | NULL    |                |
			| leafnodesize  | float       | YES  |     | NULL    |                |
			| score         | float       | YES  |     | NULL    |                |
			| accuracy      | float       | YES  |     | NULL    |                |
			| novelty       | float       | YES  |     | NULL    |                |
			| size          | float       | YES  |     | NULL    |                |
			| collaborators | int(11)     | YES  |     | NULL    |                |
			+---------------+-------------+------+-----+---------+----------------+
		 */
		
		query="select * from badge";
		ResultSet badgerslt = conn.executeQuery(query);
		ResultSetMetaData md = badgerslt.getMetaData();
		int columns = md.getColumnCount();
		int i = columns;
		int flag = 1;
		
		while(badgerslt.next()){
			if(badgerslt.getObject("treeno")==null && badgerslt.getObject("globaltreeno")==null){
				query = "select tree.json_tree,tree_dataset_score.score,tree_dataset_score.size,tree_dataset_score.percent_correct,tree_dataset_score.novelty from tree,tree_dataset_score where tree.id = tree_dataset_score.tree_id and tree.id="+tree_id;
				if(badgerslt.getString("globaltreeno")!=null){
					globaltreeno = badgerslt.getInt("globaltreeno");
				}
				if(badgerslt.getString("treeno")!=null){
					treeno = badgerslt.getInt("treeno");
				}
			} else {
				query = "select tree.json_tree,tree_dataset_score.score,tree_dataset_score.size,tree_dataset_score.percent_correct,tree_dataset_score.novelty from tree,tree_dataset_score where tree.id = tree_dataset_score.tree_id and tree.user_saved = 1 and tree.player_id="+player_id;
			}
			rslt = conn.executeQuery(query);

			countglobaltreeno = 0;
			acctreeno = 0;
			sizetreeno = 0;
			hm = new HashMap<String,Integer>();
			
			//TODO: Better way to do this??
			hm.put("cfnumber",-1);
			hm.put("genenumber",-1);
			hm.put("leafnodeacc",-1);
			hm.put("leafnodesize",-1);
			hm.put("score",-1);
			hm.put("accuracy",-1);
			hm.put("novelty",-1);
			hm.put("size",-1);
			hm.put("collaborators",-1);
			
			while(rslt.next()){
				i = columns;
				collaborators = new ArrayList<String>();
				leafnodeacc = new ArrayList<Double>();
				leafnodesize = new ArrayList<Integer>();
				geneid = new ArrayList<String>();
				cfid = new ArrayList<String>();
				flag = 1;
				countglobaltreeno++;
				treenode = mapper.readTree(rslt.getString("json_tree")).path("treestruct");
				if(treenode.path("options").path("id").asText().contains("metabric")){
					cfid.add(treenode.path("options").path("id").asText());
				} else {
					geneid.add(treenode.path("options").path("id").asText());
				}
				if(!collaborators.contains(treenode.path("collaborator").path("id").asText())){
					collaborators.add(treenode.path("collaborator").path("id").asText());
				}
				JsonNode msgNode = treenode.path("children");
				Iterator<JsonNode> ite = msgNode.elements();
				
				while (ite.hasNext()) {
					JsonNode temp = ite.next();
					if(temp.path("options").path("id").asText().contains("metabric") && temp.path("options").path("kind").asText().equals("split_node")){
						cfid.add(temp.path("options").path("id").asText());
					} else if(!temp.path("options").path("id").asText().contains("metabric") && temp.path("options").path("kind").asText().equals("split_node")) {
						geneid.add(temp.path("options").path("id").asText());
					}
					if(temp.path("options").path("kind").asText().equals("leaf_node")){
						leafnodeacc.add(temp.path("options").path("pct_correct").asDouble());
						leafnodesize.add(temp.path("options").path("size").asInt());
					}
					if(!collaborators.contains(temp.path("collaborator").path("id").asText())){
						
						collaborators.add(temp.path("collaborator").path("id").asText());
					}
					msgNode = temp.path("children");
					ite = msgNode.elements();
				}
				while(i >= 1){
					if(badgerslt.getObject(md.getColumnName(i))!=null){
						 switch (md.getColumnName(i)) {
				            case "globaltreeno":  
				            	if(countglobaltreeno < badgerslt.getInt(md.getColumnName(i))){
				            		flag = 0;
				            	}
				                break;
				            case "treeno":  
				            	if(treeno < badgerslt.getInt(md.getColumnName(i))){
				            		flag = 0;
				            	}
				                break;
				            case "genenumber":  
				            	if(geneid.size() < badgerslt.getInt(md.getColumnName(i)) && badgerslt.getInt(md.getColumnName(i))!=0){
				            		flag = 0;
				            	} else if(badgerslt.getInt(md.getColumnName(i))==0 && geneid.size()!=0){
				            		flag = 0;
				            	} else {
					            	hm.put(md.getColumnName(i),(hm.get(md.getColumnName(i)).intValue()+1));
				            	}
				                break;
				            case "cfnumber":  
				            	if(cfid.size() < badgerslt.getInt(md.getColumnName(i))){
				            		flag = 0;
				            	} else if(badgerslt.getInt(md.getColumnName(i))==0 && cfid.size()!=0){
				            		flag = 0;
				            	} else {
					            	hm.put(md.getColumnName(i),(hm.get(md.getColumnName(i)).intValue()+1));
				            	}
				                break;
				            case "leafnodeacc":  
				            	boolean accFlag = true;
				            	for(Iterator<Double> j = leafnodeacc.iterator(); j.hasNext(); ) {
				            		if(j.next()<badgerslt.getDouble(md.getColumnName(i))){
				            			flag = 0;
				            			accFlag = false;
				            		}
				            	}
				            	if(accFlag){
				            		hm.put(md.getColumnName(i),(hm.get(md.getColumnName(i)).intValue()+1));
				            	}
				                break;
				            case "leafnodesize":  
				            	boolean sizeFlag = true;
				            	for(Iterator<Integer> j = leafnodesize.iterator(); j.hasNext(); ) {
				            		if(j.next()<badgerslt.getInt(md.getColumnName(i))){
				            			flag = 0;
				            			sizeFlag =false;
				            		}
				            	}
				            	if(sizeFlag){
				            		hm.put(md.getColumnName(i),(hm.get(md.getColumnName(i)).intValue()+1));
				            	}
				                break;
				            case "score":  
				            	if(rslt.getFloat("score") < badgerslt.getFloat(md.getColumnName(i))){
				            		flag = 0;
				            	} else {
					            	hm.put(md.getColumnName(i),(hm.get(md.getColumnName(i)).intValue()+1));
				            	}
				                break;
				            case "size":  
				            	if(rslt.getFloat("size") < badgerslt.getFloat(md.getColumnName(i))){
				            		flag = 0;
				            	} else {
					            	hm.put(md.getColumnName(i),(hm.get(md.getColumnName(i)).intValue()+1));
				            	}
				                break;
				            case "accuracy":  
				            	if(rslt.getFloat("percent_correct") < badgerslt.getFloat(md.getColumnName(i))){
				            		flag = 0;
				            	} else {
					            	hm.put(md.getColumnName(i),(hm.get(md.getColumnName(i)).intValue()+1));
				            	}
				                break;
				            case "novelty":  
				            	if(rslt.getFloat("novelty") < badgerslt.getFloat(md.getColumnName(i))){
				            		flag = 0;
				            	} else {
					            	hm.put(md.getColumnName(i),(hm.get(md.getColumnName(i)).intValue()+1));
				            	}
				                break;
				            case "collaborators":  
				            	if(collaborators.size() < badgerslt.getInt(md.getColumnName(i))){
				            		flag = 0;
				            	} else {
					            	hm.put(md.getColumnName(i),(hm.get(md.getColumnName(i)).intValue()+1));
				            	}
				                break;
				        }
					}
					i-- ;
					boolean checkflag = true;
					int checkvalue = 0;
					int tempValue = -1;
					for (Map.Entry<String, Integer> entry : hm.entrySet()) {
						if(entry.getValue()!=-1){
							if(entry.getValue() != tempValue && tempValue!=-1){
								checkflag = false;
							} else if(entry.getValue() == tempValue) {
								checkvalue = tempValue;
							}
						}
						tempValue = entry.getValue();
					}
					if(checkflag){
						treeno = (checkvalue+1);
					}
				}
				
				if(flag == 1){
					boolean addBadgeFlag = true;
					for (int a =0; a<listOfBadges.size();a++)
			        {
			            HashMap<String, Integer> tmpData = (HashMap<String, Integer>) listOfBadges.get(a);
			            for (Map.Entry<String, Integer> entry : tmpData.entrySet()) {
							if(entry.getValue() == badgerslt.getObject("id")){
								addBadgeFlag = false;
							}
						}
			        }       
					if(addBadgeFlag){
						map = new HashMap();
						map.put("id", badgerslt.getObject("id"));
						map.put("description", badgerslt.getObject("description"));
						listOfBadges.add(map);	
					}
				}

			}
		}
		return listOfBadges;
	}
}
