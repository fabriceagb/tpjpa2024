package dao;

import dao.generic.AbstractJpaDao;
import entity.User;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;

public class UserDao extends AbstractJpaDao<Long, User> {
    public UserDao(){
        super();
    }

    public void create(User user) {
        EntityTransaction transaction = this.entityManager.getTransaction();
        try {
            transaction.begin();

            this.entityManager.persist(user);

            transaction.commit();
            System.out.println("Utilisateur créé avec succès !");
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }

    public User findByEmail(String email) {
        try {
            // Création d'une requête JPQL pour chercher par email
            return entityManager.createQuery("SELECT u FROM User u WHERE u.email = :email", User.class)
                    .setParameter("email", email)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public void update(String email,User updateUser){
        EntityTransaction transaction = this.entityManager.getTransaction();

        try{
            // Identification de l'utilisateur
            User user = this.findByEmail(email);
            user.setEmail(updateUser.getEmail());
            user.setFirstName(updateUser.getFirstName());
            user.setLastName(updateUser.getLastName());

            this.entityManager.merge(user);

            transaction.commit();
            System.out.println("Utilisateur modifié avec succès !");
        }
        catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }




}
