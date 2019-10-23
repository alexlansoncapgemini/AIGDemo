package db.test;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import demo.aspects.EmployeeGlobalExceptionHandler;
import demo.exceptions.EmployeeDuplicateEntryExistsException;
import demo.exceptions.EmployeeInvalidPasswordException;
import demo.exceptions.EmployeeInvalidRequestParameterException;
import demo.exceptions.EmployeeNotFoundException;
import demo.exceptions.EmployeeRequestOnNullObjectException;
import demo.responses.EmployeeResponseBodySuperclass;

//explicitly tests the exception handling methods in EmployeeGlobalExceptionHandler
public class EmployeeExceptionHandlerTester {
  @Mock
  private EmployeeGlobalExceptionHandler exceptionHandler;
  
  @Before
  public void initSetup() {
    MockitoAnnotations.initMocks(this);
  }
  
  @Test
  public void validateDuplicateEntryException() {
    ResponseEntity<EmployeeResponseBodySuperclass> resp = 
        exceptionHandler.handleDuplicateEntryExistsException(
            new EmployeeDuplicateEntryExistsException("duplicate entry test message"));
    
    assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
  }
  
  @Test
  public void validateNullObjectRequestException() {
    ResponseEntity<EmployeeResponseBodySuperclass> resp = 
        exceptionHandler.handleRequestOnNullObjectException(
            new EmployeeRequestOnNullObjectException("null object test message"));
    
    assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
  }
  
  @Test
  public void validateInvalidRequestParameterException() {
    ResponseEntity<EmployeeResponseBodySuperclass> resp = 
        exceptionHandler.handleInvalidRequestParameterException(
            new EmployeeInvalidRequestParameterException("invalid request parameter test message"));
    
    assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, resp.getStatusCode());
  }
  
  @Test
  public void validateNotFoundException() {
    ResponseEntity<EmployeeResponseBodySuperclass> resp = 
        exceptionHandler.handleNotFoundException(
            new EmployeeNotFoundException("not found test message"));
    
    assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
  }
  
  @Test
  public void validateInvalidPasswordException() {
    ResponseEntity<EmployeeResponseBodySuperclass> resp = 
        exceptionHandler.handleInvalidPasswordException(
            new EmployeeInvalidPasswordException("invalid password test message"));
    
    assertEquals(HttpStatus.CONFLICT, resp.getStatusCode());
  }
}
