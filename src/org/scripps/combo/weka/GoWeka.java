/**
 * 
 */
package org.scripps.combo.weka;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.scripps.combo.weka.Weka.card;
import org.scripps.ontologies.go.Annotations;
import org.scripps.util.Gene;
import org.scripps.util.MyGeneInfo;

import weka.classifiers.Classifier;
import weka.classifiers.trees.J48;
import weka.core.Attribute;



/**
 * @author bgood
 *
 */
public class GoWeka extends Weka {
	public Map<String, Set<String>> go2genes;
	public Map<String, String> acc2name;
	/**
	 * 
	 */
	public GoWeka() {
		super(false);//load unfiltered data (filter seems to screw things up somewhere here)		
		String annotations = "/usr/local/data/go2gene_3_51.txt";		
		try {
			go2genes = Annotations.readCachedGoAcc2Genes(annotations);
			acc2name = Annotations.readCachedGoName(annotations);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(go2genes.get("GO:0070830"));
		eval_method = "cross_validation";//"test_set";//"training_set";
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		GoWeka w = new GoWeka();
		
		List<card> gocars = w.getRandomGoCards(5,1);
		for(card c : gocars){
			System.out.println(c.acc+" "+c.geneids);
			Weka.execution e = w.limitByGoAndExecute(c.acc, new J48());
			if(e!=null){
				System.out.println(e.toString());
			}else{
				System.out.println("could not run "+c.acc);
			}
		}
	}

	/**
	 * a card represents a collection of attribute in the training set determined by a GO category
	 * @author bgood
	 *
	 */
	public class card{
		public String acc;
		public String name;
		public String group;
		public Set<String> geneids;
		
		public card(String acc, String name, Set<String> geneids) {
			this.group = name.substring(0, name.indexOf('	')).trim();
			this.acc = acc;
			this.name = name.substring(name.indexOf('	')).trim();
			this.geneids = geneids;
		}
		public String getAcc() {
			return acc;
		}
		public void setAcc(String acc) {
			this.acc = acc;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public Set<String> getGeneids() {
			return geneids;
		}
		public void setGeneids(Set<String> geneids) {
			this.geneids = geneids;
		}
		public String getGroup() {
			return group;
		}
		public void setGroup(String group) {
			this.group = group;
		}
		
	}

	/**
	 * Get a random selection of n go terms from our annotation set
	 * @param n
	 * @return
	 */
	public List<card> getRandomGoCards(int n, int ranseed){
		rand.setSeed((long)ranseed);
		Set<String> u = new HashSet<String>();
		List<card> cards = new ArrayList<card>();
		for(int i=0;i<n;i++){
			List<String> keys = new ArrayList<String>(acc2name.keySet());
			int randomNum = rand.nextInt(keys.size()-1);
			if(u.contains(randomNum+"")){
				i--;
			}else{
				String acc = keys.get(randomNum);
				String name = acc2name.get(acc);
				Set<String> genes = go2genes.get(acc);
				Set<String> gene_symbols = new HashSet<String>();
				//get mygene.info info
				try {
					Map<String, Gene> gene_info = MyGeneInfo.getBatchGeneInfo(genes, true);
					for(Gene gene : gene_info.values()){
						gene_symbols.add("<a target=\"blank\" href=http://www.ncbi.nlm.nih.gov/gene/"+gene.getGeneID()+">"+gene.getGeneSymbol()+"</a>");
					}
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				card c = new card(acc, name, gene_symbols);
				cards.add(c);
				u.add(randomNum+"");
			}
		}
		return cards;
	}
	
	/**
	 * Given a go accession, get the right list of genes, get the related attributes in this dataset and run 
	 * the requested classifier
	 */
	
	public Weka.execution limitByGoAndExecute(String go_acc, Classifier wekamodel){
		Set<String> genes = go2genes.get(go_acc);
		String atts = "";
		int found = 0;
		for(String gene : genes){
			String id = gene;
			List<Weka.card> cards = geneid_cards.get(id);
			if(cards!=null){
				for(Weka.card card : cards){
					atts+=card.getAtt_index()+",";
					found++;
				}
			}
		} 
		if(found>1){
			Weka.execution e = pruneAndExecute(atts, wekamodel);
			return e;
		}else{
			return null;
		}
	}
	
	public Weka.execution limitByGoSetAndExecute(Set<String> go_accs, Classifier wekamodel){
		Set<String> genes = new HashSet<String>();
		for(String acc : go_accs){	
			if(go2genes.get(acc)!=null){
				genes.addAll(go2genes.get(acc));
			}
		}
		String atts = "";
		int found = 0;
		for(String gene : genes){
			String id = gene;
			List<Weka.card> cards = geneid_cards.get(id);
			if(cards!=null){
				for(Weka.card card : cards){
					atts+=card.getAtt_index()+",";
					found++;
				}
			}
		} 
		if(found>1){
			Weka.execution e = pruneAndExecute(atts, wekamodel);
			return e;
		}else{
			return null;
		}
	}
}
