package db.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

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
import demo.responses.EmployeeListResponseBody;
import demo.responses.EmployeeResponseBodySuperclass;
import demo.service.EmployeeServiceImplementation;
import demo.util.EmployeeMessageManager;
import demo.util.EmployeeValidator;
import demo.util.EmployeeMessageManager.employeeValidationResults;
import demo.util.EmployeeBeanModifier;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;


//tests the getAll (all employees) employee read methods
@FieldDefaults(level=AccessLevel.PRIVATE)
public class EmployeeReadAllTester {
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
		//the three employee objects to be called in the below tests
		employeelist.add(EmployeeBeanModifier.convertFromDetails(Optional.ofNullable(
				EmployeeBeanModifier.employeeStringParserNoId(
						EmployeeMessageManager.getVal("testemployeeRead")))));
		employeelist.add(EmployeeBeanModifier.convertFromDetails(Optional.ofNullable(
				EmployeeBeanModifier.employeeStringParserNoId(
						EmployeeMessageManager.getVal("testemployeeControllerRead")))));
		employeelist.add(EmployeeBeanModifier.convertFromDetails(Optional.ofNullable(
				EmployeeBeanModifier.employeeStringParserNoId(
						EmployeeMessageManager.getVal("testemployeeControllerReadMethod")))));
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
	public void validateDatabaseReadAll() 
			throws EmployeeRequestOnNullObjectException, 
			EmployeeInvalidRequestParameterException, 
			EmployeeDuplicateEntryExistsException {
		Employee testemployeeReadAll = employeelist.get(0);
		employeeValidationResults validationResult = 
				EmployeeValidator.validateIsThisEmployeeValid(
						Optional.ofNullable(testemployeeReadAll));
		
		assertEquals(employeeValidationResults.SUCCESS, validationResult);
		
		if(!EmployeeValidator.validateIsEmployeeFirstNameInDatabase(
				databaseImpl, testemployeeReadAll.getFirst_name())) {
			serviceImpl.newEmployee(testemployeeReadAll);
		}
		
		List<Employee> allemployees = databaseImpl.getAllEmployees();
				
		assertFalse(allemployees.isEmpty());
	}
	
	@Test
	public void validateServiceReadAll() 
			throws EmployeeNotFoundException, 
			EmployeeRequestOnNullObjectException, 
			EmployeeInvalidRequestParameterException, 
			EmployeeDuplicateEntryExistsException {
		Employee testemployeeServiceReadAll = employeelist.get(1);
		employeeValidationResults validationResult = 
				EmployeeValidator.validateIsThisEmployeeValid(
						Optional.ofNullable(testemployeeServiceReadAll));
		
		assertEquals(employeeValidationResults.SUCCESS, validationResult);
		
		if(!EmployeeValidator.validateIsEmployeeFirstNameInDatabase(
				databaseImpl, testemployeeServiceReadAll.getFirst_name())) {
			serviceImpl.newEmployee(testemployeeServiceReadAll);
		}
		
		List<Employee> allemployees = serviceImpl.getAllEmployees();
		assertFalse(allemployees.isEmpty());
	}
	
	@Test
	public void validateControllerReadAll() 
			throws EmployeeRequestOnNullObjectException, 
			EmployeeInvalidRequestParameterException, 
			EmployeeDuplicateEntryExistsException {
		Employee testemployeeControllerReadAll = employeelist.get(1);
		employeeValidationResults validationResult = 
				EmployeeValidator.validateIsThisEmployeeValid(
						Optional.ofNullable(testemployeeControllerReadAll));
		
		assertEquals(employeeValidationResults.SUCCESS, validationResult);
		
		if(!EmployeeValidator.validateIsEmployeeFirstNameInDatabase(
				databaseImpl, testemployeeControllerReadAll.getFirst_name())) {
			serviceImpl.newEmployee(testemployeeControllerReadAll);
		}
		
		EmployeeListResponseBody resp = restTemplate.getForObject(EmployeeMessageManager.getVal("baseUrl") + 
				EmployeeMessageManager.getVal("findAllMapping"), EmployeeListResponseBody.class);
		
		assertEquals(200, resp.getStatusCode());
	}
	
	@Test
	public void validateControllerReadAllMethod() 
			throws EmployeeNotFoundException, 
			EmployeeRequestOnNullObjectException, 
			EmployeeInvalidRequestParameterException, 
			EmployeeDuplicateEntryExistsException {
		Employee testemployeeControllerReadAllMethod = employeelist.get(2);
		employeeValidationResults validationResult = 
				EmployeeValidator.validateIsThisEmployeeValid(
						Optional.ofNullable(testemployeeControllerReadAllMethod));
		
		assertEquals(employeeValidationResults.SUCCESS, validationResult);
		
		if(!EmployeeValidator.validateIsEmployeeFirstNameInDatabase(
				databaseImpl, testemployeeControllerReadAllMethod.getFirst_name())) {
			serviceImpl.newEmployee(testemployeeControllerReadAllMethod);
		}
		
		ResponseEntity<EmployeeResponseBodySuperclass> resp = restController.getAllEmployees();
		
		assertEquals(HttpStatus.OK, resp.getStatusCode());
	}
}
