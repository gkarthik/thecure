package org.scripps.combo.model;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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

import org.scripps.combo.weka.Weka;
import org.scripps.combo.model.Attribute;
import org.scripps.util.Gene;
import org.scripps.util.JdbcConnection;
import org.scripps.util.MyGeneInfo;

import weka.attributeSelection.Ranker;
import weka.attributeSelection.ReliefFAttributeEval;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;

/**
 * Selectable features for the cure/combo game.
 * For the time being most of these are going to be genes..
 * @author bgood
 * create table feature (id int(10) NOT NULL AUTO_INCREMENT, unique_id varchar(50) not null, short_name varchar(30), long_name varchar(250), description text, created Date, updated timestamp, primary key (id));
 *
 */
public class Feature {

	int id; //database
	String unique_id; //public
	String short_name;
	String long_name;
	String description;
	//a single feature (gene) may map to multiple attributes (e.g. probe set ids)
	List<Attribute> dataset_attributes; // >> dataset attribute
	//linked annotation 
	List<Annotation> annotations;
	List<TextAnnotation> text_annotations;

	Date created;
	Timestamp updated;

	public static void main(String args[]){
		updateEntrezGene();
		//		String train_file = "/Users/bgood/workspace/acure/WebContent/WEB-INF/data/zoo_mammals.arff";
		//		String dataset = "mammal";
		//		try {
		//			loadRawWeka(train_file, dataset);
		//		} catch (FileNotFoundException e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		} catch (Exception e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		}
	}

	public void getAllMetadataFromDb(){
		getAnnotationsFromDb();
		getTextAnnotationsFromDb();
		getMappedAttributesFromDb();
	}

	public void getAnnotationsFromDb(){
		if(getId()!=0){
			List<Annotation> annotations = Annotation.getByFeatureDbId(""+getId());
			setAnnotations(annotations);
		}
	}
	public void getTextAnnotationsFromDb(){
		if(getId()!=0){
			List<TextAnnotation> annotations = TextAnnotation.getByFeatureDbId(""+getId());
			setText_annotations(annotations);
		}
	}
	//todo may want to add a dataset parameter here
	public void getMappedAttributesFromDb(){
		if(getId()!=0){
			List<Attribute> attributes = Attribute.getByFeatureDbId(""+getId());
			setDataset_attributes(attributes);
		}
	}

	public Map<String, List<Annotation>> getOntologyMap(){
		Map<String, List<Annotation>> ont_annos = new HashMap<String, List<Annotation>>();
		for(Annotation anno : getAnnotations()){
			List<Annotation> annos = ont_annos.get(anno.getVocabulary());
			if(annos==null){
				annos = new ArrayList<Annotation>();
			}
			annos.add(anno);
			ont_annos.put(anno.getVocabulary(), annos);
		}		
		return ont_annos;
	}

	/**
	 * Insert a new feature.
	 * @throws SQLException 
	 */
	public int insert() throws SQLException{
		int newid = 0;
		JdbcConnection conn = new JdbcConnection();
		ResultSet generatedKeys = null; PreparedStatement pst = null;
		try {
			pst = conn.connection.prepareStatement(
					"insert into feature (id, unique_id, short_name, long_name, description, created) values(null,?,?,?,?,?)",
					Statement.RETURN_GENERATED_KEYS);
			pst.clearParameters();
			pst.setString(1,getUnique_id());
			pst.setString(2,getShort_name());
			pst.setString(3,getLong_name());
			pst.setString(4, getDescription());
			pst.setDate(5, new Date(System.currentTimeMillis()));

			int affectedRows = pst.executeUpdate();
			if (affectedRows == 0) {
				throw new SQLException("Creating feature failed, no rows affected.");
			}
			generatedKeys = pst.getGeneratedKeys();
			if (generatedKeys.next()) {
				newid = generatedKeys.getInt(1);
				//use to link up the feature to the attribute ids

			} else {
				throw new SQLException("Creating feature failed, no generated key obtained.");
			}
		} finally {
			if (generatedKeys != null) try { generatedKeys.close(); } catch (SQLException logOrIgnore) {}
			if (pst != null) try { pst.close(); } catch (SQLException logOrIgnore) {}
			if (conn.connection != null) try { conn.connection.close(); } catch (SQLException logOrIgnore) {}
		}
		return newid;
	}

	public int updateByUniqueId() throws SQLException{
		int affectedRows  = 0;
		JdbcConnection conn = new JdbcConnection();
		PreparedStatement pst = null;
		try {
			pst = conn.connection.prepareStatement(
					"update feature " +
							"set short_name = ?, " +
							" long_name = ?, " +
							" description = ? " +
					"where unique_id = ? ");
			pst.clearParameters();
			pst.setString(1,getShort_name());
			pst.setString(2,getLong_name());
			pst.setString(3,getDescription());
			pst.setString(4, getUnique_id());

			affectedRows = pst.executeUpdate();
			if (affectedRows == 0) {
				throw new SQLException("Creating feature failed, no rows affected.");
			}

		} finally {
			if (pst != null) try { pst.close(); } catch (SQLException logOrIgnore) {}
			if (conn.connection != null) try { conn.connection.close(); } catch (SQLException logOrIgnore) {}
		}
		return affectedRows;
	}

	public static Map<String, Feature> getByDataset(String dataset, boolean load_annotations_very_slowly){
		Map<String, Feature> features = new HashMap<String, Feature>();
		JdbcConnection conn = new JdbcConnection();
		String q = "select feature.*, attribute.* from feature, attribute where attribute.dataset = '"+dataset+"' and feature.id = attribute.feature_id ";

		ResultSet rslt = conn.executeQuery(q);
		try {
			while(rslt.next()){
				String fid = rslt.getString("feature.unique_id");
				Feature f = features.get(fid);						
				if(f==null){
					f = new Feature();
					f.setCreated(rslt.getDate("feature.created"));
					f.setUpdated(rslt.getTimestamp("feature.updated"));
					f.setDescription(rslt.getString("feature.description"));
					f.setId(rslt.getInt("feature.id"));
					f.setLong_name(rslt.getString("feature.long_name"));
					f.setShort_name(rslt.getString("feature.short_name"));
					f.setUnique_id(rslt.getString("feature.unique_id"));
				}
				//this replaces
				//	f.getMappedAttributesFromDb();
				List<Attribute> atts = f.getDataset_attributes();
				if(atts==null){ atts = new ArrayList<Attribute>();}
				Attribute a = new Attribute();
				a.setName(rslt.getString("attribute.name"));
				a.setCol_index(rslt.getInt("attribute.col_index"));
				a.setCreated(rslt.getDate("attribute.created"));
				a.setFeature_id(rslt.getInt("attribute.feature_id"));
				a.setId(rslt.getInt("attribute.id"));
				a.setUpdated(rslt.getTimestamp("attribute.updated"));
				a.setDataset(rslt.getString("attribute.dataset"));
				a.setReliefF(rslt.getFloat("attribute.reliefF"));
				if(!atts.contains(a)){
					atts.add(a);
					f.setDataset_attributes(atts);
				}
				//add the linked data
				if(load_annotations_very_slowly){
					f.getAnnotationsFromDb();
					f.getTextAnnotationsFromDb();
				}
				features.put(f.getUnique_id(), f);
			}
			rslt.close();
			conn.connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return features;
	}

	public static Feature getByDbId(int id){
		Feature f = null;
		JdbcConnection conn = new JdbcConnection();
		String q = "select * from feature where id = "+id;

		ResultSet rslt = conn.executeQuery(q);
		try {
			if(rslt.next()){
				f = new Feature();
				f.setCreated(rslt.getDate("created"));
				f.setUpdated(rslt.getTimestamp("updated"));
				f.setDescription(rslt.getString("description"));
				f.setId(id);
				f.setLong_name(rslt.getString("long_name"));
				f.setShort_name(rslt.getString("short_name"));
				f.setUnique_id(rslt.getString("unique_id"));
			}
			rslt.close();
			conn.connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return f;
	}

	public static Feature getByUniqueId(String unique_id){
		Feature f = null;
		JdbcConnection conn = new JdbcConnection();
		String q = "select * from feature where unique_id = '"+unique_id+"'";

		ResultSet rslt = conn.executeQuery(q);
		try {
			if(rslt.next()){
				f = new Feature();
				f.setCreated(rslt.getDate("created"));
				f.setUpdated(rslt.getTimestamp("updated"));
				f.setDescription(rslt.getString("description"));
				f.setId(rslt.getInt("id"));
				f.setLong_name(rslt.getString("long_name"));
				f.setShort_name(rslt.getString("short_name"));
				f.setUnique_id(unique_id);
			}
			rslt.close();
			conn.connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return f;
	}

	public static void loadRawWeka(String train_file, String dataset) throws FileNotFoundException, Exception{
		Weka weka = new Weka();
		weka.buildWeka(new FileInputStream(train_file), null, dataset);
		Instances data = weka.getTrain();
		Map<String, Float> index_relief = weka.getRelief();
		for(int a=0; a<data.numAttributes(); a++){
			weka.core.Attribute att = data.attribute(a);
			if(att.index()!=data.classIndex()){
				Feature f = new Feature();
				f.setUnique_id(dataset+"_"+att.index());
				f.setShort_name(att.name());
				f.setLong_name(att.name());
				f.setDescription("");
				int f_id = f.insert();
				org.scripps.combo.model.Attribute combo_att = new org.scripps.combo.model.Attribute();
				combo_att.setCol_index(att.index());
				combo_att.setDataset(dataset);
				combo_att.setFeature_id(f_id);
				combo_att.setName(att.name());
				combo_att.setReliefF(index_relief.get(att.name()));
				combo_att.insert();
			}
		}				
	}

	public static void loadEntrezGene(){
		String gene_info_file = "/Users/bgood/workspace/acure/WebContent/WEB-INF/data/gene/Homo_sapiens.gene_info"; //gene info file from entrez ftp://ftp.ncbi.nih.gov/gene/DATA/
		BufferedReader f;
		try {			
			f = new BufferedReader(new FileReader(gene_info_file));
			String line = f.readLine(); 
			int c = 0;
			while(line!=null){
				c++;
				if(!line.startsWith("#")){
					String[] items = line.split("\t");
					Feature gene = new Feature();
					gene.setUnique_id(items[1]);
					gene.setShort_name(items[2]);
					gene.setLong_name(items[11]);
					gene.setDescription(items[8]);
					//chromosome 6, map location 7
					try {
						gene.insert();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				line = f.readLine(); 
				System.out.println(c);
			}
			f.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void updateEntrezGene(){
		String gene_info_file = "/Users/bgood/workspace/acure/database/gene/Homo_sapiens.gene_info"; //gene info file from entrez ftp://ftp.ncbi.nih.gov/gene/DATA/
		BufferedReader f;
		try {			
			f = new BufferedReader(new FileReader(gene_info_file));
			String line = f.readLine(); 
			int c = 0;
			while(line!=null){
				c++;
				if(!line.startsWith("#")){
					String[] items = line.split("\t");
					Feature gene = new Feature();
					gene.setUnique_id(items[1]);
					gene.setShort_name(items[2]);
					gene.setLong_name(items[11]);

					if(gene.getUnique_id()!=null){
						Gene g = MyGeneInfo.getGeneInfoByGeneid(gene.getUnique_id(), true);
						if(g!=null){
							gene.setDescription(g.getGeneDescription());
						}
					}
					try {
						gene.updateByUniqueId();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				line = f.readLine(); 
				System.out.println(c);
			}
			f.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}

	public String getShort_name() {
		return short_name;
	}
	public void setShort_name(String short_name) {
		this.short_name = short_name;
	}
	public String getLong_name() {
		return long_name;
	}
	public void setLong_name(String long_name) {
		this.long_name = long_name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
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

	public String getUnique_id() {
		return unique_id;
	}

	public void setUnique_id(String unique_id) {
		this.unique_id = unique_id;
	}

	public List<Attribute> getDataset_attributes() {
		return dataset_attributes;
	}

	public void setDataset_attributes(List<Attribute> dataset_attributes) {
		this.dataset_attributes = dataset_attributes;
	}

	public List<Annotation> getAnnotations() {
		return annotations;
	}

	public void setAnnotations(List<Annotation> annotations) {
		this.annotations = annotations;
	}

	public List<TextAnnotation> getText_annotations() {
		return text_annotations;
	}

	public void setText_annotations(List<TextAnnotation> text_annotations) {
		this.text_annotations = text_annotations;
	}



}
