package db.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
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
import demo.service.EmployeeServiceImplementation;
import demo.util.EmployeeBeanModifier;
import demo.util.EmployeeMessageManager;
import demo.util.EmployeeValidator;

//tests certain functionality of the employee beans
//this includes the employee beans folder and the employee bean modifier
public class EmployeeBeanTester {
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
						EmployeeMessageManager.getVal("testemployeeBean")))));
		employeelist.add(EmployeeBeanModifier.convertFromDetails(Optional.ofNullable(
				EmployeeBeanModifier.employeeStringParserNoId(
						EmployeeMessageManager.getVal("testemployeeDetailsBean")))));
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
	public void validateParseEmployee() 
			throws EmployeeRequestOnNullObjectException, 
			EmployeeInvalidRequestParameterException, 
			EmployeeDuplicateEntryExistsException {
		Employee testemployeeBean = employeelist.get(0);
			
		if(!EmployeeValidator.validateIsEmployeeFirstNameInDatabase(
				databaseImpl, testemployeeBean.getFirst_name())) {
			serviceImpl.newEmployee(testemployeeBean);
		}
		
		assertTrue(EmployeeValidator.validateIsEmployeeFirstNameInDatabase(
				databaseImpl, testemployeeBean.getFirst_name()));
		
		testemployeeBean = serviceImpl.getEmployeeByFirstName(
				testemployeeBean.getFirst_name()).get(0);
		
		testemployeeBean = databaseImpl.getEmployeeById(
				testemployeeBean.getEmployee_id()).get(0);
		
		assertNotNull(testemployeeBean);
		
		//testing conversions
		String employeeString = testemployeeBean.toString();
		
		testemployeeBean = EmployeeBeanModifier.convertFromDetails(Optional.ofNullable(
				EmployeeBeanModifier.employeeStringParser(employeeString)));
		
		assertNotNull(testemployeeBean);
	}

	@Test
	public void validateEmployeeEquals() {
		Employee employeeBean = EmployeeBeanModifier.convertFromDetails(Optional.ofNullable(
				EmployeeBeanModifier.employeeStringParserNoId(
						EmployeeMessageManager.getVal("testemployeeBean"))));
		
		assertTrue(employeeBean.equals(employeelist.get(0)));
	}
	
	@Test
	public void validateParseEmployeeDetails() {
		EmployeeDetails testemployeeDetailsBean = 
				EmployeeBeanModifier.convertToDetails(Optional.ofNullable(employeelist.get(1)));
		
		//testing conversions
		String employeeDetailsString = testemployeeDetailsBean.toString();
		
		testemployeeDetailsBean = EmployeeBeanModifier.employeeStringParser(employeeDetailsString);
		
		assertNotNull(testemployeeDetailsBean);
	}
	
	@Test
	public void validateEmployeeDetailsEquals() {
		EmployeeDetails employeeDetailsBean = EmployeeBeanModifier.employeeStringParserNoId(
						EmployeeMessageManager.getVal("testemployeeDetailsBean"));
		
		assertTrue(employeeDetailsBean.equals(
				EmployeeBeanModifier.convertToDetails(Optional.ofNullable(employeelist.get(1)))));
	}
}
