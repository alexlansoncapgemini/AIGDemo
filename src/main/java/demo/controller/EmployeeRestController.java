package demo.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import demo.beans.Employee;
import demo.beans.EmployeeDetails;
import demo.exceptions.EmployeeDuplicateEntryExistsException;
import demo.exceptions.EmployeeInvalidPasswordException;
import demo.exceptions.EmployeeInvalidRequestParameterException;
import demo.exceptions.EmployeeNotFoundException;
import demo.exceptions.EmployeeRequestOnNullObjectException;
import demo.responses.EmployeeResponseBodySuperclass;
import demo.service.EmployeeServiceImplementation;
import demo.util.EmployeeBeanModifier;
import demo.util.EmployeeRestControllerResponseModifier;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;


//This is the rest api controller. 
//it maps http requests to various database call methods
@RestController
@RequestMapping		//goes after the context path
@FieldDefaults(level=AccessLevel.PRIVATE)
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings({"unchecked","rawtypes"}) /*deals with the warnings otherwise
 											 *caused by returning raw ResponseEntity
 											 *objects (i.e. ResponseEntity instead
 											 *of ResponseEntity<SomeType>)*/
public class EmployeeRestController {
	@Autowired									
	EmployeeServiceImplementation serviceImpl;	//object for service layer method calls
												//the service class also has a database class object
												//this is to ensure that the same database instance is being
												//accessed at every level of the program 
	
	//gets a list of all employees (if there are any)
	//mappings have to be string constants or the program won't compile
	@GetMapping(value="/employees/findall", produces = MediaType.APPLICATION_JSON_VALUE)	
	@ResponseBody
	public ResponseEntity<EmployeeResponseBodySuperclass> getAllEmployees() throws EmployeeNotFoundException{
		
		List<Employee> allEmployees = serviceImpl.getAllEmployees();	//database call
		
		EmployeeResponseBodySuperclass response = 
				EmployeeRestControllerResponseModifier.getAllEmployeesResponseBuilder(allEmployees);
		
		return new ResponseEntity<>(response, HttpStatus.resolve(response.getStatusCode()));
	}
	
	//looks for a specific employee, returns that employee if found
	@GetMapping(value="/employees/getemployee/{employee_id}", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<EmployeeResponseBodySuperclass> getEmployeeById(
			@PathVariable("employee_id")Integer eid) throws EmployeeNotFoundException{
		
		Employee employee = serviceImpl.getEmployeeById(eid); //database call 
		EmployeeResponseBodySuperclass response = 						
				EmployeeRestControllerResponseModifier.getEmployeeByIdResponseBuilder(
						employee); 
		
		return new ResponseEntity<>(response, HttpStatus.resolve(response.getStatusCode()));		
	}
	
	//inserts a new employee into the database 
	@PostMapping(value="/employees/create", headers = "Accept=application/json", 
			produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<EmployeeResponseBodySuperclass> addNewEmployee(
			//the EmployeeDetails is less of a security risk than a persistent entity (e.g. a Employee object)
			//for that reason, http calls with persistent entities are not permitted
			@RequestBody EmployeeDetails employeedetails) 
					throws EmployeeDuplicateEntryExistsException, 
					EmployeeRequestOnNullObjectException, 
					EmployeeInvalidRequestParameterException{
		
		Employee employee = 
				serviceImpl.newEmployee(
						EmployeeBeanModifier.convertFromDetails(Optional.ofNullable(employeedetails)));
		EmployeeResponseBodySuperclass response = 
				EmployeeRestControllerResponseModifier.addNewEmployeeResponseBuilder(employee);
		
		return new ResponseEntity(response, HttpStatus.resolve(response.getStatusCode()));
	}
	
	//deletes a employee from the database 
	@DeleteMapping(value="/employees/delete/{employee_id}", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<EmployeeResponseBodySuperclass> deleteEmployee(
			@PathVariable("employee_id") Integer eid) throws EmployeeNotFoundException {
		
		serviceImpl.deleteEmployee(eid);
		EmployeeResponseBodySuperclass response = 
				EmployeeRestControllerResponseModifier.deleteEmployeeResponseBuilder();
		
		return new ResponseEntity(response, HttpStatus.resolve(response.getStatusCode()));
	}
	
	//updates (that is, changes) a employee's first name in the database
	@PatchMapping(value="/employees/partup/firstname/{employee_id}/{first_name}", 
			produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<EmployeeResponseBodySuperclass> updateEmployeeFirstName(
			@PathVariable("employee_id") Integer eid,
			@PathVariable("first_name") String fname) 
					throws EmployeeNotFoundException, EmployeeInvalidRequestParameterException {
		
		Employee employee = serviceImpl.updateEmployeeFirstName(eid, fname);
		EmployeeResponseBodySuperclass response = 
				EmployeeRestControllerResponseModifier.updateEmployeeFirstNameResponseBuilder(employee);
		
		return new ResponseEntity(response, HttpStatus.resolve(response.getStatusCode()));
	}
	
	//updates (that is, changes) a employee's last name in the database
	@PatchMapping(value="/employees/partup/lastname/{employee_id}/{last_name}", 
			produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<EmployeeResponseBodySuperclass> updateEmployeeLastName(
			@PathVariable("employee_id") Integer eid,
			@PathVariable("last_name") String lname) 
					throws EmployeeInvalidRequestParameterException, EmployeeNotFoundException{
		
		Employee employee = serviceImpl.updateEmployeeLastName(eid, lname);
		EmployeeResponseBodySuperclass response = 
				EmployeeRestControllerResponseModifier.updateEmployeeLastNameResponseBuilder(employee);
		
		return new ResponseEntity(response, HttpStatus.resolve(response.getStatusCode()));
	}
	
	//updates (that is, changes) a employee's password in the database
	//but only if the current password is given correctly 
	@PatchMapping(value="/employees/partup/pwrd/{employee_id}/{curr_pass}/{new_pass}", 
			produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<EmployeeResponseBodySuperclass> updateEmployeePassword(
			@PathVariable("employee_id") Integer eid,
			@PathVariable("curr_pass") String pword1,
			@PathVariable("new_pass") String pword2) 
					throws EmployeeInvalidRequestParameterException, 
					EmployeeNotFoundException, 
					EmployeeInvalidPasswordException{
		
		Employee employee = serviceImpl.updateEmployeePassWord(eid, pword1, pword2);
		EmployeeResponseBodySuperclass response = 
				EmployeeRestControllerResponseModifier.updateEmployeePasswordResponseBuilder(employee);
		
		return new ResponseEntity(response, HttpStatus.resolve(response.getStatusCode()));
	}
	
	//updates (that is, changes) a employee's age in the database
	@PatchMapping(value="/employees/partup/age/{employee_id}/{age}", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<EmployeeResponseBodySuperclass> updateEmployeeAge(
			@PathVariable("employee_id") Integer eid,
			@PathVariable("age") Integer age) 
					throws EmployeeNotFoundException, EmployeeInvalidRequestParameterException{
		
		Employee employee = serviceImpl.updateEmployeeAge(eid, age);
		EmployeeResponseBodySuperclass response = 
				EmployeeRestControllerResponseModifier.updateEmployeeAgeResponseBuilder(employee);
		
		return new ResponseEntity(response, HttpStatus.resolve(response.getStatusCode()));
	}
	
	//updates (that is, changes) a employee's department name in the database
	@PatchMapping(value="/employees/partup/department/{employee_id}/{department}", 
			produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<EmployeeResponseBodySuperclass> updateEmployeeDepartment(
			@PathVariable("employee_id") Integer eid,
			@PathVariable("department") String department) 
					throws EmployeeInvalidRequestParameterException, EmployeeNotFoundException{
		
		Employee employee = serviceImpl.updateEmployeeDepartment(eid, department);
		EmployeeResponseBodySuperclass response = 
				EmployeeRestControllerResponseModifier.updateEmployeeDepartmentResponseBuilder(employee);
		
		return new ResponseEntity(response, HttpStatus.resolve(response.getStatusCode()));
	}
	
}
