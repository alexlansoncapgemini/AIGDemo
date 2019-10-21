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
//the three ways these can be called are:
//1. raw database (object) calls
//2. database calls through a rest template
//3. direct rest controller calls
//it is in this order that each update method below is tested
public class EmployeePartialUpdateLastNameTester {
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
	
	@Test
	public void validateLastNameUpdate() 
			throws EmployeeRequestOnNullObjectException, 
			EmployeeInvalidRequestParameterException, 
			EmployeeDuplicateEntryExistsException, 
			EmployeeNotFoundException {
		Employee testemployeeLastName = employeelist.get(0);
		
		if(!EmployeeValidator.validateIsEmployeeFirstNameInDatabase(
				databaseImpl, testemployeeLastName.getFirst_name())) {
			serviceImpl.newEmployee(testemployeeLastName);
		}
		
		assertTrue(EmployeeValidator.validateIsEmployeeFirstNameInDatabase(
				databaseImpl, testemployeeLastName.getFirst_name()));
		
		//retrieves the assigned employee id from the database and stores it into the local variable
		testemployeeLastName = serviceImpl.getEmployeeByFirstName(testemployeeLastName.getFirst_name()).get(0);
		
		databaseImpl.updateEmployeeLastName(testemployeeLastName.getEmployee_id(), "german");
		
		testemployeeLastName = serviceImpl.getEmployeeById(testemployeeLastName.getEmployee_id());
		
		assertNotNull(testemployeeLastName);
		
		assertEquals("german", testemployeeLastName.getLast_name());
	}
	
	@Test
	public void validateLastNameServiceUpdate() 
			throws EmployeeRequestOnNullObjectException, 
			EmployeeInvalidRequestParameterException, 
			EmployeeDuplicateEntryExistsException, EmployeeNotFoundException {
		Employee testemployeeServiceLastName = employeelist.get(1);
		
		if(!EmployeeValidator.validateIsEmployeeFirstNameInDatabase(
				databaseImpl, testemployeeServiceLastName.getFirst_name())) {
			serviceImpl.newEmployee(testemployeeServiceLastName);
		}
		
		assertTrue(EmployeeValidator.validateIsEmployeeFirstNameInDatabase(
				databaseImpl, testemployeeServiceLastName.getFirst_name()));
		
		testemployeeServiceLastName = serviceImpl.getEmployeeByFirstName(testemployeeServiceLastName.getFirst_name()).get(0);
		
		serviceImpl.updateEmployeeLastName(testemployeeServiceLastName.getEmployee_id(), "french");
		
		testemployeeServiceLastName = serviceImpl.getEmployeeById(testemployeeServiceLastName.getEmployee_id());
		
		assertNotNull(testemployeeServiceLastName);
		
		assertEquals("french", testemployeeServiceLastName.getLast_name());
	}
	
	@Test
	public void validateLastNameControllerUpdate() 
			throws EmployeeDuplicateEntryExistsException, 
			EmployeeRequestOnNullObjectException, 
			EmployeeInvalidRequestParameterException {
		Employee testemployeeControllerLastName = employeelist.get(2);
		employeeValidationResults validationResult = 
				EmployeeValidator.validateIsThisEmployeeValid(
						Optional.ofNullable(testemployeeControllerLastName));
		
		assertEquals(employeeValidationResults.SUCCESS, validationResult);
		
		if(!EmployeeValidator.validateIsEmployeeFirstNameInDatabase(
				databaseImpl, testemployeeControllerLastName.getFirst_name())) {
			restController.addNewEmployee(
					EmployeeBeanModifier.convertToDetails(Optional.ofNullable(testemployeeControllerLastName)));
		}
		
		assertTrue(EmployeeValidator.validateIsEmployeeFirstNameInDatabase(
				databaseImpl, testemployeeControllerLastName.getFirst_name()));
		
		testemployeeControllerLastName = serviceImpl.getEmployeeByFirstName(
				testemployeeControllerLastName.getFirst_name()).get(0);
		
		restTemplate.patchForObject(EmployeeMessageManager.getVal("baseUrl") + 
				EmployeeMessageManager.getVal("partialUpdateLastNameMapping"), 
				null, EmployeeResponseBody.class, testemployeeControllerLastName.getEmployee_id(), "italian");
		
		EmployeeResponseBody resp = restTemplate.getForObject(EmployeeMessageManager.getVal("baseUrl") + 
				EmployeeMessageManager.getVal("getEmployeeMapping"), 
				EmployeeResponseBody.class, testemployeeControllerLastName.getEmployee_id());
		
		EmployeeDetails employeedetails = resp.getEmployeeDetails();
		
		assertEquals("italian", employeedetails.getLast_name());
	}
	
	@Test
	public void validateLastNameControllerUpdateMethod() 
			throws EmployeeInvalidRequestParameterException, 
			EmployeeNotFoundException, 
			EmployeeRequestOnNullObjectException, 
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
		
		testemployeeControllerLastNameMethod = serviceImpl.getEmployeeByFirstName(
				testemployeeControllerLastNameMethod.getFirst_name()).get(0);
		
		ResponseEntity<EmployeeResponseBodySuperclass> resp = restController.updateEmployeeLastName(
				testemployeeControllerLastNameMethod.getEmployee_id(), "dutch");
		
		assertEquals(HttpStatus.OK, resp.getStatusCode());
	}
}
