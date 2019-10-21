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
import demo.util.EmployeeBeanModifier;
import demo.util.EmployeeValidator;
import demo.util.EmployeeMessageManager.employeeValidationResults;

//tests the validation methodd
public class EmployeeValidatiorGoodEmployeeTester {
	@Mock
	Employee goodemployee;
	
	@Before
	public void Init() {
		MockitoAnnotations.initMocks(this);
		goodemployee = EmployeeBeanModifier.convertFromDetails(	//resets the fields for each test
				Optional.ofNullable(EmployeeBeanModifier.employeeStringParserNoId(
						EmployeeMessageManager.getVal("testemployeeValidatorGoodEmployee"))));
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
	public void nullCheck() {
		employeeValidationResults goodResults = 
				EmployeeValidator.validateIsThisEmployeeValid(Optional.of(goodemployee));
		assertNotEquals(employeeValidationResults.NULLARGUMENT, goodResults);
	}
	
	@Test
	public void validateGoodEmployee() {
		assertEquals(employeeValidationResults.SUCCESS, 
				EmployeeValidator.validateIsThisEmployeeValid(Optional.of(goodemployee)));
	}
}
