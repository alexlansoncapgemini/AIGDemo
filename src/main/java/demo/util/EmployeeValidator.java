package demo.util;

import java.util.Optional;

import demo.beans.Employee;
import demo.dao.EmployeeDatabaseImplementation;
import demo.util.EmployeeMessageManager.employeeValidationResults;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
//static class containing various validator functions 
//these functions take in objects (e.g. employee objects) and determine
//if the object is valid (i.e. the object isn't null and its fields contain valid data)
@UtilityClass
public class EmployeeValidator {
	
	//checks whether a string contains any non-alphanumeric characters
	public Boolean containsNullOrNonAlphanumericValue(Optional<String> string) {	//I would use a one-line regex check
		if(string.isPresent()) {													//but this is faster 
			for(char c : string.get().toCharArray()) {	
				if(!Character.isLetterOrDigit(c)) {
					return true;
				}
			}
			return false;
		}
		return true;
	}
	
	//this seems like overkill for a simple numerical comparison
	//but this way I wont have to retrieve and convert the minimum age from app.properties
	//every time I want it. Having this method do it instead makes the code a bit cleaner
	public Boolean isTooYoung(int age) {
		int minimumAge = Integer.parseInt(EmployeeMessageManager.getVal("minimumAge"));
		return (age < minimumAge);
	}
	//checks if a employee object contains valid field values
	public employeeValidationResults validateIsThisEmployeeValid(Optional<Employee> employee) {
		try {
			if(employee.isPresent()) {
				if(isTooYoung(employee.get().getAge())) {
					log.debug(employeeValidationResults.INVALIDAGE.getMessage());
					return employeeValidationResults.INVALIDAGE;
				}
				if(containsNullOrNonAlphanumericValue(Optional.ofNullable((employee.get().getFirst_name())))) {
					log.debug(employeeValidationResults.INVALIDFIRSTNAME.getMessage());
					return employeeValidationResults.INVALIDFIRSTNAME;
				}
				if(containsNullOrNonAlphanumericValue(Optional.ofNullable(employee.get().getLast_name()))) {
					log.debug(employeeValidationResults.INVALIDLASTNAME.getMessage());
					return employeeValidationResults.INVALIDLASTNAME;
				}
				if(containsNullOrNonAlphanumericValue(Optional.ofNullable(employee.get().getPass_word()))) {
					log.debug(employeeValidationResults.INVALIDPASSWORD.getMessage());
					return employeeValidationResults.INVALIDPASSWORD;
				}
				if(containsNullOrNonAlphanumericValue(Optional.ofNullable(employee.get().getDepartment()))) {
					log.debug(employeeValidationResults.INVALIDDEPARTMENT.getMessage());
					return employeeValidationResults.INVALIDDEPARTMENT;
				}
				log.info(employeeValidationResults.SUCCESS.getMessage());
				return employeeValidationResults.SUCCESS;
			}
			else {
				log.debug(employeeValidationResults.NULLARGUMENT.getMessage());
				return employeeValidationResults.NULLARGUMENT;
			}
			
		}
		catch(NullPointerException npe) {
			log.debug(employeeValidationResults.NULLARGUMENT.getMessage());
			return employeeValidationResults.NULLARGUMENT;
		}
		catch(Exception e) {
			log.debug(employeeValidationResults.UNKNOWNVALIDATIONERROR.getMessage() 
					+ " Exception message: " + e.getLocalizedMessage());
			return employeeValidationResults.UNKNOWNVALIDATIONERROR;
		}
	}
	
	public Boolean validateIsEmployeeIdInDatabase(EmployeeDatabaseImplementation db, int eid) {
		return !db.getEmployeeById(eid).isEmpty();	//is there a employee with this id in the database? 
												//if not, return false
	}
	
	public Boolean validateIsEmployeeFirstNameInDatabase(EmployeeDatabaseImplementation db, String fname) {
		return !db.getEmployeeByFirstName(fname).isEmpty(); //is there a employee with this name in the database? 
															//if not, return false
	}
}
