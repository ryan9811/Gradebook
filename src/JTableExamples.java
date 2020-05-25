// Packages to import 
import javax.swing.JFrame; 
import javax.swing.JScrollPane; 
import javax.swing.JTable; 
  
public class JTableExamples { 
    // frame 
    JFrame f; 
    // Table 
    JTable j; 
  
    // Constructor 
    JTableExamples() 
    { 
        // Frame initiallization 
        JFrame f = new JFrame(); 
  
        // Frame Title 
        f.setTitle("JTable Example"); 
  
        // Data to be displayed in the JTable 
        String[][] data = { 
            { "Principles of Economics I", "Yezer, Anthony", "12:15-1:35", "56789", "3", "A" }, 
            { "French", "Anaye, Hadia", "12:15-1:35", "52367", "3", "A-" } 
        }; 
  
        // Column Names 
        String[] columnNames = { "Course Title", "Professor", "Time", "Identifier", "Credit Hours", "Current Grade" }; 
  
        // Initializing the JTable 
        j = new JTable(data, columnNames); 
        j.setBounds(30, 40, 200, 300); 
        j.setEnabled(false);
  
        // adding it to JScrollPane 
        JScrollPane sp = new JScrollPane(j); 
        f.add(sp); 
        // Frame Size 
        f.setSize(500, 200); 
        // Frame Visible = true 
        f.setVisible(true); 
    } 
  
    // Driver  method 
    public static void main(String[] args) 
    { 
        new JTableExamples(); 
    } 
} 