# 📚 Biblioteka – aplikacja webowa

Projekt **Biblioteka** to aplikacja webowa typu **full-stack**, służąca do zarządzania biblioteką.
System umożliwia obsługę książek, czytelników oraz wypożyczeń.

---

## 🛠️ Technologie

**Backend:**
- Java
- Spring Boot
- Spring Data JPA
- PostgreSQL
- Maven

**Frontend:**
- React
- TypeScript
- Vite
- Tailwind CSS

**Inne:**
- Docker
- Docker Compose
- Nginx

---

## ⚙️ Funkcjonalności

- zarządzanie książkami
- zarządzanie czytelnikami
- obsługa wypożyczeń i zwrotów
- komunikacja frontend ↔ backend przez REST API

---

## 🚀 Uruchomienie

### Docker (zalecane)

```bash
docker compose up --build
```

Po uruchomieniu aplikacja będzie dostępna pod adresami:

Frontend: http://localhost

Backend API: http://localhost:8080

### Lokalnie (bez Dockera)
**Backend**
```bash
cd backend
mvn clean package
java -jar target/biblioteka-0.0.1-SNAPSHOT.jar
```

Backend uruchomi się pod adresem:
http://localhost:8080

**Frontend**

```bash
cd frontend
npm install
npm run dev
```


Frontend będzie dostępny pod adresem:

http://localhost:5173

---

## 👥 Contributors

- **xWolfQ**
- **oONeXuSOo**

---