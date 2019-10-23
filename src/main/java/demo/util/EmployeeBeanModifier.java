package demo.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import demo.beans.Employee;
import demo.beans.EmployeeDetails;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.UtilityClass;

@UtilityClass	//makes all members and methods static 
/*class with helper functions to aid in initializing model object data*/
public class EmployeeBeanModifier {
	
	//converts employee details object into a employee object
	public Employee convertFromDetails(Optional<EmployeeDetails> employeedetails) { 
		if(employeedetails.isPresent()) {
			//there was a validation step here, but it messed up the error reporting, so I removed it
			//the validation is handled in multiple places elsewhere in the code regardless
			return Employee.builder().employee_id(employeedetails.get().getEmployee_id()).
					first_name(employeedetails.get().getFirst_name()).
					last_name(employeedetails.get().getLast_name()).
					pass_word(employeedetails.get().getPass_word()).
					age(employeedetails.get().getAge()).
					department(employeedetails.get().getDepartment()).build();	
		}
		else {
			return null;
		}
	}
	
	//converts employee object into employee details object
	public EmployeeDetails convertToDetails(Optional<Employee> employee) {
		if(employee.isPresent()) {
			return EmployeeDetails.builder().employee_id(employee.get().getEmployee_id()).
					first_name(employee.get().getFirst_name()).
					last_name(employee.get().getLast_name()).
					pass_word(employee.get().getPass_word()).
					age(employee.get().getAge()).
					department(employee.get().getDepartment()).build();
		}
		else {
			return null;
		}
	}
	
	//removes all non-alphanumeric characters from a string
	//used to sanitize inputs when constucting a employee object from field data
	//represented as strings
	//essentially this is a helper function for the employee string parser methods below
	public String employeeFieldSanitizer(String input) {
		if(input.isEmpty()) {
			return input;
		}
		return input.replaceAll("[^a-zA-Z0-9]+", "");	//removes everything that isn't a letter or number
	}
	
	//helper nested class for the employeeStringParser method
	@Getter @Setter
	@AllArgsConstructor
	@EqualsAndHashCode
	class Keyval implements Comparable<Keyval>{
		Integer indx;
		String val;
		
		@Override
		public int compareTo(Keyval o) {
			return this.getIndx().compareTo(o.getIndx());
		}
	}
	
	//helper method for the employeeStringParser method
	//sets the indices of the keyval elements to the order they will be read in
	protected List<Keyval> indexer(List<Keyval> ordersort){
		for(Keyval k : ordersort) {
			String labl = k.getVal();
			switch(labl) {	//constant string expressions needed here
				case "employee_id":
					k.setIndx(0);
					break;
				case "first_name":
					k.setIndx(1);
					break;
				case "last_name":
					k.setIndx(2);
					break;
				case "pass_word":
					k.setIndx(3);
					break;
				case "age":
					k.setIndx(4);
					break;
				case "department":
					k.setIndx(5);
					break;
				default:
					break;
			}
		}
		return ordersort;
	}	
	
	//parses a string containing employee data into a EmployeeDetails object
	public EmployeeDetails employeeStringParser(String input) {
		//values: employee_id, first_name, last_name, pass_word, age, department
		//order of these values in the database is not guaranteed
		//we will need to determine the order, then change it to the order
		//it will be read in
		
		//this gathers the element labels and the indices they appear at
		List<Keyval> ordersort = new ArrayList<>(); 
		ordersort.add(new Keyval(input.indexOf(EmployeeMessageManager.commonLiterals.EMPLOYEEID.getLiteral()), 
				EmployeeMessageManager.commonLiterals.EMPLOYEEID.getLiteral()));
		ordersort.add(new Keyval(input.indexOf(EmployeeMessageManager.commonLiterals.FIRSTNAME.getLiteral()), 
				EmployeeMessageManager.commonLiterals.FIRSTNAME.getLiteral()));
		ordersort.add(new Keyval(input.indexOf(EmployeeMessageManager.commonLiterals.LASTNAME.getLiteral()), 
				EmployeeMessageManager.commonLiterals.LASTNAME.getLiteral()));
		ordersort.add(new Keyval(input.indexOf(EmployeeMessageManager.commonLiterals.PASSWORD.getLiteral()), 
				EmployeeMessageManager.commonLiterals.PASSWORD.getLiteral()));
		ordersort.add(new Keyval(input.indexOf(EmployeeMessageManager.commonLiterals.AGE.getLiteral()), 
				EmployeeMessageManager.commonLiterals.AGE.getLiteral()));
		ordersort.add(new Keyval(input.indexOf(EmployeeMessageManager.commonLiterals.DEPARTMENT.getLiteral()), 
				EmployeeMessageManager.commonLiterals.DEPARTMENT.getLiteral()));
		Collections.sort(ordersort); //sorts the element labels into the order they appear in output
		
		//gets the value of each employee data field (in the order they appear in output)
		String[] vals = input.split(",");
		List<String> valholder = new ArrayList<>();
		for(String v : vals) {
			int splitpoint = v.indexOf('=');
			valholder.add(v.substring(splitpoint+1));
		}
		
		ordersort = indexer(ordersort); //sets the indices to the order 
										//that the values will be read in
		
		//stores the values in the order they appear in
		int loopcounter = 0;
		for(Keyval k : ordersort) {
			k.setVal(employeeFieldSanitizer(valholder.get(loopcounter)));
			loopcounter++;
		}
		
		Collections.sort(ordersort); //finally sorts the list into the read order below
		
		return EmployeeDetails.builder()
				.employee_id(Integer.parseInt(ordersort.get(0).getVal()))
				.first_name(ordersort.get(1).getVal())
				.last_name(ordersort.get(2).getVal())
				.pass_word(ordersort.get(3).getVal())
				.age(Integer.parseInt(ordersort.get(4).getVal()))
				.department(ordersort.get(5).getVal()).build();
	}

	//same as indexer, but doesn't expect a employee_id
	//making this a separate method was less of a pain than refactoring the
	//indexer method to be able to handle a case without a employee_id expectation
	protected List<Keyval> indexerNoId(List<Keyval> ordersort){
		for(Keyval k : ordersort) {
			String labl = k.getVal();
			switch(labl) {	//constant string expressions needed here
				case "first_name":
					k.setIndx(0);
					break;
				case "last_name":
					k.setIndx(1);
					break;
				case "pass_word":
					k.setIndx(2);
					break;
				case "age":
					k.setIndx(3);
					break;
				case "department":
					k.setIndx(4);
					break;
				default:
					break;
			}
		}
		return ordersort;
	}
	
	//same as employeeStringParser, but doesn't expect a employee_id
	//making this a separate method was less of a pain than refactoring the
	//employeeStringParser method to be able to handle a case without a employee_id expectation
	public EmployeeDetails employeeStringParserNoId(String input) {
		//values: first_name, last_name, pass_word, age, department
		//order of these values in the database is not guaranteed
		//we will need to determine the order, then change it to the order
		//it will be read in
		
		//this gathers the element labels and the indices they appear at
		List<Keyval> ordersort = new ArrayList<>(); 
		ordersort.add(new Keyval(input.indexOf(EmployeeMessageManager.commonLiterals.FIRSTNAME.getLiteral()), 
				EmployeeMessageManager.commonLiterals.FIRSTNAME.getLiteral()));
		ordersort.add(new Keyval(input.indexOf(EmployeeMessageManager.commonLiterals.LASTNAME.getLiteral()), 
				EmployeeMessageManager.commonLiterals.LASTNAME.getLiteral()));
		ordersort.add(new Keyval(input.indexOf(EmployeeMessageManager.commonLiterals.PASSWORD.getLiteral()), 
				EmployeeMessageManager.commonLiterals.PASSWORD.getLiteral()));
		ordersort.add(new Keyval(input.indexOf(EmployeeMessageManager.commonLiterals.AGE.getLiteral()), 
				EmployeeMessageManager.commonLiterals.AGE.getLiteral()));
		ordersort.add(new Keyval(input.indexOf(EmployeeMessageManager.commonLiterals.DEPARTMENT.getLiteral()), 
				EmployeeMessageManager.commonLiterals.DEPARTMENT.getLiteral()));
		Collections.sort(ordersort); //sorts the element labels into the order they appear in output
		
		//gets the value of each employee data field (in the order they appear in output)
		String[] vals = input.split(",");
		List<String> valholder = new ArrayList<>();
		for(String v : vals) {
			int splitpoint = v.indexOf('=');
			valholder.add(v.substring(splitpoint+1));
		}
		
		ordersort = indexerNoId(ordersort); //sets the indices to the order 
										//that the values will be read in
		
		//stores the values in the order they appear in
		int loopcounter = 0;
		for(Keyval k : ordersort) {
			k.setVal(employeeFieldSanitizer(valholder.get(loopcounter)));
			loopcounter++;
		}
		
		Collections.sort(ordersort); //finally sorts the list into the read order below
		
		return EmployeeDetails.builder()
				.first_name(ordersort.get(0).getVal())
				.last_name(ordersort.get(1).getVal())
				.pass_word(ordersort.get(2).getVal())
				.age(Integer.parseInt(ordersort.get(3).getVal()))
				.department(ordersort.get(4).getVal()).build();
	}
	
}
