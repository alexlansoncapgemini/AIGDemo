package db.test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.HttpClientErrorException.NotFound;
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
import demo.responses.EmployeeResponseBodySuperclass;
import demo.service.EmployeeServiceImplementation;
import demo.util.EmployeeMessageManager;
import demo.util.EmployeeBeanModifier;
import demo.util.EmployeeValidator;
import demo.util.EmployeeMessageManager.employeeValidationResults;

//tests the delete method(s) with each way it can be called
//the four ways these can be called are:
//1. raw database (object) calls
//2. service layer method calls
//3. database calls through a rest template
//4. direct rest controller calls
//it is in this order that each delete method call is tested
@SpringBootTest
public class EmployeeDeletionTester {
	@Mock
	RestTemplate restTemplate;
	static List<Employee> employeelist = new ArrayList<>();
	
	final EmployeeDatabaseImplementation databaseImpl = new EmployeeDatabaseImplementation();
	final EmployeeServiceImplementation serviceImpl = new EmployeeServiceImplementation(databaseImpl);
	final EmployeeRestController restController = new EmployeeRestController(serviceImpl);
	
	@BeforeClass
	public static void populateEmployeeList() {
		employeelist.add(EmployeeBeanModifier.convertFromDetails(Optional.ofNullable(
				EmployeeBeanModifier.employeeStringParserNoId(
						EmployeeMessageManager.getVal("testemployeeDelete")))));
		employeelist.add(EmployeeBeanModifier.convertFromDetails(Optional.ofNullable(
				EmployeeBeanModifier.employeeStringParserNoId(
						EmployeeMessageManager.getVal("testemployeeServiceDelete")))));
		employeelist.add(EmployeeBeanModifier.convertFromDetails(Optional.ofNullable(
				EmployeeBeanModifier.employeeStringParserNoId(
						EmployeeMessageManager.getVal("testemployeeControllerDelete")))));
		employeelist.add(EmployeeBeanModifier.convertFromDetails(Optional.ofNullable(
				EmployeeBeanModifier.employeeStringParserNoId(
						EmployeeMessageManager.getVal("testemployeeControllerDeleteMethod")))));
	}
	
	@Before
	public void initSetup() {
		MockitoAnnotations.initMocks(this);
		restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
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
	public void validateDatabaseDelete() 
			throws EmployeeRequestOnNullObjectException, 
			EmployeeInvalidRequestParameterException, 
			EmployeeDuplicateEntryExistsException {
		Employee testemployeeDelete = employeelist.get(0);
		
		if(!EmployeeValidator.validateIsEmployeeFirstNameInDatabase(
				databaseImpl, testemployeeDelete.getFirst_name())) {
			serviceImpl.newEmployee(testemployeeDelete);
		}
		
		assertTrue(EmployeeValidator.validateIsEmployeeFirstNameInDatabase(
				databaseImpl, testemployeeDelete.getFirst_name()));
		
		testemployeeDelete = serviceImpl.getEmployeeByFirstName(testemployeeDelete.getFirst_name()).get(0);
		
		databaseImpl.deleteEmployee(testemployeeDelete.getEmployee_id()); //method test
		
		assertTrue(databaseImpl.getEmployeeById(testemployeeDelete.getEmployee_id()).isEmpty());
	}
	
	@Test
	public void validateServiceDelete() 
			throws EmployeeRequestOnNullObjectException, 
			EmployeeInvalidRequestParameterException, 
			EmployeeDuplicateEntryExistsException, 
			EmployeeNotFoundException{
		Employee testemployeeServiceDelete = employeelist.get(1);
		
		if(!EmployeeValidator.validateIsEmployeeFirstNameInDatabase(
				databaseImpl, testemployeeServiceDelete.getFirst_name())) {
			serviceImpl.newEmployee(testemployeeServiceDelete);
		}
		
		assertTrue(EmployeeValidator.validateIsEmployeeFirstNameInDatabase(
				databaseImpl, testemployeeServiceDelete.getFirst_name()));
		
		final Employee testemployeeServiceDeleteTest = 
				serviceImpl.getEmployeeByFirstName(testemployeeServiceDelete.getFirst_name()).get(0);
		
		serviceImpl.deleteEmployee(testemployeeServiceDeleteTest.getEmployee_id());
		
		ThrowingCallable resp = () ->
				serviceImpl.getEmployeeById(testemployeeServiceDeleteTest.getEmployee_id());
				
		assertThatCode(resp).isInstanceOf(EmployeeNotFoundException.class);
	}
	
	@Test
	public void validateControllerDelete() 
			throws EmployeeRequestOnNullObjectException, 
			EmployeeInvalidRequestParameterException, 
			EmployeeDuplicateEntryExistsException {
		Employee testemployeeControllerDelete = employeelist.get(2);
		
		employeeValidationResults validationResult = 
				EmployeeValidator.validateIsThisEmployeeValid(Optional.ofNullable(testemployeeControllerDelete));
		
		assertEquals(employeeValidationResults.SUCCESS, validationResult);
		
		if(!EmployeeValidator.validateIsEmployeeFirstNameInDatabase(
				databaseImpl, testemployeeControllerDelete.getFirst_name())) {
			serviceImpl.newEmployee(testemployeeControllerDelete);
		}
		
		assertTrue(EmployeeValidator.validateIsEmployeeFirstNameInDatabase(
				databaseImpl, testemployeeControllerDelete.getFirst_name()));
		
		EmployeeDetails emp = EmployeeBeanModifier.convertToDetails(
				Optional.ofNullable(serviceImpl.getEmployeeByFirstName(
						testemployeeControllerDelete.getFirst_name()).get(0)));
		
		restTemplate.delete(EmployeeMessageManager.getVal("baseUrl") + 
				EmployeeMessageManager.getVal("deleteMapping"), emp.getEmployee_id());
		
		ThrowingCallable resp2 = () ->
				restTemplate.getForObject(EmployeeMessageManager.getVal("baseUrl") + 
						EmployeeMessageManager.getVal("getEmployeeMapping"), 
						EmployeeResponseBodySuperclass.class, 
						emp.getEmployee_id());
				
		assertThatCode(resp2).isInstanceOf(NotFound.class);
	}
	
	@Test
	public void validateControllerDeleteMethod() 
			throws EmployeeNotFoundException, 
			EmployeeRequestOnNullObjectException, 
			EmployeeInvalidRequestParameterException, 
			EmployeeDuplicateEntryExistsException {
		Employee testemployeeControllerDeleteMethod = employeelist.get(3);
		
		employeeValidationResults validationResult = 
				EmployeeValidator.validateIsThisEmployeeValid(
						Optional.ofNullable(testemployeeControllerDeleteMethod));
		
		assertEquals(employeeValidationResults.SUCCESS, validationResult);
		
		if(!EmployeeValidator.validateIsEmployeeFirstNameInDatabase(
				databaseImpl, testemployeeControllerDeleteMethod.getFirst_name())) {
			serviceImpl.newEmployee(testemployeeControllerDeleteMethod);
		}
		
		assertTrue(EmployeeValidator.validateIsEmployeeFirstNameInDatabase(
				databaseImpl, testemployeeControllerDeleteMethod.getFirst_name()));
		
		testemployeeControllerDeleteMethod = 
				serviceImpl.getEmployeeByFirstName(testemployeeControllerDeleteMethod.getFirst_name()).get(0);
		
		ResponseEntity<EmployeeResponseBodySuperclass> resp = 
				restController.deleteEmployee(testemployeeControllerDeleteMethod.getEmployee_id());
		
		assertEquals(HttpStatus.OK, resp.getStatusCode());
	}
}
