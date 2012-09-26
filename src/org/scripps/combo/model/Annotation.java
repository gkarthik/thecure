/**
 * 
 */
package org.scripps.combo.model;

import java.io.BufferedReader;
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
import java.util.List;

import org.scripps.util.JdbcConnection;

/**
 * Capture ontology-based annotations.  Typically, but necessarily from GO
 * @author bgood
 * create table annotation (id int(10) NOT NULL AUTO_INCREMENT, feature_id int not null, vocabulary varchar(50), accession varchar(50), term varchar(250), evidence_type varchar(100), source varchar(200), created date, updated timestamp, primary key (id), CONSTRAINT f1 UNIQUE (feature_id, accession, evidence_type, source));
 */

public class Annotation {

	int id;
	int feature_id; 
	String vocabulary;
	String accession;
	String term;
	String evidence_type;
	String source;
	Date created;
	Timestamp updated;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		List<Annotation> annos = Annotation.getByFeatureUniqueId("2");
		for(Annotation anno : annos){
			System.out.println(anno.getTerm()+" "+anno.getVocabulary()+" "+anno.getEvidence_type()+" "+anno.getAccession());
		}
	}

	public static void loadGeneOntology(){
		//Load gene ontology
		String gene_info_file = "/Users/bgood/workspace/acure/database/gene/gene2go"; //gene info file from entrez ftp://ftp.ncbi.nih.gov/gene/DATA/
		BufferedReader f;
		try {			
			f = new BufferedReader(new FileReader(gene_info_file));
			String line = f.readLine(); 
			int c = 0;
			while(line!=null){

				if((!line.startsWith("#"))&&(line.startsWith("9606"))){
					String[] items = line.split("\t");
					Annotation a = new Annotation();
					Feature feat = Feature.getByUniqueId(items[1]);
					if(feat!=null){
						a.setFeature_id(feat.getId()); 
						a.setAccession(items[2]);
						a.setEvidence_type(items[3]);
						a.setTerm(items[5]);
						a.setVocabulary(items[7]);
						a.setSource("ncbi_gene2go");
						try {
							a.insert();
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						c++;
						System.out.println(c);
					}
				}
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
	}
	
	public int insert() throws SQLException{
		int newid = 0;
		JdbcConnection conn = new JdbcConnection();
		ResultSet generatedKeys = null; PreparedStatement pst = null;
		try {
			pst = conn.connection.prepareStatement(
					"insert into annotation (id, feature_id, vocabulary, accession, term, evidence_type, source, created) values(null,?,?,?,?,?,?,?)",
					Statement.RETURN_GENERATED_KEYS);
			pst.clearParameters();
			pst.setInt(1, getFeature_id());
			pst.setString(2,getVocabulary());
			pst.setString(3,getAccession());
			pst.setString(4, getTerm());
			pst.setString(5, getEvidence_type());
			pst.setString(6, getSource());
			pst.setDate(7, new Date(System.currentTimeMillis()));

			int affectedRows = pst.executeUpdate();
			if (affectedRows == 0) {
				throw new SQLException("Creating annotation failed, no rows affected.");
			}
			generatedKeys = pst.getGeneratedKeys();
			if (generatedKeys.next()) {
				newid = generatedKeys.getInt(1);	            
			} else {
				throw new SQLException("Creating annotation failed, no generated key obtained.");
			}
		} finally {
			if (generatedKeys != null) try { generatedKeys.close(); } catch (SQLException logOrIgnore) {}
			if (pst != null) try { pst.close(); } catch (SQLException logOrIgnore) {}
			if (conn.connection != null) try { conn.connection.close(); } catch (SQLException logOrIgnore) {}
		}
		return newid;
	}

	public static List<Annotation> getByFeatureUniqueId(String unique_id){
		List<Annotation> annos = new ArrayList<Annotation>();
		JdbcConnection conn = new JdbcConnection();
		String q = "select annotation.* from annotation, feature where feature.unique_id = '"+unique_id+"' and annotation.feature_id = feature.id ";

		ResultSet rslt = conn.executeQuery(q);
		try {
			while(rslt.next()){
				Annotation a = new Annotation();
				a.setAccession(rslt.getString("accession"));
				a.setCreated(rslt.getDate("created"));
				a.setEvidence_type(rslt.getString("evidence_type"));
				a.setFeature_id(rslt.getInt("feature_id"));
				a.setId(rslt.getInt("id"));
				a.setSource(rslt.getString("source"));
				a.setTerm(rslt.getString("term"));
				a.setUpdated(rslt.getTimestamp("updated"));
				a.setVocabulary(rslt.getString("vocabulary"));
				annos.add(a);
			}
			rslt.close();
			conn.connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return annos;
	}
	
	public static List<Annotation> getByFeatureDbId(String db_id){
		List<Annotation> annos = new ArrayList<Annotation>();
		JdbcConnection conn = new JdbcConnection();
		String q = "select annotation.* from annotation where feature_id = "+db_id;

		ResultSet rslt = conn.executeQuery(q);
		try {
			while(rslt.next()){
				Annotation a = new Annotation();
				a.setAccession(rslt.getString("accession"));
				a.setCreated(rslt.getDate("created"));
				a.setEvidence_type(rslt.getString("evidence_type"));
				a.setFeature_id(rslt.getInt("feature_id"));
				a.setId(rslt.getInt("id"));
				a.setSource(rslt.getString("source"));
				a.setTerm(rslt.getString("term"));
				a.setUpdated(rslt.getTimestamp("updated"));
				a.setVocabulary(rslt.getString("vocabulary"));
				annos.add(a);
			}
			rslt.close();
			conn.connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return annos;
	}
	
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getVocabulary() {
		return vocabulary;
	}

	public void setVocabulary(String vocabulary) {
		this.vocabulary = vocabulary;
	}

	public String getAccession() {
		return accession;
	}

	public void setAccession(String accession) {
		this.accession = accession;
	}

	public String getTerm() {
		return term;
	}

	public void setTerm(String term) {
		this.term = term;
	}

	public String getEvidence_type() {
		return evidence_type;
	}

	public void setEvidence_type(String evidence_type) {
		this.evidence_type = evidence_type;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}



	public int getFeature_id() {
		return feature_id;
	}



	public void setFeature_id(int feature_id) {
		this.feature_id = feature_id;
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



}
