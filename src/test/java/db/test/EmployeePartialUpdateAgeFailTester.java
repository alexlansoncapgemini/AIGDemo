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

public class EmployeePartialUpdateAgeFailTester {
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
						EmployeeMessageManager.getVal("testemployeeUpdateAge")))));
		employeelist.add(EmployeeBeanModifier.convertFromDetails(Optional.ofNullable(
				EmployeeBeanModifier.employeeStringParserNoId(
						EmployeeMessageManager.getVal("testemployeeServiceUpdateAge")))));
		employeelist.add(EmployeeBeanModifier.convertFromDetails(Optional.ofNullable(
				EmployeeBeanModifier.employeeStringParserNoId(
						EmployeeMessageManager.getVal("testemployeeControllerUpdateAge")))));
		employeelist.add(EmployeeBeanModifier.convertFromDetails(Optional.ofNullable(
				EmployeeBeanModifier.employeeStringParserNoId(
						EmployeeMessageManager.getVal("testemployeeControllerUpdateAgeMethod")))));
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
	
	@Test
	public void validateServiceAgeUpdateFailInvalid() 
			throws EmployeeRequestOnNullObjectException, 
			EmployeeInvalidRequestParameterException, 
			EmployeeDuplicateEntryExistsException, 
			EmployeeNotFoundException {
		Employee testemployeeServiceAge = employeelist.get(1);

		if(!EmployeeValidator.validateIsEmployeeFirstNameInDatabase(
				databaseImpl, testemployeeServiceAge.getFirst_name())) {
			serviceImpl.newEmployee(testemployeeServiceAge);
		}
		
		assertTrue(EmployeeValidator.validateIsEmployeeFirstNameInDatabase(
				databaseImpl, testemployeeServiceAge.getFirst_name()));
		
		final Employee testServiceAgeTest = 
				serviceImpl.getEmployeeByFirstName(testemployeeServiceAge.getFirst_name()).get(0);
		
		ThrowingCallable resp = () ->
				serviceImpl.updateEmployeeAge(testServiceAgeTest.getEmployee_id(), 12);
				
		assertThatCode(resp).isInstanceOf(EmployeeInvalidRequestParameterException.class);
	}
	
	@Test(expected = EmployeeNotFoundException.class)
	public void validateServiceAgeUpdateFailMissing() 
			throws EmployeeNotFoundException, 
			EmployeeInvalidRequestParameterException {
		Employee testServiceAge = employeelist.get(1);
		
		testServiceAge = serviceImpl.updateEmployeeAge(testServiceAge.getEmployee_id(), 20);
	}
	
	@Test
	public void validateAgeControllerUpdateFailInvalid() 
			throws EmployeeRequestOnNullObjectException, 
			EmployeeInvalidRequestParameterException, 
			EmployeeDuplicateEntryExistsException {
		Employee testemployeeControllerAge = employeelist.get(2);
		employeeValidationResults validationResult = 
				EmployeeValidator.validateIsThisEmployeeValid(Optional.ofNullable(testemployeeControllerAge));
		
		assertEquals(employeeValidationResults.SUCCESS, validationResult);
		
		if(!EmployeeValidator.validateIsEmployeeFirstNameInDatabase(
				databaseImpl, testemployeeControllerAge.getFirst_name())) {
			serviceImpl.newEmployee(testemployeeControllerAge);
		}
		
		assertTrue(EmployeeValidator.validateIsEmployeeFirstNameInDatabase(
				databaseImpl, testemployeeControllerAge.getFirst_name()));
		
		testemployeeControllerAge = serviceImpl.getEmployeeByFirstName(
				testemployeeControllerAge.getFirst_name()).get(0);
		
		int eid = testemployeeControllerAge.getEmployee_id();
		
		ThrowingCallable resp = () ->
			restTemplate.patchForObject(EmployeeMessageManager.getVal("baseUrl") + 
					EmployeeMessageManager.getVal("partialUpdateAgeMapping"), 
					null, EmployeeResponseBody.class, eid, 13);
		
		assertThatCode(resp).isInstanceOf(UnprocessableEntity.class);
	}
	
	@Test
	public void validateAgeControllerUpdateFailMissing() {
		Employee testemployeeControllerAgeMethod = employeelist.get(2);
		employeeValidationResults validationResult = 
				EmployeeValidator.validateIsThisEmployeeValid(
						Optional.ofNullable(testemployeeControllerAgeMethod));
		
		assertEquals(employeeValidationResults.SUCCESS, validationResult);
		
		int eid = testemployeeControllerAgeMethod.getEmployee_id();
		
		ThrowingCallable resp = () ->
			restTemplate.patchForObject(EmployeeMessageManager.getVal("baseUrl") + 
					EmployeeMessageManager.getVal("partialUpdateAgeMapping"), 
					null, EmployeeResponseBody.class, eid, 31);
		
		assertThatCode(resp).isInstanceOf(NotFound.class);
	}
	
	@Test
	public void validateAgeControllerUpdateMethodFailInvalid() 
			throws EmployeeNotFoundException, 
			EmployeeInvalidRequestParameterException, 
			EmployeeRequestOnNullObjectException, 
			EmployeeDuplicateEntryExistsException {
		Employee testemployeeControllerAgeMethod = employeelist.get(3);
		
		employeeValidationResults validationResult = 
				EmployeeValidator.validateIsThisEmployeeValid(
						Optional.ofNullable(testemployeeControllerAgeMethod));
		
		assertEquals(employeeValidationResults.SUCCESS, validationResult);
		
		if(!EmployeeValidator.validateIsEmployeeFirstNameInDatabase(
				databaseImpl, testemployeeControllerAgeMethod.getFirst_name())) {
			serviceImpl.newEmployee(testemployeeControllerAgeMethod);
		}
		
		assertTrue(EmployeeValidator.validateIsEmployeeFirstNameInDatabase(
				databaseImpl, testemployeeControllerAgeMethod.getFirst_name()));
		
		final Employee testAgeControllerMethodTest = serviceImpl.getEmployeeByFirstName(
				testemployeeControllerAgeMethod.getFirst_name()).get(0);
		
		ThrowingCallable resp = () ->
				restController.updateEmployeeAge(testAgeControllerMethodTest.getEmployee_id(), 11);
		
		assertThatCode(resp).isInstanceOf(EmployeeInvalidRequestParameterException.class);
	}
	
	@Test
	public void validateAgeControllerUpdateMethodFailMissing() 
			throws EmployeeNotFoundException, EmployeeInvalidRequestParameterException {
		ThrowingCallable resp = () ->
				restController.updateEmployeeAge(55555, 32);
				
		assertThatCode(resp).isInstanceOf(EmployeeNotFoundException.class);
	}
}
