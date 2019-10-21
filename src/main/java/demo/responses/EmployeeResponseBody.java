package demo.responses;

import demo.beans.EmployeeDetails;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

//response body for formatting employee details
//it made more sense to create this than to alter the employee toString method
@Getter @Setter
@FieldDefaults(level=AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor 
public class EmployeeResponseBody extends EmployeeResponseBodySuperclass {
	EmployeeDetails employeeDetails;
}
