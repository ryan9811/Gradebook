import javax.swing.*;
import javax.swing.table.TableColumn;

import java.awt.*;

/**
 * Ryan Hudson
 */
public class Gradebook extends JFrame {

	private JTextArea textArea;
	protected Container contentPane;


	public Gradebook(){

        JFrame f = new JFrame(); 
        
        // Frame Title 
        f.setTitle("JTable Example"); 
  
        // Data to be displayed in the JTable 
        String[][] data = { 
            { "Principles of Economics I", "Yezer, Anthony", "MW 12:15-1:35", "56789", "3", "76.8", "CC", "C", "1"  }, 
            { "French", "Anaye, Hadia", "TR 12:15-1:35", "52367", "3", "92.7", "CTP", "A-", "1"  },
            { "Spanish", "Anaye, Hadia", "MW 12:15-1:35", "52367", "3", "82.7", "P/NP", "P", "1"  } 
        }; 
  
        // Column Names 
        String[] columnNames = { "Course Title", "Professor", "Day/Time", "Identifier", "Credits", "Numeric Grade", "Grade Mode", "Final Grade", "Year" }; 
  
        // Initializing the JTable 
        JTable j = new JTable(data, columnNames);
        j.setShowVerticalLines(true);
        j.setColumnSelectionAllowed(false);
        j.getTableHeader().setReorderingAllowed(false);
        j.setEnabled(false);
  
        // adding it to JScrollPane 
        JScrollPane sp = new JScrollPane(j); 
        f.add(sp, BorderLayout.NORTH); 
        j.getColumnModel().getColumn(0).setPreferredWidth(200);
        j.getColumnModel().getColumn(1).setPreferredWidth(200);
        j.getColumnModel().getColumn(2).setPreferredWidth(100);
        j.getColumnModel().getColumn(3).setPreferredWidth(90);
        j.getColumnModel().getColumn(4).setPreferredWidth(100);
        j.getColumnModel().getColumn(5).setPreferredWidth(150);
        j.getColumnModel().getColumn(6).setPreferredWidth(75);
        j.getColumnModel().getColumn(7).setPreferredWidth(75);
        j.getColumnModel().getColumn(8).setPreferredWidth(75);

        // Frame Visible = true 
        f.setVisible(true); 
        
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
        f.add(buttons, BorderLayout.CENTER);
        
        String[][] data2 = {{"Principles of Economics I", "56789","1111","Quizzes","15%","91.5%"}};
        
        String[] columnNames2 = {"Course Title", "Identifier", "Assignment Code", "Category", "Category Weight", "Grade"};
        
        JTable grades = new JTable(data2, columnNames2);
        grades.setShowVerticalLines(true);
        grades.setColumnSelectionAllowed(false);
        grades.getTableHeader().setReorderingAllowed(false);
        grades.setEnabled(false);
        JScrollPane sp2 = new JScrollPane(grades); 
        f.add(sp2, BorderLayout.SOUTH);
        
        f.pack();

	}

	public static void main(String[] a) {
		new Gradebook();
	}

}