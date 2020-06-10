import javax.swing.JOptionPane;

public enum Errors {
	
	AER1("AER1", "Entered Term is Finalized"),
	AER2("AER2", "Must Finalize Previous Term"),
	AER3("AER3", "Category Name Cannot Contain Numbers"),
	AER4("AER4", "Category Name Already Exists"),
	AER5("AER5", "Cannot Have Negative Weight"),
	AER6("AER6", "Must Create a Grade Scale"),
	AER7("AER7", "Element Does Not Exist"),
	AER8("AER8", "Cannot Remove Finalized Course"),
	AER9("AER9", "Cannot Edit Finalized Course"),
	EG1("EG1", "Negative Values Not Accepted"),
	EG2("EG2", "No Courses Available"),
	EG3("EG3", "Must Add Category"),
	FG1("FG1", "Cannot Finalize with Final Grade = In Progress"),
	FG2("FG2", "All Courses Finalized"),
	SS1("SS1", "Cannot Have Negative Bonus"),
	SS2("SS2", "Invalid Grade Scale"),
	SS3("SS3", "No Grade Scales to Delete"),
	SS4("SS4", "Grade Scale In Use"),
	ML1("ML1", "Missing Information"),
	ML2("ML2", "Number Format Exception");
	
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
}
