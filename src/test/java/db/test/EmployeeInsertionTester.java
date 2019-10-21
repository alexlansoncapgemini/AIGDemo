package db.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
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
import demo.exceptions.EmployeeRequestOnNullObjectException;
import demo.responses.EmployeeResponseBodySuperclass;
import demo.service.EmployeeServiceImplementation;
import demo.util.EmployeeMessageManager;
import demo.util.EmployeeBeanModifier;
import demo.util.EmployeeValidator;
import demo.util.EmployeeMessageManager.employeeValidationResults;

//tests employee insertion calls done via raw database object call, 
//rest template calls, and rest controller calls.
@SpringBootTest
public class EmployeeInsertionTester {
	@Mock
	RestTemplate restTemplate;
	static List<Employee> employeelist = new ArrayList<>();
	
	final EmployeeDatabaseImplementation databaseImpl = new EmployeeDatabaseImplementation();
	final EmployeeServiceImplementation serviceImpl = new EmployeeServiceImplementation(databaseImpl);
	final EmployeeRestController restController = new EmployeeRestController(serviceImpl);
	
	@Before
	public void initSetup() {
		MockitoAnnotations.initMocks(this);
		restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
		employeelist.add(EmployeeBeanModifier.convertFromDetails(Optional.ofNullable(
				EmployeeBeanModifier.employeeStringParserNoId(
						EmployeeMessageManager.getVal("testemployeeInsert")))));
		employeelist.add(EmployeeBeanModifier.convertFromDetails(Optional.ofNullable(
				EmployeeBeanModifier.employeeStringParserNoId(
						EmployeeMessageManager.getVal("testemployeeServiceInsert")))));
		employeelist.add(EmployeeBeanModifier.convertFromDetails(Optional.ofNullable(
				EmployeeBeanModifier.employeeStringParserNoId(
						EmployeeMessageManager.getVal("testemployeeControllerInsert")))));
		employeelist.add(EmployeeBeanModifier.convertFromDetails(Optional.ofNullable(
				EmployeeBeanModifier.employeeStringParserNoId(
						EmployeeMessageManager.getVal("testemployeeControllerInsertMethod")))));
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
	public void validateInsert() {
		Employee testemployeeInsert = databaseImpl.addNewEmployee(employeelist.get(0));
		
		assertTrue(EmployeeValidator.validateIsEmployeeFirstNameInDatabase(
				databaseImpl, testemployeeInsert.getFirst_name()));
		
		assertTrue(45 == testemployeeInsert.getAge());
	}
	
	@Test
	public void validateServiceInsert() 
			throws EmployeeRequestOnNullObjectException, 
			EmployeeInvalidRequestParameterException, 
			EmployeeDuplicateEntryExistsException {
		Employee testemployeeServiceInsert = serviceImpl.newEmployee(employeelist.get(1));
		
		assertTrue(EmployeeValidator.validateIsEmployeeFirstNameInDatabase(
				databaseImpl, testemployeeServiceInsert.getFirst_name()));
		
		assertTrue(32 == testemployeeServiceInsert.getAge());
	}
	
	@Test
	public void ValidateControllerInsert() {
		Employee employee = employeelist.get(2);
		employeeValidationResults validationResult = 
				EmployeeValidator.validateIsThisEmployeeValid(Optional.ofNullable(employee));
		
		assertEquals(employeeValidationResults.SUCCESS, validationResult);
		
		EmployeeDetails testemployeeControllerInsert = 
				EmployeeBeanModifier.convertToDetails(Optional.ofNullable(employee));
		
		EmployeeResponseBodySuperclass resp = 
				restTemplate.postForObject(EmployeeMessageManager.getVal("baseUrl") + 
				EmployeeMessageManager.getVal("createMapping"), 
				testemployeeControllerInsert, EmployeeResponseBodySuperclass.class);
		
		assertEquals(201, resp.getStatusCode());
	}
	
	@Test
	public void ValidateControllerInsertMethod() 
			throws EmployeeDuplicateEntryExistsException, 
			EmployeeRequestOnNullObjectException, 
			EmployeeInvalidRequestParameterException {
		EmployeeDetails testemployeeControllerInsertMethod = 
				EmployeeBeanModifier.convertToDetails(Optional.ofNullable(employeelist.get(3)));
		
		ResponseEntity<EmployeeResponseBodySuperclass> respo = 
				restController.addNewEmployee(testemployeeControllerInsertMethod);
				
		assertEquals(HttpStatus.CREATED, respo.getStatusCode());
	}
}
