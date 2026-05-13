package dao;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

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

public class EventDao extends AbstractJpaDao<Long, Event> {

    public EventDao() {
        super();
    }

    /**
     *  Ajouter un nouvel événement en base de données
     */
    public Event create(EventDto eventDto, Manager manager, CategoryEvent category) {
        if (eventDto.getDate() == null || eventDto.getDate().trim().isEmpty()) {
            throw new IllegalArgumentException("La date de l'événement est obligatoire.");
        }
        Date parsedDate;
        try {
            // Le format "dd/MM/yyyy HH:mm" est utilisé pour parser la date et l'heure.
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            formatter.setLenient(false); // Pour une validation stricte du format
            parsedDate = formatter.parse(eventDto.getDate());
        } catch (ParseException e) {
            throw new IllegalArgumentException("Format de date invalide pour '" + eventDto.getDate() + "'. Le format attendu est jj/MM/aaaa HH:mm.");
        }

        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            Event event = new Event();            
            event.setLabel(eventDto.getLabel());
            event.setDescription(eventDto.getDescription());
            event.setLocation(eventDto.getLocation());
            event.setPrice(eventDto.getPrice());
            event.setPopularity(eventDto.getPopularity());
            event.setNumberOfTickets(eventDto.getNumberOfTickets());
            event.setCancelled(false);
            event.setManager(manager);
            event.setCategoryEvent(category);
            event.setDate(parsedDate);
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
     * Annuler un événement (pose le flag cancelled, ne supprime pas)
     */
    public Event cancelEvent(Long eventId) {
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            Event event = entityManager.find(Event.class, eventId);
            if (event == null) {
                return null;
            }
            event.setCancelled(true);
            Event updated = entityManager.merge(event);
            transaction.commit();
            System.out.println("L'événement " + eventId + " a été marqué comme annulé.");
            return updated;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();
            return null;
        }
    }
}