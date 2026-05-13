package ressources;


import dao.UserDao;
import dto.LoginDto;
import dto.LoginResponseDto;
import dto.UpdateUserDto;
import dto.UserDto;
import entity.Customer;
import entity.Manager;
import entity.Administrator;
import entity.User;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.JwtUtil;


@Path("/api/user")
@Produces({"application/json", "application/xml"})
public class UserRessource {
    private static final Logger log = LoggerFactory.getLogger(UserRessource.class);

    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addUser(UserDto request){
        // Initialisation du DAO
        UserDao userDao = new UserDao();

        try {
            // 1. Vérification si l'email existe déjà
            if (request.getEmail() != null && userDao.findByEmail(request.getEmail()) != null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"Un utilisateur avec cet email existe déjà.\"}")
                        .build();
            }

            // 2. Vérification et instanciation selon le rôle
            String role = request.getRole();
            if (role == null || role.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"Le rôle est obligatoire (ex: CUSTOMER, MANAGER, ADMIN).\"}")
                        .build();
            }

            User user;
            switch (role.toUpperCase()) {
                case "USER_CUSTOMER":
                    user = new Customer();
                    break;
                case "USER_MANAGER":
                    user = new Manager();
                    break;
                case "USER_ADMINISTRATOR":
                    user = new Administrator();
                    break;
                default:
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity("{\"error\": \"Rôle inconnu : " + role + "\"}")
                            .build();
            }

            // 3. Pour hacher le mot de passe (avant l'insertion en base)
            String motDePasseClair = request.getPassword();
            String motDePasseHache = BCrypt.hashpw(motDePasseClair, BCrypt.gensalt());

            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setPassword(motDePasseHache);
            user.setEmail(request.getEmail());
            user.setRole(role.toUpperCase());
            user.setPhoneNumber(request.getPhoneNumber());
            
            // 4. Sauvegarde
            userDao.create(user);
            return Response.status(Response.Status.CREATED).entity(user).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Erreur lors de la création de l'utilisateur.")
                    .build();
        }
    }

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(LoginDto request) {

         UserDao userDao = new UserDao();

        try {
            User user = userDao.findByEmail(request.getEmail());

            if (user == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"error\": \"Email ou mot de passe incorrect.\"}")
                        .build();
            }


            boolean isPasswordValid = BCrypt.checkpw(request.getPassword(), user.getPassword());

            if (isPasswordValid) {

                String token = JwtUtil.generateToken(user.getEmail(),  user.getRole());
                LoginResponseDto loginResponseDto  = new LoginResponseDto();
                loginResponseDto.setToken(token);
                loginResponseDto.setEmail(user.getEmail());
                loginResponseDto.setFirstName(user.getFirstName());
                loginResponseDto.setLastName(user.getLastName());
                loginResponseDto.setRole(user.getRole());
                loginResponseDto.setPhone(user.getPhoneNumber());
                return Response.ok(loginResponseDto).build();

            } else {
                // Mauvais mot de passe -> Erreur 401
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"error\": \"Email ou mot de passe incorrect.\"}")
                        .build();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Erreur interne du serveur.\"}")
                    .build();
        }
    }

    @POST
    @Path("/update")
    @RolesAllowed({"USER_CUSTOMER", "USER_MANAGER", "USER_ADMINISTRATOR"})
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(UpdateUserDto request, @HeaderParam("Authorization") String authHeader) {

        UserDao userDao = new UserDao();

        try {
            String email = JwtUtil.getEmailFromToken(authHeader.substring(7));

            User updateData = new User();
            updateData.setFirstName(request.getFirstName());
            updateData.setLastName(request.getLastName());
            updateData.setEmail(request.getEmail());
            updateData.setPhoneNumber(request.getPhoneNumber());

            userDao.update(email, updateData);

            User updated = userDao.findByEmail(
                request.getEmail() != null ? request.getEmail() : email
            );
            if (updated != null) updated.setPassword(null);
            return Response.ok(updated).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Erreur interne du serveur.\"}")
                    .build();
        }
    }


}
