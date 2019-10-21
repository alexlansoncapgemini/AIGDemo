package demo.exceptions;

import lombok.NoArgsConstructor;

@SuppressWarnings("serial")
@NoArgsConstructor
public class EmployeeServerErrorException extends Exception{
	public EmployeeServerErrorException(String message) {
		super(message);
	}
}
