package demo.aspects;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import demo.exceptions.EmployeeDuplicateEntryExistsException;
import demo.exceptions.EmployeeInvalidPasswordException;
import demo.exceptions.EmployeeInvalidRequestParameterException;
import demo.exceptions.EmployeeNotFoundException;
import demo.exceptions.EmployeeRequestOnNullObjectException;
import demo.responses.EmployeeResponseBodySuperclass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class EmployeeGlobalExceptionHandler {
	
	//thrown when trying to add an employee that already exists in the database
	@ExceptionHandler(EmployeeDuplicateEntryExistsException.class)
	public final ResponseEntity<EmployeeResponseBodySuperclass> handleDuplicateEntryExistsException(Exception ex){
		EmployeeResponseBodySuperclass response = new EmployeeResponseBodySuperclass();
		response.setStatusCode(HttpStatus.BAD_REQUEST.value());
		response.setStatusName(HttpStatus.BAD_REQUEST.name());
		response.setStatusDescription(ex.getMessage());
		log.debug(response.getStatusDescription());
		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	}
	
	//generally thrown as a result of validating a null employee argument
	@ExceptionHandler(EmployeeRequestOnNullObjectException.class)
	public final ResponseEntity<EmployeeResponseBodySuperclass> handleRequestOnNullObjectException(Exception ex){
		EmployeeResponseBodySuperclass response = new EmployeeResponseBodySuperclass();
		response.setStatusCode(HttpStatus.BAD_REQUEST.value());
		response.setStatusName(HttpStatus.BAD_REQUEST.name());
		response.setStatusDescription(ex.getMessage());
		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	}
	
	//generally thrown as a result of validating invalid employee field data (e.g. age, name, etc.)
	@ExceptionHandler(EmployeeInvalidRequestParameterException.class)
	public final ResponseEntity<EmployeeResponseBodySuperclass> handleInvalidRequestParameterException(
			Exception ex){
		EmployeeResponseBodySuperclass response = new EmployeeResponseBodySuperclass();
		response.setStatusCode(HttpStatus.UNPROCESSABLE_ENTITY.value());
		response.setStatusName(HttpStatus.UNPROCESSABLE_ENTITY.name());
		response.setStatusDescription(ex.getMessage());
		return new ResponseEntity<>(response, HttpStatus.UNPROCESSABLE_ENTITY);
	}
	
	//generally thrown when the controller tries to access something that doesn't exist in the database
	@ExceptionHandler(EmployeeNotFoundException.class)
	public final ResponseEntity<EmployeeResponseBodySuperclass> handleNotFoundException(
			Exception ex){
		EmployeeResponseBodySuperclass response = new EmployeeResponseBodySuperclass();
		response.setStatusCode(HttpStatus.NOT_FOUND.value());
		response.setStatusName(HttpStatus.NOT_FOUND.name());
		response.setStatusDescription(ex.getMessage());
		return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
	}
	
	//thrown when password authentication is required, but the password given is incorrect
	@ExceptionHandler(EmployeeInvalidPasswordException.class)
	public final ResponseEntity<EmployeeResponseBodySuperclass> handleInvalidPasswordException(Exception ex){
		EmployeeResponseBodySuperclass response = new EmployeeResponseBodySuperclass();
		response.setStatusCode(HttpStatus.CONFLICT.value());	//this is a conflict code, because the 401 error
		response.setStatusName(HttpStatus.CONFLICT.name());		//code requires the response to contain "a
		response.setStatusDescription(ex.getMessage());			//WWW-Authenticate header field containing a 
																//challenge applicable to the requested resource"
		return new ResponseEntity<>(response, HttpStatus.CONFLICT);
	}
}
