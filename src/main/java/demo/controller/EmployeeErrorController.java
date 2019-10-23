package demo.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import demo.exceptions.EmployeeNotFoundException;
import demo.responses.EmployeeResponseBodySuperclass;

@RestController
public class EmployeeErrorController {

	//catches attempted access to non-existent page mappings, and throws a not-found exception
	@RequestMapping(value = "/**")
	public ResponseEntity<EmployeeResponseBodySuperclass> handleErrors(HttpServletRequest request) 
			throws EmployeeNotFoundException {
		throw new EmployeeNotFoundException("Page does not exist");
	}

}