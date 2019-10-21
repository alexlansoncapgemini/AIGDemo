package demo.dao;

import java.util.List;

import demo.beans.Employee;

public interface EmployeeDatabaseInterface {
	public List<Employee> getAllEmployees();
	public Employee addNewEmployee(Employee employee);
	public List<Employee> getEmployeeById(int eid);
	public void deleteEmployee(int eid);
	public Employee updateEmployeeFirstName(int eid, String fname);
	public Employee updateEmployeeLastName(int eid, String lname);
	public Employee updateEmployeePassWord(int eid, String pword1, String pword2);
	public Employee updateEmployeeAge(int eid, int newage);	
	public Employee updateEmployeeDepartment(int eid, String department);
	List<Employee> getEmployeeByFirstName(String firstName);	
}
