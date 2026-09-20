# Autogarage Beheersysteem - Backend

Github link: https://github.com/Viggoro/CargarageNoviAugust

Een uitgebreid backendsysteem voor het beheren van een autogarage, inclusief klantbeheer, reparaties, keuringen, voorraad en personeelsbeheer.

## Inhoudsopgave

- [Vereisten](#vereisten)
- [Technologie Stack](#technologie-stack)
- [Database Installatie](#database-installatie)
- [Installatiehandleiding](#installatiehandleiding)
- [De Applicatie Uitvoeren](#de-applicatie-uitvoeren)
- [API Documentatie](#api-documentatie)
- [Testen](#testen)
- [Projectstructuur](#projectstructuur)
- [Configuratie](#configuratie)
- [Functionaliteiten](#functionaliteiten)
- [Probleemoplossing](#probleemoplossing)

## Vereisten

Voordat je begint, zorg dat je het volgende hebt geïnstalleerd op je computer:

1. **Java 17 of hoger**
   - Download van: https://www.oracle.com/java/technologies/downloads/
   - Controleer de installatie: open de opdrachtprompt/terminal en typ `java -version`

2. **PostgreSQL Database**
   - Download van: https://www.postgresql.org/download/
   - Zorg dat je pgAdmin 4 installeert (wordt meestal meegeleverd met PostgreSQL)
   - Onthoud het wachtwoord dat je hebt ingesteld voor de gebruiker `postgres` tijdens de installatie

5. **IDE** (Aanbevolen)
   - IntelliJ IDEA: https://www.jetbrains.com/idea/download/
   - Eclipse: https://www.eclipse.org/downloads/
   - VS Code met Java-extensies: https://code.visualstudio.com/

## Technologie Stack

- **Framework**: Spring Boot 3.2.1
- **Taal**: Java 17
- **Database**: PostgreSQL
- **Beveiliging**: Spring Security met JWT
- **ORM**: Spring Data JPA met Hibernate
- **API Documentatie**: SpringDoc OpenAPI (Swagger)
- **PDF Generatie**: iText
- **Build Tool**: Maven
- **Testen**: JUnit 5, Spring Boot Test

## Database Installatie

Je hoeft geen SQL-bestand te importeren. Maak alleen een lege PostgreSQL-database aan. Bij het starten van de applicatie gebeurt het volgende automatisch:

1. **Hibernate** (`spring.jpa.hibernate.ddl-auto=update`) maakt of werkt de tabellen bij op basis van de entiteiten
2. **`DataInitializer`** vult de database met voorbeelddata en testgebruikers als er nog geen medewerkers bestaan

### Belangrijke Opmerking Over Wachtwoord

Deze applicatie is geconfigureerd om **`root`** als PostgreSQL-wachtwoord te gebruiken. Je hebt twee opties:

1. **Aanbevolen voor beginners**: Stel je PostgreSQL-wachtwoord in op `root` tijdens de installatie
2. **Alternatief**: Gebruik een willekeurig wachtwoord, maar vergeet niet dit later bij te werken in `application.properties` (zie sectie [Configuratie](#configuratie))

### Stap 1: Database Aanmaken in pgAdmin 4

1. **Open pgAdmin 4**
   - Start pgAdmin 4 vanuit je applicaties

2. **Verbinden met PostgreSQL Server**
   - Klik op "Servers" in het linkerpaneel
   - Klik met de rechtermuisknop op "PostgreSQL" en selecteer "Connect Server"
   - Voer je wachtwoord in (het wachtwoord dat je hebt ingesteld tijdens de PostgreSQL-installatie)
   - Als je de bovenstaande aanbeveling hebt gevolgd, is dit `root`

3. **Nieuwe Database Aanmaken**
   - Klik met de rechtermuisknop op "Databases"
   - Selecteer "Create" > "Database..."
   - Voer in het veld "Database" in: `garage_db`
   - Klik op "Save"

Je kunt de database ook via `psql` aanmaken:

```sql
CREATE DATABASE garage_db;
```

### Stap 2: Applicatie Starten

Na het aanmaken van `garage_db` en het configureren van de databaseverbinding (zie [Installatiehandleiding](#installatiehandleiding)) start je de applicatie. Tabellen en voorbeelddata worden dan automatisch aangemaakt.

Controleer in de console op berichten zoals:
```
Initializing garage database with sample data...
Database initialization completed successfully.
```

Als er al medewerkers in de database staan, wordt de initialisatie overgeslagen.

### Database Resetten

Om opnieuw te beginnen met schone voorbeelddata:

1. Stop de applicatie
2. Drop en maak de database opnieuw aan:
   ```sql
   DROP DATABASE IF EXISTS garage_db;
   CREATE DATABASE garage_db;
   ```
3. Start de applicatie opnieuw

## Installatiehandleiding

### Stap 1: Het Project Downloaden/Klonen

Als je de projectmap al hebt:
- Navigeer naar de projectmap in je terminal/opdrachtprompt

Als je Git gebruikt:
```bash
git clone https://github.com/Viggoro/CargarageNoviAugust
cd garage-backend
```

### Stap 2: Databaseverbinding Configureren

1. **Open het configuratiebestand**
   - Navigeer naar: `src/main/resources/application.properties`

2. **Werk de databasegegevens bij indien nodig**
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/garage_db
   spring.datasource.username=postgres
   spring.datasource.password=root
   ```
   
   Wijzig `root` naar je eigen PostgreSQL-wachtwoord

3. **Sla het bestand op**

## De Applicatie Uitvoeren

### Optie 1: Maven Wrapper Gebruiken (Aanbevolen)

**Windows:**
```cmd
mvnw.cmd spring-boot:run
```

**Windows Powershell (IntelliJ Terminal):**
```powershell
.\mvnw.cmd spring-boot:run
```

**Mac/Linux:**
```bash
./mvnw spring-boot:run
```

### Optie 2: Met een IDE

**IntelliJ IDEA:**
1. Open het project in IntelliJ
2. Wacht tot Maven de dependencies heeft gedownload
3. Zoek `GarageApplication.java` in `src/main/java/com/garage/`
4. Klik er met de rechtermuisknop op en selecteer "Run 'GarageApplication'"

**Eclipse:**
1. Importeer het project als een Maven-project
2. Klik met de rechtermuisknop op het project
3. Selecteer "Run As" > "Spring Boot App"

### Optie 3: Met Geïnstalleerde Maven

```powershell
.\mvnw.cmd spring-boot:run
```

### Controleren of de Applicatie Draait

Zodra de applicatie is gestart, zou je een uitvoer moeten zien die eindigt met:
```
Started GarageApplication in X.XXX seconds
```

De applicatie draait op: `http://localhost:8080`

## API Documentatie

### Swagger UI

Zodra de applicatie draait, heb je toegang tot de interactieve API-documentatie:

**URL:** http://localhost:8080/swagger-ui.html

Dit biedt:
- Een lijst van alle beschikbare endpoints
- Voorbeelden van verzoeken/antwoorden
- De mogelijkheid om API's rechtstreeks vanuit de browser te testen

### API Docs (JSON)

**URL:** http://localhost:8080/api-docs

## Testen

### Alle Tests Uitvoeren

**Met Maven Wrapper (Windows):**
```cmd
mvnw.cmd test
```

**Met Maven Wrapper (Windows Powershell / IntelliJ Terminal)):**
```powershell
.\mvnw.cmd test
```

**Met Maven Wrapper (Mac/Linux):**
```bash
./mvnw test
```

### Tests Uitvoeren in de IDE

**IntelliJ IDEA:**
- Klik met de rechtermuisknop op de map `src/test/java`
- Selecteer "Run 'All Tests'"

**Eclipse:**
- Klik met de rechtermuisknop op het project
- Selecteer "Run As" > "JUnit Test"

## Projectstructuur

```
garage-backend/
├── src/
│   ├── main/
│   │   ├── java/com/garage/
│   │   │   ├── config/           # Configuratieklassen
│   │   │   ├── controller/       # REST Controllers
│   │   │   ├── exception/        # Foutafhandeling
│   │   │   ├── model/            # Entiteitklassen
│   │   │   ├── repository/       # Database repositories
│   │   │   ├── service/          # Bedrijfslogica
│   │   │   └── GarageApplication.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/                     # Testklassen
├── pom.xml                       # Maven-configuratie
├── mvnw                          # Maven wrapper (Unix)
├── mvnw.cmd                      # Maven wrapper (Windows)
└── README.md                     # Dit bestand
```

## Configuratie

### Applicatie-eigenschappen

Belangrijke configuraties in `application.properties`:

```properties
# Server Poort
server.port=8080

# Database Configuratie
spring.datasource.url=jdbc:postgresql://localhost:5432/garage_db
spring.datasource.username=postgres
spring.datasource.password=root
spring.jpa.hibernate.ddl-auto=update

# JWT Configuratie
jwt.secret=mySecretKeyForJWTTokenGenerationThatIsAtLeast256BitsLong123456789
jwt.expiration=86400000

# Uploadlimieten voor Bestanden
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
```

## Functionaliteiten

### Kernmodules

1. **Personeelsbeheer**
   - Rollen: Admin, Monteur, Kassier, Back Office
   - Authenticatie met JWT
   - Rolgebaseerde toegangscontrole

2. **Klantbeheer**
   - Klantregistratie en profielbeheer
   - Klanthistorie bijhouden

3. **Autobeheer**
   - Voertuigregistratie
   - Eigenaarregistratie
   - Onderhoudshistorie

4. **Reparatiesysteem**
   - Reparatieopdrachten
   - Aangepaste reparatieacties
   - Onderdelenbeheer
   - Statusbewaking

5. **Keuringssysteem**
   - Voertuigkeuringen
   - Goedkeurings-/afkeuringsworkflow
   - Keuringsrapporten

6. **Voorraadbeheer**
   - Onderdelenvoorraad bijhouden
   - Meldingen bij lage voorraad

7. **Betalingen & Bonnen**
   - Betalingsverwerking
   - PDF-bonnengeneratie

8. **Documentbeheer**
   - Bestandsuploads
   - Documentopslag

9. **Auditlogging**
   - Alle systeemactiviteiten bijhouden
   - Geschiedenis van gebruikersacties

## Probleemoplossing

### Veelvoorkomende Problemen

#### 1. Databaseverbinding Mislukt

**Fout:** `Connection refused` of `Authentication failed`

**Oplossingen:**
- Controleer of PostgreSQL draait
- Controleer de gebruikersnaam/wachtwoord in `application.properties`
- Zorg dat de database `garage_db` bestaat
- Controleer of PostgreSQL luistert op poort 5432

#### 2. Poort Al in Gebruik

**Fout:** `Port 8080 is already in use`

**Oplossingen:**
- Wijzig de poort in `application.properties`: `server.port=8081`
- Of stop de applicatie die poort 8080 gebruikt

#### 3. Maven Wrapper Werkt Niet

**Fout:** Toegang geweigerd of commando niet gevonden

**Windows:**
```cmd
mvnw.cmd clean install
```

**Windows Powershell (IntelliJ Terminal):**
```powershell
mvnw.cmd clean install
```

**Mac/Linux:**
```bash
chmod +x mvnw
./mvnw clean install
```

#### 4. Java-versieproblemen

**Fout:** `Unsupported class file major version`

**Oplossing:**
- Zorg dat Java 17 of hoger is geïnstalleerd
- Stel de omgevingsvariabele JAVA_HOME in
- Controleer met: `java -version`

#### 5. Dependencies Worden Niet Gedownload

**Oplossing:**
```bash
mvnw.cmd clean install -U
```

#### 6. Applicatie Start Maar Geen Databasetabellen of Voorbeelddata

**Oplossing:**
- Controleer of de database `garage_db` bestaat en bereikbaar is
- Controleer of `spring.jpa.hibernate.ddl-auto=update` in `application.properties` staat
- Herstart de applicatie en controleer de logs op initialisatieberichten
- Als er al oude data staat, reset de database (zie [Database Resetten](#database-resetten))

### Hulp Krijgen

1. Controleer de applicatielogs in de console
2. Controleer pgAdmin 4 voor databaseproblemen
3. Controleer of alle vereisten correct zijn geïnstalleerd
4. Bekijk de foutmeldingen zorgvuldig

## Beveiligingsopmerkingen

### Standaard Testgebruikers

Bij een lege database maakt `DataInitializer` bij het opstarten de volgende testgebruikers aan:

| Gebruikersnaam | Wachtwoord | Rol |
|----------------|------------|-----|
| admin | admin123 | ADMIN |
| backoffice.jane | jane123 | BACK_OFFICE |
| mechanic.john | john123 | MECHANIC |
| mechanic.mike | mike123 | MECHANIC |
| cashier.sarah | sarah123 | CASHIER |
| cashier.lisa | lisa123 | CASHIER |

**Opmerking:** Het wachtwoordformaat is de naam van de gebruiker (zonder rolvoorvoegsel) + "123". Alleen `ADMIN` kan medewerkers beheren (`/api/employees`).

### JWT Token

- Tokens verlopen na 24 uur (instelbaar in `application.properties`)
- Gebruik het endpoint `/api/auth/login` om een token te verkrijgen
- Voeg de token toe in de Authorization-header: `Bearer <token>`

### Inlogvoorbeeld

Om in te loggen en een JWT-token te verkrijgen, stuur een POST-verzoek naar `/api/auth/login`:

```json
{
  "username": "admin",
  "password": "admin123"
}
```

Het antwoord bevat een JWT-token dat je kunt gebruiken voor geauthenticeerde verzoeken.

## Volgende Stappen

1. Maak de lege database `garage_db` aan
2. Configureer je databasewachtwoord in `application.properties`
3. Start de applicatie (tabellen en voorbeelddata worden automatisch aangemaakt)
4. Ga naar Swagger UI om de API's te verkennen
5. Test de authenticatie-endpoints met de testgebruikers
6. Begin met het bouwen van je frontend of testen met Postman

## Ondersteuning

Voor vragen of problemen:
- Bekijk de sectie Probleemoplossing
- Bekijk de Spring Boot documentatie: https://spring.io/projects/spring-boot
- Bekijk de PostgreSQL documentatie: https://www.postgresql.org/docs/
