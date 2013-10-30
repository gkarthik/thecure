/**
 * 
 */
package org.scripps.combo.evaluation;

/**
 * @author bgood
 *
 */
public class ClassifierEvaluation {
	int accuracy;
	String modelrep;
	
	public ClassifierEvaluation(int accuracy, String modelrep) {
		super();
		this.accuracy = accuracy;
		this.modelrep = modelrep;
	}
	public int getAccuracy() {
		return accuracy;
	}
	public void setAccuracy(int accuracy) {
		this.accuracy = accuracy;
	}
	public String getModelrep() {
		return modelrep;
	}
	public void setModelrep(String modelrep) {
		this.modelrep = modelrep;
	}
	
}
