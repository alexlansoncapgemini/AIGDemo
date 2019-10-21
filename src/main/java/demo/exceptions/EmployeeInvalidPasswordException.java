package demo.exceptions;

import lombok.NoArgsConstructor;

@SuppressWarnings("serial")
@NoArgsConstructor
public class EmployeeInvalidPasswordException extends Exception{
	public EmployeeInvalidPasswordException(String message) {
		super(message);
	}
}
