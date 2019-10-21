package demo.exceptions;

import lombok.NoArgsConstructor;

@SuppressWarnings("serial")
@NoArgsConstructor
public class EmployeeInvalidRequestParameterException extends Exception{
	public EmployeeInvalidRequestParameterException(String message) {
		super(message);
	}
}
