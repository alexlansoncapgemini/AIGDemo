package db.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
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
//2. service layer calls
//3. database calls through a rest template
//4. direct rest controller calls
//it is in this order that each update method below is tested
public class EmployeePartialUpdateFirstNameTester {
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
	
	@Test
	public void validateFirstNameUpdate() 
			throws EmployeeRequestOnNullObjectException, 
			EmployeeInvalidRequestParameterException, 
			EmployeeDuplicateEntryExistsException, 
			EmployeeNotFoundException {
		Employee testemployeeFirstName = employeelist.get(0);

		if(!EmployeeValidator.validateIsEmployeeFirstNameInDatabase(
				databaseImpl, testemployeeFirstName.getFirst_name())) {
			serviceImpl.newEmployee(testemployeeFirstName);
		}
		
		assertTrue(EmployeeValidator.validateIsEmployeeFirstNameInDatabase(
				databaseImpl, testemployeeFirstName.getFirst_name()));
		
		//retrieves the assigned employee id from the database and stores it into the local variable
		testemployeeFirstName = serviceImpl.getEmployeeByFirstName(testemployeeFirstName.getFirst_name()).get(0);
		
		databaseImpl.updateEmployeeFirstName(testemployeeFirstName.getEmployee_id(), "jackson");
		
		testemployeeFirstName = serviceImpl.getEmployeeById(testemployeeFirstName.getEmployee_id());
		
		assertNotNull(testemployeeFirstName);
		
		assertEquals("jackson", testemployeeFirstName.getFirst_name());
	}
	
	@Test
	public void validateServiceFirstNameUpdate() 
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
		
		serviceImpl.updateEmployeeFirstName(testemployeeServiceFirstName.getEmployee_id(), "jerry");
		
		testemployeeServiceFirstName = serviceImpl.getEmployeeById(testemployeeServiceFirstName.getEmployee_id());
		
		assertNotNull(testemployeeServiceFirstName);
		
		assertEquals("jerry", testemployeeServiceFirstName.getFirst_name());
	}
	
	@Test
	public void validateFirstNameControllerUpdate() 
			throws EmployeeDuplicateEntryExistsException, 
			EmployeeRequestOnNullObjectException, 
			EmployeeInvalidRequestParameterException {
		Employee testemployeeControllerFirstName = employeelist.get(2);
		employeeValidationResults validationResult = 
				EmployeeValidator.validateIsThisEmployeeValid(
						Optional.ofNullable(testemployeeControllerFirstName));
		
		assertEquals(employeeValidationResults.SUCCESS, validationResult);
		
		if(!EmployeeValidator.validateIsEmployeeFirstNameInDatabase(
				databaseImpl, testemployeeControllerFirstName.getFirst_name())) {
			restController.addNewEmployee(
					EmployeeBeanModifier.convertToDetails(
							Optional.ofNullable(testemployeeControllerFirstName)));
		}
		
		assertTrue(EmployeeValidator.validateIsEmployeeFirstNameInDatabase(
				databaseImpl, testemployeeControllerFirstName.getFirst_name()));
		
		testemployeeControllerFirstName = serviceImpl.getEmployeeByFirstName(
				testemployeeControllerFirstName.getFirst_name()).get(0);
		
		restTemplate.patchForObject(EmployeeMessageManager.getVal("baseUrl") + 
				EmployeeMessageManager.getVal("partialUpdateFirstNameMapping"), 
				null, EmployeeResponseBody.class, testemployeeControllerFirstName.getEmployee_id(), "jack");
		
		EmployeeResponseBody resp = restTemplate.getForObject(EmployeeMessageManager.getVal("baseUrl") + 
				EmployeeMessageManager.getVal("getEmployeeMapping"), 
				EmployeeResponseBody.class, testemployeeControllerFirstName.getEmployee_id());
		
		EmployeeDetails employeedetails = resp.getEmployeeDetails();
		
		assertNotEquals("harrison", employeedetails.getFirst_name());
	}
	
	@Test
	public void validateFirstNameControllerUpdateMethod() 
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
		
		testemployeeControllerFirstNameMethod = serviceImpl.getEmployeeByFirstName(
				testemployeeControllerFirstNameMethod.getFirst_name()).get(0);
		
		ResponseEntity<EmployeeResponseBodySuperclass> resp = restController.updateEmployeeFirstName(
				testemployeeControllerFirstNameMethod.getEmployee_id(), "henry");
		
		assertEquals(HttpStatus.OK, resp.getStatusCode());
	}
}
