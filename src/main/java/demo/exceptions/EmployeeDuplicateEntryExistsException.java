package demo.exceptions;

@SuppressWarnings("serial")
public class EmployeeDuplicateEntryExistsException extends Exception{
	public EmployeeDuplicateEntryExistsException(String message) {
		super(message);
	}
}
