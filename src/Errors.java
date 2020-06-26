import java.math.RoundingMode;
import java.text.DecimalFormat;

import javax.swing.JOptionPane;

public enum Errors {
	
	AER1("AER1", "Entered Term is Finalized"),
	AER2("AER2", "Must Finalize Previous Term"),
	AER3("AER3", "Category Name Cannot Be Numbers"),
	AER4("AER4", "Category Name Already Exists"),
	AER5("AER5", "Cannot Have Negative Weight"),
	AER6("AER6", "Must Create a Grade Scale"),
	AER7("AER7", "Element Does Not Exist"),
	AER8("AER8", "Cannot Remove Finalized Course"),
	AER9("AER9", "Cannot Edit Finalized Course"),
	AER10("AER10", "No Categories Available"),
	AM1("AM1", "Negative Values Not Accepted"),
	AM2("AM2", "No Courses Available"),
	AM3("AM3", "Must Add Category"),
	FG1("FG1", "Cannot Finalize with Final Grade = In Progress"),
	FG2("FG2", "All Courses Finalized"),
	SS1("SS1", "Cannot Have Negative Bonus"),
	SS2("SS2", "Invalid Grade Scale"),
	SS3("SS3", "No Grade Scales Available"),
	SS4("SS4", "Grade Scale In Use"),
	SS5("SS5", "Check Settings Before Adding Course"),
	ML1("ML1", "Missing Information"),
	ML2("ML2", "Number Format Exception"),
	ML3("ML3", "No Breakdown to Formulate"),
	ML4("ML4", "No Grades for Calculation");
	
	private final String code, msg;
	
	Errors(String code, String msg) {
		this.code = code;
		this.msg = msg;
	}
	
	public String getMsg() {
		return msg;
	}
	
	public void displayErrorMsg() {
		JOptionPane.showMessageDialog(null, "User Action Denied\n"+ "Error Code [" + 
				code + "]\n" + msg, "System Notification", JOptionPane.ERROR_MESSAGE);
	}
	
//public void displayWhatIfGPA() {
//		
//		String term = getUnfinalizedTerm();
//		
//		double creditSum = 0;
//		double qualitySum = 0;
//		double nonGpaSum = 0;
//		double failCreditSum = 0;
//		double tempTotalFCreditSum = totalFCreditSum;
//		double tempNonGpaCreditTotal = nonGpaCreditTotal;
//		
//		for(int i = 0; i < cdtm.getRowCount(); i++) {
//			if(cdtm.getValueAt(i, 8).equals(term + "") && cdtm.getValueAt(i, 6).equals("Letter") && !cdtm.getValueAt(i, 7).equals("F")) {
//				qualitySum += Double.parseDouble((String)cdtm.getValueAt(i, 4)) * letToQual(cdtm.getValueAt(i, 3)+"",(String)cdtm.getValueAt(i, 7));
//				creditSum += Double.parseDouble((String)cdtm.getValueAt(i, 4));
//			}
//			
//			else if(cdtm.getValueAt(i, 8).equals(term + "") && cdtm.getValueAt(i, 6).equals("Letter") && cdtm.getValueAt(i, 7).equals("F")) {
//				qualitySum += Double.parseDouble((String)cdtm.getValueAt(i, 4)) * letToQual(cdtm.getValueAt(i, 3)+"",(String)cdtm.getValueAt(i, 7));
//				failCreditSum += Double.parseDouble((String)cdtm.getValueAt(i, 4));
//				totalFCreditSum += Double.parseDouble((String)cdtm.getValueAt(i, 4));
//			}
//			
//			else if(cdtm.getValueAt(i, 8).equals(term + "") && cdtm.getValueAt(i, 6).equals("P/NP") && cdtm.getValueAt(i, 7).equals("P")) {
//				creditSum += Double.parseDouble((String)cdtm.getValueAt(i, 4));
//				nonGpaSum += Double.parseDouble((String)cdtm.getValueAt(i, 4));
//				nonGpaCreditTotal += Double.parseDouble((String)cdtm.getValueAt(i, 4));
//			}
//			
//			else if(cdtm.getValueAt(i, 8).equals(term + "") && cdtm.getValueAt(i, 6).equals("Notation") && cdtm.getValueAt(i, 7).equals("TR")) {
//				creditSum += Double.parseDouble((String)cdtm.getValueAt(i, 4));
//				nonGpaSum += Double.parseDouble((String)cdtm.getValueAt(i, 4));
//				nonGpaCreditTotal += Double.parseDouble((String)cdtm.getValueAt(i, 4));
//			}
//		}
//		
//		if(((String) cdtm.getValueAt(cdtm.getRowCount() - 1, 9)).equals("Finalized")) {
//			
//				DecimalFormat rounder = new DecimalFormat("#.####");
//				rounder.setRoundingMode(RoundingMode.HALF_UP);
//			
//				gdtm.addRow(new Object[] {"", "", "", "", "", "", "", "", "", ""});		
//				String gpa = rounder.format(qualitySum / (creditSum + failCreditSum - nonGpaSum)) + "";
//				
//				if(!gpa.contains("."))
//					gpa = gpa + ".0";
//				
//				String creditSumString = rounder.format(creditSum) + "";
//				
//				String qualitySumString = rounder.format(qualitySum) + "";
//				
//				gdtm.addRow(new Object[] {"Term Credits Earned", creditSumString, "", 
//						"General Information", "", "", "Term GPA", gpa, "What If GPA (If Finalized)"});
//				
//				double allQualitySum = 0;
//				double allCreditSum = 0;
//				for(int i = 0; i < cdtm.getRowCount(); i++)
//					if(((String) cdtm.getValueAt(i, 0)).equals("Term Credits Earned") && ((String) cdtm.getValueAt(i, 4)).equals("")) {
//						allQualitySum += Double.parseDouble(cdtm.getValueAt(i, 3) + "");
//						allCreditSum += Double.parseDouble(cdtm.getValueAt(i, 1) + "");
//					}
//				
//				String allCreditSumString = rounder.format(allCreditSum) + "";
//				
//				String allQualitySumString = rounder.format(allQualitySum) + "";
//				
//				String totalGpa = rounder.format(allQualitySum / (allCreditSum + totalFCreditSum - nonGpaCreditTotal)) + "";
//				
//				if(!totalGpa.contains("."))
//					totalGpa = totalGpa + ".0";
//				
//				gdtm.addRow(new Object[] {"Total Credits Earned", allCreditSumString, "", 
//						"General Information", "", "", "Cumulative GPA", gpa, "What If GPA (If Finalized)"});	
//				
//				for(int i = 0; i < cdtm.getRowCount(); i++) {
//					if(cdtm.getValueAt(i, 0).equals("Total Credits Earned") || cdtm.getValueAt(i, 0).equals("Term Credits Earned"))
//						courseList.addRowSelectionInterval(i, i);
//				}
//				
//				gdtm.addRow(new Object[] {"", "", "", "", "", "", "", "", "", ""});
//				
//				nonGpaCreditTotal = tempNonGpaCreditTotal;
//				totalFCreditSum = tempTotalFCreditSum;
//		}
//	}
}
