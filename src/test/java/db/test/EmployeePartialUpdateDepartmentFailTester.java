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

public class EmployeePartialUpdateDepartmentFailTester {
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
						EmployeeMessageManager.getVal("testemployeeUpdateDepartment")))));
		employeelist.add(EmployeeBeanModifier.convertFromDetails(Optional.ofNullable(
				EmployeeBeanModifier.employeeStringParserNoId(
						EmployeeMessageManager.getVal("testemployeeServiceUpdateDepartment")))));
		employeelist.add(EmployeeBeanModifier.convertFromDetails(Optional.ofNullable(
				EmployeeBeanModifier.employeeStringParserNoId(
						EmployeeMessageManager.getVal("testemployeeControllerUpdateDepartment")))));
		employeelist.add(EmployeeBeanModifier.convertFromDetails(Optional.ofNullable(
				EmployeeBeanModifier.employeeStringParserNoId(
						EmployeeMessageManager.getVal("testemployeeControllerUpdateDepartmentMethod")))));
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
	public void validateServiceDepartmentUpdateFailInvalid() 
			throws EmployeeRequestOnNullObjectException, 
			EmployeeInvalidRequestParameterException, 
			EmployeeDuplicateEntryExistsException, 
			EmployeeNotFoundException {
		Employee testemployeeServiceDepartment = employeelist.get(1);
		
		if(!EmployeeValidator.validateIsEmployeeFirstNameInDatabase(
				databaseImpl, testemployeeServiceDepartment.getFirst_name())) {
			serviceImpl.newEmployee(testemployeeServiceDepartment);
		}
		
		assertTrue(EmployeeValidator.validateIsEmployeeFirstNameInDatabase(
				databaseImpl, testemployeeServiceDepartment.getFirst_name()));
		
		testemployeeServiceDepartment = serviceImpl.getEmployeeByFirstName(
				testemployeeServiceDepartment.getFirst_name()).get(0);
		
		serviceImpl.updateEmployeeDepartment(testemployeeServiceDepartment.getEmployee_id(), "depart ment");
	}
	
	@Test(expected = EmployeeNotFoundException.class)
	public void validateServiceDepartmentUpdateFailMissing() 
			throws EmployeeNotFoundException, EmployeeInvalidRequestParameterException {
		Employee testemployeeServiceDepartment = employeelist.get(1);
		
		serviceImpl.updateEmployeeDepartment(testemployeeServiceDepartment.getEmployee_id(), "department");
	}
	
	@Test
	public void validateDepartmentControllerUpdateFailInvalid() 
			throws EmployeeRequestOnNullObjectException, 
			EmployeeInvalidRequestParameterException, 
			EmployeeDuplicateEntryExistsException {
		Employee testemployeeControllerDepartment = employeelist.get(2);
		employeeValidationResults validationResult = 
				EmployeeValidator.validateIsThisEmployeeValid(
						Optional.ofNullable(testemployeeControllerDepartment));
		
		assertEquals(employeeValidationResults.SUCCESS, validationResult);
		
		if(!EmployeeValidator.validateIsEmployeeFirstNameInDatabase(
				databaseImpl, testemployeeControllerDepartment.getFirst_name())) {
			serviceImpl.newEmployee(testemployeeControllerDepartment);
		}
		
		assertTrue(EmployeeValidator.validateIsEmployeeFirstNameInDatabase(
				databaseImpl, testemployeeControllerDepartment.getFirst_name()));
		
		testemployeeControllerDepartment = serviceImpl.getEmployeeByFirstName(
				testemployeeControllerDepartment.getFirst_name()).get(0);
		
		int eid = testemployeeControllerDepartment.getEmployee_id();
		
		ThrowingCallable resp = () ->
			restTemplate.patchForObject(EmployeeMessageManager.getVal("baseUrl") + 
					EmployeeMessageManager.getVal("partialUpdateDepartmentMapping"), 
					null, EmployeeResponseBody.class, eid, "depart ment");
		
		assertThatCode(resp).isInstanceOf(UnprocessableEntity.class);
	}
	
	@Test
	public void validateDepartmentControllerUpdateFailMissing() {
		Employee testDepartmentController = employeelist.get(2);
		employeeValidationResults validationResult = 
				EmployeeValidator.validateIsThisEmployeeValid(Optional.ofNullable(testDepartmentController));
		
		assertEquals(employeeValidationResults.SUCCESS, validationResult);
		
		int eid = testDepartmentController.getEmployee_id();
		
		ThrowingCallable resp = () ->
			restTemplate.patchForObject(EmployeeMessageManager.getVal("baseUrl") + 
					EmployeeMessageManager.getVal("partialUpdateDepartmentMapping"), 
					null, EmployeeResponseBody.class, eid, "department");
		
		assertThatCode(resp).isInstanceOf(NotFound.class);
	}
	
	@Test
	public void validateDepartmentControllerUpdateMethodFailInvalid() 
			throws EmployeeInvalidRequestParameterException, 
			EmployeeNotFoundException, 
			EmployeeRequestOnNullObjectException, 
			EmployeeDuplicateEntryExistsException {
		Employee testemployeeControllerDepartmentMethod = employeelist.get(3);
		
		employeeValidationResults validationResult = 
				EmployeeValidator.validateIsThisEmployeeValid(
						Optional.ofNullable(testemployeeControllerDepartmentMethod));
		
		assertEquals(employeeValidationResults.SUCCESS, validationResult);
		
		if(!EmployeeValidator.validateIsEmployeeFirstNameInDatabase(
				databaseImpl, testemployeeControllerDepartmentMethod.getFirst_name())) {
			serviceImpl.newEmployee(testemployeeControllerDepartmentMethod);
		}
		
		assertTrue(EmployeeValidator.validateIsEmployeeFirstNameInDatabase(
				databaseImpl, testemployeeControllerDepartmentMethod.getFirst_name()));
		
		final Employee testDepartmentControllerMethodTest = serviceImpl.getEmployeeByFirstName(
				testemployeeControllerDepartmentMethod.getFirst_name()).get(0);
		
		ThrowingCallable resp = () ->
				restController.updateEmployeeDepartment(
						testDepartmentControllerMethodTest.getEmployee_id(), "depart ment");
		
		assertThatCode(resp).isInstanceOf(EmployeeInvalidRequestParameterException.class);
	}
	
	@Test
	public void validateDepartmentControllerUpdateMethodFailMissing() 
			throws EmployeeInvalidRequestParameterException, EmployeeNotFoundException {
		Employee testemployeeControllerDepartmentMethod = employeelist.get(3);
		
		employeeValidationResults validationResult = 
				EmployeeValidator.validateIsThisEmployeeValid(
						Optional.ofNullable(testemployeeControllerDepartmentMethod));
		
		assertEquals(employeeValidationResults.SUCCESS, validationResult);
		
		ThrowingCallable resp = () ->
				restController.updateEmployeeDepartment(
						testemployeeControllerDepartmentMethod.getEmployee_id(), "department");
		
		assertThatCode(resp).isInstanceOf(EmployeeNotFoundException.class);
	}
}
