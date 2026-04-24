package jpa;


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
