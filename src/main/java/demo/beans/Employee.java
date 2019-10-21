package demo.beans;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Entity
@Builder	
@Data 	//Data includes @Getter and @Setter, as well as toString, hashCode, and others
@FieldDefaults(level=AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor 
public class Employee {
	@Id  
	@GeneratedValue(strategy=GenerationType.AUTO)  
	int employee_id;
	
	String first_name;
	String last_name; 
	String pass_word; 
	
	int age; 
	
	String department; 
}
