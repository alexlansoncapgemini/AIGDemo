package demo.responses;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

//generic response body
@Getter @Setter
@FieldDefaults(level=AccessLevel.PROTECTED)
@NoArgsConstructor
@AllArgsConstructor 
public class EmployeeResponseBodySuperclass {
	int statusCode;
	String statusName;
	String statusDescription;
}
