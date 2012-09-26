/**
 * 
 */
package org.scripps.combo.model;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.scripps.combo.weka.Weka;
import org.scripps.util.JdbcConnection;

import weka.core.Instances;

/**
 * Maps to a column in a weka data model.
 * @author bgood
 *create table attribute (id int(10) NOT NULL AUTO_INCREMENT, col_index int not null, name varchar(30), dataset varchar(50), reliefF float, created Date, updated timestamp, feature_id int, primary key (id), CONSTRAINT dataset_index_name UNIQUE (col_index, name, dataset));
 */

public class Attribute {

	int id;
	int col_index;
	String name;
	String dataset;
	Date created;
	float reliefF;
	Timestamp updated;
	int feature_id;

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		//load();
		//No feature 1 for ILMN_1777971
		//No feature 1 for ILMN_1715947
	}


	/**
	 *Load up attributes from a weka dataset - make sure they map to rows in the feature table
	 * One feature >> multiple attributes
	 * @param args
	 * @throws Exception 
	 */
	public static void load() throws Exception {

		//somewhere there needs to be a mapping between the attribute id and the feature id
		String att_info_file = "/Users/bgood/workspace/acure/WebContent/WEB-INF/data/dream/id_map2.txt";
		//Att_name	Gene_symbol	Entrez
		Map<String, String> att_uni = new HashMap<String, String>();
		BufferedReader f;
		try {			
			f = new BufferedReader(new FileReader(att_info_file));
			String line = f.readLine(); line = f.readLine(); 
			int c = 0;
			while(line!=null){
				c++;
				String[] items = line.split("\t");
				String uid = items[2]; String att = items[0];
				att_uni.put(att, uid);
				line = f.readLine(); 
			}
			f.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//there also needs to be a weka-structured dataset so we can pull out the column index
		String weka_data = "/Users/bgood/workspace/acure/WebContent/WEB-INF/data/dream/Exprs_CNV_2500genes.arff";		
		//declare what dataset this is
		String dataset = "dream_breast_cancer";
		try {	
			Weka weka = new Weka();
			weka.buildWeka(new FileInputStream(weka_data), null, dataset);

			//get the col index...
			Instances data = weka.getTrain();
			for(int i=0; i<data.numAttributes();i++){
				weka.core.Attribute att = data.attribute(i);
				String name = att.name();
				int col = att.index();
				String unique_id = att_uni.get(name);
				if(att.index()!=data.classIndex()){
					if(unique_id!=null){
						Feature feat = Feature.getByUniqueId(unique_id);
						if(feat!=null){
							Attribute a = new Attribute();
							a.setCol_index(col);
							a.setDataset(dataset);
							a.setName(name);
							//					a.setReliefF(c.getPower());
							a.setFeature_id(feat.getId());
							a.insert();
						}else{
							System.out.println("No feature 1 for "+name);
						}
					}else{
						System.out.println("No feature 2 for "+name);
					}
				}
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

	}
	public static List<Attribute> getByFeatureUniqueId(String unique_id){
		List<Attribute> atts = new ArrayList<Attribute>();
		JdbcConnection conn = new JdbcConnection();
		String q = "select attribute.* from attribute, feature where feature.unique_id = '"+unique_id+"' and attribute.feature_id = feature.id ";

		ResultSet rslt = conn.executeQuery(q);
		try {
			while(rslt.next()){
				Attribute a = new Attribute();
				a.setName(rslt.getString("name"));
				a.setCol_index(rslt.getInt("col_index"));
				a.setCreated(rslt.getDate("created"));
				a.setFeature_id(rslt.getInt("feature_id"));
				a.setId(rslt.getInt("id"));
				a.setUpdated(rslt.getTimestamp("updated"));
				a.setDataset(rslt.getString("dataset"));
				a.setReliefF(rslt.getFloat("reliefF"));
				atts.add(a);
			}
			rslt.close();
			conn.connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return atts;
	}

	public static List<Attribute> getByFeatureDbId(String db_id){
		List<Attribute> atts = new ArrayList<Attribute>();
		JdbcConnection conn = new JdbcConnection();
		String q = "select attribute.* from attribute where feature_id = "+db_id;

		ResultSet rslt = conn.executeQuery(q);
		try {
			while(rslt.next()){
				Attribute a = new Attribute();
				a.setName(rslt.getString("name"));
				a.setCol_index(rslt.getInt("col_index"));
				a.setCreated(rslt.getDate("created"));
				a.setFeature_id(rslt.getInt("feature_id"));
				a.setId(rslt.getInt("id"));
				a.setUpdated(rslt.getTimestamp("updated"));
				a.setDataset(rslt.getString("dataset"));
				a.setReliefF(rslt.getFloat("reliefF"));
				atts.add(a);
			}
			rslt.close();
			conn.connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return atts;
	}

	public int insert() throws SQLException{
		int newid = 0;
		JdbcConnection conn = new JdbcConnection();
		ResultSet generatedKeys = null; PreparedStatement pst = null;
		try {
			pst = conn.connection.prepareStatement(
					"insert into attribute (id, col_index, name, dataset, reliefF, created, feature_id) values(null,?,?,?,?,?,?)",
					Statement.RETURN_GENERATED_KEYS);
			pst.clearParameters();
			pst.setInt(1, getCol_index());
			pst.setString(2,getName());
			pst.setString(3,getDataset());
			pst.setFloat(4, getReliefF());
			pst.setDate(5, new Date(System.currentTimeMillis()));
			pst.setInt(6, getFeature_id());

			int affectedRows = pst.executeUpdate();
			if (affectedRows == 0) {
				throw new SQLException("Creating attribute failed, no rows affected.");
			}
			generatedKeys = pst.getGeneratedKeys();
			if (generatedKeys.next()) {
				newid = generatedKeys.getInt(1);	            
			} else {
				throw new SQLException("Creating attribute failed, no generated key obtained.");
			}
		} finally {
			if (generatedKeys != null) try { generatedKeys.close(); } catch (SQLException logOrIgnore) {}
			if (pst != null) try { pst.close(); } catch (SQLException logOrIgnore) {}
			if (conn.connection != null) try { conn.connection.close(); } catch (SQLException logOrIgnore) {}
		}
		return newid;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDataset() {
		return dataset;
	}

	public void setDataset(String dataset) {
		this.dataset = dataset;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public Timestamp getUpdated() {
		return updated;
	}

	public void setUpdated(Timestamp updated) {
		this.updated = updated;
	}

	public int getCol_index() {
		return col_index;
	}

	public void setCol_index(int col_index) {
		this.col_index = col_index;
	}

	public float getReliefF() {
		return reliefF;
	}

	public void setReliefF(float reliefF) {
		this.reliefF = reliefF;
	}

	public int getFeature_id() {
		return feature_id;
	}

	public void setFeature_id(int feature_id) {
		this.feature_id = feature_id;
	}





}
