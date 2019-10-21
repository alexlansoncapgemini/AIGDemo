package db.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.openpojo.reflection.PojoClass;
import com.openpojo.reflection.impl.PojoClassFactory;
import com.openpojo.validation.Validator;
import com.openpojo.validation.ValidatorBuilder;
import com.openpojo.validation.rule.impl.GetterMustExistRule;
import com.openpojo.validation.rule.impl.SetterMustExistRule;
import com.openpojo.validation.test.impl.GetterTester;
import com.openpojo.validation.test.impl.SetterTester;

import demo.beans.Employee;
import demo.util.EmployeeMessageManager;
import demo.util.EmployeeMessageManager.employeeValidationResults;
import demo.util.EmployeeBeanModifier;
import demo.util.EmployeeValidator;

public class EmployeeValidatiorBadEmployeeTester {
	@Mock
	Employee bademployee;
	
	@Before
	public void Init() {
		MockitoAnnotations.initMocks(this);
		bademployee = EmployeeBeanModifier.convertFromDetails(	//resets the fields for each test
				Optional.ofNullable(EmployeeBeanModifier.employeeStringParserNoId(
						EmployeeMessageManager.getVal("testemployeeValidatorBadEmployee"))));
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
	//makes sure bademployee exists
	public void nullCheck() {
		employeeValidationResults badResults = 
				EmployeeValidator.validateIsThisEmployeeValid(Optional.ofNullable(bademployee));
		assertNotEquals(employeeValidationResults.NULLARGUMENT, badResults);
	}
	
	@Test 
	public void validateNullEmployee() {
		assertEquals(employeeValidationResults.NULLARGUMENT, 
				EmployeeValidator.validateIsThisEmployeeValid(null));
	}
	
	@Test 
	public void validateBadEmployeeAge() {
		bademployee.setAge(-1);
		assertEquals(employeeValidationResults.INVALIDAGE, 
				EmployeeValidator.validateIsThisEmployeeValid(Optional.ofNullable(bademployee)));
	}
	
	@Test
	public void validateBadEmployeeFirstName() {
		bademployee.setFirst_name(null);
		assertEquals(employeeValidationResults.INVALIDFIRSTNAME, 
				EmployeeValidator.validateIsThisEmployeeValid(Optional.ofNullable(bademployee)));
	}
	
	@Test
	public void validateBadEmployeeLastName() {
		bademployee.setLast_name(";DROP TABLE employee;");
		assertEquals(employeeValidationResults.INVALIDLASTNAME, 
				EmployeeValidator.validateIsThisEmployeeValid(Optional.ofNullable(bademployee)));
	}
	
	@Test
	public void validateBadEmployeePassword() {
		bademployee.setPass_word("pass word");
		assertEquals(employeeValidationResults.INVALIDPASSWORD, 
				EmployeeValidator.validateIsThisEmployeeValid(Optional.ofNullable(bademployee)));
	}
	
	@Test
	public void validateBadEmployeeDepartment() {
		bademployee.setDepartment(" ");
		assertEquals(employeeValidationResults.INVALIDDEPARTMENT, 
				EmployeeValidator.validateIsThisEmployeeValid(Optional.ofNullable(bademployee)));
	}
}
