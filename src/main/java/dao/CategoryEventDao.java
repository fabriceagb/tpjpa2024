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

    /**
     * CREATE : Ajouter une nouvelle catégorie en base de données
     */
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


    /**
     * UPDATE : Mettre à jour une catégorie existante
     */
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

    /**
     * DELETE : Supprimer une catégorie via son ID
     */
    public void delete(Long id) {
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            // On doit d'abord récupérer l'objet avant de le supprimer
            CategoryEvent category = entityManager.find(CategoryEvent.class, id);
            if (category != null) {
                // remove() supprime l'entité de la base
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

    /**
     * READ (Un seul) : Trouver une catégorie spécifique par son ID
     */
    public CategoryEvent findById(Long id) {
        // find() récupère l'entité directement grâce à sa clé primaire
        return entityManager.find(CategoryEvent.class, id);
    }

    /**
     * READ (Tous) : Récupérer la liste de toutes les catégories
     */
    public List<CategoryEvent> findAll() {
        // On utilise JPQL pour interroger l'entité (et non la table SQL)
        // "SELECT c FROM CategoryEvent c" = "Récupère tous les objets CategoryEvent"
        return entityManager.createQuery("SELECT c FROM CategoryEvent c", CategoryEvent.class)
                .getResultList();
    }

    /**
     * CRITERIA QUERY : Recherche exacte par nom
     * Cette approche utilise l'API Criteria de JPA pour trouver une catégorie correspondant exactement au nom fourni.
     */
    public CategoryEvent findByNameCriteria(String name) {
        // 1. Initialiser le CriteriaBuilder
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        // 2. Créer la requête Criteria ciblant l'entité CategoryEvent
        CriteriaQuery<CategoryEvent> cq = cb.createQuery(CategoryEvent.class);

        // 3. Définir la racine de la requête (clause FROM)
        Root<CategoryEvent> root = cq.from(CategoryEvent.class);

        // 4. Construire la condition (clause WHERE name = 'name')
        Predicate condition = cb.equal(root.get("libelle"), name);

        // 5. Assembler la requête (SELECT root WHERE condition)
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
