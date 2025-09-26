# 📚 Library Management System v2
### A Comprehensive Spring Boot Application with Advanced Security Features

---

## 🎯 Project Overview

This is a **full-stack library management system** built with Spring Boot 3.4.5 and Java 21, featuring comprehensive security implementations including both **session-based** and **JWT token-based authentication**. The system demonstrates enterprise-level security practices and modern web application architecture.

### 🚀 Key Features

- 🔐 **Dual Authentication Systems** (Sessions + JWT)
- 👥 **Role-based Access Control** (USER & ADMIN roles)
- 🛡️ **Comprehensive Security** (CSRF protection, input validation, secure headers)
- 📖 **Complete Library Operations** (Books, Authors, Loans management)
- 🔄 **Token Refresh Mechanism** for extended sessions
- 📊 **Security Audit Logging** with detailed event tracking
- 🎨 **RESTful API Design** with proper HTTP status codes

---

## 🏗️ Architecture Overview

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Controllers   │    │    Services     │    │  Repositories   │
│                 │    │                 │    │                 │
│ • AuthController│───▶│ • UserService   │───▶│ • UserRepo      │
│ • AdminController│   │ • BookService   │    │ • BookRepo      │
│ • BookController│    │ • LoanService   │    │ • LoanRepo      │
│ • UserController│    │ • AuthorService │    │ • AuthorRepo    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│Security Filters │    │   DTOs & Val.   │    │    Database     │
│                 │    │                 │    │                 │
│ • JWT Filter    │    │ • Input Valid.  │    │ • SQLite        │
│ • CSRF Filter   │    │ • Bean Valid.   │    │ • JPA/Hibernate │
│ • Auth Handlers │    │ • Custom DTOs   │    │ • Entity Mgmt   │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

---

## 🛡️ Security Implementation

### 🔑 Authentication Systems

#### 1. **Session-Based Authentication** (Traditional Web)
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    // Form login with custom success/failure handlers
    // CSRF protection enabled
    // Session management with security logging
}
```

#### 2. **JWT Token Authentication** (API/Mobile)
```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    // Stateless authentication
    // Token validation on every request
    // Automatic user context setup
}
```

### 👤 User Management & Roles

| Role | Permissions | Endpoints |
|------|-------------|-----------|
| **ADMIN** 👑 | Full system access | `/admin/**`, `/users/**`, `/authors/**` |
| **USER** 👤 | Library operations | `/books/**`, `/loans/**` |
| **Anonymous** 🌐 | Public content | `/`, `/auth/**`, `/test/**` |

### 🔐 Password Security
- **BCrypt hashing** with salt
- **Password policy enforcement**:
    - Minimum 8 characters
    - Must contain letters AND numbers
    - Server-side validation with regex patterns

### 🛡️ Attack Protection

#### ✅ Implemented Protections:
- **SQL Injection**: JPA/Hibernate prepared statements
- **XSS Prevention**: Input validation + output escaping
- **CSRF Protection**: Token-based form protection
- **Session Fixation**: Spring Security automatic protection
- **Brute Force**: Security event logging (extensible to rate limiting)

---

## 📊 Database Schema

### 🗄️ Core Entities

```sql
👤 USERS (8 columns)
├── user_id (Primary Key)
├── first_name, last_name
├── email (Unique)
├── password (BCrypt hashed)
├── role (USER/ADMIN)
├── enabled (Account status)
└── registration_date

📚 BOOKS (6 columns)  
├── book_id (Primary Key)
├── title, publication_year
├── available_copies, total_copies
└── author_id (Foreign Key)

✍️ AUTHORS (5 columns)
├── author_id (Primary Key)
├── first_name, last_name
├── birth_year
└── nationality

📋 LOANS (6 columns)
├── loan_id (Primary Key)
├── user_id, book_id (Foreign Keys)  
├── borrowed_date, due_date
└── returned_date (nullable)

🔄 REFRESH_TOKENS (4 columns)
├── id (Primary Key)
├── token (Unique UUID)
├── expiry_date
└── user_id (Foreign Key)
```

---

## 🚦 API Endpoints

### 🔐 Authentication & Authorization
```http
POST /auth/login          # JWT login
POST /auth/register       # User registration  
POST /auth/refresh        # Token refresh
POST /auth/logout         # Secure logout
GET  /auth/me            # Current user info
```

### 👑 Admin Operations
```http
GET  /admin/dashboard     # System overview
GET  /admin/users        # All users list
GET  /admin/system       # System configuration
```

### 📚 Library Management
```http
GET    /books            # Browse all books
GET    /books/search     # Search by title/author
POST   /books            # Add new book (Admin only)

GET    /authors          # List all authors
GET    /authors/name/{lastName}  # Search by name
POST   /authors          # Add new author (Admin only)

POST   /loans            # Borrow a book
PUT    /loans/{id}/return    # Return a book
PUT    /loans/{id}/extend    # Extend loan period
GET    /{userId}/loans   # User's loan history
```

---

## 🔧 Technical Implementation Details

### 🏷️ Framework & Dependencies
```xml
<dependencies>
    <!-- Core Spring Boot -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
        <version>3.4.5</version>
    </dependency>
    
    <!-- Security -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    
    <!-- JWT Implementation -->
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>0.12.3</version>
    </dependency>
    
    <!-- Database -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    
    <!-- SQLite -->
    <dependency>
        <groupId>org.xerial</groupId>
        <artifactId>sqlite-jdbc</artifactId>
        <version>3.45.1.0</version>
    </dependency>
</dependencies>
```

### 🔄 JWT Token Management

#### Access Tokens (Short-lived: 15 minutes)
```java
public class JwtUtil {
    private final long JWT_EXPIRATION = 1000 * 60 * 15; // 15 minutes
    
    public String generateToken(UserDetails userDetails) {
        // Include user role in token payload
        // HS256 signing with secure secret key
    }
}
```

#### Refresh Tokens (Long-lived: 7 days)
```java
@Entity
public class RefreshToken {
    private String token;        // UUID-based
    private String expiryDate;   // 7 days from creation
    private User user;           // One token per user
}
```

### 🛠️ Service Layer Architecture

#### Core Services
- **🔐 UserDetailsServiceImpl**: Spring Security integration
- **👤 UserService**: User management with security logging
- **📚 BookService**: Library catalog operations
- **📋 LoanService**: Borrowing business logic
- **✍️ AuthorService**: Author management
- **🔄 RefreshTokenService**: Token lifecycle management
- **📊 SecurityLoggingService**: Comprehensive audit logging

### 📝 Data Transfer Objects (DTOs)

#### Input Validation Examples:
```java
public class CreateUserDTO {
    @NotBlank(message = "Förnamn får inte vara tomt")
    private String firstName;
    
    @Email(message = "Ogiltig e-postadress")  
    private String email;
    
    @Size(min = 8, message = "Lösenordet måste innehålla minst 8 tecken")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d).+$", 
             message = "Lösenordet måste innehålla både bokstäver och siffror")
    private String password;
}
```

### 🔍 Security Logging & Monitoring

#### Event Categories:
- ✅ **Authentication Events**: Login success/failure, logout
- 🔐 **Authorization Events**: Access attempts, privilege escalation
- 👤 **User Management**: Registration, profile changes
- 🛡️ **Security Incidents**: CSRF attempts, suspicious activities
- 📊 **Admin Activities**: Administrative access and operations

---

## 🧪 Testing & Validation

### 🔬 Built-in Test Data
```java
@Service
public class DataInitService {
    @PostConstruct  
    public void initData() {
        // Creates test users on startup:
        // user@test.com / password123 (USER role)
        // admin@test.com / admin123 (ADMIN role)
    }
}
```

### 📋 Manual Testing Endpoints
```http
GET /test                # Basic connectivity
GET /test/database       # Database connection test
GET /csrf-info          # CSRF token information
```

---

## 🚀 Getting Started

### 📋 Prerequisites
- ☕ **Java 21** or higher
- 📦 **Maven 3.6+**
- 🗄️ **SQLite** (included)

### 🛠️ Installation Steps

1. **Clone the repository**
   ```bash
   git clone [repository-url]
   cd library-management-v2
   ```

2. **Build the project**
   ```bash
   mvn clean install
   ```

3. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

4. **Access the application**
    - 🌐 **Main API**: `http://localhost:8080`
    - 📊 **Database**: `libraryWithRole.db` (auto-created)

### 🧪 Testing Authentication

#### Session-Based Login:
```bash
# Register new user
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"firstName":"John","lastName":"Doe","email":"john@test.com","password":"password123"}'

# Login and get JWT token  
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"john@test.com","password":"password123"}'
```

#### JWT API Access:
```bash
# Use JWT token for authenticated requests
curl -X GET http://localhost:8080/books \
  -H "Authorization: Bearer [YOUR_JWT_TOKEN]"
```

---

## 📖 Educational Value

### 🎓 Learning Objectives Achieved

This project demonstrates mastery of:

1. **🔐 Spring Security Fundamentals**
    - Authentication vs Authorization
    - Role-based access control
    - Security filter chains

2. **🏗️ Enterprise Architecture Patterns**
    - Service layer separation
    - DTO pattern for data transfer
    - Repository pattern for data access

3. **🛡️ Modern Web Security**
    - JWT stateless authentication
    - CSRF protection mechanisms
    - Input validation best practices

4. **📊 Database Security**
    - SQL injection prevention
    - Password hashing strategies
    - Audit logging implementation

5. **🎯 API Design Principles**
    - RESTful endpoint design
    - Proper HTTP status codes
    - Error handling patterns

---

## 🔮 Future Enhancements

### 🎯 Potential Improvements
- 📱 **Two-Factor Authentication** (TOTP)
- ⏰ **Rate Limiting** per endpoint
- 🔄 **Redis Session Storage** for scalability
- 📧 **Email Verification** for registration
- 🔍 **Advanced Search** with Elasticsearch
- 📊 **Metrics & Monitoring** with Actuator
- 🌐 **API Documentation** with OpenAPI/Swagger

---

## 👨‍💻 Developer Notes

### 🧠 Key Design Decisions

1. **Dual Authentication Support**: Supports both traditional web sessions and modern JWT tokens
2. **SQLite for Development**: Easy setup without external database dependencies
3. **Comprehensive Validation**: Multi-layer validation (annotation + custom logic)
4. **Security-First Approach**: Secure by default with extensive logging
5. **Clean Architecture**: Clear separation between controllers, services, and repositories

### ⚡ Performance Considerations
- JWT tokens reduce server session storage
- Repository query optimization with JPA
- Lazy loading for entity relationships
- Connection pooling with HikariCP (default)

---

## 📄 License & Attribution

This project is developed for educational purposes as part of a Spring Security course assignment. It demonstrates industry best practices for secure web application development using the Spring Boot ecosystem.

---

**🎯 Ready to explore secure Spring Boot development? Clone, run, and start building!** 🚀