# ⚠️ Warning

This README.md (for now) is just purely for something to be put in this repo including the diagram that will be used for later in development

The instructions bellow are just stuff I vibe coded on the side of the project and are generally how the project will look

# BrewMap

Very simple Docker Compose setup based on the provided diagram:

- `nginx` reverse proxy
- ASP.NET Core MVC `webapp`
- ASP.NET Core REST `api`
- `mongodb`

The Mobile app from the diagram is intentionally ignored.

## Architecture

- Client calls `nginx` on `http://example.org`
- `nginx` forwards:
  - `/` to the MVC webapp
  - `/api` to the REST API
- The REST API stores and reads words from MongoDB
- The MVC app loads words from the API and submits new words to the API

