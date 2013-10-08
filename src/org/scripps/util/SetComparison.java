/**
 * 
 */
package org.scripps.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashSet;
import java.util.Set;

import org.scripps.StatUtil;

/**
 * Implements a background-specific comparison between two sets.  (Currently scoped to Integers, but could be generalized easily)
 * Items in either set not in the background set do not get used in the comparison
 * @author bgood
 *
 */
public class SetComparison {
	public int a, b, c, d;
	public double fisherP;
	public double pct_agreement;
	public double kappa;
	public double pct_agreement_positive;
	public double pct_agreement_negative;
	public Set<Integer> set1;
	public Set<Integer> set2;
	public Set<Integer> backgroundset;
	public Set<Integer> intersection;
	public Set<Integer> union;
	public Set<Integer> set1notset2;
	public Set<Integer> set2notset1;

	public SetComparison(int a, int b, int c, int d) {
		setNumbers(a, b, c, d);
	}

	public SetComparison(Set<Integer> test, Set<Integer> against, Set<Integer> background){
		set1 = new HashSet<Integer>(test);
		set2 = new HashSet<Integer>(against);
		backgroundset = new HashSet<Integer>(background);
		//remove any genes not in background
		set2.retainAll(background);
		//				System.out.println("After removing non-background, compareto contains: "+against.size());
		//this should not be necessary, but just to be sure
		set1.retainAll(background);
		//				System.out.println("After removing non-background, test set contains: "+testgenes.size());
		//build 2 by 2 table
		//a tp = n genes in both sets
		Set<Integer> tp_a = new HashSet<Integer>(set2);
		tp_a.retainAll(set1);
		intersection = tp_a;
		//b fp = n genes in the testset and not in the against set
		Set<Integer> fp_b = new HashSet<Integer>(set1);
		fp_b.removeAll(set2);
		set1notset2 = new HashSet<Integer>(fp_b);
		//c fn = n genes in against set and not in the test set
		Set<Integer> fn_c = new HashSet<Integer>(set2);
		fn_c.removeAll(set1);
		set2notset1 = new HashSet<Integer>(fn_c);
		union = new HashSet<Integer>(tp_a);
		union.addAll(fp_b); union.addAll(fn_c);
		//d tn = n genes not in the test set and not in the against set
		Set<Integer> tn_d = new HashSet<Integer>(background);
		tn_d.removeAll(set2);  tn_d.removeAll(set1);
		//		System.out.println("\n"+tp_a.size()+"\t"+fp_b.size()+"\n"+fn_c.size()+"\t"+tn_d.size());
		setNumbers(tp_a.size(), fp_b.size(), fn_c.size(), tn_d.size());
	}
	
	public void setNumbers(int a, int b, int c, int d) {
		this.a = a;
		this.b = b;
		this.c = c;
		this.d = d;
		setFisherP();
		setPctAgree();
		setKappa();
		setPctAgreePositive();
		setPctAgreeNegative();
	}
	

	public void setFisherP(){
		//test with fisherexact
		fisherP = StatUtil.fishersExact2tailed(a, b, c, d);
	}

	public void setPctAgreePositive(){
		pct_agreement_positive = (double)(a)/(double)(a+b+c);
	}

	public void setPctAgreeNegative(){
		pct_agreement_negative = (double)(d)/(double)(d+b+c);
	}

	public void setPctAgree(){
		pct_agreement = (double)(a+d)/(double)(a+b+c+d);
	}

	public void setKappa(){
		double prA = (double)(a+d)/(double)(a+b+c+d); //actual prob of agreement
		double prE = ((double)(a+b)/(double)(a+b+c+d))*((double)(a+c)/(double)(a+b+c+d)); //chance of random agreement based on + rates
		kappa = (prA - prE)/(1 - prE);
	}

	public String get2by2string(){
		String row = a+"\t"+b+"\n"+c+"\t"+d+"\nFisherP\t"+fisherP+"\tpct_agree\t"+pct_agreement+"\nkappa\t"+kappa+"\tpct_positive_agree\t"+pct_agreement_positive+"\npact_negative_agree\t"+pct_agreement_negative;
		return row;
	}
	
	public String getString(){
		String row = a+"\t"+b+"\t"+c+"\t"+d+"\t"+fisherP+"\t"+pct_agreement+"\t"+kappa+"\t"+pct_agreement_positive+"\t"+pct_agreement_negative;
		return row;
	}

	public String getMatrixCell(){
		NumberFormat percentForm = NumberFormat.getPercentInstance();
		DecimalFormat sci = (DecimalFormat) DecimalFormat.getNumberInstance();
		sci.applyPattern("0.###E0");
		String cell = a+", "+percentForm.format(pct_agreement_positive)+", "+sci.format(fisherP);
		if(fisherP<=0.05){
			cell+= "__";
		}
		return cell;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Set<Integer> t = new HashSet<Integer>();
		t.add(1); t.add(2); t.add(3);
		System.out.println("set1 "+t);
		Set<Integer> a = new HashSet<Integer>();
		a.add(3); a.add(4); a.add(5);
		System.out.println("set2 "+a);
		Set<Integer> b = new HashSet<Integer>();
		b.add(2); b.add(3); b.add(4); b.add(5); b.add(6);
		System.out.println("background "+b);
		SetComparison s = new SetComparison(t, a, b);
		System.out.println("set1 distinct: "+s.set1notset2+"\tset2 distinct: "+s.set2notset1);
		System.out.println(s.get2by2string());
		
	}

}
