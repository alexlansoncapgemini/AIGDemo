package demo.exceptions;

@SuppressWarnings("serial")
public class EmployeeInvalidRequestParameterException extends Exception{
	public EmployeeInvalidRequestParameterException(String message) {
		super(message);
	}
}
