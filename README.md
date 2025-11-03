# Journal System - Patient Information Management

Ett fullstack journalsystem för att hantera patientinformation med Spring Boot backend, React frontend och PostgreSQL databas.

## Teknisk Stack

- **Backend**: Spring Boot 3.2.1, Hibernate, Spring Security, JWT
- **Frontend**: React 18, React Router, Axios
- **Databas**: PostgreSQL 15
- **Containerisering**: Docker & Docker Compose

## Funktionalitet

### Användarroller
- **Patient**: Kan se sin egen information, diagnoser, observationer och skicka meddelanden
- **Läkare**: Kan se alla patienter, skapa noter, fastställa diagnoser och svara på meddelanden
- **Övrig Personal**: Kan skapa noter och observationer för patienter

### Huvudfunktioner
1. Inloggning och registrering med tre olika användartyper
2. Patienthantering
3. Vårdtillfällen (Encounters)
4. Observationer
5. Diagnoser (Conditions)
6. Meddelandesystem mellan patienter och vårdpersonal

## Entiteter

- **User**: Användarinformation och autentisering
- **Patient**: Patientspecifik information
- **Practitioner**: Vårdpersonal
- **Organization**: Organisationer
- **Location**: Platser
- **Encounter**: Vårdtillfällen
- **Observation**: Medicinska observationer
- **Condition**: Diagnoser och tillstånd
- **Message**: Meddelanden mellan användare

## Kom igång

### Förutsättningar
- Docker & Docker Compose installerat
- Port 3000, 8080 och 5432 måste vara tillgängliga

### Starta systemet

1. Klona projektet eller navigera till projektmappen

2. Bygg och starta alla containrar:
```bash
docker-compose up --build
```

3. Systemet kommer att vara tillgängligt på:
   - Frontend: http://localhost:3000
   - Backend API: http://localhost:8080/api
   - Database: localhost:5432

### Stoppa systemet

```bash
docker-compose down
```

För att även ta bort volymer (databas):
```bash
docker-compose down -v
```

## API Endpoints

### Autentisering
- `POST /api/auth/login` - Logga in
- `POST /api/auth/register` - Registrera ny användare

### Patienter
- `GET /api/patients` - Hämta alla patienter
- `GET /api/patients/{id}` - Hämta specifik patient
- `GET /api/patients/user/{userId}` - Hämta patient baserat på user ID
- `PUT /api/patients/{id}` - Uppdatera patient
- `DELETE /api/patients/{id}` - Ta bort patient

### Vårdtillfällen
- `GET /api/encounters/patient/{patientId}` - Hämta patientens vårdtillfällen
- `POST /api/encounters` - Skapa nytt vårdtillfälle
- `PUT /api/encounters/{id}` - Uppdatera vårdtillfälle

### Observationer
- `GET /api/observations/patient/{patientId}` - Hämta patientens observationer
- `POST /api/observations` - Skapa ny observation
- `PUT /api/observations/{id}` - Uppdatera observation

### Diagnoser
- `GET /api/conditions/patient/{patientId}` - Hämta patientens diagnoser
- `POST /api/conditions` - Skapa ny diagnos
- `PUT /api/conditions/{id}` - Uppdatera diagnos

### Meddelanden
- `GET /api/messages/user/{userId}` - Hämta användarens meddelanden
- `GET /api/messages/received/{userId}` - Hämta mottagna meddelanden
- `GET /api/messages/sent/{userId}` - Hämta skickade meddelanden
- `GET /api/messages/unread/{userId}` - Hämta olästa meddelanden
- `POST /api/messages` - Skapa nytt meddelande
- `PUT /api/messages/{id}/read` - Markera meddelande som läst

## Testanvändare

Efter första uppstarten kan du registrera testanvändare via UI:t eller använda API:t.

Exempel registrering:
- **Patient**: Användarnamn, lösenord, personnummer, födelsedatum
- **Läkare**: Användarnamn, lösenord, specialisering, licensnummer
- **Personal**: Användarnamn, lösenord, specialisering

## Utveckling

### Backend
Backend körs på Spring Boot med automatisk databas-schema generering via Hibernate.

Huvudfiler:
- `backend/src/main/java/com/journalsystem/`
  - `model/` - Entitetsklasser
  - `repository/` - Data access layer
  - `service/` - Business logic
  - `controller/` - REST endpoints
  - `security/` - JWT och autentisering
  - `config/` - Konfiguration

### Frontend
React applikation med React Router för routing och Axios för API-anrop.

Huvudfiler:
- `frontend/src/`
  - `components/` - Återanvändbara komponenter
  - `pages/` - Sidkomponenter (dashboards)
  - `services/` - API service layer
  - `contexts/` - React contexts (Auth)
  - `styles/` - CSS styling

## Framtida Förbättringar

För högre betyg kan systemet integreras med HAPI FHIR:
- HAPI FHIR endpoint: https://hapi-fhir.app.cloud.cbh.kth.se/fhir
- Ersätt lokala patient-relaterade entiteter med FHIR resurser

## Licens

Detta är ett utbildningsprojekt för laboration i kursen.
