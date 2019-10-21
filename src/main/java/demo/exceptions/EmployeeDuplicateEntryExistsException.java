package demo.exceptions;

import lombok.NoArgsConstructor;

@SuppressWarnings("serial")
@NoArgsConstructor
public class EmployeeDuplicateEntryExistsException extends Exception{
	public EmployeeDuplicateEntryExistsException(String message) {
		super(message);
	}
}
