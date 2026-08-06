# Spring WebSocket Chat

Application de chat en temps réel construite avec Spring Boot (WebSocket + STOMP) côté serveur et une interface front-end moderne (HTML/CSS/JS vanilla) côté client, dans le style d'un client de messagerie (Discord/Messenger).

## Fonctionnalités

**Backend (Spring Boot)**
- WebSocket + STOMP via SockJS, endpoint `/ws`
- Broker simple sur `/topic/public` (diffusion) et `/queue/**` (messages privés par session, historique + erreurs)
- Envoi de message : `/app/chat.sendMessage`
- Arrivée d'un utilisateur : `/app/chat.addUser`
- Diffusion automatique d'un message `LEAVE` quand une session WebSocket se ferme (déconnexion propre ou fermeture d'onglet), via `WebSocketEventListener`
- **Persistance H2** : chaque message (`CHAT`, `JOIN`, `LEAVE`) est sauvegardé en base via Spring Data JPA (`ChatMessageRepository`). La base est un fichier H2 (`./data/chatdb.mv.db`), donc les données survivent aux redémarrages
- **Historique rejoué à la connexion** : à la connexion, un nouvel arrivant reçoit en privé (`/user/queue/history`) les 50 derniers messages de la conversation avant que son propre message `JOIN` ne soit diffusé à tout le monde
- **Règle métier : pseudo déjà utilisé** — `ActiveUserRegistry` garde en mémoire la liste des pseudos actuellement connectés. Si quelqu'un tente de rejoindre avec un pseudo déjà pris, `ChatController` lève une `UsernameAlreadyInUseException`, interceptée par `StompExceptionHandler` (package `org.fab.chat.exception`) qui renvoie une erreur `USERNAME_TAKEN` uniquement à ce client via `/user/queue/errors` (le pseudo est libéré automatiquement à la déconnexion)

**Frontend (`src/main/resources/static`)**
- Popup de connexion (modal) avec animation "Connecting..." (spinner)
- Dark / Light mode, sauvegardé dans `localStorage` (persiste après rechargement)
- Bouton Disconnect qui ferme proprement la connexion WebSocket et revient au popup
- Avatar circulaire = première lettre du pseudo (dans le header et sur chaque message reçu)
- Messages envoyés alignés à droite, messages reçus alignés à gauche avec avatar
- Messages `JOIN` / `LEAVE` centrés sous forme de pastille discrète
- Horodatage (heure) sur chaque message
- Interface responsive (mobile/desktop) et animations CSS (fond animé, apparition des messages, transitions)
- Contenu des messages inséré en `textContent` (jamais `innerHTML`) pour éviter toute injection HTML/XSS via le pseudo ou le message

Si un pseudo est refusé (déjà pris), le popup reste ouvert et affiche l'erreur en rouge : il suffit de choisir un autre pseudo et de resoumettre le formulaire, sans recréer la connexion WebSocket.

## Prérequis

- Java 21
- Maven (le wrapper `mvnw` / `mvnw.cmd` est fourni, pas besoin d'installation globale)

## Lancer l'application (port 8080 par défaut)

Aucun port n'est configuré dans `application.properties` : Spring Boot démarre donc sur le port **8080** par défaut.

Depuis la racine du projet :

```bash
./mvnw spring-boot:run
```

Sur Windows :

```bash
mvnw.cmd spring-boot:run
```

Ou en buildant puis en exécutant le jar :

```bash
./mvnw clean package
java -jar target/chat-0.0.1-SNAPSHOT.jar
```

Une fois démarré, ouvrir :

```
http://localhost:8080
```

Pour changer de port, ajouter dans `src/main/resources/application.properties` :

```properties
server.port=8081
```

## Structure du projet

```
src/main/java/org/fab/chat/
├── controller/ChatController.java        # /app/chat.sendMessage, /app/chat.addUser
├── config/WebSocketConfig.java           # endpoint /ws, broker /topic + /queue
├── config/WebSocketEventListener.java    # broadcast LEAVE + libère le pseudo à la déconnexion
├── entities/ChatMessage.java             # entité JPA + DTO WebSocket (id, sender, content, type, timestamp)
├── repository/ChatMessageRepository.java # accès H2 (historique des 50 derniers messages)
├── service/ActiveUserRegistry.java       # pseudos actuellement connectés (en mémoire)
└── exception/                            # UsernameAlreadyInUseException, StompExceptionHandler, ChatErrorPayload

src/main/resources/static/
├── index.html   # Popup de connexion + interface de chat
├── css/
│   └── styles.css  # Thème clair/sombre, bulles de messages, responsive, animations
└── js/
    └── main.js     # Connexion STOMP/SockJS, thème, historique, gestion des erreurs, envoi/réception
```

## Inspecter les données persistées (H2 Console)

En développement, la console H2 est activée : `http://localhost:8080/h2-console`

- JDBC URL : `jdbc:h2:file:./data/chatdb`
- User : `sa` — mot de passe : *(vide)*

## Tester avec plusieurs utilisateurs

Ouvrir plusieurs onglets/navigateurs sur `http://localhost:8080`, choisir un pseudo différent dans chacun, et échanger des messages : chaque client voit ses propres messages à droite et ceux des autres à gauche, avec les notifications d'arrivée/départ centrées. Un nouvel arrivant reçoit automatiquement l'historique de la conversation. Essayer de rejoindre avec un pseudo déjà utilisé par un autre onglet déclenche l'erreur `USERNAME_TAKEN` dans le popup.
