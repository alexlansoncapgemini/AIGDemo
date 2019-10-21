package demo.responses;

import java.util.List;

import demo.beans.EmployeeDetails;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

//response body formatted for a list of employees
//this will be used for the get all employees method
@Getter @Setter
@FieldDefaults(level=AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor 
public class EmployeeListResponseBody extends EmployeeResponseBodySuperclass {
	List<EmployeeDetails> employeeList;
}
