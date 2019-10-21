package demo.service;

import java.util.List;

import demo.beans.Employee;
import demo.exceptions.EmployeeDuplicateEntryExistsException;
import demo.exceptions.EmployeeInvalidPasswordException;
import demo.exceptions.EmployeeInvalidRequestParameterException;
import demo.exceptions.EmployeeNotFoundException;
import demo.exceptions.EmployeeRequestOnNullObjectException;

public interface EmployeeServiceInterface {
	public List<Employee> getAllEmployees() 
			throws EmployeeNotFoundException;
	public Employee newEmployee(Employee employee) 
			throws EmployeeRequestOnNullObjectException, 
			EmployeeInvalidRequestParameterException, 
			EmployeeDuplicateEntryExistsException;
	public Employee getEmployeeById(int eid) 
			throws EmployeeNotFoundException;
	public void deleteEmployee(int eid) 
			throws EmployeeNotFoundException;
	public Employee updateEmployeeFirstName(int eid, String fname) 
			throws EmployeeNotFoundException, 
			EmployeeInvalidRequestParameterException;
	public Employee updateEmployeeLastName(int eid, String lname)
			throws EmployeeNotFoundException, 
			EmployeeInvalidRequestParameterException;
	public Employee updateEmployeePassWord(int eid, String pword1, String pword2) 
			throws EmployeeInvalidPasswordException, 
			EmployeeInvalidRequestParameterException, 
			EmployeeNotFoundException;
	public Employee updateEmployeeAge(int eid, int newage) 
			throws EmployeeNotFoundException, EmployeeInvalidRequestParameterException;	
	public Employee updateEmployeeDepartment(int eid, String department) 
			throws EmployeeNotFoundException, 
			EmployeeInvalidRequestParameterException;
	List<Employee> getEmployeeByFirstName(String firstName);
}
