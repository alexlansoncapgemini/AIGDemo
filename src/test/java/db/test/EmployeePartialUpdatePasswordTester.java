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
import demo.exceptions.EmployeeInvalidPasswordException;
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
//3. direct rest controller calls
//it is in this order that each update method below is tested
@SpringBootTest
public class EmployeePartialUpdatePasswordTester {
	@Mock
	RestTemplate restTemplate;
	static List<Employee> employeelist = new ArrayList<>();
	
	final static EmployeeDatabaseImplementation databaseImpl = new EmployeeDatabaseImplementation();
	final static EmployeeServiceImplementation serviceImpl = new EmployeeServiceImplementation(databaseImpl);
	final static EmployeeRestController restController = new EmployeeRestController(serviceImpl);
	
	@Before
	public void initSetup() {
		MockitoAnnotations.initMocks(this);
		restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
		employeelist.add(EmployeeBeanModifier.convertFromDetails(Optional.ofNullable(
				EmployeeBeanModifier.employeeStringParserNoId(
						EmployeeMessageManager.getVal("testemployeeUpdatePassword")))));
		employeelist.add(EmployeeBeanModifier.convertFromDetails(Optional.ofNullable(
				EmployeeBeanModifier.employeeStringParserNoId(
						EmployeeMessageManager.getVal("testemployeeServiceUpdatePassword")))));
		employeelist.add(EmployeeBeanModifier.convertFromDetails(Optional.ofNullable(
				EmployeeBeanModifier.employeeStringParserNoId(
						EmployeeMessageManager.getVal("testemployeeControllerUpdatePassword")))));
		employeelist.add(EmployeeBeanModifier.convertFromDetails(Optional.ofNullable(
				EmployeeBeanModifier.employeeStringParserNoId(
						EmployeeMessageManager.getVal("testemployeeControllerUpdatePasswordMethod")))));
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
	public void validatePassWordUpdate() 
			throws EmployeeRequestOnNullObjectException, 
			EmployeeInvalidRequestParameterException, 
			EmployeeDuplicateEntryExistsException, 
			EmployeeNotFoundException {
		Employee testemployeePassword = employeelist.get(0);
		
		if(!EmployeeValidator.validateIsEmployeeFirstNameInDatabase(
				databaseImpl, testemployeePassword.getFirst_name())) {
			serviceImpl.newEmployee(testemployeePassword);
		}
		
		assertTrue(EmployeeValidator.validateIsEmployeeFirstNameInDatabase(
				databaseImpl, testemployeePassword.getFirst_name()));
		
		//retrieves the assigned employee id from the database and stores it into the local variable
		testemployeePassword = serviceImpl.getEmployeeByFirstName(testemployeePassword.getFirst_name()).get(0);
		testemployeePassword.setFirst_name("steve");
		databaseImpl.updateEmployeePassWord(testemployeePassword.getEmployee_id(), "tiptop", "toptup");
		
		testemployeePassword = serviceImpl.getEmployeeById(testemployeePassword.getEmployee_id());
		
		assertNotNull(testemployeePassword);
		
		assertEquals("toptup", testemployeePassword.getPass_word());
	}
	
	@Test
	public void validatePassWordServiceUpdate() 
			throws EmployeeInvalidPasswordException, 
			EmployeeInvalidRequestParameterException, 
			EmployeeNotFoundException, 
			EmployeeRequestOnNullObjectException, 
			EmployeeDuplicateEntryExistsException {
		Employee testemployeeServicePassword = employeelist.get(1);
		
		if(!EmployeeValidator.validateIsEmployeeFirstNameInDatabase(
				databaseImpl, testemployeeServicePassword.getFirst_name())) {
			serviceImpl.newEmployee(testemployeeServicePassword);
		}
		
		assertTrue(EmployeeValidator.validateIsEmployeeFirstNameInDatabase(
				databaseImpl, testemployeeServicePassword.getFirst_name()));
		
		//retrieves the assigned employee id from the database and stores it into the local variable
		testemployeeServicePassword = serviceImpl.getEmployeeByFirstName(testemployeeServicePassword.getFirst_name()).get(0);
		testemployeeServicePassword.setFirst_name("steve");
		serviceImpl.updateEmployeePassWord(testemployeeServicePassword.getEmployee_id(), "teptep", "taptep");
		
		testemployeeServicePassword = serviceImpl.getEmployeeById(testemployeeServicePassword.getEmployee_id());
		
		assertNotNull(testemployeeServicePassword);
		
		assertEquals("taptep", testemployeeServicePassword.getPass_word());
	}
	
	@Test
	public void validatePassWordControllerUpdate() 
			throws EmployeeDuplicateEntryExistsException, 
			EmployeeRequestOnNullObjectException, 
			EmployeeInvalidRequestParameterException {
		Employee testemployeeControllerPassword = employeelist.get(2);
		employeeValidationResults validationResult = 
				EmployeeValidator.validateIsThisEmployeeValid(
						Optional.ofNullable(testemployeeControllerPassword));
		
		assertEquals(employeeValidationResults.SUCCESS, validationResult);
		
		if(!EmployeeValidator.validateIsEmployeeFirstNameInDatabase(
				databaseImpl, testemployeeControllerPassword.getFirst_name())) {
			restController.addNewEmployee(
					EmployeeBeanModifier.convertToDetails(Optional.ofNullable(testemployeeControllerPassword)));
		}
		
		assertTrue(EmployeeValidator.validateIsEmployeeFirstNameInDatabase(
				databaseImpl, testemployeeControllerPassword.getFirst_name()));
		
		testemployeeControllerPassword = serviceImpl.getEmployeeByFirstName(
				testemployeeControllerPassword.getFirst_name()).get(0);
		testemployeeControllerPassword.setFirst_name("steve");
		EmployeeResponseBody resp = restTemplate.patchForObject(EmployeeMessageManager.getVal("baseUrl") + 
				EmployeeMessageManager.getVal("partialUpdatePasswordMapping"), 
				null, 
				EmployeeResponseBody.class,
				testemployeeControllerPassword.getEmployee_id(), 
				testemployeeControllerPassword.getPass_word(), 
				"toptip");
		
		EmployeeDetails employeedetails = resp.getEmployeeDetails();
		
		assertEquals("toptip", employeedetails.getPass_word());
	}
	
	@Test
	public void validatePassWordControllerUpdateMethod() 
			throws EmployeeInvalidRequestParameterException, 
			EmployeeNotFoundException, 
			EmployeeInvalidPasswordException, 
			EmployeeRequestOnNullObjectException, 
			EmployeeDuplicateEntryExistsException {
		Employee testemployeeControllerPasswordMethod = employeelist.get(3);
		
		employeeValidationResults validationResult = 
				EmployeeValidator.validateIsThisEmployeeValid(
						Optional.ofNullable(testemployeeControllerPasswordMethod));
		
		assertEquals(employeeValidationResults.SUCCESS, validationResult);
		
		if(!EmployeeValidator.validateIsEmployeeFirstNameInDatabase(
				databaseImpl, testemployeeControllerPasswordMethod.getFirst_name())) {
			serviceImpl.newEmployee(testemployeeControllerPasswordMethod);
		}
		
		assertTrue(EmployeeValidator.validateIsEmployeeFirstNameInDatabase(
				databaseImpl, testemployeeControllerPasswordMethod.getFirst_name()));
		
		testemployeeControllerPasswordMethod = serviceImpl.getEmployeeByFirstName(
				testemployeeControllerPasswordMethod.getFirst_name()).get(0);
		
		ResponseEntity<EmployeeResponseBodySuperclass> resp = restController.updateEmployeePassword(
				testemployeeControllerPasswordMethod.getEmployee_id(), "tiptip", "toptop");
		
		assertEquals(HttpStatus.OK, resp.getStatusCode());
	}
}
