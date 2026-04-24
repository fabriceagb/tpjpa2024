package jpa;

import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import ressources.SwaggerResource;
import ressources.UserRessource;

import java.util.HashSet;
import java.util.Set;

public class EntityManagerHelper {

    private static final EntityManagerFactory emf; 
    private static final ThreadLocal<EntityManager> threadLocal;

    static {
        emf = Persistence.createEntityManagerFactory("dev");
        threadLocal = new ThreadLocal<EntityManager>();
    }

    public static EntityManager getEntityManager() {
        EntityManager em = threadLocal.get();

        if (em == null) {
            em = emf.createEntityManager();
            threadLocal.set(em);
        }
        return em;
    }

    public static void closeEntityManager() {
        EntityManager em = threadLocal.get();
        if (em != null) {
            em.close();
            threadLocal.set(null);
        }
    }

    public static void closeEntityManagerFactory() {
        emf.close();
    }

    public static void beginTransaction() {
        getEntityManager().getTransaction().begin();
    }

    public static void rollback() {
        getEntityManager().getTransaction().rollback();
    }

    public static void commit() {
        getEntityManager().getTransaction().commit();
    }

    @ApplicationPath("/")
    public static class TestApplication extends Application {


        @Override
        public Set<Class<?>> getClasses() {
            final Set<Class<?>> clazzes = new HashSet<Class<?>>();

            clazzes.add(OpenApiResource.class);
            clazzes.add(UserRessource.class);
            clazzes.add(SwaggerResource.class);
    //        clazzes.add(AcceptHeaderOpenApiResource.class);


            return clazzes;
        }

    }
}