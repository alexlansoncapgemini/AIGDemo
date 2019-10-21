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

public class EmployeePartialUpdateFirstNameFailTester {
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
						EmployeeMessageManager.getVal("testemployeeUpdateFirstName")))));
		employeelist.add(EmployeeBeanModifier.convertFromDetails(Optional.ofNullable(
				EmployeeBeanModifier.employeeStringParserNoId(
						EmployeeMessageManager.getVal("testemployeeServiceUpdateFirstName")))));
		employeelist.add(EmployeeBeanModifier.convertFromDetails(Optional.ofNullable(
				EmployeeBeanModifier.employeeStringParserNoId(
						EmployeeMessageManager.getVal("testemployeeControllerUpdateFirstName")))));
		employeelist.add(EmployeeBeanModifier.convertFromDetails(Optional.ofNullable(
				EmployeeBeanModifier.employeeStringParserNoId(
						EmployeeMessageManager.getVal("testemployeeControllerUpdateFirstNameMethod")))));
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
	public void validateServiceFirstNameUpdateFailInvalid() 
			throws EmployeeRequestOnNullObjectException, 
			EmployeeInvalidRequestParameterException, 
			EmployeeDuplicateEntryExistsException, 
			EmployeeNotFoundException {
		Employee testemployeeServiceFirstName = employeelist.get(1);
		
		if(!EmployeeValidator.validateIsEmployeeFirstNameInDatabase(
				databaseImpl, testemployeeServiceFirstName.getFirst_name())) {
			serviceImpl.newEmployee(testemployeeServiceFirstName);
		}
		
		assertTrue(EmployeeValidator.validateIsEmployeeFirstNameInDatabase(
				databaseImpl, testemployeeServiceFirstName.getFirst_name()));
		
		testemployeeServiceFirstName = serviceImpl.getEmployeeByFirstName(
				testemployeeServiceFirstName.getFirst_name()).get(0);
		
		serviceImpl.updateEmployeeFirstName(testemployeeServiceFirstName.getEmployee_id(), "first name");
	}
	
	@Test(expected = EmployeeNotFoundException.class)
	public void validateServiceFirstNameUpdateFailMissing() 
			throws EmployeeNotFoundException, EmployeeInvalidRequestParameterException {
		Employee testServiceFirstName = employeelist.get(1);
		
		serviceImpl.updateEmployeeFirstName(testServiceFirstName.getEmployee_id(), "firstname");
	}
	
	@Test
	public void validateFirstNameControllerUpdateFailInvalid() 
			throws EmployeeRequestOnNullObjectException, 
			EmployeeInvalidRequestParameterException, 
			EmployeeDuplicateEntryExistsException {
		Employee testemployeeControllerFirstName = employeelist.get(2);
		employeeValidationResults validationResult = 
				EmployeeValidator.validateIsThisEmployeeValid(
						Optional.ofNullable(testemployeeControllerFirstName));
		
		assertEquals(employeeValidationResults.SUCCESS, validationResult);
		
		if(!EmployeeValidator.validateIsEmployeeFirstNameInDatabase(
				databaseImpl, testemployeeControllerFirstName.getFirst_name())) {
			serviceImpl.newEmployee(testemployeeControllerFirstName);
		}
		
		assertTrue(EmployeeValidator.validateIsEmployeeFirstNameInDatabase(
				databaseImpl, testemployeeControllerFirstName.getFirst_name()));
		
		testemployeeControllerFirstName = serviceImpl.getEmployeeByFirstName(
				testemployeeControllerFirstName.getFirst_name()).get(0);
		
		int eid = testemployeeControllerFirstName.getEmployee_id();
		
		ThrowingCallable resp = () ->
			restTemplate.patchForObject(EmployeeMessageManager.getVal("baseUrl") + 
					EmployeeMessageManager.getVal("partialUpdateFirstNameMapping"), 
					null, EmployeeResponseBody.class, eid, "first name");
		
		assertThatCode(resp).isInstanceOf(UnprocessableEntity.class);
	}
	
	@Test
	public void validateFirstNameControllerUpdateFailMissing() {
		Employee testFirstNameController = employeelist.get(2);
		employeeValidationResults validationResult = 
				EmployeeValidator.validateIsThisEmployeeValid(Optional.ofNullable(testFirstNameController));
		
		assertEquals(employeeValidationResults.SUCCESS, validationResult);
		
		int eid = testFirstNameController.getEmployee_id();
		
		ThrowingCallable resp = () ->
			restTemplate.patchForObject(EmployeeMessageManager.getVal("baseUrl") + 
					EmployeeMessageManager.getVal("partialUpdateFirstNameMapping"), 
					null, EmployeeResponseBody.class, eid, "firstname");
		
		assertThatCode(resp).isInstanceOf(NotFound.class);
	}
	
	@Test
	public void validateFirstNameControllerUpdateMethodFailInvalid() 
			throws EmployeeNotFoundException, 
			EmployeeInvalidRequestParameterException, 
			EmployeeRequestOnNullObjectException, 
			EmployeeDuplicateEntryExistsException {
		Employee testemployeeControllerFirstNameMethod = employeelist.get(3);
		
		employeeValidationResults validationResult = 
				EmployeeValidator.validateIsThisEmployeeValid(
						Optional.ofNullable(testemployeeControllerFirstNameMethod));
		
		assertEquals(employeeValidationResults.SUCCESS, validationResult);
		
		if(!EmployeeValidator.validateIsEmployeeFirstNameInDatabase(
				databaseImpl, testemployeeControllerFirstNameMethod.getFirst_name())) {
			serviceImpl.newEmployee(testemployeeControllerFirstNameMethod);
		}
		
		assertTrue(EmployeeValidator.validateIsEmployeeFirstNameInDatabase(
				databaseImpl, testemployeeControllerFirstNameMethod.getFirst_name()));
		
		final Employee testFirstNameControllerMethodTest = serviceImpl.getEmployeeByFirstName(
				testemployeeControllerFirstNameMethod.getFirst_name()).get(0);
		
		ThrowingCallable resp = () ->
				restController.updateEmployeeFirstName(
						testFirstNameControllerMethodTest.getEmployee_id(), "first name");
		
		assertThatCode(resp).isInstanceOf(EmployeeInvalidRequestParameterException.class);
	}
	
	@Test
	public void validateFirstNameControllerUpdateMethodFailMissing() 
			throws EmployeeNotFoundException, EmployeeInvalidRequestParameterException {
		Employee testFirstNameControllerMethod = employeelist.get(3);
		
		employeeValidationResults validationResult = 
				EmployeeValidator.validateIsThisEmployeeValid(
						Optional.ofNullable(testFirstNameControllerMethod));
		
		assertEquals(employeeValidationResults.SUCCESS, validationResult);
		
		ThrowingCallable resp = () ->
				restController.updateEmployeeFirstName(
						testFirstNameControllerMethod.getEmployee_id(), "firstname");
		
		assertThatCode(resp).isInstanceOf(EmployeeNotFoundException.class);
	}
}
