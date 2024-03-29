import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

/**
 * Grade Calculator v5
 * Copyright © 2020 Ryan Hudson. All rights reserved.
 */
public class Gradebook extends JFrame implements ActionListener {

	private JTextField identifierInput; // The text field
	
	private static DefaultTableModel cdtm; // Course Default Table Model
	private static DefaultTableModel gdtm; // Grade Default Table Model
	
	private JTable courseList, gradeList; // JTables containing grades and courses
	
	private int assignmentCode; // Code unique to each grade/assignment
	private int courseCode; // Code unique to each course (AKA identifier)
	
	private double nonGpaCreditTotal; // The total amount of credits not applied to the gpa
	private double gpaCreditTotal; // The total amount of credits applied to the gpa
	
	private ArrayList<Double> termGpas; // Used to calculate total gpa for what if gpa
	private ArrayList<Double> termCreditTotals; // Used to calculate total gpa for what if gpa
	
	private JFrame frame; // The frame that holds the tables and buttons
	
	private Hashtable<String, ArrayList<String>> categories; // Contains the category weights of each course
	
	private JButton viewBreakdown; // JButton triggering the breakdown of category grading
	
	private boolean isAPluses; // Whether or not A+ is allowed
	private boolean isHonorsAPClasses; // Whether or not there are honors/AP classes
	private double honorsBonus; // Honors GPA bonus
	private double apBonus; // AP GPA bonus
	private Hashtable<String, String> honorsAPStatuses; // Links Course ID to Honors/AP/CP
	private String rounding; // GPA is calculated using tenths/hundredths
	
	private boolean checkedSettings; // Whether or not the user confirmed settings
	
	private ArrayList<Hashtable> gradeScales; // ArrayList of all available grade scales
	
	private Hashtable<String, Hashtable> courseScales; // Links a course to a specific grade scale
	
	private Hashtable defaultScale; // The default grade scale
	
	private String defaultCreditAmount;
	private String defaultGMode;
	private String defaultGMethod;
	
	private double totalFCreditSum; // Total credits failed
	
	private JFileChooser fileChooser; // Selects file for import/export
	
	public Gradebook() {
		
		UIManager UI = new UIManager();
		
		try {
			UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		UIManager.getLookAndFeelDefaults().replace("Panel.background", new ColorUIResource(242, 236, 228));
		UIManager.getLookAndFeelDefaults().replace("OptionPane.background", new ColorUIResource(242, 236, 228));

		// Frame that holds everything
        frame = new JFrame();
        
        frame.setTitle("Grade Calculator v5"); 
        
        frame.setVisible(true); 
        
        frame.setResizable(false);
        
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);;
        
        // Initialize variables
        isAPluses = false;
        isHonorsAPClasses = false;
        honorsBonus = 1;
        apBonus = 1;
        rounding = "Tenths";
        checkedSettings = false;
        gradeScales = new ArrayList<Hashtable>();
        courseScales = new Hashtable<String, Hashtable>();
        honorsAPStatuses = new Hashtable<String, String>();
        termCreditTotals = new ArrayList<Double>();
        termGpas = new ArrayList<Double>();
        
        defaultCreditAmount = "3";
        defaultGMode = "Letter";
        defaultGMethod = "CategoryWeightings";
        
        defaultScale = new Hashtable();
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
        
        fileChooser = new JFileChooser(new File("."));
  
        // ------------------------Creating the table for the list of courses------------------------
        courseList = new JTable();
        cdtm = new DefaultTableModel(0,0);
        
        String courseHeader[] = new String[] { "Subject/Course Number", "Course Title", "Comment", "Identifier",
        		"Credits", "Numeric Grade", "Grade Mode", "Final Grade", "Term", "Status" };
        categories = new Hashtable<String, ArrayList<String>>();
        
        cdtm.setColumnIdentifiers(courseHeader);
        courseList.setModel(cdtm);
        courseCode = 11111;
  
        // adding it to JScrollPane 
        JScrollPane spc = new JScrollPane(courseList); 
        spc.getViewport().setBackground(Color.WHITE);
        frame.add(spc, BorderLayout.NORTH); 
        
        // ------------------------Creating the panel for the buttons------------------------
        JPanel buttonPanel = new JPanel();
        buttonPanel.setVisible(true);
        
        // Initialize buttons and add action listeners
        JButton addCourse = new JButton("Add Course");
        addCourse.addActionListener(this);
        
        JButton addAssignment = new JButton("Add Assignment");
        addAssignment.addActionListener(this);
        
        JButton removeCourse = new JButton("Remove Element");
        removeCourse.addActionListener(this);
        
        JButton editElement = new JButton("Edit Element");
        editElement.addActionListener(this);
        
        viewBreakdown = new JButton("Analyze Grades");
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
        JLabel identifier = new JLabel("     Identifier/Code: ");
        identifierInput = new JTextField();
        identifierInput.setPreferredSize(new Dimension(100,25));
        identifierInput.setEditable(true);
        identifierInput.setBounds(10,10,300,50);
        identifierInput.setSize(200, 20);
        
        // Add the buttons to the panel
        double screenWidth = Toolkit.getDefaultToolkit().getScreenSize().getWidth();
        Dimension preferredSize = new Dimension(115, 25);
        JButton[] buttons = {addCourse, addAssignment, removeCourse, editElement, viewBreakdown, finalizeGrades,
        		importExport, settings, help};
        
        JLabel calcLabel = new JLabel("Grade Calculator v5     ");
        calcLabel.setFont(new Font("Courier", Font.ITALIC, 14));
        buttonPanel.add(calcLabel);
        
        for(int i = 0; i < buttons.length; i++) {
        	buttonPanel.add(buttons[i]);
        	buttons[i].setSelected(false);
        	buttons[i].setBackground(new Color(115,213,255));
        	//buttons[i].setBackground(new Color(225, 136, 52));
        }
        
        buttonPanel.add(identifier);
        buttonPanel.add(identifierInput);
        buttonPanel.setBackground(new Color(242, 236, 228));
        buttonPanel.setPreferredSize(new Dimension((int) screenWidth,40));
        
        // Add the panel to the frame
        frame.add(buttonPanel);
        
        // ------------------------Creating the table for the list of grades/assignments------------------------
        gradeList = new JTable();
        gdtm = new DefaultTableModel(0,0);
        
        assignmentCode = 11111;
        nonGpaCreditTotal = 0;
        totalFCreditSum = 0;
        
        String gradeHeader[] = new String[] { "Course Title", "Identifier", "Assignment Code", "Category Name", 
        		"Category Weight", "Points Earned", "Total Points", "Grade", "Comment" };
        
        gdtm.setColumnIdentifiers(gradeHeader);
        gradeList.setModel(gdtm);
        
        JScrollPane spg = new JScrollPane(gradeList); 
        frame.add(spg, BorderLayout.SOUTH);
        spg.getViewport().setBackground(Color.WHITE);
        
        revertTableSettings();
        
        buttonPanel.setPreferredSize(new Dimension(1400, 40));
        
        frame.pack();

	}
	
	public void displayWhatIfGPA() {
	
		String term = getUnfinalizedTerm();
		
		double creditSum = 0;
		double qualitySum = 0;
		double nonGpaSum = 0;
		double failCreditSum = 0;
		double tempTotalFCreditSum = totalFCreditSum;
		double tempNonGpaCreditTotal = nonGpaCreditTotal;
		
		String gpaDisplayer = "~~What-If GPA~~\n\n";
		
		for(int i = 0; i < cdtm.getRowCount(); i++) {
			if(cdtm.getValueAt(i, 8).equals(term + "") && cdtm.getValueAt(i, 6).equals("Letter") && !cdtm.getValueAt(i, 7).equals("F")) {
				qualitySum += Double.parseDouble((String)cdtm.getValueAt(i, 4)) * letToQual(cdtm.getValueAt(i, 3)+"",(String)cdtm.getValueAt(i, 7));
				creditSum += Double.parseDouble((String)cdtm.getValueAt(i, 4));
			}
			
			else if(cdtm.getValueAt(i, 8).equals(term + "") && cdtm.getValueAt(i, 6).equals("Letter") && cdtm.getValueAt(i, 7).equals("F")) {
				qualitySum += Double.parseDouble((String)cdtm.getValueAt(i, 4)) * letToQual(cdtm.getValueAt(i, 3)+"",(String)cdtm.getValueAt(i, 7));
				failCreditSum += Double.parseDouble((String)cdtm.getValueAt(i, 4));
				totalFCreditSum += Double.parseDouble((String)cdtm.getValueAt(i, 4));
			}
			
			else if(cdtm.getValueAt(i, 8).equals(term + "") && cdtm.getValueAt(i, 6).equals("P/NP") && cdtm.getValueAt(i, 7).equals("P")) {
				creditSum += Double.parseDouble((String)cdtm.getValueAt(i, 4));
				nonGpaSum += Double.parseDouble((String)cdtm.getValueAt(i, 4));
				nonGpaCreditTotal += Double.parseDouble((String)cdtm.getValueAt(i, 4));
			}
			
			else if(cdtm.getValueAt(i, 8).equals(term + "") && cdtm.getValueAt(i, 6).equals("Notation") && cdtm.getValueAt(i, 7).equals("TR")) {
				creditSum += Double.parseDouble((String)cdtm.getValueAt(i, 4));
				nonGpaSum += Double.parseDouble((String)cdtm.getValueAt(i, 4));
				nonGpaCreditTotal += Double.parseDouble((String)cdtm.getValueAt(i, 4));
			}
		}
		
		if(creditSum == 0) {
			Errors.ML5.displayErrorMsg();
			return;
		}
			
		DecimalFormat rounder = new DecimalFormat("#.####");
		rounder.setRoundingMode(RoundingMode.HALF_UP);
			
		String gpa = rounder.format(qualitySum / (creditSum + failCreditSum - nonGpaSum)) + "";
				
		if(!gpa.contains("."))
			gpa = gpa + ".0";
				
		String creditSumString = rounder.format(creditSum) + "";
				
		String qualitySumString = rounder.format(qualitySum) + "";
		
		//gpaDisplayer += "Current Term Credits Earned: " + creditSumString + "\n";
		gpaDisplayer += "Current Term GPA: " + gpa + "\n\n";
		
		double totalGpa = 0;
		for(int i = 0; i < termGpas.size(); i++) {
			totalGpa += termGpas.get(i) * termCreditTotals.get(i);
		}
		
		totalGpa += Double.parseDouble(gpa) * (creditSum + failCreditSum - nonGpaSum);
		
		double termCreditSummation = 0;
		for(int i = 0; i < termCreditTotals.size(); i++) {
			termCreditSummation += termCreditTotals.get(i);
		}
		
		termCreditSummation += Double.parseDouble(creditSumString);
		
		totalGpa /= termCreditSummation;
		
		//gpaDisplayer += "Cumulative Credits Earned: " + rounder.format(termCreditSummation) + "\n";
		gpaDisplayer += "Cumulative GPA: " + rounder.format(totalGpa);	
		
		JOptionPane.showMessageDialog(null, gpaDisplayer, "Grade Master", JOptionPane.INFORMATION_MESSAGE);
		
		nonGpaCreditTotal = tempNonGpaCreditTotal;
		totalFCreditSum = tempTotalFCreditSum;
	}
	
	/**
	 * Resets the table settings so that it cannot be edited except through the buttons.
	 */
	public void revertTableSettings() {
		
		courseList.setShowGrid(false);
        courseList.setColumnSelectionAllowed(false);
        courseList.getTableHeader().setReorderingAllowed(false);
        courseList.getTableHeader().setResizingAllowed(false);
        courseList.setEnabled(false);
        courseList.setSelectionBackground(new Color(250, 246, 212));
        courseList.setSelectionForeground(Color.BLACK);
        courseList.setBackground(Color.WHITE);
        
        courseList.getColumnModel().getColumn(0).setPreferredWidth(150);
        courseList.getColumnModel().getColumn(1).setPreferredWidth(200);
        courseList.getColumnModel().getColumn(2).setPreferredWidth(200);
        courseList.getColumnModel().getColumn(3).setPreferredWidth(100);
        courseList.getColumnModel().getColumn(4).setPreferredWidth(100);
        courseList.getColumnModel().getColumn(5).setPreferredWidth(100);
        courseList.getColumnModel().getColumn(6).setPreferredWidth(100);
        courseList.getColumnModel().getColumn(7).setPreferredWidth(100);
        courseList.getColumnModel().getColumn(8).setPreferredWidth(100);
        courseList.getColumnModel().getColumn(9).setPreferredWidth(100);
		
        gradeList.setShowGrid(false);
        gradeList.setColumnSelectionAllowed(false);
        gradeList.getTableHeader().setReorderingAllowed(false);
        gradeList.getTableHeader().setResizingAllowed(false);
        gradeList.setEnabled(false);
        gradeList.setSelectionBackground(new Color(250, 246, 212));
        gradeList.setSelectionForeground(Color.BLACK);
        
        gradeList.getColumnModel().getColumn(0).setPreferredWidth(180);
        gradeList.getColumnModel().getColumn(1).setPreferredWidth(100);
        gradeList.getColumnModel().getColumn(2).setPreferredWidth(100);
        gradeList.getColumnModel().getColumn(3).setPreferredWidth(100);
        gradeList.getColumnModel().getColumn(4).setPreferredWidth(100);
        gradeList.getColumnModel().getColumn(5).setPreferredWidth(100);
        gradeList.getColumnModel().getColumn(6).setPreferredWidth(100);
        gradeList.getColumnModel().getColumn(7).setPreferredWidth(100);
        gradeList.getColumnModel().getColumn(8).setPreferredWidth(180);
	}
	
	/**
	 * Prompts the JFileChooser where files can be selected by the user for export
	 */
	public void saveTable() {
		
		if(fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			saveTable(fileChooser.getSelectedFile());
		}
	}
	
	/**
	 * Writes the data in the JTables to a file
	 * @param file the file to be written to/exported to
	 */
	public void saveTable(File file) {
		
		try {
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
			out.writeObject(cdtm.getDataVector()); 
			out.writeObject(getColumnNamesC());
			out.writeObject(gdtm.getDataVector()); 
			out.writeObject(getColumnNamesG());
			out.writeObject(categories); 
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
			out.writeObject(checkedSettings);
			out.writeObject(defaultCreditAmount);
			out.writeObject(defaultGMode);
			out.writeObject(defaultGMethod);
			out.writeObject(termGpas); 
			out.writeObject(termCreditTotals); 
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
		
		if(fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
			loadTable(fileChooser.getSelectedFile());
		
		for(int i = 0; i < cdtm.getRowCount(); i++) {
			if(cdtm.getValueAt(i, 0).equals("Total Credits Earned") || cdtm.getValueAt(i, 0).equals("Term Credits Earned"))
				courseList.addRowSelectionInterval(i, i);
		}
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
			gradeScales = (ArrayList<Hashtable>) in.readObject();
			courseScales = (Hashtable<String, Hashtable>) in.readObject();
			checkedSettings = (boolean) in.readObject();
			defaultCreditAmount = (String) in.readObject();
			defaultGMode = (String) in.readObject();
			defaultGMethod = (String) in.readObject();
			termGpas = (ArrayList<Double>) in.readObject(); 
			termCreditTotals = (ArrayList<Double>) in.readObject(); 
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
	 * Links a course identifier to a grading scale using a hash table
	 * @param identifier the identifier of the course to be linked
	 * @param scaleName the name of the grade scale to be linked
	 */
	public void linkScale(String identifier, String scaleName) {
		for(int i = 0; i < gradeScales.size(); i++)
			if(gradeScales.get(i).get("Name").equals(scaleName)) {
				courseScales.put(identifier, gradeScales.get(i));
				return;
			}
	}
	
	/**
	 * Calculates the final grade for a specified course from the grades table and changes the Numeric Grade
	 * and Final Grade fields in the course table
	 * @param identifier the unique ID of the course
	 */
	public void calculateGrade(String identifier) {
		
		if(!gradeExists(identifier))
			for(int i = 0; i < cdtm.getRowCount(); i++)
				if(cdtm.getValueAt(i, 3).equals(identifier)) {
					cdtm.setValueAt("n/a", i, 5);
					cdtm.setValueAt("In Progress", i, 7);
					return;
				}
		
		int countGrades = 0;
		int countUngraded = 0;
		
		for(int i = 0; i < gdtm.getRowCount(); i++) {
			if(gdtm.getValueAt(i, 1).equals(identifier))
				countGrades++;
			if(gdtm.getValueAt(i, 1).equals(identifier) && gdtm.getValueAt(i, 5).equals("Ungraded"))
				countUngraded++;
		}
		
		if(countGrades == countUngraded) {
			for(int i = 0; i < cdtm.getRowCount(); i++)
				if(cdtm.getValueAt(i, 3).equals(identifier)) {
					cdtm.setValueAt("n/a", i, 5);
					cdtm.setValueAt("In Progress", i, 7);
					return;
				}
		}
		
			
		double sumCategoryWeightsUsed = 0;
		double categoryWeight = 0;
		double sumPointsEarned = 0;
		double sumTotalPoints = 0;
		double finalGrade = 0;
		
		ArrayList<String> finishedCats = new ArrayList<String>();
		String category = "";
		for(int i = 0; i < gdtm.getRowCount(); i++)
			if(gdtm.getValueAt(i,1).equals(identifier) && !finishedCats.contains(gdtm.getValueAt(i, 3)) && !gdtm.getValueAt(i, 5).equals("Ungraded")) {
				category = gdtm.getValueAt(i, 3) + "";
				sumCategoryWeightsUsed += Double.parseDouble(gdtm.getValueAt(i, 4) + "");
				finishedCats.add(category);
			}
		
		for(int i = 0; i < finishedCats.size(); i++) {
			for(int j = 0; j < gdtm.getRowCount(); j++) {
				if(gdtm.getValueAt(j, 3).equals(finishedCats.get(i)) && gdtm.getValueAt(j, 1).equals(identifier)) {
					categoryWeight = Double.parseDouble(gdtm.getValueAt(j, 4) + "");
				}
				if(gdtm.getValueAt(j,1).equals(identifier) && finishedCats.get(i).equals(gdtm.getValueAt(j, 3))) {
					if(!(gdtm.getValueAt(j, 5) + "").equals("Ungraded")) {
						sumPointsEarned += Double.parseDouble(gdtm.getValueAt(j, 5) + "");
						sumTotalPoints += Double.parseDouble(gdtm.getValueAt(j, 6) + "");
					}
				}
			}
			finalGrade += (sumPointsEarned / sumTotalPoints) * (categoryWeight / sumCategoryWeightsUsed) * 100;
			sumPointsEarned = 0;
			sumTotalPoints = 0;
		}
		
		DecimalFormat rounder = new DecimalFormat("#.####");
		rounder.setRoundingMode(RoundingMode.HALF_UP);
		
		String finalGradeString = rounder.format(finalGrade);

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
		
		if(gMode.equals("Letter")) {
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
	
	public void displayCancelMsg() {
		JOptionPane.showMessageDialog(null, "Action Cancelled", "System Notification", JOptionPane.INFORMATION_MESSAGE);
	}
	
	/**
	 * Adds a new course to the course table
	 */
	public void addCourse() {	
		
		if(cdtm.getRowCount() == 0 && !checkedSettings) {
			Errors.SS5.displayErrorMsg();
			return;
		}
		
		String credits, gMode, subject, title, comment, term;
		credits = gMode = subject = title = comment = term = "";
		JTextField subjectEntry = new JTextField();
		JTextField titleEntry = new JTextField();
		JTextField commentEntry = new JTextField();
		JTextField termEntry = new JTextField();
		
		String[] creditsChoices = {"0", "0.5", "1", "1.5", "2", "2.5", "3", "4", "5", "6"};
		JComboBox creditEntry = new JComboBox(creditsChoices);
		creditEntry.setSelectedItem(defaultCreditAmount);
		
		String[] gModeChoices = {"Letter", "P/NP", "Notation"};
		JComboBox gModeEntry = new JComboBox(gModeChoices);
		gModeEntry.setSelectedItem(defaultGMode);
		
		String[] gMethodChoices = {"Category Weightings", "Total Points"};
		JComboBox gMethodEntry = new JComboBox(gMethodChoices);
		gMethodEntry.setSelectedItem(defaultGMethod);
		
		String[] scaleChoices = new String[gradeScales.size()];
		for(int i = 0; i < scaleChoices.length; i++) {
			scaleChoices[i] = (String) gradeScales.get(i).get("Name");
		}
		JComboBox scaleEntry = new JComboBox(scaleChoices);
		
		JPanel courseInfoPanel = new JPanel();
		courseInfoPanel.setLayout(new GridLayout(8, 0));
		
		courseInfoPanel.add(new JLabel("Enter Subject/Course Number"));
		courseInfoPanel.add(subjectEntry);
		
		courseInfoPanel.add(new JLabel("Enter Course Title"));
		courseInfoPanel.add(titleEntry);
		
		courseInfoPanel.add(new JLabel("Enter Comment"));
		courseInfoPanel.add(commentEntry);
		
		courseInfoPanel.add(new JLabel("Select Number of Credits"));
		courseInfoPanel.add(creditEntry);
		
		courseInfoPanel.add(new JLabel("Select Grade Mode"));
		courseInfoPanel.add(gModeEntry);
		
		courseInfoPanel.add(new JLabel("Select Grading Method"));
		courseInfoPanel.add(gMethodEntry);
		
		courseInfoPanel.add(new JLabel("Select Grade Scale"));
		courseInfoPanel.add(scaleEntry);
		
		courseInfoPanel.add(new JLabel("Enter Term"));
		termEntry.setText(getUnfinalizedTerm());
		courseInfoPanel.add(termEntry);
		
		courseCode += (int) (Math.random() * 50 + 1);
		String identifier = "C" + courseCode;
		
		int addResult = JOptionPane.showConfirmDialog(null, courseInfoPanel, "Course Master", JOptionPane.OK_CANCEL_OPTION);
		
		if(addResult == JOptionPane.OK_OPTION) {
			credits = (String) creditEntry.getSelectedItem();
			gMode = (String) gModeEntry.getSelectedItem();
			subject = subjectEntry.getText();
			title = titleEntry.getText();
			comment = commentEntry.getText();
			term = termEntry.getText();
			linkScale(identifier, (String) scaleEntry.getSelectedItem());
			
			if((title.isEmpty() && subject.isEmpty()) || term.isEmpty()) {
				Errors.ML1.displayErrorMsg();
				return;
			}
			
			if(subject.isEmpty())
				subject = title;
			
			if(title.isEmpty())
				title = subject;
		}
		
		else {
			displayCancelMsg();
			return;
		}
		
		if(isTermFinalized(term)) {
			Errors.AER1.displayErrorMsg();
			return;
		}
		
		if(!getUnfinalizedTerm().equals(term) && existsUnfinalizedTerm()) {
			Errors.AER2.displayErrorMsg();
			return;
		}
		
		honorsAPStatuses.put(identifier, "College Prep");
		
		String numGrade = "n/a";
		
		String[] fGradeChoicesC = {"In Progress", "A","A-","B+","B","B-","C+","C","C-","D+","D","D-","F"};
		String[] fGradeChoicesCPlus = {"In Progress", "A+", "A","A-","B+","B","B-","C+","C","C-","D+","D","D-","F"};
		String[] fGradeChoicesP = {"In Progress", "P", "NP"};
		String[] notationChoices = {"TR","I","W","Z"};
		String fGrade;
		if(gMode.equals("Letter")) {
			if(isAPluses)
				fGrade = (String) JOptionPane.showInputDialog(null, "Select Final Grade", "Course Master", 
						JOptionPane.QUESTION_MESSAGE, null, fGradeChoicesCPlus, fGradeChoicesCPlus[0]);
			else
				fGrade = (String) JOptionPane.showInputDialog(null, "Select Final Grade", "Course Master", 
						JOptionPane.QUESTION_MESSAGE, null, fGradeChoicesC, fGradeChoicesC[0]);
		}
		else if(gMode.equals("P/NP"))
			fGrade = (String) JOptionPane.showInputDialog(null, "Select Final Grade", "Course Master", 
					JOptionPane.QUESTION_MESSAGE, null, fGradeChoicesP, fGradeChoicesP[0]);
		else
			fGrade = (String) JOptionPane.showInputDialog(null, "Select Final Grade", "Course Master", 
					JOptionPane.QUESTION_MESSAGE, null, notationChoices, notationChoices[0]);
		if(fGrade == null) {
			displayCancelMsg();
			return;
		}
		
		if(!gMode.equals("Notation") && fGrade.equals("In Progress")) {

			ArrayList<String> catsAndWeights = new ArrayList<String>();
			
			if(gMethodEntry.getSelectedItem().equals("Total Points")) {
				catsAndWeights.add("Total Points");
				catsAndWeights.add("100");
			}
			
			else {	
				ArrayList<JTextField> catNameEntries = new ArrayList<JTextField>();
				ArrayList<JTextField> catWeightEntries = new ArrayList<JTextField>();
				JPanel categoryPanel = new JPanel();
				categoryPanel.setLayout(new GridLayout(16, 2));
				
				categoryPanel.add(new JLabel("Category Names"));
				categoryPanel.add(new JLabel("Category Weights"));
				
				for(int i = 0; i < 15; i++) {
					catNameEntries.add(new JTextField(15));
					catWeightEntries.add(new JTextField(15));
					categoryPanel.add(catNameEntries.get(i));
					categoryPanel.add(catWeightEntries.get(i));
				}
				
				int categoryResult = JOptionPane.showConfirmDialog(null, categoryPanel, 
						"Course Master", JOptionPane.OK_CANCEL_OPTION);
				
				if(categoryResult == JOptionPane.OK_OPTION) {
					
					for(int i = 0; i < catNameEntries.size(); i++) {
						if(!catNameEntries.get(i).getText().isEmpty() && isNumbers(catNameEntries.get(i).getText())) {
							Errors.AER3.displayErrorMsg();
							return;
						}
						if(!catNameEntries.get(i).getText().isEmpty()) {
							try {
								double testError = Double.parseDouble(catWeightEntries.get(i).getText());
							} catch (NumberFormatException e) {
								Errors.ML2.displayErrorMsg();
								return;
							}
							catsAndWeights.add(catNameEntries.get(i).getText());
							catsAndWeights.add(catWeightEntries.get(i).getText());
						}
					}
					
					if(catsAndWeights.size() == 0) {
						Errors.ML1.displayErrorMsg();
						return;
					}
				}
				else {
					displayCancelMsg();
					return;
				}
			}
			categories.put(identifier, catsAndWeights);
		}
		
		String status;
		if(fGrade.equals("In Progress")) 
			status = "In Progress";
		else status = "Manual Entry";

		cdtm.addRow(new Object[] {subject, title, comment, identifier, credits, numGrade, gMode, fGrade, term, status});
		
		JOptionPane.showMessageDialog(null, "Successfully Updated", "System Notification", JOptionPane.INFORMATION_MESSAGE);
	}
	
	/**
	 * Adds a new course to the course table with honors/AP distinction
	 */
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
		creditEntry.setSelectedItem(defaultCreditAmount);
		
		String[] gMethodChoices = {"Category Weightings", "Total Points"};
		JComboBox gMethodEntry = new JComboBox(gMethodChoices);
		gMethodEntry.setSelectedItem(defaultGMethod);
		
		String[] honorsAPChoices = {"College Prep", "Honors", "Advanced Placement"};
		JComboBox courseTypeEntry = new JComboBox(honorsAPChoices);
		
		String[] gModeChoices = {"Letter", "P/NP", "Notation"};
		JComboBox gModeEntry = new JComboBox(gModeChoices);
		gModeEntry.setSelectedItem(defaultGMode);
		
		JPanel courseInfoPanel = new JPanel();
		courseInfoPanel.setLayout(new GridLayout(8, 0));
		
		courseInfoPanel.add(new JLabel("Enter Subject/Course Number"));
		courseInfoPanel.add(subjectEntry);
		
		courseInfoPanel.add(new JLabel("Enter Course Title"));
		courseInfoPanel.add(titleEntry);
		
		courseInfoPanel.add(new JLabel("Enter Course Type"));
		courseInfoPanel.add(courseTypeEntry);
		
		courseInfoPanel.add(new JLabel("Enter Comment"));
		courseInfoPanel.add(commentEntry);
		
		courseInfoPanel.add(new JLabel("Select Number of Credits"));
		courseInfoPanel.add(creditEntry);
		
		courseInfoPanel.add(new JLabel("Select Grade Mode"));
		courseInfoPanel.add(gModeEntry);
		
		courseInfoPanel.add(new JLabel("Select Grading Method"));
		courseInfoPanel.add(gMethodEntry);
		
		String[] scaleChoices = new String[gradeScales.size()];
		for(int i = 0; i < scaleChoices.length; i++) {
			scaleChoices[i] = (String) gradeScales.get(i).get("Name");
		}
		JComboBox scaleEntry = new JComboBox(scaleChoices);
		courseInfoPanel.add(new JLabel("Select Grade Scale"));
		courseInfoPanel.add(scaleEntry);
		
		courseInfoPanel.add(new JLabel("Enter Term"));
		termEntry.setText(getUnfinalizedTerm());
		courseInfoPanel.add(termEntry);
		
		int courseInfoResult = JOptionPane.showConfirmDialog(null, courseInfoPanel, "Course Master", JOptionPane.OK_CANCEL_OPTION);
		
		if(courseInfoResult == JOptionPane.OK_OPTION) {
			credits = (String) creditEntry.getSelectedItem();
			gMode = (String) gModeEntry.getSelectedItem();
			subject = subjectEntry.getText();
			title = titleEntry.getText();
			comment = commentEntry.getText();
			term = termEntry.getText();
			if(courseTypeEntry.getSelectedItem().equals("Honors"))
				identifier += "H";
			else if(courseTypeEntry.getSelectedItem().equals("Advanced Placement"))
				identifier += "AP";
			honorsAPStatuses.put(identifier, courseTypeEntry.getSelectedItem() + "");
			
			linkScale(identifier, (String) scaleEntry.getSelectedItem());
			
			if((title.isEmpty() && subject.isEmpty()) || term.isEmpty()) {
				Errors.ML1.displayErrorMsg();
				return;
			}
			
			if(subject.isEmpty())
				subject = title;
			
			if(title.isEmpty())
				title = subject;
		}
		
		else {
			displayCancelMsg();
			return;
		}
		
		if(isTermFinalized(term)) {
			Errors.AER1.displayErrorMsg();
			return;
		}
		
		if(!getUnfinalizedTerm().equals(term) && existsUnfinalizedTerm()) {
			Errors.AER2.displayErrorMsg();
			return;
		}
		
		String numGrade = "n/a";
		
		String[] fGradeChoicesC = {"In Progress", "A","A-","B+","B","B-","C+","C","C-","D+","D","D-","F"};
		String[] fGradeChoicesCPlus = {"In Progress", "A+", "A","A-","B+","B","B-","C+","C","C-","D+","D","D-","F"};
		String[] fGradeChoicesP = {"In Progress", "P", "NP"};
		String[] notationChoices = {"TR","I","W","Z"};
		String fGrade;
		if(gMode.equals("Letter")) {
			if(isAPluses)
				fGrade = (String) JOptionPane.showInputDialog(null, "Select Final Grade", "Course Master", 
						JOptionPane.QUESTION_MESSAGE, null, fGradeChoicesCPlus, fGradeChoicesCPlus[0]);
			else
				fGrade = (String) JOptionPane.showInputDialog(null, "Select Final Grade", "Course Master", 
						JOptionPane.QUESTION_MESSAGE, null, fGradeChoicesC, fGradeChoicesC[0]);
		}
		else if(gMode.equals("P/NP"))
			fGrade = (String) JOptionPane.showInputDialog(null, "Select Final Grade", "Course Master", 
					JOptionPane.QUESTION_MESSAGE, null, fGradeChoicesP, fGradeChoicesP[0]);
		else
			fGrade = (String) JOptionPane.showInputDialog(null, "Select Final Grade", "Course Master", 
					JOptionPane.QUESTION_MESSAGE, null, notationChoices, notationChoices[0]);
		if(fGrade == null) {
			displayCancelMsg();
			return;
		}
		
		if(!gMode.equals("Notation") && fGrade.equals("In Progress")) {

			ArrayList<String> catsAndWeights = new ArrayList<String>();
			
			if(gMethodEntry.getSelectedItem().equals("Total Points")) {
				catsAndWeights.add("Total Points");
				catsAndWeights.add("100");
			}
			
			else {
				ArrayList<JTextField> catNameEntries = new ArrayList<JTextField>();
				ArrayList<JTextField> catWeightEntries = new ArrayList<JTextField>();
				JPanel categoryPanel = new JPanel();
				categoryPanel.setLayout(new GridLayout(16, 2));
				
				categoryPanel.add(new JLabel("Category Names"));
				categoryPanel.add(new JLabel("Category Weights"));
				
				for(int i = 0; i < 15; i++) {
					catNameEntries.add(new JTextField(15));
					catWeightEntries.add(new JTextField(15));
					categoryPanel.add(catNameEntries.get(i));
					categoryPanel.add(catWeightEntries.get(i));
				}
				
				int categoryResult = JOptionPane.showConfirmDialog(null, categoryPanel, 
						"Course Master", JOptionPane.OK_CANCEL_OPTION);
				
				if(categoryResult == JOptionPane.OK_OPTION) {
					
					for(int i = 0; i < catNameEntries.size(); i++) {
						if(!catNameEntries.get(i).getText().isEmpty() && isNumbers(catNameEntries.get(i).getText())) {
							Errors.AER3.displayErrorMsg();
							return;
						}
						if(!catNameEntries.get(i).getText().isEmpty()) {
							try {
								double testError = Double.parseDouble(catWeightEntries.get(i).getText());
							} catch (NumberFormatException e) {
								Errors.ML2.displayErrorMsg();
								return;
							}
							catsAndWeights.add(catNameEntries.get(i).getText());
							catsAndWeights.add(catWeightEntries.get(i).getText());
						}
					}
					
					if(catsAndWeights.size() == 0) {
						Errors.ML1.displayErrorMsg();
						return;
					}
				}
				else {
					displayCancelMsg();
					return;
				}
			}
			categories.put(identifier, catsAndWeights);
		}
		
		String status;
		if(fGrade.equals("In Progress")) 
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
			Errors.AER7.displayErrorMsg();
			return;
		}
		
		for(int i = 0; i < cdtm.getRowCount(); i++) {
			if(cdtm.getValueAt(i, 3).equals(identifierInput.getText()) && cdtm.getValueAt(i, 9).equals("Finalized")) {
				Errors.AER8.displayErrorMsg();
				return;
			}
		}
		
		for(int i = 0; i < cdtm.getRowCount(); i++) {
			if(cdtm.getValueAt(i, 3).equals(identifierInput.getText())) {
				String courseName = (String) cdtm.getValueAt(i, 0);
				if(JOptionPane.showConfirmDialog(null, "Are you sure you want to delete [" + 
						courseName + "]? \nThis action cannot be reversed.", "Course Master", JOptionPane.YES_NO_CANCEL_OPTION) == 0) {
					removeAssociatedGrades(identifierInput.getText());
					cdtm.removeRow(i);
					JOptionPane.showMessageDialog(null, "Successfully Updated", "System Notification", JOptionPane.INFORMATION_MESSAGE);
				}
				else {
					displayCancelMsg();
					return;
				}
			}
		}
		
		for(int i = 0; i < gdtm.getRowCount(); i++) {
			if(gdtm.getValueAt(i, 2).equals(identifierInput.getText())) {
				String assignmentCode = (String) gdtm.getValueAt(i, 2);
				if(JOptionPane.showConfirmDialog(null, "Are you sure you want to delete assignment [" + 
						assignmentCode + "]? \nThis action cannot be reversed.", "Course Master", JOptionPane.YES_NO_CANCEL_OPTION) == 0) {
					String id = gdtm.getValueAt(i, 1) + "";
					gdtm.removeRow(i);
					calculateGrade(id);
					JOptionPane.showMessageDialog(null, "Successfully Updated", "System Notification", JOptionPane.INFORMATION_MESSAGE);
				}
				else {
					displayCancelMsg();
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
		
		String term = getUnfinalizedTerm();
		
		double creditSum = 0;
		double qualitySum = 0;
		double nonGpaSum = 0;
		double failCreditSum = 0;
		
		boolean allNotation = true;
		
		for(int i = 0; i < cdtm.getRowCount(); i++) {
			if(cdtm.getValueAt(i, 8).equals(term + "") && cdtm.getValueAt(i, 6).equals("Letter") && !cdtm.getValueAt(i, 7).equals("F")) {
				qualitySum += Double.parseDouble((String)cdtm.getValueAt(i, 4)) * letToQual(cdtm.getValueAt(i, 3)+"",(String)cdtm.getValueAt(i, 7));
				creditSum += Double.parseDouble((String)cdtm.getValueAt(i, 4));
				cdtm.setValueAt("Finalized", i, 9);
				if(Integer.parseInt(cdtm.getValueAt(i, 4) + "") != 0) {
					allNotation = false;
				}
			}
			
			else if(cdtm.getValueAt(i, 8).equals(term + "") && cdtm.getValueAt(i, 6).equals("Letter") && cdtm.getValueAt(i, 7).equals("NP")) {
				cdtm.setValueAt("Finalized", i, 9);
				if(Integer.parseInt(cdtm.getValueAt(i, 4) + "") != 0) {
					allNotation = false;
				}
			}
			
			else if(cdtm.getValueAt(i, 8).equals(term + "") && cdtm.getValueAt(i, 6).equals("Letter") && cdtm.getValueAt(i, 7).equals("F")) {
				qualitySum += Double.parseDouble((String)cdtm.getValueAt(i, 4)) * letToQual(cdtm.getValueAt(i, 3)+"",(String)cdtm.getValueAt(i, 7));
				failCreditSum += Double.parseDouble((String)cdtm.getValueAt(i, 4));
				totalFCreditSum += Double.parseDouble((String)cdtm.getValueAt(i, 4));
				cdtm.setValueAt("Finalized", i, 9);
				if(Integer.parseInt(cdtm.getValueAt(i, 4) + "") != 0) {
					allNotation = false;
				}
			}
			
			else if(cdtm.getValueAt(i, 8).equals(term + "") && cdtm.getValueAt(i, 6).equals("P/NP") && cdtm.getValueAt(i, 7).equals("P")) {
				creditSum += Double.parseDouble((String)cdtm.getValueAt(i, 4));
				nonGpaSum += Double.parseDouble((String)cdtm.getValueAt(i, 4));
				nonGpaCreditTotal += Double.parseDouble((String)cdtm.getValueAt(i, 4));
				cdtm.setValueAt("Finalized", i, 9);
				if(Integer.parseInt(cdtm.getValueAt(i, 4) + "") != 0) {
					allNotation = false;
				}
			}
			
			else if(cdtm.getValueAt(i, 8).equals(term + "") && cdtm.getValueAt(i, 6).equals("Notation") && cdtm.getValueAt(i, 7).equals("TR")) {
				creditSum += Double.parseDouble((String)cdtm.getValueAt(i, 4));
				nonGpaSum += Double.parseDouble((String)cdtm.getValueAt(i, 4));
				nonGpaCreditTotal += Double.parseDouble((String)cdtm.getValueAt(i, 4));
				cdtm.setValueAt("Finalized", i, 9);
			}
			
			else if(cdtm.getValueAt(i, 6).equals("Notation")) {
				cdtm.setValueAt("Finalized", i, 9);
			}
		}
		
		if(((String) cdtm.getValueAt(cdtm.getRowCount() - 1, 9)).equals("Finalized")) {
			
				DecimalFormat rounder = new DecimalFormat("#.####");
				rounder.setRoundingMode(RoundingMode.HALF_UP);
			
				cdtm.addRow(new Object[] {"", "", "", "", "", "", "", "", "", ""});
				
				String gpa = rounder.format(qualitySum / (creditSum + failCreditSum - nonGpaSum)) + "";
				
				if(!gpa.contains("."))
					gpa = gpa + ".0";
				
				String creditSumString = rounder.format(creditSum) + "";
				
				String qualitySumString = rounder.format(qualitySum) + "";
				
				double highestCreditsRow = 0;
				double highestCredits = 0;
				boolean everFinalized = false;
				
				for(int i = 0; i < cdtm.getRowCount(); i++) {
					if(cdtm.getValueAt(i, 0).equals("Total Credits Earned"))
						everFinalized = true;
				}
				
				for(int i = 0; i < cdtm.getRowCount(); i++) {
					if(cdtm.getValueAt(i, 0).equals("Total Credits Earned") && Double.parseDouble(cdtm.getValueAt(i, 1) + "") > highestCredits) {
						highestCreditsRow = i;
						highestCredits = Double.parseDouble(cdtm.getValueAt(i, 1) + "");
					}
				}
				
				if(!everFinalized && (highestCreditsRow == 0 || highestCredits == 0)) {
					cdtm.addRow(new Object[] {"Term Credits Earned", creditSumString, "Term Quality Points", 
							qualitySumString, "", "", "", "", "Term GPA", gpa});
					
					cdtm.addRow(new Object[] {"Total Credits Earned", creditSumString, "Total Quality Points", 
							qualitySumString, "", "", "", "", "Cumulative GPA", gpa});
					
					for(int i = 0; i < cdtm.getRowCount(); i++) {
						if(cdtm.getValueAt(i, 0).equals("Total Credits Earned") || cdtm.getValueAt(i, 0).equals("Term Credits Earned"))
							courseList.addRowSelectionInterval(i, i);
					}
					
					cdtm.addRow(new Object[] {"", "", "", "", "", "", "", "", "", ""});
					
					return;
				}

				if(creditSum == 0) {
					cdtm.addRow(new Object[] {"Term Credits Earned", creditSumString, "Term Quality Points", 
							qualitySumString, "", "", "", "", "Term GPA", "n/a"});
					
					cdtm.addRow(new Object[] {"Total Credits Earned", cdtm.getValueAt((int)highestCreditsRow, 1), "Total Quality Points", 
							cdtm.getValueAt((int)highestCreditsRow, 3), "", "", "", "", "Cumulative GPA", cdtm.getValueAt((int)highestCreditsRow, 9)});
					
					for(int i = 0; i < cdtm.getRowCount(); i++) {
						if(cdtm.getValueAt(i, 0).equals("Total Credits Earned") || cdtm.getValueAt(i, 0).equals("Term Credits Earned"))
							courseList.addRowSelectionInterval(i, i);
					}
					
					cdtm.addRow(new Object[] {"", "", "", "", "", "", "", "", "", ""});
					
					return;
				}
				
				if(!allNotation) {
					termGpas.add(Double.parseDouble(gpa));
					termCreditTotals.add(creditSum + failCreditSum - nonGpaSum);
				

					cdtm.addRow(new Object[] {"Term Credits Earned", creditSumString, "Term Quality Points", 
							qualitySumString, "", "", "", "", "Term GPA", gpa});
				}
				
				else { 
					cdtm.addRow(new Object[] {"Term Credits Earned", creditSumString, "Term Quality Points", 
							qualitySumString, "", "", "", "", "Term GPA", "n/a"});
				}
				
				double allQualitySum = 0;
				double allCreditSum = 0;
				for(int i = 0; i < cdtm.getRowCount(); i++)
					if(((String) cdtm.getValueAt(i, 0)).equals("Term Credits Earned") && ((String) cdtm.getValueAt(i, 4)).equals("")) {
						allQualitySum += Double.parseDouble(cdtm.getValueAt(i, 3) + "");
						allCreditSum += Double.parseDouble(cdtm.getValueAt(i, 1) + "");
					}
				
				String allCreditSumString = rounder.format(allCreditSum) + "";
				
				String allQualitySumString = rounder.format(allQualitySum) + "";
				
				String totalGpa = rounder.format(allQualitySum / (allCreditSum + totalFCreditSum - nonGpaCreditTotal)) + "";
				
				if(!totalGpa.contains("."))
					totalGpa = totalGpa + ".0";
				
				if(!allNotation) {
					cdtm.addRow(new Object[] {"Total Credits Earned", allCreditSumString, "Total Quality Points", 
							allQualitySumString, "", "", "", "", "Cumulative GPA", totalGpa});
				}
				
				else {
					if(termGpas.size() > 1)
						cdtm.addRow(new Object[] {"Total Credits Earned", allCreditSumString, "Total Quality Points", 
								allQualitySumString, "", "", "", "", "Cumulative GPA", totalGpa});
					else
						cdtm.addRow(new Object[] {"Total Credits Earned", allCreditSumString, "Total Quality Points", 
								allQualitySumString, "", "", "", "", "Cumulative GPA", "n/a"});
				}
				
				for(int i = 0; i < cdtm.getRowCount(); i++) {
					if(cdtm.getValueAt(i, 0).equals("Total Credits Earned") || cdtm.getValueAt(i, 0).equals("Term Credits Earned"))
						courseList.addRowSelectionInterval(i, i);
				}
				
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
			Errors.AER7.displayErrorMsg();
			return;
		}
		
		if(identifierInput.getText().contains("C")) {
		
			for(int i = 0; i < cdtm.getRowCount(); i++) {
				if(cdtm.getValueAt(i, 3).equals(identifierInput.getText()) && cdtm.getValueAt(i, 9).equals("Finalized")) {
					Errors.AER9.displayErrorMsg();
					return;
				}
			}
			
			int row = 0;
			for(int i = 0; i < cdtm.getRowCount(); i++) {
				if(cdtm.getValueAt(i, 3).equals(identifierInput.getText())) {
					row = i;
				}
			}
			
			String[] editChoices = {"Subject/Course Number", "Course Title", "Comment", "Credits", "Category Weightings", 
					"Grade Scale", "Grade Mode", "Final Grade"};
			String[] editChoicesManualEntry = {"Subject/Course Number", "Course Title", "Comment", "Credits", "Final Grade"};
			
			String edit = "";
			
			for(int i = 0; i < cdtm.getRowCount(); i++)
				if(cdtm.getValueAt(i, 3).equals(identifierInput.getText()) && cdtm.getValueAt(i, 9).equals("Manual Entry")) {
					edit = (String) JOptionPane.showInputDialog(null, "Select Field for Edit", "Course Master", 
							JOptionPane.QUESTION_MESSAGE, null, editChoicesManualEntry, editChoicesManualEntry[0]);
					if(edit == null) {
						displayCancelMsg();
						return;
					}
				}
			
			if(edit.equals(""))
				edit = (String) JOptionPane.showInputDialog(null, "Select Field for Edit", "Course Master", 
						JOptionPane.QUESTION_MESSAGE, null, editChoices, editChoices[0]);
			
			if(edit == null) {
				JOptionPane.showMessageDialog(null, "Action Cancelled", "System Notification", JOptionPane.INFORMATION_MESSAGE);
				return;
			}
			
			if(edit.equals("Subject/Course Number")) {
				String course = JOptionPane.showInputDialog(null, "Enter Subject/Course Number", "Course Master", JOptionPane.INFORMATION_MESSAGE);
				if(course == null) {
					displayCancelMsg();
					return;
				}
				else cdtm.setValueAt(course, row, 0);
				JOptionPane.showMessageDialog(null, "Successfully Updated", "System Notification", JOptionPane.INFORMATION_MESSAGE);
			}
			
			if(edit.equals("Category Weightings")) {
				ArrayList<String> courseWeightings = categories.get(identifierInput.getText());
				
				String[] catEditChoices = {"Add Category", "Remove Category", "Change Weighting"};
				String selection = (String) JOptionPane.showInputDialog(null, "Select Action to Perform", "Course Master", 
						JOptionPane.QUESTION_MESSAGE, null, catEditChoices, catEditChoices[0]);
				
				if(selection == null) {
					displayCancelMsg();
					return;
				}
				
				if(selection.equals("Add Category")) {
					JPanel categoryPanel2 = new JPanel(); // For additional category entries
					categoryPanel2.setLayout(new GridLayout(2, 0));
					JTextField catNameEntry2 = new JTextField(15);
					JTextField catWeightEntry2 = new JTextField(15);
					catNameEntry2.setText("");
					catWeightEntry2.setText("");
					categoryPanel2.add(new JLabel("Enter Category Name"));
					categoryPanel2.add(catNameEntry2);
					categoryPanel2.add(new JLabel("Enter Category Weight"));
					categoryPanel2.add(catWeightEntry2);
					
					int additionalCategoryResult = JOptionPane.showConfirmDialog(null, categoryPanel2, 
							"Course Master", JOptionPane.OK_CANCEL_OPTION);
					
					if(additionalCategoryResult == JOptionPane.OK_OPTION) {
						if(catNameEntry2.getText().isEmpty() || catWeightEntry2.getText().isEmpty()) {
							Errors.ML1.displayErrorMsg();
							return;
						}
					
						if(isNumbers(catNameEntry2.getText())) {
							Errors.AER3.displayErrorMsg();
							return;
						}
						try {
							double testError = Double.parseDouble(catWeightEntry2.getText());
						} catch (NumberFormatException e) {
							Errors.ML2.displayErrorMsg();
							return;
						}
						courseWeightings.add(catNameEntry2.getText());
						courseWeightings.add(catWeightEntry2.getText());
						
						JOptionPane.showMessageDialog(null, "Successfully Updated", "System Notification", JOptionPane.INFORMATION_MESSAGE);
					}
					else {
						displayCancelMsg();
						return;
					}
				}
				
				else if(selection.equals("Remove Category")) {
					ArrayList<String> categoryNames = getCategoryNames(courseWeightings);
					String[] nameChoices = new String[categoryNames.size()];
					for(int i = 0; i < nameChoices.length; i++)
						nameChoices[i] = categoryNames.get(i);
					
					if(categoryNames.size() == 0) {
						Errors.AER10.displayErrorMsg();
						return;
					}
					
					String nameSelection = (String) JOptionPane.showInputDialog(null, "Select Category to Remove", "Course Master", 
							JOptionPane.QUESTION_MESSAGE, null, nameChoices, nameChoices[0]);
					
					if(nameSelection == null) {
						displayCancelMsg();
						return;
					}
					
					if(JOptionPane.showConfirmDialog(null, "Are you sure you wish to delete the category [" + 
							nameSelection + "]?\nThis action cannot be reversed.", "Course Master", JOptionPane.YES_NO_CANCEL_OPTION) != 0) {
						displayCancelMsg();
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
					
					if(categoryNames.size() == 0) {
						Errors.AER10.displayErrorMsg();
						return;
					}
					
					String nameSelection = (String) JOptionPane.showInputDialog(null, "Select Category to Change Weight", "Course Master", 
							JOptionPane.QUESTION_MESSAGE, null, nameChoices, nameChoices[0]);
					
					if(nameSelection == null) {
						displayCancelMsg();
						return;
					}
					
					String newWeight = JOptionPane.showInputDialog(null, "Enter New Weight", "Course Master", JOptionPane.INFORMATION_MESSAGE);
					
					try {
						double testError = Double.parseDouble(newWeight);
					} catch (NumberFormatException e) {
						Errors.ML2.displayErrorMsg();
						return;
					}
					
					if(Double.parseDouble(newWeight) < 0) {
						Errors.AER5.displayErrorMsg();
						return;
					}
					
					if(newWeight == null) {
						displayCancelMsg();
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
			
			if(edit.equals("Course Title")) {
				String prof = JOptionPane.showInputDialog(null, "Enter New Course Title", "Course Master", JOptionPane.INFORMATION_MESSAGE);
				if(prof == null) {
					displayCancelMsg();
					return;
				}
				else cdtm.setValueAt(prof, row, 1);
				JOptionPane.showMessageDialog(null, "Successfully Updated", "System Notification", JOptionPane.INFORMATION_MESSAGE);
			}
			
			if(edit.equals("Grade Scale")) {
				String[] choices = new String[gradeScales.size()];
				for(int i = 0; i < gradeScales.size(); i++)
					choices[i] = (String) gradeScales.get(i).get("Name");
				
				int index = 0;
				for(int i = 0; i < choices.length; i++)
					if(choices[i].equals(courseScales.get(identifierInput.getText()).get("Name")))
						index = i;
				
				String scale = (String) JOptionPane.showInputDialog(null, "Select New Grading Scale", "Course Master", 
						JOptionPane.QUESTION_MESSAGE, null, choices, choices[index]);
				
				if(scale == null) {
					displayCancelMsg();
					return;
				}
				
				courseScales.remove(identifierInput.getText());
				
				linkScale(identifierInput.getText(), scale);
				
				calculateGrade(identifierInput.getText());
				
				JOptionPane.showMessageDialog(null, "Successfully Updated", "System Notification", JOptionPane.INFORMATION_MESSAGE);
			}
			
			if(edit.equals("Comment")) {
				String dayTime = JOptionPane.showInputDialog(null, "Enter New Comment", "Course Master", JOptionPane.INFORMATION_MESSAGE);
				if(dayTime == null) {
					displayCancelMsg();
					return;
				}
				else cdtm.setValueAt(dayTime, row, 2);
				JOptionPane.showMessageDialog(null, "Successfully Updated", "System Notification", JOptionPane.INFORMATION_MESSAGE);
			}
			
			if(edit.equals("Credits")) {
				String[] creditsChoices = {"0", "0.5", "1", "1.5", "2", "2.5", "3", "4", "5", "6"};
				String credits = (String) JOptionPane.showInputDialog(null, "Select Number of Credits", "Course Edit Master", 
						JOptionPane.QUESTION_MESSAGE, null, creditsChoices, creditsChoices[6]);
				if(credits == null) {
					displayCancelMsg();
					return;
				}
				else cdtm.setValueAt(credits, row, 4);
				JOptionPane.showMessageDialog(null, "Successfully Updated", "System Notification", JOptionPane.INFORMATION_MESSAGE);
			}
			
			if(edit.equals("Grade Mode")) {
				String[] gModeChoices = {"Letter", "P/NP", "Notation"};
				String gMode = (String) JOptionPane.showInputDialog(null, "Select Grade Mode", "Course Edit Master", 
						JOptionPane.QUESTION_MESSAGE, null, gModeChoices, gModeChoices[0]);
				if(gMode == null) {
					displayCancelMsg();
					return;
				}
				
				String[] fGradeChoicesC = {"In Progress", "A","A-","B+","B","B-","C+","C","C-","D+","D","D-","F"};
				String[] fGradeChoicesP = {"In Progress", "P", "NP"};
				String[] notationChoices = {"TR","I","W","Z"};
				String fGrade = "";
				String identifier = (String) cdtm.getValueAt(row, 3);
				
				if(gMode.equals("Letter")) {
					cdtm.setValueAt(gMode, row, 6);
					if(!cdtm.getValueAt(row, 7).equals("In Progress"))
						cdtm.setValueAt(numToLet(identifier, Double.parseDouble(cdtm.getValueAt(row, 5) + ""), gMode), row, 7);
					JOptionPane.showMessageDialog(null, "Successfully Updated", "System Notification", JOptionPane.INFORMATION_MESSAGE);
					return;
				}
				else if(gMode.equals("P/NP")) {
					cdtm.setValueAt(gMode, row, 6);
					if(!cdtm.getValueAt(row, 7).equals("In Progress"))
						cdtm.setValueAt(numToLet(identifier, Double.parseDouble(cdtm.getValueAt(row, 5) + ""), gMode), row, 7);
					JOptionPane.showMessageDialog(null, "Successfully Updated", "System Notification", JOptionPane.INFORMATION_MESSAGE);
					return;
				}
				else if(gMode.equals("Notation")) {
					if(JOptionPane.showConfirmDialog(null, "Are you sure you wish\nto change Grade Mode to Notation?\n"
							+ "Note: Grade Mode Notation cannot be changed back.", "Course Master", JOptionPane.YES_NO_CANCEL_OPTION) == 0) {
						fGrade = (String) JOptionPane.showInputDialog(null, "Select Notation", "Course Master", 
								JOptionPane.QUESTION_MESSAGE, null, notationChoices, notationChoices[0]);
						if(fGrade == null) {
							displayCancelMsg();
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
						displayCancelMsg();
						return;
					}
				}
			}
			
			if(edit.equals("Final Grade")) {
				String[] fGradeChoicesC = {"A","A-","B+","B","B-","C+","C","C-","D+","D","D-","F"};
				String[] fGradeChoicesCPlus = {"A+","A","A-","B+","B","B-","C+","C","C-","D+","D","D-","F"};
				String[] fGradeChoicesP = {"P", "NP"};
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
					fGrade = (String) JOptionPane.showInputDialog(null, "Select Final Grade", "Course Master", 
							JOptionPane.QUESTION_MESSAGE, null, fGradeChoicesP, fGradeChoicesP[0]);
				else 
					fGrade = (String) JOptionPane.showInputDialog(null, "Select Final Grade", "Course Master", 
							JOptionPane.QUESTION_MESSAGE, null, notationChoices, notationChoices[0]);
				if(fGrade == null) {
					displayCancelMsg();
					return;
				}
				else cdtm.setValueAt(fGrade, row, 7);
				JOptionPane.showMessageDialog(null, "Successfully Updated", "System Notification", JOptionPane.INFORMATION_MESSAGE);
			}
			
			if(edit.equals("Term")) {
				String term = JOptionPane.showInputDialog(null, "Enter Term", "Course Master", JOptionPane.INFORMATION_MESSAGE);
				try {
					double testError = Double.parseDouble(term);
				} catch (NumberFormatException e) {
					Errors.ML2.displayErrorMsg();
					return;
				}
				if(term == null) {
					displayCancelMsg();
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
			
			String[] catEditChoices1 = {"Mark Ungraded", "Regrade", "Change Category", "Edit Comment"};
			
			String[] catEditChoices2 = {"Grade Assignment", "Change Point Value", "Change Category", "Edit Comment"};
			
			String catEditChoice;
			if(isAssignmentGraded(identifierInput.getText())) {
				catEditChoice = (String) JOptionPane.showInputDialog(null, "Select Action to Perform", "Assignment Master", 
					JOptionPane.QUESTION_MESSAGE, null, catEditChoices1, catEditChoices1[0]);
			}
			else {
				catEditChoice = (String) JOptionPane.showInputDialog(null, "Select Action to Perform", "Assignment Master", 
						JOptionPane.QUESTION_MESSAGE, null, catEditChoices2, catEditChoices2[0]);
			}
			if(catEditChoice == null) {
				displayCancelMsg();
				return;
			}
					
			if(catEditChoice.equals("Change Category")) {
				ArrayList<String> names = getCategoryNames(categories.get(id));
				String[] categoryNames = new String[names.size()];
				for(int i = 0; i < categoryNames.length; i++)
					categoryNames[i] = names.get(i);
						
				String category = (String) JOptionPane.showInputDialog(null, "Select New Category", "Assignment Master", 
						JOptionPane.QUESTION_MESSAGE, null, categoryNames, categoryNames[0]);
				
				gdtm.setValueAt(category, row, 3);
				gdtm.setValueAt(categories.get(id).get(categories.get(id).indexOf(category) + 1), row, 4);
				
				calculateGrade(id);
				JOptionPane.showMessageDialog(null, "Successfully Updated", "System Notification", JOptionPane.INFORMATION_MESSAGE);
			}
			
			else if(catEditChoice.equals("Change Point Value")) {
				String totalPoints = JOptionPane.showInputDialog(null, "Enter New Point Value", "Assignment Master", JOptionPane.INFORMATION_MESSAGE);
				try {
					double test = Double.parseDouble(totalPoints);
				} catch (NumberFormatException e) {
					Errors.ML2.displayErrorMsg();
					return;
				}
				DecimalFormat rounder = new DecimalFormat("#.####");
				rounder.setRoundingMode(RoundingMode.HALF_UP);
				gdtm.setValueAt(rounder.format(Double.parseDouble(totalPoints)), row, 6);
			}
			
			else if(catEditChoice.equals("Mark Ungraded")) {
				if(JOptionPane.showConfirmDialog(null, "Are you sure you want to mark [" + identifierInput.getText() + "] "
						+ "Ungraded?\n","Assignment Master", JOptionPane.YES_NO_CANCEL_OPTION) == 0) {
					gdtm.setValueAt("Ungraded", row, 5);
					gdtm.setValueAt("Ungraded", row, 7);
					
					calculateGrade(id);
				}
				else {
					displayCancelMsg();
					return;
				}
			}
			
			else if(catEditChoice.equals("Regrade") || catEditChoice.equals("Grade Assignment")) {
				
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
				
				int result = JOptionPane.showConfirmDialog(null, p, "Assignment Master", JOptionPane.OK_CANCEL_OPTION);
				
				if(result == JOptionPane.OK_OPTION) {
					pointsEarned = pointsEarnedEntry.getText();
					totalPoints = totalPointsEntry.getText();
					
					if(pointsEarned.isEmpty() || totalPoints.isEmpty()) {
						Errors.ML1.displayErrorMsg();
						return;
					}
				}
				else {
					displayCancelMsg();
					return;
				}
				
				String grade;
				
				try {
					grade = (Double.parseDouble(pointsEarned) / Double.parseDouble(totalPoints) * 100 + "");
				} catch (NumberFormatException e) {
					Errors.ML2.displayErrorMsg();
					return;
				}
				
				if(Double.parseDouble(pointsEarned) < 0 || Double.parseDouble(totalPoints) < 0) {
					Errors.AM1.displayErrorMsg();
					return;
				}
				
				DecimalFormat rounder = new DecimalFormat("#.####");
				rounder.setRoundingMode(RoundingMode.HALF_UP);
				
				gdtm.setValueAt(rounder.format(Double.parseDouble(pointsEarned)), row, 5);
				gdtm.setValueAt(rounder.format(Double.parseDouble(totalPoints)), row, 6);
				gdtm.setValueAt(rounder.format(Double.parseDouble(grade)), row, 7);
				
				calculateGrade(id);
				JOptionPane.showMessageDialog(null, "Successfully Updated", "System Notification", JOptionPane.INFORMATION_MESSAGE);
			}
			
			else if(catEditChoice.equals("Edit Comment")) {
				String comment = JOptionPane.showInputDialog(null, "Enter New Comment", "Assignment Master", JOptionPane.INFORMATION_MESSAGE);
				gdtm.setValueAt(comment, row, 8);
				JOptionPane.showMessageDialog(null, "Successfully Updated", "System Notification", JOptionPane.INFORMATION_MESSAGE);
			}
		}
	}
	
	/**
	 * Tells whether or not a specified assignment has a grade (i.e. not "Ungraded")
	 * @param code the assignment code for the specified grade
	 * @return whether or not the assignment is graded
	 */
	public boolean isAssignmentGraded(String code) {
		for(int i = 0; i < gdtm.getRowCount(); i++) {
			if(gdtm.getValueAt(i, 2).equals(code) && gdtm.getValueAt(i, 5).equals("Ungraded"))
				return false;
		}
		return true;
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
	 * Tells whether or not a word is all numbers
	 * @param word the word to be checked for numbers
	 * @return whether or not the word contains all numbers
	 */
	public boolean isNumbers(String word) {
		
		ArrayList<String> nums = new ArrayList<String>();
		for(int i = 0; i < 10; i++) {
			nums.add(i + "");
		}
		
		int counter = 0;
		for(int i = 0; i < word.length(); i++)
			if(nums.contains(word.charAt(i) + ""))
				counter++;
		
		if(counter == word.length())
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
	 * @param identifier the id of the course whose letter grade is being converted
	 * @param letterGrade the letter grade to calculate the quality points
	 * @return the number of quality points earned
	 */
	public double letToQual(String identifier, String letterGrade) {
		
		double quality = 0;
		if(rounding.equals("Tenths")) {
			if(letterGrade.equals("A+"))
				quality = 4.3;
			if(letterGrade.equals("A")) 
				quality = 4;
			if(letterGrade.equals("A-")) 
				quality = 3.7;
			if(letterGrade.equals("B+")) 
				quality = 3.3;
			if(letterGrade.equals("B")) 
				quality = 3;
			if(letterGrade.equals("B-")) 
				quality = 2.7;
			if(letterGrade.equals("C+")) 
				quality = 2.3;
			if(letterGrade.equals("C")) 
				quality = 2;
			if(letterGrade.equals("C-")) 
				quality = 1.7;
			if(letterGrade.equals("D+")) 
				quality = 1.3;
			if(letterGrade.equals("D")) 
				quality = 1;
			if(letterGrade.equals("D-")) 
				quality = 0.7;
		}
		else {
			if(letterGrade.equals("A+"))
				quality = 4.33;
			if(letterGrade.equals("A")) 
				quality = 4;
			if(letterGrade.equals("A-")) 
				quality = 3.67;
			if(letterGrade.equals("B+")) 
				quality = 3.33;
			if(letterGrade.equals("B")) 
				quality = 3;
			if(letterGrade.equals("B-")) 
				quality = 2.67;
			if(letterGrade.equals("C+")) 
				quality = 2.33;
			if(letterGrade.equals("C")) 
				quality = 2;
			if(letterGrade.equals("C-")) 
				quality = 1.67;
			if(letterGrade.equals("D+")) 
				quality = 1.33;
			if(letterGrade.equals("D")) 
				quality = 1;
			if(letterGrade.equals("D-")) 
				quality = 0.67;
		}
		
		if(honorsAPStatuses.get(identifier).equals("Honors") && !letterGrade.equals("F"))
			quality += honorsBonus;
		else if(honorsAPStatuses.get(identifier).equals("Advanced Placement") && !letterGrade.equals("F"))
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
			Errors.AM2.displayErrorMsg();
			return;
		}
		
		String identifier = "";
		
		if(identifiers.contains(identifierInput.getText()))
			identifier = identifierInput.getText();
		
		else {
			String[] identifierChoices = new String[identifiers.size()];
			for(int i = 0; i < identifiers.size(); i++)
				identifierChoices[i] = identifiers.get(i);
	
			identifier = (String) JOptionPane.showInputDialog(null, "Select Identifier", "Assignment Master", 
					JOptionPane.QUESTION_MESSAGE, null, identifierChoices, identifierChoices[0]);
			
			if(identifier == null) {
				displayCancelMsg();
				return;
			}
		}
		
		String[] assignmentChoices = {"Graded Assignment", "Ungraded Assignment"};
		
		String assnChoice = (String) JOptionPane.showInputDialog(null, "Select Assignment Type", "Assignment Master", 
				JOptionPane.QUESTION_MESSAGE, null, assignmentChoices, assignmentChoices[0]);
		
		if(assnChoice == null) {
			displayCancelMsg();
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
		
		if(categoryChoices.length == 0) {
			Errors.AM3.displayErrorMsg();
			return;
		}
		
		JPanel p = new JPanel();
		if(assnChoice.equals("Graded Assignment"))
			p.setLayout(new GridLayout(4, 0));
		else
			p.setLayout(new GridLayout(3, 0));
		String pointsEarned, totalPoints, category, comment;
		pointsEarned = totalPoints = category = comment = "";
		
		JComboBox categoryEntry = new JComboBox(categoryChoices);
		JTextField pointsEarnedEntry = new JTextField(15);
		JTextField totalPointsEntry = new JTextField(15);
		JTextField commentEntry = new JTextField(15);
		
		p.add(new JLabel("Select Category"));
		p.add(categoryEntry);
		
		if(assnChoice.equals("Graded Assignment")) {
			p.add(new JLabel("Enter Points Earned"));
			p.add(pointsEarnedEntry);
		}
		
		p.add(new JLabel("Enter Total Points"));
		p.add(totalPointsEntry);
		
		p.add(new JLabel("Enter Comment"));
		p.add(commentEntry);
		
		int result = JOptionPane.showConfirmDialog(null, p, "Assignment Master", JOptionPane.OK_CANCEL_OPTION);
		
		if(result == JOptionPane.OK_OPTION) {
			category = (String) categoryEntry.getSelectedItem();
			if(assnChoice.equals("Graded Assignment")) {
				pointsEarned = pointsEarnedEntry.getText();
				totalPoints = totalPointsEntry.getText();
				
				if(pointsEarned.isEmpty() || totalPoints.isEmpty()) {
					Errors.ML1.displayErrorMsg();
					return;
				}
			}
			else {
				totalPoints = totalPointsEntry.getText();
				if(totalPoints.isEmpty()) {
					Errors.ML1.displayErrorMsg();
					return;
				}
			}
			comment = commentEntry.getText();
		}
		
		else {
			displayCancelMsg();
			return; 
		}
				
		int catIndex = 0;
		for(int i = 0; i < categories.get(identifier).size(); i++)
			if(categories.get(identifier).get(i).equals(category))
				catIndex = i;
		
		String catWeight = categories.get(identifier).get(catIndex + 1);
		
		DecimalFormat rounder = new DecimalFormat("#.####");
		rounder.setRoundingMode(RoundingMode.HALF_UP);
		
		String grade = "";
		
		if(assnChoice.equals("Graded Assignment")) {
			try {
				grade = rounder.format(Double.parseDouble(pointsEarned) / Double.parseDouble(totalPoints) * 100);
			} catch (NumberFormatException e) {
				Errors.ML2.displayErrorMsg();
				return;
			}
			
			if(Double.parseDouble(pointsEarned) < 0 || Double.parseDouble(totalPoints) < 0) {
				Errors.AM1.displayErrorMsg();
				return;
			}
		}
		else {
			pointsEarned = "Ungraded";
			grade = "Ungraded";
		}
		
		if(assnChoice.equals("Graded Assignment"))
			pointsEarned = rounder.format(Double.parseDouble(pointsEarned));
		
		try {
			totalPoints = rounder.format(Double.parseDouble(totalPoints));
		} catch (NumberFormatException e) {
			Errors.ML2.displayErrorMsg();
			return;
		}
		
		if(assnChoice.equals("Graded Assignment"))
			gdtm.addRow(new Object[] {courseTitle, identifier, code, category, catWeight, rounder.format(Double.parseDouble(pointsEarned)),
				rounder.format(Double.parseDouble(totalPoints)), rounder.format(Double.parseDouble(grade)), comment});
		else
			gdtm.addRow(new Object[] {courseTitle, identifier, code, category, catWeight, pointsEarned,
					rounder.format(Double.parseDouble(totalPoints)), grade, comment});
		
		if(assnChoice.equals("Graded Assignment"))
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
	 * Tells whether or not there is a course in progress
	 * @return whether or not any courses are in progress
	 */
	public boolean existsInProgressCourse() {
		for(int i = 0; i < cdtm.getRowCount(); i++)
			if(cdtm.getValueAt(i, 9).equals("In Progress"))
				return true;
		return false;
	}
	
	/**
	 * Tells whether or not grades have been inputted for a course based on identifier
	 * @param id the identifier of a course
	 * @return whether or not any grades exist for the given course
	 */
	public boolean existsGrade(String id) {
		for(int i = 0; i < gdtm.getRowCount(); i++) {
			if(gdtm.getValueAt(i, 1).equals(id))
				return true;
		}
		return false;
	}
	
	/**
	 * Views grade category breakdown for course entered in identifierInput
	 * @param the unique course identifier to view the breakdown
	 */
	public void viewParticularBreakdown(String id) {
		
		if(!existsGrade(id)) {
			Errors.ML3.displayErrorMsg();
			return;
		}
		
		if(existsUnfinalizedTerm() && existsInProgressCourse()) {
			
			DecimalFormat rounder = new DecimalFormat("#.####");
			rounder.setRoundingMode(RoundingMode.HALF_UP);
			
			Hashtable scale = courseScales.get(id);
			
			String analyzerInfo = "Grade Analysis for Course [" + id + "]\n\n~~Category Breakdown~~\n\n";
			
			ArrayList<String> identifiers = new ArrayList<String>();
			ArrayList<String> titles = new ArrayList<String>();
			ArrayList<String> weights = new ArrayList<String>();
			ArrayList<String> courseTitles = new ArrayList<String>();
			
			double minPossibleGrade = 0;
			double totalPercentUngraded = 0;
			
			if(true) {
			
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
					double totalPointsUngraded = 0;
					titles = getCategoryNames(categories.get(identifiers.get(i)));
					weights = getCategoryValues(categories.get(identifiers.get(i)));
					
					for(int j = 0; j < titles.size(); j++) {
						for(int k = 0; k < gdtm.getRowCount(); k++) 
							if(gdtm.getValueAt(k, 1).equals(identifier) && gdtm.getValueAt(k, 3).equals(titles.get(j))) {
								if(!gdtm.getValueAt(k, 5).equals("Ungraded")) {
									pointsEarned += Double.parseDouble(gdtm.getValueAt(k, 5) + "");
									totalPoints += Double.parseDouble(gdtm.getValueAt(k, 6) + "");
								}
								else {
									totalPointsUngraded += Double.parseDouble(gdtm.getValueAt(k, 6) + "");
									totalPoints += Double.parseDouble(gdtm.getValueAt(k, 6) + "");
								}
							}
						double grade = pointsEarned / (totalPoints - totalPointsUngraded) * 100;
	
						if(totalPoints == 0) {
							analyzerInfo += "\nCategory Title: " + titles.get(j) + "\nWeight: " + weights.get(j) + "\nCategory Points: " + rounder.format(pointsEarned) + " / "
									+ rounder.format(totalPoints - totalPointsUngraded) + "\nCategory Average: " + rounder.format(grade) + "\n\n";
							totalPercentUngraded += Double.parseDouble(weights.get(j));
						}
						else {
							analyzerInfo += "\nCategory Title: " + titles.get(j) + "\nWeight: " + weights.get(j) + "\nCategory Points: " + rounder.format(pointsEarned) + " / "
									+ rounder.format(totalPoints - totalPointsUngraded) + "\nCategory Average: " + rounder.format(grade) + "\n\n";
							
							totalPercentUngraded += totalPointsUngraded / totalPoints * Double.parseDouble(weights.get(j));
						}
						
						if(totalPoints != 0)
							minPossibleGrade += pointsEarned / totalPoints * (Double.parseDouble(weights.get(j)) / getSumWeights(id)) * 100;
						pointsEarned = 0;
						totalPoints = 0;
						totalPointsUngraded = 0;
					}
					
					
					analyzerInfo += "~~Minimum Grade Possible~~\n\nYou will earn a " + rounder.format(minPossibleGrade) + " for the course\nif no additional "
							+ "work is completed\n(assuming all assignments have been entered).\n\n";
					
					boolean printedAp, printedA, printedAm, printedBp, printedB, printedBm, printedCp, printedC, printedCm,
						printedDp, printedD, printedDm, printedP;
					
					printedAp = printedA = printedAm = printedBp = printedB = printedBm = printedCp = printedC = printedCm = printedDp = 
							printedD = printedDm = printedP = false;
					
					double tAp, tA, tAm, tBp, tB, tBm, tCp, tC, tCm, tDp, tD, tDm, tP;
					
					tAp = tA = tAm = tBp = tB = tBm = tCp = tC = tCm = tDp = tD = tDm = tP = -1;
					
					for(double x = 0; x < 200; x+= 0.125) {
						if(!printedAp && isAPluses && (minPossibleGrade + totalPercentUngraded / getSumWeights(id) * x) >= Double.parseDouble(scale.get("A+") + "")) {
							printedAp = true;
							tAp = x;
						}
						if(!printedA && (minPossibleGrade + totalPercentUngraded / getSumWeights(id) * x) >= Double.parseDouble(scale.get("A") + "")) {
							printedA = true;
							tA = x;
						}
						if(!printedAm && (minPossibleGrade + totalPercentUngraded / getSumWeights(id) * x) >= Double.parseDouble(scale.get("A-") + "")) {
							printedAm = true;
							tAm = x;
						}
						if(!printedBp && (minPossibleGrade + totalPercentUngraded / getSumWeights(id) * x) >= Double.parseDouble(scale.get("B+") + "")) {
							printedBp = true;
							tBp = x;
						}
						if(!printedB && (minPossibleGrade + totalPercentUngraded / getSumWeights(id) * x) >= Double.parseDouble(scale.get("B") + "")) {
							printedB = true;
							tB = x;
						}
						if(!printedBm && (minPossibleGrade + totalPercentUngraded / getSumWeights(id) * x) >= Double.parseDouble(scale.get("B-") + "")) {
							printedBm = true;
							tBm = x;
						}
						if(!printedCp && (minPossibleGrade + totalPercentUngraded / getSumWeights(id) * x) >= Double.parseDouble(scale.get("C+") + "")) {
							printedCp = true;
							tCp = x;
						}
						if(!printedC && (minPossibleGrade + totalPercentUngraded / getSumWeights(id) * x) >= Double.parseDouble(scale.get("C") + "")) {
							printedC = true;
							tC = x;
						}
						if(!printedCm && (minPossibleGrade + totalPercentUngraded / getSumWeights(id) * x) >= Double.parseDouble(scale.get("C-") + "")) {
							printedCm = true;
							tCm = x;
						}
						if(!printedDp && (minPossibleGrade + totalPercentUngraded / getSumWeights(id) * x) >= Double.parseDouble(scale.get("D+") + "")) {
							printedDp = true;
							tDp = x;
						}
						if(!printedD && (minPossibleGrade + totalPercentUngraded / getSumWeights(id) * x) >= Double.parseDouble(scale.get("D") + "")) {
							printedD = true;
							tD = x;
						}
						if(!printedDm && (minPossibleGrade + totalPercentUngraded / getSumWeights(id) * x) >= Double.parseDouble(scale.get("D-") + "")) {
							printedDm = true;
							tDm = x;
						}
						if(!printedP && (minPossibleGrade + totalPercentUngraded / getSumWeights(id) * x) >= Double.parseDouble(scale.get("P") + "")) {
							printedP = true;
							tP = x;
						}
					}
					
					double moreDetailedGrade = minPossibleGrade/(getSumWeights(id)-totalPercentUngraded) * 100;
					String roundDetailedGrade = rounder.format(moreDetailedGrade);
					
					analyzerInfo += "~~More Detailed Average~~\n" + roundDetailedGrade + 
							"\nThis grade is more representative of the\n"
							+ "courseworkthat has been completed thus far\n"
							+ "and calculates based on individual assignments\n"
							+ "rather than full categories.\n\n";
					
					analyzerInfo += "~~Grade Qualification Analysis~~\n\n";
					
					if(isAPluses) {
						analyzerInfo += "Remaining Average Required for [A+]: " + rounder.format(tAp) + "\n";
					}
					
					analyzerInfo += "Remaining Average Required for [A]: " + rounder.format(tA) + "\n";
					
					analyzerInfo += "Remaining Average Required for [A-]: " + rounder.format(tAm) + "\n";
					
					analyzerInfo += "Remaining Average Required for [B+]: " + rounder.format(tBp) + "\n";
					
					analyzerInfo += "Remaining Average Required for [B]: " + rounder.format(tB) + "\n";
					
					analyzerInfo += "Remaining Average Required for [B-]: " + rounder.format(tBm) + "\n";
					
					analyzerInfo += "Remaining Average Required for [C+]: " + rounder.format(tCp) + "\n";
					
					analyzerInfo += "Remaining Average Required for [C]: " + rounder.format(tC) + "\n";
					
					analyzerInfo += "Remaining Average Required for [C-]: " + rounder.format(tCm) + "\n";
					
					analyzerInfo += "Remaining Average Required for [D+]: " + rounder.format(tDp) + "\n";
					
					analyzerInfo += "Remaining Average Required for [D]: " + rounder.format(tD) + "\n";
					
					analyzerInfo += "Remaining Average Required for [D-]: " + rounder.format(tDm) + "\n";
					
					analyzerInfo += "Remaining Average Required for [Pass]: " + rounder.format(tP);
				}
			}	
			
			JTextArea a = new JTextArea(analyzerInfo, 30, 25);
			a.setEditable(false);
			JScrollPane sp = new JScrollPane(a);
			sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
			sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			JOptionPane.showMessageDialog(null, sp, "Grade Master", JOptionPane.INFORMATION_MESSAGE);
			// JOptionPane.showMessageDialog(null, analyzerInfo, "Grade Master", JOptionPane.INFORMATION_MESSAGE);
		}
	}
	
	/**
	 * Tells whether there exists ungraded assignments for a specified course
	 * @param id the identifier of the course
	 * @return whether or not a course has ungraded assignments
	 */
	public boolean hasUngradedAssignments(String id) {
		for(int i = 0; i < gdtm.getRowCount(); i++) {
			if(gdtm.getValueAt(i, 1).equals(id) && gdtm.getValueAt(i, 5).equals("Ungraded"))
				return true;
		}
		return false;
	}
	
	/**
	 * Gets the sum of the category weights for a specified course
	 * @param id the identifier of the course
	 * @return the sum of the category weights for the course
	 */
	public double getSumWeights(String id) {
		double sum = 0;
		ArrayList<String> values = getCategoryValues(categories.get(id));
		for(int i = 0; i < values.size(); i++)
			sum += Double.parseDouble(values.get(i));
		return sum;
	}
	
	/**
	 * Hides grade breakdown if it is currently being viewed
	 */
	public void hideBreakdown() {
		for(int i = 0; i < gdtm.getRowCount(); i++)
			if(gdtm.getValueAt(i, 2).equals("")) {
				gdtm.removeRow(i);
				i--;
			}
	}
	
	/**
	 * Allows user to change settings such as...
	 * Whether or not A+ is allowed
	 * Whether or not there are Honors/AP Courses
	 * Honors/AP GPA bonuses
	 * Adding new grade scales/deleting grade scales
	 */
	public void settings() {
		
		String[] aPlusChoices = {"No", "Yes"};
		String[] decimalChoices = {"Tenths", "Hundredths"};
		String[] apHonorsChoices = {"No", "Yes"};
		
		JPanel p = new JPanel();
		p.setLayout(new GridLayout(7, 0));
		
		JComboBox aPlusEntry = new JComboBox(aPlusChoices);
		JComboBox decimalEntry = new JComboBox(decimalChoices);
		JComboBox isAPHonorsEntry = new JComboBox(apHonorsChoices);
		JTextField honorsBonusEntry = new JTextField(15);
		JTextField apBonusEntry = new JTextField(15);
		
		
		JButton setDefaults = new JButton("Set Defaults");
		setDefaults.addActionListener(this);
		setDefaults.setBackground(new Color(93, 201, 247));
		
		JButton deleteGradeScale = new JButton("Delete Grade Scale");
		deleteGradeScale.addActionListener(this);
		deleteGradeScale.setBackground(new Color(93, 201, 247));
		
		JButton addNewGradeScale = new JButton("Add Grade Scale");
		addNewGradeScale.addActionListener(this);
		addNewGradeScale.setBackground(new Color(93, 201, 247));
		
		JButton viewGradeScale = new JButton("View Grade Scale");
		viewGradeScale.addActionListener(this);
		viewGradeScale.setBackground(new Color(93, 201, 247));
		
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
		p.add(viewGradeScale);
		p.add(setDefaults);
		
		if(cdtm.getRowCount() > 0) {
			aPlusEntry.setEnabled(false);
			decimalEntry.setEnabled(false);
			isAPHonorsEntry.setEnabled(false);
			honorsBonusEntry.setEnabled(false);
			apBonusEntry.setEnabled(false);
		}
		
		int result = JOptionPane.showConfirmDialog(null, p, "Settings Master", JOptionPane.OK_CANCEL_OPTION);
		
		if(result == JOptionPane.OK_OPTION) {
			
			if(aPlusEntry.getSelectedItem().equals("Yes")) {
				isAPluses = true;
				
			}
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
				Errors.ML2.displayErrorMsg();
				return;
			}
			
			try {
				double testError = Double.parseDouble(apBonusEntry.getText());
			} catch (NumberFormatException e) {
				Errors.ML2.displayErrorMsg();
				return;
			}
			
			honorsBonus = Double.parseDouble(honorsBonusEntry.getText());
			apBonus = Double.parseDouble(apBonusEntry.getText());
			
			if(honorsBonus < 0 || apBonus < 0) {
				honorsBonus = 1;
				apBonus = 1;
				Errors.SS1.displayErrorMsg();
				return;
			}
			
			if(honorsBonusEntry.getText().isEmpty() || apBonusEntry.getText().isEmpty()) {
				Errors.ML1.displayErrorMsg();
				return;
			}
		} 
	}
	
	/**
	 * Adds a new grade scale to gradeScales
	 * Allows user to assign each letter to a numeric grade
	 */
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
		p.add(new JLabel("Minimum Grade for A+"));
		p.add(minAp);
		p.add(new JLabel("Minimum Grade for A"));
		p.add(minA);
		p.add(new JLabel("Minimum Grade for A-"));
		p.add(minAm);
		p.add(new JLabel("Minimum Grade for B+"));
		p.add(minBp);
		p.add(new JLabel("Minimum Grade for B"));
		p.add(minB);
		p.add(new JLabel("Minimum Grade for B-"));
		p.add(minBm);
		p.add(new JLabel("Minimum Grade for C+"));
		p.add(minCp);
		p.add(new JLabel("Minimum Grade for C"));
		p.add(minC);
		p.add(new JLabel("Minimum Grade for C-"));
		p.add(minCm);
		p.add(new JLabel("Minimum Grade for D+"));
		p.add(minDp);
		p.add(new JLabel("Minimum Grade for D"));
		p.add(minD);
		p.add(new JLabel("Minimum Grade for D-"));
		p.add(minDm);
		p.add(new JLabel("Minimum Grade for Pass"));
		p.add(minP);
		
		int result = JOptionPane.showConfirmDialog(null, p, "Settings Master", JOptionPane.OK_CANCEL_OPTION);
		
		if(result == JOptionPane.OK_OPTION) {
			
			for(int i = 0; i < gradeScales.size(); i++)
				if(gradeScales.get(i).containsValue(name.getText())) {
					Errors.SS2.displayErrorMsg();
					return;
				}
			
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
				Errors.ML2.displayErrorMsg();
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
					Errors.SS2.displayErrorMsg();
					return;
				}
				if((Double.parseDouble(minA.getText()) < 0 || Double.parseDouble(minAm.getText()) < 0 &&
					    Double.parseDouble(minBp.getText()) < 0 || Double.parseDouble(minB.getText()) < 0 ||
						Double.parseDouble(minBm.getText()) < 0 || Double.parseDouble(minCp.getText()) < 0 ||
						Double.parseDouble(minC.getText()) < 0 || Double.parseDouble(minCm.getText()) < 0 ||
						Double.parseDouble(minDp.getText()) < 0 || Double.parseDouble(minD.getText()) < 0 ||
						Double.parseDouble(minDm.getText()) < 0)) {
					Errors.SS2.displayErrorMsg();
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
					Errors.SS2.displayErrorMsg();
					return;
				}
				if((Double.parseDouble(minA.getText()) < 0 || Double.parseDouble(minAm.getText()) < 0 &&
					    Double.parseDouble(minBp.getText()) < 0 || Double.parseDouble(minB.getText()) < 0 ||
						Double.parseDouble(minBm.getText()) < 0 || Double.parseDouble(minCp.getText()) < 0 ||
						Double.parseDouble(minC.getText()) < 0 || Double.parseDouble(minCm.getText()) < 0 ||
						Double.parseDouble(minDp.getText()) < 0 || Double.parseDouble(minD.getText()) < 0 ||
						Double.parseDouble(minDm.getText()) < 0)) {
					Errors.SS2.displayErrorMsg();
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
		else {
			displayCancelMsg();
			return;
		}
	}
	
	/**
	 * Adds user to delete a grade scale if it is not being used
	 */
	public void deleteGradeScale() {
		
		if(gradeScales.size() == 0) {
			Errors.SS3.displayErrorMsg();
			return;
		}
		
		String[] choices = new String[gradeScales.size()];
		for(int i = 0; i < gradeScales.size(); i++)
			choices[i] = (String) gradeScales.get(i).get("Name");
		
		String delete = (String) JOptionPane.showInputDialog(null, "Select Grade Scale to Delete", "Settings Master", 
				JOptionPane.QUESTION_MESSAGE, null, choices, choices[0]);
		
		if(delete == null) {
			displayCancelMsg();
			return;
		}
		
		for(int i = 0; i < gradeScales.size(); i++) {
			if(gradeScales.get(i).get("Name").equals(delete)) {
				for(int j = 0; j < courseScales.size(); j++)
				if(courseScales.containsValue(gradeScales.get(i))) {
					Errors.SS4.displayErrorMsg();
					return;
				}
				gradeScales.remove(i);
				JOptionPane.showMessageDialog(null, "Successfully Updated", "System Notification", JOptionPane.INFORMATION_MESSAGE);
			}
		}
	}
	
	/**
	 * Allows user to view minimum grade required for each tier in a grade scale
	 */
	public void viewGradeScale() {
		
		if(gradeScales.size() == 0) {
			Errors.SS3.displayErrorMsg();
			return;
		}
		
		String[] choices = new String[gradeScales.size()];
		for(int i = 0; i < gradeScales.size(); i++)
			choices[i] = (String) gradeScales.get(i).get("Name");
		
		String view = (String) JOptionPane.showInputDialog(null, "Select Grade Scale to View", "Settings Master", 
				JOptionPane.QUESTION_MESSAGE, null, choices, choices[0]);
		
		if(view == null) {
			displayCancelMsg();
			return;
		}
		
		for(int i = 0; i < gradeScales.size(); i++) {
			if(isAPluses && gradeScales.get(i).get("Name").equals(view)) {
				JOptionPane.showMessageDialog(null, "Grade Scale Name: " + gradeScales.get(i).get("Name") + "\n"
						+ "Minimum Grade for [A+]: " + gradeScales.get(i).get("A+") + "\n"
						+ "Minimum Grade for [A]: " + gradeScales.get(i).get("A") + "\n"
						+ "Minimum Grade for [A-]: " + gradeScales.get(i).get("A-") + "\n"
						+ "Minimum Grade for [B+]: " + gradeScales.get(i).get("B+") + "\n"
						+ "Minimum Grade for [B]: " + gradeScales.get(i).get("B") + "\n"
						+ "Minimum Grade for [B-]: " + gradeScales.get(i).get("B-") + "\n"
						+ "Minimum Grade for [C+]: " + gradeScales.get(i).get("C+") + "\n"
						+ "Minimum Grade for [C]: " + gradeScales.get(i).get("C") + "\n"
						+ "Minimum Grade for [C-]: " + gradeScales.get(i).get("C-") + "\n"
						+ "Minimum Grade for [D+]: " + gradeScales.get(i).get("D+") + "\n"
						+ "Minimum Grade for [D]: " + gradeScales.get(i).get("D") + "\n"
						+ "Minimum Grade for [D-]: " + gradeScales.get(i).get("D-") + "\n"
						+ "Minimum Grade for [Pass]: " + gradeScales.get(i).get("P") + "\n",
						"Settings Master", JOptionPane.INFORMATION_MESSAGE);
			}
			else if(!isAPluses && gradeScales.get(i).get("Name").equals(view)) {
				JOptionPane.showMessageDialog(null, "Grade Scale Name: " + gradeScales.get(i).get("Name") + "\n"
						+ "Minimum Grade for [A]: " + gradeScales.get(i).get("A") + "\n"
						+ "Minimum Grade for [A-]: " + gradeScales.get(i).get("A-") + "\n"
						+ "Minimum Grade for [B+]: " + gradeScales.get(i).get("B+") + "\n"
						+ "Minimum Grade for [B]: " + gradeScales.get(i).get("B") + "\n"
						+ "Minimum Grade for [B-]: " + gradeScales.get(i).get("B-") + "\n"
						+ "Minimum Grade for [C+]: " + gradeScales.get(i).get("C+") + "\n"
						+ "Minimum Grade for [C]: " + gradeScales.get(i).get("C") + "\n"
						+ "Minimum Grade for [C-]: " + gradeScales.get(i).get("C-") + "\n"
						+ "Minimum Grade for [D+]: " + gradeScales.get(i).get("D+") + "\n"
						+ "Minimum Grade for [D]: " + gradeScales.get(i).get("D") + "\n"
						+ "Minimum Grade for [D-]: " + gradeScales.get(i).get("D-") + "\n"
						+ "Minimum Grade for [Pass]: " + gradeScales.get(i).get("P") + "\n",
						"Settings Master", JOptionPane.INFORMATION_MESSAGE);
			}
		}
	}
	
	/**
	 * Gets the name of a grade scale
	 * @param the grade scale
	 * @return the name of a grade scale
	 */
	public String getScaleName(Hashtable scale) {
	    return (String) scale.get("Name");
	}
	
	/**
	 * Allows user to change default values for adding a course, including
	 * number of credits, grade mode, and grading method
	 */
	public void setDefaults() {
		
		String[] creditChoices = {"0","0.5","1","1.5","2.5","3","4","5","6"};
		JComboBox creditDefaults = new JComboBox(creditChoices);
		creditDefaults.setSelectedItem(defaultCreditAmount);
		
		String[] gModeChoices = {"Letter", "P/NP", "Notation"};
		JComboBox gModeDefaults = new JComboBox(gModeChoices);
		gModeDefaults.setSelectedItem(defaultGMode);
		
		String[] gMethodChoices = {"Category Weightings", "Total Points"};
		JComboBox gMethodDefaults = new JComboBox(gMethodChoices);
		gMethodDefaults.setSelectedItem(defaultGMethod);
		
		JPanel p = new JPanel();
		p.setLayout(new GridLayout(3, 2));
		
		p.add(new JLabel("Default Number of Credits"));
		p.add(creditDefaults);
		
		p.add(new JLabel("Default Grade Mode"));
		p.add(gModeDefaults);
		
		p.add(new JLabel("Default Grading Method"));
		p.add(gMethodDefaults);
		
		int result = JOptionPane.showConfirmDialog(null, p, "Settings Master", JOptionPane.OK_CANCEL_OPTION);
		
		if(result == JOptionPane.OK_OPTION) {
			defaultCreditAmount = (String) creditDefaults.getSelectedItem();
			defaultGMode = (String) gModeDefaults.getSelectedItem();
			defaultGMethod = (String) gMethodDefaults.getSelectedItem();
			
			JOptionPane.showMessageDialog(null, "Successfully Updated", "System Notification", JOptionPane.INFORMATION_MESSAGE);
		}
		else {
			displayCancelMsg();
			return;
		}
	}
	
	/**
	 * Helps user with navigating the program including an explanation of terminology, how things work, and error details
	 */
	public void help() {
		String[] options = {"Glossary (Part 1)", "Glossary (Part 2)", "Functionality", "Error Details", "Copyright"};
		String choice = (String) JOptionPane.showInputDialog(null, "Select an Option to Learn More", "Help Master", JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
		
		if(choice == null) {
			displayCancelMsg();
			return;
		}
		
		if(choice.equals("Glossary (Part 1)")) {
			String s = "Subject/Course Number. Also known as a course code.\nExamples: ENGL 1011, CSCI 2113, BISC 1112, etc.\n\n"
					+ "Course Title. What is the course called?\nExamples: Introduction to Psychology, Single-Variable Calculus I, etc.\n\n" 
					+ "Comment. Anything you wish to put as additional information for the course.\nSuggestions: teacher name, course time/day, "
					+ "course location, or, if itâ€™s a grade, what the assignment/grade is for, etc.\n\n" 
					+ "Identifier. A unique, system-generated ID for the course.\n"
					+ "This is what you will enter into the Identifier/Code textfield to make any deletions or edits. \n"
					+ "Note: You can tell if you correctly input a course as Honors/AP by the Identifier, which will end\n"
					+ "in \"H\" for Honors or \"AP\" for Advanced Placement. If you added it with the incorrect distinction, you must remove\n"
					+ "and readd the course to change the distinction. AP/Honors status cannot be changed through Edit Element.\n\n"
					+ "Credits. The number of credits for the course. (Typically 3 or 4 for a college course).\n\n"
					+ "Numeric Grade. The current average grade for the class, calculated from entering grades.\n\n"
					+ "Grade Mode. How the course is graded.\n\n"
					+ "Grade Mode - Letter. Indicates a Letter Grade for the course.\n\n"
					+ "Grade Mode - P/NP. Indicates a Pass/No Pass grade for the course.\n\n"
					+ "Grade Mode - Notation. Indicates that the course is there for record, but is not factored into GPA calculation.\n\n"
					+ "Notation - TR. Transfer. Transfer credit has been awarded for the course.\n\n"
					+ "Notation - I. Incomplete. An incomplete has been granted, meaning the student has provided a valid reason\n"
					+ "for why they could not complete their work on time.\n\n"
					+ "Notation - W. Withdrawal. The course has been withdrawn from.\n\n"
					+ "Notation - Z. Assigned when a student has registered for a course but has not attended class or done any graded work.\n\n"
					+ "Grading Method. How the course is graded, either by weighted averages or by total points earned / total points attempted.";		
			JOptionPane.showMessageDialog(null, s, "Help Master", JOptionPane.INFORMATION_MESSAGE);
		}
		
		if(choice.equals("Glossary (Part 2)")) {
			String s = "Final Grade. The final grade for the course, either manually entered by the user or calculated automatically\n"
					+ "based on entered grades and the selected grade mode.\n\n"
					+ "Term. The term that the courses were taken in. Examples: Spring 2020, Summer 2017, Fall 2015, etc.\n\n"
					+ "Status. The current status of the course.\n\n"
					+ "Status - In Progress. Indicates that the course is still being taken.\n\n"
					+ "Status - Manual Entry. Indicates that the user has manually entered a final grade. The final grade will not\n"
					+ "be calculated automatically. Typically used to input past history of grades.\n\n"
					+ "Status - Finalized. Indicates that the user has finalized grades for the past term. A GPA will be calculated\n"
					+ "and these courses can no longer be altered or removed once finalized.\n\n"
					+ "Assignment Code. A unique, system-generated ID for an entered grade/assignment. This is what you will enter\n"
					+ "into the Identifier/Code textfield to make any deletions or edits.\n\n"
					+ "Category. The name of the category that a grade belongs to. These names should not contain numbers.\n"
					+ "Examples: Homework, Exams, Quizzes, etc.\n\n"
					+ "Category Weight. How much weight a category holds in calculating the numeric grade for the course. \n"
					+ "Should be entered as a single number, with decimals allowed. Examples: 15, 25, 37.5, etc.\n"
					+ "(Note: Percentage symbols should not be entered when setting up category weights, they are implied).\n\n"
					+ "Points Earned. How many points were earned for an assignment. The numerator of the grade.\n"
					+ "(Example: 90/100, Points Earned = 90).\n\n"
					+ "Total Points. How many total possible points could have been earned for an assignment. The denominator of the grade.\n"
					+ "(Example: 90/100, Total Points = 100).\n\n"
					+ "Grade. The grade earned for a particular assignment, based on Points Earned / Total Points.";
			JOptionPane.showMessageDialog(null, s, "Help Master", JOptionPane.INFORMATION_MESSAGE);
		}
		
		if(choice.equals("Functionality")) {
			String s = "Add Course. Used to add a new course to the top table, which is a list of all courses entered by the user.\n\n"
					+ "Remove Element. Used to delete either a course or an assignment, based on the Identifier or Assignment Code\n"
					+ "entered into the textfield.\n\n"
					+ "Edit Element. Used to edit a course or an assignment, based on the Identifier or Assignment Code entered into\n"
					+ "the textfield.\n\n"
					+ "Add Assignment. Used to enter an assignment into the bottom table. Upon entering a grade, the course that the grade is\n"
					+ "for will have its numeric grade and final grade recalculated.\n\n"
					+ "Analyze Grades. Used to view more detailed information about grades for a course. Displays the average for\n"
					+ "each individual grade category and tells you the minimum grade necessary to attain each threshold.\n\n"
					+ "Hide Analyzer. Hides the additional grading information from the Analyze Grades button.\n\n"
					+ "Finalize Grades. Calculates the GPA for the current term and the overall GPA based on all previous terms.\n"
					+ "Sets the status of all courses to Finalized.\n\n"
					+ "Import/Export. Used to save and load the gradebook so all data is maintained upon exiting the program.\n\n"
					+ "Settings. Used to customize the gradebook for the user. Changes can be made to how grades and GPA is calculated.\n"
					+ "Once a course is added, settings cannot be changed unless all courses are removed to prevent inconsistency.\n\n"
					+ "Identifier/Code. In the text field, the Identifier of a course or the Assignment Code of a grade should be\n"
					+ "entered when using Remove Element, Edit Element, and View Breakdown buttons.";
			JOptionPane.showMessageDialog(null, s, "Help Master", JOptionPane.INFORMATION_MESSAGE);		
		}
		
		if(choice.equals("Error Details")) {
			String[] errorOptions = {"AER", "AM", "FG", "SS", "ML"};
			String choice2 = (String) JOptionPane.showInputDialog(null, "Select Error Type", "Help Master", 
					JOptionPane.QUESTION_MESSAGE, null, errorOptions, errorOptions[0]);
			
			if(choice2 == null) {
				displayCancelMsg();
				return;
			}
			
			if(choice2.equals("AER")) {
				String s = "AER: Add/Edit/Remove Errors\n\n"
						+ "AER1. Entered Term is Finalized. User cannot enter a term for a course if that term has already been used.\n"
						+ "For example, if a user has already finalized grades for Fall 2019, the user cannot use Fall 2019 as a term anymore.\n\n"
						+ "AER2. Must Finalize Previous Term. User cannot enter courses for two different terms simultaneously.\n"
						+ "Each term must be finalized because a new term can be used.\n\n"
						+ "AER3. Category Name Cannot Be Numbers. This will cause confusion between the name of the category and the category weight.\n"
						+ "If numbers must be used, write them out in word form or use Roman numerals.\n\n"
						+ "AER4. Category Name Already Exists. The entered category name has already been used for the course.\n\n"
						+ "AER5. Cannot Have Negative Weight. Category weights cannot be negative. They must be positive numbers, with decimals permitted.\n\n"
						+ "AER6. Must Create a Grade Scale. There are no available grade scales so a course cannot be added.\n"
						+ "Add a grade scale in Settings and try again.\n\n"
						+ "AER7. Element Does Not Exist. There is no course or assignment with a matching Identifier or Assignment Code\n"
						+ "to what was entered into the Identifier/Code text field.\n\n"
						+ "AER8. Cannot Remove Finalized Course. The course with a matching Identifier to what was entered into the Identifier/Code\n"
						+ "text field has been finalized.\n\n"
						+ "AER9. Cannot Edit Finalized Course. The course with a matching Identifier to what was entered into the Identifier/Code\n"
						+ "text field has been finalized.\n\n"
						+ "AER10. No Categories Available. All grade categories for this course have been removed.";
				JOptionPane.showMessageDialog(null, s, "Help Master", JOptionPane.INFORMATION_MESSAGE);	
			}
			
			if(choice2.equals("AM")) {
				String s = "AM: Assignment Errors\n\n"
						+ "AM1. Negative Values Not Accepted. Cannot use negative values when entering a grade.\n\n"
						+ "AM2. No Courses Available. There are no courses available for which grades can be entered.\n"
						+ "Courses must have Status = In Progress to be eligible to have grades entered.\n\n"
						+ "AM3. Must Add Category. If all categories have been deleted, a new one must be added in order to enter a grade.";
				JOptionPane.showMessageDialog(null, s, "Help Master", JOptionPane.INFORMATION_MESSAGE);	
			}
			
			if(choice2.equals("FG")) {
				String s = "FG: Finalizing Grade Errors\n\n"
						+ "FG1. Cannot Finalize with Final Grade = In Progress. If any of the cells in the Final Grade columns states \"In Progress\",\n"
						+ "grades cannot be finalized as \"In Progress\" cannot be interpreted for GPA calculation.\n\n"
						+ "FG2. All Courses Finalized. There are no courses to be finalized. Either all courses are finalized,\n"
						+ "or no courses have yet been entered.";
				JOptionPane.showMessageDialog(null, s, "Help Master", JOptionPane.INFORMATION_MESSAGE);
			}
			
			if(choice2.equals("SS")) {
				String s = "SS: Settings Errors\n\n"
						+ "SS1. Cannot Have Negative Bonus. An Honors or AP GPA Bonus cannot be a negative value.\n\n"
						+ "SS2. Invalid Grade Scale. Occurs if negative values have been entered or if the scale does not make sense.\n"
						+ "For example, a scale cannot contain a minimum grade of 90 for an A and 95 for a B because 95 > 90 but an A is a better grade.\n"
						+ "Also occurs if the name of the grade scale is already taken. Two grade scales cannot exist with the same name.\n\n"
						+ "SS3. No Grade Scales Available. There are no grade scales left to view or delete.\n\n"
						+ "SS4. Grade Scale In Use. A grade scale cannot be deleted if it is currently being used by a course to calculate grades.\n"
						+ "All courses linked to that grade scale must be changed to a different grade scale before it can be deleted.\n\n"
						+ "SS5. Check Settings Before Adding Course. User must check settings before using Grade Calculator v5.\n"
						+ "Once a course is added, settings cannot be changed unless all courses are removed to prevent inconsistency.";
				JOptionPane.showMessageDialog(null, s, "Help Master", JOptionPane.INFORMATION_MESSAGE);
			}
			
			if(choice2.equals("ML")) {
				String s = "ML: Miscellaneous Errors\n\n"
						+ "ML1. Missing Information. If any text fields are left blank other than a comment, this error will arise.\n\n"
						+ "ML2. Number Format Exception. Arises when anything other than a number has been entered into a text field\n"
						+ "that is only meant to accept numbers.\n\n" 
						+ "ML3. No Breakdown to Formulate. Arises when no grades have been inputted for the identified course.\n\n"
						+ "ML4. No Grades for Calculation. Arises when trying to calculate What-If GPA when no grades have been entered.\n\n"
						+ "ML5. Zero Credit Total. Cannot Calculate What-If GPA if the sum of the term credits is zero.";
				JOptionPane.showMessageDialog(null, s, "Help Master", JOptionPane.INFORMATION_MESSAGE);
			}
		}
		if(choice.equals("Copyright")) {
			String s = "Copyright \u00A9 2020 Ryan Hudson. All rights reserved.";
			JOptionPane.showMessageDialog(null, s, "Help Master", JOptionPane.INFORMATION_MESSAGE);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		String s = e.getActionCommand();
		
		hideBreakdown();
		viewBreakdown.setText("Analyze Grades");
		
		if(s.equals("Add Course")) {
			if(gradeScales.size() == 0) {
				Errors.AER6.displayErrorMsg();
				return;
			}
			
			if(!isHonorsAPClasses)
				addCourse();
			else 
				addCourseHonorsAP();
		}
		
		if(s.equals("Remove Element")) {
			removeElement();
		}
		
		if(s.equals("Finalize Grades")) {
			
			for(int i = 0; i < cdtm.getRowCount(); i++) {
				if(cdtm.getValueAt(i, 7).equals("In Progress")) {
					Errors.FG1.displayErrorMsg();
					return;
				}
			}
				
			if(!existsUnfinalizedCourses() || cdtm.getRowCount() == 0) {
				Errors.FG2.displayErrorMsg();
				return;
			}
			
			else if(JOptionPane.showConfirmDialog(null, "Are you sure you want to finalize grades?\nThis action cannot be reversed.\n"
					+ "Note: It is advised to export before finalizing.\nGrades will be cleared.", "Grade Master", JOptionPane.YES_NO_CANCEL_OPTION) == 0) {
				int counter = 0;
				for(int i = 0; i < cdtm.getRowCount(); i++)
					if(cdtm.getValueAt(i, 9).equals("In Progress") || cdtm.getValueAt(i, 9).equals("Manual Entry"))
						counter++;
				if(counter > 0)
					finalizeGrades();
				else {
					Errors.FG2.displayErrorMsg();
					return;
				}
			}
			else {
				displayCancelMsg();
				return;
			}
		}
		
		if(s.equals("Manual Override")) {
			if(!courseList.isEnabled()) {
				if(JOptionPane.showConfirmDialog(null, "Are you sure you want to enter Manual Override mode? \n"
						+ "It is highly recommended to use the Edit Course function.\nNote: Reclick Manual Override to return to Automatic.",
						"Settings Master", JOptionPane.YES_NO_CANCEL_OPTION) == 0) {
					courseList.setEnabled(true);
					gradeList.setEnabled(true);
				}
				else {
					displayCancelMsg();
					return;
				}
			}
			else {
				JOptionPane.showMessageDialog(null, "Returned to Automatic Mode");
				courseList.setEnabled(false);
				courseList.getSelectionModel().clearSelection();
				gradeList.getSelectionModel().clearSelection();
				gradeList.setEnabled(false);
			}
		}
		
		if(s.equals("Edit Element")) {
			hideBreakdown();
			editElement();
		}
		
		if(s.equals("Add Assignment")) {
			hideBreakdown();
			enterGrade();
			
			TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(gradeList.getModel());
			gradeList.setRowSorter(sorter);
			List<RowSorter.SortKey> sortKeys = new ArrayList<>();
			
			int columnIndexToSort = 1;
			sortKeys.add(new RowSorter.SortKey(columnIndexToSort, SortOrder.ASCENDING));
			
			sorter.setSortKeys(sortKeys);
			sorter.sort();
			
			for(int i = 0; i < gdtm.getColumnCount(); i++)
				sorter.setSortable(i, false);
		}
		
		if(s.equals("Analyze Grades")) {
			
			String[] analyzeChoices = {"Analyze Course Grades", "View What-If GPA"};
			
			String analyze = (String) JOptionPane.showInputDialog(null, "Select Action to Perform", "Grade Master", 
					JOptionPane.QUESTION_MESSAGE, null, analyzeChoices, analyzeChoices[0]);
			
			if(analyze == null) {
				displayCancelMsg();
				return;
			}
			
			if(analyze.equals("Analyze Course Grades") && isIdentifierFound()) {
				viewParticularBreakdown(identifierInput.getText());
			}
			
			else if(analyze.equals("Analyze Course Grades") && !isIdentifierFound()) {
				Errors.AER7.displayErrorMsg();
			}
			
			if(analyze.equals("View What-If GPA") && cdtm.getRowCount() > 0)
				displayWhatIfGPA();
			
			else if(analyze.equals("View What-If GPA") && cdtm.getRowCount() <= 0)
				Errors.ML4.displayErrorMsg();
		}
		
		if(s.equals("Hide Analyzer")) {
			hideBreakdown();
			viewBreakdown.setText("Analyze Grades");
			JOptionPane.showMessageDialog(null, "Successfully Updated", "System Notification", JOptionPane.INFORMATION_MESSAGE);
		}
		
		if(s.equals("Import/Export")) {
			if(cdtm.getRowCount() == 0 && gdtm.getRowCount() == 0) {
				loadTable();
			}
			else
				saveTable();
		}
		
		if(s.equals("Settings")) {
			checkedSettings = true;
			settings();
		}
		
		if(s.equals("Help")) {
			help();
		}
		
		if(s.equals("Add Grade Scale")) {
			addGradeScale();
		}
		
		if(s.equals("Delete Grade Scale")) {
			deleteGradeScale();
		}
		
		if(s.equals("View Grade Scale")) {
			viewGradeScale();
		}
		
		if(s.equals("Set Defaults")) {
			setDefaults();
		}
			
	}
	
	public static void main(String[] a) {
		new Gradebook();
	}
}
