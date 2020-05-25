import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Ryan Hudson
 */
public class Gradebook extends JFrame implements ActionListener {

	private JTextArea textArea;
	private static JTextField identifierInput;
	protected Container contentPane;
	private static DefaultTableModel cdtm;
	private static DefaultTableModel gdtm;
	protected JFrame frame;


	public Gradebook(){

		// Frame that holds everything
        frame = new JFrame(); 
        
        frame.setTitle("Gradebook"); 
        
        frame.setVisible(true); 
        
        frame.setResizable(false);
        
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);;
  
        // ------------------------Creating the table for the list of courses------------------------
        JTable courseList = new JTable();
        cdtm = new DefaultTableModel(0,0);
        
        String courseHeader[] = new String[] { "Course Title", "Professor", "Day/Time", "Identifier", "Credits", "Numeric Grade", "Grade Mode", "Final Grade", "Year", "Status" };
        
        cdtm.setColumnIdentifiers(courseHeader);
        courseList.setModel(cdtm);
        
        courseList.setShowVerticalLines(true);
        courseList.setColumnSelectionAllowed(false);
        courseList.getTableHeader().setReorderingAllowed(false);
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
        
        JButton removeCourse = new JButton("Remove Course");
        removeCourse.addActionListener(this);
        
        JButton editCourse = new JButton("Edit Course");
        editCourse.addActionListener(this);
        
        JButton enterGrade = new JButton("Enter Grade");
        enterGrade.addActionListener(this);
        
        JButton finalizeGrades = new JButton("Finalize Grades");
        finalizeGrades.addActionListener(this);
        
        JButton saveChanges = new JButton("Save Changes");
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
        JTable gradeList = new JTable();
        gdtm = new DefaultTableModel(0,0);
        
        String gradeHeader[] = new String[] { "Course Title", "Identifier", "Assignment Code", "Category", "Category Weight", "Points Earned", "Total Points", "Grade" };
        
        gdtm.setColumnIdentifiers(gradeHeader);
        gradeList.setModel(gdtm);
        
        gradeList.setShowVerticalLines(true);
        gradeList.setColumnSelectionAllowed(false);
        gradeList.getTableHeader().setReorderingAllowed(false);
        gradeList.setEnabled(false);
        
        JScrollPane spg = new JScrollPane(gradeList); 
        frame.add(spg, BorderLayout.SOUTH);
        
        frame.pack();

	}
	
	public void addCourse() {	
		String title = JOptionPane.showInputDialog("Enter Course Title");
		
		String prof = JOptionPane.showInputDialog("Enter Professor Name");
		
		String time = JOptionPane.showInputDialog("Enter Course Day/Time");
		
		String identifier = JOptionPane.showInputDialog("Enter Course Identifier");
		
		String credits = JOptionPane.showInputDialog("Enter Number of Credits");
		
		String numGrade = "n/a";
		
		String[] gModeChoices = {"C", "P/NP"};
		String gMode = (String) JOptionPane.showInputDialog(null, "Select Grade Mode", "Course Master", JOptionPane.QUESTION_MESSAGE, null, gModeChoices, gModeChoices[0]);
		
		String[] fGradeChoicesC = {"In Progress", "A","A-","B+","B","B-","C+","C","C-","D+","D","D-","F"};
		String[] fGradeChoicesP = {"In Progress", "P", "NP"};
		String fGrade;
		if(gMode.equalsIgnoreCase("C"))
			fGrade = (String) JOptionPane.showInputDialog(null, "Select Final Grade", "Course Master", JOptionPane.QUESTION_MESSAGE, null, fGradeChoicesC, fGradeChoicesC[0]);
		else
			fGrade = (String) JOptionPane.showInputDialog(null, "Select Final Grade", "Course Master", JOptionPane.QUESTION_MESSAGE, null, fGradeChoicesP, fGradeChoicesP[0]);
		
		String[] yearChoices = {"1", "2", "3", "4", "5"};
		String year = (String) JOptionPane.showInputDialog(null, "Select Year", "Course Master", JOptionPane.QUESTION_MESSAGE, null, yearChoices, yearChoices[0]);
		
		String status;
		if(fGrade.equalsIgnoreCase("In Progress")) 
			status = "In Progress";
		else status = "Finalized (Manual)";

		cdtm.addRow(new Object[] {title, prof, time, identifier, credits, numGrade, gMode, fGrade, year, status});
	}
	
	public void removeCourse() {
		for(int i = 0; i < cdtm.getRowCount(); i++) {
			if(cdtm.getValueAt(i, 3).equals(identifierInput.getText())) {
				String courseName = (String) cdtm.getValueAt(i, 0);
				if(JOptionPane.showConfirmDialog(null, "Are you sure you want to delete " + courseName + "? This operation cannot be undone.") == 0) {
					removeAssociatedGrades();
					cdtm.removeRow(i);
				}
				else return;
			}
		}
	}
	
	public void finalizeGrades() {
		double qualityPoints = 0;
		
		boolean year1f = true;
		boolean year2f = true;
		boolean year3f = true;
		boolean year4f = true;
		
		for(int i = 0; i < cdtm.getRowCount(); i++) {
			if(cdtm.getValueAt(i, 8).equals("1") && !cdtm.getValueAt(i, 8).equals("Finalized"))
				year1f = false;
			if(cdtm.getValueAt(i, 8).equals("2") && !cdtm.getValueAt(i, 8).equals("Finalized"))
				year2f = false;
			if(cdtm.getValueAt(i, 8).equals("3") && !cdtm.getValueAt(i, 8).equals("Finalized"))
				year3f = false;
			if(cdtm.getValueAt(i, 8).equals("4") && !cdtm.getValueAt(i, 8).equals("Finalized"))
				year4f = false;
		}
		
		int year = 0;
		boolean[] yearsFinalized = {year1f, year2f, year3f, year4f};
		for(int i = 0; i < yearsFinalized.length; i++)
			if(!yearsFinalized[i])
				year = i + 1;
		
		int creditSum = 0;
		double qualitySum = 0;
		for(int i = 0; i < cdtm.getRowCount(); i++) {
			if(cdtm.getValueAt(i, 8).equals(year + "")) {
				qualitySum += Integer.parseInt((String)cdtm.getValueAt(i, 4)) * letToQual((String)cdtm.getValueAt(i, 7));
				creditSum += Integer.parseInt((String)cdtm.getValueAt(i, 4));
				cdtm.setValueAt("Finalized", i, 9);
			}
		}
		if(((String) cdtm.getValueAt(cdtm.getRowCount() - 1, 9)).equalsIgnoreCase("Finalized"))
				cdtm.addRow(new Object[] {"", "", "", "", "", "", "", "", "GPA", qualitySum / creditSum});
		
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
	
	// removes grades associated with a deleted course
	public void removeAssociatedGrades() {
		return;
	}

	public static void main(String[] a) {
		new Gradebook();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String s = e.getActionCommand();
		
		if(s.equalsIgnoreCase("Add Course")) {
			addCourse();
		}
		
		if(s.equalsIgnoreCase("Remove Course")) {
			removeCourse();
		}
		
		if(s.equalsIgnoreCase("Finalize Grades")) {
			finalizeGrades();
		}
		
	}

}