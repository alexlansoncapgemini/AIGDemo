package demo.dao;

import demo.beans.*;
import demo.util.*;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Repository("EmployeeDbDao")
@FieldDefaults(level=AccessLevel.PRIVATE)
@SuppressWarnings("unchecked")
public class EmployeeDatabaseImplementation implements EmployeeDatabaseInterface {

	static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("aigdemo");
	@PersistenceContext
	EntityManager entityManager = emf.createEntityManager();
		//creates the object through which the MySQL databse is accessed
	final EntityTransaction entityTransaction = entityManager.getTransaction();
	
	public EmployeeDatabaseImplementation() {
		entityTransaction.begin();
	}
	
	public EntityManager getManager() {
		return entityManager;
	}
	
	//checks if a employee with the same name and age is already in the database
	public Boolean doesDuplicateExist(Employee employee) {
		String qry = EmployeeMessageManager.getVal("findDuplicateEmployee");
		Query query = entityManager.createQuery(qry)
				.setParameter("fname", employee.getFirst_name())
				.setParameter("lname", employee.getLast_name())
				.setParameter("age", employee.getAge());
		List<Employee> emps = query.getResultList();
		return(!emps.isEmpty());
	}
	
	/* 													  *
	 * all major validation happens in the service layer, *
	 * which is the only place that calls these methods   *
	 * 													  */
	
	//returns a list of all employees in the database
	@Override
	public List<Employee> getAllEmployees(){
		return entityManager.createQuery(
				EmployeeMessageManager.getVal("findAllEmployees")).getResultList();
	}
	
	//sends a employee object to the database, unless it's a duplicate
	@Override
	@Transactional
	public Employee addNewEmployee(Employee employee) {
		if(!entityTransaction.isActive()) {
			entityTransaction.begin();
		}
		entityManager.persist(employee);
		entityTransaction.commit();
		
		return employee;
	}
	
	//looks for a employee in the database with a given id and retrieves the employee if found
	@Override
	public List<Employee> getEmployeeById(int eid) {		
		Query query = entityManager
				.createQuery(EmployeeMessageManager.getVal("findEmployee"))
				.setParameter("eid", eid);
		
		return query.getResultList();
		//the result list should only contain, at most, one employee object
		//in the service layer, that object is retrieved from this returned list
	}
	
	//searches for a employee by first name
	//only really used in testing to get an employee object with an id
	@Override
	public List<Employee> getEmployeeByFirstName(String firstName) {
		Query qry = entityManager.createQuery(
				EmployeeMessageManager.getVal("findEmployeeByFirstName")).setParameter("fname", firstName);
		return qry.getResultList();
	}
	
	//deletes a employee from the database if such a employee exists within it
	@Override
	@Transactional
	public void deleteEmployee(int eid) {
		if(!entityTransaction.isActive()) {
			entityTransaction.begin();
		}
		Employee employee = entityManager.find(Employee.class, eid);
		
		entityManager.remove(employee);
		entityTransaction.commit();
	}
	
	@Override
	@Transactional
	public Employee updateEmployeeFirstName(int eid, String fname) {
		if(!entityTransaction.isActive()) {
			entityTransaction.begin();
		}
		
		Employee employee = entityManager.find(Employee.class, eid);
		
		employee.setFirst_name(fname);
		entityManager.merge(employee);	//overwrites the employee's first name
		entityTransaction.commit();
		return employee;
	}
	
	@Override
	@Transactional
	public Employee updateEmployeeLastName(int eid, String lname) {
		if(!entityTransaction.isActive()) {
			entityTransaction.begin();
		}
		
		Employee employee = entityManager.find(Employee.class, eid);

		employee.setLast_name(lname);
		entityManager.merge(employee);	//overwrites the employee's last name
		entityTransaction.commit();
		return employee;
	}
	
	//changes the employee's password only if the current one is correctly given
	//pword1 is the current password. pword2 is the new password
	@Override 
	@Transactional
	public Employee updateEmployeePassWord(int eid, String pword1, String pword2) {
		if(!entityTransaction.isActive()) {
			entityTransaction.begin();
		}
		
		Employee employee = entityManager.find(Employee.class, eid);

		employee.setPass_word(pword2);
		entityManager.merge(employee);
		entityTransaction.commit();
		return employee;
	}
	
	@Override
	@Transactional
	public Employee updateEmployeeAge(int eid, int newage) {
		if(!entityTransaction.isActive()) {
			entityTransaction.begin();
		}
		
		Employee employee = entityManager.find(Employee.class, eid);

		employee.setAge(newage);
		entityManager.merge(employee);
		entityTransaction.commit();
		return employee;
	}
	
	@Override
	@Transactional
	public Employee updateEmployeeDepartment(int eid, String department) {
		if(!entityTransaction.isActive()) {
			entityTransaction.begin();
		}
		
		Employee employee = entityManager.find(Employee.class, eid);

		employee.setDepartment(department);
		entityManager.merge(employee);
		entityTransaction.commit();
		return employee;
	}
}
