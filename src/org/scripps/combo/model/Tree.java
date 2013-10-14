/**
 * 
 */
package org.scripps.combo.model;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.scripps.util.JdbcConnection;

/**
 * Represent a decision tree entered by players of The Cure
 * 
  CREATE TABLE `tree` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `player_id` int(11) NOT NULL,
  `ip` varchar(50),
  `json_tree` text not NULL,
  `comment` text,
  `created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
)

  CREATE TABLE `tree_feature` (
  `tree_id` int(11) NOT NULL,
  `feature_id` int(11) NOT NULL,
  UNIQUE KEY `tree_feature` (`feature_id`,`tree_id`)
)

  CREATE TABLE `tree_dataset_score` (
  `tree_id` int(11) NOT NULL,
  `dataset` varchar(100) NOT NULL,
  `percent_correct` float,
  `size` float,
  `novelty` float,
  `score` float,
  UNIQUE KEY `tree_score` (`tree_id`,`dataset`)
)
 * 
 * @author bgood
 *
 */
public class Tree {

	int player_id;
	int id;
	String ip;
	List<Feature> features; 
	String json_tree; // this is the blob used by the javascript client to render the tree and by the ManualTree class to evaluate it
	Date created;
	Map<String, TreeScore> dataset_score; // trees could be tried on multiple datasets
	String comment;

	public class TreeScore{
		float size;
		float novelty;
		float percent_correct;
		float score;
		
		public String toString(){
			String s = size+"\t"+novelty+"\t"+percent_correct+"\t"+score;
			return s;
		}
	}
	
	public Tree(){
		this.player_id = 0;
		this.id = 0;
		this.features = new ArrayList<Feature>();
	}

	public Tree (int id, int player_id, String ip, List<Feature> features, String json_tree, String comment) {
		this.player_id = player_id;
		this.id = id;
		this.ip = ip;
		this.features = features;
		this.json_tree = json_tree;
		this.comment = comment;
	}

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		List<Integer> features = new ArrayList<Integer>();
		features.add(3002);  features.add(2003);
		//Tree test = new Tree(1, 0, features, null,  "{i am the, json tree []}");

		List<String> uniques = new ArrayList<String>();
		uniques.add("1009");  uniques.add("10052");
		List<Feature> fs = new ArrayList<Feature>();
		for(String u : uniques){
			fs.add(Feature.getByUniqueId(u));
		}
		Tree test = new Tree(0, 48, "anip", fs,  "{i bens, great json tree []}", " I love trees");
		int tid = test.insert();
		test.insertScore(tid, "datasetname", (float) 68.98, (float) 3, (float) .58, (float) 62.8);
		List<Tree> trees = test.getAll(); //getForPlayer(48);
		for(Tree t : trees){
			System.out.println(t.json_tree+" comment: "+t.comment);
			for(Feature f : t.features){
				System.out.println("\t"+f.getShort_name());
			}
			if(t.dataset_score!=null){
				for(String dataset : t.dataset_score.keySet()){
					System.out.println("\t\t"+dataset+"\t"+t.dataset_score.get(dataset).toString());
				}
			}
		}

	}



	public List<Tree> getAll(){
		List<Tree> trees = new ArrayList<Tree>();
		String q = "select * from tree";
		JdbcConnection conn = new JdbcConnection();
		try {
			ResultSet ts = conn.executeQuery(q);
			while(ts.next()){
				Tree tree = new Tree(ts.getInt("id"), player_id, ts.getString("ip"), null, ts.getString("json_tree"), ts.getString("comment"));
				tree.created = ts.getDate("created");
				String fq = "select * from tree_feature where tree_id="+tree.id;
				ResultSet fs = conn.executeQuery(fq);
				List<Feature> features = new ArrayList<Feature>();
				while(fs.next()){
					int fid = fs.getInt("feature_id");
					Feature f = Feature.getByDbId(fid);
					if(f!=null){
						features.add(f);
					}
				}
				tree.features = features;
				//scores
				ResultSet scores = conn.executeQuery("select * from tree_dataset_score where tree_id="+tree.id);
				if(scores!=null){
					Map<String, TreeScore> data_score = new HashMap<String, TreeScore>();
					while(scores.next()){
						TreeScore score = new TreeScore();
						score.novelty = scores.getFloat("novelty");
						score.percent_correct = scores.getFloat("percent_correct");
						score.size = scores.getFloat("size");
						score.score = scores.getFloat("score");
						data_score.put(scores.getString("dataset"), score);
					}
					tree.dataset_score = data_score;
				}
				trees.add(tree);
			}
			ts.close();
			conn.connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return trees;
	}

	public List<Tree> getForPlayer(int player_id){
		List<Tree> trees = new ArrayList<Tree>();
		String q = "select * from tree where player_id="+player_id;
		JdbcConnection conn = new JdbcConnection();
		try {
			ResultSet ts = conn.executeQuery(q);
			while(ts.next()){
				Tree tree = new Tree(ts.getInt("id"), player_id, ts.getString("ip"), null, ts.getString("json_tree"),ts.getString("comment"));
				tree.created = ts.getDate("created");
				String fq = "select * from tree_feature where tree_id="+tree.id;
				ResultSet fs = conn.executeQuery(fq);
				List<Feature> features = new ArrayList<Feature>();
				while(fs.next()){
					int fid = fs.getInt("feature_id");
					Feature f = Feature.getByDbId(fid);
					if(f!=null){
						features.add(f);
					}
				}
				tree.features = features;
				trees.add(tree);
			}
			ts.close();
			conn.connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return trees;
	}

	public int insert() throws Exception{
		int newid = 0;
		JdbcConnection conn = new JdbcConnection();
		ResultSet generatedKeys = null;
		String insert = "insert into tree (player_id, ip, json_tree, comment) values(?,?,?,?)";

		PreparedStatement p = null;
		try {
			p = conn.connection.prepareStatement( insert, Statement.RETURN_GENERATED_KEYS);
			p.setInt(1, player_id);
			p.setString(2, ip);
			p.setString(3, json_tree);
			p.setString(4, comment);
			int affectedRows = p.executeUpdate();
			if (affectedRows == 0) {
				throw new SQLException("Creating tree failed, no rows affected.");
			}
			generatedKeys = p.getGeneratedKeys();
			if (generatedKeys.next()) {
				id = generatedKeys.getInt(1);
				newid = id;
				if(features!=null){
					for(Feature f : features){
						conn.executeUpdate("insert into tree_feature values("+newid+","+f.getId()+")");
					}
				}else{
					throw new Exception("Creating tree failed, no features detected.");
				}
			} else {
				throw new SQLException("Creating tree failed, no generated key obtained.");
			}
		} finally {
			if (generatedKeys != null) try { generatedKeys.close(); } catch (SQLException logOrIgnore) {}
			if (p != null) try { p.close(); } catch (SQLException logOrIgnore) {}
			if (conn.connection != null) try { conn.connection.close(); } catch (SQLException logOrIgnore) {}
		}

		return newid;
	}

	public void insertScore(int tree_id, String dataset, float percent_correct, float size, float novelty, float score) throws Exception{
		JdbcConnection conn = new JdbcConnection();
		try {
			conn.executeUpdate("insert into tree_dataset_score " +
					"values("+tree_id+",'"+dataset+"',"+percent_correct+","+size+","+novelty+","+score+")");
		} finally {
			if (conn.connection != null) try { conn.connection.close(); } catch (SQLException logOrIgnore) {}
		}
		return;
	}



}
