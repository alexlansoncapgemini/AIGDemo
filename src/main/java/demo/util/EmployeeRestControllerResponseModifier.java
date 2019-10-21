package demo.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;

import demo.beans.Employee;
import demo.beans.EmployeeDetails;
import demo.responses.EmployeeListResponseBody;
import demo.responses.EmployeeResponseBody;
import demo.responses.EmployeeResponseBodySuperclass;
import demo.util.EmployeeMessageManager.employeeValidationResults;
import lombok.experimental.UtilityClass;

@UtilityClass
//helps form the responses for the rest controller
//a place to keep the rest logic instead of in the controller itself
public class EmployeeRestControllerResponseModifier {
	
	public EmployeeResponseBodySuperclass getAllEmployeesResponseBuilder(List<Employee> allEmployees) {
		EmployeeListResponseBody response = new EmployeeListResponseBody();
		
		List<EmployeeDetails> allEmployeesDetails = new ArrayList<>();
		for(Employee employee : allEmployees) {
			if(EmployeeValidator.validateIsThisEmployeeValid(Optional.ofNullable(employee))
					== employeeValidationResults.SUCCESS) {
				allEmployeesDetails.add(EmployeeBeanModifier.convertToDetails(Optional.ofNullable(employee)));
			}
			
		}
		
		response.setStatusCode(HttpStatus.OK.value());
		response.setStatusName(HttpStatus.OK.name());
		response.setStatusDescription(EmployeeMessageManager.getVal("foundEmployeeList"));
		response.setEmployeeList(allEmployeesDetails);
		return response;
	}
	
	public EmployeeResponseBodySuperclass getEmployeeByIdResponseBuilder(Employee employee) {
		EmployeeResponseBody response = new EmployeeResponseBody();
		
		response.setStatusCode(HttpStatus.OK.value());
		response.setStatusName(HttpStatus.OK.name());
		response.setStatusDescription(EmployeeMessageManager.getVal("foundEmployee"));
		
		response.setEmployeeDetails(EmployeeBeanModifier.convertToDetails(Optional.ofNullable(employee))); 
		return response;															    
	}
	
	public EmployeeResponseBodySuperclass addNewEmployeeResponseBuilder(Employee employee) {
		
		EmployeeResponseBody response = new EmployeeResponseBody();
		response.setStatusCode(HttpStatus.CREATED.value());
		response.setStatusName(HttpStatus.CREATED.name());
		response.setStatusDescription(EmployeeMessageManager.getVal("addEmployeeSuccess"));
		
		response.setEmployeeDetails(EmployeeBeanModifier.convertToDetails(Optional.ofNullable(employee)));
		return response;
	}
	
	public EmployeeResponseBodySuperclass deleteEmployeeResponseBuilder() {
		//we arent returning any employee information, 
		//so we only need the standard response body superclass
		EmployeeResponseBodySuperclass response = new EmployeeResponseBodySuperclass();

		response.setStatusCode(HttpStatus.OK.value());
		response.setStatusName(HttpStatus.OK.name());
		response.setStatusDescription(EmployeeMessageManager.getVal("deletingEmployee"));
		
		return response;
	}
	
	public EmployeeResponseBodySuperclass updateEmployeeFirstNameResponseBuilder(Employee employee) {
		
		EmployeeResponseBody response = new EmployeeResponseBody();
		response.setStatusCode(HttpStatus.OK.value());
		response.setStatusName(HttpStatus.OK.name());
		response.setStatusDescription(EmployeeMessageManager.getVal("updatingEmployeeFirstName"));
		
		response.setEmployeeDetails(
				EmployeeBeanModifier.convertToDetails(Optional.ofNullable(employee)));
		return response;
	}
	
	public EmployeeResponseBodySuperclass updateEmployeeLastNameResponseBuilder(Employee employee) {
		
		EmployeeResponseBody response = new EmployeeResponseBody();
		response.setStatusCode(HttpStatus.OK.value());
		response.setStatusName(HttpStatus.OK.name());
		response.setStatusDescription(EmployeeMessageManager.getVal("updatingEmployeeLastName"));
		
		response.setEmployeeDetails(
				EmployeeBeanModifier.convertToDetails(Optional.ofNullable(employee)));
		return response;
	}
	
	public EmployeeResponseBodySuperclass updateEmployeePasswordResponseBuilder(Employee employee) {
		
		EmployeeResponseBody response = new EmployeeResponseBody();
		response.setStatusCode(HttpStatus.OK.value());
		response.setStatusName(HttpStatus.OK.name());
		response.setStatusDescription(EmployeeMessageManager.getVal("updatingEmployeePassword"));
		
		response.setEmployeeDetails(
				EmployeeBeanModifier.convertToDetails(Optional.ofNullable(employee)));
		return response;
	}
	
	public EmployeeResponseBodySuperclass updateEmployeeAgeResponseBuilder(Employee employee) {
		
		EmployeeResponseBody response = new EmployeeResponseBody();
		response.setStatusCode(HttpStatus.OK.value());
		response.setStatusName(HttpStatus.OK.name());
		response.setStatusDescription(EmployeeMessageManager.getVal("updatingEmployeeAge"));
		
		response.setEmployeeDetails(
				EmployeeBeanModifier.convertToDetails(Optional.ofNullable(employee)));
		
		return response;
	}
	
	public EmployeeResponseBodySuperclass updateEmployeeDepartmentResponseBuilder(Employee employee) {
		
		EmployeeResponseBody response = new EmployeeResponseBody();
		response.setStatusCode(HttpStatus.OK.value());
		response.setStatusName(HttpStatus.OK.name());
		response.setStatusDescription(EmployeeMessageManager.getVal("updatingEmployeeDepartment"));
		
		response.setEmployeeDetails(
				EmployeeBeanModifier.convertToDetails(Optional.ofNullable(employee)));
		return response;
	}
	
}
