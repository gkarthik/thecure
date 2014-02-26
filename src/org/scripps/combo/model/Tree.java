/**
 * 
 */
package org.scripps.combo.model;

import java.io.IOException;
import java.sql.Date;
import java.sql.PreparedStatement;
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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

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
	int user_saved;

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

	public Tree (int id, int player_id, String ip, List<Feature> features, String json_tree, String comment, int user_saved) {
		this.player_id = player_id;
		this.id = id;
		this.ip = ip;
		this.features = features;
		this.json_tree = json_tree;
		this.comment = comment;
		this.user_saved = user_saved;
	}

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		Tree tree_ = new Tree();
		List<Tree> trees = tree_.getAll(); //add controls to get by user, get all	
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode treess = tree_.getTreeListAsJson(trees, mapper);
		String json = mapper.writeValueAsString(treess);
		System.out.println(json);
	}

	public ObjectNode getTreeListAsJson(List<Tree> trees, ObjectMapper mapper){
		ObjectNode root = mapper.createObjectNode();
		root.put("n_trees", trees.size());
		ArrayNode treelist = mapper.createArrayNode();
		for(Tree tree : trees){
			ObjectNode treeobj = mapper.createObjectNode();
			treeobj.put("comment",tree.comment);
			treeobj.put("id",tree.id);
			treeobj.put("ip",tree.ip);
			treeobj.put("created", tree.created.getTime());
			treeobj.put("user_saved",tree.user_saved);
			treeobj.put("player_id", tree.player_id);
			JsonNode jtree;
			try {
				jtree = mapper.readTree(tree.json_tree);
				treeobj.put("json_tree", jtree);
				treelist.add(treeobj);
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		root.put("trees",treelist);
		return root;
	}

	public List<Tree> getAll(){
		List<Tree> trees = new ArrayList<Tree>();
		String q = "select * from tree";
		JdbcConnection conn = new JdbcConnection();
		try {
			ResultSet ts = conn.executeQuery(q);
			while(ts.next()){
				Tree tree = new Tree(ts.getInt("id"), player_id, ts.getString("ip"), null, ts.getString("json_tree"), ts.getString("comment"), ts.getInt("user_saved"));
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
				Tree tree = new Tree(ts.getInt("id"), player_id, ts.getString("ip"), null, ts.getString("json_tree"),ts.getString("comment"), ts.getInt("user_saved"));
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

	public List<Tree> getByIP(String ip){
		List<Tree> trees = new ArrayList<Tree>();
		String q = "select * from tree where ip="+ip;
		JdbcConnection conn = new JdbcConnection();
		try {
			ResultSet ts = conn.executeQuery(q);
			while(ts.next()){
				Tree tree = new Tree(ts.getInt("id"), player_id, ts.getString("ip"), null, ts.getString("json_tree"),ts.getString("comment"), ts.getInt("user_saved"));
				tree.created = ts.getDate("created");
				//TODO stop being lazy and do this properly in SQL...
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
	
	public Tree getById(String id){
		Tree tree = new Tree();
		String q = "select * from tree where id="+id;
		JdbcConnection conn = new JdbcConnection();
		try {
			ResultSet ts = conn.executeQuery(q);
			if(ts.next()){
				tree = new Tree(ts.getInt("id"), player_id, ts.getString("ip"), null, ts.getString("json_tree"),ts.getString("comment"), ts.getInt("user_saved"));
				tree.created = ts.getDate("created");
				//TODO stop being lazy and do this properly in SQL...
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
			}
			ts.close();
			conn.connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return tree;
	}
	
	public List<Tree> getWithLimit(String lowerLimit, String upperLimit){
		List<Tree> trees = new ArrayList<Tree>();
		String q = "select * from tree where user_saved='1' order by id desc limit "+lowerLimit+","+upperLimit;
		JdbcConnection conn = new JdbcConnection();
		try {
			ResultSet ts = conn.executeQuery(q);
			while(ts.next()){
				Tree tree = new Tree(ts.getInt("id"), player_id, ts.getString("ip"), null, ts.getString("json_tree"), ts.getString("comment"), ts.getInt("user_saved"));
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
	
	
	public int insert() throws Exception{
		int newid = 0;
		JdbcConnection conn = new JdbcConnection();
		ResultSet generatedKeys = null;
		String insert = "insert into tree (player_id, ip, json_tree, comment, user_saved) values(?,?,?,?,?)";

		PreparedStatement p = null;
		try {
			p = conn.connection.prepareStatement( insert, Statement.RETURN_GENERATED_KEYS);
			p.setInt(1, player_id);
			p.setString(2, ip);
			p.setString(3, json_tree);
			p.setString(4, comment);
			p.setInt(5, user_saved);
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
						//tree_feature(Unique_Key) duplicated if same node added. Causes MySQL integrity error.
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

	//TODO add flag to indicate whether tree was saved purposefully by the user or captured autoamtically as part of the scoring process.
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

	public int getPlayer_id() {
		return player_id;
	}

	public void setPlayer_id(int player_id) {
		this.player_id = player_id;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public List<Feature> getFeatures() {
		return features;
	}

	public void setFeatures(List<Feature> features) {
		this.features = features;
	}

	public String getJson_tree() {
		return json_tree;
	}

	public void setJson_tree(String json_tree) {
		this.json_tree = json_tree;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public Map<String, TreeScore> getDataset_score() {
		return dataset_score;
	}

	public void setDataset_score(Map<String, TreeScore> dataset_score) {
		this.dataset_score = dataset_score;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public int getUser_saved() {
		return user_saved;
	}

	public void setUser_saved(int user_saved) {
		this.user_saved = user_saved;
	}



}
