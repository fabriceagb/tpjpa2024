package ressources;

import dao.TicketDao;
import dao.UserDao;
import entity.Ticket;
import entity.User;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

@Path("/api/ticket")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TicketRessource {

    /**
     * Acheter un billet pour un événement
     * POST http://localhost:8080/api/ticket/buy/{eventId}
     * Le client est identifié automatiquement via son token JWT.
     */
    @POST
    @Path("/buy/{eventId}")
    @RolesAllowed("USER_CUSTOMER")
    public Response buyTicket(@PathParam("eventId") Long eventId, @Context SecurityContext securityContext) {
        try {
            String email = securityContext.getUserPrincipal().getName();

            UserDao userDao = new UserDao();
            User user = userDao.findByEmail(email);
            if (user == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Utilisateur introuvable.\"}")
                        .build();
            }

            TicketDao ticketDao = new TicketDao();
            Ticket ticket = ticketDao.buyTicket(eventId, user.getId());
            return Response.status(Response.Status.CREATED).entity(ticket).build();

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
}
