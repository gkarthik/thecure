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
 * Capture things like Gene RIFs
 * @author bgood
 * create table text_annotation (id int(10) NOT NULL AUTO_INCREMENT, feature_id int not null, source varchar(50), anno_text text, created date, updated timestamp, primary key (id));
   create index f_index on text_annotation (feature_id);
 */
public class TextAnnotation {

	int id;
	int feature_id;
	int pubmed_id;
	String anno_text;
	String source;
	Date created;
	Timestamp updated;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		List<TextAnnotation> annos = TextAnnotation.getByFeatureUniqueId("2");
		for(TextAnnotation anno : annos){
			System.out.println(anno.getAnno_text()+" "+anno.getPubmed_id());
		}

	}

	public static void loadGeneRifs(){
		//Load gene rifs
		String gene_info_file = "/Users/bgood/workspace/acure/database/gene/generifs_basic"; //gene info file from entrez ftp://ftp.ncbi.nih.gov/gene/DATA/
		BufferedReader f;
		try {			
			f = new BufferedReader(new FileReader(gene_info_file));
			String line = f.readLine(); 
			int c = 0; int skip = 34572;
			while(line!=null){
				if((!line.startsWith("#"))&&(line.startsWith("9606"))){
					String[] items = line.split("\t");
					TextAnnotation a = new TextAnnotation();
					Feature feat = Feature.getByUniqueId(items[1]);
					if(feat!=null){
						String text = items[4].trim();
						if(!text.equals("Observational study of gene-disease association. (HuGE Navigator)")&&
								!text.equals("Observational study of gene-disease association and gene-gene interaction. (HuGE Navigator)")){

							c++;
							System.out.println(c);
							if(c>skip){
								a.setFeature_id(feat.getId()); 
								String pmids = items[2];
								int pmid = 0;
								try{
									if(pmids.contains(",")){
										pmid = Integer.parseInt(pmids.split(",")[0]);
									}else{
										pmid = Integer.parseInt(pmids);
									}
								}catch(NumberFormatException e){} //ignore and move on
								a.setPubmed_id(pmid);
								a.setAnno_text(text);
								a.setSource("ncbi_generifs");
								try {
									a.insert();
								} catch (SQLException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}
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
					"insert into text_annotation (id, feature_id, pubmed_id, anno_text, source, created) values(null,?,?,?,?,?)",
					Statement.RETURN_GENERATED_KEYS);
			pst.clearParameters();
			pst.setInt(1, getFeature_id());
			pst.setInt(2,getPubmed_id());
			pst.setString(3,getAnno_text());
			pst.setString(4, getSource());
			pst.setDate(5, new Date(System.currentTimeMillis()));

			int affectedRows = pst.executeUpdate();
			if (affectedRows == 0) {
				throw new SQLException("Creating text annotation failed, no rows affected.");
			}
			generatedKeys = pst.getGeneratedKeys();
			if (generatedKeys.next()) {
				newid = generatedKeys.getInt(1);	            
			} else {
				throw new SQLException("Creating text annotation failed, no generated key obtained.");
			}
			conn.connection.close();
		} finally {
			if (generatedKeys != null) try { generatedKeys.close(); } catch (SQLException logOrIgnore) {}
			if (pst != null) try { pst.close(); } catch (SQLException logOrIgnore) {}
			if (conn.connection != null) try { conn.connection.close(); } catch (SQLException logOrIgnore) {}
		}
		return newid;
	}

	public static List<TextAnnotation> getByFeatureUniqueId(String unique_id){
		List<TextAnnotation> annos = new ArrayList<TextAnnotation>();
		JdbcConnection conn = new JdbcConnection();
		String q = "select text_annotation.* from text_annotation, feature where feature.unique_id = '"+unique_id+"' and text_annotation.feature_id = feature.id ";

		ResultSet rslt = conn.executeQuery(q);
		try {
			while(rslt.next()){
				TextAnnotation a = new TextAnnotation();
				a.setCreated(rslt.getDate("created"));
				a.setFeature_id(rslt.getInt("feature_id"));
				a.setId(rslt.getInt("id"));
				a.setSource(rslt.getString("source"));
				a.setPubmed_id(rslt.getInt("pubmed_id"));
				a.setUpdated(rslt.getTimestamp("updated"));
				a.setAnno_text(rslt.getString("anno_text"));
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
	
	public static List<TextAnnotation> getByFeatureDbId(String db_id){
		List<TextAnnotation> annos = new ArrayList<TextAnnotation>();
		JdbcConnection conn = new JdbcConnection();
		String q = "select text_annotation.* from text_annotation where feature_id = "+db_id;

		ResultSet rslt = conn.executeQuery(q);
		try {
			while(rslt.next()){
				TextAnnotation a = new TextAnnotation();
				a.setCreated(rslt.getDate("created"));
				a.setFeature_id(rslt.getInt("feature_id"));
				a.setId(rslt.getInt("id"));
				a.setSource(rslt.getString("source"));
				a.setPubmed_id(rslt.getInt("pubmed_id"));
				a.setUpdated(rslt.getTimestamp("updated"));
				a.setAnno_text(rslt.getString("anno_text"));
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

	public int getFeature_id() {
		return feature_id;
	}

	public void setFeature_id(int feature_id) {
		this.feature_id = feature_id;
	}

	public int getPubmed_id() {
		return pubmed_id;
	}

	public void setPubmed_id(int pubmed_id) {
		this.pubmed_id = pubmed_id;
	}



	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getAnno_text() {
		return anno_text;
	}

	public void setAnno_text(String anno_text) {
		this.anno_text = anno_text;
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
