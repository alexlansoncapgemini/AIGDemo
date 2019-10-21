package demo.controller;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import demo.exceptions.EmployeeNotFoundException;
import demo.exceptions.EmployeeServerErrorException;

@RestController
public class EmployeeErrorController implements ErrorController{

	@GetMapping(value = "/error")
	public void handleErrors(HttpServletRequest request) 
			throws EmployeeNotFoundException, EmployeeServerErrorException {
		Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
	     
	    if (status != null) {
	        Integer statusCode = Integer.valueOf(status.toString());
	     
	        if(statusCode == HttpStatus.NOT_FOUND.value()) {
	        	throw new EmployeeNotFoundException("Page does not exist");
	        }
	        else if(statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
	            throw new EmployeeServerErrorException("An error occurred within the server.");
	        }
	    }
	}
	
	@Override
	public String getErrorPath() {
		return "/error";
	}

}
