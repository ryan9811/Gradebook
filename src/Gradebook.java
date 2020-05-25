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
  
        // ------------------------Creating the table for the list of courses------------------------
        JTable courseList = new JTable();
        cdtm = new DefaultTableModel(0,0);
        
        String courseHeader[] = new String[] { "Course Title", "Professor", "Day/Time", "Identifier", "Credits", "Numeric Grade", "Grade Mode", "Final Grade", "Year" };
        
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
        courseList.getColumnModel().getColumn(2).setPreferredWidth(100);
        courseList.getColumnModel().getColumn(3).setPreferredWidth(90);
        courseList.getColumnModel().getColumn(4).setPreferredWidth(100);
        courseList.getColumnModel().getColumn(5).setPreferredWidth(150);
        courseList.getColumnModel().getColumn(6).setPreferredWidth(75);
        courseList.getColumnModel().getColumn(7).setPreferredWidth(75);
        courseList.getColumnModel().getColumn(8).setPreferredWidth(75);
        
        
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
        JLabel identifier = new JLabel("Enter Identifier: ");
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
        
        String gradeHeader[] = new String[] { "Course Title", "Identifier", "Assignment Code", "Category", "Category Weight", "Grade" };
        
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
		
		String fGrade = "n/a";
		
		String[] yearChoices = {"1", "2", "3", "4", "5"};
		String year = (String) JOptionPane.showInputDialog(null, "Select Year", "Course Master", JOptionPane.QUESTION_MESSAGE, null, yearChoices, yearChoices[0]);

		cdtm.addRow(new Object[] {title, prof, time, identifier, credits, numGrade, gMode, fGrade, year});
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
		
	}

}