package dao;

import dao.generic.AbstractJpaDao;
import entity.CategoryEvent;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.util.List;

public class CategoryEventDao extends AbstractJpaDao<Long, CategoryEvent> {

    public CategoryEventDao(){
        super();
    }

    public void create(CategoryEvent category) {
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            entityManager.persist(category);
            transaction.commit();
            System.out.println("Catégorie créée avec succès.");
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }


    public CategoryEvent update(CategoryEvent category) {
        EntityTransaction transaction = entityManager.getTransaction();
        CategoryEvent updatedCategory = null;
        try {
            transaction.begin();
            updatedCategory = entityManager.merge(category);
            transaction.commit();
            System.out.println("Catégorie mise à jour avec succès.");
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
        return updatedCategory;
    }

    public void delete(Long id) {
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            CategoryEvent category = entityManager.find(CategoryEvent.class, id);
            if (category != null) {
                entityManager.remove(category);
                System.out.println("Catégorie supprimée avec succès.");
            } else {
                System.out.println("Aucune catégorie trouvée avec cet ID.");
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }

    public CategoryEvent findById(Long id) {
        return entityManager.find(CategoryEvent.class, id);
    }



    public List<CategoryEvent> findAll() {

        return entityManager.createQuery("SELECT c FROM CategoryEvent c", CategoryEvent.class)
                .getResultList();
    }


    public CategoryEvent findByNameCriteria(String name) {
        // 1. Initialiser le CriteriaBuilder
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<CategoryEvent> cq = cb.createQuery(CategoryEvent.class);
        Root<CategoryEvent> root = cq.from(CategoryEvent.class);
        Predicate condition = cb.equal(root.get("libelle"), name);
        cq.select(root).where(condition);

        // 6. Exécuter la requête
        try {
            return entityManager.createQuery(cq).getSingleResult();
        } catch (NoResultException e) {
            System.out.println("Aucune catégorie trouvée avec le nom exact : " + name);
            return null;
        }
    }
}
