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
 */
public class Gradebook extends JFrame implements ActionListener {

	private JTextArea textArea;
	private JTextField identifierInput;
	private Container contentPane;
	private static DefaultTableModel cdtm;
	private static DefaultTableModel gdtm;
	private JTable courseList, gradeList;
	private int assignmentCode;
	private int courseCode;
	private JFrame frame;
	private Hashtable<String, ArrayList<String>> categories;
	
	private JFileChooser myJFileChooser = new JFileChooser(new File("."));
	
	public void saveTable() {
		if(myJFileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			saveTable(myJFileChooser.getSelectedFile());
		}
	}
	
	public void saveTable(File file) {
		try {
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
			out.writeObject(cdtm.getDataVector());
			out.writeObject(getColumnNamesC());
			out.writeObject(gdtm.getDataVector());
			out.writeObject(getColumnNamesG());
			out.writeObject(categories);
			out.close();		
		}
		
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public Vector<String> getColumnNamesC() {
		Vector<String> columnNames = new Vector<String>();
		for(int i = 0; i < courseList.getColumnCount(); i++)
			columnNames.add(courseList.getColumnName(i) + "");
		return columnNames;
	}
	
	public Vector<String> getColumnNamesG() {
		Vector<String> columnNames = new Vector<String>();
		for(int i = 0; i < gradeList.getColumnCount(); i++)
			columnNames.add(gradeList.getColumnName(i) + "");
		return columnNames;
	}
	
	public void loadTable() {
		if(myJFileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
			loadTable(myJFileChooser.getSelectedFile());
	}
	
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
	
	public void calculateGrade(String identifier) {
		// For a class
		// Figure out which categories have grades so far
		// Total up those categories weights
		// For each category, sum up points earned and total points, multiply by categoryWeight/sumCategoryWeightsUsed
		// Change the grade in the table
		
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

		String letGrade = numToLet(finalGrade);
		
		for(int i = 0; i < cdtm.getRowCount(); i++)
			if(cdtm.getValueAt(i, 3).equals(identifier)) {
				cdtm.setValueAt(finalGrade + "", i, 5);
				cdtm.setValueAt(letGrade, i, 7);
			}
	}
	
	public String numToLet(double grade) {
		
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

	public Gradebook(){

		// Frame that holds everything
        frame = new JFrame(); 
        
        frame.setTitle("Gradebook"); 
        
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
        
        JButton login = new JButton("Login");
//        login.setBackground(Color.BLUE);
//        login.setOpaque(true);
//        login.setBorderPainted(false);
        login.addActionListener(this);
        login.setSelected(true);
        
        // Create the textfield for allowing edits
        JLabel identifier = new JLabel("Identifier/Code: ");
        identifierInput = new JTextField();
        identifierInput.setPreferredSize(new Dimension(150,20));
        identifierInput.setEditable(true);
        identifierInput.setBounds(10,10,300,50);
        identifierInput.setSize(200, 20);
        
        // Add the buttons to the panel
        buttons.add(login);
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
        
        String gradeHeader[] = new String[] { "Course Title", "Identifier", "Assignment Code", "Category", "Category Weight", "Points Earned", "Total Points", "Grade" };
        
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
		
//		String identifier = JOptionPane.showInputDialog("Enter Course Identifier");
//		if(identifier == null) {
//			JOptionPane.showMessageDialog(null, "Action Cancelled");
//			return;
//		}
		
		courseCode += (int) (Math.random() * 50);
		String identifier = "C" + courseCode;
		
		String[] creditsChoices = {"0", "1", "2", "3", "4", "5"};
		String credits = (String) JOptionPane.showInputDialog(null, "Select Number of Credits", "Course Master", JOptionPane.QUESTION_MESSAGE, null, creditsChoices, creditsChoices[0]);
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
		
		String category = "";
		String catWeight = "";
		ArrayList<String> catsAndWeights = new ArrayList<String>();
		category = JOptionPane.showInputDialog("Enter a grade weight category.");
		if(category == null) {
			JOptionPane.showMessageDialog(null, "Action Cancelled");
			return;
		}
		catWeight = JOptionPane.showInputDialog("Enter Category Weight (ex. 15)");
		if(catWeight == null) {
			JOptionPane.showMessageDialog(null, "Action Cancelled");
			return;
		}
		catsAndWeights.add(category);
		catsAndWeights.add(catWeight);
		int yesNo = 0;
		while(yesNo == 0) {
			yesNo = JOptionPane.showConfirmDialog(null, "Would you like to enter another category?");
			if(yesNo == 0) {
				category = JOptionPane.showInputDialog("Enter Category Name");
				catWeight = JOptionPane.showInputDialog("Enter Category Weight (ex. 15)");
				catsAndWeights.add(category);
				catsAndWeights.add(catWeight);
			}
			else {
				JOptionPane.showMessageDialog(null, "Action Cancelled");
				return;
			}
		}
		categories.put(identifier, catsAndWeights);
		
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
		
		String[] yearChoices = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
		String year = (String) JOptionPane.showInputDialog(null, "Select Term", "Course Master", JOptionPane.QUESTION_MESSAGE, null, yearChoices, yearChoices[0]);
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
					removeAssociatedGrades();
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
					removeAssociatedGrades();
					gdtm.removeRow(i);
				}
				else {
					JOptionPane.showMessageDialog(null, "Action Cancelled");
					return;
				}
			}
		}
		
		
	}
	
	public void finalizeGrades() {
		double qualityPoints = 0;
		
		boolean year1f = true;
		boolean year2f = true;
		boolean year3f = true;
		boolean year4f = true;
		boolean year5f = true;
		boolean year6f = true;
		boolean year7f = true;
		boolean year8f = true;
		boolean year9f = true;
		boolean year10f = true;
		
		
		for(int i = 0; i < cdtm.getRowCount(); i++) {
			if(cdtm.getValueAt(i, 8).equals("1") && !cdtm.getValueAt(i, 8).equals("Finalized"))
				year1f = false;
			if(cdtm.getValueAt(i, 8).equals("2") && !cdtm.getValueAt(i, 8).equals("Finalized"))
				year2f = false;
			if(cdtm.getValueAt(i, 8).equals("3") && !cdtm.getValueAt(i, 8).equals("Finalized"))
				year3f = false;
			if(cdtm.getValueAt(i, 8).equals("4") && !cdtm.getValueAt(i, 8).equals("Finalized"))
				year4f = false;
			if(cdtm.getValueAt(i, 8).equals("5") && !cdtm.getValueAt(i, 8).equals("Finalized"))
				year5f = false;
			if(cdtm.getValueAt(i, 8).equals("6") && !cdtm.getValueAt(i, 8).equals("Finalized"))
				year6f = false;
			if(cdtm.getValueAt(i, 8).equals("7") && !cdtm.getValueAt(i, 8).equals("Finalized"))
				year7f = false;
			if(cdtm.getValueAt(i, 8).equals("8") && !cdtm.getValueAt(i, 8).equals("Finalized"))
				year8f = false;
			if(cdtm.getValueAt(i, 8).equals("9") && !cdtm.getValueAt(i, 8).equals("Finalized"))
				year9f = false;
			if(cdtm.getValueAt(i, 8).equals("10") && !cdtm.getValueAt(i, 8).equals("Finalized"))
				year10f = false;
		}
		
		int year = 0;
		boolean[] yearsFinalized = {year1f, year2f, year3f, year4f, year5f, year6f, year7f, year8f, year9f, year10f};
		for(int i = 0; i < yearsFinalized.length; i++)
			if(!yearsFinalized[i])
				year = i + 1;
		
		int creditSum = 0;
		double qualitySum = 0;
		int pnpSum = 0;
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
				pnpSum += Integer.parseInt((String)cdtm.getValueAt(i, 4));
				cdtm.setValueAt("Finalized", i, 9);
			}
			else if(cdtm.getValueAt(i, 8).equals(year + "") && cdtm.getValueAt(i, 6).equals("Notation") && cdtm.getValueAt(i, 7).equals("TR")) {
				creditSum += Integer.parseInt((String)cdtm.getValueAt(i, 4));
				pnpSum += Integer.parseInt((String)cdtm.getValueAt(i, 4));
				cdtm.setValueAt("Finalized", i, 9);
			}
		}
		if(((String) cdtm.getValueAt(cdtm.getRowCount() - 1, 9)).equalsIgnoreCase("Finalized")) {
				cdtm.addRow(new Object[] {"", "", "", "", "", "", "", "", "", ""});		
				cdtm.addRow(new Object[] {"Term Credits", creditSum, "Term Quality Points", qualitySum, "", "", "", "", "GPA", qualitySum / (creditSum - pnpSum)});
				
				double allQualitySum = 0;
				int allCreditSum = 0;
				for(int i = 0; i < cdtm.getRowCount(); i++)
					if(((String) cdtm.getValueAt(i, 0)).equalsIgnoreCase("Term Credits")) {
						allQualitySum += Double.parseDouble(cdtm.getValueAt(i, 3) + "");
						allCreditSum += Integer.parseInt(cdtm.getValueAt(i, 1) + "");
					}
				
				cdtm.addRow(new Object[] {"Total Credits", allCreditSum, "Total Quality Points", allQualitySum, "", "", "", "", "GPA", allQualitySum / (allCreditSum - pnpSum)});		
		}
		
	}
	
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
		
		int row = 0;
		for(int i = 0; i < cdtm.getRowCount(); i++) {
			if(cdtm.getValueAt(i, 3).equals(identifierInput.getText()))
				row = i;
		}
		
		String[] editChoices = {"Course Title", "Professor", "Day/Time", "Credits", "Grade Mode", "Final Grade", "Term"};
		String edit = (String) JOptionPane.showInputDialog(null, "Select Field for Edit", "Course Master", JOptionPane.QUESTION_MESSAGE, null, editChoices, editChoices[0]);
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
			String[] gModeChoices = {"Letter", "P/NP"};
			String gMode = (String) JOptionPane.showInputDialog(null, "Select Grade Mode", "Course Edit Master", JOptionPane.QUESTION_MESSAGE, null, gModeChoices, gModeChoices[0]);
			if(gMode == null) {
				JOptionPane.showMessageDialog(null, "Action Cancelled");
				return;
			}
			else cdtm.setValueAt(gMode, row, 6);
		}
		
		if(edit.equalsIgnoreCase("Final Grade")) {
			String[] fGradeChoicesC = {"In Progress", "A","A-","B+","B","B-","C+","C","C-","D+","D","D-","F"};
			String[] fGradeChoicesP = {"In Progress", "P", "NP"};
			String fGrade;
			if(cdtm.getValueAt(row, 6).equals("Letter"))
				fGrade = (String) JOptionPane.showInputDialog(null, "Select Final Grade", "Course Edit Master", JOptionPane.QUESTION_MESSAGE, null, fGradeChoicesC, fGradeChoicesC[0]);
			else
				fGrade = (String) JOptionPane.showInputDialog(null, "Select Final Grade", "Course Edit Master", JOptionPane.QUESTION_MESSAGE, null, fGradeChoicesP, fGradeChoicesP[0]);
			if(fGrade == null) {
				JOptionPane.showMessageDialog(null, "Action Cancelled");
				return;
			}
			else cdtm.setValueAt(fGrade, row, 7);
			
		}
		
		if(edit.equalsIgnoreCase("Term")) {
			String[] yearChoices = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
			String year = (String) JOptionPane.showInputDialog(null, "Select Term", "Course Edit Master", JOptionPane.QUESTION_MESSAGE, null, yearChoices, yearChoices[0]);
			if(year == null) {
				JOptionPane.showMessageDialog(null, "Action Cancelled");
				return;
			}
			else cdtm.setValueAt(year, row, 8);
		}
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
		
		String grade = (Double.parseDouble(pointsEarned) / Double.parseDouble(totalPoints) * 100 + "");
		
		gdtm.addRow(new Object[] {courseTitle, identifier, code, category, catWeight, pointsEarned, totalPoints, grade});
		
		calculateGrade(identifier);
		
	}
	
	// removes grades associated with a deleted course
	public void removeAssociatedGrades() {
		return;
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
			if(JOptionPane.showConfirmDialog(null, "Are you sure you want to finalize grades? This action cannot be reversed.") == 0) {
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
				}
				else {
					JOptionPane.showMessageDialog(null, "Action Cancelled");
					return;
				}
			}
			else {
				courseList.setEnabled(false);
				for(int i = 0; i < cdtm.getRowCount(); i++)
					courseList.changeSelection(i, 0, true, false);
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