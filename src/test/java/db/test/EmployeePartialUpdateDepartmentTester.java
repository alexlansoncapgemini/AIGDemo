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
//2. service layer calls
//2. database calls through a rest template
//3. direct rest controller calls
//it is in this order that each update method below is tested
public class EmployeePartialUpdateDepartmentTester {
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
	
	@Test
	public void validateDepartmentUpdate() 
			throws EmployeeRequestOnNullObjectException, 
			EmployeeInvalidRequestParameterException, 
			EmployeeDuplicateEntryExistsException, 
			EmployeeNotFoundException {
		Employee testemployeeDepartment = employeelist.get(0);
		
		if(!EmployeeValidator.validateIsEmployeeFirstNameInDatabase(
				databaseImpl, testemployeeDepartment.getFirst_name())) {
			serviceImpl.newEmployee(testemployeeDepartment);
		}
		
		assertTrue(EmployeeValidator.validateIsEmployeeFirstNameInDatabase(
				databaseImpl, testemployeeDepartment.getFirst_name()));
		
		//retrieves the assigned employee id from the database and stores it into the local variable
		testemployeeDepartment = serviceImpl.getEmployeeByFirstName(testemployeeDepartment.getFirst_name()).get(0);
		
		databaseImpl.updateEmployeeDepartment(testemployeeDepartment.getEmployee_id(), "management");
		
		testemployeeDepartment = serviceImpl.getEmployeeById(testemployeeDepartment.getEmployee_id());
		
		assertNotNull(testemployeeDepartment);
		
		assertEquals("management", testemployeeDepartment.getDepartment());
	}
	
	@Test
	public void validateServiceDepartmentUpdate() 
			throws EmployeeRequestOnNullObjectException, 
			EmployeeInvalidRequestParameterException, 
			EmployeeDuplicateEntryExistsException, EmployeeNotFoundException {
		Employee testemployeeServiceDepartment = employeelist.get(1);
		
		if(!EmployeeValidator.validateIsEmployeeFirstNameInDatabase(
				databaseImpl, testemployeeServiceDepartment.getFirst_name())) {
			serviceImpl.newEmployee(testemployeeServiceDepartment);
		}
		
		assertTrue(EmployeeValidator.validateIsEmployeeFirstNameInDatabase(
				databaseImpl, testemployeeServiceDepartment.getFirst_name()));
		
		//retrieves the assigned employee id from the database and stores it into the local variable
		testemployeeServiceDepartment = serviceImpl.getEmployeeByFirstName(testemployeeServiceDepartment.getFirst_name()).get(0);
		
		serviceImpl.updateEmployeeDepartment(testemployeeServiceDepartment.getEmployee_id(), "management");
		
		testemployeeServiceDepartment = serviceImpl.getEmployeeById(testemployeeServiceDepartment.getEmployee_id());
		
		assertNotNull(testemployeeServiceDepartment);
		
		assertEquals("management", testemployeeServiceDepartment.getDepartment());
	}
	
	@Test
	public void validateDepartmentControllerUpdate() 
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
		
		restTemplate.patchForObject(EmployeeMessageManager.getVal("baseUrl") + 
				EmployeeMessageManager.getVal("partialUpdateDepartmentMapping"), 
				null, EmployeeResponseBody.class, testemployeeControllerDepartment.getEmployee_id(), "management");
		
		EmployeeResponseBody resp = restTemplate.getForObject(EmployeeMessageManager.getVal("baseUrl") + 
				EmployeeMessageManager.getVal("getEmployeeMapping"), 
				EmployeeResponseBody.class, testemployeeControllerDepartment.getEmployee_id());
		
		EmployeeDetails employeedetails = resp.getEmployeeDetails();
		
		assertEquals("management", employeedetails.getDepartment());
	}
	
	@Test
	public void validateDepartmentControllerUpdateMethod() 
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
		
		testemployeeControllerDepartmentMethod = serviceImpl.getEmployeeByFirstName(
				testemployeeControllerDepartmentMethod.getFirst_name()).get(0);
		
		ResponseEntity<EmployeeResponseBodySuperclass> resp = 
				restController.updateEmployeeDepartment(
						testemployeeControllerDepartmentMethod.getEmployee_id(), "management");
		
		assertEquals(HttpStatus.OK, resp.getStatusCode());
	}
}
