package dao;

import dao.generic.AbstractJpaDao;
import dto.EventDto;
import entity.CategoryEvent;
import entity.Event;
import entity.Manager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.util.Date;
import java.util.List;

public class EventDao extends AbstractJpaDao<Long, Event> {

    public EventDao() {
        super();
    }

    /**
     *  Ajouter un nouvel événement en base de données
     */
    public Event create(EventDto eventDto, Manager manager, CategoryEvent category) {
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            Event event = new Event();
            System.out.println("Création de l'événement : " + eventDto.getLabel() + " pour le manager ID : " + eventDto.getManagerId());
            
            event.setLabel(eventDto.getLabel());
            event.setDescription(eventDto.getDescription());
            event.setLocation(eventDto.getLocation());
            event.setPrice(eventDto.getPrice());
            event.setPopularity(eventDto.getPopularity());
            event.setManager(manager);
            event.setCategoryEvent(category);
            event.setDate(new Date());
            entityManager.persist(event);
            
            transaction.commit();
            System.out.println("Événement créé avec succès.");
            return event;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();
            return null;
        }
    }


    /**
     * Mettre à jour un événement existant
     */
    public Event update(Event event) {
        EntityTransaction transaction = entityManager.getTransaction();
        Event updatedEvent = null;
        try {
            transaction.begin();
            updatedEvent = entityManager.merge(event);
            transaction.commit();
            System.out.println("Événement mis à jour avec succès.");
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
        return updatedEvent;
    }

    /**
     * Supprimer un événement via son ID
     */
    public void delete(Long id) {
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            Event event = entityManager.find(Event.class, id);
            if (event != null) {
                entityManager.remove(event);
                System.out.println("Événement supprimé avec succès.");
            } else {
                System.out.println("Aucun événement trouvé avec cet ID.");
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
     *  Trouver un événement spécifique par son ID
     */
    public Event findById(Long id) {
        return entityManager.find(Event.class, id);
    }

    /**
     * Récupérer la liste de tous les événements
     */
    public List<Event> findAll() {
        return entityManager.createNamedQuery("Event.findAll", Event.class)
                .getResultList();
    }

    /**
     * Trouver des événements selon leur lieu
     */
    public List<Event> findEventsByLocation(String location) {
        return entityManager.createNamedQuery("Event.findByLocation", Event.class)
                .setParameter("location", location)
                .getResultList();
    }

    /**
     * Trouver les événements à venir
     */
    public List<Event> findUpcomingEvents() {
        return entityManager.createNamedQuery("Event.findUpcoming", Event.class)
                .getResultList();
    }

    /**
     * Trouver les événements par catégorie
     */
    public List<Event> findByCategory(Long categoryId) {
        return entityManager.createNamedQuery("Event.findByCategory", Event.class)
                .setParameter("categoryId", categoryId)
                .getResultList();
    }

    /**
     * Trouver les événements d'un manager spécifique
     */
    public List<Event> findByManager(Long managerId) {
        return entityManager.createNamedQuery("Event.findByManager", Event.class)
                .setParameter("managerId", managerId)
                .getResultList();
    }

    /**
     * Rechercher des événements contenant un mot clé dans le label (titre)
     */
    public List<Event> findByLabelCriteria(String labelKeyword) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Event> cq = cb.createQuery(Event.class);
        Root<Event> root = cq.from(Event.class);
        
        Predicate condition = cb.like(cb.lower(root.get("label")), "%" + labelKeyword.toLowerCase() + "%");
        cq.select(root).where(condition);
        
        return entityManager.createQuery(cq).getResultList();
    }

    /**
     * Trouver les événements dans une fourchette de prix
     */
    public List<Event> findEventsByPriceRange(double minPrice, double maxPrice) {
        return entityManager.createQuery("SELECT e FROM Event e WHERE e.price BETWEEN :min AND :max", Event.class)
                .setParameter("min", minPrice)
                .setParameter("max", maxPrice)
                .getResultList();
    }

    /**
     * Annuler un événement
     */
    public void cancelEvent(Long eventId) {
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            
            entityManager.createQuery("DELETE FROM Ticket t WHERE t.event.id = :eventId") 
                         .setParameter("eventId", eventId)
                         .executeUpdate();
            
            System.out.println("L'événement a été annulé, et les tickets associés ont été retirés.");
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }
}