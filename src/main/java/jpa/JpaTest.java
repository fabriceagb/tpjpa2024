package jpa;


import dao.CategoryEventDao;
import entity.CategoryEvent;
import io.undertow.Undertow;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.jboss.resteasy.plugins.server.undertow.UndertowJaxrsServer;
import java.util.logging.Logger;

public class JpaTest {


	private static EntityManager manager;
	private static final Logger logger = Logger.getLogger(JpaTest.class.getName());


	public JpaTest(EntityManager manager) {
		this.manager = manager;
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		/*
		 * Creation de l'entityManager et creation des tables dans la base
		 */
		EntityManager manager = EntityManagerHelper.getEntityManager();

		EntityTransaction tx = manager.getTransaction();
		CategoryEventDao categoryDao = new CategoryEventDao();

		// 3. Liste des catégories de base
		String[] categoriesDeBase = {
				"Concert & Musique",
				"Conférence & Business",
				"Formation & Atelier",
				"Spectacle & Art",
				"Sport & E-sport",
				"Webinaire & En ligne",
				"Networking & Rencontres"
		};

		System.out.println("--- Début de l'insertion des catégories ---");

		// 4. Boucle pour insérer chaque catégorie
		for (String name : categoriesDeBase) {
			// On vérifie d'abord si elle n'existe pas déjà (pour éviter les doublons si on relance le test)
			if (categoryDao.findByNameCriteria(name) == null) {
				CategoryEvent newCategory = new CategoryEvent();
				newCategory.setLibelle(name);

				categoryDao.create(newCategory);
			} else {
				System.out.println("La catégorie '" + name + "' existe déjà.");
			}
		}

		System.out.println("--- Fin de l'insertion ---");
   	 	manager.close();
		EntityManagerHelper.closeEntityManagerFactory();

		UndertowJaxrsServer ut = new UndertowJaxrsServer();

		EntityManagerHelper.TestApplication ta = new EntityManagerHelper.TestApplication();

		ut.deploy(ta);

		ut.start(
				Undertow.builder()
						.addHttpListener(8080, "localhost")
		);
		logger.info("JAX-RS based micro-service running!");
	}

}
