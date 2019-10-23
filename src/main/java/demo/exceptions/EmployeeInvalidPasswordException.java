package demo.exceptions;

@SuppressWarnings("serial")
public class EmployeeInvalidPasswordException extends Exception{
	public EmployeeInvalidPasswordException(String message) {
		super(message);
	}
}
