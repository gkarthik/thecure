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
  `json_tree` text not NULL,
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
  `score` float,
  UNIQUE KEY `tree_score` (`tree_id`,`dataset`,`score`)
)
 * 
 * @author bgood
 *
 */
public class Tree {

	int player_id;
	int id;
	List<Feature> features; 
	String json_tree; // this is the blob used by the javascript client to render the tree and by the ManualTree class to evaluate it
	Date created;
	Map<String, Float> dataset_score; // trees could be tried on multiple datasets

	public Tree(){
		this.player_id = 0;
		this.id = 0;
		this.features = new ArrayList<Feature>();
		this.json_tree =  null;
		this.created = null;
	}

	public Tree(int player_id, int id, List<Feature> features, String json_tree, Date date) {
		this.player_id = player_id;
		this.id = id;
		this.features = features;
		this.json_tree = json_tree;
		this.created = date;
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
		uniques.add("1003");  uniques.add("10002");
		List<Feature> fs = new ArrayList<Feature>();
		for(String u : uniques){
			fs.add(Feature.getByUniqueId(u));
		}
		Tree test = new Tree(48, 0, fs,  "{i bens, great json tree []}", null);
		//	int tid = test.insert();
		//	test.insertScore(tid, "datasetname", (float) 68.98);
		List<Tree> trees = test.getAll(); //getForPlayer(48);
		for(Tree t : trees){
			System.out.println(t.json_tree);
			for(Feature f : t.features){
				System.out.println("\t"+f.getShort_name());
			}
			if(t.dataset_score!=null){
				for(String dataset : t.dataset_score.keySet()){
					System.out.println("\t\t"+dataset+"\t"+t.dataset_score.get(dataset));
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
				Tree tree = new Tree(player_id, ts.getInt("id"), null, ts.getString("json_tree"), ts.getDate("created"));
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
					Map<String, Float> data_score = new HashMap<String, Float>();
					while(scores.next()){
						data_score.put(scores.getString("dataset"), scores.getFloat("score"));
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
				Tree tree = new Tree(player_id, ts.getInt("id"), null, ts.getString("json_tree"), ts.getDate("created"));
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
		ResultSet generatedKeys = null; PreparedStatement pst = null;
		String insert = "insert into tree (player_id, json_tree) values(?,?)";

		PreparedStatement p = null;
		try {
			p = conn.connection.prepareStatement( insert, Statement.RETURN_GENERATED_KEYS);
			p.setInt(1, player_id);
			p.setString(2, json_tree);
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

	public void insertScore(int tree_id, String dataset, float score) throws Exception{
		JdbcConnection conn = new JdbcConnection();
		try {
			conn.executeUpdate("insert into tree_dataset_score values("+tree_id+",'"+dataset+"',"+score+")");
		} finally {
			if (conn.connection != null) try { conn.connection.close(); } catch (SQLException logOrIgnore) {}
		}
		return;
	}



}
