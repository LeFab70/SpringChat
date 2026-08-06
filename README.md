# Spring WebSocket Chat

Application de chat en temps réel construite avec Spring Boot (WebSocket + STOMP) côté serveur et une interface front-end moderne (HTML/CSS/JS vanilla) côté client, dans le style d'un client de messagerie (Discord/Messenger).

## Fonctionnalités

**Backend (Spring Boot)**
- WebSocket + STOMP via SockJS, endpoint `/ws`
- Broker simple sur `/topic/public`
- Envoi de message : `/app/chat.sendMessage`
- Arrivée d'un utilisateur : `/app/chat.addUser`
- Diffusion automatique d'un message `LEAVE` quand une session WebSocket se ferme (déconnexion propre ou fermeture d'onglet), via `WebSocketEventListener`

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

Aucune donnée n'est persistée côté serveur : l'historique des messages n'est pas rejoué à la connexion, seuls les messages envoyés après la connexion d'un client lui sont visibles.

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

## Structure du frontend

```
src/main/resources/static/
├── index.html   # Popup de connexion + interface de chat
├── css/
│   └── styles.css  # Thème clair/sombre, bulles de messages, responsive, animations
└── js/
    └── main.js     # Connexion STOMP/SockJS, thème, envoi/réception, déconnexion
```

## Tester avec plusieurs utilisateurs

Ouvrir plusieurs onglets/navigateurs sur `http://localhost:8080`, choisir un pseudo différent dans chacun, et échanger des messages : chaque client voit ses propres messages à droite et ceux des autres à gauche, avec les notifications d'arrivée/départ centrées.
