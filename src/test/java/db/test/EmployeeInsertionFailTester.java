package db.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException.BadRequest;
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
import demo.beans.EmployeeDetails;
import demo.controller.EmployeeRestController;
import demo.dao.EmployeeDatabaseImplementation;
import demo.exceptions.EmployeeDuplicateEntryExistsException;
import demo.exceptions.EmployeeInvalidRequestParameterException;
import demo.exceptions.EmployeeRequestOnNullObjectException;
import demo.responses.EmployeeResponseBodySuperclass;
import demo.service.EmployeeServiceImplementation;
import demo.util.EmployeeBeanModifier;
import demo.util.EmployeeMessageManager;
import demo.util.EmployeeValidator;

public class EmployeeInsertionFailTester {
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
	
	@Test(expected = EmployeeInvalidRequestParameterException.class)
	public void validateServiceInsertFailInvalid() 
			throws EmployeeRequestOnNullObjectException, 
			EmployeeInvalidRequestParameterException, 
			EmployeeDuplicateEntryExistsException {
		Employee testemployeeServiceInsert = employeelist.get(1);
		testemployeeServiceInsert.setFirst_name("first name");
		
		testemployeeServiceInsert = serviceImpl.newEmployee(testemployeeServiceInsert);
	}
	
	@Test(expected = EmployeeDuplicateEntryExistsException.class)
	public void validateServiceInsertFailDuplicate() 
			throws EmployeeRequestOnNullObjectException, 
			EmployeeInvalidRequestParameterException, 
			EmployeeDuplicateEntryExistsException {
		Employee testemployeeServiceInsert = employeelist.get(1);
		
		if(EmployeeValidator.validateIsEmployeeFirstNameInDatabase(
				databaseImpl, testemployeeServiceInsert.getFirst_name())) {
			serviceImpl.newEmployee(testemployeeServiceInsert);
		}
		else {
			serviceImpl.newEmployee(testemployeeServiceInsert);
			serviceImpl.newEmployee(testemployeeServiceInsert);
		}
	}
	
	@Test(expected = UnprocessableEntity.class)
	public void ValidateControllerInsertFailInvalid() {
		EmployeeDetails testemployeeBadControllerInsert = 
				EmployeeBeanModifier.convertToDetails(Optional.ofNullable(employeelist.get(2)));
		
		testemployeeBadControllerInsert.setFirst_name("fail case");
		
		restTemplate.postForObject(EmployeeMessageManager.getVal("baseUrl") + 
				EmployeeMessageManager.getVal("createMapping"), 
				testemployeeBadControllerInsert, EmployeeResponseBodySuperclass.class);
	}
	
	@Test(expected = BadRequest.class)
	public void ValidateControllerInsertFailDuplicate() {
		EmployeeDetails testemployeeBadControllerInsert = 
				EmployeeBeanModifier.convertToDetails(Optional.ofNullable(employeelist.get(2)));
		
		restTemplate.postForObject(EmployeeMessageManager.getVal("baseUrl") + 
				EmployeeMessageManager.getVal("createMapping"), 
				testemployeeBadControllerInsert, EmployeeResponseBodySuperclass.class);
		
		restTemplate.postForObject(EmployeeMessageManager.getVal("baseUrl") + 
				EmployeeMessageManager.getVal("createMapping"), 
				testemployeeBadControllerInsert, EmployeeResponseBodySuperclass.class);
	}
	
	@Test(expected = EmployeeInvalidRequestParameterException.class)
	public void ValidateControllerInsertMethodFailInvalid() 
			throws EmployeeDuplicateEntryExistsException, 
			EmployeeRequestOnNullObjectException, 
			EmployeeInvalidRequestParameterException {
		EmployeeDetails testemployeeControllerInsertMethod = 
				EmployeeBeanModifier.convertToDetails(Optional.ofNullable(employeelist.get(3)));
		
		testemployeeControllerInsertMethod.setAge(10);
		
		restController.addNewEmployee(testemployeeControllerInsertMethod);
	}
	
	@Test(expected = EmployeeDuplicateEntryExistsException.class)
	public void ValidateControllerInsertMethodFailDuplicate() 
			throws EmployeeDuplicateEntryExistsException, 
			EmployeeRequestOnNullObjectException, 
			EmployeeInvalidRequestParameterException {
		EmployeeDetails testemployeeControllerInsertMethod = 
				EmployeeBeanModifier.convertToDetails(Optional.ofNullable(employeelist.get(3)));
		
		restController.addNewEmployee(testemployeeControllerInsertMethod);
		restController.addNewEmployee(testemployeeControllerInsertMethod);
	}
}
