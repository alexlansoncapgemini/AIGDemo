package demo.exceptions;

import lombok.NoArgsConstructor;

@SuppressWarnings("serial")
@NoArgsConstructor
public class EmployeeRequestOnNullObjectException extends Exception{
	public EmployeeRequestOnNullObjectException(String message) {
		super(message);
	}
}
