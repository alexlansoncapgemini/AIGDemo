package demo.exceptions;

@SuppressWarnings("serial")
public class EmployeeRequestOnNullObjectException extends Exception{
	public EmployeeRequestOnNullObjectException(String message) {
		super(message);
	}
}
