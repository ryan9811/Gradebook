import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import java.awt.*;

/**
 * Ryan Hudson
 */
public class Gradebook extends JFrame {

	private JTextArea textArea;
	protected Container contentPane;
	private static DefaultTableModel cdtm;
	private static DefaultTableModel gdtm;
	protected JFrame frame;


	public Gradebook(){

		// Frame that holds everything
        frame = new JFrame(); 
        
        frame.setTitle("JTable Example"); 
        
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
        JButton addCourse = new JButton("Add Course");
        JButton removeCourse = new JButton("Remove Course");
        JButton editCourse = new JButton("Edit Course");
        JButton enterGrade = new JButton("Enter Grade");
        JButton finalizeGrades = new JButton("Finalize Grades");
        JButton saveChanges = new JButton("Save Changes");
        JButton manualOverride = new JButton("Manual Override");
        JLabel identifier = new JLabel("Enter Identifier: ");
        JTextField identifierInput = new JTextField();
        identifierInput.setPreferredSize(new Dimension(150,20));
        identifierInput.setEditable(true);
        identifierInput.setBounds(10,10,300,50);
        identifierInput.setSize(200, 20);
        buttons.add(addCourse);
        buttons.add(removeCourse);
        buttons.add(editCourse);
        buttons.add(enterGrade);
        buttons.add(finalizeGrades);
        buttons.add(manualOverride);
        buttons.add(saveChanges);
        buttons.add(identifier);
        buttons.add(identifierInput);
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
	
	public void addCourse(String title, String prof, String time, String identifier, String credits, String numGrade, String gMode, String fGrade, String year) {
		cdtm.addRow(new Object[] {title, prof, time, identifier, credits, numGrade, gMode, fGrade, year});
	}

	public static void main(String[] a) {
		new Gradebook();
	}

}