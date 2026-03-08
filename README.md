# Spring Demo

After working on [huangsam/kotlin-trial](https://github.com/huangsam/kotlin-trial),
I wanted to try out [Spring in Kotlin](https://docs.spring.io/spring-framework/reference/languages/kotlin.html).
This project evolved from a simple Spring Boot application into a feature-rich blog platform demonstrating data persistence, security, web development, and testing.

## Running the Application

- `./gradlew bootRun` – Start the application
- `./gradlew test` – Run the test suite

## Spring Capabilities Demonstrated

**Data & Persistence**
- Spring Data JPA with entity relationships and complex queries
- Query optimization using EntityGraph to prevent N+1 problems
- H2 in-memory database with seeded data

**Security**
- Spring Security for authentication and authorization
- BCrypt password encryption for user credentials
- Role-based access control for comments and articles

**Web Layer**
- REST and HTML controllers with content negotiation
- Mustache templating for server-side rendering
- Pagination and filtering support
- Rate limiting for API protection

**Features**
- Multi-user article authoring with markdown support
- Article taxonomies and related articles
- Admin dashboard
- RSS feeds
- Search and monitoring

**Testing & Observability**
- Integration testing with RestTestClient
- Test fixtures and comprehensive test coverage
- Logging throughout the application

## Resources

- [Spring Boot Reference Documentation](https://docs.spring.io/spring-boot/docs/current/reference/)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- [Spring Security](https://spring.io/projects/spring-security)
- [Spring Boot Gradle Plugin](https://docs.spring.io/spring-boot/docs/current/gradle-plugin/)
- [Mustache.js](https://github.com/janl/mustache.js)
- [Gradle Build Scans](https://scans.gradle.com#gradle)
