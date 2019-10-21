package demo.beans;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;


//non-persisted entity used for http request data passing
@Builder	
@Data  //Data includes @Getter and @Setter, as well as toString, hashCode, and others
@FieldDefaults(level=AccessLevel.PRIVATE)
@NoArgsConstructor 
@AllArgsConstructor 
public class EmployeeDetails {	
	int employee_id;			
	
	String first_name;
	String last_name;
	String pass_word;
	
	int age;
	
	String department;
}
