# TPJPA2024 — Système de Gestion d'Événements

API REST back-end développée avec Jakarta EE, JPA/Hibernate et RESTEasy, permettant de gérer des événements, des catégories et l'achat de billets par des clients authentifiés.

---

## Table des matières

1. [Modèle métier](#1-modèle-métier)
2. [Diagramme de classes](#2-diagramme-de-classes)
3. [Couche JPA — Entités](#3-couche-jpa--entités)
4. [Couche DAO](#4-couche-dao)
5. [API REST — Endpoints](#5-api-rest--endpoints)
6. [Authentification et rôles](#6-authentification-et-rôles)
7. [Lancer le projet](#7-lancer-le-projet)

---

## 1. Modèle métier

L'application est une **plateforme de gestion d'événements** articulée autour de trois acteurs :

| Rôle | Identifiant technique | Responsabilités |
| --- | --- | --- |
| **Administrateur** | `USER_ADMINISTRATOR` | Crée et supprime les catégories d'événements ; peut annuler ou supprimer des événements |
| **Manager** | `USER_MANAGER` | Crée, modifie, annule ses propres événements |
| **Client** | `USER_CUSTOMER` | Consulte les événements disponibles et achète des billets |

**Flux principal :**

1. Un **Administrateur** crée des catégories (`CategoryEvent`).
2. Un **Manager** crée des événements (`Event`) rattachés à une catégorie, avec un prix, une date et un stock de billets.
3. Un **Client** recherche les événements (par lieu, catégorie, fourchette de prix, mot-clé…) et achète un ou plusieurs billets (`Ticket`).
4. Chaque billet reçoit un numéro unique de format `TICK-{UUID}` et mémorise le prix au moment de l'achat.
5. Un événement peut être **annulé** (suppression logique via le champ `cancelled`) ; aucun billet ne peut alors être acheté pour cet événement.

---

## 2. Diagramme de classes

```
                        ┌─────────────────────────────┐
                        │           User               │
                        │─────────────────────────────│
                        │ - id : Long (PK)             │
                        │ - firstName : String         │
                        │ - lastName : String          │
                        │ - email : String             │
                        │ - password : String (BCrypt) │
                        │ - role : String              │
                        │ - phoneNumber : String       │
                        └──────────────┬──────────────┘
                    Héritage JOINED    │
          ┌───────────────────┬────────┴───────────────┐
          │                   │                        │
  ┌───────┴────────┐  ┌───────┴────────┐  ┌───────────┴──────────┐
  │  Administrator │  │    Manager     │  │       Customer        │
  │────────────────│  │────────────────│  │───────────────────────│
  │  (pas de champ │  │  (pas de champ │  │  tickets : List<Ticket>│
  │   additionnel) │  │   additionnel) │  │  (mappedBy="customer") │
  └────────────────┘  └───────┬────────┘  └──────────┬────────────┘
                              │ 1                     │ 1
                              │                       │
                              │ *                     │ *
                    ┌─────────┴──────────┐   ┌────────┴──────────┐
                    │       Event         │   │      Ticket        │
                    │────────────────────│   │───────────────────│
                    │ - id : Long (PK)   │   │ - id : Long (PK)  │
                    │ - label : String   │   │ - number : String │
                    │ - description      │1  │ - price : double  │
                    │ - location         ├──►│ - event : Event   │
                    │ - price : double   │ * │ - customer        │
                    │ - date : Date      │   └───────────────────┘
                    │ - popularity : int │
                    │ - numberOfTickets  │
                    │ - cancelled : bool │
                    └────────┬───────────┘
                             │ *
                             │
                             │ 1
                   ┌─────────┴──────────┐
                   │   CategoryEvent     │
                   │────────────────────│
                   │ - id : Long (PK)   │
                   │ - libelle : String │
                   │ - events (mappedBy)│
                   └────────────────────┘
```

**Relations bidirectionnelles (mappedBy) :**

| Relation | Type | mappedBy côté propriétaire |
| --- | --- | --- |
| `CategoryEvent` ↔ `Event` | `@OneToMany` / `@ManyToOne` | `categoryEvent` dans `CategoryEvent` |
| `Event` ↔ `Ticket` | `@OneToMany` / `@ManyToOne` | `event` dans `Event` |
| `Customer` ↔ `Ticket` | `@OneToMany` / `@ManyToOne` | `customer` dans `Customer` |

**Héritage :** stratégie `JOINED` — chaque sous-classe (`Administrator`, `Manager`, `Customer`) possède sa propre table, liée à la table `User` par clé primaire partagée.

---

## 3. Couche JPA — Entités

### User (classe mère — héritage JOINED)
Classe de base contenant les informations communes à tous les utilisateurs. Utilise `@Inheritance(strategy = InheritanceType.JOINED)`.

### Administrator
Sous-classe de `User` sans champ additionnel. Représente un utilisateur disposant des droits d'administration des catégories.

### Manager
Sous-classe de `User`. Responsable de la création et de la gestion des événements.

### Customer
Sous-classe de `User`. Porte la relation bidirectionnelle `@OneToMany(mappedBy="customer")` vers `Ticket`.

### Event
Entité centrale du modèle. Déclare **5 requêtes nommées** (`@NamedQuery`) :

```java
@NamedQuery(name = "Event.findAll",
            query = "SELECT e FROM Event e")
@NamedQuery(name = "Event.findUpcoming",
            query = "SELECT e FROM Event e WHERE e.date >= CURRENT_DATE")
@NamedQuery(name = "Event.findByLocation",
            query = "SELECT e FROM Event e WHERE e.location = :location")
@NamedQuery(name = "Event.findByCategory",
            query = "SELECT e FROM Event e WHERE e.categoryEvent.id = :categoryId")
@NamedQuery(name = "Event.findByManager",
            query = "SELECT e FROM Event e WHERE e.manager.id = :managerId")
```

Porte deux relations bidirectionnelles : `@OneToMany(mappedBy="event")` vers `Ticket` et `@ManyToOne` vers `CategoryEvent`.  
Implémente la suppression logique via le champ `cancelled` (boolean, défaut `false`).

### Ticket
Généré à l'achat. Numéro unique au format `TICK-{UUID}`. Mémorise le prix de l'événement au moment de la transaction (le prix peut changer ensuite).

### CategoryEvent
Relation bidirectionnelle avec `Event` via `@OneToMany(mappedBy="categoryEvent")` annotée `@JsonIgnore` pour éviter les cycles de sérialisation JSON.

---

## 4. Couche DAO

### Pattern générique

Toutes les DAOs étendent `AbstractJpaDao<K, T>` qui implémente `IGenericDao<K, T>`.  
Opérations CRUD héritées : `findOne`, `findAll`, `save`, `update`, `delete`, `deleteById`.

### Une DAO par entité

| DAO | Entité servie |
| --- | --- |
| `UserDao` | `User` |
| `CustomerDao` | `Customer` |
| `ManagerDao` | `Manager` |
| `AdministratorDao` | `Administrator` |
| `EventDao` | `Event` |
| `TicketDao` | `Ticket` |
| `CategoryEventDao` | `CategoryEvent` |

---

### Requêtes JPQL

**UserDao** — recherche par email :
```java
// JPQL directe
"SELECT u FROM User u WHERE u.email = :email"
```

**EventDao** — recherche par fourchette de prix :
```java
// JPQL directe
"SELECT e FROM Event e WHERE e.price BETWEEN :min AND :max"
```

**CategoryEventDao / ManagerDao** — recherche par email / liste :
```java
"SELECT c FROM CategoryEvent c"
"SELECT m FROM Manager m WHERE m.email = :email"
```

---

### Requêtes nommées (Named Queries)

Toutes déclarées sur l'entité `Event` et invoquées dans `EventDao` :

```java
eventDao.findAll()               // utilise Event.findAll
eventDao.findUpcomingEvents()    // utilise Event.findUpcoming
eventDao.findEventsByLocation()  // utilise Event.findByLocation
eventDao.findByCategory()        // utilise Event.findByCategory
eventDao.findByManager()         // utilise Event.findByManager
```

---

### Criteria Queries

**EventDao** — recherche par mot-clé dans le label (insensible à la casse) :
```java
CriteriaBuilder cb = em.getCriteriaBuilder();
CriteriaQuery<Event> cq = cb.createQuery(Event.class);
Root<Event> root = cq.from(Event.class);
cq.where(cb.like(cb.lower(root.get("label")), "%" + keyword.toLowerCase() + "%"));
```

**CategoryEventDao** — recherche par nom exact :
```java
CriteriaBuilder cb = em.getCriteriaBuilder();
CriteriaQuery<CategoryEvent> cq = cb.createQuery(CategoryEvent.class);
Root<CategoryEvent> root = cq.from(CategoryEvent.class);
cq.where(cb.equal(root.get("libelle"), name));
```

---

### Méthodes métier

**TicketDao.buyTicket** — achat de N billets :
```java
// Crée N entités Ticket, décrémente event.numberOfTickets, persiste en base
List<Ticket> buyTicket(int numberOfTickets, Event event, Customer customer)
```

**EventDao.cancelEvent** — annulation logique :
```java
// Positionne cancelled = true sur l'événement, retourne l'entité mise à jour
Event cancelEvent(Long eventId)
```

---

## 5. API REST — Endpoints

> La documentation OpenAPI interactive est disponible à : **http://localhost:8080/api**

---

### `/api/user` — Utilisateurs

#### `POST /api/user/register` — Inscription
| Champ | Valeur |
| --- | --- |
| Sécurité | Public |
| Content-Type | `application/json` |
| Statut succès | `201 Created` |

**Corps de la requête**
```json
{
  "email": "alice@example.com",
  "password": "secret",
  "firstName": "Alice",
  "lastName": "Dupont",
  "role": "USER_CUSTOMER",
  "phoneNumber": "0600000000"
}
```
Valeurs valides pour `role` : `USER_CUSTOMER`, `USER_MANAGER`, `USER_ADMINISTRATOR`.

**Réponse 201**
```json
{
  "id": 1,
  "firstName": "Alice",
  "lastName": "Dupont",
  "email": "alice@example.com",
  "password": "$2a$10$hashedpassword...",
  "role": "USER_CUSTOMER",
  "phoneNumber": "0600000000"
}
```

**Erreurs**
```json
400 { "error": "Un utilisateur avec cet email existe déjà." }
400 { "error": "Le rôle est obligatoire (ex: CUSTOMER, MANAGER, ADMIN)." }
400 { "error": "Rôle inconnu : XXX" }
```

---

#### `POST /api/user/login` — Connexion
| Champ | Valeur |
| --- | --- |
| Sécurité | Public |
| Content-Type | `application/json` |
| Statut succès | `200 OK` |

**Corps de la requête**
```json
{
  "email": "alice@example.com",
  "password": "secret"
}
```

**Réponse 200**
```json
{
  "id": 1,
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "email": "alice@example.com",
  "firstName": "Alice",
  "lastName": "Dupont",
  "role": "USER_CUSTOMER",
  "phone": "0600000000"
}
```

**Erreurs**
```json
401 { "error": "Email ou mot de passe incorrect." }
```

---

#### `POST /api/user/update` — Mise à jour du profil
| Champ | Valeur |
| --- | --- |
| Sécurité | `USER_CUSTOMER`, `USER_MANAGER`, `USER_ADMINISTRATOR` |
| En-tête requis | `Authorization: Bearer <token>` |
| Content-Type | `application/json` |
| Statut succès | `200 OK` |

**Corps de la requête**
```json
{
  "firstName": "Alice",
  "lastName": "Martin",
  "email": "alice.martin@example.com",
  "phoneNumber": "0611111111"
}
```

**Réponse 200**
```json
{
  "id": 1,
  "firstName": "Alice",
  "lastName": "Martin",
  "email": "alice.martin@example.com",
  "password": "$2a$10$hashedpassword...",
  "role": "USER_CUSTOMER",
  "phoneNumber": "0611111111"
}
```

---

### `/api/event` — Événements

#### `GET /api/event/all` — Liste de tous les événements
| Champ | Valeur |
| --- | --- |
| Sécurité | Public |
| Statut succès | `200 OK` |

**Réponse 200** — tableau d'événements
```json
[
  {
    "id": 1,
    "label": "Festival Jazz",
    "description": "Grand festival de jazz en plein air",
    "location": "Paris",
    "price": 45.0,
    "date": "2025-07-15T17:00:00.000+0000",
    "popularity": 5,
    "numberOfTickets": 500,
    "cancelled": false,
    "manager": { "id": 2, "firstName": "Bob", "lastName": "Dupont", "email": "bob@example.com", "role": "USER_MANAGER" },
    "categoryEvent": { "id": 1, "libelle": "Musique" },
    "tickets": []
  }
]
```

---

#### `GET /api/event/{id}` — Détail d'un événement
| Champ | Valeur |
| --- | --- |
| Sécurité | Public |
| Paramètre | `id` (Long) — identifiant de l'événement |
| Statut succès | `200 OK` |

**Réponse 200** — objet Event (même structure que ci-dessus)

**Erreurs**
```json
404 { "error": "Événement introuvable." }
```

---

#### `GET /api/event/upcoming` — Événements à venir
| Champ | Valeur |
| --- | --- |
| Sécurité | Public |
| Statut succès | `200 OK` |

Retourne tous les événements dont la date est supérieure ou égale à aujourd'hui.

**Réponse 200** — tableau d'événements (même structure que `/all`)

---

#### `GET /api/event/search?label={mot}` — Recherche par mot-clé
| Champ | Valeur |
| --- | --- |
| Sécurité | Public |
| Paramètre query | `label` (String) — mot-clé recherché dans le titre |
| Statut succès | `200 OK` |

Recherche insensible à la casse via Criteria Query.

**Réponse 200** — tableau d'événements correspondants

**Erreurs**
```json
400 { "error": "Le paramètre 'label' est requis." }
```

---

#### `GET /api/event/category/{categoryId}` — Événements par catégorie
| Champ | Valeur |
| --- | --- |
| Sécurité | Public |
| Paramètre | `categoryId` (Long) |
| Statut succès | `200 OK` |

**Réponse 200** — tableau d'événements de la catégorie demandée

---

#### `GET /api/event/manager/{managerId}` — Événements par manager
| Champ | Valeur |
| --- | --- |
| Sécurité | Public |
| Paramètre | `managerId` (Long) |
| Statut succès | `200 OK` |

**Réponse 200** — tableau d'événements créés par ce manager

---

#### `GET /api/event/location/{location}` — Événements par lieu
| Champ | Valeur |
| --- | --- |
| Sécurité | Public |
| Paramètre | `location` (String) — nom du lieu exact |
| Statut succès | `200 OK` |

**Réponse 200** — tableau d'événements se tenant dans ce lieu

---

#### `GET /api/event/price?min={min}&max={max}` — Événements par fourchette de prix
| Champ | Valeur |
| --- | --- |
| Sécurité | Public |
| Paramètres query | `min` (Double, défaut `0.0`) et `max` (Double, défaut `MAX_VALUE`) |
| Statut succès | `200 OK` |

**Réponse 200** — tableau d'événements dont le prix est compris entre `min` et `max`

---

#### `POST /api/event/add` — Créer un événement
| Champ | Valeur |
| --- | --- |
| Sécurité | `USER_MANAGER` |
| En-tête requis | `Authorization: Bearer <token>` |
| Content-Type | `application/json` |
| Statut succès | `201 Created` |

Le manager est automatiquement résolu depuis le token JWT.

**Corps de la requête**
```json
{
  "label": "Festival Jazz",
  "description": "Grand festival de jazz en plein air",
  "location": "Paris",
  "date": "15/07/2025 19:00",
  "price": 45.0,
  "popularity": 5,
  "numberOfTickets": 500,
  "categoryId": 1
}
```
Format de la date : `dd/MM/yyyy HH:mm`.

**Réponse 201**
```json
{
  "id": 3,
  "label": "Festival Jazz",
  "description": "Grand festival de jazz en plein air",
  "location": "Paris",
  "price": 45.0,
  "date": "2025-07-15T17:00:00.000+0000",
  "popularity": 5,
  "numberOfTickets": 500,
  "cancelled": false,
  "manager": { "id": 2, "firstName": "Bob", "lastName": "Dupont", "email": "bob@example.com", "role": "USER_MANAGER" },
  "categoryEvent": { "id": 1, "libelle": "Musique" },
  "tickets": []
}
```

**Erreurs**
```json
403 { "error": "Aucun manager trouvé pour ce compte." }
400 { "error": "Catégorie introuvable." }
400 { "error": "Format de date invalide, utilisez dd/MM/yyyy HH:mm" }
```

---

#### `PUT /api/event/update/{id}` — Modifier un événement
| Champ | Valeur |
| --- | --- |
| Sécurité | `USER_MANAGER` |
| En-tête requis | `Authorization: Bearer <token>` |
| Paramètre | `id` (Long) — identifiant de l'événement |
| Content-Type | `application/json` |
| Statut succès | `200 OK` |

**Corps de la requête**
```json
{
  "label": "Festival Jazz 2025",
  "description": "Nouvelle description mise à jour",
  "location": "Lyon",
  "price": 50.0,
  "popularity": 8
}
```

**Réponse 200** — événement mis à jour (même structure que `/add`)

**Erreurs**
```json
404 { "error": "Événement introuvable pour la mise à jour." }
```

---

#### `DELETE /api/event/delete/{id}` — Supprimer définitivement un événement
| Champ | Valeur |
| --- | --- |
| Sécurité | `USER_MANAGER`, `USER_ADMINISTRATOR` |
| En-tête requis | `Authorization: Bearer <token>` |
| Paramètre | `id` (Long) |
| Statut succès | `200 OK` |

**Réponse 200**
```json
{ "message": "Événement supprimé avec succès." }
```

**Erreurs**
```json
404 { "error": "Événement introuvable." }
```

---

#### `POST /api/event/cancel/{id}` — Annuler un événement
| Champ | Valeur |
| --- | --- |
| Sécurité | `USER_MANAGER`, `USER_ADMINISTRATOR` |
| En-tête requis | `Authorization: Bearer <token>` |
| Paramètre | `id` (Long) |
| Statut succès | `200 OK` |

Suppression logique : positionne `cancelled = true` sans supprimer l'enregistrement.

**Réponse 200** — événement avec `"cancelled": true`
```json
{
  "id": 3,
  "label": "Festival Jazz",
  "cancelled": true,
  ...
}
```

**Erreurs**
```json
404 { "error": "Événement introuvable." }
400 { "error": "Cet événement est déjà annulé." }
```

---

#### `POST /api/event/{id}/buy` — Acheter des billets
| Champ | Valeur |
| --- | --- |
| Sécurité | `USER_CUSTOMER` |
| En-tête requis | `Authorization: Bearer <token>` |
| Paramètre | `id` (Long) — identifiant de l'événement |
| Content-Type | `application/json` |
| Statut succès | `201 Created` |

**Corps de la requête**
```json
{
  "numberOfTickets": 2
}
```

**Réponse 201** — liste de `TicketResponseDto`
```json
[
  {
    "id": 10,
    "number": "TICK-550e8400-e29b-41d4-a716-446655440000",
    "price": 45.0,
    "eventId": 3,
    "eventLabel": "Festival Jazz",
    "customerId": 7,
    "customerEmail": "alice@example.com"
  },
  {
    "id": 11,
    "number": "TICK-661f9511-f30c-52e5-b827-557766551111",
    "price": 45.0,
    "eventId": 3,
    "eventLabel": "Festival Jazz",
    "customerId": 7,
    "customerEmail": "alice@example.com"
  }
]
```

**Erreurs**
```json
404 { "error": "Événement introuvable." }
409 { "error": "Cet événement est annulé, l'achat de billets est impossible." }
400 { "error": "Le nombre de tickets doit être supérieur à 0." }
409 { "error": "Pas assez de billets disponibles. Disponibles : 1" }
```

---

### `/api/categoryEvent` — Catégories d'événements

#### `GET /api/categoryEvent/all` — Liste toutes les catégories
| Champ | Valeur |
| --- | --- |
| Sécurité | Public |
| Statut succès | `200 OK` |

**Réponse 200**
```json
[
  { "id": 1, "libelle": "Musique" },
  { "id": 2, "libelle": "Sport" },
  { "id": 3, "libelle": "Théâtre" }
]
```

---

#### `GET /api/categoryEvent/{id}` — Détail d'une catégorie
| Champ | Valeur |
| --- | --- |
| Sécurité | Public |
| Paramètre | `id` (Long) |
| Statut succès | `200 OK` |

**Réponse 200**
```json
{ "id": 1, "libelle": "Musique" }
```

**Erreurs**
```json
404 { "error": "Catégorie introuvable." }
```

---

#### `GET /api/categoryEvent/search?name={nom}` — Recherche par nom exact
| Champ | Valeur |
| --- | --- |
| Sécurité | Public |
| Paramètre query | `name` (String) — nom exact de la catégorie |
| Statut succès | `200 OK` |

Recherche via Criteria Query avec prédicat d'égalité stricte.

**Réponse 200**
```json
{ "id": 1, "libelle": "Musique" }
```

**Erreurs**
```json
400 { "error": "Le paramètre 'name' est requis." }
404 { "error": "Aucune catégorie trouvée avec ce nom." }
```

---

#### `POST /api/categoryEvent/add` — Créer une catégorie
| Champ | Valeur |
| --- | --- |
| Sécurité | `USER_ADMINISTRATOR` |
| En-tête requis | `Authorization: Bearer <token>` |
| Content-Type | `application/json` |
| Statut succès | `201 Created` |

**Corps de la requête**
```json
{ "libelle": "Musique" }
```

**Réponse 201**
```json
{ "id": 1, "libelle": "Musique" }
```

---

#### `PUT /api/categoryEvent/update/{id}` — Modifier une catégorie
| Champ | Valeur |
| --- | --- |
| Sécurité | `USER_ADMINISTRATOR` |
| En-tête requis | `Authorization: Bearer <token>` |
| Paramètre | `id` (Long) |
| Content-Type | `application/json` |
| Statut succès | `200 OK` |

**Corps de la requête**
```json
{ "libelle": "Musique & Arts" }
```

**Réponse 200**
```json
{ "id": 1, "libelle": "Musique & Arts" }
```

**Erreurs**
```json
404 { "error": "Catégorie introuvable pour mise à jour." }
```

---

#### `DELETE /api/categoryEvent/delete/{id}` — Supprimer une catégorie
| Champ | Valeur |
| --- | --- |
| Sécurité | `USER_ADMINISTRATOR` |
| En-tête requis | `Authorization: Bearer <token>` |
| Paramètre | `id` (Long) |
| Statut succès | `200 OK` |

**Réponse 200**
```json
{ "message": "Catégorie supprimée avec succès." }
```

**Erreurs**
```json
404 { "error": "Catégorie introuvable." }
500 { "error": "Erreur lors de la suppression. Peut-être qu'elle est liée à des événements existants ?" }
```

---

## 6. Authentification et rôles

L'authentification repose sur des **tokens JWT** (expiration : 2 heures, encodage HMAC-SHA256).

**Utilisation :**
1. Appeler `POST /api/user/login` pour obtenir le token.
2. Inclure le token dans chaque requête protégée via l'en-tête HTTP :
   ```
   Authorization: Bearer <token>
   ```

**Matrice des permissions :**

| Action | `USER_CUSTOMER` | `USER_MANAGER` | `USER_ADMINISTRATOR` |
| --- | :---: | :---: | :---: |
| Consulter les événements / catégories | ✓ | ✓ | ✓ |
| Acheter des billets | ✓ | — | — |
| Créer / modifier des événements | — | ✓ | — |
| Annuler / supprimer des événements | — | ✓ | ✓ |
| Gérer les catégories | — | — | ✓ |

---

## 7. Lancer le projet

### Prérequis
- Java 11+
- Maven 3.6+

### Démarrage
```bash
mvn clean package jetty:run
```

Le serveur démarre sur **http://localhost:8080**.

### Documentation Swagger interactive
**http://localhost:8080/api**

### Exemples de démarrage rapide

```bash
# 1. Créer un administrateur
curl -X POST http://localhost:8080/api/user/register \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@test.com","password":"admin","firstName":"Admin","lastName":"Test","role":"USER_ADMINISTRATOR","phoneNumber":"0600000000"}'

# 2. Se connecter et récupérer le token JWT
curl -X POST http://localhost:8080/api/user/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@test.com","password":"admin"}'

# 3. Créer une catégorie (token requis)
curl -X POST http://localhost:8080/api/categoryEvent/add \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN>" \
  -d '{"libelle":"Musique"}'

# 4. Consulter tous les événements (public)
curl http://localhost:8080/api/event/all
```
