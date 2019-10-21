package db.test;

import static org.junit.Assert.assertEquals;
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
import demo.beans.EmployeeDetails;
import demo.controller.EmployeeRestController;
import demo.dao.EmployeeDatabaseImplementation;
import demo.exceptions.EmployeeDuplicateEntryExistsException;
import demo.exceptions.EmployeeInvalidRequestParameterException;
import demo.exceptions.EmployeeNotFoundException;
import demo.exceptions.EmployeeRequestOnNullObjectException;
import demo.responses.EmployeeResponseBody;
import demo.responses.EmployeeResponseBodySuperclass;
import demo.service.EmployeeServiceImplementation;
import demo.util.EmployeeMessageManager;
import demo.util.EmployeeBeanModifier;
import demo.util.EmployeeValidator;
import demo.util.EmployeeMessageManager.employeeValidationResults;

//tests each partial (patch) update method with each way they can be called
//the ways these can be called are:
//1. raw database (object) calls
//2. service layer calls
//3. database calls through a rest template
//4. direct rest controller calls
//it is in this order that each update method below is tested
public class EmployeePartialUpdateAgeTester {
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
	public void validateAgeUpdate() 
			throws EmployeeRequestOnNullObjectException, 
			EmployeeInvalidRequestParameterException, 
			EmployeeDuplicateEntryExistsException, 
			EmployeeNotFoundException {
		Employee testemployeeAge = employeelist.get(0);
		
		if(!EmployeeValidator.validateIsEmployeeFirstNameInDatabase(
				databaseImpl, testemployeeAge.getFirst_name())) {
			serviceImpl.newEmployee(testemployeeAge);
		}
		
		assertTrue(EmployeeValidator.validateIsEmployeeFirstNameInDatabase(
				databaseImpl, testemployeeAge.getFirst_name()));
		
		//retrieves the assigned employee id from the database and stores it into the local variable
		testemployeeAge = serviceImpl.getEmployeeByFirstName(testemployeeAge.getFirst_name()).get(0);
		
		databaseImpl.updateEmployeeAge(testemployeeAge.getEmployee_id(), 30);
		
		testemployeeAge = serviceImpl.getEmployeeById(testemployeeAge.getEmployee_id());
		
		assertNotNull(testemployeeAge);
		
		assertEquals(30, testemployeeAge.getAge());
	}
	
	@Test
	public void validateServiceAgeUpdate() 
			throws EmployeeRequestOnNullObjectException, 
			EmployeeInvalidRequestParameterException, 
			EmployeeDuplicateEntryExistsException, EmployeeNotFoundException {
		Employee testemployeeServiceAge = employeelist.get(1);
		
		if(!EmployeeValidator.validateIsEmployeeFirstNameInDatabase(
				databaseImpl, testemployeeServiceAge.getFirst_name())) {
			serviceImpl.newEmployee(testemployeeServiceAge);
		}
		
		assertTrue(EmployeeValidator.validateIsEmployeeFirstNameInDatabase(
				databaseImpl, testemployeeServiceAge.getFirst_name()));
		
		testemployeeServiceAge = serviceImpl.getEmployeeByFirstName(testemployeeServiceAge.getFirst_name()).get(0);
		
		serviceImpl.updateEmployeeAge(testemployeeServiceAge.getEmployee_id(), 31);
		
		testemployeeServiceAge = serviceImpl.getEmployeeById(testemployeeServiceAge.getEmployee_id());
		
		assertNotNull(testemployeeServiceAge);
		
		assertEquals(31, testemployeeServiceAge.getAge());
	}
	
	@Test
	public void validateAgeControllerUpdate() 
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
		
		restTemplate.patchForObject(EmployeeMessageManager.getVal("baseUrl") + 
				EmployeeMessageManager.getVal("partialUpdateAgeMapping"), 
				null, EmployeeResponseBody.class, testemployeeControllerAge.getEmployee_id(), 31);
		
		EmployeeResponseBody resp = restTemplate.getForObject(EmployeeMessageManager.getVal("baseUrl") + 
				EmployeeMessageManager.getVal("getEmployeeMapping"), 
				EmployeeResponseBody.class, testemployeeControllerAge.getEmployee_id());
		
		EmployeeDetails employeedetails = resp.getEmployeeDetails();
		
		assertEquals(31, employeedetails.getAge());
	}
	
	@Test
	public void validateAgeControllerUpdateMethod() 
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
		
		testemployeeControllerAgeMethod = serviceImpl.getEmployeeByFirstName(
				testemployeeControllerAgeMethod.getFirst_name()).get(0);
		
		ResponseEntity<EmployeeResponseBodySuperclass> resp = 
				restController.updateEmployeeAge(testemployeeControllerAgeMethod.getEmployee_id(), 32);
		
		assertEquals(HttpStatus.OK, resp.getStatusCode());
	}
}
