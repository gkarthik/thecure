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
import java.util.List;
import java.util.Map;

import org.scripps.util.JdbcConnection;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

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
			if(!metaData.getColumnLabel(i).equals("id") && !metaData.getColumnLabel(i).equals("level_id") && !metaData.getColumnLabel(i).equals("badgehash")){
				attributes[counter] = metaData.getColumnLabel(i);
				counter++;
			}
		}
		return attributes;
	}
	
	public void insert(Map<String, String> mp, int level) throws Exception{
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
		String insert = "insert into badge(level_id,badgehash,"+row+") values("+level+",'"+badgehash+"',"+value+")";
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
}
