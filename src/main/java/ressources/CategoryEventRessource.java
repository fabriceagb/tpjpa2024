package ressources;


import dao.CategoryEventDao;
import dto.CategoryEventDto;
import entity.CategoryEvent;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/api/categoryEvent")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CategoryEventRessource {
    /**
     * READ ALL : Récupérer toutes les catégories
     * GET http://localhost:8080/votre-app/api/categories/all
     */
    @GET
    @Path("/all")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllCategories() {
        CategoryEventDao dao = new CategoryEventDao();
        try {
            List<CategoryEvent> categories = dao.findAll();
            return Response.ok(categories).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Erreur lors de la récupération des catégories.\"}")
                    .build();
        }
    }

    /**
     * READ ONE : Récupérer une catégorie par son ID
     * GET http://localhost:8080/votre-app/api/categories/1
     */
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCategoryById(@PathParam("id") Long id) {
        CategoryEventDao dao = new CategoryEventDao();
        try {
            CategoryEvent category = dao.findById(id);
            if (category != null) {
                return Response.ok(category).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Catégorie introuvable.\"}")
                        .build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * SEARCH : Chercher une catégorie par son nom exact
     * GET http://localhost:8080/votre-app/api/categories/search?name=Concert
     */
    @GET
    @Path("/search")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCategoryByName(@QueryParam("name") String name) {
        if (name == null || name.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Le paramètre 'name' est requis.\"}")
                    .build();
        }

        CategoryEventDao dao = new CategoryEventDao();
        try {
            CategoryEvent category = dao.findByNameCriteria(name);
            if (category != null) {
                return Response.ok(category).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Aucune catégorie trouvée avec ce nom.\"}")
                        .build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * CREATE : Ajouter une nouvelle catégorie
     * POST http://localhost:8080/votre-app/api/categories/add
     */
    @POST
    @Path("/add")
    @RolesAllowed("USER_ADMINISTRATOR")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createCategory(CategoryEventDto request) {
        CategoryEventDao dao = new CategoryEventDao();
        try {
            CategoryEvent category = new CategoryEvent();
            category.setLibelle(request.getLibelle());
            dao.create(category);
            return Response.status(Response.Status.CREATED).entity(category).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Erreur lors de la création de la catégorie.\"}")
                    .build();
        }
    }



    /**
     * UPDATE : Modifier une catégorie existante
     * PUT http://localhost:8080/votre-app/api/categories/update/1
     */
    @PUT
    @Path("/update/{id}")
    @RolesAllowed("USER_ADMINISTRATOR")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateCategory(@PathParam("id") Long id, CategoryEventDto request) {
        CategoryEventDao dao = new CategoryEventDao();
        try {
            // 1. Vérifier si la catégorie existe d'abord
            CategoryEvent existingCategory = dao.findById(id);
            if (existingCategory == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Catégorie introuvable pour mise à jour.\"}")
                        .build();
            }
            existingCategory.setLibelle(request.getLibelle());
            CategoryEvent updatedCategory = dao.update(existingCategory);

            return Response.ok(updatedCategory).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Erreur lors de la mise à jour.\"}")
                    .build();
        }
    }

    /**
     * DELETE : Supprimer une catégorie
     * DELETE http://localhost:8080/votre-app/api/categories/delete/1
     */
    @DELETE
    @Path("/delete/{id}")
    @RolesAllowed("USER_ADMINISTRATOR")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteCategory(@PathParam("id") Long id) {
        CategoryEventDao dao = new CategoryEventDao();
        try {
            CategoryEvent existingCategory = dao.findById(id);
            if (existingCategory == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Catégorie introuvable.\"}")
                        .build();
            }

            dao.delete(id);
            return Response.ok("{\"message\": \"Catégorie supprimée avec succès.\"}").build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Erreur lors de la suppression. Peut-être qu'elle est liée à des événements existants ?\"}")
                    .build();
        }
    }

}
