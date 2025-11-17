Project Documentation: Account Service
Version: 1.0 Date: 2024-11-11

1. Project Overview
The Account Service is a robust Spring Boot application designed to function as the core backend for a modern banking system. It provides a comprehensive suite of banking functionalities through a RESTful API, including customer and account management, fund transfers, card services, and transaction logging.

The project is built with a focus on maintainability, scalability, and security, following industry-best practices for software architecture and deployment. It is designed to be deployed as a cloud-native application on the Render platform.

2. Core Architecture: The Layered Approach
The application follows a classic Layered Architecture, which separates concerns into distinct logical layers. This design makes the application easier to understand, test, and maintain.

API Layer (Controllers): The entry point for all external communication. It handles incoming HTTP requests, validates input, and delegates tasks to the Service Layer.
Business Logic Layer (Services): The "brain" of the application. It contains the core business rules and orchestrates operations by coordinating with the Data Access Layer.
Data Access Layer (Repositories): The component responsible for all communication with the database. It abstracts away the complexities of database interaction.
Data Model (Entities): The foundation of the application, representing the core data structures and their relationships, which map directly to database tables.
3. Key Components Explained
3.1. Entities (The Data Model)
Location: com.bank.account.entity
Description: Plain Old Java Objects (POJOs) that use JPA (Java Persistence API) annotations (@Entity, @Id, @ManyToOne, etc.) to map to tables in the PostgreSQL database.
Key Classes: Customer, Account, Card, Transaction.
3.2. Repositories (The Data Access Layer)
Location: com.bank.account.repository
Description: These are Java interfaces that extend Spring Data JPA's JpaRepository. They provide a complete set of CRUD (Create, Read, Update, Delete) operations out-of-the-box and allow for the easy creation of custom database queries.
Example: AccountRepository provides methods like save(), findById(), and custom-defined ones like findByAccountNumber().
3.3. Services (The Business Logic Layer)
Location: com.bank.account.service
Description: This layer contains the core business logic. It is responsible for orchestrating data from repositories to fulfill business requirements.
Example: The fundTransfer method in AccountService would use the AccountRepository to fetch accounts, validate the transfer (e.g., check for sufficient funds), update the account balances, and save them back to the database.
3.4. Controllers (The API Layer)
Location: com.bank.account.controller
Description: These classes define the public REST API for the application using Spring Web annotations (@RestController, @PostMapping, @GetMapping, etc.). They handle HTTP requests, deserialize JSON request bodies into DTOs, call service methods, and serialize results back into JSON responses.
3.5. DTOs (Data Transfer Objects)
Location: com.bank.account.dto
Description: DTOs are used to define the "shape" of data sent to and from the API. They decouple the API contract from the internal database structure (Entities) and are used for input validation with annotations like @NotBlank and @Min.
4. Configuration Strategy
The project utilizes Spring Profiles to manage environment-specific configurations, ensuring a clear separation between development and production settings.

application.properties (Default/Development):

Configured for local development.
Points to a local PostgreSQL database.
Enables detailed SQL logging (spring.jpa.show-sql=true) for easier debugging.
Uses spring.jpa.hibernate.ddl-auto=update for convenience, allowing Hibernate to automatically adjust the database schema.
application-prod.properties (Production):

Activated by the SPRING_PROFILES_ACTIVE=prod environment variable.
Uses placeholders (${DB_URL}, ${DB_USERNAME}, etc.) that are securely filled by Render's environment variables.
Uses the safer spring.jpa.hibernate.ddl-auto=validate setting to prevent accidental data loss in production.
Disables verbose SQL logging for better performance.
5. The End-to-End Project Workflow
The project follows a modern CI/CD (Continuous Integration/Continuous Deployment) workflow, automating the path from local code changes to a live application on the internet.

Local Development: A developer writes and tests code on their local machine, using the default application.properties to connect to a local database.
Version Control: Changes are committed to a Git repository and pushed to GitHub. No secrets or credentials are ever committed to the repository.
Deployment Trigger: Render is connected to the GitHub repository. A git push to the main branch automatically triggers a new deployment.
Build Phase (on Render):
Render spins up a build environment based on the specified Java version (JAVA_VERSION: 21).
It checks out the code and runs the buildCommand: "./mvnw clean install".
Maven compiles the code and packages the application into an executable .jar file.
Deploy Phase (on Render):
Render starts a new web service instance.
It reads the render.yaml file and injects the necessary environment variables, including the SPRING_PROFILES_ACTIVE=prod flag and the secure database credentials fetched via the fromDatabase directive.
It runs the startCommand: "java -jar ..." to launch the application.
Application Startup: The Spring Boot application starts, activates the prod profile, reads the production properties, and connects to the production PostgreSQL database using the credentials provided by Render.
Live: The application is now live and serving requests on its public onrender.com URL.
6. Key Project Accomplishments
Infrastructure as Code (IaC): The entire deployment process is defined in render.yaml. This provides a single source of truth for the application's infrastructure, making deployments automated, repeatable, and consistent.
Secure Configuration Management: A clear and secure strategy for managing secrets has been established. Sensitive data is kept out of the Git repository and is securely managed by Render's environment variables for production and a local .env file (ignored by Git) for development.
Profile-Based Configuration: The use of Spring Profiles (dev vs. prod) allows for tailored application behavior and settings suitable for each environment.
Containerization-Ready: The inclusion of a .dockerignore file and a standard Maven build process makes the application easy to containerize with Docker if needed in the future.
Modular Authentication: The Keycloak security integration was successfully implemented and then cleanly disabled by commenting out the relevant dependencies, configuration, and code. This demonstrates a modular design where features can be toggled on or off without breaking the core application.
