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
import java.util.Set;

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
		//somewhere there needs to be a mapping between the attribute id and the feature id
		//		String att_info_file = "/Users/bgood/workspace/acure/WebContent/WEB-INF/data/dream/Illumina2entrez_extras.tsv";
		//there also needs to be a weka-structured dataset so we can pull out the column index
		//		String weka_data = "/Users/bgood/workspace/acure/WebContent/WEB-INF/data/dream/Exprs_CNV_lts_2500genes.arff";	
		//		String dataset_name = "dream_breast_cancer_2";
		//		load(dataset_name, weka_data, att_info_file);
		//		Attribute v1 = Attribute.getByAttNameDataset("ILMN_1679920", "dream_breast_cancer");
		//		System.out.println(v1.feature_id);
//		String dataset = "dream_breast_cancer_2";
//		String weka_data = "/Users/bgood/workspace/acure/WebContent/WEB-INF/data/dream/Exprs_CNV_lts_2500genes.arff";
//		setReliefValue(dataset, weka_data);
		
		String att_info_file = "/Users/bgood/workspace/aacure/WebContent/WEB-INF/pubdata/griffith/griffith_meta.txt";
		//there also needs to be a weka-structured dataset so we can pull out the column index
		//the filtered set used in the game
		//String weka_data = "/Users/bgood/workspace/acure/WebContent/WEB-INF/data/griffith/griffith_breast_cancer_1.arff";	
		//the whole thing
		String weka_data = "/Users/bgood/workspace/aacure/WebContent/WEB-INF/pubdata/griffith/griffith_breast_cancer_2.arff";
		String dataset_name = "griffith_breast_cancer_full_train";
		//load(dataset_name, weka_data, att_info_file);
		setReliefValue(dataset_name, weka_data);
	}

	//set the relief value for all attributes
	//integrate it with the frequency information to make a final ranking..
	public static void setReliefValue(String dataset_name, String weka_data) throws FileNotFoundException, Exception{
		Weka weka = new Weka();
		weka.buildWeka(new FileInputStream(weka_data), null, dataset_name);
		Map<String, Float> index_relief = weka.getRelief();
		//String atest = "ILMN_1737586"; //0.0413
		for(String index : index_relief.keySet()){
			weka.core.Attribute tmp = weka.getTrain().attribute(index);
			//System.out.println(tmp.name()+"\t"+index_relief.get(index));
			Attribute dbatt = Attribute.getByAttNameDataset(index, dataset_name);
			if(dbatt!=null){
				dbatt.setReliefF(index_relief.get(index));
				dbatt.updateRelief();
			}else{
				System.out.println("Att missing\t"+index+"\t"+dataset_name);
			}
		}
	}

	/**
	 *Load up attributes from a weka dataset - make sure they map to rows in the feature table
	 * One feature >> multiple attributes
	 * @param args
	 * @throws Exception 
	 */
	public static void load(String dataset_name, String weka_data, String att_info_file) throws Exception {
		//Att_name	Entrez	Gene_symbol
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

		//declare what dataset this is
		//String dataset_name = "dream_breast_cancer";
		try {	
			Weka weka = new Weka();
			weka.buildWeka(new FileInputStream(weka_data), null, dataset_name);

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
						if(feat==null){//meaning that the mapping table presented here is not aligned with the feature database
							//see if we have already got it
							Attribute v1 = Attribute.getByAttNameDataset(name, dataset_name);
							if(v1!=null){
								feat = Feature.getByDbId(v1.getFeature_id());
							}
						}
						if(feat!=null){
							Attribute a = new Attribute();
							a.setCol_index(col);
							a.setDataset(dataset_name);
							a.setName(name);
							//					a.setReliefF(c.getPower());
							a.setFeature_id(feat.getId());
							a.insert();
						}else{
							System.out.println("No feature 1 for "+name);
						}
					}else{
						System.out.println("No feature in mapping table for "+name);
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

	public static Attribute getByAttNameDataset(String att_name, String dataset){
		JdbcConnection conn = new JdbcConnection();
		String q = "select attribute.* from attribute where name = '"+att_name+"' and dataset = '"+dataset+"'";
		Attribute a = null;
		ResultSet rslt = conn.executeQuery(q);
		try {
			if(rslt.next()){
				a = new Attribute();
				a.setName(rslt.getString("name"));
				a.setCol_index(rslt.getInt("col_index"));
				a.setCreated(rslt.getDate("created"));
				a.setFeature_id(rslt.getInt("feature_id"));
				a.setId(rslt.getInt("id"));
				a.setUpdated(rslt.getTimestamp("updated"));
				a.setDataset(rslt.getString("dataset"));
				a.setReliefF(rslt.getFloat("reliefF"));
			}
			rslt.close();
			conn.connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return a;
	}

	public static List<Attribute> getByFeatureUniqueId(String unique_id, String dataset){
		List<Attribute> atts = new ArrayList<Attribute>();
		JdbcConnection conn = new JdbcConnection();
		String q = "select attribute.* from attribute, feature where feature.unique_id = '"+unique_id+"' and attribute.feature_id = feature.id ";
		if(dataset!=null){
			q = "select attribute.* from attribute, feature " +
					"where feature.unique_id = '"+unique_id+"' " +
					"and attribute.feature_id = feature.id " +
					"and dataset = '"+dataset+"'";			
		}
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

	public static Map<Integer, List<Attribute>> getByFeatureUniqueIds(Set<Integer> genes, String dataset) {
		Map<Integer, List<Attribute>> gene_atts = new HashMap<Integer, List<Attribute>>();
		JdbcConnection conn = new JdbcConnection();
		String gq = "(";
		for(Integer gene : genes){
			gq+="feature.unique_id = '"+gene+"' or ";
		}
		gq = gq.substring(0,gq.length()-3);
		gq+=")";
		String q = "select feature.unique_id, attribute.* from attribute, feature where "+gq+" and attribute.feature_id = feature.id";
		if(dataset!=null){
			q += " and dataset = '"+dataset+"'";	
		}
		ResultSet rslt = conn.executeQuery(q);
		try {
			while(rslt.next()){
				int gene = rslt.getInt("unique_id");
				List<Attribute> atts = gene_atts.get(gene);
				if(atts==null){
					atts = new ArrayList<Attribute>();
				}
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
				gene_atts.put(gene, atts);
			}
			rslt.close();
			conn.connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return gene_atts;
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

	public int updateRelief() throws SQLException{
		int newid = 0;
		JdbcConnection conn = new JdbcConnection();
		PreparedStatement pst = null;
		try {
			pst = conn.connection.prepareStatement(
					"update attribute set reliefF = "+getReliefF()+" where id = "+getId());
			int affectedRows = pst.executeUpdate();
			if (affectedRows == 0) {
				throw new SQLException("Updating attribute failed, no rows affected.");
			}
			pst.close();
			conn.connection.close();
		} finally {
			if (pst != null) try { pst.close(); } catch (SQLException logOrIgnore) {}
			if (conn.connection != null) try { conn.connection.close(); } catch (SQLException logOrIgnore) {}
		}
		return newid;
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
