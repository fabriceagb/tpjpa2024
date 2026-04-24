package ressources;

import entity.User;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;

@Path("user")
@Produces({"application/json", "application/xml"})
public class UserRessource {
    @GET
    public Response addUser(){
        return Response.ok().entity("SUCCESS").build();
    }

    @POST
    public Response login(){
        User user = new User();
        return Response.ok().entity("SUCCESS").build();
    }
}
