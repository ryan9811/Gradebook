import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Ryan Hudson
 * Grade Calculator v5
 */
public class Gradebook extends JFrame implements ActionListener {

	private JTextField identifierInput; // The text field
	
	private static DefaultTableModel cdtm; // Course Default Table Model
	private static DefaultTableModel gdtm; // Grade Default Table Model
	
	private JTable courseList, gradeList; // JTables containing grades and courses
	
	private int assignmentCode; // Code unique to each grade/assignment
	private int courseCode; // Code unique to each course (AKA identifier)
	
	private int nonGpaCreditTotal; // The total amount of credits not applied to the gpa
	
	private JFrame frame; // The frame that holds the tables and buttons
	
	private Hashtable<String, ArrayList<String>> categories; // Contains the category weights of each course
	
	private JFileChooser myJFileChooser = new JFileChooser(new File("."));
	
	public Gradebook() {

		// Frame that holds everything
        frame = new JFrame(); 
        
        frame.setTitle("Grade Calculator v5"); 
        
        frame.setVisible(true); 
        
        frame.setResizable(false);
        
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);;
  
        // ------------------------Creating the table for the list of courses------------------------
        courseList = new JTable();
        cdtm = new DefaultTableModel(0,0);
        
        String courseHeader[] = new String[] { "Course Title", "Professor", "Day/Time", "Identifier", "Credits", "Numeric Grade", "Grade Mode", "Final Grade", "Term", "Status" };
        categories = new Hashtable<String, ArrayList<String>>();
        
        cdtm.setColumnIdentifiers(courseHeader);
        courseList.setModel(cdtm);
        courseCode = 11111;
        
        courseList.setShowVerticalLines(true);
        courseList.setColumnSelectionAllowed(false);
        courseList.getTableHeader().setReorderingAllowed(false);
        courseList.getTableHeader().setResizingAllowed(false);
        courseList.setEnabled(false);
  
        // adding it to JScrollPane 
        JScrollPane spc = new JScrollPane(courseList); 
        frame.add(spc, BorderLayout.NORTH); 
        courseList.getColumnModel().getColumn(0).setPreferredWidth(200);
        courseList.getColumnModel().getColumn(1).setPreferredWidth(200);
        courseList.getColumnModel().getColumn(2).setPreferredWidth(150);
        courseList.getColumnModel().getColumn(3).setPreferredWidth(100);
        courseList.getColumnModel().getColumn(4).setPreferredWidth(50);
        courseList.getColumnModel().getColumn(5).setPreferredWidth(100);
        courseList.getColumnModel().getColumn(6).setPreferredWidth(75);
        courseList.getColumnModel().getColumn(7).setPreferredWidth(75);
        courseList.getColumnModel().getColumn(8).setPreferredWidth(30);
        courseList.getColumnModel().getColumn(9).setPreferredWidth(125);
        
        // ------------------------Creating the panel for the buttons------------------------
        JPanel buttons = new JPanel();
        buttons.setVisible(true);
        
        // Initialize buttons and add action listeners
        JButton addCourse = new JButton("Add Course");
        addCourse.addActionListener(this);
        
        JButton removeCourse = new JButton("Remove Element");
        removeCourse.addActionListener(this);
        
        JButton editCourse = new JButton("Edit Course");
        editCourse.addActionListener(this);
        
        JButton enterGrade = new JButton("Enter Grade");
        enterGrade.addActionListener(this);
        
        JButton finalizeGrades = new JButton("Finalize Grades");
        finalizeGrades.addActionListener(this);
        
        JButton saveChanges = new JButton("Import/Export");
        saveChanges.addActionListener(this);
        
        JButton manualOverride = new JButton("Manual Override");
        manualOverride.addActionListener(this);
        
        // Create the textfield for allowing edits
        JLabel identifier = new JLabel("Identifier/Code: ");
        identifierInput = new JTextField();
        identifierInput.setPreferredSize(new Dimension(150,20));
        identifierInput.setEditable(true);
        identifierInput.setBounds(10,10,300,50);
        identifierInput.setSize(200, 20);
        
        // Add the buttons to the panel
        buttons.add(addCourse);
        buttons.add(removeCourse);
        buttons.add(editCourse);
        buttons.add(enterGrade);
        buttons.add(finalizeGrades);
        buttons.add(manualOverride);
        buttons.add(saveChanges);
        buttons.add(identifier);
        buttons.add(identifierInput);
        
        // Add the panel to the frame
        frame.add(buttons, BorderLayout.CENTER);
        
        // ------------------------Creating the table for the list of grades/assignments------------------------
        gradeList = new JTable();
        gdtm = new DefaultTableModel(0,0);
        
        assignmentCode = 11111;
        nonGpaCreditTotal = 0;
        
        String gradeHeader[] = new String[] { "Course Title", "Identifier", "Assignment Code", "Category", "Category Weight", "Points Earned", "Total Points", "Grade", "Comment" };
        
        gdtm.setColumnIdentifiers(gradeHeader);
        gradeList.setModel(gdtm);
        
        gradeList.setShowVerticalLines(true);
        gradeList.setColumnSelectionAllowed(false);
        gradeList.getTableHeader().setReorderingAllowed(false);
        gradeList.getTableHeader().setResizingAllowed(false);
        gradeList.setEnabled(false);
        
        JScrollPane spg = new JScrollPane(gradeList); 
        frame.add(spg, BorderLayout.SOUTH);
        
        frame.pack();

	}
	
	/**
	 * Prompts the JFileChooser where files can be selected by the user for export
	 */
	public void saveTable() {
		
		if(myJFileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			saveTable(myJFileChooser.getSelectedFile());
		}
	}
	
	/**
	 * Writes the data in the JTables to a file
	 * @param file the file to be written to/exported to
	 */
	public void saveTable(File file) {
		
		try {
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
			out.writeObject(cdtm.getDataVector()); // Write the data from the course table
			out.writeObject(getColumnNamesC());
			out.writeObject(gdtm.getDataVector()); // Write the data from the grade table
			out.writeObject(getColumnNamesG());
			out.writeObject(categories); // Write the category weightings hash table
			out.close();		
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Gets the column names of the course table into a vector format so it can be written
	 * to a file.
	 * @return a string vector containing the column names of the course table
	 */
	public Vector<String> getColumnNamesC() {
		
		Vector<String> columnNames = new Vector<String>();
		for(int i = 0; i < courseList.getColumnCount(); i++)
			columnNames.add(courseList.getColumnName(i) + "");
		return columnNames;
	}
	
	/**
	 * Gets the column names of the grades table into a vector format so it can be written
	 * to a file.
	 * @return a string vector containing the column names of the grades table
	 */
	public Vector<String> getColumnNamesG() {
		
		Vector<String> columnNames = new Vector<String>();
		for(int i = 0; i < gradeList.getColumnCount(); i++)
			columnNames.add(gradeList.getColumnName(i) + "");
		return columnNames;
	}
	
	/**
	 * Prompts the JFileChooser where files can be selected by the user for import
	 */
	public void loadTable() {
		
		if(myJFileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
			loadTable(myJFileChooser.getSelectedFile());
	}
	
	/**
	 * Reads the data into the JTables from a file
	 * @param file the file to be read from/imported from
	 */
	public void loadTable(File file) {
		
		try {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
			Vector rowDataC = (Vector) in.readObject();
			Vector columnNamesC = (Vector) in.readObject();
			Vector rowDataG = (Vector) in.readObject();
			Vector columnNamesG = (Vector) in.readObject();
			categories = (Hashtable<String, ArrayList<String>>) in.readObject();
			cdtm.setDataVector(rowDataC, columnNamesC);
			gdtm.setDataVector(rowDataG, columnNamesG);
			revertTableSettings();
			in.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Calculates the final grade for a specified course from the grades table and changes the Numeric Grade
	 * and Final Grade fields in the course table
	 * @param identifier the unique ID of the course
	 */
	public void calculateGrade(String identifier) {
		// For a class
		// Figure out which categories have grades so far
		// Total up those categories weights
		// For each category, sum up points earned and total points, multiply by categoryWeight/sumCategoryWeightsUsed
		// Change the grade in the table
		
		if(!gradeExists(identifier))
			for(int i = 0; i < cdtm.getRowCount(); i++)
				if(cdtm.getValueAt(i, 3).equals(identifier)) {
					cdtm.setValueAt("n/a", i, 5);
					cdtm.setValueAt("In Progress", i, 7);
					return;
				}
			
		double sumCategoryWeightsUsed = 0;
		double categoryWeight = 0;
		double sumPointsEarned = 0;
		double sumTotalPoints = 0;
		double finalGrade = 0;
		
		ArrayList<String> finishedCats = new ArrayList<String>();
		String category = "";
		for(int i = 0; i < gdtm.getRowCount(); i++)
			if(gdtm.getValueAt(i,1).equals(identifier) && !finishedCats.contains(gdtm.getValueAt(i, 3))) {
				category = gdtm.getValueAt(i, 3) + "";
				sumCategoryWeightsUsed += Double.parseDouble(gdtm.getValueAt(i, 4) + "");
				System.out.println("sumCategoryWeightsUsed: " + sumCategoryWeightsUsed);
				finishedCats.add(category);
			}
		
		for(int i = 0; i < finishedCats.size(); i++) {
			for(int j = 0; j < gdtm.getRowCount(); j++) {
				if(gdtm.getValueAt(j, 3).equals(finishedCats.get(i))) {
					categoryWeight = Double.parseDouble(gdtm.getValueAt(j, 4) + "");
					System.out.println("categoryWeight: " + categoryWeight);
				}
				if(gdtm.getValueAt(j,1).equals(identifier) && finishedCats.get(i).equals(gdtm.getValueAt(j, 3))) {
					sumPointsEarned += Double.parseDouble(gdtm.getValueAt(j, 5) + "");
					System.out.println("sumPointsEarned: " + sumPointsEarned);
					sumTotalPoints += Double.parseDouble(gdtm.getValueAt(j, 6) + "");
					System.out.println("sumTotalPoints: " + sumTotalPoints);
				}
			}	
			finalGrade += (sumPointsEarned / sumTotalPoints) * (categoryWeight / sumCategoryWeightsUsed) * 100;
			System.out.println(finalGrade);
			sumPointsEarned = 0;
			sumTotalPoints = 0;
		}

		String gMode = "";
		for(int i = 0; i < cdtm.getRowCount(); i++)
			if(cdtm.getValueAt(i, 3).equals(identifier))
				gMode = cdtm.getValueAt(i, 6) + "";
		
		String letGrade = numToLet(finalGrade, gMode);
		
		for(int i = 0; i < cdtm.getRowCount(); i++)
			if(cdtm.getValueAt(i, 3).equals(identifier)) {
				cdtm.setValueAt(finalGrade + "", i, 5);
				cdtm.setValueAt(letGrade, i, 7);
			}
	}
	
	/**
	 * Helper method to convert a numeric grade to a letter grade
	 * @param grade the grade to be converted
	 * @param gMode the grade mode
	 * @return the letter equivalent of the grade
	 */
	public String numToLet(double grade, String gMode) {
		
		if(gMode.equals("Letter")) {
			if(grade >= 94) return "A";
			else if(grade >= 90) return "A-";
			else if(grade >= 87) return "B+";
			else if(grade >= 84) return "B";
			else if(grade >= 80) return "B-";
			else if(grade >= 77) return "C+";
			else if(grade >= 74) return "C";
			else if(grade >= 70) return "C-";
			else if(grade >= 67) return "D+";
			else if(grade >= 64) return "D";
			else if(grade >= 60) return "D-";
			else return "F";
		}
		else if(gMode.equals("P/NP")) {
			if(grade >= 60) return "P";
			else return "NP";
		}
		return "";
	}
	
	/**
	 * Tells whether or not there are any grades associated with a given course
	 * @param identifier the unique ID of the course
	 * @return whether or not there are any grades associated with a given course
	 */
	public boolean gradeExists(String identifier) {
		
		for(int i = 0; i < gdtm.getRowCount(); i++)
			if(gdtm.getValueAt(i, 1).equals(identifier))
				return true;
		return false;
	}
	
	/**
	 * Adds a new course to the course table
	 */
	public void addCourse() {	
		
		String title = JOptionPane.showInputDialog("Enter Course Title");
		if(title == null) {
			JOptionPane.showMessageDialog(null, "Action Cancelled");
			return;
		}
		
		String prof = JOptionPane.showInputDialog("Enter Professor Name");
		if(prof == null) {
			JOptionPane.showMessageDialog(null, "Action Cancelled");
			return;
		}
		
		String time = JOptionPane.showInputDialog("Enter Course Day/Time");
		if(time == null) {
			JOptionPane.showMessageDialog(null, "Action Cancelled");
			return;
		}
		
		courseCode += (int) (Math.random() * 50 + 1);
		String identifier = "C" + courseCode;
		
		String[] creditsChoices = {"0", "1", "2", "3", "4", "5","6"};
		String credits = (String) JOptionPane.showInputDialog(null, "Select Number of Credits", "Course Master", JOptionPane.QUESTION_MESSAGE, null, creditsChoices, creditsChoices[3]);
		if(credits == null) {
			JOptionPane.showMessageDialog(null, "Action Cancelled");
			return;
		}
		
		String numGrade = "n/a";
		
		String[] gModeChoices = {"Letter", "P/NP", "Notation"};
		String gMode = (String) JOptionPane.showInputDialog(null, "Select Grade Mode", "Course Master", JOptionPane.QUESTION_MESSAGE, null, gModeChoices, gModeChoices[0]);
		if(gMode == null) {
			JOptionPane.showMessageDialog(null, "Action Cancelled");
			return;
		}
		
		String[] fGradeChoicesC = {"In Progress", "A","A-","B+","B","B-","C+","C","C-","D+","D","D-","F"};
		String[] fGradeChoicesP = {"In Progress", "P", "NP"};
		String[] notationChoices = {"TR","I","W","Z"};
		String fGrade;
		if(gMode.equalsIgnoreCase("Letter"))
			fGrade = (String) JOptionPane.showInputDialog(null, "Select Final Grade", "Course Master", JOptionPane.QUESTION_MESSAGE, null, fGradeChoicesC, fGradeChoicesC[0]);
		else if(gMode.equalsIgnoreCase("P/NP"))
			fGrade = (String) JOptionPane.showInputDialog(null, "Select Final Grade", "Course Master", JOptionPane.QUESTION_MESSAGE, null, fGradeChoicesP, fGradeChoicesP[0]);
		else
			fGrade = (String) JOptionPane.showInputDialog(null, "Select Final Grade", "Course Master", JOptionPane.QUESTION_MESSAGE, null, notationChoices, notationChoices[0]);
		if(fGrade == null) {
			JOptionPane.showMessageDialog(null, "Action Cancelled");
			return;
		}
		
		if(!gMode.equals("Notation") && fGrade.equals("In Progress")) {
			String category = "";
			String catWeight = "";
			ArrayList<String> catsAndWeights = new ArrayList<String>();
			category = JOptionPane.showInputDialog("Enter a grade weight category.");
			
			if(containsNumbers(category)) {
				JOptionPane.showMessageDialog(null, "User Action Denied\nReason:\nCategory Name Cannot Contain Numbers");
				return;
			}
				
			if(category == null) {
				JOptionPane.showMessageDialog(null, "Action Cancelled");
				return;
			}
			catWeight = JOptionPane.showInputDialog("Enter Category Weight (ex. 15)");
			if(catWeight == null) {
				JOptionPane.showMessageDialog(null, "Action Cancelled");
				return;
			}
			try {
				double testError = Double.parseDouble(catWeight);
			} catch (NumberFormatException e) {
				JOptionPane.showMessageDialog(null, "User Action Denied\nReason:\nNumber Format Exception");
				return;
			}
			catsAndWeights.add(category);
			catsAndWeights.add(catWeight);
			int yesNo = 0;
			while(yesNo == 0) {
				yesNo = JOptionPane.showConfirmDialog(null, "Would you like to enter another category?");
				if(yesNo == 0) {
					category = JOptionPane.showInputDialog("Enter Category Name");
					
					if(containsNumbers(category)) {
						JOptionPane.showMessageDialog(null, "User Action Denied\nReason:\nCategory Name Cannot Contain Numbers");
						return;
					}
					
					catWeight = JOptionPane.showInputDialog("Enter Category Weight (ex. 15)");
					try {
						double testError = Double.parseDouble(catWeight);
					} catch (NumberFormatException e) {
						JOptionPane.showMessageDialog(null, "User Action Denied\nReason:\nNumber Format Exception");
						return;
					}
					catsAndWeights.add(category);
					catsAndWeights.add(catWeight);
				}
				else if(yesNo == JOptionPane.CANCEL_OPTION){
					JOptionPane.showMessageDialog(null, "Action Cancelled");
					return;
				}
			}
			categories.put(identifier, catsAndWeights);
		}
	
		String year = JOptionPane.showInputDialog("Enter Term Number");
		
		try {
			double testError = Double.parseDouble(year);
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(null, "User Action Denied\nReason:\nNumber Format Exception");
			return;
		}
		
		if(isTermFinalized(year)) {
			JOptionPane.showMessageDialog(null, "User Action Denied\nReason:\nEntered Term is Finalized");
			return;
		}
		
		if(!getUnfinalizedTerm().equals(year) && existsUnfinalizedTerm()) {
			JOptionPane.showMessageDialog(null, "User Action Denied\nReason:\nMust Finalize Previous Term");
			return;
		}
		
		if(year == null) {
			JOptionPane.showMessageDialog(null, "Action Cancelled");
			return;
		}
		
		String status;
		if(fGrade.equalsIgnoreCase("In Progress")) 
			status = "In Progress";
		else status = "Manual Entry";

		cdtm.addRow(new Object[] {title, prof, time, identifier, credits, numGrade, gMode, fGrade, year, status});
	}
	
	/**
	 * Tells whether or not there is an unfinalized term. If there is, grades can be finalized and a new course
	 * can be added for THAT TERM ONLY
	 * @return whether or not there is an unfinalized term
	 */
	public boolean existsUnfinalizedTerm() {
		
		for(int i = 0; i < cdtm.getRowCount(); i++)
			if(cdtm.getValueAt(i, 9).equals("Manual Entry") || cdtm.getValueAt(i, 9).equals("In Progress"))
				return true;
		return false;
	}
	
	/**
	 * Tells whether or not a term is finalized. If it is, a new course should not be able to be added to that term.
	 * @param term the term being checked for finalization
	 * @return whether or not the term is finalized
	 */
	public boolean isTermFinalized(String term) {
		
		for(int i = 0; i < cdtm.getRowCount(); i++)
			if(cdtm.getValueAt(i, 8).equals(term) && cdtm.getValueAt(i, 9).equals("Finalized"))
				return true;
		return false;
	}
	
	/**
	 * Gets the unfinalized term of which there can only be 1. Grades cannot be entered for new terms
	 * until all previous terms have been finalized.
	 * @return the unfinalized term
	 */
	public String getUnfinalizedTerm() {
		
		for(int i = 0; i < cdtm.getRowCount(); i++) {
			if(cdtm.getValueAt(i, 9).equals("Manual Entry") || cdtm.getValueAt(i, 9).equals("In Progress"))
				return cdtm.getValueAt(i, 8) + "";
		}
		return "";
	}
	
	/**
	 * Removes an element from the course table or grades table based on the assignment code or course
	 * identifier entered into the text field.
	 */
	public void removeElement() {
		
		if(!isIdentifierFound() && !isCodeFound()) {
			JOptionPane.showMessageDialog(null, "User Action Denied\nReason:\nElement Does Not Exist");
			return;
		}
		
		for(int i = 0; i < cdtm.getRowCount(); i++) {
			if(cdtm.getValueAt(i, 3).equals(identifierInput.getText()) && cdtm.getValueAt(i, 9).equals("Finalized")) {
				JOptionPane.showMessageDialog(null, "User Action Denied\nReason:\nCannot Remove Finalized Course");
				return;
			}
		}
		
		for(int i = 0; i < cdtm.getRowCount(); i++) {
			if(cdtm.getValueAt(i, 3).equals(identifierInput.getText())) {
				String courseName = (String) cdtm.getValueAt(i, 0);
				if(JOptionPane.showConfirmDialog(null, "Are you sure you want to delete " + courseName + "? \nThis operation cannot be undone.") == 0) {
					removeAssociatedGrades(identifierInput.getText());
					cdtm.removeRow(i);
				}
				else {
					JOptionPane.showMessageDialog(null, "Action Cancelled");
					return;
				}
			}
		}
		
		for(int i = 0; i < gdtm.getRowCount(); i++) {
			if(gdtm.getValueAt(i, 2).equals(identifierInput.getText())) {
				String assignmentCode = (String) gdtm.getValueAt(i, 2);
				if(JOptionPane.showConfirmDialog(null, "Are you sure you want to delete assignment " + assignmentCode + "? \nThis operation cannot be undone.") == 0) {
					String id = gdtm.getValueAt(i, 1) + "";
					gdtm.removeRow(i);
					calculateGrade(id);
				}
				else {
					JOptionPane.showMessageDialog(null, "Action Cancelled");
					return;
				}
			}
		}
	}
	
	/**
	 * Tells whether or not there are any unfinalized courses. If there are none, there will not be 
	 * any grades to finalize.
	 * @return whether or not there are any unfinalized courses
	 */
	public boolean existsUnfinalizedCourses() {
		
		for(int i = 0; i < cdtm.getRowCount(); i++)
			if(cdtm.getValueAt(i, 9).equals("Manual Entry") || cdtm.getValueAt(i, 9).equals("In Progress"))
				return true;
		return false;
	}
	
	/**
	 * Finalizes grades by setting the status of each course to finalized and calculating the GPA
	 * for the current term being finalized and the total GPA based on all terms.
	 */
	public void finalizeGrades() {
		
		String year = getUnfinalizedTerm();
		
		int creditSum = 0;
		double qualitySum = 0;
		int nonGpaSum = 0;
		
		for(int i = 0; i < cdtm.getRowCount(); i++) {
			if(cdtm.getValueAt(i, 8).equals(year + "") && cdtm.getValueAt(i, 6).equals("Letter") && !cdtm.getValueAt(i, 7).equals("F")) {
				qualitySum += Integer.parseInt((String)cdtm.getValueAt(i, 4)) * letToQual((String)cdtm.getValueAt(i, 7));
				creditSum += Integer.parseInt((String)cdtm.getValueAt(i, 4));
				cdtm.setValueAt("Finalized", i, 9);
			}
			
			else if(cdtm.getValueAt(i, 8).equals(year + "") && cdtm.getValueAt(i, 6).equals("Letter") && cdtm.getValueAt(i, 7).equals("F")) {
				qualitySum += Integer.parseInt((String)cdtm.getValueAt(i, 4)) * letToQual((String)cdtm.getValueAt(i, 7));
				cdtm.setValueAt("Finalized", i, 9);
			}
			
			else if(cdtm.getValueAt(i, 8).equals(year + "") && cdtm.getValueAt(i, 6).equals("P/NP") && cdtm.getValueAt(i, 7).equals("P")) {
				creditSum += Integer.parseInt((String)cdtm.getValueAt(i, 4));
				nonGpaSum += Integer.parseInt((String)cdtm.getValueAt(i, 4));
				nonGpaCreditTotal += Integer.parseInt((String)cdtm.getValueAt(i, 4));
				cdtm.setValueAt("Finalized", i, 9);
			}
			
			else if(cdtm.getValueAt(i, 8).equals(year + "") && cdtm.getValueAt(i, 6).equals("Notation") && cdtm.getValueAt(i, 7).equals("TR")) {
				creditSum += Integer.parseInt((String)cdtm.getValueAt(i, 4));
				nonGpaSum += Integer.parseInt((String)cdtm.getValueAt(i, 4));
				nonGpaCreditTotal += Integer.parseInt((String)cdtm.getValueAt(i, 4));
				cdtm.setValueAt("Finalized", i, 9);
			}
		}
		
		if(((String) cdtm.getValueAt(cdtm.getRowCount() - 1, 9)).equalsIgnoreCase("Finalized")) {
				cdtm.addRow(new Object[] {"", "", "", "", "", "", "", "", "", ""});		
				String gpa = qualitySum / (creditSum - nonGpaSum) + "";
				
				if(gpa.length() > 4)
					gpa = gpa.substring(0,4);
				
				String creditSumString = creditSum + "";
				if(creditSumString.length() > 5)
					creditSumString = creditSumString.substring(0,5);
				
				String qualitySumString = qualitySum + "";
				if(qualitySumString.length() > 5)
					qualitySumString = qualitySumString.substring(0,5);
				
				cdtm.addRow(new Object[] {"Term Credits", creditSumString, "Term Quality Points", qualitySum, "", "", "", "", "GPA", gpa});
				
				double allQualitySum = 0;
				int allCreditSum = 0;
				for(int i = 0; i < cdtm.getRowCount(); i++)
					if(((String) cdtm.getValueAt(i, 0)).equalsIgnoreCase("Term Credits")) {
						allQualitySum += Double.parseDouble(cdtm.getValueAt(i, 3) + "");
						allCreditSum += Integer.parseInt(cdtm.getValueAt(i, 1) + "");
					}
				
				String allCreditSum1 = allCreditSum + "";
				if(allCreditSum1.length() > 5)
					allCreditSum1 = allCreditSum1.substring(0,5);
				
				String allQualitySum1 = allQualitySum + "";
				if(allQualitySum1.length() > 5)
					allQualitySum1 = allQualitySum1.substring(0,5);
				
				String totalGpa = allQualitySum / (allCreditSum - nonGpaCreditTotal) + "";
				if(totalGpa.length() > 4)
					totalGpa = totalGpa.substring(0,4);
				
				cdtm.addRow(new Object[] {"Total Credits", allCreditSum1, "Total Quality Points", allQualitySum1, "", "", "", "", "GPA", totalGpa});	
				
				cdtm.addRow(new Object[] {"", "", "", "", "", "", "", "", "", ""});	
		}
		
		while(gdtm.getRowCount() > 0)
			gdtm.removeRow(0);
	}
	
	/**
	 * Tells whether or not an identifier can be found in the course table that matches the text field
	 * @return whether or not an identifier can be found
	 */
	public boolean isIdentifierFound() {
		
		if(identifierInput.getText().equals(""))
			return false;
		for(int i = 0; i < cdtm.getRowCount(); i++) {
			if(cdtm.getValueAt(i, 3).equals(identifierInput.getText()))
			    return true;
		}
		return false;
	}
	
	public boolean isCodeFound() {
		for(int i = 0; i < gdtm.getRowCount(); i++) {
			if(gdtm.getValueAt(i, 2).equals(identifierInput.getText()))
			    return true;
		}
		return false;
	}
	
	public void editCourse() {
		
		if(!isIdentifierFound()) {
			JOptionPane.showMessageDialog(null, "User Action Denied\nReason:\nIdentifier Does Not Exist");
			return;
		}
		
		for(int i = 0; i < cdtm.getRowCount(); i++) {
			if(cdtm.getValueAt(i, 3).equals(identifierInput.getText()) && cdtm.getValueAt(i, 9).equals("Finalized")) {
				JOptionPane.showMessageDialog(null, "User Action Denied\nReason:\nCannot Edit Finalized Course");
				return;
			}
		}
		
		int row = 0;
		for(int i = 0; i < cdtm.getRowCount(); i++) {
			if(cdtm.getValueAt(i, 3).equals(identifierInput.getText()))
				row = i;
		}
		
		String[] editChoices = {"Course Title", "Professor", "Day/Time", "Credits", "Category Weightings", "Grade Mode", "Final Grade", "Term"};
		String[] editChoices2 = {"Course Title", "Professor", "Day/Time", "Credits", "Category Weightings", "Final Grade", "Term"};
		String edit = "";
		
		for(int i = 0; i < cdtm.getRowCount(); i++)
			if(cdtm.getValueAt(i, 3).equals(identifierInput.getText()) && cdtm.getValueAt(i, 9).equals("Manual Entry")) {
				edit = (String) JOptionPane.showInputDialog(null, "Select Field for Edit", "Course Master", JOptionPane.QUESTION_MESSAGE, null, editChoices2, editChoices2[0]);
				if(edit == null) {
					JOptionPane.showMessageDialog(null, "Action Cancelled");
					return;
				}
			}
		
		if(edit.equals(""))
			edit = (String) JOptionPane.showInputDialog(null, "Select Field for Edit", "Course Master", JOptionPane.QUESTION_MESSAGE, null, editChoices, editChoices[0]);
		
		if(edit == null) {
			JOptionPane.showMessageDialog(null, "Action Cancelled");
			return;
		}
		
		if(edit.equalsIgnoreCase("Course Title")) {
			String course = JOptionPane.showInputDialog("Enter Course Title");
			if(course == null) {
				JOptionPane.showMessageDialog(null, "Action Cancelled");
				return;
			}
			else cdtm.setValueAt(course, row, 0);
		}
		
		if(edit.equalsIgnoreCase("Category Weightings")) {
			ArrayList<String> courseWeightings = categories.get(identifierInput.getText());
			
			String[] options = {"Add Category", "Remove Category", "Change Weighting"};
			String selection = (String) JOptionPane.showInputDialog(null, "Select Action to Perform", "Course Master", JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
			
			if(selection == null) {
				JOptionPane.showMessageDialog(null, "Action Cancelled");
				return;
			}
			
			if(selection.equals("Add Category")) {
				String categoryName = JOptionPane.showInputDialog("Enter Category Name");
				
				if(containsNumbers(categoryName)) {
					JOptionPane.showMessageDialog(null, "User Action Denied\nReason:\nCategory Name Cannot Contain Numbers");
					return;
				}
				
				
				if(courseWeightings.contains(categoryName)) {
					JOptionPane.showMessageDialog(null, "User Action Denied\nReason:\nCategory Name Already Exists");
					return;
				}
				String categoryWeight = JOptionPane.showInputDialog("Enter Category Weight");
				try {
					double testError = Double.parseDouble(categoryWeight);
				} catch (NumberFormatException e) {
					JOptionPane.showMessageDialog(null, "User Action Denied\nReason:\nNumber Format Exception");
					return;
				}
				courseWeightings.add(categoryName);
				courseWeightings.add(categoryWeight);
			}
			
			else if(selection.equals("Remove Category")) {
				ArrayList<String> categoryNames = getCategoryNames(courseWeightings);
				String[] nameChoices = new String[categoryNames.size()];
				for(int i = 0; i < nameChoices.length; i++)
					nameChoices[i] = categoryNames.get(i);
				
				String nameSelection = (String) JOptionPane.showInputDialog(null, "Select Category to Remove", "Course Master", 
						JOptionPane.QUESTION_MESSAGE, null, nameChoices, nameChoices[0]);
				
				if(nameSelection == null) {
					JOptionPane.showMessageDialog(null, "Action Cancelled");
					return;
				}
				
				if(JOptionPane.showConfirmDialog(null, "Are you sure you wish to delete the category [" + nameSelection + "]?\nThis action cannot be reversed.") != 0) {
					JOptionPane.showMessageDialog(null, "Action Cancelled");
					return;
				}
				
				int remIndex = courseWeightings.indexOf(nameSelection);
				courseWeightings.remove(remIndex);
				courseWeightings.remove(remIndex);
				
				for(int i = 0; i < gdtm.getRowCount(); i++) {
					if(gdtm.getValueAt(i, 3).equals(nameSelection)) {
						gdtm.removeRow(i);
						i--;
					}
				}
				
				calculateGrade(identifierInput.getText());
			}
			
			else {
				ArrayList<String> categoryNames = getCategoryNames(courseWeightings);
				String[] nameChoices = new String[categoryNames.size()];
				for(int i = 0; i < nameChoices.length; i++)
					nameChoices[i] = categoryNames.get(i);
				
				String nameSelection = (String) JOptionPane.showInputDialog(null, "Select Category to Change Weight", "Course Master", 
						JOptionPane.QUESTION_MESSAGE, null, nameChoices, nameChoices[0]);
				
				if(nameSelection == null) {
					JOptionPane.showMessageDialog(null, "Action Cancelled");
					return;
				}
				
				String newWeight = JOptionPane.showInputDialog("Enter New Weight");
				
				try {
					double testError = Double.parseDouble(newWeight);
				} catch (NumberFormatException e) {
					JOptionPane.showMessageDialog(null, "User Action Denied\nReason:\nNumber Format Exception");
					return;
				}
				
				if(newWeight == null) {
					JOptionPane.showMessageDialog(null, "Action Cancelled");
					return;
				}
				
				int changeIndex = courseWeightings.indexOf(nameSelection) + 1;
				courseWeightings.set(changeIndex, newWeight);
				
				for(int i = 0; i < gdtm.getRowCount(); i++)
					if(gdtm.getValueAt(i, 1).equals(identifierInput.getText()) && gdtm.getValueAt(i, 3).equals(nameSelection))
						gdtm.setValueAt(newWeight, i, 4);
				
				calculateGrade(identifierInput.getText());
			}
		}
		
		if(edit.equalsIgnoreCase("Professor")) {
			String prof = JOptionPane.showInputDialog("Enter Professor Name");
			if(prof == null) {
				JOptionPane.showMessageDialog(null, "Action Cancelled");
				return;
			}
			else cdtm.setValueAt(prof, row, 1);
		}
		
		if(edit.equalsIgnoreCase("Day/Time")) {
			String dayTime = JOptionPane.showInputDialog("Enter Day/Time");
			if(dayTime == null) {
				JOptionPane.showMessageDialog(null, "Action Cancelled");
				return;
			}
			else cdtm.setValueAt(dayTime, row, 2);
		}
		
		if(edit.equalsIgnoreCase("Credits")) {
			String[] creditsChoices = {"0", "1", "2", "3", "4", "5"};
			String credits = (String) JOptionPane.showInputDialog(null, "Select Number of Credits", "Course Edit Master", JOptionPane.QUESTION_MESSAGE, null, creditsChoices, creditsChoices[0]);
			if(credits == null) {
				JOptionPane.showMessageDialog(null, "Action Cancelled");
				return;
			}
			else cdtm.setValueAt(credits, row, 4);
		}
		
		if(edit.equalsIgnoreCase("Grade Mode")) {
			String[] gModeChoices = {"Letter", "P/NP", "Notation"};
			String gMode = (String) JOptionPane.showInputDialog(null, "Select Grade Mode", "Course Edit Master", JOptionPane.QUESTION_MESSAGE, null, gModeChoices, gModeChoices[0]);
			if(gMode == null) {
				JOptionPane.showMessageDialog(null, "Action Cancelled");
				return;
			}
			
			//else cdtm.setValueAt(gMode, row, 6);
			
			String[] fGradeChoicesC = {"In Progress", "A","A-","B+","B","B-","C+","C","C-","D+","D","D-","F"};
			String[] fGradeChoicesP = {"In Progress", "P", "NP"};
			String[] notationChoices = {"TR","I","W","Z"};
			String fGrade = "";
			
			if(gMode.equals("Letter")) {
				cdtm.setValueAt(gMode, row, 6);
				cdtm.setValueAt(numToLet(Double.parseDouble(cdtm.getValueAt(row, 5) + ""), gMode), row, 7);
				return;
			}
			else if(gMode.equals("P/NP")) {
				cdtm.setValueAt(gMode, row, 6);
				cdtm.setValueAt(numToLet(Double.parseDouble(cdtm.getValueAt(row, 5) + ""), gMode), row, 7);
				return;
			}
			else {
				if(JOptionPane.showConfirmDialog(null, "Are you sure you wish\nto change Grade Mode to Notation?\nNote: Grade Mode Notation cannot be changed back.") == 0) {
					fGrade = (String) JOptionPane.showInputDialog(null, "Select Notation", "Course Master", JOptionPane.QUESTION_MESSAGE, null, notationChoices, notationChoices[0]);
					if(fGrade == null) {
						JOptionPane.showMessageDialog(null, "Action Cancelled");
						return;
					}
					else {
						cdtm.setValueAt(gMode, row, 6);
						removeAssociatedGrades(identifierInput.getText());
						cdtm.setValueAt("Manual Entry", row, 9);
						cdtm.setValueAt("n/a", row, 5);
						cdtm.setValueAt(fGrade, row, 7);
					}
				}
				else {
					JOptionPane.showMessageDialog(null, "Action Cancelled");
					return;
				}
			}
		}
		
		if(edit.equalsIgnoreCase("Final Grade")) {
			String[] fGradeChoicesC = {"In Progress", "A","A-","B+","B","B-","C+","C","C-","D+","D","D-","F"};
			String[] fGradeChoicesP = {"In Progress", "P", "NP"};
			String[] notationChoices = {"TR","I","W","Z"};
			String fGrade;
			
			if(cdtm.getValueAt(row, 6).equals("Letter"))
				fGrade = (String) JOptionPane.showInputDialog(null, "Select Final Grade", "Course Edit Master", JOptionPane.QUESTION_MESSAGE, null, fGradeChoicesC, fGradeChoicesC[0]);
			else if(cdtm.getValueAt(row, 6).equals("P/NP"))
				fGrade = (String) JOptionPane.showInputDialog(null, "Select Final Grade", "Course Edit Master", JOptionPane.QUESTION_MESSAGE, null, fGradeChoicesP, fGradeChoicesP[0]);
			else 
				fGrade = (String) JOptionPane.showInputDialog(null, "Select Final Grade", "Course Master", JOptionPane.QUESTION_MESSAGE, null, notationChoices, notationChoices[0]);
			if(fGrade == null) {
				JOptionPane.showMessageDialog(null, "Action Cancelled");
				return;
			}
			else cdtm.setValueAt(fGrade, row, 7);
			
		}
		
		if(edit.equalsIgnoreCase("Term")) {
			String[] yearChoices = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
			String term = JOptionPane.showInputDialog(null, "Enter Term Number");
			try {
				double testError = Double.parseDouble(term);
			} catch (NumberFormatException e) {
				JOptionPane.showMessageDialog(null, "User Action Denied\nReason:\nNumber Format Exception");
				return;
			}
			if(term == null) {
				JOptionPane.showMessageDialog(null, "Action Cancelled");
				return;
			}
			else cdtm.setValueAt(term, row, 8);
		}
	}
	
	public ArrayList<String> getCategoryNames(ArrayList<String> categoryWeights) {
		ArrayList<String> names = new ArrayList<String>();
		for(int i = 0; i < categoryWeights.size(); i++)
			if(i % 2 == 0)
				names.add(categoryWeights.get(i));
		return names;
	}
	
	public boolean containsNumbers(String word) {
		String[] nums = {"0","1","2","3","4","5","6","7","8","9","10"};
		for(int i = 0; i < nums.length; i++)
			if(word.contains(nums[i]))
				return true;
		return false;
	}
	
	public ArrayList<String> getCategoryValues(ArrayList<String> categoryWeights) {
		ArrayList<String> values = new ArrayList<String>();
		for(int i = 0; i < categoryWeights.size(); i++)
			if(i % 2 != 0)
				values.add(categoryWeights.get(i));
		return values;
	}
	
	public static double letToQual(String letterGrade) {
		if(letterGrade.equalsIgnoreCase("A")) 
			return 4;
		if(letterGrade.equalsIgnoreCase("A-")) 
			return 3.7;
		if(letterGrade.equalsIgnoreCase("B+")) 
			return 3.3;
		if(letterGrade.equalsIgnoreCase("B")) 
			return 3;
		if(letterGrade.equalsIgnoreCase("B-")) 
			return 2.7;
		if(letterGrade.equalsIgnoreCase("C+")) 
			return 2.3;
		if(letterGrade.equalsIgnoreCase("C")) 
			return 2;
		if(letterGrade.equalsIgnoreCase("C-")) 
			return 1.7;
		if(letterGrade.equalsIgnoreCase("D+")) 
			return 1.3;
		if(letterGrade.equalsIgnoreCase("D")) 
			return 1;
		if(letterGrade.equalsIgnoreCase("D-")) 
			return 0.7;
		return 0;
	}
	
	public void enterGrade() {
		ArrayList<String> identifiers = new ArrayList<String>();
		for(int i = 0; i < cdtm.getRowCount(); i++)
			if(cdtm.getValueAt(i, 9).equals("In Progress"))
				identifiers.add(cdtm.getValueAt(i, 3) + "");
		
		if(identifiers.size() == 0) {
			JOptionPane.showMessageDialog(null, "User Action Denied\nReason:\nNo Courses Available");
			return;
		}
		
		String[] identifierChoices = new String[identifiers.size()];
		for(int i = 0; i < identifiers.size(); i++)
			identifierChoices[i] = identifiers.get(i);

		String identifier = (String) JOptionPane.showInputDialog(null, "Select Identifier", "Grade Master", 
				JOptionPane.QUESTION_MESSAGE, null, identifierChoices, identifierChoices[0]);
		
		if(identifier == null) {
			JOptionPane.showMessageDialog(null, "Action Cancelled");
			return;
		}
		
		String courseTitle = "";
		for(int i = 0; i < cdtm.getRowCount(); i++)
			if(identifier.equals(cdtm.getValueAt(i, 3)))
				courseTitle = cdtm.getValueAt(i, 0) + "";
		
		assignmentCode += (int) (Math.random() * 50 + 1);
		String code = "A" + assignmentCode;
		
		String[] categoryChoices = new String[categories.get(identifier).size() / 2];
		for(int i = 0; i < categoryChoices.length; i++)
			categoryChoices[i] = categories.get(identifier).get(i * 2);
				
		String category = (String) JOptionPane.showInputDialog(null, "Select Category", "Grade Master", 
				JOptionPane.QUESTION_MESSAGE, null, categoryChoices, categoryChoices[0]);
		
		if(category == null) {
			JOptionPane.showMessageDialog(null, "Action Cancelled");
			return;
		}
		
		int catIndex = 0;
		for(int i = 0; i < categories.get(identifier).size(); i++)
			if(categories.get(identifier).get(i).equals(category))
				catIndex = i;
		
		String catWeight = categories.get(identifier).get(catIndex + 1);
		
		String pointsEarned = JOptionPane.showInputDialog("Enter Points Earned for Assignment");
		
		String totalPoints = JOptionPane.showInputDialog("Enter Total Points for Assignment");
		
		String grade;
		
		try {
			grade = (Double.parseDouble(pointsEarned) / Double.parseDouble(totalPoints) * 100 + "");
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(null, "User Action Denied\nReason:\nNumber Format Exception");
			return;
		}
		
		String comment = JOptionPane.showInputDialog(null, "Enter a comment for this assignment.");
		
		if(grade.length() > 5)
			grade = grade.substring(0, 5);
		
		gdtm.addRow(new Object[] {courseTitle, identifier, code, category, catWeight, pointsEarned, totalPoints, grade, comment});
		
		calculateGrade(identifier);
		
	}
	
	// removes grades associated with a deleted course
	public void removeAssociatedGrades(String identifier) {
		for(int i = 0; i < gdtm.getRowCount(); i++)
			if(gdtm.getValueAt(i, 1).equals(identifier)) {
				gdtm.removeRow(i);
				i--;
			}
	}

	public static void main(String[] a) {
		new Gradebook();
	}
	
	public void revertTableSettings() {
		courseList.getColumnModel().getColumn(0).setPreferredWidth(200);
        courseList.getColumnModel().getColumn(1).setPreferredWidth(200);
        courseList.getColumnModel().getColumn(2).setPreferredWidth(150);
        courseList.getColumnModel().getColumn(3).setPreferredWidth(100);
        courseList.getColumnModel().getColumn(4).setPreferredWidth(50);
        courseList.getColumnModel().getColumn(5).setPreferredWidth(100);
        courseList.getColumnModel().getColumn(6).setPreferredWidth(75);
        courseList.getColumnModel().getColumn(7).setPreferredWidth(75);
        courseList.getColumnModel().getColumn(8).setPreferredWidth(30);
        courseList.getColumnModel().getColumn(9).setPreferredWidth(125);
        courseList.setShowVerticalLines(true);
        courseList.setColumnSelectionAllowed(false);
        courseList.getTableHeader().setReorderingAllowed(false);
        courseList.setEnabled(false);
        
        gradeList.setShowVerticalLines(true);
        gradeList.setColumnSelectionAllowed(false);
        gradeList.getTableHeader().setReorderingAllowed(false);
        gradeList.setEnabled(false);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String s = e.getActionCommand();
		
		if(s.equalsIgnoreCase("Add Course")) {
			addCourse();
		}
		
		if(s.equalsIgnoreCase("Remove Element")) {
			removeElement();
		}
		
		if(s.equalsIgnoreCase("Finalize Grades")) {
			
			for(int i = 0; i < cdtm.getRowCount(); i++) {
				if(cdtm.getValueAt(i, 7).equals("In Progress")) {
					JOptionPane.showMessageDialog(null, "User Action Denied\nReason:\nCannot Finalize with\nFinal Grade = In Progress");
					return;
				}
			}
			
			if(!existsUnfinalizedCourses() || cdtm.getRowCount() == 0) {
				JOptionPane.showMessageDialog(null, "User Action Denied\nReason:\nAll Courses Finalized");
				return;
			}
			
			else if(JOptionPane.showConfirmDialog(null, "Are you sure you want to finalize grades?\nThis action cannot be reversed.\n"
					+ "Note: It is advised to export before finalizing.\nGrades will be cleared.") == 0) {
				int counter = 0;
				for(int i = 0; i < cdtm.getRowCount(); i++)
					if(cdtm.getValueAt(i, 9).equals("In Progress") || cdtm.getValueAt(i, 9).equals("Manual Entry"))
						counter++;
				if(counter > 0)
					finalizeGrades();
				else {
					JOptionPane.showMessageDialog(null, "User Action Denied\nReason:\nAll Courses Finalized");
					return;
				}
			}
			else {
				JOptionPane.showMessageDialog(null, "Action Cancelled");
				return;
			}
		}
		
		if(s.equalsIgnoreCase("Manual Override")) {
			if(!courseList.isEnabled()) {
				if(JOptionPane.showConfirmDialog(null, "Are you sure you want to enter Manual Override mode? \n"
						+ "It is highly recommended to use the Edit Course function.\nNote: Reclick Manual Override to return to Automatic.") == 0) {
					courseList.setEnabled(true);
					gradeList.setEnabled(true);
				}
				else {
					JOptionPane.showMessageDialog(null, "Action Cancelled");
					return;
				}
			}
			else {
				JOptionPane.showMessageDialog(null, "Returned to Automatic Mode");
				courseList.setEnabled(false);
				for(int i = 0; i < cdtm.getRowCount(); i++) {
					courseList.getSelectionModel().clearSelection();
					gradeList.getSelectionModel().clearSelection();
				}
			}
		}
		
		if(s.equalsIgnoreCase("Edit Course")) {
			editCourse();
		}
		
		if(s.equalsIgnoreCase("Enter Grade")) {
			enterGrade();
		}
		
		if(s.equalsIgnoreCase("Import/Export")) {
			if(cdtm.getRowCount() == 0 && gdtm.getRowCount() == 0) {
				loadTable();
			}
			else
				saveTable();
		}
	}

}