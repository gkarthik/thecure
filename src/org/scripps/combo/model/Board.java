package org.scripps.combo.model;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.scripps.util.MapFun;
import org.scripps.combo.weka.Weka;
import org.scripps.combo.weka.Weka.execution;
import org.scripps.util.JdbcConnection;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * A pre-computed set of features (e.g. genes) for building board games.
 * create table board (id int(10) NOT NULL AUTO_INCREMENT, dataset varchar(50), room varchar(25), n_players int, n_wins int, avg_score float, max_score float, base_score float, created Date, updated timestamp, primary key (id));
	create table board_feature (board_id int, feature_id int);
 * @author bgood
 *
 */
public class Board {

	int id;
	String dataset;
	String room;
	List<Feature> features;
	int n_players;
	int n_wins;
	float avg_score;
	float max_score;
	float base_score;
	Date created;
	Timestamp updated;

	public static void main(String args[]) throws Exception{
//		Board b = Board.getBoardById(101+"", true);	
//		String j = b.toJSON(true);
//		System.out.println(j);
//		Board.setupCureV3();
//		setupCureV4_griffith_1();
		setupCureV5_griffith_1();
	}

	
	/**
	 * {
    "unique_id": "9338",
    "power": 0.010211045,
    "short_name": "ABC",
    "long_name": "Aaa Bbb Ccc",
    "metadata": {
      "ontology": [
        {
          "type": "Biological Processes",
          "values": [
            {
              "accession_id": 12345,
              "term": "a"
            }
          ]
        }
      ],
      "rifs": [
        "a",
        "b",
        "c",
        "d",
        "e"
      ]
    },
    "board_index": 0
  }
	 * @return
	 */
	public String toJSON(boolean shuffle){
		String json = "";
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode root = mapper.createObjectNode();
		root.put("board_id", getId());
		root.put("dataset", getDataset());
		root.put("room", getRoom());
		ArrayNode cards = mapper.createArrayNode();
		if(shuffle){
			Collections.shuffle(features);
		}
		int loc = 0;
		for(Feature feature : getFeatures()){
			ObjectNode card = mapper.createObjectNode();
			card.put("board_index", loc);
			card.put("unique_id", feature.getUnique_id());
			card.put("short_name", feature.getShort_name());
			card.put("long_name", feature.getLong_name());
			card.put("description", feature.getDescription());
			ObjectNode metadata = mapper.createObjectNode();
			ArrayNode ontology = mapper.createArrayNode();
			Map<String, List<Annotation>> ont_annos = feature.getOntologyMap();
			for(String ont : ont_annos.keySet()){
				ObjectNode ont_terms = mapper.createObjectNode();
				ont_terms.put("type", ont);
				ArrayNode values = mapper.createArrayNode();
				for(Annotation anno : ont_annos.get(ont)){
					ObjectNode term = mapper.createObjectNode();
					term.put("accession", anno.getAccession());
					term.put("term", anno.getTerm());
					term.put("evidence_type", anno.getEvidence_type());
					term.put("source", anno.getSource());
					values.add(term);
				}
				ont_terms.put("values",values);
				ontology.add(ont_terms);
			}
			metadata.put("ontology", ontology);	
			ArrayNode rifs = mapper.createArrayNode();
			for(TextAnnotation t : feature.getText_annotations()){
				ObjectNode rif = mapper.createObjectNode();
				rif.put("pubmed_id", t.getPubmed_id());
				rif.put("text", t.getAnno_text());
				rifs.add(rif);
			}
			metadata.put("rifs", rifs);
			card.put("metadata", metadata);
			cards.add(card);
			loc++;
		}
		root.put("cards", cards);	
		try {
			json = mapper.writeValueAsString(root);
		} catch (JsonGenerationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json;
	}
	
	/**
	 * For second iteration of the cure, build 100 boards with this
	 */
	public static void setupCureV2(){
		String dataset = "dream_breast_cancer";
		String train_file = "/Users/bgood/workspace/acure/WebContent/WEB-INF/data/dream/Exprs_CNV_2500genes.arff";
		int nper = 36; int nboards = 50; int topper = 2; String room = "2";
		try {
			for(int i=0; i<2; i++){
				createAndSaveInterestingBoards(train_file, nper, dataset, room, nboards, topper);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//also build the mammal boards
		dataset = "mammal";
		train_file = "/Users/bgood/workspace/acure/WebContent/WEB-INF/data/zoo_mammals.arff";
		List<String> feature_ids = MapFun.string2list("mammal_1,mammal_10", ",");
		//List<String> feature_ids = MapFun.string2list("mammal_3,mammal_9", ",");
		//List<String> feature_ids = MapFun.string2list("mammal_9,mammal_7,mammal_6,mammal_14", ",");
		//List<String> feature_ids = MapFun.string2list("mammal_4,mammal_3,mammal_1,mammal_13", ",");
		try {
			createAndSaveSpecificBoard(train_file, dataset, "training_1", feature_ids);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void setupCureV3(){
		String dataset = "dream_breast_cancer_2";
		String train_file = "/Users/bgood/workspace/acure/WebContent/WEB-INF/data/dream/Exprs_CNV_lts_2500genes.arff";
		int nper = 25; int nboards = 100; String room = "3";
		try {
			//for(int i=0; i<2; i++){
				createAndSaveBoardsCoverGenesTwice(train_file, nper, dataset, room, nboards);
			//}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void setupCureV4_griffith_1(){
		String dataset = "griffith_breast_cancer_1";
		String train_file = "/Users/bgood/workspace/acure/WebContent/WEB-INF/data/griffith/griffith_breast_cancer_1.arff";
		int nper = 25; int nboards = 100; String room = "4";
		try {
			//for(int i=0; i<2; i++){
				createAndSaveBoardsCoverGenesTwice(train_file, nper, dataset, room, nboards);
			//}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void setupCureV5_griffith_1(){
		String dataset = "griffith_breast_cancer_1";
		String train_file = "/Users/bgood/workspace/acure/WebContent/WEB-INF/pubdata/griffith/griffith_breast_cancer_1.arff";
		int nper = 25; int nboards = 100; String room = "5";
		try {
			//for(int i=0; i<2; i++){
				createAndSaveBoardsCoverGenesTwice(train_file, nper, dataset, room, nboards);
			//}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * For each of 'total' genes, generate two boards containing it.
	 * @param train_file
	 * @param n_per_board
	 * @param dataset
	 * @param room
	 * @param total
	 * @throws Exception
	 */
	public static void createAndSaveBoardsCoverGenesTwice(String train_file, int n_per_board, String dataset, String room, int total) throws Exception{	
		Weka weka = new Weka();
		weka.buildWeka(new FileInputStream(train_file), null, dataset);

		//produce the list of unique entrez gene ids

		List<String> gene_ids = new ArrayList<String>(weka.getFeatures().keySet());
		System.out.println("N gene ids: "+gene_ids.size());

		List<String> kept_genes = new ArrayList<String>();
		//randomize
		Collections.shuffle(gene_ids);
				
		//build the boards with random collections
		for(int board_id =1; board_id<=total/2; board_id++){
			Board board = new Board();
			board.setDataset(dataset);
			board.setRoom(room);
			List<Feature> bfs = new ArrayList<Feature>();
			List<String> unique_ids = new ArrayList<String>();		
			//add the rest
			for(int r=0; r<n_per_board;r++){
				unique_ids.add(gene_ids.get(0));
				kept_genes.add(gene_ids.get(0));
				gene_ids.remove(0);				
			}
			//add the features			
			for(String gene : unique_ids){
				bfs.add(weka.getFeatures().get(gene));
			}
			board.setFeatures(bfs);
			//test it
			execution base = weka.pruneAndExecuteWithUniqueIds(unique_ids, null, dataset);
			float base_score = (float)base.eval.pctCorrect();
			board.setBase_score(base_score);
			board.insert();
			System.out.println(board_id+"\t"+base_score+"\t"+gene_ids.size()+"\t");
		}
		//build second batch 
		//todo generalize
		Collections.shuffle(kept_genes);
		for(int board_id =1; board_id<=total/2; board_id++){
			Board board = new Board();
			board.setDataset(dataset);
			board.setRoom(room);
			List<Feature> bfs = new ArrayList<Feature>();
			List<String> unique_ids = new ArrayList<String>();		
			//add the rest
			for(int r=0; r<n_per_board;r++){
				unique_ids.add(kept_genes.get(0));
				kept_genes.remove(kept_genes.get(0));
			}
			//add the features			
			for(String gene : unique_ids){
				bfs.add(weka.getFeatures().get(gene));
			}
			board.setFeatures(bfs);
			//test it
			execution base = weka.pruneAndExecuteWithUniqueIds(unique_ids, null, dataset);
			float base_score = (float)base.eval.pctCorrect();
			board.setBase_score(base_score);
			board.insert();
			System.out.println(board_id+"\t"+base_score+"\t"+gene_ids.size()+"\t");
		}
	}
	
	/**
	 * Use an attribute ranking system to generate hopefully useful boards..
	 * @param train_file
	 * @param metadatafile
	 * @param n_per_board
	 * @throws Exception 
	 */
	public static void createAndSaveInterestingBoards(String train_file, int n_per_board, String dataset, String room, int total, int topper) throws Exception{	
		Weka weka = new Weka();
		weka.buildWeka(new FileInputStream(train_file), null, dataset);

		//produce the list of unique entrez gene ids

		List<String> gene_ids = new ArrayList<String>(weka.getFeatures().keySet());
		System.out.println("N gene ids: "+gene_ids.size());


		//get the genes sorted by max power of related probes etc.
		Map<String, Double> power_map = new HashMap<String, Double>();
		for(String gene : gene_ids){
			List<Attribute> atts = weka.getFeatures().get(gene).getDataset_attributes();
			double max_power = -10;
			for(Attribute a : atts){
				if(a.getReliefF()>max_power){
					max_power = a.getReliefF();
				}
			}
			power_map.put(gene, max_power);
		}
		List<String> sorted_genes = MapFun.sortMapByValue(power_map);
		//best on top
		Collections.reverse(sorted_genes);
		//randomize the other list
		Collections.shuffle(gene_ids);
				
		//build the boards by sampling topper from the top, then the rest at random
		for(int board_id =1; board_id<=total; board_id++){
			Board board = new Board();
			board.setDataset(dataset);
			board.setRoom(room);
			List<Feature> bfs = new ArrayList<Feature>();
			List<String> unique_ids = new ArrayList<String>();		
			//pop topper good ones
			for(int t=0;t<topper; t++){
				unique_ids.add(sorted_genes.get(0));
				//remove it from both lists
				gene_ids.remove(sorted_genes.get(0));
				sorted_genes.remove(0);
			}
			//add the rest
			for(int r=topper; r<n_per_board;r++){
				unique_ids.add(gene_ids.get(0));
				sorted_genes.remove(gene_ids.get(0));
				gene_ids.remove(0);
			}
			//add the features			
			for(String gene : unique_ids){
				bfs.add(weka.getFeatures().get(gene));
			}
			board.setFeatures(bfs);
			//test it
			execution base = weka.pruneAndExecuteWithUniqueIds(unique_ids, null, dataset);
			float base_score = (float)base.eval.pctCorrect();
			board.setBase_score(base_score);
			board.insert();
			System.out.println(board_id+"\t"+base_score+"\t"+gene_ids.size()+"\t"+sorted_genes.size()+"\t");
		}
	}
	
	
	public static void createAndSaveRandomBoards(String train_file, int n_per_board, String dataset, String room, int n_boards) throws Exception{	
		Weka weka = new Weka();
		weka.buildWeka(new FileInputStream(train_file), null, dataset);

		//produce the list of unique entrez gene ids

		List<String> gene_ids = new ArrayList<String>(weka.getFeatures().keySet());
		System.out.println("N gene ids: "+gene_ids.size());
		for(int b=0; b< n_boards; b++){
			Collections.shuffle(gene_ids);
			Board board = new Board();
			board.setRoom(room);
			board.setDataset(dataset);
			List<Feature> bfs = new ArrayList<Feature>();
			List<String> unique_ids = new ArrayList<String>();
			for(int i=0; i<n_per_board;i++){
				unique_ids.add(gene_ids.get(i));
				bfs.add(weka.getFeatures().get(gene_ids.get(i)));
			}
			execution base = weka.pruneAndExecuteWithUniqueIds(unique_ids, null, dataset);
			float base_score = (float)base.eval.pctCorrect();
			board.setBase_score(base_score);
			board.setFeatures(bfs);
			//System.out.println(base_score+" \n "+base.model.getClassifier().toString());
			board.insert();
		}

	}
	
	public static void createAndSaveSpecificBoard(String train_file, String dataset, String room, List<String> feature_ids) throws Exception{	
		Weka weka = new Weka();
		weka.buildWeka(new FileInputStream(train_file), null, dataset);	
		Board board = new Board();
		board.setRoom(room);
		board.setDataset(dataset);
		List<Feature> bfs = new ArrayList<Feature>();
		for(String fid : feature_ids){
			Feature f = weka.getFeatures().get(fid);
			f.getAllMetadataFromDb();
			bfs.add(f);
		}
		execution base = weka.pruneAndExecuteWithUniqueIds(feature_ids, null, dataset);
		float base_score = (float)base.eval.pctCorrect();
		board.setBase_score(base_score);
		board.setFeatures(bfs);
		System.out.println(base_score+" \n "+base.model.getClassifier().toString());
		board.insert();
		
	}
	



	/**
	 * Insert a new board.
	 * @throws SQLException 
	 */
	public int insert() throws SQLException{
		int newid = 0;
		JdbcConnection conn = new JdbcConnection();
		ResultSet generatedKeys = null; PreparedStatement pst = null;
		try {
			pst = conn.connection.prepareStatement(
					"insert into board (id, dataset, room, base_score, created) values(?,?,?,?,?)",
					Statement.RETURN_GENERATED_KEYS);
			pst.clearParameters();
			if(id>0){
				pst.setInt(1,getId());
			}else{
				pst.setString(1, null);
			}
			pst.setString(2,getDataset());
			pst.setString(3, getRoom());
			pst.setFloat(4,getBase_score());
			pst.setDate(5, getCreated());

			int affectedRows = pst.executeUpdate();
			if (affectedRows == 0) {
				throw new SQLException("Creating board failed, no rows affected.");
			}
			generatedKeys = pst.getGeneratedKeys();
			if (generatedKeys.next()) {
				setId(generatedKeys.getInt(1));
				newid = generatedKeys.getInt(1);
				for(Feature f : getFeatures()){
					conn.executeUpdate("insert into board_feature values("+newid+","+f.getId()+")");
				}
			} else {
				throw new SQLException("Creating board failed, no generated key obtained.");
			}
		} finally {
			if (generatedKeys != null) try { generatedKeys.close(); } catch (SQLException logOrIgnore) {}
			if (pst != null) try { pst.close(); } catch (SQLException logOrIgnore) {}
			if (conn.connection != null) try { conn.connection.close(); } catch (SQLException logOrIgnore) {}
		}
		return newid;
	}




	public void updateMaxScore(){
		if(getId()<1){
			System.out.println("Can't update without an id");
			return;
		}
		JdbcConnection conn = new JdbcConnection();
		try {
			PreparedStatement pst = conn.connection.prepareStatement("update board set max_score = ? where id = "+getId());
			pst.clearParameters();
			pst.setFloat(1,getMax_score());			
			pst.executeUpdate();
			pst.close();
			conn.connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

//	public List<Integer> getBoardpppScoresfromDb(int board_id){
//		List<Integer> board_scores = new ArrayList<Integer>();
//		String gethands = "select phenotype, game_type, board_id, score, win from hand where board_id = '"+board_id+"' and player_name != 'anonymous_hero'";
//		JdbcConnection conn = new JdbcConnection();
//		try {
//			PreparedStatement p = conn.connection.prepareStatement(gethands);
//			ResultSet hands = p.executeQuery();
//			while(hands.next()){
//				int training = hands.getInt("training_accuracy");
//				int cv = hands.getInt("cv_accuracy");
//				int win = hands.getInt("win");				
//				if(win>0){
//					if(training<0){
//						training = cv;
//					}
//					board_scores.add(training);
//				}
//			}
//			conn.connection.close();
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return board_scores;
//	}

	
	public static Set<Integer> getBoardsByPlayer(int player_id){
		Set<Integer> boards= new HashSet<Integer>();
		String getboards = "select distinct(board.id) from board, game where game.player1_id = "+player_id+" and board.id = game.board_id";
		JdbcConnection conn = new JdbcConnection();
		try {
			PreparedStatement p = conn.connection.prepareStatement(getboards);
			ResultSet bds = p.executeQuery();
			while(bds.next()){
				int id = bds.getInt("id");
				boards.add(id);
			}
			bds.close();
			conn.connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return boards;
	}
	
	public static Map<String,List<Integer>> getPlayerBoardScoresForWins(int board_id){
		Map<String,List<Integer>> player_board_scores = new HashMap<String,List<Integer>>();
		String gethands = "select * from game where win = 1 and board_id = '"+board_id+"' ";
		JdbcConnection conn = new JdbcConnection();
		try {
			PreparedStatement p = conn.connection.prepareStatement(gethands);
			ResultSet hands = p.executeQuery();
			while(hands.next()){
				int score = hands.getInt("p1_score");
				String player_id = hands.getString("player1_id");
				List<Integer> board_scores = player_board_scores.get(player_id);
				if(board_scores==null){
					board_scores = new ArrayList<Integer>();
				}
				board_scores.add(score);
				player_board_scores.put(player_id, board_scores);
			}
			hands.close();
			conn.connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return player_board_scores;
	}	

		
		public static List<Board> getBoardsByDatasetRoom(String dataset, String room){
			List<Board> boards = new ArrayList<Board>();
			JdbcConnection conn = new JdbcConnection();
			ResultSet rslt = conn.executeQuery("select * from board where dataset = '"+dataset+"' and room = '"+room+"' order by base_score desc");
			try {
				while(rslt.next()){
					Board board = new Board();
					board.setAvg_score(rslt.getFloat("avg_score"));
					board.setId(rslt.getInt("id"));
					board.setMax_score(rslt.getFloat("max_score"));
					board.setN_players(rslt.getInt("n_players"));
					board.setN_wins(rslt.getInt("n_wins"));
					board.setDataset(dataset);
					board.setRoom(rslt.getString("room"));
					board.setUpdated(rslt.getTimestamp("updated"));
					board.setBase_score(rslt.getFloat("base_score"));
					
//					ResultSet f_list = conn.executeQuery("select * from board_feature where board_id = "+board.getId());
//					List<Feature> fs = new ArrayList<Feature>();
//					while(f_list.next()){
//						fs.add(Feature.getByDbId(f_list.getInt("feature_id")));
//					}
//					board.setFeatures(fs);
					boards.add(board);
				}
				rslt.close();
				conn.connection.close();	
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return boards;
		}
	
		public static Board getBoardById(String id, boolean populate_metadata){
			JdbcConnection conn = new JdbcConnection();
			ResultSet rslt = conn.executeQuery("select * from board where id = '"+id+"'");
			try {
				if(rslt.next()){
					Board board = new Board();
					board.setAvg_score(rslt.getFloat("avg_score"));
					board.setId(rslt.getInt("id"));
					board.setMax_score(rslt.getFloat("max_score"));
					board.setN_players(rslt.getInt("n_players"));
					board.setN_wins(rslt.getInt("n_wins"));
					board.setDataset(rslt.getString("dataset"));
					board.setRoom(rslt.getString("room"));
					board.setUpdated(rslt.getTimestamp("updated"));
					board.setBase_score(rslt.getFloat("base_score"));
					
					ResultSet f_list = conn.executeQuery("select * from board_feature where board_id = "+board.getId());
					List<Feature> fs = new ArrayList<Feature>();
					while(f_list.next()){
						Feature f = Feature.getByDbId(f_list.getInt("feature_id"));
						if(populate_metadata){
							f.getAllMetadataFromDb();
						}
						fs.add(f);
					}
					board.setFeatures(fs);
					rslt.close();
					conn.connection.close();
					return board;
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		
	
	public List<String> getFeatureIds() {
		if(getFeatures()!=null){
			List<String> ids = new ArrayList<String>();
			for(Feature f : getFeatures()){
				ids.add(f.getUnique_id());
			}
		}
		return null;
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getN_players() {
		return n_players;
	}
	public void setN_players(int n_players) {
		this.n_players = n_players;
	}
	public int getN_wins() {
		return n_wins;
	}
	public void setN_wins(int n_wins) {
		this.n_wins = n_wins;
	}
	public float getAvg_score() {
		return avg_score;
	}
	public void setAvg_score(float avg_score) {
		this.avg_score = avg_score;
	}
	public float getMax_score() {
		return max_score;
	}
	public void setMax_score(float max_score) {
		this.max_score = max_score;
	}

	public Timestamp getUpdated() {
		return updated;
	}

	public void setUpdated(Timestamp updated) {
		this.updated = updated;
	}

	public float getBase_score() {
		return base_score;
	}


	public void setBase_score(float base_score) {
		this.base_score = base_score;
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

	public List<Feature> getFeatures() {
		return features;
	}

	public void setFeatures(List<Feature> features) {
		this.features = features;
	}


	public String getRoom() {
		return room;
	}


	public void setRoom(String room) {
		this.room = room;
	}






}
