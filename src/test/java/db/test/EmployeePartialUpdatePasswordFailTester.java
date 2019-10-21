package db.test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException.Conflict;
import org.springframework.web.client.HttpClientErrorException.NotFound;
import org.springframework.web.client.HttpClientErrorException.UnprocessableEntity;

import com.openpojo.reflection.PojoClass;
import com.openpojo.reflection.impl.PojoClassFactory;
import com.openpojo.validation.Validator;
import com.openpojo.validation.ValidatorBuilder;
import com.openpojo.validation.rule.impl.GetterMustExistRule;
import com.openpojo.validation.rule.impl.SetterMustExistRule;
import com.openpojo.validation.test.impl.GetterTester;
import com.openpojo.validation.test.impl.SetterTester;

import demo.beans.Employee;
import demo.controller.EmployeeRestController;
import demo.dao.EmployeeDatabaseImplementation;
import demo.exceptions.EmployeeDuplicateEntryExistsException;
import demo.exceptions.EmployeeInvalidPasswordException;
import demo.exceptions.EmployeeInvalidRequestParameterException;
import demo.exceptions.EmployeeNotFoundException;
import demo.exceptions.EmployeeRequestOnNullObjectException;
import demo.responses.EmployeeResponseBody;
import demo.responses.EmployeeResponseBodySuperclass;
import demo.service.EmployeeServiceImplementation;
import demo.util.EmployeeBeanModifier;
import demo.util.EmployeeMessageManager;
import demo.util.EmployeeValidator;
import demo.util.EmployeeMessageManager.employeeValidationResults;

public class EmployeePartialUpdatePasswordFailTester {
	@Mock
	RestTemplate restTemplate;
	static List<Employee> employeelist = new ArrayList<>();
	
	final static EmployeeDatabaseImplementation databaseImpl = new EmployeeDatabaseImplementation();
	final static EmployeeServiceImplementation serviceImpl = new EmployeeServiceImplementation(databaseImpl);
	final static EmployeeRestController restController = new EmployeeRestController(serviceImpl);
	
	@Before
	public void initSetup() {
		MockitoAnnotations.initMocks(this);
		restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
		employeelist.add(EmployeeBeanModifier.convertFromDetails(Optional.ofNullable(
				EmployeeBeanModifier.employeeStringParserNoId(
						EmployeeMessageManager.getVal("testemployeeUpdatePassword")))));
		employeelist.add(EmployeeBeanModifier.convertFromDetails(Optional.ofNullable(
				EmployeeBeanModifier.employeeStringParserNoId(
						EmployeeMessageManager.getVal("testemployeeServiceUpdatePassword")))));
		employeelist.add(EmployeeBeanModifier.convertFromDetails(Optional.ofNullable(
				EmployeeBeanModifier.employeeStringParserNoId(
						EmployeeMessageManager.getVal("testemployeeControllerUpdatePassword")))));
		employeelist.add(EmployeeBeanModifier.convertFromDetails(Optional.ofNullable(
				EmployeeBeanModifier.employeeStringParserNoId(
						EmployeeMessageManager.getVal("testemployeeControllerUpdatePasswordMethod")))));
	}
	
	@Test
	public void validateModels() {
		PojoClass employeetest = PojoClassFactory.getPojoClass(Employee.class);
		Validator validator = ValidatorBuilder.create()
				.with(new GetterMustExistRule(), new SetterMustExistRule())
				.with(new GetterTester(), new SetterTester())
				.build();
		validator.validate(employeetest);
	}
	
	@Test(expected = EmployeeInvalidRequestParameterException.class)
	public void validateServicePasswordUpdateFailInvalid() 
			throws EmployeeInvalidPasswordException, 
			EmployeeInvalidRequestParameterException, 
			EmployeeNotFoundException, 
			EmployeeRequestOnNullObjectException, 
			EmployeeDuplicateEntryExistsException {
		Employee testemployeeServicePassword = employeelist.get(1);
		
		if(!EmployeeValidator.validateIsEmployeeFirstNameInDatabase(
				databaseImpl, testemployeeServicePassword.getFirst_name())) {
			serviceImpl.newEmployee(testemployeeServicePassword);
		}
		
		assertTrue(EmployeeValidator.validateIsEmployeeFirstNameInDatabase(
				databaseImpl, testemployeeServicePassword.getFirst_name()));
		
		testemployeeServicePassword = serviceImpl.getEmployeeByFirstName(testemployeeServicePassword.getFirst_name()).get(0);
		
		serviceImpl.updateEmployeePassWord(
				testemployeeServicePassword.getEmployee_id(), testemployeeServicePassword.getPass_word(), "new pass");
	}
	
	@Test(expected = EmployeeInvalidPasswordException.class)
	public void validateServicePasswordUpdateFailWrongPassword() 
			throws EmployeeInvalidPasswordException, 
			EmployeeInvalidRequestParameterException, 
			EmployeeNotFoundException, 
			EmployeeRequestOnNullObjectException, 
			EmployeeDuplicateEntryExistsException {
		Employee testemployeeServicePassword = employeelist.get(1);
		
		if(!EmployeeValidator.validateIsEmployeeFirstNameInDatabase(
				databaseImpl, testemployeeServicePassword.getFirst_name())) {
			serviceImpl.newEmployee(testemployeeServicePassword);
		}
		
		assertTrue(EmployeeValidator.validateIsEmployeeFirstNameInDatabase(
				databaseImpl, testemployeeServicePassword.getFirst_name()));
		
		testemployeeServicePassword = serviceImpl.getEmployeeByFirstName(
				testemployeeServicePassword.getFirst_name()).get(0);
		
		serviceImpl.updateEmployeePassWord(
				testemployeeServicePassword.getEmployee_id(), "wrongpassword", "newpass");
	}
	
	@Test(expected = EmployeeNotFoundException.class)
	public void validateServicePasswordUpdateFailMissing() 
			throws EmployeeInvalidPasswordException, 
			EmployeeInvalidRequestParameterException, 
			EmployeeNotFoundException {
		Employee testemployeeServicePassword = employeelist.get(1);
		
		serviceImpl.updateEmployeePassWord(
				testemployeeServicePassword.getEmployee_id(), 
				testemployeeServicePassword.getPass_word(), "newpass");
	}
	
	@Test
	public void validatePasswordControllerUpdateFailInvalid() 
			throws EmployeeRequestOnNullObjectException, 
			EmployeeInvalidRequestParameterException, 
			EmployeeDuplicateEntryExistsException {
		Employee testemployeeControllerPassword = employeelist.get(2);
		employeeValidationResults validationResult = 
				EmployeeValidator.validateIsThisEmployeeValid(
						Optional.ofNullable(testemployeeControllerPassword));
		
		assertEquals(employeeValidationResults.SUCCESS, validationResult);
		
		if(!EmployeeValidator.validateIsEmployeeFirstNameInDatabase(
				databaseImpl, testemployeeControllerPassword.getFirst_name())) {
			serviceImpl.newEmployee(testemployeeControllerPassword);
		}
		
		assertTrue(EmployeeValidator.validateIsEmployeeFirstNameInDatabase(
				databaseImpl, testemployeeControllerPassword.getFirst_name()));
		
		testemployeeControllerPassword = serviceImpl.getEmployeeByFirstName(
				testemployeeControllerPassword.getFirst_name()).get(0);
		
		int eid = testemployeeControllerPassword.getEmployee_id();
		String currentPassword = testemployeeControllerPassword.getPass_word();
		
		ThrowingCallable resp = () ->
			restTemplate.patchForObject(EmployeeMessageManager.getVal("baseUrl") + 
					EmployeeMessageManager.getVal("partialUpdatePasswordMapping"), 
					null, EmployeeResponseBody.class, eid, currentPassword, "new pass");
		
		assertThatCode(resp).isInstanceOf(UnprocessableEntity.class);
	}
	
	@Test
	public void validatePasswordControllerUpdateFailWrongPassword() 
			throws EmployeeRequestOnNullObjectException, 
			EmployeeInvalidRequestParameterException, 
			EmployeeDuplicateEntryExistsException {
		Employee testemployeeControllerPassword = employeelist.get(2);
		employeeValidationResults validationResult = 
				EmployeeValidator.validateIsThisEmployeeValid(
						Optional.ofNullable(testemployeeControllerPassword));
		
		assertEquals(employeeValidationResults.SUCCESS, validationResult);
		
		if(!EmployeeValidator.validateIsEmployeeFirstNameInDatabase(
				databaseImpl, testemployeeControllerPassword.getFirst_name())) {
			serviceImpl.newEmployee(testemployeeControllerPassword);
		}
		
		assertTrue(EmployeeValidator.validateIsEmployeeFirstNameInDatabase(
				databaseImpl, testemployeeControllerPassword.getFirst_name()));
		
		testemployeeControllerPassword = serviceImpl.getEmployeeByFirstName(
				testemployeeControllerPassword.getFirst_name()).get(0);
		
		int eid = testemployeeControllerPassword.getEmployee_id();
		
		ThrowingCallable resp = () ->
			restTemplate.patchForObject(EmployeeMessageManager.getVal("baseUrl") + 
				EmployeeMessageManager.getVal("partialUpdatePasswordMapping"), 
				null, EmployeeResponseBodySuperclass.class, eid, "wrongpassword", "newpass");
	
		assertThatCode(resp).isInstanceOf(Conflict.class);
	}
	
	@Test(expected = NotFound.class)
	public void validatePasswordControllerUpdateFailMissing() {
		Employee testPasswordController = employeelist.get(2);
		employeeValidationResults validationResult = 
				EmployeeValidator.validateIsThisEmployeeValid(Optional.ofNullable(testPasswordController));
		
		assertEquals(employeeValidationResults.SUCCESS, validationResult);
		
		String currentPassword = testPasswordController.getPass_word();
		
		restTemplate.patchForObject(EmployeeMessageManager.getVal("baseUrl") + 
					EmployeeMessageManager.getVal("partialUpdatePasswordMapping"), 
					null, EmployeeResponseBody.class, 0, currentPassword, "newpass"); //nonexistant id value 0
	}
	
	@Test
	public void validatePasswordControllerUpdateMethodFailInvalid() 
			throws EmployeeRequestOnNullObjectException, 
			EmployeeInvalidRequestParameterException, 
			EmployeeDuplicateEntryExistsException {
		Employee testemployeeControllerPasswordMethod = employeelist.get(3);
		
		employeeValidationResults validationResult = 
				EmployeeValidator.validateIsThisEmployeeValid(
						Optional.ofNullable(testemployeeControllerPasswordMethod));
		
		assertEquals(employeeValidationResults.SUCCESS, validationResult);
		
		if(!EmployeeValidator.validateIsEmployeeFirstNameInDatabase(
				databaseImpl, testemployeeControllerPasswordMethod.getFirst_name())) {
			serviceImpl.newEmployee(testemployeeControllerPasswordMethod);
		}
		
		assertTrue(EmployeeValidator.validateIsEmployeeFirstNameInDatabase(
				databaseImpl, testemployeeControllerPasswordMethod.getFirst_name()));
		
		final Employee testPasswordControllerMethodTest = serviceImpl.getEmployeeByFirstName(
				testemployeeControllerPasswordMethod.getFirst_name()).get(0);
		
		ThrowingCallable resp = () ->
				restController.updateEmployeePassword(
						testPasswordControllerMethodTest.getEmployee_id(), 
						testPasswordControllerMethodTest.getPass_word(), "new pass");
		
		assertThatCode(resp).isInstanceOf(EmployeeInvalidRequestParameterException.class);
	}
	
	@Test
	public void validatePasswordControllerUpdateMethodFailWrongPassword() 
			throws EmployeeInvalidRequestParameterException, 
			EmployeeNotFoundException, 
			EmployeeRequestOnNullObjectException, 
			EmployeeDuplicateEntryExistsException {
		Employee testemployeeControllerPasswordMethod = employeelist.get(3);
		
		employeeValidationResults validationResult = 
				EmployeeValidator.validateIsThisEmployeeValid(
						Optional.ofNullable(testemployeeControllerPasswordMethod));
		
		assertEquals(employeeValidationResults.SUCCESS, validationResult);
		
		if(!EmployeeValidator.validateIsEmployeeFirstNameInDatabase(
				databaseImpl, testemployeeControllerPasswordMethod.getFirst_name())) {
			serviceImpl.newEmployee(testemployeeControllerPasswordMethod);
		}
		
		assertTrue(EmployeeValidator.validateIsEmployeeFirstNameInDatabase(
				databaseImpl, testemployeeControllerPasswordMethod.getFirst_name()));
		
		final Employee testPasswordControllerMethodTest = serviceImpl.getEmployeeByFirstName(
				testemployeeControllerPasswordMethod.getFirst_name()).get(0);
		
		ThrowingCallable resp = () ->
				restController.updateEmployeePassword(
						testPasswordControllerMethodTest.getEmployee_id(), //it wanted a final variable
						"wrongpassword", "newpass");					   //so I gave it one
		
		assertThatCode(resp).isInstanceOf(EmployeeInvalidPasswordException.class);
	}
	
	@Test
	public void validatePasswordControllerUpdateMethodFailMissing() 
			throws EmployeeInvalidRequestParameterException, EmployeeNotFoundException {
		Employee testPasswordControllerMethod = employeelist.get(3);
		
		employeeValidationResults validationResult = 
				EmployeeValidator.validateIsThisEmployeeValid(
						Optional.ofNullable(testPasswordControllerMethod));
		
		assertEquals(employeeValidationResults.SUCCESS, validationResult);
		
		ThrowingCallable resp = () ->
				restController.updateEmployeePassword(
						testPasswordControllerMethod.getEmployee_id(), 
						testPasswordControllerMethod.getPass_word(), "newpass");
		
		assertThatCode(resp).isInstanceOf(EmployeeNotFoundException.class);
	}
}
