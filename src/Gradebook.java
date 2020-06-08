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
import java.util.Map;
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
	
	private double nonGpaCreditTotal; // The total amount of credits not applied to the gpa
	
	private JFrame frame; // The frame that holds the tables and buttons
	
	private Hashtable<String, ArrayList<String>> categories; // Contains the category weights of each course
	
	private JButton viewBreakdown;
	
	private double totalFCreditSum;
	
	private boolean isAPluses = false;
	private boolean isHonorsAPClasses = false;
	private double honorsBonus = 1;
	private double apBonus = 1;
	private Hashtable<String, String> honorsAPStatuses;
	private String rounding = "Tenths";
	
	private ArrayList<Hashtable> gradeScales = new ArrayList<Hashtable>();
	
	private Hashtable<String, Hashtable> courseScales = new Hashtable<String, Hashtable>();
	
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
        
        String courseHeader[] = new String[] { "Subject/Course Number", "Course Title", "Comment", "Identifier", "Credits", "Numeric Grade", "Grade Mode", "Final Grade", "Term", "Status" };
        categories = new Hashtable<String, ArrayList<String>>();
        
        cdtm.setColumnIdentifiers(courseHeader);
        courseList.setModel(cdtm);
        courseCode = 11111;
        honorsAPStatuses = new Hashtable<String, String>();
        
        courseList.setShowVerticalLines(true);
        courseList.setColumnSelectionAllowed(false);
        courseList.getTableHeader().setReorderingAllowed(false);
        courseList.getTableHeader().setResizingAllowed(false);
        courseList.setEnabled(false);
  
        // adding it to JScrollPane 
        JScrollPane spc = new JScrollPane(courseList); 
        frame.add(spc, BorderLayout.NORTH); 
        courseList.getColumnModel().getColumn(0).setPreferredWidth(125);
        courseList.getColumnModel().getColumn(1).setPreferredWidth(200);
        courseList.getColumnModel().getColumn(2).setPreferredWidth(200);
        courseList.getColumnModel().getColumn(3).setPreferredWidth(100);
        courseList.getColumnModel().getColumn(4).setPreferredWidth(50);
        courseList.getColumnModel().getColumn(5).setPreferredWidth(100);
        courseList.getColumnModel().getColumn(6).setPreferredWidth(75);
        courseList.getColumnModel().getColumn(7).setPreferredWidth(50);
        courseList.getColumnModel().getColumn(8).setPreferredWidth(75);
        courseList.getColumnModel().getColumn(9).setPreferredWidth(125);
        
        // ------------------------Creating the panel for the buttons------------------------
        JPanel buttons = new JPanel();
        buttons.setVisible(true);
        
        // Initialize buttons and add action listeners
        JButton addCourse = new JButton("Add Course");
        addCourse.addActionListener(this);
        
        Hashtable defaultScale = new Hashtable();
        defaultScale.put("Name", "Default Scale");
        if(isAPluses)
        	defaultScale.put("A+", 98.0);
        else
        	defaultScale.put("A+", Double.MAX_VALUE);
        defaultScale.put("A", 94.0);
        defaultScale.put("A-", 90.0);
        defaultScale.put("B+", 87.0);
        defaultScale.put("B", 84.0);
        defaultScale.put("B-", 80.0);
        defaultScale.put("C+", 77.0);
        defaultScale.put("C", 74.0);
        defaultScale.put("C-", 70.0);
        defaultScale.put("D+", 67.0);
        defaultScale.put("D", 64.0);
        defaultScale.put("D-", 60.0);
        defaultScale.put("P", 60.0);
        gradeScales.add(defaultScale);
        
        JButton removeCourse = new JButton("Remove Element");
        removeCourse.addActionListener(this);
        
        JButton editElement = new JButton("Edit Element");
        editElement.addActionListener(this);
        
        JButton enterGrade = new JButton("Enter Grade");
        enterGrade.addActionListener(this);
        
        viewBreakdown = new JButton("View Breakdown");
        viewBreakdown.addActionListener(this);
        
        JButton finalizeGrades = new JButton("Finalize Grades");
        finalizeGrades.addActionListener(this);
        
        JButton settings = new JButton("Settings");
        settings.addActionListener(this);
        
        JButton help = new JButton("Help");
        help.addActionListener(this);
        
        JButton importExport = new JButton("Import/Export");
        importExport.addActionListener(this);
        
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
        buttons.add(editElement);
        buttons.add(enterGrade);
        buttons.add(viewBreakdown);
        buttons.add(finalizeGrades);
        //buttons.add(manualOverride);
        buttons.add(importExport);
        buttons.add(settings);
        buttons.add(help);
        buttons.add(identifier);
        buttons.add(identifierInput);
        
        // Add the panel to the frame
        frame.add(buttons);
        
        // ------------------------Creating the table for the list of grades/assignments------------------------
        gradeList = new JTable();
        gdtm = new DefaultTableModel(0,0);
        
        assignmentCode = 11111;
        nonGpaCreditTotal = 0;
        totalFCreditSum = 0;
        
        String gradeHeader[] = new String[] { "Course Title", "Identifier", "Assignment Code", "Category", "Category Weight", "Points Earned", "Total Points", "Grade", "Comment" };
        
        gdtm.setColumnIdentifiers(gradeHeader);
        gradeList.setModel(gdtm);
        
        gradeList.setShowVerticalLines(true);
        gradeList.setAutoCreateRowSorter(true);
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
			out.writeObject(courseCode);
			out.writeObject(assignmentCode);
			out.writeObject(nonGpaCreditTotal);
			out.writeObject(totalFCreditSum);
			out.writeObject(isAPluses);
			out.writeObject(isHonorsAPClasses);
			out.writeObject(honorsBonus);
			out.writeObject(apBonus);
			out.writeObject(honorsAPStatuses);
			out.writeObject(rounding);
			out.writeObject(gradeScales);
			out.writeObject(courseScales);
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
			courseCode = (int) in.readObject();
			assignmentCode = (int) in.readObject();
			nonGpaCreditTotal = (double) in.readObject();
			totalFCreditSum = (double) in.readObject();
			isAPluses = (boolean) in.readObject();
			isHonorsAPClasses = (boolean) in.readObject();
			honorsBonus = (double) in.readObject();
			apBonus = (double) in.readObject();
			honorsAPStatuses = (Hashtable<String, String>) in.readObject();
			rounding = (String) in.readObject();
			cdtm.setDataVector(rowDataC, columnNamesC);
			gdtm.setDataVector(rowDataG, columnNamesG);
			revertTableSettings();
			in.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void linkScale(String identifier, String scaleName) {
		for(int i = 0; i < gradeScales.size(); i++)
			if(gradeScales.get(i).get("Name").equals(scaleName)) {
				System.out.println("linked");
				courseScales.put(identifier, gradeScales.get(i));
				System.out.println("In link: " + gradeScales.get(i).get("Name"));
				System.out.println("In link: " + courseScales.get(identifier).get("A"));
				return;
			}
		
		JOptionPane.showMessageDialog(null, "Course Link Error\nGrade Scale Not Found", "System Notification", JOptionPane.ERROR_MESSAGE);
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
		
		String finalGradeString = finalGrade + "";
		if(finalGradeString.length() > 5)
			finalGradeString = finalGradeString.substring(0, 6);

		String gMode = "";
		String id = "";
		for(int i = 0; i < cdtm.getRowCount(); i++)
			if(cdtm.getValueAt(i, 3).equals(identifier)) {
				gMode = cdtm.getValueAt(i, 6) + "";
				id = cdtm.getValueAt(i, 3) + "";
			}
		
		String letGrade = numToLet(id, finalGrade, gMode);
		
		for(int i = 0; i < cdtm.getRowCount(); i++)
			if(cdtm.getValueAt(i, 3).equals(identifier)) {
				cdtm.setValueAt(finalGradeString, i, 5);
				cdtm.setValueAt(letGrade, i, 7);
			}
	}
	
	/**
	 * Helper method to convert a numeric grade to a letter grade
	 * @param grade the grade to be converted
	 * @param gMode the grade mode
	 * @return the letter equivalent of the grade
	 */
	public String numToLet(String identifier, double grade, String gMode) {
		
		System.out.println("Tester: " + courseScales.get(identifier));
		
		if(gMode.equals("Letter")) {
			System.out.println("letterrrrrrr");
			System.out.println("IN method " + (double)courseScales.get(identifier).get("A"));
			if(isAPluses && grade >= (double)courseScales.get(identifier).get("A+")) return "A+";
			else if(grade >= (double)courseScales.get(identifier).get("A")) return "A";
			else if(grade >= (double)courseScales.get(identifier).get("A-")) return "A-";
			else if(grade >= (double)courseScales.get(identifier).get("B+")) return "B+";
			else if(grade >= (double)courseScales.get(identifier).get("B")) return "B";
			else if(grade >= (double)courseScales.get(identifier).get("B-")) return "B-";
			else if(grade >= (double)courseScales.get(identifier).get("C+")) return "C+";
			else if(grade >= (double)courseScales.get(identifier).get("C")) return "C";
			else if(grade >= (double)courseScales.get(identifier).get("C-")) return "C-";
			else if(grade >= (double)courseScales.get(identifier).get("D+")) return "D+";
			else if(grade >= (double)courseScales.get(identifier).get("D")) return "D";
			else if(grade >= (double)courseScales.get(identifier).get("D-")) return "D-";
			else return "F";
		}
		else if(gMode.equals("P/NP")) {
			if(grade >= (double)courseScales.get(identifier).get("P")) return "P";
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
		
		String credits, gMode, subject, title, comment, term;
		credits = gMode = subject = title = comment = term = "";
		JTextField subjectEntry = new JTextField();
		JTextField titleEntry = new JTextField();
		JTextField commentEntry = new JTextField();
		JTextField termEntry = new JTextField();
		
		String[] creditsChoices = {"0", "0.5", "1", "1.5", "2", "2.5", "3", "4", "5", "6"};
		JComboBox creditEntry = new JComboBox(creditsChoices);
		creditEntry.setSelectedIndex(6);
		
		String[] gModeChoices = {"Letter", "P/NP", "Notation"};
		JComboBox gModeEntry = new JComboBox(gModeChoices);
		
		String[] scaleChoices = new String[gradeScales.size()];
		for(int i = 0; i < scaleChoices.length; i++) {
			scaleChoices[i] = (String) gradeScales.get(i).get("Name");
		}
		JComboBox scaleEntry = new JComboBox(scaleChoices);
		
		JPanel p = new JPanel();
		p.setLayout(new GridLayout(7, 0));
		
		p.add(new JLabel("Enter Subject/Course Number"));
		p.add(subjectEntry);
		
		p.add(new JLabel("Enter Course Title"));
		p.add(titleEntry);
		
		p.add(new JLabel("Enter Comment"));
		p.add(commentEntry);
		
		p.add(new JLabel("Select Number of Credits"));
		p.add(creditEntry);
		
		p.add(new JLabel("Select Grade Mode"));
		p.add(gModeEntry);
		
		p.add(new JLabel("Select Grade Scale"));
		p.add(scaleEntry);
		
		p.add(new JLabel("Enter Term (Ex. Fall 2017)"));
		termEntry.setText(getUnfinalizedTerm());
		p.add(termEntry);
		
		courseCode += (int) (Math.random() * 50 + 1);
		String identifier = "C" + courseCode;
		
		int result = JOptionPane.showConfirmDialog(null, p, "Course Master", JOptionPane.OK_CANCEL_OPTION);
		
		if(result == JOptionPane.OK_OPTION) {
			credits = (String) creditEntry.getSelectedItem();
			gMode = (String) gModeEntry.getSelectedItem();
			subject = subjectEntry.getText();
			title = titleEntry.getText();
			comment = commentEntry.getText();
			term = termEntry.getText();
			linkScale(identifier, (String) scaleEntry.getSelectedItem());
			
			if(subject.isEmpty() || title.isEmpty() || term.isEmpty()) {
				JOptionPane.showMessageDialog(null, "User Action Denied\nReason:\nMissing Information", "System Notification", JOptionPane.INFORMATION_MESSAGE);
				return;
			}
		}
		
		else {
			JOptionPane.showMessageDialog(null, "Action Cancelled", "System Notification", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		
		if(isTermFinalized(term)) {
			JOptionPane.showMessageDialog(null, "User Action Denied\nReason:\nEntered Term is Finalized", "System Notification", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		if(!getUnfinalizedTerm().equals(term) && existsUnfinalizedTerm()) {
			JOptionPane.showMessageDialog(null, "User Action Denied\nReason:\nMust Finalize Previous Term", "System Notification", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		honorsAPStatuses.put(identifier, "College Prep");
		
		String numGrade = "n/a";
		
		String[] fGradeChoicesC = {"In Progress", "A","A-","B+","B","B-","C+","C","C-","D+","D","D-","F"};
		String[] fGradeChoicesCPlus = {"In Progress", "A+", "A","A-","B+","B","B-","C+","C","C-","D+","D","D-","F"};
		String[] fGradeChoicesP = {"In Progress", "P", "NP"};
		String[] notationChoices = {"TR","I","W","Z"};
		String fGrade;
		if(gMode.equalsIgnoreCase("Letter")) {
			if(isAPluses)
				fGrade = (String) JOptionPane.showInputDialog(null, "Select Final Grade", "Course Master", 
						JOptionPane.QUESTION_MESSAGE, null, fGradeChoicesCPlus, fGradeChoicesCPlus[0]);
			else
				fGrade = (String) JOptionPane.showInputDialog(null, "Select Final Grade", "Course Master", 
						JOptionPane.QUESTION_MESSAGE, null, fGradeChoicesC, fGradeChoicesC[0]);
		}
		else if(gMode.equalsIgnoreCase("P/NP"))
			fGrade = (String) JOptionPane.showInputDialog(null, "Select Final Grade", "Course Master", JOptionPane.QUESTION_MESSAGE, null, fGradeChoicesP, fGradeChoicesP[0]);
		else
			fGrade = (String) JOptionPane.showInputDialog(null, "Select Final Grade", "Course Master", JOptionPane.QUESTION_MESSAGE, null, notationChoices, notationChoices[0]);
		if(fGrade == null) {
			JOptionPane.showMessageDialog(null, "Action Cancelled", "System Notification", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		
		if(!gMode.equals("Notation") && fGrade.equals("In Progress")) {
			String category = "";
			String catWeight = "";
			ArrayList<String> catsAndWeights = new ArrayList<String>();
			category = JOptionPane.showInputDialog(null, "Enter Grade Weight Category Name", "Course Master", JOptionPane.INFORMATION_MESSAGE);
			
			if(category == null) {
				JOptionPane.showMessageDialog(null, "Action Cancelled", "System Notification", JOptionPane.INFORMATION_MESSAGE);
				return;
			}
			
			if(containsNumbers(category)) {
				JOptionPane.showMessageDialog(null, "User Action Denied\nReason:\nCategory Name Cannot Contain Numbers", "System Notification", JOptionPane.ERROR_MESSAGE);
				return;
			}

			catWeight = JOptionPane.showInputDialog(null, "Enter Category Weight (Ex. 15)", "Course Master", JOptionPane.INFORMATION_MESSAGE);
			if(catWeight == null) {
				JOptionPane.showMessageDialog(null, "Action Cancelled", "System Notification", JOptionPane.INFORMATION_MESSAGE);
				return;
			}
			try {
				double testError = Double.parseDouble(catWeight);
			} catch (NumberFormatException e) {
				JOptionPane.showMessageDialog(null, "User Action Denied\nReason:\nNumber Format Exception", "System Notification", JOptionPane.ERROR_MESSAGE);
				return;
			}
			catsAndWeights.add(category);
			catsAndWeights.add(catWeight);
			int yesNo = 0;
			while(yesNo == 0) {
				yesNo = JOptionPane.showConfirmDialog(null, "Enter Another Category?");
				if(yesNo == 0) {
					category = JOptionPane.showInputDialog(null, "Enter Category Name", "Course Master", JOptionPane.INFORMATION_MESSAGE);
					
					if(containsNumbers(category)) {
						JOptionPane.showMessageDialog(null, "User Action Denied\nReason:\nCategory Name Cannot Contain Numbers", "System Notification", JOptionPane.ERROR_MESSAGE);
						return;
					}
					
					catWeight = JOptionPane.showInputDialog(null, "Enter Category Weight (ex. 15)", "Course Master", JOptionPane.INFORMATION_MESSAGE);
					try {
						double testError = Double.parseDouble(catWeight);
					} catch (NumberFormatException e) {
						JOptionPane.showMessageDialog(null, "User Action Denied\nReason:\nNumber Format Exception", "System Notification", JOptionPane.ERROR_MESSAGE);
						return;
					}
					catsAndWeights.add(category);
					catsAndWeights.add(catWeight);
				}
				else if(yesNo == JOptionPane.CANCEL_OPTION){
					JOptionPane.showMessageDialog(null, "Action Cancelled", "System Notification", JOptionPane.INFORMATION_MESSAGE);
					return;
				}
			}
			categories.put(identifier, catsAndWeights);
		}
		
		String status;
		if(fGrade.equalsIgnoreCase("In Progress")) 
			status = "In Progress";
		else status = "Manual Entry";

		cdtm.addRow(new Object[] {subject, title, comment, identifier, credits, numGrade, gMode, fGrade, term, status});
		
		JOptionPane.showMessageDialog(null, "Successfully Updated", "System Notification", JOptionPane.INFORMATION_MESSAGE);
	}
	
	public void addCourseHonorsAP() {
		
		String credits, gMode, subject, title, comment, term;
		credits = gMode = subject = title = comment = term = "";
		JTextField subjectEntry = new JTextField();
		JTextField titleEntry = new JTextField();
		JTextField commentEntry = new JTextField();
		JTextField termEntry = new JTextField();
		
		courseCode += (int) (Math.random() * 50 + 1);
		String identifier = "C" + courseCode;
		
		String[] creditsChoices = {"0", "0.5", "1", "1.5", "2", "2.5", "3", "4", "5", "6"};
		JComboBox creditEntry = new JComboBox(creditsChoices);
		creditEntry.setSelectedIndex(6);
		
		String[] honorsAPChoices = {"College Prep", "Honors", "Advanced Placement"};
		JComboBox courseTypeEntry = new JComboBox(honorsAPChoices);
		
		String[] gModeChoices = {"Letter", "P/NP", "Notation"};
		JComboBox gModeEntry = new JComboBox(gModeChoices);
		
		JPanel p = new JPanel();
		p.setLayout(new GridLayout(7, 0));
		
		p.add(new JLabel("Enter Subject/Course Number"));
		p.add(subjectEntry);
		
		p.add(new JLabel("Enter Course Title"));
		p.add(titleEntry);
		
		p.add(new JLabel("Enter Course Type"));
		p.add(courseTypeEntry);
		
		
		p.add(new JLabel("Enter Comment"));
		p.add(commentEntry);
		
		p.add(new JLabel("Select Number of Credits"));
		p.add(creditEntry);
		
		p.add(new JLabel("Select Grade Mode"));
		p.add(gModeEntry);
		
		p.add(new JLabel("Enter Term (Ex. Fall 2017)"));
		termEntry.setText(getUnfinalizedTerm());
		p.add(termEntry);
		
		int result = JOptionPane.showConfirmDialog(null, p, "Course Master", JOptionPane.OK_CANCEL_OPTION);
		
		if(result == JOptionPane.OK_OPTION) {
			credits = (String) creditEntry.getSelectedItem();
			gMode = (String) gModeEntry.getSelectedItem();
			subject = subjectEntry.getText();
			title = titleEntry.getText();
			comment = commentEntry.getText();
			term = termEntry.getText();
			honorsAPStatuses.put(identifier, courseTypeEntry.getSelectedItem() + "");
			
			if(subject.isEmpty() || title.isEmpty() || term.isEmpty()) {
				JOptionPane.showMessageDialog(null, "User Action Denied\nReason:\nMissing Information", "System Notification", JOptionPane.INFORMATION_MESSAGE);
				return;
			}
		}
		
		else {
			JOptionPane.showMessageDialog(null, "Action Cancelled", "System Notification", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		
		if(isTermFinalized(term)) {
			JOptionPane.showMessageDialog(null, "User Action Denied\nReason:\nEntered Term is Finalized", "System Notification", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		if(!getUnfinalizedTerm().equals(term) && existsUnfinalizedTerm()) {
			JOptionPane.showMessageDialog(null, "User Action Denied\nReason:\nMust Finalize Previous Term", "System Notification", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		String numGrade = "n/a";
		
		String[] fGradeChoicesC = {"In Progress", "A","A-","B+","B","B-","C+","C","C-","D+","D","D-","F"};
		String[] fGradeChoicesCPlus = {"In Progress", "A+", "A","A-","B+","B","B-","C+","C","C-","D+","D","D-","F"};
		String[] fGradeChoicesP = {"In Progress", "P", "NP"};
		String[] notationChoices = {"TR","I","W","Z"};
		String fGrade;
		if(gMode.equalsIgnoreCase("Letter")) {
			if(isAPluses)
				fGrade = (String) JOptionPane.showInputDialog(null, "Select Final Grade", "Course Master", 
						JOptionPane.QUESTION_MESSAGE, null, fGradeChoicesCPlus, fGradeChoicesCPlus[0]);
			else
				fGrade = (String) JOptionPane.showInputDialog(null, "Select Final Grade", "Course Master", 
						JOptionPane.QUESTION_MESSAGE, null, fGradeChoicesC, fGradeChoicesC[0]);
		}
		else if(gMode.equalsIgnoreCase("P/NP"))
			fGrade = (String) JOptionPane.showInputDialog(null, "Select Final Grade", "Course Master", JOptionPane.QUESTION_MESSAGE, null, fGradeChoicesP, fGradeChoicesP[0]);
		else
			fGrade = (String) JOptionPane.showInputDialog(null, "Select Final Grade", "Course Master", JOptionPane.QUESTION_MESSAGE, null, notationChoices, notationChoices[0]);
		if(fGrade == null) {
			JOptionPane.showMessageDialog(null, "Action Cancelled", "System Notification", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		
		if(!gMode.equals("Notation") && fGrade.equals("In Progress")) {
			String category = "";
			String catWeight = "";
			ArrayList<String> catsAndWeights = new ArrayList<String>();
			category = JOptionPane.showInputDialog(null, "Enter Grade Weight Category Name", "Course Master", JOptionPane.INFORMATION_MESSAGE);
			
			if(category == null) {
				JOptionPane.showMessageDialog(null, "Action Cancelled", "System Notification", JOptionPane.INFORMATION_MESSAGE);
				return;
			}
			
			if(containsNumbers(category)) {
				JOptionPane.showMessageDialog(null, "User Action Denied\nReason:\nCategory Name Cannot Contain Numbers", "System Notification", JOptionPane.ERROR_MESSAGE);
				return;
			}

			catWeight = JOptionPane.showInputDialog(null, "Enter Category Weight (Ex. 15)", "Course Master", JOptionPane.INFORMATION_MESSAGE);
			if(catWeight == null) {
				JOptionPane.showMessageDialog(null, "Action Cancelled", "System Notification", JOptionPane.INFORMATION_MESSAGE);
				return;
			}
			try {
				double testError = Double.parseDouble(catWeight);
			} catch (NumberFormatException e) {
				JOptionPane.showMessageDialog(null, "User Action Denied\nReason:\nNumber Format Exception", "System Notification", JOptionPane.ERROR_MESSAGE);
				return;
			}
			catsAndWeights.add(category);
			catsAndWeights.add(catWeight);
			int yesNo = 0;
			while(yesNo == 0) {
				yesNo = JOptionPane.showConfirmDialog(null, "Enter Another Category?");
				if(yesNo == 0) {
					category = JOptionPane.showInputDialog(null, "Enter Category Name", "Course Master", JOptionPane.INFORMATION_MESSAGE);
					
					if(containsNumbers(category)) {
						JOptionPane.showMessageDialog(null, "User Action Denied\nReason:\nCategory Name Cannot Contain Numbers", "System Notification", JOptionPane.ERROR_MESSAGE);
						return;
					}
					
					catWeight = JOptionPane.showInputDialog(null, "Enter Category Weight (ex. 15)", "Course Master", JOptionPane.INFORMATION_MESSAGE);
					try {
						double testError = Double.parseDouble(catWeight);
					} catch (NumberFormatException e) {
						JOptionPane.showMessageDialog(null, "User Action Denied\nReason:\nNumber Format Exception", "System Notification", JOptionPane.ERROR_MESSAGE);
						return;
					}
					catsAndWeights.add(category);
					catsAndWeights.add(catWeight);
				}
				else if(yesNo == JOptionPane.CANCEL_OPTION){
					JOptionPane.showMessageDialog(null, "Action Cancelled", "System Notification", JOptionPane.INFORMATION_MESSAGE);
					return;
				}
			}
			categories.put(identifier, catsAndWeights);
		}
		
		String status;
		if(fGrade.equalsIgnoreCase("In Progress")) 
			status = "In Progress";
		else status = "Manual Entry";

		cdtm.addRow(new Object[] {subject, title, comment, identifier, credits, numGrade, gMode, fGrade, term, status});
		
		JOptionPane.showMessageDialog(null, "Successfully Updated", "System Notification", JOptionPane.INFORMATION_MESSAGE);
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
			JOptionPane.showMessageDialog(null, "User Action Denied\nReason:\nElement Does Not Exist", "System Notification", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		for(int i = 0; i < cdtm.getRowCount(); i++) {
			if(cdtm.getValueAt(i, 3).equals(identifierInput.getText()) && cdtm.getValueAt(i, 9).equals("Finalized")) {
				JOptionPane.showMessageDialog(null, "User Action Denied\nReason:\nCannot Remove Finalized Course", "System Notification", JOptionPane.ERROR_MESSAGE);
				return;
			}
		}
		
		for(int i = 0; i < cdtm.getRowCount(); i++) {
			if(cdtm.getValueAt(i, 3).equals(identifierInput.getText())) {
				String courseName = (String) cdtm.getValueAt(i, 0);
				if(JOptionPane.showConfirmDialog(null, "Are you sure you want to delete [" + courseName + "]? \nThis action cannot be reversed.") == 0) {
					removeAssociatedGrades(identifierInput.getText());
					cdtm.removeRow(i);
					JOptionPane.showMessageDialog(null, "Successfully Updated", "System Notification", JOptionPane.INFORMATION_MESSAGE);
				}
				else {
					JOptionPane.showMessageDialog(null, "Action Cancelled", "System Notification", JOptionPane.INFORMATION_MESSAGE);
					return;
				}
			}
		}
		
		for(int i = 0; i < gdtm.getRowCount(); i++) {
			if(gdtm.getValueAt(i, 2).equals(identifierInput.getText())) {
				String assignmentCode = (String) gdtm.getValueAt(i, 2);
				if(JOptionPane.showConfirmDialog(null, "Are you sure you want to delete assignment [" + assignmentCode + "]? \nThis action cannot be reversed.") == 0) {
					String id = gdtm.getValueAt(i, 1) + "";
					gdtm.removeRow(i);
					calculateGrade(id);
					JOptionPane.showMessageDialog(null, "Successfully Updated", "System Notification", JOptionPane.INFORMATION_MESSAGE);
				}
				else {
					JOptionPane.showMessageDialog(null, "Action Cancelled", "System Notification", JOptionPane.INFORMATION_MESSAGE);
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
		
		double creditSum = 0;
		double qualitySum = 0;
		double nonGpaSum = 0;
		double failCreditSum = 0;
		
		for(int i = 0; i < cdtm.getRowCount(); i++) {
			if(cdtm.getValueAt(i, 8).equals(year + "") && cdtm.getValueAt(i, 6).equals("Letter") && !cdtm.getValueAt(i, 7).equals("F")) {
				qualitySum += Double.parseDouble((String)cdtm.getValueAt(i, 4)) * letToQual(cdtm.getValueAt(i, 3)+"",(String)cdtm.getValueAt(i, 7));
				creditSum += Double.parseDouble((String)cdtm.getValueAt(i, 4));
				cdtm.setValueAt("Finalized", i, 9);
			}
			
			else if(cdtm.getValueAt(i, 8).equals(year + "") && cdtm.getValueAt(i, 6).equals("Letter") && cdtm.getValueAt(i, 7).equals("NP")) {
				cdtm.setValueAt("Finalized", i, 9);
			}
			
			else if(cdtm.getValueAt(i, 8).equals(year + "") && cdtm.getValueAt(i, 6).equals("Letter") && cdtm.getValueAt(i, 7).equals("F")) {
				qualitySum += Double.parseDouble((String)cdtm.getValueAt(i, 4)) * letToQual(cdtm.getValueAt(i, 3)+"",(String)cdtm.getValueAt(i, 7));
				failCreditSum += Double.parseDouble((String)cdtm.getValueAt(i, 4));
				totalFCreditSum += Double.parseDouble((String)cdtm.getValueAt(i, 4));
				cdtm.setValueAt("Finalized", i, 9);
			}
			
			else if(cdtm.getValueAt(i, 8).equals(year + "") && cdtm.getValueAt(i, 6).equals("P/NP") && cdtm.getValueAt(i, 7).equals("P")) {
				creditSum += Double.parseDouble((String)cdtm.getValueAt(i, 4));
				nonGpaSum += Double.parseDouble((String)cdtm.getValueAt(i, 4));
				nonGpaCreditTotal += Double.parseDouble((String)cdtm.getValueAt(i, 4));
				cdtm.setValueAt("Finalized", i, 9);
			}
			
			else if(cdtm.getValueAt(i, 8).equals(year + "") && cdtm.getValueAt(i, 6).equals("Notation") && cdtm.getValueAt(i, 7).equals("TR")) {
				creditSum += Double.parseDouble((String)cdtm.getValueAt(i, 4));
				nonGpaSum += Double.parseDouble((String)cdtm.getValueAt(i, 4));
				nonGpaCreditTotal += Double.parseDouble((String)cdtm.getValueAt(i, 4));
				cdtm.setValueAt("Finalized", i, 9);
			}
			
			else {
				cdtm.setValueAt("Finalized", i, 9);
			}
		}
		
		if(((String) cdtm.getValueAt(cdtm.getRowCount() - 1, 9)).equalsIgnoreCase("Finalized")) {
				cdtm.addRow(new Object[] {"", "", "", "", "", "", "", "", "", ""});		
				String gpa = qualitySum / (creditSum + failCreditSum - nonGpaSum) + "";
				
				if(gpa.length() > 5)
					gpa = gpa.substring(0,5);
				
				String creditSumString = creditSum + "";
				if(creditSumString.length() > 5)
					creditSumString = creditSumString.substring(0,5);
				
				String qualitySumString = qualitySum + "";
				if(qualitySumString.length() > 5)
					qualitySumString = qualitySumString.substring(0,5);
				
				cdtm.addRow(new Object[] {"Term Credits Earned", creditSumString, "Term Quality Points", qualitySum, "", "", "", "", "GPA", gpa});
				
				double allQualitySum = 0;
				double allCreditSum = 0;
				for(int i = 0; i < cdtm.getRowCount(); i++)
					if(((String) cdtm.getValueAt(i, 0)).equalsIgnoreCase("Term Credits Earned")) {
						allQualitySum += Double.parseDouble(cdtm.getValueAt(i, 3) + "");
						allCreditSum += Double.parseDouble(cdtm.getValueAt(i, 1) + "");
					}
				
				String allCreditSum1 = allCreditSum + "";
				if(allCreditSum1.length() > 5)
					allCreditSum1 = allCreditSum1.substring(0,5);
				
				String allQualitySum1 = allQualitySum + "";
				if(allQualitySum1.length() > 5)
					allQualitySum1 = allQualitySum1.substring(0,5);
				
				String totalGpa = allQualitySum / (allCreditSum + totalFCreditSum - nonGpaCreditTotal) + "";
				if(totalGpa.length() > 5)
					totalGpa = totalGpa.substring(0,5);
				
				cdtm.addRow(new Object[] {"Total Credits Earned", allCreditSum1, "Total Quality Points", allQualitySum1, "", "", "", "", "GPA", totalGpa});	
				
				cdtm.addRow(new Object[] {"", "", "", "", "", "", "", "", "", ""});
				
				courseScales.clear();
				
				JOptionPane.showMessageDialog(null, "Successfully Updated", "System Notification", JOptionPane.INFORMATION_MESSAGE);
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
	
	/**
	 * Tells whether or not an assignment code can be found in the grades table that matches the text field
	 * @return whether or not an assignment code can be found
	 */
	public boolean isCodeFound() {
		
		for(int i = 0; i < gdtm.getRowCount(); i++) {
			if(gdtm.getValueAt(i, 2).equals(identifierInput.getText()))
			    return true;
		}
		return false;
	}
	
	/**
	 * Edits a course in the course table based on user selection.
	 */
	public void editElement() {
		
		if(!isIdentifierFound() && !isCodeFound()) {
			JOptionPane.showMessageDialog(null, "User Action Denied\nReason:\nElement Does Not Exist", "System Notification", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		if(identifierInput.getText().contains("C")) {
		
			for(int i = 0; i < cdtm.getRowCount(); i++) {
				if(cdtm.getValueAt(i, 3).equals(identifierInput.getText()) && cdtm.getValueAt(i, 9).equals("Finalized")) {
					JOptionPane.showMessageDialog(null, "User Action Denied\nReason:\nCannot Edit Finalized Course", "System Notification", JOptionPane.ERROR_MESSAGE);
					return;
				}
			}
			
			int row = 0;
			for(int i = 0; i < cdtm.getRowCount(); i++) {
				if(cdtm.getValueAt(i, 3).equals(identifierInput.getText()))
					row = i;
			}
			
			String[] editChoices = {"Subject/Course Number", "Course Title", "Comment", "Credits", "Category Weightings", "Grade Scale", "Grade Mode", "Final Grade"};
			String[] editChoices2 = {"Subject/Course Number", "Course Title", "Comment", "Credits", "Category Weightings", "Grade Scale", "Final Grade"};
			String edit = "";
			
			for(int i = 0; i < cdtm.getRowCount(); i++)
				if(cdtm.getValueAt(i, 3).equals(identifierInput.getText()) && cdtm.getValueAt(i, 9).equals("Manual Entry")) {
					edit = (String) JOptionPane.showInputDialog(null, "Select Field for Edit", "Course Master", JOptionPane.QUESTION_MESSAGE, null, editChoices2, editChoices2[0]);
					if(edit == null) {
						JOptionPane.showMessageDialog(null, "Action Cancelled", "System Notification", JOptionPane.INFORMATION_MESSAGE);
						return;
					}
				}
			
			if(edit.equals(""))
				edit = (String) JOptionPane.showInputDialog(null, "Select Field for Edit", "Course Master", JOptionPane.QUESTION_MESSAGE, null, editChoices, editChoices[0]);
			
			if(edit == null) {
				JOptionPane.showMessageDialog(null, "Action Cancelled", "System Notification", JOptionPane.INFORMATION_MESSAGE);
				return;
			}
			
			if(edit.equalsIgnoreCase("Subject/Course Number")) {
				String course = JOptionPane.showInputDialog(null, "Enter Subject/Course Number", "Course Master", JOptionPane.INFORMATION_MESSAGE);
				if(course == null) {
					JOptionPane.showMessageDialog(null, "Action Cancelled", "System Notification", JOptionPane.INFORMATION_MESSAGE);
					return;
				}
				else cdtm.setValueAt(course, row, 0);
				JOptionPane.showMessageDialog(null, "Successfully Updated", "System Notification", JOptionPane.INFORMATION_MESSAGE);
			}
			
			if(edit.equalsIgnoreCase("Category Weightings")) {
				ArrayList<String> courseWeightings = categories.get(identifierInput.getText());
				
				String[] options = {"Add Category", "Remove Category", "Change Weighting"};
				String selection = (String) JOptionPane.showInputDialog(null, "Select Action to Perform", "Course Master", JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
				
				if(selection == null) {
					JOptionPane.showMessageDialog(null, "Action Cancelled", "System Notification", JOptionPane.INFORMATION_MESSAGE);
					return;
				}
				
				if(selection.equals("Add Category")) {
					String categoryName = JOptionPane.showInputDialog(null, "Enter Category Name", "Course Master", JOptionPane.INFORMATION_MESSAGE);
					
					if(containsNumbers(categoryName)) {
						JOptionPane.showMessageDialog(null, "User Action Denied\nReason:\nCategory Name Cannot Contain Numbers", "System Notification", JOptionPane.ERROR_MESSAGE);
						return;
					}
					
					if(courseWeightings.contains(categoryName)) {
						JOptionPane.showMessageDialog(null, "User Action Denied\nReason:\nCategory Name Already Exists", "System Notification", JOptionPane.ERROR_MESSAGE);
						return;
					}
					
					String categoryWeight = JOptionPane.showInputDialog(null, "Enter Category Weight", "Course Master", JOptionPane.INFORMATION_MESSAGE);
					try {
						double testError = Double.parseDouble(categoryWeight);
					} catch (NumberFormatException e) {
						JOptionPane.showMessageDialog(null, "User Action Denied\nReason:\nNumber Format Exception", "System Notification", JOptionPane.ERROR_MESSAGE);
						return;
					}
					courseWeightings.add(categoryName);
					courseWeightings.add(categoryWeight);
					JOptionPane.showMessageDialog(null, "Successfully Updated", "System Notification", JOptionPane.INFORMATION_MESSAGE);
				}
				
				else if(selection.equals("Remove Category")) {
					ArrayList<String> categoryNames = getCategoryNames(courseWeightings);
					String[] nameChoices = new String[categoryNames.size()];
					for(int i = 0; i < nameChoices.length; i++)
						nameChoices[i] = categoryNames.get(i);
					
					String nameSelection = (String) JOptionPane.showInputDialog(null, "Select Category to Remove", "Course Master", 
							JOptionPane.QUESTION_MESSAGE, null, nameChoices, nameChoices[0]);
					
					if(nameSelection == null) {
						JOptionPane.showMessageDialog(null, "Action Cancelled", "System Notification", JOptionPane.INFORMATION_MESSAGE);
						return;
					}
					
					if(JOptionPane.showConfirmDialog(null, "Are you sure you wish to delete the category [" + nameSelection + "]?\nThis action cannot be reversed.") != 0) {
						JOptionPane.showMessageDialog(null, "Action Cancelled", "System Notification", JOptionPane.INFORMATION_MESSAGE);
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
					
					JOptionPane.showMessageDialog(null, "Successfully Updated", "System Notification", JOptionPane.INFORMATION_MESSAGE);
					
					calculateGrade(identifierInput.getText());
				}
				
				else if(selection.equals("Change Weighting")) {
					ArrayList<String> categoryNames = getCategoryNames(courseWeightings);
					String[] nameChoices = new String[categoryNames.size()];
					for(int i = 0; i < nameChoices.length; i++)
						nameChoices[i] = categoryNames.get(i);
					
					String nameSelection = (String) JOptionPane.showInputDialog(null, "Select Category to Change Weight", "Course Master", 
							JOptionPane.QUESTION_MESSAGE, null, nameChoices, nameChoices[0]);
					
					if(nameSelection == null) {
						JOptionPane.showMessageDialog(null, "Action Cancelled", "System Notification", JOptionPane.INFORMATION_MESSAGE);
						return;
					}
					
					String newWeight = JOptionPane.showInputDialog(null, "Enter New Weight", "Course Master", JOptionPane.INFORMATION_MESSAGE);
					
					try {
						double testError = Double.parseDouble(newWeight);
					} catch (NumberFormatException e) {
						JOptionPane.showMessageDialog(null, "User Action Denied\nReason:\nNumber Format Exception", "System Notification", JOptionPane.ERROR_MESSAGE);
						return;
					}
					
					if(newWeight == null) {
						JOptionPane.showMessageDialog(null, "Action Cancelled", "System Notification", JOptionPane.INFORMATION_MESSAGE);
						return;
					}
					
					int changeIndex = courseWeightings.indexOf(nameSelection) + 1;
					courseWeightings.set(changeIndex, newWeight);
					
					for(int i = 0; i < gdtm.getRowCount(); i++)
						if(gdtm.getValueAt(i, 1).equals(identifierInput.getText()) && gdtm.getValueAt(i, 3).equals(nameSelection))
							gdtm.setValueAt(newWeight, i, 4);
					
					calculateGrade(identifierInput.getText());
					
					JOptionPane.showMessageDialog(null, "Successfully Updated", "System Notification", JOptionPane.INFORMATION_MESSAGE);
				}
			}
			
			if(edit.equalsIgnoreCase("Course Title")) {
				String prof = JOptionPane.showInputDialog(null, "Enter New Course Title", "Course Master", JOptionPane.INFORMATION_MESSAGE);
				if(prof == null) {
					JOptionPane.showMessageDialog(null, "Action Cancelled", "System Notification", JOptionPane.INFORMATION_MESSAGE);
					return;
				}
				else cdtm.setValueAt(prof, row, 1);
				JOptionPane.showMessageDialog(null, "Successfully Updated", "System Notification", JOptionPane.INFORMATION_MESSAGE);
			}
			
			if(edit.equalsIgnoreCase("Grade Scale")) {
				String[] choices = new String[gradeScales.size()];
				for(int i = 0; i < gradeScales.size(); i++)
					choices[i] = (String) gradeScales.get(i).get("Name");
				
				int index = 0;
				for(int i = 0; i < choices.length; i++)
					if(choices[i].equals(courseScales.get(identifierInput.getText()).get("Name")))
						index = i;
				
				String scale = (String) JOptionPane.showInputDialog(null, "Select New Grading Scale", "Course Master", 
						JOptionPane.QUESTION_MESSAGE, null, choices, choices[index]);
				
				courseScales.remove(identifierInput.getText());
				
				linkScale(identifierInput.getText(), scale);
				
				calculateGrade(identifierInput.getText());
				
				JOptionPane.showMessageDialog(null, "Successfully Updated", "System Notification", JOptionPane.INFORMATION_MESSAGE);
			}
			
			if(edit.equalsIgnoreCase("Comment")) {
				String dayTime = JOptionPane.showInputDialog(null, "Enter New Comment", "Course Master", JOptionPane.INFORMATION_MESSAGE);
				if(dayTime == null) {
					JOptionPane.showMessageDialog(null, "Action Cancelled", "System Notification", JOptionPane.INFORMATION_MESSAGE);
					return;
				}
				else cdtm.setValueAt(dayTime, row, 2);
				JOptionPane.showMessageDialog(null, "Successfully Updated", "System Notification", JOptionPane.INFORMATION_MESSAGE);
			}
			
			if(edit.equalsIgnoreCase("Credits")) {
				String[] creditsChoices = {"0", "0.5", "1", "1.5", "2", "2.5", "3", "4", "5", "6"};
				String credits = (String) JOptionPane.showInputDialog(null, "Select Number of Credits", "Course Edit Master", JOptionPane.QUESTION_MESSAGE, null, creditsChoices, creditsChoices[6]);
				if(credits == null) {
					JOptionPane.showMessageDialog(null, "Action Cancelled", "System Notification", JOptionPane.INFORMATION_MESSAGE);
					return;
				}
				else cdtm.setValueAt(credits, row, 4);
				JOptionPane.showMessageDialog(null, "Successfully Updated", "System Notification", JOptionPane.INFORMATION_MESSAGE);
			}
			
			if(edit.equalsIgnoreCase("Grade Mode")) {
				String[] gModeChoices = {"Letter", "P/NP", "Notation"};
				String gMode = (String) JOptionPane.showInputDialog(null, "Select Grade Mode", "Course Edit Master", JOptionPane.QUESTION_MESSAGE, null, gModeChoices, gModeChoices[0]);
				if(gMode == null) {
					JOptionPane.showMessageDialog(null, "Action Cancelled", "System Notification", JOptionPane.INFORMATION_MESSAGE);
					return;
				}
				
				String[] fGradeChoicesC = {"In Progress", "A","A-","B+","B","B-","C+","C","C-","D+","D","D-","F"};
				String[] fGradeChoicesP = {"In Progress", "P", "NP"};
				String[] notationChoices = {"TR","I","W","Z"};
				String fGrade = "";
				String identifier = (String) cdtm.getValueAt(row, 3);
				
				if(gMode.equals("Letter")) {
					cdtm.setValueAt(gMode, row, 6);
					cdtm.setValueAt(numToLet(identifier, Double.parseDouble(cdtm.getValueAt(row, 5) + ""), gMode), row, 7);
					JOptionPane.showMessageDialog(null, "Successfully Updated", "System Notification", JOptionPane.INFORMATION_MESSAGE);
					return;
				}
				else if(gMode.equals("P/NP")) {
					cdtm.setValueAt(gMode, row, 6);
					cdtm.setValueAt(numToLet(identifier, Double.parseDouble(cdtm.getValueAt(row, 5) + ""), gMode), row, 7);
					JOptionPane.showMessageDialog(null, "Successfully Updated", "System Notification", JOptionPane.INFORMATION_MESSAGE);
					return;
				}
				else if(gMode.equals("Notation")) {
					if(JOptionPane.showConfirmDialog(null, "Are you sure you wish\nto change Grade Mode to Notation?\nNote: Grade Mode Notation cannot be changed back.") == 0) {
						fGrade = (String) JOptionPane.showInputDialog(null, "Select Notation", "Course Master", JOptionPane.QUESTION_MESSAGE, null, notationChoices, notationChoices[0]);
						if(fGrade == null) {
							JOptionPane.showMessageDialog(null, "Action Cancelled", "System Notification", JOptionPane.INFORMATION_MESSAGE);
							return;
						}
						else {
							cdtm.setValueAt(gMode, row, 6);
							removeAssociatedGrades(identifierInput.getText());
							cdtm.setValueAt("Manual Entry", row, 9);
							cdtm.setValueAt("n/a", row, 5);
							cdtm.setValueAt(fGrade, row, 7);
							JOptionPane.showMessageDialog(null, "Successfully Updated", "System Notification", JOptionPane.INFORMATION_MESSAGE);
						}
					}
					else {
						JOptionPane.showMessageDialog(null, "Action Cancelled", "System Notification", JOptionPane.INFORMATION_MESSAGE);
						return;
					}
				}
			}
			
			if(edit.equalsIgnoreCase("Final Grade")) {
				String[] fGradeChoicesC = {"In Progress", "A","A-","B+","B","B-","C+","C","C-","D+","D","D-","F"};
				String[] fGradeChoicesCPlus = {"In Progress", "A+","A","A-","B+","B","B-","C+","C","C-","D+","D","D-","F"};
				String[] fGradeChoicesP = {"In Progress", "P", "NP"};
				String[] notationChoices = {"TR","I","W","Z"};
				String fGrade;
				
				if(cdtm.getValueAt(row, 6).equals("Letter")) {
					if(isAPluses)
						fGrade = (String) JOptionPane.showInputDialog(null, "Select Final Grade", "Course Master", 
								JOptionPane.QUESTION_MESSAGE, null, fGradeChoicesCPlus, fGradeChoicesCPlus[0]);
					else
						fGrade = (String) JOptionPane.showInputDialog(null, "Select Final Grade", "Course Master", 
								JOptionPane.QUESTION_MESSAGE, null, fGradeChoicesC, fGradeChoicesC[0]);
				}
				else if(cdtm.getValueAt(row, 6).equals("P/NP"))
					fGrade = (String) JOptionPane.showInputDialog(null, "Select Final Grade", "Course Master", JOptionPane.QUESTION_MESSAGE, null, fGradeChoicesP, fGradeChoicesP[0]);
				else 
					fGrade = (String) JOptionPane.showInputDialog(null, "Select Final Grade", "Course Master", JOptionPane.QUESTION_MESSAGE, null, notationChoices, notationChoices[0]);
				if(fGrade == null) {
					JOptionPane.showMessageDialog(null, "Action Cancelled", "System Notification", JOptionPane.INFORMATION_MESSAGE);
					return;
				}
				else cdtm.setValueAt(fGrade, row, 7);
				JOptionPane.showMessageDialog(null, "Successfully Updated", "System Notification", JOptionPane.INFORMATION_MESSAGE);
			}
			
			if(edit.equalsIgnoreCase("Term")) {
				String term = JOptionPane.showInputDialog(null, "Enter Term Number", "Course Master", JOptionPane.INFORMATION_MESSAGE);
				try {
					double testError = Double.parseDouble(term);
				} catch (NumberFormatException e) {
					JOptionPane.showMessageDialog(null, "User Action Denied\nReason:\nNumber Format Exception", "System Notification", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if(term == null) {
					JOptionPane.showMessageDialog(null, "Action Cancelled", "System Notification", JOptionPane.INFORMATION_MESSAGE);
					return;
				}
				else cdtm.setValueAt(term, row, 8);
				JOptionPane.showMessageDialog(null, "Successfully Updated", "System Notification", JOptionPane.INFORMATION_MESSAGE);
			}
		}
		
		else {
			
			int row = 0;
			for(int i = 0; i < gdtm.getRowCount(); i++) 
				if(gdtm.getValueAt(i, 2).equals(identifierInput.getText()))
					row = i;
			
			String id = gdtm.getValueAt(row, 1) + "";
					
			String[] options = {"Category", "Grade", "Comment"};
			String choice = (String) JOptionPane.showInputDialog(null, "Select Field for Edit", "Grade Master", JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
					
			if(choice.equals("Category")) {
				ArrayList<String> names = getCategoryNames(categories.get(id));
				String[] categoryNames = new String[names.size()];
				for(int i = 0; i < categoryNames.length; i++)
					categoryNames[i] = names.get(i);
						
				String category = (String) JOptionPane.showInputDialog(null, "Select New Category", "Grade Master", 
						JOptionPane.QUESTION_MESSAGE, null, categoryNames, categoryNames[0]);
				
				gdtm.setValueAt(category, row, 3);
				gdtm.setValueAt(categories.get(id).get(categories.get(id).indexOf(category) + 1), row, 4);
				
				calculateGrade(id);
				JOptionPane.showMessageDialog(null, "Successfully Updated", "System Notification", JOptionPane.INFORMATION_MESSAGE);
			}
			
			else if(choice.equals("Grade")) {
				
				JPanel p = new JPanel();
				p.setLayout(new GridLayout(2, 0));
				String pointsEarned, totalPoints;
				pointsEarned = totalPoints = "";
				
				JTextField pointsEarnedEntry = new JTextField(12);
				JTextField totalPointsEntry = new JTextField(12);
				
				p.add(new JLabel("Enter Points Earned"));
				p.add(pointsEarnedEntry);
				
				p.add(new JLabel("Enter Total Points"));
				p.add(totalPointsEntry);
				
				int result = JOptionPane.showConfirmDialog(null, p, "Grade Master", JOptionPane.OK_CANCEL_OPTION);
				
				if(result == JOptionPane.OK_OPTION) {
					pointsEarned = pointsEarnedEntry.getText();
					totalPoints = totalPointsEntry.getText();
					
					if(pointsEarned.isEmpty() || totalPoints.isEmpty()) {
						JOptionPane.showMessageDialog(null, "User Action Denied\nReason:\nMissing Information", "System Notification", JOptionPane.INFORMATION_MESSAGE);
						return;
					}
				}
				else {
					JOptionPane.showMessageDialog(null, "Action Cancelled", "System Notification", JOptionPane.INFORMATION_MESSAGE);
					return;
				}
				
				if(pointsEarned.length() > 5)
					pointsEarned = pointsEarned.substring(0, 6);
				
				String grade;
				
				try {
					grade = (Double.parseDouble(pointsEarned) / Double.parseDouble(totalPoints) * 100 + "");
				} catch (NumberFormatException e) {
					JOptionPane.showMessageDialog(null, "User Action Denied\nReason:\nNumber Format Exception", "System Notification", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				if(Double.parseDouble(pointsEarned) > 2000) {
					JOptionPane.showMessageDialog(null, "User Action Denied\nReason:\nMaximum Points Earned Exceeded", "System Notification", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				if(Double.parseDouble(totalPoints) > 1000) {
					JOptionPane.showMessageDialog(null, "User Action Denied\nReason:\nMaximum Total Points Exceeded", "System Notification", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				if(Double.parseDouble(pointsEarned) < 0 || Double.parseDouble(totalPoints) < 0) {
					JOptionPane.showMessageDialog(null, "User Action Denied\nReason:\nNegative Values Not Accepted", "System Notification", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				if(grade.length() > 5)
					grade = grade.substring(0, 6);
				
				gdtm.setValueAt(pointsEarned, row, 5);
				gdtm.setValueAt(totalPoints, row, 6);
				gdtm.setValueAt(grade, row, 7);
				
				calculateGrade(id);
				JOptionPane.showMessageDialog(null, "Successfully Updated", "System Notification", JOptionPane.INFORMATION_MESSAGE);
			}
			
			else if(choice.equals("Comment")) {
				String comment = JOptionPane.showInputDialog(null, "Enter New Comment", "Grade Master", JOptionPane.INFORMATION_MESSAGE);
				gdtm.setValueAt(comment, row, 8);
				JOptionPane.showMessageDialog(null, "Successfully Updated", "System Notification", JOptionPane.INFORMATION_MESSAGE);
			}
		}
	}
	
	/**
	 * Gets the category names of a course's category weights
	 * @param categoryWeights a course's category weights (from the hash table)
	 * @return a list of the category names of a course's category weights
	 */
	public ArrayList<String> getCategoryNames(ArrayList<String> categoryWeights) {
		
		ArrayList<String> names = new ArrayList<String>();
		for(int i = 0; i < categoryWeights.size(); i++)
			if(i % 2 == 0)
				names.add(categoryWeights.get(i));
		return names;
	}
	
	/**
	 * Tells whether or not a word contains any numbers
	 * @param word the word to be checked for numbers
	 * @return whether or not the word contains any numbers
	 */
	public boolean containsNumbers(String word) {
		
		String[] nums = {"0","1","2","3","4","5","6","7","8","9","10"};
		for(int i = 0; i < nums.length; i++)
			if(word.contains(nums[i]))
				return true;
		return false;
	}
	
	/**
	 * Gets the category values of a course's category weights
	 * @param categoryWeights a course's category values (from the hash table)
	 * @return a list of the category names of a course's category values
	 */
	public ArrayList<String> getCategoryValues(ArrayList<String> categoryWeights) {
		
		ArrayList<String> values = new ArrayList<String>();
		for(int i = 0; i < categoryWeights.size(); i++)
			if(i % 2 != 0)
				values.add(categoryWeights.get(i));
		return values;
	}
	
	/**
	 * Converts a letter grade to quality points
	 * @param letterGrade the letter grade to calculate the quality points
	 * @return the number of quality points earned
	 */
	public double letToQual(String letterGrade) {
		
		if(rounding.equals("Tenths")) {
			if(letterGrade.equalsIgnoreCase("A+"))
				return 4.3;
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
		else {
			if(letterGrade.equalsIgnoreCase("A+"))
				return 4.33;
			if(letterGrade.equalsIgnoreCase("A")) 
				return 4;
			if(letterGrade.equalsIgnoreCase("A-")) 
				return 3.67;
			if(letterGrade.equalsIgnoreCase("B+")) 
				return 3.33;
			if(letterGrade.equalsIgnoreCase("B")) 
				return 3;
			if(letterGrade.equalsIgnoreCase("B-")) 
				return 2.67;
			if(letterGrade.equalsIgnoreCase("C+")) 
				return 2.33;
			if(letterGrade.equalsIgnoreCase("C")) 
				return 2;
			if(letterGrade.equalsIgnoreCase("C-")) 
				return 1.67;
			if(letterGrade.equalsIgnoreCase("D+")) 
				return 1.33;
			if(letterGrade.equalsIgnoreCase("D")) 
				return 1;
			if(letterGrade.equalsIgnoreCase("D-")) 
				return 0.67;
			return 0;
		}
	}
	
	public double letToQual(String identifier, String letterGrade) {
		
		double quality = 0;
		if(rounding.equals("Tenths")) {
			if(letterGrade.equalsIgnoreCase("A+"))
				quality = 4.3;
			if(letterGrade.equalsIgnoreCase("A")) 
				quality = 4;
			if(letterGrade.equalsIgnoreCase("A-")) 
				quality = 3.7;
			if(letterGrade.equalsIgnoreCase("B+")) 
				quality = 3.3;
			if(letterGrade.equalsIgnoreCase("B")) 
				quality = 3;
			if(letterGrade.equalsIgnoreCase("B-")) 
				quality = 2.7;
			if(letterGrade.equalsIgnoreCase("C+")) 
				quality = 2.3;
			if(letterGrade.equalsIgnoreCase("C")) 
				quality = 2;
			if(letterGrade.equalsIgnoreCase("C-")) 
				quality = 1.7;
			if(letterGrade.equalsIgnoreCase("D+")) 
				quality = 1.3;
			if(letterGrade.equalsIgnoreCase("D")) 
				quality = 1;
			if(letterGrade.equalsIgnoreCase("D-")) 
				quality = 0.7;
		}
		else {
			if(letterGrade.equalsIgnoreCase("A+"))
				quality = 4.33;
			if(letterGrade.equalsIgnoreCase("A")) 
				quality = 4;
			if(letterGrade.equalsIgnoreCase("A-")) 
				quality = 3.67;
			if(letterGrade.equalsIgnoreCase("B+")) 
				quality = 3.33;
			if(letterGrade.equalsIgnoreCase("B")) 
				quality = 3;
			if(letterGrade.equalsIgnoreCase("B-")) 
				quality = 2.67;
			if(letterGrade.equalsIgnoreCase("C+")) 
				quality = 2.33;
			if(letterGrade.equalsIgnoreCase("C")) 
				quality = 2;
			if(letterGrade.equalsIgnoreCase("C-")) 
				quality = 1.67;
			if(letterGrade.equalsIgnoreCase("D+")) 
				quality = 1.33;
			if(letterGrade.equalsIgnoreCase("D")) 
				quality = 1;
			if(letterGrade.equalsIgnoreCase("D-")) 
				quality = 0.67;
		}
		
		if(honorsAPStatuses.get(identifier).equals("Honors"))
			quality += honorsBonus;
		else if(honorsAPStatuses.get(identifier).equals("Advanced Placement"))
			quality += apBonus;
		
		return quality;
	}
	
	/**
	 * Prompts user to enter a grade for any available course in progress. Upon entering the grade,
	 * the course grade will be recalculated.
	 */
	public void enterGrade() {
		
		ArrayList<String> identifiers = new ArrayList<String>();
		for(int i = 0; i < cdtm.getRowCount(); i++)
			if(cdtm.getValueAt(i, 9).equals("In Progress"))
				identifiers.add(cdtm.getValueAt(i, 3) + "");
		
		if(identifiers.size() == 0) {
			JOptionPane.showMessageDialog(null, "User Action Denied\nReason:\nNo Courses Available", "System Notification", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		String[] identifierChoices = new String[identifiers.size()];
		for(int i = 0; i < identifiers.size(); i++)
			identifierChoices[i] = identifiers.get(i);

		String identifier = (String) JOptionPane.showInputDialog(null, "Select Identifier", "Grade Master", 
				JOptionPane.QUESTION_MESSAGE, null, identifierChoices, identifierChoices[0]);
		
		if(identifier == null) {
			JOptionPane.showMessageDialog(null, "Action Cancelled", "System Notification", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		
		String courseTitle = "";
		for(int i = 0; i < cdtm.getRowCount(); i++)
			if(identifier.equals(cdtm.getValueAt(i, 3))) {
				courseTitle = cdtm.getValueAt(i, 1) + "";
				if(courseTitle.equals(""))
					courseTitle = cdtm.getValueAt(i, 0) + "";
			}
		
		assignmentCode += (int) (Math.random() * 50 + 1);
		String code = "A" + assignmentCode;
		
		String[] categoryChoices = new String[categories.get(identifier).size() / 2];
		for(int i = 0; i < categoryChoices.length; i++)
			categoryChoices[i] = categories.get(identifier).get(i * 2);
		
		JPanel p = new JPanel();
		p.setLayout(new GridLayout(4, 0));
		String pointsEarned, totalPoints, category, comment;
		pointsEarned = totalPoints = category = comment = "";
		
		JComboBox categoryEntry = new JComboBox(categoryChoices);
		JTextField pointsEarnedEntry = new JTextField(15);
		JTextField totalPointsEntry = new JTextField(15);
		JTextField commentEntry = new JTextField(15);
		
		p.add(new JLabel("Select Category"));
		p.add(categoryEntry);
		
		p.add(new JLabel("Enter Points Earned"));
		p.add(pointsEarnedEntry);
		
		p.add(new JLabel("Enter Total Points"));
		p.add(totalPointsEntry);
		
		p.add(new JLabel("Enter Comment"));
		p.add(commentEntry);
		
		int result = JOptionPane.showConfirmDialog(null, p, "Grade Master", JOptionPane.OK_CANCEL_OPTION);
		
		if(result == JOptionPane.OK_OPTION) {
			category = (String) categoryEntry.getSelectedItem();
			pointsEarned = pointsEarnedEntry.getText();
			totalPoints = totalPointsEntry.getText();
			comment = commentEntry.getText();
			
			if(pointsEarned.isEmpty() || totalPoints.isEmpty()) {
				JOptionPane.showMessageDialog(null, "User Action Denied\nReason:\nMissing Information", "System Notification", JOptionPane.INFORMATION_MESSAGE);
				return;
			}
		}
		
		else {
			JOptionPane.showMessageDialog(null, "Action Cancelled", "System Notification", JOptionPane.INFORMATION_MESSAGE);
			return; 
		}
				
		int catIndex = 0;
		for(int i = 0; i < categories.get(identifier).size(); i++)
			if(categories.get(identifier).get(i).equals(category))
				catIndex = i;
		
		String catWeight = categories.get(identifier).get(catIndex + 1);
		
		if(pointsEarned.length() > 5)
			pointsEarned = pointsEarned.substring(0, 6);
		
		String grade;
		
		try {
			grade = (Double.parseDouble(pointsEarned) / Double.parseDouble(totalPoints) * 100 + "");
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(null, "User Action Denied\nReason:\nNumber Format Exception", "System Notification", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		if(Double.parseDouble(pointsEarned) > 2000) {
			JOptionPane.showMessageDialog(null, "User Action Denied\nReason:\nMaximum Points Earned Exceeded", "System Notification", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		if(Double.parseDouble(totalPoints) > 1000) {
			JOptionPane.showMessageDialog(null, "User Action Denied\nReason:\nMaximum Total Points Exceeded", "System Notification", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		if(Double.parseDouble(pointsEarned) < 0 || Double.parseDouble(totalPoints) < 0) {
			JOptionPane.showMessageDialog(null, "User Action Denied\nReason:\nNegative Values Not Accepted", "System Notification", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		if(grade.length() > 5)
			grade = grade.substring(0, 6);
		
		gdtm.addRow(new Object[] {courseTitle, identifier, code, category, catWeight, pointsEarned, totalPoints, grade, comment});
		
		calculateGrade(identifier);
		
		JOptionPane.showMessageDialog(null, "Successfully Updated", "System Notification", JOptionPane.INFORMATION_MESSAGE);
	}
	
	/**
	 * Removes grades associated with a specified course.
	 * @param identifier the unique ID of the course
	 */
	public void removeAssociatedGrades(String identifier) {
		
		for(int i = 0; i < gdtm.getRowCount(); i++)
			if(gdtm.getValueAt(i, 1).equals(identifier)) {
				gdtm.removeRow(i);
				i--;
			}
	}
	
	/**
	 * Resets the table settings so that it cannot be edited except through the buttons.
	 */
	public void revertTableSettings() {
		
		courseList.getColumnModel().getColumn(0).setPreferredWidth(125);
        courseList.getColumnModel().getColumn(1).setPreferredWidth(200);
        courseList.getColumnModel().getColumn(2).setPreferredWidth(200);
        courseList.getColumnModel().getColumn(3).setPreferredWidth(100);
        courseList.getColumnModel().getColumn(4).setPreferredWidth(50);
        courseList.getColumnModel().getColumn(5).setPreferredWidth(100);
        courseList.getColumnModel().getColumn(6).setPreferredWidth(75);
        courseList.getColumnModel().getColumn(7).setPreferredWidth(50);
        courseList.getColumnModel().getColumn(8).setPreferredWidth(75);
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
	
	public boolean existsInProgressCourse() {
		for(int i = 0; i < cdtm.getRowCount(); i++)
			if(cdtm.getValueAt(i, 9).equals("In Progress"))
				return true;
		return false;
	}
	
	public void viewParticularBreakdown(String id) {
		
		if(existsUnfinalizedTerm() && existsInProgressCourse()) {
			gdtm.addRow(new Object[] {"","","","","","","","",""});
			gdtm.addRow(new Object[] {"Breakdown Viewed:","","","","","","","",""});
			gdtm.addRow(new Object[] {"","","","","","","","",""});
			ArrayList<String> identifiers = new ArrayList<String>();
			ArrayList<String> titles = new ArrayList<String>();
			ArrayList<String> weights = new ArrayList<String>();
			ArrayList<String> courseTitles = new ArrayList<String>();
			String term = getUnfinalizedTerm();
			for(int i = 0; i < cdtm.getRowCount(); i++) {
				if(cdtm.getValueAt(i, 9).equals("In Progress") && cdtm.getValueAt(i, 3).equals(id)) {
					identifiers.add(cdtm.getValueAt(i, 3) + "");
					if(!cdtm.getValueAt(i, 1).equals(""))
						courseTitles.add(cdtm.getValueAt(i, 1) + "");
					else
						courseTitles.add(cdtm.getValueAt(i, 0) + "");
				}
			}
			for(int i = 0; i < identifiers.size(); i++) {
				String courseTitle = courseTitles.get(i);
				String identifier = identifiers.get(i);
				double pointsEarned = 0;
				double totalPoints = 0;
				titles = getCategoryNames(categories.get(identifiers.get(i)));
				weights = getCategoryValues(categories.get(identifiers.get(i)));
				
				for(int j = 0; j < titles.size(); j++) {
					for(int k = 0; k < gdtm.getRowCount(); k++) 
						if(gdtm.getValueAt(k, 1).equals(identifier) && gdtm.getValueAt(k, 3).equals(titles.get(j))) {
							pointsEarned += Double.parseDouble(gdtm.getValueAt(k, 5) + "");
							totalPoints += Double.parseDouble(gdtm.getValueAt(k, 6) + "");
						}
					double grade = pointsEarned / totalPoints * 100;
					String sGrade = grade + "";
					if(sGrade.length() > 6)
						sGrade = sGrade.substring(0, 6);
					gdtm.addRow(new Object[] {courseTitle,identifier,"",titles.get(j),weights.get(j),pointsEarned,totalPoints,sGrade,""});
					pointsEarned = 0;
					totalPoints = 0;
				}
				
				gdtm.addRow(new Object[] {"","","","","","","","",""});
				
			}
		}
	}
	
	public void viewBreakdown() {
		
		if(existsUnfinalizedTerm() && existsInProgressCourse()) {
			gdtm.addRow(new Object[] {"","","","","","","","",""});
			gdtm.addRow(new Object[] {"Breakdown Viewed:","","","","","","","",""});
			gdtm.addRow(new Object[] {"","","","","","","","",""});
			ArrayList<String> identifiers = new ArrayList<String>();
			ArrayList<String> titles = new ArrayList<String>();
			ArrayList<String> weights = new ArrayList<String>();
			ArrayList<String> courseTitles = new ArrayList<String>();
			String term = getUnfinalizedTerm();
			for(int i = 0; i < cdtm.getRowCount(); i++) {
				if(cdtm.getValueAt(i, 9).equals("In Progress")) {
					identifiers.add(cdtm.getValueAt(i, 3) + "");
					if(!cdtm.getValueAt(i, 1).equals(""))
						courseTitles.add(cdtm.getValueAt(i, 1) + "");
					else
						courseTitles.add(cdtm.getValueAt(i, 0) + "");
				}
			}
			for(int i = 0; i < identifiers.size(); i++) {
				String courseTitle = courseTitles.get(i);
				String identifier = identifiers.get(i);
				double pointsEarned = 0;
				double totalPoints = 0;
				titles = getCategoryNames(categories.get(identifiers.get(i)));
				weights = getCategoryValues(categories.get(identifiers.get(i)));
				
				for(int j = 0; j < titles.size(); j++) {
					for(int k = 0; k < gdtm.getRowCount(); k++) 
						if(gdtm.getValueAt(k, 1).equals(identifier) && gdtm.getValueAt(k, 3).equals(titles.get(j))) {
							pointsEarned += Double.parseDouble(gdtm.getValueAt(k, 5) + "");
							totalPoints += Double.parseDouble(gdtm.getValueAt(k, 6) + "");
						}
					double grade = pointsEarned / totalPoints * 100;
					String sGrade = grade + "";
					if(sGrade.length() > 6)
						sGrade = sGrade.substring(0, 6);
					gdtm.addRow(new Object[] {courseTitle,identifier,"",titles.get(j),weights.get(j),pointsEarned,totalPoints,sGrade,""});
					pointsEarned = 0;
					totalPoints = 0;
				}
				
				gdtm.addRow(new Object[] {"","","","","","","","",""});
				
			}
		}
		else {
			JOptionPane.showMessageDialog(null, "User Action Denied\nReason:\nNo Courses In Progress", "System Notification", JOptionPane.ERROR_MESSAGE);
			return;
		}
		JOptionPane.showMessageDialog(null, "Successfully Updated", "System Notification", JOptionPane.INFORMATION_MESSAGE);
	}
	
	public void hideBreakdown() {
		for(int i = 0; i < gdtm.getRowCount(); i++)
			if(gdtm.getValueAt(i, 2).equals("")) {
				gdtm.removeRow(i);
				i--;
			}
	}
	
	public void settings() {
		
		String[] aPlusChoices = {"No", "Yes"};
		String[] decimalChoices = {"Tenths", "Hundredths"};
		String[] apHonorsChoices = {"No", "Yes"};
		
		JPanel p = new JPanel();
		p.setLayout(new GridLayout(6, 0));
		
		JComboBox aPlusEntry = new JComboBox(aPlusChoices);
		JComboBox decimalEntry = new JComboBox(decimalChoices);
		JComboBox isAPHonorsEntry = new JComboBox(apHonorsChoices);
		JTextField honorsBonusEntry = new JTextField(15);
		JTextField apBonusEntry = new JTextField(15);
		JButton setDefaults = new JButton("Set Defaults");
		setDefaults.addActionListener(this);
		JButton deleteGradeScale = new JButton("Delete Grade Scale");
		deleteGradeScale.addActionListener(this);
		JButton addNewGradeScale = new JButton("Add Grade Scale");
		addNewGradeScale.addActionListener(this);
		JButton editGradeScale = new JButton("Edit Grade Scale");
		editGradeScale.addActionListener(this);
		
		p.add(new JLabel("Is A+ Allowed?"));
		p.add(aPlusEntry);
		if(isAPluses)
			aPlusEntry.setSelectedItem("Yes");
		else
			aPlusEntry.setSelectedItem("No");
		
		p.add(new JLabel("GPA Decimal Conversion"));
		p.add(decimalEntry);
		if(rounding.equals("Tenths"))
			decimalEntry.setSelectedItem("Tenths");
		else
			decimalEntry.setSelectedItem("Hundredths");
		
		p.add(new JLabel("Are there Honors/AP Bonuses?"));
		p.add(isAPHonorsEntry);
		if(isHonorsAPClasses)
			isAPHonorsEntry.setSelectedItem("Yes");
		else 
			isAPHonorsEntry.setSelectedItem("No");
		
		p.add(new JLabel("Honors Class GPA Bonus"));
		p.add(honorsBonusEntry);
		honorsBonusEntry.setText(honorsBonus + "");
		
		p.add(new JLabel("AP Class GPA Bonus"));
		p.add(apBonusEntry);
		apBonusEntry.setText(apBonus + "");
		
		p.add(addNewGradeScale);
		p.add(deleteGradeScale);
		
		int result = JOptionPane.showConfirmDialog(null, p, "Settings Master", JOptionPane.OK_CANCEL_OPTION);
		
		if(result == JOptionPane.OK_OPTION) {
			
			if(aPlusEntry.getSelectedItem().equals("Yes"))
				isAPluses = true;
			else
				isAPluses = false;
			
			if(isAPHonorsEntry.getSelectedItem().equals("Yes"))
				isHonorsAPClasses = true;
			else
				isHonorsAPClasses = false;
			
			if(decimalEntry.getSelectedItem().equals("Tenths"))
				rounding = "Tenths";
			else
				rounding = "Hundredths";
			
			try {
				double testError = Double.parseDouble(honorsBonusEntry.getText());
			} catch (NumberFormatException e) {
				JOptionPane.showMessageDialog(null, "User Action Denied\nReason:\nNumber Format Exception", "System Notification", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			try {
				double testError = Double.parseDouble(apBonusEntry.getText());
			} catch (NumberFormatException e) {
				JOptionPane.showMessageDialog(null, "User Action Denied\nReason:\nNumber Format Exception", "System Notification", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			honorsBonus = Double.parseDouble(honorsBonusEntry.getText());
			apBonus = Double.parseDouble(apBonusEntry.getText());
			
			if(honorsBonus < 0 || apBonus < 0) {
				honorsBonus = 1;
				apBonus = 1;
				JOptionPane.showMessageDialog(null, "User Action Denied\nReason:\nCannot Have Negative Bonus", "System Notification", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			if(honorsBonusEntry.getText().isEmpty() || apBonusEntry.getText().isEmpty()) {
				JOptionPane.showMessageDialog(null, "User Action Denied\nReason:\nMissing Information", "System Notification", JOptionPane.INFORMATION_MESSAGE);
				return;
			}
		} 
	}
	
	public void addGradeScale() {
		
		JPanel p = new JPanel();
		p.setLayout(new GridLayout(14, 0));
		JTextField name = new JTextField(12);
		JTextField minAp = new JTextField(12);
		if(!isAPluses)
			minAp.setEnabled(false);
		JTextField minA = new JTextField(12);
		JTextField minAm = new JTextField(12);
		JTextField minBp = new JTextField(12);
		JTextField minB = new JTextField(12);
		JTextField minBm = new JTextField(12);
		JTextField minCp = new JTextField(12);
		JTextField minC = new JTextField(12);
		JTextField minCm = new JTextField(12);
		JTextField minDp = new JTextField(12);
		JTextField minD = new JTextField(12);
		JTextField minDm = new JTextField(12);
		JTextField minP = new JTextField(12);
		
		p.add(new JLabel("Enter Name for Grade Scale"));
		p.add(name);
		p.add(new JLabel("Min Grade for A+"));
		p.add(minAp);
		p.add(new JLabel("Min Grade for A"));
		p.add(minA);
		p.add(new JLabel("Min Grade for A-"));
		p.add(minAm);
		p.add(new JLabel("Min Grade for B+"));
		p.add(minBp);
		p.add(new JLabel("Min Grade for B"));
		p.add(minB);
		p.add(new JLabel("Min Grade for B-"));
		p.add(minBm);
		p.add(new JLabel("Min Grade for C+"));
		p.add(minCp);
		p.add(new JLabel("Min Grade for C"));
		p.add(minC);
		p.add(new JLabel("Min Grade for C-"));
		p.add(minCm);
		p.add(new JLabel("Min Grade for D+"));
		p.add(minDp);
		p.add(new JLabel("Min Grade for D"));
		p.add(minD);
		p.add(new JLabel("Min Grade for D-"));
		p.add(minDm);
		p.add(new JLabel("Min Grade for Pass"));
		p.add(minP);
		
		int result = JOptionPane.showConfirmDialog(null, p, "Settings Master", JOptionPane.OK_CANCEL_OPTION);
		
		if(result == JOptionPane.OK_OPTION) {
			
			try {
				double testError = 0;
				if(isAPluses)
					testError = Double.parseDouble(minAp.getText());
				testError = Double.parseDouble(minA.getText());
				testError = Double.parseDouble(minAm.getText());
				testError = Double.parseDouble(minBp.getText());
				testError = Double.parseDouble(minB.getText());
				testError = Double.parseDouble(minBm.getText());
				testError = Double.parseDouble(minCp.getText());
				testError = Double.parseDouble(minC.getText());
				testError = Double.parseDouble(minCm.getText());
				testError = Double.parseDouble(minDp.getText());
				testError = Double.parseDouble(minD.getText());
				testError = Double.parseDouble(minDm.getText());
				testError = Double.parseDouble(minP.getText());
				
			} catch (NumberFormatException e) {
				JOptionPane.showMessageDialog(null, "User Action Denied\nReason:\nNumber Format Exception", "System Notification", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			Hashtable scale = new Hashtable();
			if(!isAPluses) {
				if(!(Double.parseDouble(minA.getText()) > Double.parseDouble(minAm.getText()) &&
						Double.parseDouble(minAm.getText()) > Double.parseDouble(minBp.getText()) &&
						Double.parseDouble(minBp.getText()) > Double.parseDouble(minB.getText()) &&
						Double.parseDouble(minB.getText()) > Double.parseDouble(minBm.getText()) &&
						Double.parseDouble(minBm.getText()) > Double.parseDouble(minCp.getText()) &&
						Double.parseDouble(minCp.getText()) > Double.parseDouble(minC.getText()) &&
						Double.parseDouble(minC.getText()) > Double.parseDouble(minCm.getText()) &&
						Double.parseDouble(minCm.getText()) > Double.parseDouble(minDp.getText()) &&
						Double.parseDouble(minDp.getText()) > Double.parseDouble(minD.getText()) &&
						Double.parseDouble(minD.getText()) > Double.parseDouble(minDm.getText()))) {
					JOptionPane.showMessageDialog(null, "User Action Denied\nReason:\nInvalid Grade Scale", "System Notification", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if((Double.parseDouble(minA.getText()) < 0 || Double.parseDouble(minAm.getText()) < 0 &&
					    Double.parseDouble(minBp.getText()) < 0 || Double.parseDouble(minB.getText()) < 0 ||
						Double.parseDouble(minBm.getText()) < 0 || Double.parseDouble(minCp.getText()) < 0 ||
						Double.parseDouble(minC.getText()) < 0 || Double.parseDouble(minCm.getText()) < 0 ||
						Double.parseDouble(minDp.getText()) < 0 || Double.parseDouble(minD.getText()) < 0 ||
						Double.parseDouble(minDm.getText()) < 0)) {
					JOptionPane.showMessageDialog(null, "User Action Denied\nReason:\nInvalid Grade Scale", "System Notification", JOptionPane.ERROR_MESSAGE);
					return;
				}
				minAp.setEnabled(false);
				scale.put("Name", name.getText());
				scale.put("A+", Double.MAX_VALUE);
				scale.put("A", Double.parseDouble(minA.getText()));
				scale.put("A-", Double.parseDouble(minAm.getText()));
				scale.put("B+", Double.parseDouble(minBp.getText()));
				scale.put("B", Double.parseDouble(minB.getText()));
				scale.put("B-", Double.parseDouble(minBm.getText()));
				scale.put("C+", Double.parseDouble(minCp.getText()));
				scale.put("C", Double.parseDouble(minC.getText()));
				scale.put("C-", Double.parseDouble(minCm.getText()));
				scale.put("D+", Double.parseDouble(minDp.getText()));
				scale.put("D", Double.parseDouble(minD.getText()));
				scale.put("D-", Double.parseDouble(minDm.getText()));
				scale.put("P", Double.parseDouble(minP.getText()));
			}
			
			else {
				if(!(Double.parseDouble(minAp.getText()) > Double.parseDouble(minA.getText()) &&
						Double.parseDouble(minA.getText()) > Double.parseDouble(minAm.getText()) &&
						Double.parseDouble(minAm.getText()) > Double.parseDouble(minBp.getText()) &&
						Double.parseDouble(minBp.getText()) > Double.parseDouble(minB.getText()) &&
						Double.parseDouble(minB.getText()) > Double.parseDouble(minBm.getText()) &&
						Double.parseDouble(minBm.getText()) > Double.parseDouble(minCp.getText()) &&
						Double.parseDouble(minCp.getText()) > Double.parseDouble(minC.getText()) &&
						Double.parseDouble(minC.getText()) > Double.parseDouble(minCm.getText()) &&
						Double.parseDouble(minCm.getText()) > Double.parseDouble(minDp.getText()) &&
						Double.parseDouble(minDp.getText()) > Double.parseDouble(minD.getText()) &&
						Double.parseDouble(minD.getText()) > Double.parseDouble(minDm.getText()))) {
					JOptionPane.showMessageDialog(null, "User Action Denied\nReason:\nInvalid Grade Scale", "System Notification", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if((Double.parseDouble(minA.getText()) < 0 || Double.parseDouble(minAm.getText()) < 0 &&
					    Double.parseDouble(minBp.getText()) < 0 || Double.parseDouble(minB.getText()) < 0 ||
						Double.parseDouble(minBm.getText()) < 0 || Double.parseDouble(minCp.getText()) < 0 ||
						Double.parseDouble(minC.getText()) < 0 || Double.parseDouble(minCm.getText()) < 0 ||
						Double.parseDouble(minDp.getText()) < 0 || Double.parseDouble(minD.getText()) < 0 ||
						Double.parseDouble(minDm.getText()) < 0)) {
					JOptionPane.showMessageDialog(null, "User Action Denied\nReason:\nInvalid Grade Scale", "System Notification", JOptionPane.ERROR_MESSAGE);
					return;
				}
				scale.put("Name", name.getText());
				scale.put("A+", Double.parseDouble(minAp.getText()));
				scale.put("A", Double.parseDouble(minA.getText()));
				scale.put("A-", Double.parseDouble(minAm.getText()));
				scale.put("B+", Double.parseDouble(minBp.getText()));
				scale.put("B", Double.parseDouble(minB.getText()));
				scale.put("B-", Double.parseDouble(minBm.getText()));
				scale.put("C+", Double.parseDouble(minCp.getText()));
				scale.put("C", Double.parseDouble(minC.getText()));
				scale.put("C-", Double.parseDouble(minCm.getText()));
				scale.put("D+", Double.parseDouble(minDp.getText()));
				scale.put("D", Double.parseDouble(minD.getText()));
				scale.put("D-", Double.parseDouble(minDm.getText()));
				scale.put("P", Double.parseDouble(minP.getText()));
				
			}
			gradeScales.add(scale);
			JOptionPane.showMessageDialog(null, "Successfully Updated", "System Notification", JOptionPane.INFORMATION_MESSAGE);
		}
	}
	
	public void deleteGradeScale() {
		
		if(gradeScales.size() == 0) {
			JOptionPane.showMessageDialog(null, "User Action Denied\nReason:\nNo Grade Scales to Delete", "System Notification", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		String[] choices = new String[gradeScales.size()];
		for(int i = 0; i < gradeScales.size(); i++)
			choices[i] = (String) gradeScales.get(i).get("Name");
		
		String delete = (String) JOptionPane.showInputDialog(null, "Select Grade Scale to Delete", "Settings Master", 
				JOptionPane.QUESTION_MESSAGE, null, choices, choices[0]);
		
		for(int i = 0; i < gradeScales.size(); i++) {
			if(gradeScales.get(i).get("Name").equals(delete)) {
				for(int j = 0; j < courseScales.size(); j++)
				if(courseScales.containsValue(gradeScales.get(i))) {
					JOptionPane.showMessageDialog(null, "User Action Denied\nReason:\nGrade Scale In Use", "System Notification", JOptionPane.ERROR_MESSAGE);
					return;
				}
				gradeScales.remove(i);
				JOptionPane.showMessageDialog(null, "Successfully Updated", "System Notification", JOptionPane.INFORMATION_MESSAGE);
			}
		}
	}
	
	public String getScaleName(Hashtable scale) {
	    return (String) scale.get("Name");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		String s = e.getActionCommand();
		
		if(s.equalsIgnoreCase("Add Course")) {
			
			if(gradeScales.size() == 0) {
				JOptionPane.showMessageDialog(null, "User Action Denied\nReason:\nMust Create a Grade Scale", "System Notification", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			if(!isHonorsAPClasses)
				addCourse();
			else 
				addCourseHonorsAP();
		}
		
		if(s.equalsIgnoreCase("Remove Element")) {
			removeElement();
		}
		
		if(s.equalsIgnoreCase("Finalize Grades")) {
			
			for(int i = 0; i < cdtm.getRowCount(); i++) {
				if(cdtm.getValueAt(i, 7).equals("In Progress")) {
					JOptionPane.showMessageDialog(null, "User Action Denied\nReason:\nCannot Finalize with\nFinal Grade = In Progress", "System Notification", JOptionPane.ERROR_MESSAGE);
					return;
				}
			}
				
			if(!existsUnfinalizedCourses() || cdtm.getRowCount() == 0) {
				JOptionPane.showMessageDialog(null, "User Action Denied\nReason:\nAll Courses Finalized", "System Notification", JOptionPane.ERROR_MESSAGE);
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
					JOptionPane.showMessageDialog(null, "User Action Denied\nReason:\nAll Courses Finalized", "System Notification", JOptionPane.ERROR_MESSAGE);
					return;
				}
			}
			else {
				JOptionPane.showMessageDialog(null, "Action Cancelled", "System Notification", JOptionPane.INFORMATION_MESSAGE);
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
					JOptionPane.showMessageDialog(null, "Action Cancelled", "System Notification", JOptionPane.INFORMATION_MESSAGE);
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
		
		if(s.equalsIgnoreCase("Edit Element")) {
			editElement();
		}
		
		if(s.equalsIgnoreCase("Enter Grade")) {
			hideBreakdown();
			enterGrade();
		}
		
		if(s.equalsIgnoreCase("View Breakdown")) {
			if(isIdentifierFound()) {
				viewBreakdown.setText("Hide Breakdown");
				viewParticularBreakdown(identifierInput.getText());
				JOptionPane.showMessageDialog(null, "Successfully Updated", "System Notification", JOptionPane.INFORMATION_MESSAGE);
			}
			else {
				JOptionPane.showMessageDialog(null, "User Action Denied\nReason:\nElement Does Not Exist", "System Notification", JOptionPane.ERROR_MESSAGE);
			}
		}
		
		if(s.equalsIgnoreCase("Hide Breakdown")) {
			hideBreakdown();
			viewBreakdown.setText("View Breakdown");
			JOptionPane.showMessageDialog(null, "Successfully Updated", "System Notification", JOptionPane.INFORMATION_MESSAGE);
		}
		
		if(s.equalsIgnoreCase("Import/Export")) {
			if(cdtm.getRowCount() == 0 && gdtm.getRowCount() == 0) {
				loadTable();
			}
			else
				saveTable();
		}
		
		if(s.equalsIgnoreCase("Settings")) {
			settings();
		}
		
		if(s.equalsIgnoreCase("Help")) {
			String[] options = {"Instructions", "Button Explanations", "Troubleshooting"};
			String choice = (String) JOptionPane.showInputDialog(null, "Select an Option to Learn More", "Help Master", JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
			
			if(choice == null) {
				JOptionPane.showMessageDialog(null, "Action Cancelled", "System Notification", JOptionPane.INFORMATION_MESSAGE);
				return;
			}
			
		}
		
		if(s.equalsIgnoreCase("Add Grade Scale")) {
			addGradeScale();
		}
		
		if(s.equalsIgnoreCase("Delete Grade Scale")) {
			deleteGradeScale();
		}
			
	}
	
	public static void main(String[] a) {
		System.out.println("loading");
		new Gradebook();
	}
}