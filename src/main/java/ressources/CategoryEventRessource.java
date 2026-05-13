package ressources;


import dao.CategoryEventDao;
import dto.CategoryEventDto;
import entity.CategoryEvent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Tag(name = "CategoryEvent", description = "Gestion des categories d'evernement")
@Path("/api/categoryEvent")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CategoryEventRessource {
    /**
     * Récupérer toutes les catégories
     */
    @GET
    @Path("/all")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Récupérer toutes les catégories"
    )
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
     * Récupérer une catégorie par son ID
     */
    @GET
    @Path("/{id}")
    @Operation(
            summary = "Récupérer une catégorie par son ID"
    )
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
     * Chercher une catégorie par son nom exact
     */
    @GET
    @Path("/search")
    @Operation(
            summary = "Chercher une catégorie par son nom exact"
    )
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
     * Ajouter une nouvelle catégorie
     */

    @POST
    @Path("/add")
    @Operation(
            summary = "Ajouter une nouvelle catégorie"
    )
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
     * Modifier une catégorie existante
     */
    @PUT
    @Path("/update/{id}")
    @Operation(
            summary = "Modifier une catégorie existante"
    )
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
     * Supprimer une catégorie
     */
    @DELETE
    @Path("/delete/{id}")
    @Operation(
            summary = "Supprimer une catégorie"
    )
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
