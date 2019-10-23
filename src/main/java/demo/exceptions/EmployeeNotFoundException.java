package demo.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@SuppressWarnings("serial")
@ResponseStatus(HttpStatus.NOT_FOUND)
public class EmployeeNotFoundException extends Exception{
	public EmployeeNotFoundException(String message) {
		super(message);
	}
}
