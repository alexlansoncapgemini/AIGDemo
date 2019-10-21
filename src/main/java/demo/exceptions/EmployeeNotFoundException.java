package demo.exceptions;

import lombok.NoArgsConstructor;

@SuppressWarnings("serial")
@NoArgsConstructor
public class EmployeeNotFoundException extends Exception{
	public EmployeeNotFoundException(String message) {
		super(message);
	}
}
