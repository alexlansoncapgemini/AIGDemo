package db.test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException.NotFound;

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
import demo.exceptions.EmployeeNotFoundException;
import demo.responses.EmployeeResponseBodySuperclass;
import demo.service.EmployeeServiceImplementation;
import demo.util.EmployeeMessageManager;
import demo.util.EmployeeValidator;

public class EmployeeSpecificReadFailTester {
	@Mock
	RestTemplate restTemplate;
	
	final EmployeeDatabaseImplementation databaseImpl = new EmployeeDatabaseImplementation();
	final EmployeeServiceImplementation serviceImpl = new EmployeeServiceImplementation(databaseImpl);
	final EmployeeRestController restController = new EmployeeRestController(serviceImpl);
	
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
	public void validateSpecificReadFail() {
		assertTrue(databaseImpl.getEmployeeById(5000).isEmpty());	
	}
	
	@Test(expected = EmployeeNotFoundException.class)
	public void validateServiceSpecificReadFail() throws EmployeeNotFoundException {
		serviceImpl.getEmployeeById(50000);	//fail case
	}
	
	@Test
	public void validateControllerSpecificReadFail() {
		//failure case
		assertFalse(EmployeeValidator.validateIsEmployeeFirstNameInDatabase(databaseImpl, "abcde"));
		
		ThrowingCallable resp = () ->		
			restTemplate.getForObject(EmployeeMessageManager.getVal("baseUrl") + 
					EmployeeMessageManager.getVal("getEmployeeMapping"), 
						EmployeeResponseBodySuperclass.class, 50000);	
		
		assertThatCode(resp).isInstanceOf(NotFound.class);
	}
	
	@Test(expected = EmployeeNotFoundException.class)
	public void validateControllerSpecificReadMethodFail() throws EmployeeNotFoundException {
		restController.getEmployeeById(500000);	
	}
}
