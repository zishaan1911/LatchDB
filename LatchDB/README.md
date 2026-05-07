# LatchDB

LatchDB is a lightweight database system featuring a custom B+ Tree storage engine, SQL query execution with an iterator model, and a React-based analytics dashboard.

## Project Structure

- `backend/`: Java-based database engine (Spring Boot, Maven).
- `frontend/`: React-based dashboard (Vite, TypeScript).

## Getting Started

### Backend

1. Navigate to `backend/`.
2. Ensure you have JDK 17+ installed.
3. Build the project:
   ```bash
   mvn clean compile
   ```
4. Run the application:
   ```bash
   mvn spring-boot:run
   ```

### Frontend

1. Navigate to `frontend/`.
2. Install dependencies:
   ```bash
   npm install
   ```
3. Start the development server:
   ```bash
   npm run dev
   ```

## Architecture

- **Clean Architecture**: Separation between storage, schema, index, query, and analytics.
- **Iterator Model**: Query operations are implemented as `Iterator` subclasses.
- **Clustered Indexing**: Data is stored in leaf nodes and indexed by primary key.
