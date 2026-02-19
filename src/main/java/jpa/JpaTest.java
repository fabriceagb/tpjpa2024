package jpa;


import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;

public class JpaTest {


	private static EntityManager manager;

	public JpaTest(EntityManager manager) {
		this.manager = manager;
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
			EntityManager manager = EntityManagerHelper.getEntityManager();

		JpaTest test = new JpaTest(manager);

		EntityTransaction tx = manager.getTransaction();
		tx.begin();
		try {
//			createEmployees();
//			listEmployees();
		} catch (Exception e) {
			e.printStackTrace();
		}
		tx.commit();

			
   	 manager.close();
		EntityManagerHelper.closeEntityManagerFactory();
		System.out.println(".. done");
	}

//	private static void createEmployees() {
//		int numOfEmployees = manager.createQuery("Select a From Employee a", Event.class).getResultList().size();
//		if (numOfEmployees == 0) {
//			Department department = new Department("java");
//			manager.persist(department);
//
//			manager.persist(new Employee("Jakab Gipsz",department));
//			manager.persist(new Employee("Captain Nemo",department));
//
//		}
//	}
//	private static void listEmployees() {
//		List<Employee> resultList = manager.createQuery("Select a From Employee a", Employee.class).getResultList();
//		System.out.println("num of employess:" + resultList.size());
//		for (Employee next : resultList) {
//			System.out.println("next employee: " + next);
//		}
//	}
}
