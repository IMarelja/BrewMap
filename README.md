# BrewMap ☕

BrewMap the service that lets users review not just locations but also their drinks for a more informative experience

## Technology stack

| Component | Main technologies / languages |
| --- | --- |
| Database | MongoDB, with JavaScript init and seed scripts |
| Web API | ASP.NET Core 8.0 REST API written in C# |
| Web app (frontend) | Next.js and React written in TypeScript/TSX, with CSS/Tailwind styling |
| Mobile app | Android app written in Kotlin, with XML layouts and Gradle Kotlin DSL |

## Infrastructure

The project uses Docker Compose for infrastructure. It currently starts the MongoDB database in a container, keeps database data in Docker volumes, and runs the initialization scripts from `init-db` when the database is created.

## How to install

Check technical documentation of the BrewMap project
