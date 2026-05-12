package ressources;

import dao.CategoryEventDao;
import dao.EventDao;
import dao.ManagerDao;
import dto.EventDto;
import dto.UpdateEventDto;
import entity.CategoryEvent;
import entity.Event;
import entity.Manager;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/api/event")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EventRessource {

    /**
     * Récupérer tous les événements
     * GET http://localhost:8080/votre-app/api/event/all
     */
    @GET
    @Path("/all")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllEvents() {
        EventDao dao = new EventDao();
        try {
            List<Event> events = dao.findAll();
            return Response.ok(events).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Erreur lors de la récupération des événements.\"}")
                    .build();
        }
    }

    /**
     * Récupérer un événement par son ID
     * GET http://localhost:8080/votre-app/api/event/1
     */
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getEventById(@PathParam("id") Long id) {
        EventDao dao = new EventDao();
        try {
            Event event = dao.findById(id);
            if (event != null) {
                return Response.ok(event).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Événement introuvable.\"}")
                        .build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Ajouter un nouvel événement
     * POST http://localhost:8080/votre-app/api/event/add
     */
    @POST
    @Path("/add")
    @RolesAllowed("USER_MANAGER")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createEvent(EventDto request) {
        EventDao dao = new EventDao();
        try {
            ManagerDao managerDao = new ManagerDao();
            CategoryEventDao categoryEventDao = new CategoryEventDao();

            Manager manager = managerDao.findById(request.getManagerId());
            if(manager == null) {
                 return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"Le champ managerId est obligatoire pour créer un événement.\"}")
                        .build();
            }
            
            CategoryEvent category = categoryEventDao.findById(request.getCategoryId());
            if(category == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"Le champ categoryId est obligatoire pour créer un événement.\"}")
                        .build();
            }
            
            
            Event event =  dao.create(request, manager, category);

            return Response.status(Response.Status.CREATED).entity(event).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"" + e.getMessage() + "\"}").build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Erreur lors de la création de l'événement.\"}")
                    .build();
        }
    }

    /**
     * Supprimer un événement
     * DELETE http://localhost:8080/votre-app/api/event/delete/1
     */
    @DELETE
    @Path("/delete/{id}")
    @RolesAllowed({"USER_MANAGER", "USER_ADMINISTRATOR"})
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteEvent(@PathParam("id") Long id) {
        EventDao dao = new EventDao();
        try {
            Event existingEvent = dao.findById(id);
            if (existingEvent == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Événement introuvable.\"}")
                        .build();
            }

            dao.delete(id);
            return Response.ok("{\"message\": \"Événement supprimé avec succès.\"}").build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Erreur lors de la suppression de l'événement.\"}")
                    .build();
        }
    }

    /**
     * Mettre à jour un événement existant
     * PUT http://localhost:8080/votre-app/api/event/update/1
     */
    @PUT
    @Path("/update/{id}")
    @RolesAllowed("USER_MANAGER")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateEvent(@PathParam("id") Long id, UpdateEventDto request) {
        EventDao dao = new EventDao();
        try {
            Event existingEvent = dao.findById(id);
            if (existingEvent == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Événement introuvable pour la mise à jour.\"}")
                        .build();
            }

            existingEvent.setLabel(request.getLabel());
            existingEvent.setDescription(request.getDescription());
            existingEvent.setLocation(request.getLocation());
            existingEvent.setPrice(request.getPrice());
            existingEvent.setPopularity(request.getPopularity());

            Event updatedEvent = dao.update(existingEvent);
            return Response.ok(updatedEvent).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Erreur lors de la mise à jour de l'événement.\"}")
                    .build();
        }
    }

    /**
     * Récupérer les événements à venir
     * GET http://localhost:8080/votre-app/api/event/upcoming
     */
    @GET
    @Path("/upcoming")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUpcomingEvents() {
        EventDao dao = new EventDao();
        try {
            List<Event> events = dao.findUpcomingEvents();
            return Response.ok(events).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * RECHERCHE : Chercher par mot-clé dans le titre/label
     * GET http://localhost:8080/votre-app/api/event/search?label=Festival
     */
    @GET
    @Path("/search")
    @Produces(MediaType.APPLICATION_JSON)
    public Response searchEvents(@QueryParam("label") String labelKeyword) {
        if (labelKeyword == null || labelKeyword.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Le paramètre 'label' est requis.\"}").build();
        }
        EventDao dao = new EventDao();
        try {
            List<Event> events = dao.findByLabelCriteria(labelKeyword);
            return Response.ok(events).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Événements par catégorie
     * GET http://localhost:8080/votre-app/api/event/category/1
     */
    @GET
    @Path("/category/{categoryId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getEventsByCategory(@PathParam("categoryId") Long categoryId) {
        EventDao dao = new EventDao();
        try {
            List<Event> events = dao.findByCategory(categoryId);
            return Response.ok(events).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Événements par manager
     * GET http://localhost:8080/votre-app/api/event/manager/1
     */
    @GET
    @Path("/manager/{managerId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getEventsByManager(@PathParam("managerId") Long managerId) {
        EventDao dao = new EventDao();
        try {
            List<Event> events = dao.findByManager(managerId);
            return Response.ok(events).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Événements par lieu (Requête nommée)
     * GET http://localhost:8080/votre-app/api/event/location/Paris
     */
    @GET
    @Path("/location/{location}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getEventsByLocation(@PathParam("location") String location) {
        EventDao dao = new EventDao();
        try {
            List<Event> events = dao.findEventsByLocation(location);
            return Response.ok(events).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     *  Événements par fourchette de prix (Requête JPQL)
     * GET http://localhost:8080/votre-app/api/event/price?min=10&max=50
     */
    @GET
    @Path("/price")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getEventsByPriceRange(@QueryParam("min") Double min, @QueryParam("max") Double max) {
        if (min == null) min = 0.0;
        if (max == null) max = Double.MAX_VALUE;
        EventDao dao = new EventDao();
        try {
            List<Event> events = dao.findEventsByPriceRange(min, max);
            return Response.ok(events).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Annuler un événement (pose le flag cancelled=true, ne supprime pas)
     * POST http://localhost:8080/votre-app/api/event/cancel/1
     */
    @POST
    @Path("/cancel/{id}")
    @RolesAllowed({"USER_MANAGER", "USER_ADMINISTRATOR"})
    @Produces(MediaType.APPLICATION_JSON)
    public Response cancelEvent(@PathParam("id") Long id) {
        EventDao dao = new EventDao();
        try {
            Event existing = dao.findById(id);
            if (existing == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Événement introuvable.\"}")
                        .build();
            }
            if (existing.isCancelled()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"Cet événement est déjà annulé.\"}")
                        .build();
            }
            Event cancelled = dao.cancelEvent(id);
            return Response.ok(cancelled).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}
