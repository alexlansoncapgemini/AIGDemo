package db.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

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
import demo.responses.EmployeeResponseBodySuperclass;
import demo.service.EmployeeServiceImplementation;
import demo.util.EmployeeMessageManager;
import demo.util.EmployeeMessageManager.employeeValidationResults;
import demo.util.EmployeeBeanModifier;
import demo.util.EmployeeValidator;

//tests the getEmployee (specific employee) employee read methods
public class EmployeeSpecificReadTester {
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
						EmployeeMessageManager.getVal("testemployeeSpecificRead")))));
		employeelist.add(EmployeeBeanModifier.convertFromDetails(Optional.ofNullable(
				EmployeeBeanModifier.employeeStringParserNoId(
						EmployeeMessageManager.getVal("testemployeeServiceSpecificRead")))));
		employeelist.add(EmployeeBeanModifier.convertFromDetails(Optional.ofNullable(
				EmployeeBeanModifier.employeeStringParserNoId(
						EmployeeMessageManager.getVal("testemployeeControllerSpecificRead")))));
		employeelist.add(EmployeeBeanModifier.convertFromDetails(Optional.ofNullable(
				EmployeeBeanModifier.employeeStringParserNoId(
						EmployeeMessageManager.getVal("testemployeeControllerSpecificReadMethod")))));
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
	public void validateDatabaseSpecificRead() 
			throws EmployeeRequestOnNullObjectException, 
			EmployeeInvalidRequestParameterException, 
			EmployeeDuplicateEntryExistsException {
		Employee testemployeeSpecificRead = employeelist.get(0);
			
		if(!EmployeeValidator.validateIsEmployeeFirstNameInDatabase(
				databaseImpl, testemployeeSpecificRead.getFirst_name())) {
			serviceImpl.newEmployee(testemployeeSpecificRead);
		}
		
		assertTrue(EmployeeValidator.validateIsEmployeeFirstNameInDatabase(
				databaseImpl, testemployeeSpecificRead.getFirst_name()));
		
		testemployeeSpecificRead = serviceImpl.getEmployeeByFirstName(
				testemployeeSpecificRead.getFirst_name()).get(0);
		
		testemployeeSpecificRead = databaseImpl.getEmployeeById(
				testemployeeSpecificRead.getEmployee_id()).get(0);
		
		assertNotNull(testemployeeSpecificRead);
		
		//testing conversions
		String employeeString = testemployeeSpecificRead.toString();
		
		testemployeeSpecificRead = EmployeeBeanModifier.convertFromDetails(Optional.ofNullable(
				EmployeeBeanModifier.employeeStringParser(employeeString)));
		
		assertNotNull(testemployeeSpecificRead);
	}
	
	@Test
	public void validateServiceSpecificRead() 
			throws EmployeeRequestOnNullObjectException, 
			EmployeeInvalidRequestParameterException, 
			EmployeeDuplicateEntryExistsException, 
			EmployeeNotFoundException {
		Employee testemployeeServiceSpecificRead = employeelist.get(1);
		
		if(!EmployeeValidator.validateIsEmployeeFirstNameInDatabase(
				databaseImpl, testemployeeServiceSpecificRead.getFirst_name())) {
			serviceImpl.newEmployee(testemployeeServiceSpecificRead);
		}
		
		assertTrue(EmployeeValidator.validateIsEmployeeFirstNameInDatabase(
				databaseImpl, testemployeeServiceSpecificRead.getFirst_name()));
		
		testemployeeServiceSpecificRead = 
				serviceImpl.getEmployeeByFirstName(testemployeeServiceSpecificRead.getFirst_name()).get(0);
		
		testemployeeServiceSpecificRead = serviceImpl.getEmployeeById(testemployeeServiceSpecificRead.getEmployee_id());
		
		assertNotNull(testemployeeServiceSpecificRead);
	}
	
	@Test
	public void validateControllerSpecificRead() 
			throws EmployeeDuplicateEntryExistsException, 
			EmployeeRequestOnNullObjectException, 
			EmployeeInvalidRequestParameterException {
		employeeValidationResults validationResult = 
				EmployeeValidator.validateIsThisEmployeeValid(Optional.ofNullable(employeelist.get(2)));
		
		assertEquals(employeeValidationResults.SUCCESS, validationResult);
		
		if(!EmployeeValidator.validateIsEmployeeFirstNameInDatabase(
				databaseImpl, employeelist.get(2).getFirst_name())) {
			restController.addNewEmployee(
					EmployeeBeanModifier.convertToDetails(Optional.ofNullable(employeelist.get(2))));
		}
		
		assertTrue(EmployeeValidator.validateIsEmployeeFirstNameInDatabase(
				databaseImpl, employeelist.get(2).getFirst_name()));
		
		Employee testControllerSpecificRead = 
				serviceImpl.getEmployeeByFirstName(employeelist.get(2).getFirst_name()).get(0);
		
		EmployeeResponseBodySuperclass resp = 
				restTemplate.getForObject(EmployeeMessageManager.getVal("baseUrl") + 
					EmployeeMessageManager.getVal("getEmployeeMapping"), 
						EmployeeResponseBodySuperclass.class, testControllerSpecificRead.getEmployee_id());
		
		assertEquals(200, resp.getStatusCode());
	}
	
	@Test
	public void validateControllerSpecificReadMethod() 
			throws EmployeeDuplicateEntryExistsException, 
			EmployeeRequestOnNullObjectException, 
			EmployeeInvalidRequestParameterException, EmployeeNotFoundException {
		employeeValidationResults validationResult = 
				EmployeeValidator.validateIsThisEmployeeValid(Optional.ofNullable(employeelist.get(3)));
		
		assertEquals(employeeValidationResults.SUCCESS, validationResult);
		
		if(!EmployeeValidator.validateIsEmployeeFirstNameInDatabase(
				databaseImpl, employeelist.get(3).getFirst_name())) {
			restController.addNewEmployee(
					EmployeeBeanModifier.convertToDetails(Optional.ofNullable(employeelist.get(3))));
		}
		
		assertTrue(EmployeeValidator.validateIsEmployeeFirstNameInDatabase(
				databaseImpl, employeelist.get(3).getFirst_name()));
		
		//failure case
		assertFalse(EmployeeValidator.validateIsEmployeeFirstNameInDatabase(databaseImpl, "abcde"));
		
		Employee testControllerSpecificMethod = 
				serviceImpl.getEmployeeByFirstName(employeelist.get(3).getFirst_name()).get(0);
		
		ResponseEntity<EmployeeResponseBodySuperclass> resp = 
				restController.getEmployeeById(testControllerSpecificMethod.getEmployee_id());
		
		assertEquals(HttpStatus.OK, resp.getStatusCode());
	}
}
