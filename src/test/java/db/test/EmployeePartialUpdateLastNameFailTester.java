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
import demo.exceptions.EmployeeInvalidRequestParameterException;
import demo.exceptions.EmployeeNotFoundException;
import demo.exceptions.EmployeeRequestOnNullObjectException;
import demo.responses.EmployeeResponseBody;
import demo.service.EmployeeServiceImplementation;
import demo.util.EmployeeBeanModifier;
import demo.util.EmployeeMessageManager;
import demo.util.EmployeeValidator;
import demo.util.EmployeeMessageManager.employeeValidationResults;

public class EmployeePartialUpdateLastNameFailTester {
	@Mock
	RestTemplate restTemplate;
	List<Employee> employeelist = new ArrayList<>();
	
	final EmployeeDatabaseImplementation databaseImpl = new EmployeeDatabaseImplementation();
	final EmployeeServiceImplementation serviceImpl = new EmployeeServiceImplementation(databaseImpl);
	final EmployeeRestController restController = new EmployeeRestController(serviceImpl);
	
	@Before
	public void initSetup() {
		MockitoAnnotations.initMocks(this);
		restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
		employeelist.add(EmployeeBeanModifier.convertFromDetails(Optional.ofNullable(
				EmployeeBeanModifier.employeeStringParserNoId(
						EmployeeMessageManager.getVal("testemployeeUpdateLastName")))));
		employeelist.add(EmployeeBeanModifier.convertFromDetails(Optional.ofNullable(
				EmployeeBeanModifier.employeeStringParserNoId(
						EmployeeMessageManager.getVal("testemployeeServiceUpdateLastName")))));
		employeelist.add(EmployeeBeanModifier.convertFromDetails(Optional.ofNullable(
				EmployeeBeanModifier.employeeStringParserNoId(
						EmployeeMessageManager.getVal("testemployeeControllerUpdateLastName")))));
		employeelist.add(EmployeeBeanModifier.convertFromDetails(Optional.ofNullable(
				EmployeeBeanModifier.employeeStringParserNoId(
						EmployeeMessageManager.getVal("testemployeeControllerUpdateLastNameMethod")))));
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
	public void validateServiceLastNameUpdateFailInvalid() 
			throws EmployeeRequestOnNullObjectException, 
			EmployeeInvalidRequestParameterException, 
			EmployeeDuplicateEntryExistsException, 
			EmployeeNotFoundException {
		Employee testemployeeServiceLastName = employeelist.get(1);
		
		if(!EmployeeValidator.validateIsEmployeeFirstNameInDatabase(
				databaseImpl, testemployeeServiceLastName.getFirst_name())) {
			serviceImpl.newEmployee(testemployeeServiceLastName);
		}
		
		assertTrue(EmployeeValidator.validateIsEmployeeFirstNameInDatabase(
				databaseImpl, testemployeeServiceLastName.getFirst_name()));
		
		testemployeeServiceLastName = serviceImpl.getEmployeeByFirstName(testemployeeServiceLastName.getFirst_name()).get(0);
		
		serviceImpl.updateEmployeeLastName(testemployeeServiceLastName.getEmployee_id(), "last name");
	}
	
	@Test(expected = EmployeeNotFoundException.class)
	public void validateServiceLastNameUpdateFailMissing() 
			throws EmployeeNotFoundException, EmployeeInvalidRequestParameterException {
		Employee testServiceLastName = employeelist.get(1);
		
		serviceImpl.updateEmployeeLastName(testServiceLastName.getEmployee_id(), "lastname");
	}
	
	@Test
	public void validateLastNameControllerUpdateFailInvalid() 
			throws EmployeeRequestOnNullObjectException, 
			EmployeeInvalidRequestParameterException, 
			EmployeeDuplicateEntryExistsException {
		Employee testemployeeControllerLastName = employeelist.get(2);
		employeeValidationResults validationResult = 
				EmployeeValidator.validateIsThisEmployeeValid(
						Optional.ofNullable(testemployeeControllerLastName));
		
		assertEquals(employeeValidationResults.SUCCESS, validationResult);
		
		if(!EmployeeValidator.validateIsEmployeeFirstNameInDatabase(
				databaseImpl, testemployeeControllerLastName.getFirst_name())) {
			serviceImpl.newEmployee(testemployeeControllerLastName);
		}
		
		assertTrue(EmployeeValidator.validateIsEmployeeFirstNameInDatabase(
				databaseImpl, testemployeeControllerLastName.getFirst_name()));
		
		testemployeeControllerLastName = serviceImpl.getEmployeeByFirstName(
				testemployeeControllerLastName.getFirst_name()).get(0);
		
		int eid = testemployeeControllerLastName.getEmployee_id();
		
		ThrowingCallable resp = () ->
			restTemplate.patchForObject(EmployeeMessageManager.getVal("baseUrl") + 
					EmployeeMessageManager.getVal("partialUpdateLastNameMapping"), 
					null, EmployeeResponseBody.class, eid, "last name");
		
		assertThatCode(resp).isInstanceOf(UnprocessableEntity.class);
	}
	
	@Test
	public void validateLastNameControllerUpdateFailMissing() {
		Employee testLastNameController = employeelist.get(2);
		employeeValidationResults validationResult = 
				EmployeeValidator.validateIsThisEmployeeValid(Optional.ofNullable(testLastNameController));
		
		assertEquals(employeeValidationResults.SUCCESS, validationResult);
		
		int eid = testLastNameController.getEmployee_id();
		
		ThrowingCallable resp = () ->
			restTemplate.patchForObject(EmployeeMessageManager.getVal("baseUrl") + 
					EmployeeMessageManager.getVal("partialUpdateLastNameMapping"), 
					null, EmployeeResponseBody.class, eid, "lastname");
		
		assertThatCode(resp).isInstanceOf(NotFound.class);
	}
	
	@Test
	public void validateLastNameControllerUpdateMethodFailInvalid() 
			throws EmployeeRequestOnNullObjectException, 
			EmployeeInvalidRequestParameterException, 
			EmployeeDuplicateEntryExistsException {
		Employee testemployeeControllerLastNameMethod = employeelist.get(3);
		
		employeeValidationResults validationResult = 
				EmployeeValidator.validateIsThisEmployeeValid(
						Optional.ofNullable(testemployeeControllerLastNameMethod));
		
		assertEquals(employeeValidationResults.SUCCESS, validationResult);
		
		if(!EmployeeValidator.validateIsEmployeeFirstNameInDatabase(
				databaseImpl, testemployeeControllerLastNameMethod.getFirst_name())) {
			serviceImpl.newEmployee(testemployeeControllerLastNameMethod);
		}
		
		assertTrue(EmployeeValidator.validateIsEmployeeFirstNameInDatabase(
				databaseImpl, testemployeeControllerLastNameMethod.getFirst_name()));
		
		final Employee testLastNameControllerMethodTest = serviceImpl.getEmployeeByFirstName(
				testemployeeControllerLastNameMethod.getFirst_name()).get(0);
		
		ThrowingCallable resp = () ->
				restController.updateEmployeeLastName(
						testLastNameControllerMethodTest.getEmployee_id(), "last name");
		
		assertThatCode(resp).isInstanceOf(EmployeeInvalidRequestParameterException.class);
	}
	
	@Test
	public void validateLastNameControllerUpdateMethodFailMissing() {
		Employee testLastNameControllerMethod = employeelist.get(3);
		
		employeeValidationResults validationResult = 
				EmployeeValidator.validateIsThisEmployeeValid(
						Optional.ofNullable(testLastNameControllerMethod));
		
		assertEquals(employeeValidationResults.SUCCESS, validationResult);
		
		ThrowingCallable resp = () ->
				restController.updateEmployeeLastName(
						testLastNameControllerMethod.getEmployee_id(), "lastname");
		
		assertThatCode(resp).isInstanceOfAny(EmployeeNotFoundException.class);
	}
}
