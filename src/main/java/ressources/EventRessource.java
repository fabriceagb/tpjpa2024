package ressources;

import dao.*;
import dto.BuyTicketDto;
import dto.EventDto;
import dto.TicketResponseDto;
import dto.UpdateEventDto;
import entity.CategoryEvent;
import entity.Customer;
import entity.Event;
import entity.Manager;
import entity.Ticket;
import entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import utils.JwtUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Event", description = "Gestion des evernement")
@Path("/api/event")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EventRessource {

    /**
     * Récupérer tous les événements
     */
    @GET
    @Path("/all")
    @Operation(
            summary = "Récupérer tous les événements"
    )
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
     */
    @GET
    @Path("/{id}")
    @Operation(
            summary = "Récupérer un événement par son ID"
    )
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
     */
    @POST
    @Path("/add")
    @Operation(
            summary = "Ajouter un nouvel événement"
    )
    @RolesAllowed("USER_MANAGER")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createEvent(EventDto request, @HeaderParam("Authorization") String authHeader) {
        EventDao dao = new EventDao();
        try {
            String email = JwtUtil.getEmailFromToken(authHeader.substring(7));
            ManagerDao managerDao = new ManagerDao();
            Manager manager = managerDao.findByEmail(email);
            if (manager == null) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity("{\"error\": \"Aucun manager trouvé pour ce compte.\"}")
                        .build();
            }

            CategoryEventDao categoryEventDao = new CategoryEventDao();
            CategoryEvent category = categoryEventDao.findById(request.getCategoryId());
            if (category == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"Catégorie introuvable.\"}")
                        .build();
            }

            Event event = dao.create(request, manager, category);
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
     */
    @DELETE
    @Path("/delete/{id}")
    @Operation(
            summary = "Supprimer un événement par son id"
    )
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
     */
    @PUT
    @Path("/update/{id}")
    @Operation(
            summary = "Mettre à jour un événement existant par son id"
    )
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
     */
    @GET
    @Path("/upcoming")
    @Operation(
            summary = "Récupérer les événements à venir"
    )
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
     * Chercher par mot-clé dans le titre/label
     */
    @GET
    @Path("/search")
    @Operation(
            summary = "Chercher par mot-clé dans le titre/label"
    )
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
     */
    @GET
    @Path("/category/{categoryId}")
    @Operation(
            summary = "Événements par catégorie"
    )
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
     */
    @GET
    @Operation(
            summary = "Événements par manager"
    )
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
     */
    @GET
    @Operation(
            summary = "Événements par lieu"
    )
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
     */
    @GET
    @Path("/price")
    @Operation(
            summary = "Événements par fourchette de prix"
    )
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
     * Acheter un billet pour un événement
     */
    @Operation(
            summary = "Acheter un billet pour un événement"
    )
    @POST
    @Path("/{id}/buy")
    @RolesAllowed("USER_CUSTOMER")
    @Produces(MediaType.APPLICATION_JSON)
    public Response buyTicket(@PathParam("id") Long eventId, BuyTicketDto request, @HeaderParam("Authorization") String authHeader) {
        try {
            String email = JwtUtil.getEmailFromToken(authHeader.substring(7));
            UserDao userDao = new UserDao();
            Customer customer = (Customer) userDao.findByEmail(email);
            if (customer == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Client introuvable.\"}")
                        .build();
            }

            EventDao eventDao = new EventDao();
            Event event = eventDao.findById(eventId);
            if (event == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Événement introuvable.\"}")
                        .build();
            }

            if (event.isCancelled()) {
                return Response.status(Response.Status.CONFLICT)
                        .entity("{\"error\": \"Cet événement est annulé, l'achat de billets est impossible.\"}")
                        .build();
            }

            if (request.numberOfTickets <= 0) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"Le nombre de tickets doit être supérieur à 0.\"}")
                        .build();
            }

            if (event.getNumberOfTickets() < request.numberOfTickets) {
                return Response.status(Response.Status.CONFLICT)
                        .entity("{\"error\": \"Pas assez de billets disponibles. Disponibles : " + event.getNumberOfTickets() + "\"}")
                        .build();
            }

            TicketDao ticketDao = new TicketDao();
            List<Ticket> tickets = ticketDao.buyTicket(request.numberOfTickets, event, customer);
            List<TicketResponseDto> response = tickets.stream()
                    .map(TicketResponseDto::new)
                    .collect(Collectors.toList());
            return Response.status(Response.Status.CREATED).entity(response).build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"" + e.getMessage() + "\"}")
                    .build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("{\"error\": \"" + e.getMessage() + "\"}")
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Erreur lors de l'achat du billet.\"}")
                    .build();
        }
    }

    /**
     * Annuler un événement (pose le flag cancelled=true, ne supprime pas)
     */
    @POST
    @Path("/cancel/{id}")
    @Operation(
            summary = "Annuler un événement"
    )
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
