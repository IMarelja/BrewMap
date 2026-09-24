# BrewMap ☕

BrewMap the service that lets users review not just locations but also their drinks for a more informative experience

## Technology stack

| Component | Main technologies / languages |
| --- | --- |
| Database | MongoDB, with JavaScript init and seed scripts |
| Web API | ASP.NET Core 8.0 REST API written in C# |
| Web app (frontend) | Next.js and React written in TypeScript/TSX, with CSS/Tailwind styling |
| Mobile app | Android app written in Kotlin, with XML layouts and Gradle Kotlin DSL |

## Project structure

```
BrewMap/
├── src/
│   ├── BrewMapAPI/     # ASP.NET Core API + endpoint tests (BrewMapAPI.sln)
│   ├── Frontend/       # Next.js web app
│   └── MobileApp/      # Android app
├── init-db/            # MongoDB init & seed scripts
├── docker-compose.yaml
└── .env.example
```

## Infrastructure

The project uses Docker Compose for infrastructure. It currently starts the MongoDB database in a container, keeps database data in Docker volumes, and runs the initialization scripts from `init-db` when the database is created.

## How to install

~~Check technical documentation of the BrewMap project~~
The full details are in `BrewMap-TechnicalDocumentation.pdf`. The steps below are a quick-start version.

### Prerequisites

| Component | Requirement |
| --- | --- |
| Database | Docker with Docker Compose v2 |
| API | .NET 8.0 SDK |
| Web app | Node.js with npm |
| Mobile app | Android Studio (JDK 11+, Android SDK 36, minimum device API 26) |

### 1. Environment file (`.env`)

The database, API and mobile app all read the same `.env` file in the repository root. Create it from the example:

```bash
cp .env.example .env
```

| Variable | Used by | Description |
| --- | --- | --- |
| `MONGO_ROOT_USER` | Database | Root user created on first start (default `admin`) |
| `MONGO_ROOT_PASSWORD` | Database | Password of the root user. **Change it.** |
| `MONGO_PORT` | Database | Port MongoDB is exposed on (default `27017`) |
| `BIND_IP` | API | IP the API binds to: `127.0.0.1` for local-only access, `0.0.0.0` to allow other devices (e.g. a phone) |
| `HTTP_PORT` | API | HTTP port of the API (default `5239`) |
| `HTTPS_PORT` | API | HTTPS port of the API (default `7000`) |
| `APP_MODE` | Mobile app | `HARD_CODE`, `API` or `PERSISTENT` (see [Mobile app](#4-mobile-app)) |
| `BIND_API_URL` | Mobile app | URL the app uses to reach the API, e.g. `http://10.0.2.2:5239/api` |

### 2. Database and API

Start MongoDB from the repository root:

```bash
docker compose up -d
```

On the first start, this creates the `brewmap` database and runs the scripts in `init-db/`:
- `01-init.js` creates the collections and their schemas.
- `02-seed.js` loads sample data.

Use `docker compose down` to stop the database (the data is kept in a Docker volume). Use `docker compose down -v` to stop it and delete all data.

#### Configure the API

In `src/BrewMapAPI/BrewMapAPI/`, copy `appsettings.example.json` to `appsettings.json` or to `appsettings.Development.json`. If both files exist, `appsettings.Development.json` takes precedence when the API runs in the Development environment.

| Setting | Description |
| --- | --- |
| `DatabaseSettings:ConnectionString` | MongoDB connection URL. Its user, password and port must match `MONGO_ROOT_USER`, `MONGO_ROOT_PASSWORD` and `MONGO_PORT` from `.env`, e.g. `mongodb://admin:changeme@localhost:27017/?authSource=admin` |
| `DatabaseSettings:DatabaseName` | Leave it as `brewmap` when using Docker Compose |
| `Jwt:SecureKey` | Secret used to sign login tokens. The example value is fine for development, but **it must be changed for production** |
| `Jwt:Issuer`, `Jwt:Audience` | Token issuer and audience. They can stay as they are |
| `EmailConfiguration` | SMTP account used to send emails (e.g. password resets): `From`, `SmtpServer`, `Port`, `Username` (same as `From`) and `Password`. For Gmail, keep `smtp.gmail.com` / `465` and use an [app password](https://support.google.com/accounts/answer/185833) |

#### Run the API

```bash
cd src/BrewMapAPI/BrewMapAPI
dotnet run
```

The API refuses to start if `BIND_IP`, `HTTP_PORT` or `HTTPS_PORT` is missing from `.env`. It listens on `http://<BIND_IP>:<HTTP_PORT>`, and Swagger UI is available at `/swagger`, e.g. http://localhost:5239/swagger.

You can also open `src/BrewMapAPI/BrewMapAPI.sln` in Visual Studio or Rider. In VS Code, use the **BrewMap API** launch configuration.

To run the endpoint tests, copy `src/BrewMapAPI/BrewMapEndpointUnitTest/appsettings.example.json` to `appsettings.json` in the same folder and fill in the test data. Then, with the API running, run `dotnet test src/BrewMapAPI/BrewMapAPI.sln`.

### 3. Web app

The web app is built with Next.js 14 (App Router), TypeScript and Tailwind CSS. It reads the API URL from `NEXT_PUBLIC_API_URL` in `src/Frontend/.env.local`:

```bash
NEXT_PUBLIC_API_URL=http://localhost:5239
```

Set it to the base URL of your API, without `/api` at the end.

```bash
cd src/Frontend
npm install
npm run dev        # development server with hot reload
```

The app runs at http://localhost:3000.

For a production build:

```bash
npm run build      # compile and optimize
npm run start      # serve the build on http://localhost:3000
```

### 4. Mobile app

The mobile app is a native Android app written in Kotlin. It talks to the BrewMap API and shows locations on an OpenStreetMap map. Its Gradle build reads `APP_MODE` and `BIND_API_URL` from the root `.env` file.

`APP_MODE` controls where the app gets its data:
- **`HARD_CODE`**: offline. All data is built into the app.
- **`API`**: online only. Every request goes to the API.
- **`PERSISTENT`**: online with offline support. API responses are cached on the device for a while.

`BIND_API_URL` depends on where the app runs:
- **Android emulator**: `http://10.0.2.2:5239/api` (`10.0.2.2` is the emulator's address for the host machine).
- **Physical device on the same Wi-Fi**: `http://<your computer's LAN IP>:5239/api`. Also set `BIND_IP=0.0.0.0` in `.env` so the API accepts connections from other devices.
- **Public server**: `https://<your domain>/api`.

Use HTTP for development unless the device trusts your API's certificate.

#### Build and run

1. In Android Studio, choose **File → Open**, select `src/MobileApp`, and click **OK**.
2. Wait for the Gradle sync to finish. If `.env` is missing, or `APP_MODE` or `BIND_API_URL` is missing or invalid, Gradle stops with an error in the **Build** window that explains what to fix.
3. Connect a device with USB debugging enabled, or start an emulator from **Device Manager**.
4. Select the device in the toolbar and click **Run** (`Shift + F10`).

To build an APK without running it, choose **Build → Build Bundle(s) / APK(s) → Build APK(s)**. The debug APK is written to `src/MobileApp/app/build/outputs/apk/debug/app-debug.apk`.
