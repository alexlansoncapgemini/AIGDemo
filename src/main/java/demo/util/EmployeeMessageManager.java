package demo.util;

import java.util.ResourceBundle;

import lombok.Getter;
import lombok.experimental.UtilityClass;

//this keeps the method used to return any string literals, log messages, or query strings
//stored in the app.properties file
@UtilityClass
public class EmployeeMessageManager {
	
	//accesses the app.properties file in the resources folder
	ResourceBundle props = ResourceBundle.getBundle("app");	
	
	//returns string values (stored in the app.properties file) for a given key
	public String getVal(String key) {
		return props.getString(key);
	}
	
	@Getter
	//originally these were stored in app.properties, but SonarLint
	//kept yelling at me for calling their stored variable names too many times
	//so now they're here
	public enum commonLiterals {
		AGE("age"),
		DEPARTMENT("department"),
		FIRSTNAME("first_name"),
		LASTNAME("last_name"),
		EMPLOYEEID("employee_id"),
		PASSWORD("pass_word");
		
		String literal;
		
		private commonLiterals(String literal) {
			this.literal = literal;
		}
	}

	@Getter
	//possible results for calling the method to validate an employee
	//along with descriptive messages for what they mean
	//the full messages are stored in app.properties
	public enum employeeValidationResults{
		SUCCESS(getVal("validationSuccessEmployee")),
		INVALIDAGE(getVal("validationErrorInvalidAge") + getVal("minimumAge")),
		INVALIDFIRSTNAME(getVal("validationErrorInvalidFirstName")),
		INVALIDLASTNAME(getVal("validationErrorInvalidLastName")),
		INVALIDPASSWORD(getVal("validationErrorInvalidPassword")),
		INVALIDDEPARTMENT(getVal("validationErrorInvalidDepartment")),
		NULLARGUMENT(getVal("validationErrorNullArgument")),
		UNKNOWNVALIDATIONERROR(getVal("validationErrorGeneric"));
		
		String message;
		
		private employeeValidationResults(String message) {
			this.message = message;
		}
	}
	
	@Getter
	//these error log messages are called so many times that they warrant a containing enum
	public enum commonErrorLogs{
		UPDATINGERRORWRONGPASSWORD(getVal("errorUpdatingWrongPassword")),
		UPDATINGERROREMPLOYEENOTFOUND(getVal("errorUpdatingEmployeeNotFound")),
		UPDATINGERRORVALIDATIONFAILED(getVal("errorUpdatingValidationFailed")),
		FINDINGERROREMPLOYEEIDNOTFOUND(getVal("errorEmployeeIdNotFound"));
		
		String logMessage;
		
		private commonErrorLogs(String logMessage) {
			this.logMessage = logMessage;
		}
	}
}
