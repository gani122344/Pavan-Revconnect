# 🎯 REVCONNECT — COMPLETE PROJECT INTERVIEW GUIDE
## 🚀 Full-Stack Social Media Application — Deep Dive

---

# 📊 SLIDE 1: PROJECT OVERVIEW

## 🎯 What is RevConnect?
RevConnect is a **full-stack monolithic social media platform** built with **Angular 19 (Frontend)** and **Spring Boot 3 (Backend)** with **MySQL** database.

### 🎨 3D Conceptual Architecture
```
┌─────────────────────────────────────────────────┐
│  🔵 TOP LAYER — FRONTEND (Angular 19)           │
│  Components: Feed, Profile, Explore, Messages    │
│  Services: AuthService, PostService, UserService │
│  Interceptors: JWT Auth Interceptor              │
├─────────────────────────────────────────────────┤
│  🟢 MIDDLE LAYER — BACKEND (Spring Boot 3)      │
│  Controllers → Services → Repositories           │
│  Security: JWT + Spring Security                 │
│  AOP: Logging Aspect                             │
├─────────────────────────────────────────────────┤
│  🟠 BOTTOM LAYER — DATABASE (MySQL)             │
│  Tables: users, posts, comments, likes,          │
│  connections, messages, notifications, stories,  │
│  bookmarks, shares, hashtags                     │
└─────────────────────────────────────────────────┘
```

### ⭐ Key Features
- **Authentication** — Register, Login, Email Verification (OTP), Forgot/Reset Password
- **Posts** — Create, Edit, Delete, Pin, Schedule, Feed (Public + Personalized)
- **Interactions** — Like/Unlike, Comment (with replies), Share/Repost, Bookmark
- **Connections** — Follow/Unfollow, Accept/Reject Requests, Block/Unblock
- **Notifications** — Like, Comment, Share, Follow notifications with read/unread
- **Messages** — Direct messaging with conversations, read receipts
- **Stories** — 24-hour expiry, highlights, reactions, replies
- **Analytics** — Post performance, follower growth, engagement metrics
- **Search** — Global search, advanced filters, trending, suggestions
- **Media** — Upload images/videos, profile pictures, cover photos
- **Business/Creator** — CTA buttons, product tags, promotional posts, scheduling

---

# 📊 SLIDE 2: TECH STACK (WHY EACH CHOICE)

| Layer | Technology | WHY? |
|-------|-----------|------|
| 🔵 Frontend | **Angular 19** | Component-based, TypeScript safety, standalone components, lazy loading |
| 🔵 Styling | **Custom CSS** | Full control over responsive design, glassmorphism UI |
| 🔵 HTTP | **HttpClient + Interceptors** | Automatic JWT injection on every request |
| 🟢 Backend | **Spring Boot 3** | Auto-configuration, embedded Tomcat, production-ready |
| 🟢 Security | **Spring Security + JWT** | Stateless authentication, role-based access |
| 🟢 ORM | **Spring Data JPA + Hibernate** | Automatic CRUD, custom JPQL queries, pagination |
| 🟢 Validation | **Jakarta Validation** | `@Valid`, `@NotBlank` — declarative input validation |
| 🟢 API Docs | **Swagger/OpenAPI** | Auto-generated interactive API documentation |
| 🟢 AOP | **Spring AOP** | Cross-cutting logging without modifying business code |
| 🟢 Build | **Lombok** | Eliminates boilerplate: `@Getter`, `@Setter`, `@Builder` |
| 🟠 Database | **MySQL** | Relational data with ACID compliance, indexing, JOINs |
| 🔧 CI/CD | **Jenkins** | Automated build pipeline |

### ⭐ Why Monolithic Architecture?
- **Simpler deployment** — Single JAR/WAR
- **Easier debugging** — All code in one place
- **Lower latency** — No inter-service network calls
- **Good for MVP** — Faster development cycle

---

# 📊 SLIDE 3: PROJECT STRUCTURE

## 🟢 Backend Structure
```
backend/src/main/java/org/revature/revconnect/
├── RevconnectApplication.java          ← Entry point (@SpringBootApplication)
├── aspect/
│   └── LoggingAspect.java              ← AOP cross-cutting logging
├── config/
│   ├── SecurityConfig.java             ← Spring Security filter chain ⭐
│   ├── SwaggerConfig.java              ← OpenAPI documentation config
│   ├── WebMvcConfig.java               ← CORS and static resources
│   └── WebSocketConfig.java            ← WebSocket for real-time messaging
├── controller/                         ← REST API endpoints (16 controllers)
│   ├── AuthController.java             ← /api/auth/** ⭐
│   ├── PostController.java             ← /api/posts/** ⭐
│   ├── InteractionController.java      ← /api/posts/{id}/like, /comments ⭐
│   ├── ConnectionController.java       ← /api/users/{id}/follow ⭐
│   ├── NotificationController.java     ← /api/notifications/**
│   ├── MessageController.java          ← /api/messages/**
│   ├── UserController.java             ← /api/users/**
│   ├── StoryController.java            ← /api/stories/**
│   ├── BookmarkController.java         ← /api/bookmarks/**
│   ├── SearchController.java           ← /api/search/**
│   ├── AnalyticsController.java        ← /api/analytics/**
│   ├── MediaController.java            ← /api/media/**
│   ├── HashtagController.java          ← /api/hashtags/**
│   ├── BusinessController.java         ← /api/business/**
│   ├── AdminController.java            ← /api/admin/**
│   ├── SettingsController.java         ← /api/settings/**
│   └── WebSocketController.java        ← WebSocket endpoints
├── dto/
│   ├── request/                        ← Incoming request DTOs (10+ files)
│   │   ├── LoginRequest.java
│   │   ├── RegisterRequest.java
│   │   ├── PostRequest.java
│   │   ├── CommentRequest.java
│   │   ├── ShareRequest.java
│   │   ├── MessageRequest.java
│   │   ├── ProfileUpdateRequest.java
│   │   ├── SchedulePostRequest.java
│   │   └── ...
│   └── response/                       ← Outgoing response DTOs (10+ files)
│       ├── ApiResponse.java            ← Generic wrapper {success, message, data} ⭐
│       ├── PagedResponse.java          ← Pagination wrapper ⭐
│       ├── AuthResponse.java
│       ├── PostResponse.java
│       ├── UserResponse.java
│       ├── CommentResponse.java
│       ├── NotificationResponse.java
│       └── ...
├── enums/                              ← Application enumerations
│   ├── UserType.java                   ← PERSONAL, CREATOR, BUSINESS
│   ├── PostType.java                   ← TEXT, IMAGE, VIDEO, PROMOTIONAL, REPOST
│   ├── ConnectionStatus.java           ← PENDING, ACCEPTED, REJECTED, BLOCKED
│   ├── NotificationType.java           ← LIKE, COMMENT, SHARE, NEW_FOLLOWER
│   ├── Privacy.java                    ← PUBLIC, PRIVATE
│   └── ...
├── exception/                          ← Custom exceptions with @ControllerAdvice
├── mapper/                             ← Entity ↔ DTO mappers (8 files)
├── model/                              ← JPA Entities (17 files) ⭐
│   ├── User.java                       ← implements UserDetails ⭐
│   ├── Post.java
│   ├── Comment.java
│   ├── Like.java
│   ├── Connection.java
│   ├── Notification.java
│   ├── Message.java
│   ├── Story.java
│   ├── Bookmark.java
│   ├── Share.java
│   └── ...
├── repository/                         ← Spring Data JPA repos (17 files) ⭐
│   ├── UserRepository.java
│   ├── PostRepository.java
│   ├── ConnectionRepository.java
│   └── ...
├── security/                           ← JWT Authentication ⭐
│   ├── JwtAuthenticationFilter.java    ← OncePerRequestFilter
│   ├── JwtTokenProvider.java           ← Generate/Validate JWT
│   └── JwtAuthenticationEntryPoint.java
└── service/                            ← Business logic (18 files) ⭐
    ├── AuthService.java
    ├── PostService.java
    ├── InteractionService.java
    ├── ConnectionService.java
    ├── NotificationService.java
    ├── MessageService.java
    ├── UserService.java
    ├── StoryService.java
    ├── AnalyticsService.java
    ├── SearchService.java
    ├── BookmarkService.java
    ├── MediaService.java
    ├── HashtagService.java
    ├── EmailService.java
    └── ...
```

## 🔵 Frontend Structure
```
frontend/src/app/
├── app.ts                              ← Root component
├── app.routes.ts                       ← All route definitions (lazy-loaded)
├── app.config.ts                       ← Providers: Router, HttpClient, Interceptors
├── core/
│   ├── components/
│   │   ├── navbar/navbar.ts            ← Top navigation bar
│   │   ├── sidebar/sidebar.ts          ← Left sidebar with menu
│   │   └── bottom-nav/bottom-nav.ts    ← Mobile bottom navigation
│   ├── interceptors/
│   │   └── auth.interceptor.ts         ← Adds Bearer token to all HTTP requests ⭐
│   └── services/                       ← Angular services (HTTP calls)
│       ├── auth.service.ts             ← login(), register(), storeToken() ⭐
│       ├── post.service.ts             ← CRUD posts, feed APIs
│       ├── user.service.ts             ← getMyProfile(), updateProfile()
│       ├── interaction.service.ts      ← like, comment, share APIs
│       ├── connection.service.ts       ← follow, accept, reject APIs
│       ├── notification.service.ts     ← getNotifications(), unreadCount$
│       ├── message.service.ts          ← conversations, sendMessage()
│       ├── story.service.ts            ← createStory(), getStoriesFeed()
│       ├── analytics.service.ts        ← getOverview(), getPostPerformance()
│       ├── search.service.ts           ← searchAll(), advancedSearch()
│       ├── bookmark.service.ts         ← bookmark/unbookmark
│       ├── media.service.ts            ← uploadFile(), uploadProfilePicture()
│       └── settings.service.ts         ← getSettings(), updateSettings()
└── features/
    ├── auth/
    │   ├── login/login.component.ts    ← Login page ⭐
    │   ├── register/register.ts        ← Registration page
    │   ├── forgot-password/            ← Forgot password flow
    │   └── reset-password/             ← Reset password flow
    ├── feed/feed-page/feed-page.ts     ← Main feed with posts ⭐
    ├── explore/explore-page/           ← Discover users & trending posts
    ├── profile/profile-page/           ← User profile with tabs
    ├── messages/messages-page/         ← Direct messaging
    ├── notifications/                  ← Notification center
    ├── bookmarks/                      ← Saved posts
    ├── settings/                       ← Account settings
    ├── analytics/                      ← Creator/Business dashboard
    ├── stories/stories-feed/           ← Stories component (embedded in feed)
    └── landing/landing-page/           ← Public landing page
```

---

# 📊 SLIDE 4: SPRING BOOT REQUEST LIFECYCLE ⭐

```
[User Clicks Button in Angular App]
        │
        ▼
[🔵 Angular HttpClient sends HTTP Request]
        │  Authorization: Bearer <JWT_TOKEN>
        ▼
[🟢 JwtAuthenticationFilter (OncePerRequestFilter)]
        │  1. Extracts JWT from "Authorization" header
        │  2. Validates token via JwtTokenProvider.validateToken()
        │  3. Loads UserDetails via UserDetailsService
        │  4. Sets SecurityContext with authenticated user
        ▼
[🟢 SecurityFilterChain checks authorization]
        │  permitAll() for /api/auth/**
        │  authenticated() for everything else
        ▼
[🟢 @RestController method receives request]
        │  @RequestBody → DTO deserialization
        │  @Valid → Jakarta Bean Validation
        ▼
[🟢 @Service layer — Business Logic]
        │  authService.getCurrentUser() → gets user from SecurityContext
        │  Processes business rules
        ▼
[🟢 @Repository — Spring Data JPA]
        │  JPQL queries / method-name queries
        │  Hibernate generates SQL
        ▼
[🟠 MySQL Database executes query]
        ▼
[🟢 Entity → Mapper → DTO Response]
        ▼
[🟢 ApiResponse.success(message, data)]
        │  { "success": true, "message": "...", "data": {...} }
        ▼
[🔵 Angular receives JSON response]
        │  Updates component state
        ▼
[🔵 Angular re-renders UI]
```

### ⭐ Key Concepts
- **Stateless** — No server-side sessions; JWT carries all auth info
- **Dependency Injection** — Spring `@RequiredArgsConstructor` injects all dependencies via constructor
- **Layered Architecture** — Controller → Service → Repository (separation of concerns)

---

# 📊 SLIDE 5: DATABASE SCHEMA (ER DESIGN) ⭐

## Tables & Relationships
```
┌──────────┐     ┌──────────┐     ┌──────────┐
│  USERS   │────<│  POSTS   │────<│ COMMENTS │
│──────────│     │──────────│     │──────────│
│ id (PK)  │     │ id (PK)  │     │ id (PK)  │
│ username │     │ user_id  │     │ user_id  │
│ email    │     │ content  │     │ post_id  │
│ password │     │ post_type│     │ content  │
│ name     │     │ pinned   │     │ parent_id│  ← Self-referencing for replies
│ bio      │     │ like_cnt │     │ like_cnt │
│ profile_ │     │ comment_ │     │ reply_cnt│
│  picture │     │ share_cnt│     └──────────┘
│ cover_   │     │ original_│
│  photo   │     │  post_id │  ← For reposts
│ user_type│     └──────────┘
│ privacy  │
│ is_verified│   ┌──────────┐     ┌──────────────┐
│ is_active│     │  LIKES   │     │ CONNECTIONS   │
└──────────┘     │──────────│     │──────────────│
                 │ user_id  │     │ follower_id  │
                 │ post_id  │     │ following_id │
                 │ UNIQUE   │     │ status       │
                 └──────────┘     │ (PENDING/    │
                                  │  ACCEPTED/   │
┌──────────────┐                  │  REJECTED/   │
│ NOTIFICATIONS│                  │  BLOCKED)    │
│──────────────│                  └──────────────┘
│ user_id      │
│ actor_id     │  ┌──────────┐   ┌──────────┐
│ type         │  │ MESSAGES │   │ STORIES  │
│ message      │  │──────────│   │──────────│
│ reference_id │  │ sender_id│   │ user_id  │
│ is_read      │  │ receiver │   │ media_url│
└──────────────┘  │ content  │   │ caption  │
                  │ media_url│   │ expires  │
┌──────────┐      │ is_read  │   │ highlight│
│BOOKMARKS │      └──────────┘   │ view_cnt │
│──────────│                     └──────────┘
│ user_id  │      ┌──────────┐
│ post_id  │      │ SHARES   │   ┌──────────┐
│ UNIQUE   │      │──────────│   │ HASHTAGS │
└──────────┘      │ user_id  │   │──────────│
                  │ post_id  │   │ name     │
                  │ comment  │   │ count    │
                  └──────────┘   └──────────┘
```

### ⭐ Key Design Decisions
- **`ddl-auto=update`** — Hibernate auto-creates/updates tables from entity classes
- **Database Indexes** — On `user_id`, `email`, `created_at` for fast queries
- **Unique Constraints** — `likes(user_id, post_id)`, `bookmarks(user_id, post_id)` prevent duplicates
- **Soft Relationships** — `notification.reference_id` is a generic FK (can point to post, comment, etc.)
- **Self-referencing** — `comments.parent_id` → enables nested replies; `posts.original_post_id` → enables reposts

---

# 📊 SLIDE 6: SECURITY & JWT AUTHENTICATION ⭐

## 🔐 Spring Security Configuration
**File:** `config/SecurityConfig.java`

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) {
    http.csrf(csrf -> csrf.disable())           // Disabled — REST API uses JWT, not cookies
        .cors(cors -> cors.configurationSource(...))
        .sessionManagement(session -> session
            .sessionCreationPolicy(STATELESS))   // ⭐ No server-side sessions
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/auth/**").permitAll()       // Public: login, register
            .requestMatchers("/api/public/**").permitAll()     // Public endpoints
            .requestMatchers("/swagger-ui/**").permitAll()     // API docs
            .anyRequest().authenticated())                      // Everything else needs JWT
        .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
}
```

### ⭐ Why CSRF is Disabled?
- REST APIs are **stateless** — no cookies/sessions
- Authentication is via `Authorization: Bearer <token>` header
- CSRF attacks exploit cookie-based auth, which we don't use

### ⭐ Why STATELESS Session Management?
- Each request carries its own auth info (JWT)
- Server doesn't store session data → **horizontally scalable**
- No session fixation vulnerability

## 🔐 JWT Token Flow
```
[Register/Login] → [Server generates JWT with userId + username] → [Client stores in localStorage]
       ↓
[Every subsequent request] → [Auth Interceptor adds "Bearer <token>" header]
       ↓
[JwtAuthenticationFilter] → [Validates token signature + expiry] → [Sets SecurityContext]
       ↓
[Controller accesses user via SecurityContextHolder.getContext().getAuthentication()]
```

### 🔐 JwtTokenProvider.java — Key Methods
**File:** `security/JwtTokenProvider.java`

| Method | Purpose |
|--------|---------|
| `generateToken(User user)` | Creates JWT with `user.id` as subject, signed with HMAC-SHA256, expires in 24h |
| `getUserIdFromToken(String token)` | Parses JWT claims → extracts subject (userId) |
| `validateToken(String token)` | Verifies signature, checks expiry. Returns `true` if valid |

### 🔐 JwtAuthenticationFilter.java — Request Processing
**File:** `security/JwtAuthenticationFilter.java`

```
doFilterInternal(request, response, filterChain):
  1. String token = extractTokenFromHeader(request)  // Gets "Bearer xxx" → "xxx"
  2. if (token != null && jwtTokenProvider.validateToken(token)):
       Long userId = jwtTokenProvider.getUserIdFromToken(token)
       UserDetails user = customUserDetailsService.loadUserById(userId)
       Set SecurityContext authentication
  3. filterChain.doFilter(request, response)          // Continue to controller
```

### 🔵 Frontend Auth Interceptor
**File:** `core/interceptors/auth.interceptor.ts`

```typescript
export const authInterceptor: HttpInterceptorFn = (req, next) => {
    const token = localStorage.getItem('revconnect_token');
    if (token) {
        const cloned = req.clone({
            setHeaders: { Authorization: `Bearer ${token}` }
        });
        return next(cloned).pipe(
            tap({
                error: (err) => {
                    if (err.status === 401) {         // ⭐ Token expired
                        localStorage.removeItem('revconnect_token');
                        router.navigate(['/login']);   // Redirect to login
                    }
                }
            })
        );
    }
    return next(req);
};
```

### 🔵 Frontend Auth Guard
**File:** `core/guards/auth.guard.ts`

```typescript
export const authGuard: CanActivateFn = () => {
    const token = localStorage.getItem('revconnect_token');
    if (token) return true;
    router.navigate(['/login']);
    return false;                    // ⭐ Blocks route access if no token
};
```

---

# 📊 SLIDE 7: 🔐 FEATURE — USER REGISTRATION ⭐

## 1. Feature Overview
Users can create a new account with username, email, password, and name. Email verification via 6-digit OTP is required before login.

## 2. User Flow ⭐
```
User fills form → Clicks "Register" → API call → Server validates → 
Saves user → Sends OTP email → User enters OTP → Email verified → 
JWT issued → Redirected to Feed
```

## 3. 3D Conceptual View ⭐
```
┌─────────────────────────────────────────────┐
│ 🔵 FRONTEND — Register Component            │
│    Input fields: username, email, password   │
│    Button: "Register"                        │
│    POST /api/auth/register                   │
├─────────────────────────────────────────────┤
│ 🟢 BACKEND — AuthController.register()      │
│    Validates → AuthService.register()        │
│    Checks duplicate username/email           │
│    BCrypt encodes password                   │
│    Saves User + UserSettings                 │
│    Generates 6-digit OTP                     │
│    Sends verification email                  │
├─────────────────────────────────────────────┤
│ 🟠 DATABASE                                 │
│    INSERT INTO users (...)                   │
│    INSERT INTO user_settings (...)           │
│    INSERT INTO password_reset_tokens (otp)   │
└─────────────────────────────────────────────┘
```

## 4. Button-Level Explanation ⭐ — Register Button

| Step | Layer | Action |
|------|-------|--------|
| 1 | 🔵 User clicks **"Register"** | Form validation triggers |
| 2 | 🔵 `register.ts` | Calls `authService.register(registerData)` |
| 3 | 🔵 `auth.service.ts` | `POST /api/auth/register` with `{username, email, password, name, userType}` |
| 4 | 🟢 `AuthController.register()` | `@PostMapping("/api/auth/register")` |
| 5 | 🟢 `AuthService.register()` | Checks `userRepository.existsByUsername()` and `existsByEmail()` |
| 6 | 🟢 AuthService | If duplicate → throws `DuplicateResourceException` |
| 7 | 🟢 AuthService | `passwordEncoder.encode(password)` — BCrypt hashing ⭐ |
| 8 | 🟢 AuthService | `User.builder().username().email().password().name().userType().build()` |
| 9 | 🟠 `userRepository.save(user)` | `INSERT INTO users VALUES (...)` |
| 10 | 🟠 `userSettingsRepository.save(settings)` | Default settings created |
| 11 | 🟢 AuthService | Generates 6-digit OTP: `String.format("%06d", random * 1000000)` |
| 12 | 🟠 `passwordResetTokenRepository.save(token)` | Stores OTP with 24h expiry |
| 13 | 🟢 `emailService.sendVerificationEmail()` | Sends OTP to user's email |
| 14 | 🔵 Frontend | Shows "Check your email for OTP" message |

## 5. Flow Diagram ⭐
```
[User Clicks Register]
       ↓
[🔵 Frontend validates form fields]
       ↓ POST /api/auth/register
[🟢 AuthController.register(@RequestBody RegisterRequest)]
       ↓
[🟢 AuthService.register(request)]
       ↓
[🟢 Check existsByUsername() → DuplicateResourceException if exists]
       ↓
[🟢 Check existsByEmail() → DuplicateResourceException if exists]
       ↓
[🟢 passwordEncoder.encode(password) — BCrypt]
       ↓
[🟢 User.builder()...build() → userRepository.save()]
       ↓
[🟠 INSERT INTO users]
       ↓
[🟢 UserSettings.builder()...build() → save()]
       ↓
[🟢 Generate OTP → save to password_reset_tokens]
       ↓
[🟢 emailService.sendVerificationEmail(email, otp)]
       ↓
[🔵 AuthResponse returned (no token yet — must verify first)]
       ↓
[🔵 UI shows email verification screen]
```

## 6. Email Verification — Verify Button ⭐
| Step | Layer | Action |
|------|-------|--------|
| 1 | 🔵 User enters OTP, clicks **"Verify"** | `POST /api/auth/verify-email` |
| 2 | 🟢 `AuthController.verifyEmail()` | Delegates to `AuthService.verifyEmail()` |
| 3 | 🟢 AuthService | Finds user by email, finds stored OTP token |
| 4 | 🟢 AuthService | Compares OTP → `BadRequestException` if wrong |
| 5 | 🟢 AuthService | Checks `token.isExpired()` → error if expired |
| 6 | 🟢 AuthService | Sets `user.isVerified = true`, saves |
| 7 | 🟢 AuthService | `jwtTokenProvider.generateToken(user)` — JWT issued ⭐ |
| 8 | 🔵 Frontend | Stores token in `localStorage`, navigates to `/feed` |

## 7. Security ⭐
- Passwords **never stored in plaintext** — BCrypt with salt
- OTP expires in **24 hours**
- Unverified users **cannot login** (`BadRequestException: "Please verify your email"`)

## 8. Edge Cases
- Duplicate username/email → `409 DuplicateResourceException`
- Invalid OTP → `400 BadRequestException`
- Expired OTP → Token deleted, user must request new one
- Resend verification → Old OTP deleted, new one generated

---

# 📊 SLIDE 8: 🔐 FEATURE — LOGIN ⭐

## 1. Feature Overview
Users login with username/email + password. Server validates credentials, checks email verification status, generates JWT.

## 2. Button-Level Explanation ⭐ — Login Button

| Step | Layer | Action |
|------|-------|--------|
| 1 | 🔵 User clicks **"Login"** | Form submits |
| 2 | 🔵 `login.component.ts` | Calls `authService.login({usernameOrEmail, password})` |
| 3 | 🔵 `auth.service.ts` | `POST /api/auth/login` |
| 4 | 🟢 `AuthController.login()` | `@PostMapping("/api/auth/login")` |
| 5 | 🟢 `AuthService.login()` | `authenticationManager.authenticate(UsernamePasswordAuthenticationToken)` ⭐ |
| 6 | 🟢 Spring Security | Calls `CustomUserDetailsService.loadUserByUsername()` |
| 7 | 🟠 `userRepository.findByUsernameOrEmail()` | SQL: `SELECT * FROM users WHERE username=? OR email=?` |
| 8 | 🟢 Spring Security | `passwordEncoder.matches(rawPassword, encodedPassword)` — BCrypt comparison ⭐ |
| 9 | 🟢 AuthService | Checks `user.getIsVerified()` → error if not verified |
| 10 | 🟢 AuthService | If account was deactivated → reactivates (`isActive = true`) |
| 11 | 🟢 `jwtTokenProvider.generateToken(user)` | Creates JWT: `{sub: userId, iat: now, exp: +24h}` |
| 12 | 🟢 Returns `AuthResponse` | `{accessToken, tokenType, userId, username, email, name, userType}` |
| 13 | 🔵 `auth.service.ts` | `localStorage.setItem('revconnect_token', token)` |
| 14 | 🔵 `auth.service.ts` | `sessionStorage.setItem('revconnect_login_time', new Date())` |
| 15 | 🔵 `login.component.ts` | `router.navigate(['/feed'])` → Redirect to feed page |

## 3. Flow Diagram ⭐
```
[User Clicks Login Button]
       ↓
[🔵 login.component.ts → authService.login()]
       ↓ POST /api/auth/login {usernameOrEmail, password}
[🟢 AuthController.login(@RequestBody LoginRequest)]
       ↓
[🟢 AuthService.login(request)]
       ↓
[🟢 authenticationManager.authenticate(
       new UsernamePasswordAuthenticationToken(username, password))]
       ↓
[🟢 CustomUserDetailsService.loadUserByUsername(username)]
       ↓
[🟠 SELECT * FROM users WHERE username = ? OR email = ?]
       ↓
[🟢 BCrypt.matches(password, storedHash)] → ❌ BadCredentialsException
       ↓ ✅ Match
[🟢 Check isVerified → ❌ "Please verify your email"]
       ↓ ✅ Verified
[🟢 jwtTokenProvider.generateToken(user)]
       ↓
[🟢 AuthResponse {accessToken: "eyJ...", userId: 1, username: "john"}]
       ↓
[🔵 Store token → Navigate to /feed]
       ↓
[🔵 Feed loads → All API calls include Bearer token]
```

## 4. Security ⭐
- **`AuthenticationManager`** delegates to `DaoAuthenticationProvider` which uses `BCryptPasswordEncoder`
- **Bad credentials** → Spring throws `BadCredentialsException` (401)
- **Unverified email** → Custom `BadRequestException` (400)
- **JWT expiry** = `86400000ms` (24 hours) configured in `application.properties`
- **Token contains**: `{sub: userId, iat: issuedAt, exp: expiration}` signed with HMAC-SHA256

---

# 📊 SLIDE 9: 🔐 FORGOT/RESET PASSWORD

## Button-Level: "Forgot Password" Button
| Step | Layer | Action |
|------|-------|--------|
| 1 | 🔵 User clicks **"Forgot Password?"** link | Navigates to `/forgot-password` |
| 2 | 🔵 User enters email, clicks **"Send OTP"** | `POST /api/auth/forgot-password` |
| 3 | 🟢 `AuthController.forgotPassword()` | Delegates to `AuthService.forgotPassword()` |
| 4 | 🟢 AuthService | Finds user by email → `ResourceNotFoundException` if not found |
| 5 | 🟢 AuthService | Deletes existing reset tokens for this user |
| 6 | 🟢 AuthService | Generates new 6-digit OTP, saves with 24h expiry |
| 7 | 🟢 `emailService.sendPasswordResetEmail()` | Sends OTP |
| 8 | 🔵 UI shows "Enter OTP and new password" form |

## Button-Level: "Reset Password" Button
| Step | Layer | Action |
|------|-------|--------|
| 1 | 🔵 User enters OTP + new password, clicks **"Reset"** | `POST /api/auth/reset-password` |
| 2 | 🟢 `AuthService.resetPassword()` | Finds token by OTP value |
| 3 | 🟢 AuthService | Checks `token.isExpired()` |
| 4 | 🟢 AuthService | `passwordEncoder.encode(newPassword)` → updates user |
| 5 | 🟠 `userRepository.save(user)` | Updates password in DB |
| 6 | 🟢 AuthService | Deletes used token |
| 7 | 🔵 UI shows "Password reset successful" → redirects to login |

---

# 📊 SLIDE 10: 📝 FEATURE — POST MANAGEMENT ⭐

## 1. Feature Overview
Users can create text/image/video posts, edit, delete, pin, and schedule posts. Feed supports public, personalized (following-based), and trending views.

## 2. Database Entity — `Post.java`
**File:** `model/Post.java`
```
Post {
  id (Long, PK, auto-generated)
  content (TEXT, not null)            ← Post body
  user (ManyToOne → User)            ← Author
  postType (Enum: TEXT|IMAGE|VIDEO|PROMOTIONAL|ANNOUNCEMENT|UPDATE|REPOST)
  mediaUrls (ElementCollection)      ← List of image/video URLs
  pinned (Boolean, default false)
  likeCount (Integer, default 0)     ← Denormalized counter ⭐
  commentCount (Integer, default 0)  ← Denormalized counter ⭐
  shareCount (Integer, default 0)    ← Denormalized counter ⭐
  originalPost (ManyToOne → Post)    ← For reposts, references original
  createdAt (LocalDateTime, auto)
  updatedAt (LocalDateTime, auto)
}
```

### ⭐ Why Denormalized Counters?
Instead of `SELECT COUNT(*) FROM likes WHERE post_id = ?` on every request (expensive JOIN), we store `likeCount` directly on the post and increment/decrement it. This is a **read optimization** — trades write complexity for fast reads.

## 3. Button-Level Explanation ⭐ — "Post" Button (Create Post)

| Step | Layer | Action |
|------|-------|--------|
| 1 | 🔵 User types content, optionally attaches media | Feed page create area |
| 2 | 🔵 User clicks **"Post"** button | `createPost()` called in `feed-page.ts` |
| 3 | 🔵 `post.service.ts` | `POST /api/posts` with `{content, postType, mediaUrls}` |
| 4 | 🟢 `PostController.createPost()` | `@PostMapping("/api/posts")` |
| 5 | 🟢 `PostService.createPost()` | Gets current user from `SecurityContext` |
| 6 | 🟢 PostService | `Post.builder().content().user().postType().mediaUrls().build()` |
| 7 | 🟠 `postRepository.save(post)` | `INSERT INTO posts (content, user_id, post_type, ...)` |
| 8 | 🟢 `hashtagService.processHashtagsFromContent()` | Extracts `#hashtags` → increments counts |
| 9 | 🟢 PostService | Converts to `PostResponse` via `PostMapper` |
| 10 | 🔵 Frontend | Prepends new post to `posts[]` array → UI instantly updates |

## 4. Flow Diagram ⭐
```
[User Clicks "Post" Button]
       ↓
[🔵 feed-page.ts → postService.createPost({content, postType, mediaUrls})]
       ↓ POST /api/posts
[🟢 PostController.createPost(@RequestBody PostRequest)]
       ↓
[🟢 PostService.createPost(request)]
       ↓
[🟢 authService.getCurrentUser() → gets user from SecurityContext]
       ↓
[🟢 Post.builder().content(req.content).user(currentUser).build()]
       ↓
[🟠 postRepository.save(post) → INSERT INTO posts]
       ↓
[🟢 hashtagService.processHashtagsFromContent(content)]
       ↓
[🟢 toResponseWithFullMetadata(post) → PostResponse with author info]
       ↓
[🔵 New post added to feed array → Angular re-renders]
```

## 5. Button-Level — "Edit" Button

| Step | Layer | Action |
|------|-------|--------|
| 1 | 🔵 User clicks **"Edit"** (⋮ menu) | Opens edit modal |
| 2 | 🔵 User modifies content, clicks **"Save"** | `updatePost()` in `feed-page.ts` |
| 3 | 🔵 `post.service.ts` | `PUT /api/posts/{postId}` |
| 4 | 🟢 `PostController.updatePost()` | `@PutMapping("/api/posts/{postId}")` |
| 5 | 🟢 `PostService.updatePost()` | Verifies `post.user.id == currentUser.id` ⭐ |
| 6 | 🟢 PostService | If not owner → `UnauthorizedException("You can only edit your own posts")` |
| 7 | 🟢 PostService | Updates content, postType, mediaUrls |
| 8 | 🟠 `postRepository.save(post)` | `UPDATE posts SET content=? WHERE id=?` |
| 9 | 🔵 Frontend | Replaces post in array → UI updates |

## 6. Button-Level — "Delete" Button

| Step | Layer | Action |
|------|-------|--------|
| 1 | 🔵 User clicks **"Delete"** (⋮ menu) | Confirmation dialog |
| 2 | 🔵 User confirms | `deletePost()` in `feed-page.ts` |
| 3 | 🔵 `post.service.ts` | `DELETE /api/posts/{postId}` |
| 4 | 🟢 `PostService.deletePost()` | Verifies ownership ⭐ |
| 5 | 🟢 PostService | **Cascade cleanup in order:** |
| 6 | 🟠 Step 1: `commentLikeRepository.deleteByCommentId()` for each comment | Clean comment likes first |
| 7 | 🟠 Step 2: `commentRepository.deleteByPostId()` | Delete all comments |
| 8 | 🟠 Step 3: `likeRepository.deleteByPostId()` | Delete all likes |
| 9 | 🟠 Step 4: `bookmarkRepository.deleteByPostId()` | Delete all bookmarks |
| 10 | 🟠 Step 5: `postAnalyticsRepository.deleteByPostId()` | Delete analytics |
| 11 | 🟠 Step 6: `postRepository.delete(post)` | Finally delete the post |
| 12 | 🔵 Frontend | Removes post from array → UI updates |

### ⭐ Why Manual Cascade Deletion?
JPA `CascadeType.ALL` on relationships could cause issues with bidirectional mappings and orphan removal. Manual deletion gives **explicit control** over the deletion order, preventing foreign key constraint violations.

## 7. Feed Generation — Personalized Feed ⭐

**Endpoint:** `GET /api/posts/personalized?page=0&size=10&postType=&userType=`

```
PostService.getPersonalizedFeed():
  1. Get current user from SecurityContext
  2. Get list of followed user IDs: connectionRepository.findFollowingUserIds(userId)
  3. Add current user's own ID to the list
  4. Query: SELECT p FROM posts WHERE user_id IN (:userIds) 
           AND (postType filter) AND (userType filter)
           ORDER BY created_at DESC
  5. Return PagedResponse<PostResponse>
```

### ⭐ Feed Algorithm
- Shows posts from users you **follow** + your own posts
- Sorted by **recency** (newest first)
- Optional filters: `postType` (TEXT, IMAGE, VIDEO...) and `userType` (PERSONAL, CREATOR, BUSINESS)
- **Pagination** via Spring Data `PageRequest.of(page, size)`

## 8. Button-Level — "Pin" Button

| Step | Layer | Action |
|------|-------|--------|
| 1 | 🔵 User clicks **"Pin"** | `PATCH /api/posts/{postId}/pin` |
| 2 | 🟢 `PostService.togglePinPost()` | Toggles `post.pinned` boolean |
| 3 | 🟠 `UPDATE posts SET pinned = !pinned WHERE id = ?` | Persisted |
| 4 | 🔵 Pinned posts appear first in profile | Custom query: `ORDER BY pinned DESC, created_at DESC` |

## 9. Scheduled Posts ⭐ (Creator/Business Feature)

| Step | Layer | Action |
|------|-------|--------|
| 1 | 🔵 User sets publish time, clicks **"Schedule"** | `POST /api/posts/schedule` |
| 2 | 🟢 `PostService.schedulePost()` | Calculates delay from now to `publishAt` |
| 3 | 🟢 PostService | Uses `ScheduledExecutorService` to delay post creation |
| 4 | 🟢 PostService | Stores schedule info in `ConcurrentHashMap<Long, Map>` |
| 5 | 🟢 After delay | Post is saved to DB, hashtags processed |
| 6 | 🟢 Status changes | `SCHEDULED → PUBLISHED` or `FAILED` |

### ⭐ Important: In-memory scheduling — scheduled posts are lost on server restart. Production would use a persistent job scheduler (Quartz, etc.)

---

# 📊 SLIDE 11: ❤️ FEATURE — LIKE/UNLIKE ⭐

## 1. Feature Overview
Users can like/unlike posts. Each like creates a `Like` entity and increments the post's `likeCount`. Duplicate likes are prevented by a unique constraint.

## 2. Database Entity — `Like.java`
```
Like {
  id (Long, PK)
  user (ManyToOne → User)
  post (ManyToOne → Post)
  createdAt (LocalDateTime)
  UNIQUE CONSTRAINT: (user_id, post_id)  ← Prevents double-liking ⭐
}
```

## 3. Button-Level Explanation ⭐ — Like Button (❤️)

| Step | Layer | Action |
|------|-------|--------|
| 1 | 🔵 User clicks **❤️ Like** button | `toggleLike(post)` in `feed-page.ts` |
| 2 | 🔵 `feed-page.ts` | Checks `post.isLikedByCurrentUser` |
| 3a | 🔵 **If NOT liked** | Calls `interactionService.likePost(postId)` |
| 3b | 🔵 **If already liked** | Calls `interactionService.unlikePost(postId)` |
| 4 | 🔵 `interaction.service.ts` | `POST /api/posts/{postId}/like` or `DELETE /api/posts/{postId}/like` |
| 5 | 🟢 `InteractionController.likePost()` | `@PostMapping("/api/posts/{postId}/like")` |
| 6 | 🟢 `InteractionService.likePost()` | Gets current user |
| 7 | 🟢 InteractionService | Checks `likeRepository.existsByUserIdAndPostId()` |
| 8 | 🟢 InteractionService | If already liked → `BadRequestException` |
| 9 | 🟢 InteractionService | `Like.builder().user(currentUser).post(post).build()` |
| 10 | 🟠 `likeRepository.save(like)` | `INSERT INTO likes (user_id, post_id)` |
| 11 | 🟢 InteractionService | `post.setLikeCount(post.getLikeCount() + 1)` |
| 12 | 🟠 `postRepository.save(post)` | `UPDATE posts SET like_count = ? WHERE id = ?` |
| 13 | 🟢 `notificationService.notifyLike()` | Creates notification for post owner ⭐ |
| 14 | 🔵 Frontend | Toggles heart icon ❤️, increments displayed count |

## 4. Unlike Flow
```
[User Clicks ❤️ (already liked)]
       ↓
[🔵 interactionService.unlikePost(postId)]
       ↓ DELETE /api/posts/{postId}/like
[🟢 InteractionService.unlikePost()]
       ↓
[🟢 likeRepository.findByUserIdAndPostId() → Like entity]
       ↓
[🟠 likeRepository.delete(like) → DELETE FROM likes WHERE id = ?]
       ↓
[🟢 post.setLikeCount(Math.max(0, likeCount - 1))]  ← Math.max prevents negative ⭐
       ↓
[🟠 postRepository.save(post)]
       ↓
[🔵 UI: Heart becomes empty 🤍, count decrements]
```

## 5. How `isLikedByCurrentUser` Works ⭐
In `PostService.toResponseWithFullMetadata()`:
```java
resp.setIsLikedByCurrentUser(
    likeRepository.existsByUserIdAndPostId(currentUser.getId(), post.getId())
);
```
This boolean is sent with **every post** in the feed, so the frontend knows which heart icon to show.

---

# 📊 SLIDE 12: 💬 FEATURE — COMMENT SYSTEM ⭐

## 1. Feature Overview
Users can comment on posts, reply to comments (nested/threaded), edit/delete comments, and like/unlike comments. Comments support infinite nesting via self-referencing `parent_id`.

## 2. Database Entity — `Comment.java`
```
Comment {
  id (Long, PK)
  content (TEXT, not null)
  user (ManyToOne → User)         ← Comment author
  post (ManyToOne → Post)         ← Parent post
  parent (ManyToOne → Comment)    ← Self-referencing for replies ⭐
  likeCount (Integer, default 0)
  replyCount (Integer, default 0)
  createdAt, updatedAt
}
```

## 3. Button-Level ⭐ — "Comment Submit" Button

| Step | Layer | Action |
|------|-------|--------|
| 1 | 🔵 User types comment, clicks **"Submit"** | `submitComment(post)` in `feed-page.ts` |
| 2 | 🔵 `interaction.service.ts` | `POST /api/posts/{postId}/comments` with `{content, parentId?}` |
| 3 | 🟢 `InteractionController.addComment()` | `@PostMapping` |
| 4 | 🟢 `InteractionService.addComment()` | Gets current user |
| 5 | 🟢 InteractionService | If `parentId` provided → loads parent comment (it's a reply) |
| 6 | 🟢 InteractionService | `Comment.builder().content().user().post().parent().build()` |
| 7 | 🟠 `commentRepository.saveAndFlush(comment)` | `INSERT INTO comments` |
| 8 | 🟢 InteractionService | If reply → increments parent's `replyCount` |
| 9 | 🟢 InteractionService | Increments post's `commentCount` |
| 10 | 🟢 `notificationService.notifyComment()` | Notifies post owner ⭐ |
| 11 | 🟢 InteractionService | `enrichCommentResponse()` — adds `isLikedByCurrentUser` |
| 12 | 🔵 Frontend | Appends comment to displayed list |

## 4. Button-Level — "Delete Comment" Button

| Step | Layer | Action |
|------|-------|--------|
| 1 | 🔵 User clicks **"Delete"** on comment | `deleteComment(commentId)` |
| 2 | 🔵 `interaction.service.ts` | `DELETE /api/comments/{commentId}` |
| 3 | 🟢 `InteractionService.deleteComment()` | Ownership check ⭐ |
| 4 | 🟢 InteractionService | **Recursive deletion**: finds all replies, deletes them first |
| 5 | 🟠 For each reply: `commentLikeRepository.deleteByCommentId()` | Clean likes |
| 6 | 🟠 `commentRepository.delete(comment)` | Delete comment |
| 7 | 🟢 InteractionService | Decrements post's `commentCount` |
| 8 | 🟢 InteractionService | If reply → decrements parent's `replyCount` |

### ⭐ Ownership Rule
- Comment author can delete their own comment
- Post owner can delete ANY comment on their post
- Anyone else → `UnauthorizedException`

## 5. Comment Like Button
| Step | Layer | Action |
|------|-------|--------|
| 1 | 🔵 User clicks **like** on comment | `POST /api/comments/{commentId}/like` |
| 2 | 🟢 `InteractionService.likeComment()` | Creates `CommentLike` entity |
| 3 | 🟠 `commentLikeRepository.save()` | `INSERT INTO comment_likes` |
| 4 | 🟢 Increments `comment.likeCount` | |

---

# 📊 SLIDE 13: 🔄 FEATURE — SHARE/REPOST

## 1. Feature Overview
Users can share (repost) posts. This creates a new `Post` with `postType = REPOST` pointing to the original, a `Share` record, and increments the original post's `shareCount`.

## 2. Button-Level ⭐ — "Share" Button

| Step | Layer | Action |
|------|-------|--------|
| 1 | 🔵 User clicks **"Share"** | Optional comment input |
| 2 | 🔵 `interaction.service.ts` | `POST /api/posts/{postId}/share` with `{comment?}` |
| 3 | 🟢 `InteractionController.sharePost()` | `@PostMapping` |
| 4 | 🟢 `InteractionService.sharePost()` | Checks if already shared → `BadRequestException` |
| 5 | 🟢 InteractionService | Increments `originalPost.shareCount` |
| 6 | 🟢 InteractionService | Creates **repost**: `Post.builder().postType(REPOST).originalPost(original).build()` |
| 7 | 🟠 `postRepository.save(repost)` | New post created |
| 8 | 🟢 InteractionService | Creates `Share` record |
| 9 | 🟠 `shareRepository.save(share)` | Tracks who shared what |
| 10 | 🟢 `notificationService.notifyShare()` | Notifies original author |
| 11 | 🔵 Frontend | Updates share count in UI |

---

# 📊 SLIDE 14: 👥 FEATURE — CONNECTION/NETWORK SYSTEM ⭐

## 1. Feature Overview
Users can follow/unfollow others, send connection requests (for private accounts), accept/reject requests, block/unblock users, and view followers/following lists.

## 2. Database Entity — `Connection.java`
```
Connection {
  id (Long, PK)
  follower (ManyToOne → User)       ← The user who follows
  following (ManyToOne → User)      ← The user being followed
  status (Enum: PENDING | ACCEPTED | REJECTED | BLOCKED) ⭐
  createdAt, updatedAt
  UNIQUE CONSTRAINT: (follower_id, following_id)
}
```

### ⭐ Connection Status Flow
```
[Follow Request Sent] → status = PENDING
       ↓
[Target Accepts] → status = ACCEPTED   → Now visible in followers/following
       OR
[Target Rejects] → status = REJECTED
       OR
[User Blocks] → status = BLOCKED       → All interactions blocked
```

## 3. Button-Level ⭐ — "Follow" Button

| Step | Layer | Action |
|------|-------|--------|
| 1 | 🔵 User clicks **"Follow"** on another user's profile | `followUser(userId)` |
| 2 | 🔵 `connection.service.ts` | `POST /api/users/{userId}/follow` |
| 3 | 🟢 `ConnectionController.followUser()` | `@PostMapping` |
| 4 | 🟢 `ConnectionService.followUser()` | Gets current user |
| 5 | 🟢 ConnectionService | Checks if already following → `BadRequestException` |
| 6 | 🟢 ConnectionService | Checks target user's privacy setting |
| 7a | 🟢 **If PUBLIC** | Creates connection with `status = ACCEPTED` immediately |
| 7b | 🟢 **If PRIVATE** | Creates connection with `status = PENDING` ⭐ |
| 8 | 🟠 `connectionRepository.save(connection)` | `INSERT INTO connections` |
| 9 | 🟢 `notificationService.notifyFollow()` | Notifies target user |
| 10 | 🔵 Frontend | Button changes to "Following" or "Requested" |

## 4. Button-Level — "Unfollow" Button

| Step | Layer | Action |
|------|-------|--------|
| 1 | 🔵 User clicks **"Unfollow"** | Confirmation dialog |
| 2 | 🔵 `connection.service.ts` | `DELETE /api/users/{userId}/follow` |
| 3 | 🟢 `ConnectionService.unfollowUser()` | Finds connection between users |
| 4 | 🟠 `connectionRepository.delete(connection)` | `DELETE FROM connections` |
| 5 | 🔵 Frontend | Button reverts to "Follow" |

## 5. Button-Level — "Accept Request" Button ⭐

| Step | Layer | Action |
|------|-------|--------|
| 1 | 🔵 User sees pending request, clicks **"Accept"** | `acceptRequest(requestId)` |
| 2 | 🔵 `connection.service.ts` | `PUT /api/connections/{id}/accept` |
| 3 | 🟢 `ConnectionService.acceptRequest()` | Loads connection, verifies current user is the `following` (target) |
| 4 | 🟢 ConnectionService | Changes `status` from `PENDING → ACCEPTED` |
| 5 | 🟠 `connectionRepository.save(connection)` | `UPDATE connections SET status = 'ACCEPTED'` |
| 6 | 🟢 `notificationService.notifyConnectionAccepted()` | Notifies requester |
| 7 | 🔵 Frontend | Request removed from pending list |

## 6. Button-Level — "Reject Request" Button

| Step | Layer | Action |
|------|-------|--------|
| 1 | 🔵 User clicks **"Reject"** | `rejectRequest(requestId)` |
| 2 | 🟢 `ConnectionService.rejectRequest()` | Changes `status` from `PENDING → REJECTED` |
| 3 | 🟠 `UPDATE connections SET status = 'REJECTED'` | Persisted |

## 7. Connection Statistics
**Endpoint:** `GET /api/users/{userId}/connection-stats`

Returns:
```json
{
  "followersCount": 150,     // countByFollowingIdAndStatus(userId, ACCEPTED)
  "followingCount": 89,      // countByFollowerIdAndStatus(userId, ACCEPTED)
  "pendingRequests": 5       // countByFollowingIdAndStatus(userId, PENDING)
}
```

## 8. Flow Diagram ⭐
```
[User Clicks "Follow"]
       ↓
[🔵 connection.service.ts → POST /api/users/{id}/follow]
       ↓
[🟢 ConnectionController.followUser()]
       ↓
[🟢 ConnectionService.followUser()]
       ↓
[🟢 Check: already following? → BadRequestException]
       ↓
[🟢 Check: target.privacy == PUBLIC?]
       ↓ YES                    ↓ NO
[status = ACCEPTED]     [status = PENDING]
       ↓                        ↓
[🟠 INSERT INTO connections (follower_id, following_id, status)]
       ↓
[🟢 notificationService.notifyFollow(target, actor)]
       ↓
[🟠 INSERT INTO notifications (user_id, actor_id, type='NEW_FOLLOWER')]
       ↓
[🔵 UI updates: "Follow" → "Following" or "Requested"]
```

## 9. Block/Unblock ⭐
**File:** `UserController.java`
- `POST /api/users/{id}/block` → Creates connection with `status = BLOCKED`
- `DELETE /api/users/{id}/block` → Removes the blocked connection
- Blocked users cannot see your posts, send messages, or follow you

## 10. Mutual Connections
**Endpoint:** `GET /api/users/{userId}/mutual-connections`
**Query:** Finds users followed by both current user and target user using a JOIN query:
```sql
SELECT DISTINCT f FROM Connection c1
JOIN Connection c2 ON c1.following = c2.following
WHERE c1.follower.id = :userId1 AND c2.follower.id = :userId2
AND c1.status = 'ACCEPTED' AND c2.status = 'ACCEPTED'
```

---

# 📊 SLIDE 15: 🔖 FEATURE — BOOKMARKS

## 1. Feature Overview
Users can save/bookmark posts for later viewing. Bookmarks are private — only the user can see their bookmarked posts.

## 2. Database Entity — `Bookmark.java`
```
Bookmark {
  id (Long, PK)
  user (ManyToOne → User)
  post (ManyToOne → Post)
  createdAt (LocalDateTime)
  UNIQUE CONSTRAINT: (user_id, post_id)  ← One bookmark per user per post
}
```

## 3. Button-Level ⭐ — "Bookmark" Button (🔖)

| Step | Layer | Action |
|------|-------|--------|
| 1 | 🔵 User clicks **🔖 Bookmark** icon | `toggleBookmark(post)` in `feed-page.ts` |
| 2a | 🔵 **If NOT bookmarked** | `bookmarkService.bookmarkPost(postId)` → `POST /api/bookmarks/posts/{postId}` |
| 2b | 🔵 **If already bookmarked** | `bookmarkService.removeBookmark(postId)` → `DELETE /api/bookmarks/posts/{postId}` |
| 3 | 🟢 `BookmarkController.bookmarkPost()` | Delegates to `BookmarkService` |
| 4 | 🟢 `BookmarkService.bookmarkPost()` | Creates `Bookmark` entity |
| 5 | 🟠 `bookmarkRepository.save(bookmark)` | `INSERT INTO bookmarks (user_id, post_id)` |
| 6 | 🔵 Frontend | Bookmark icon toggles (filled/empty) |

## 4. Viewing Bookmarks
**Endpoint:** `GET /api/bookmarks?page=0&size=10`
- Returns `PagedResponse<BookmarkResponse>` with post details
- Only returns current user's bookmarks (filtered by `SecurityContext`)

---

# 📊 SLIDE 16: 🔔 FEATURE — NOTIFICATION SYSTEM ⭐

## 1. Feature Overview
Notifications are triggered by social interactions: likes, comments, shares, follow requests, and connection acceptances. Users can view, mark as read, and delete notifications.

## 2. Database Entity — `Notification.java`
```
Notification {
  id (Long, PK)
  user (ManyToOne → User)            ← Recipient
  actor (ManyToOne → User)           ← Who triggered it
  type (Enum: LIKE | COMMENT | SHARE | NEW_FOLLOWER | CONNECTION_REQUEST | CONNECTION_ACCEPTED)
  message (String, not null)         ← Human-readable message
  referenceId (Long)                 ← ID of related entity (post/comment/user) ⭐
  isRead (Boolean, default false)
  createdAt (LocalDateTime)
}
```

### ⭐ `referenceId` Design
This is a **polymorphic reference** — it can point to a post ID, comment ID, or user ID depending on the `type`. This avoids having separate FK columns for each entity type.

## 3. How Notifications Are Created ⭐
Notifications are created **inside service methods** when actions occur:

| Trigger | Created In | Type | Message Example |
|---------|-----------|------|-----------------|
| Like a post | `InteractionService.likePost()` | `LIKE` | "john liked your post" |
| Comment on post | `InteractionService.addComment()` | `COMMENT` | "john commented on your post" |
| Share a post | `InteractionService.sharePost()` | `SHARE` | "john shared your post" |
| Follow user | `ConnectionService.followUser()` | `NEW_FOLLOWER` | "john started following you" |
| Send request | `ConnectionService.followUser()` | `CONNECTION_REQUEST` | "john sent you a connection request" |
| Accept request | `ConnectionService.acceptRequest()` | `CONNECTION_ACCEPTED` | "john accepted your request" |

## 4. Button-Level ⭐ — Notification Bell (🔔)

| Step | Layer | Action |
|------|-------|--------|
| 1 | 🔵 User sees **🔔** badge with unread count | `notification.service.ts` polls `GET /api/notifications/unread/count` |
| 2 | 🔵 User clicks **🔔** | Navigates to `/notifications` |
| 3 | 🔵 `notifications-page.ts` | Calls `GET /api/notifications` |
| 4 | 🟢 `NotificationController.getNotifications()` | Gets current user's notifications |
| 5 | 🟠 `notificationRepository.findByUserIdOrderByCreatedAtDesc()` | `SELECT * FROM notifications WHERE user_id = ? ORDER BY created_at DESC` |
| 6 | 🔵 Frontend | Displays notification list with actor info and message |

## 5. Button-Level — "Mark as Read" / "Mark All as Read"

| Step | Layer | Action |
|------|-------|--------|
| 1 | 🔵 User clicks notification or **"Mark All Read"** | |
| 2 | 🔵 Single: `PUT /api/notifications/{id}/read` | Marks one as read |
| 3 | 🔵 All: `PUT /api/notifications/read-all` | Marks all as read |
| 4 | 🟢 `NotificationService` | Updates `isRead = true` |
| 5 | 🟠 `UPDATE notifications SET is_read = true WHERE ...` | |
| 6 | 🔵 Frontend | Badge count resets, notification styling changes |

## 6. Button-Level — "Delete Notification"

| Step | Layer | Action |
|------|-------|--------|
| 1 | 🔵 User clicks **"Delete"** | `DELETE /api/notifications/{id}` |
| 2 | 🟢 `NotificationService` | Verifies ownership, deletes |
| 3 | 🟠 `DELETE FROM notifications WHERE id = ?` | |
| 4 | 🔵 Frontend | Removes from list |

## 7. Notification Click Actions ⭐
When a user clicks on a notification, the frontend navigates based on `type`:
- **LIKE/COMMENT/SHARE** → Navigate to the **post** using `referenceId`
- **NEW_FOLLOWER/CONNECTION_REQUEST** → Navigate to the **user's profile**
- **CONNECTION_ACCEPTED** → Navigate to the **user's profile**

---

# 📊 SLIDE 17: 👤 FEATURE — USER PROFILE MANAGEMENT

## 1. Feature Overview
Users can view/edit their profile (bio, profile picture, cover photo, location, website), search for other users, manage privacy settings, and view profile statistics.

## 2. Key User Entity Fields
```
User {
  // Identity
  id, username, email, password, name
  
  // Profile
  bio (TEXT)                         ← User biography
  profilePicture (String)            ← URL to profile image
  coverPhoto (String)                ← URL to cover image
  location (String)                  ← City/Country
  website (String)                   ← Personal website URL
  
  // Settings
  userType (PERSONAL | CREATOR | BUSINESS)
  privacy (PUBLIC | PRIVATE)
  isVerified (Boolean)
  isActive (Boolean)
  
  // Business Fields
  businessName, category, industry, contactInfo, businessAddress, businessHours
  externalLinks, socialMediaLinks
}
```

## 3. Button-Level ⭐ — "Edit Profile" / "Save" Button

| Step | Layer | Action |
|------|-------|--------|
| 1 | 🔵 User clicks **"Edit Profile"** | Opens edit form |
| 2 | 🔵 User modifies fields, clicks **"Save"** | `updateProfile()` in `profile-page.ts` |
| 3 | 🔵 `user.service.ts` | `PUT /api/users/me` with `ProfileUpdateRequest` |
| 4 | 🟢 `UserController.updateProfile()` | `@PutMapping("/api/users/me")` |
| 5 | 🟢 `UserService.updateProfile()` | Gets current user, updates non-null fields |
| 6 | 🟢 UserService | Updates: `name, bio, profilePicture, coverPhoto, location, website` |
| 7 | 🟢 UserService | Also updates business fields if provided |
| 8 | 🟠 `userRepository.save(user)` | `UPDATE users SET bio=?, profile_picture=? WHERE id=?` |
| 9 | 🟢 `UserMapper.toResponse(user)` | Maps entity to `UserResponse` DTO |
| 10 | 🔵 Frontend | Profile page refreshes with updated info |

## 4. Button-Level — "Search Users" / Search Bar

| Step | Layer | Action |
|------|-------|--------|
| 1 | 🔵 User types in search bar | Triggers on input change |
| 2 | 🔵 `user.service.ts` | `GET /api/users/search?query={query}` |
| 3 | 🟢 `UserController.searchUsers()` | `@GetMapping` |
| 4 | 🟢 `UserService.searchUsers()` | |
| 5 | 🟠 `userRepository.searchByUsernameOrName(query)` | JPQL: `WHERE LOWER(username) LIKE %query% OR LOWER(name) LIKE %query%` |
| 6 | 🔵 Frontend | Displays matching users in dropdown |

## 5. Privacy Settings
**Endpoint:** `PUT /api/users/me/privacy`
- Changes user's `privacy` between `PUBLIC` and `PRIVATE`
- **PUBLIC**: Anyone can follow without approval, posts visible to all
- **PRIVATE**: Follow requests require approval, posts visible only to followers

---

# 📊 SLIDE 18: 💬 FEATURE — DIRECT MESSAGING

## 1. Feature Overview
Users can send direct messages to other users, manage conversations, share media, react to messages, and search within messages. Conversations support read receipts, muting, and unread counts.

## 2. Database Entity — `Message.java`
```
Message {
  id (Long, PK)
  sender (ManyToOne → User)          ← Who sent it
  receiver (ManyToOne → User)        ← Who receives it
  content (String, max 2000 chars)
  mediaUrl (String)                  ← Optional media attachment
  timestamp (LocalDateTime)
  isRead (Boolean, default false)    ← Read receipt ⭐
  isDeleted (Boolean, default false) ← Soft delete
}
```

## 3. Button-Level ⭐ — "Send Message" Button

| Step | Layer | Action |
|------|-------|--------|
| 1 | 🔵 User types message, clicks **"Send"** | `sendMessage()` in `messages-page.ts` |
| 2 | 🔵 `message.service.ts` | `POST /api/messages/conversations/{convId}/messages` with `{content}` |
| 3 | 🟢 `MessageController.sendMessage()` | `@PostMapping` |
| 4 | 🟢 `MessageService.sendMessage()` | Gets current user, finds conversation |
| 5 | 🟢 MessageService | `Message.builder().sender(currentUser).receiver(other).content().build()` |
| 6 | 🟠 `messageRepository.save(message)` | `INSERT INTO messages` |
| 7 | 🔵 Frontend | Message appears in chat, scrolls to bottom |

## 4. Key Endpoints

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/api/messages/conversations` | GET | Get all conversations |
| `/api/messages/conversations` | POST | Create new conversation |
| `/api/messages/conversations/{id}/messages` | GET | Get messages in conversation |
| `/api/messages/conversations/{id}/messages` | POST | Send message |
| `/api/messages/conversations/{id}` | DELETE | Delete conversation |
| `/api/messages/{id}` | DELETE | Delete single message |
| `/api/messages/{id}` | PUT | Edit message |
| `/api/messages/conversations/{id}/read` | PUT | Mark conversation as read |
| `/api/messages/unread/count` | GET | Get unread message count |
| `/api/messages/{id}/react` | POST | React to message |
| `/api/messages/conversations/{id}/mute` | POST | Mute conversation |
| `/api/messages/search` | GET | Search messages |
| `/api/messages/conversations/{id}/attachment` | POST | Send media attachment |

## 5. Read Receipts ⭐
- When user opens a conversation → `PUT /api/messages/conversations/{id}/read`
- Backend marks all messages from the other user as `isRead = true`
- Frontend shows ✓✓ (double check) for read messages

---

# 📊 SLIDE 19: 📖 FEATURE — STORIES

## 1. Feature Overview
Users can create ephemeral stories (images/text with captions) that expire after **24 hours**. Stories support views, reactions, replies, highlights (permanent), and archiving.

## 2. Database Entity — `Story.java`
```
Story {
  id (Long, PK)
  user (ManyToOne → User)
  mediaUrl (String, max 500)
  caption (String, max 280)
  createdAt (LocalDateTime)
  expiresAt (LocalDateTime)           ← createdAt + 24 hours ⭐
  isHighlight (boolean)               ← Persists beyond 24h
  viewCount (int)
  
  @PrePersist: createdAt = now(), expiresAt = now() + 24h
  isExpired(): now().isAfter(expiresAt)
}
```

### ⭐ Key Design: `@PrePersist` Lifecycle Hook
When a story is saved for the first time, JPA automatically sets `createdAt = now()` and `expiresAt = now + 24 hours`. The `isExpired()` method checks if the current time is past the expiration.

## 3. Button-Level ⭐ — "Create Story" Button

| Step | Layer | Action |
|------|-------|--------|
| 1 | 🔵 User clicks **"+" (Add Story)** | Opens story creation UI |
| 2 | 🔵 User uploads image, adds caption, clicks **"Share"** | `createStory()` |
| 3 | 🔵 `story.service.ts` | `POST /api/stories` with `{mediaUrl, caption}` |
| 4 | 🟢 `StoryController.createStory()` | `@PostMapping("/api/stories")` |
| 5 | 🟢 `StoryService.createStory()` | Gets current user, builds Story entity |
| 6 | 🟠 `storyRepository.save(story)` | `INSERT INTO stories` — `@PrePersist` sets timestamps |
| 7 | 🔵 Frontend | Story appears in stories feed carousel |

## 4. Stories Feed
**Endpoint:** `GET /api/stories/feed`
- Returns stories from users the current user follows
- Filters out expired stories (`WHERE expiresAt > now()`)
- Stories are grouped by user in the frontend carousel

## 5. Key Story Endpoints

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/api/stories` | POST | Create story |
| `/api/stories/my` | GET | Get my stories |
| `/api/stories/feed` | GET | Get followed users' stories |
| `/api/stories/user/{userId}` | GET | Get specific user's stories |
| `/api/stories/{id}` | DELETE | Delete story |
| `/api/stories/{id}/view` | POST | Mark story as viewed (increments viewCount) |
| `/api/stories/{id}/viewers` | GET | Get list of story viewers |
| `/api/stories/{id}/react` | POST | React to story |
| `/api/stories/{id}/reply` | POST | Reply to story (sends DM) |
| `/api/stories/{id}/highlight` | PUT | Toggle highlight status |
| `/api/stories/highlights` | GET | Get highlighted stories (permanent) |
| `/api/stories/archive` | GET | Get archived stories |

## 6. Story Highlights ⭐
- Setting `isHighlight = true` makes a story **permanent** (doesn't expire)
- Highlights appear on the user's profile
- Useful for businesses: "Products", "Reviews", "Events" sections

---

# 📊 SLIDE 20: 📊 FEATURE — ANALYTICS DASHBOARD ⭐

## 1. Feature Overview
Creator and Business accounts get access to an analytics dashboard showing post performance, follower growth, engagement metrics, audience demographics, reach, impressions, best posting times, and content type performance.

## 2. 3D Conceptual View ⭐
```
┌─────────────────────────────────────────────────┐
│ 🔵 FRONTEND — Analytics Dashboard Component     │
│    Charts: Line, Bar, Pie (via Chart libraries)  │
│    Widgets: Overview, Top Posts, Best Time        │
│    Date range selector (7/14/30 days)            │
├─────────────────────────────────────────────────┤
│ 🟢 BACKEND — AnalyticsService                   │
│    Aggregates data from PostRepository,          │
│    ConnectionRepository, PostAnalyticsRepository │
│    Computes: engagement rate, growth, reach      │
├─────────────────────────────────────────────────┤
│ 🟠 DATABASE — Aggregate Queries                 │
│    SUM(like_count), COUNT(posts), COUNT(followers)│
│    GROUP BY date, post_type                      │
└─────────────────────────────────────────────────┘
```

## 3. Key Analytics Endpoints

| Endpoint | Purpose | Data Returned |
|----------|---------|---------------|
| `GET /api/analytics/overview` | Overall stats | Total posts, likes, comments, shares, followers |
| `GET /api/analytics/profile-views?days=7` | Profile view trends | Daily view counts |
| `GET /api/analytics/post-performance?days=7` | Post metrics | Likes, comments, shares per day |
| `GET /api/analytics/posts/{postId}/analytics` | Single post analytics | Detailed metrics for one post |
| `GET /api/analytics/followers/growth?days=30` | Follower growth | Daily follower count changes |
| `GET /api/analytics/engagement?days=7` | Engagement rate | (likes+comments+shares) / impressions |
| `GET /api/analytics/audience` | Audience demographics | User types, locations of followers |
| `GET /api/analytics/reach?days=7` | Content reach | Unique users who saw posts |
| `GET /api/analytics/impressions?days=7` | Impression count | Total views of posts |
| `GET /api/analytics/best-time` | Best posting times | Hour-by-hour engagement analysis ⭐ |
| `GET /api/analytics/top-posts?limit=10` | Top performing posts | Posts ranked by engagement |
| `GET /api/analytics/hashtag-performance` | Hashtag analytics | Which hashtags drive engagement |
| `GET /api/analytics/content-type` | Content type analysis | Performance by TEXT/IMAGE/VIDEO |
| `GET /api/analytics/export?days=30&format=csv` | Export data | CSV/JSON export |

## 4. How Analytics Are Computed ⭐
The `AnalyticsService` uses **aggregate queries** from repositories:
```java
// Total likes across all user posts
postRepository.getTotalLikesByUserId(userId);   // COALESCE(SUM(like_count), 0)

// Total comments
postRepository.getTotalCommentsByUserId(userId);

// Follower count
connectionRepository.countByFollowingIdAndStatus(userId, ACCEPTED);

// Top posts
postRepository.findTopPostsByUserId(userId, PageRequest.of(0, limit));
// ORDER BY (likeCount + commentCount + shareCount) DESC
```

---

# 📊 SLIDE 21: 🔍 FEATURE — SEARCH & DISCOVERY

## 1. Feature Overview
Comprehensive search system with global search (users + posts), advanced filters (by date, author, post type, likes), search suggestions, trending searches, and recent search history.

## 2. Button-Level ⭐ — Search Bar / "Search" Button

| Step | Layer | Action |
|------|-------|--------|
| 1 | 🔵 User types query in global search bar | Debounced input |
| 2 | 🔵 `search.service.ts` | `GET /api/search/suggestions?query={q}` (as you type) |
| 3 | 🔵 User presses **Enter** or clicks **"Search"** | `GET /api/search/all?query={q}&limit=5` |
| 4 | 🟢 `SearchController.searchAll()` | Delegates to `SearchService.searchAll()` |
| 5 | 🟢 `SearchService.searchAll()` | Searches both users AND posts |
| 6 | 🟠 `userRepository.searchPublicUsers(query)` | JPQL: `WHERE LOWER(username) LIKE %query% OR LOWER(name) LIKE %query%` |
| 7 | 🟠 `postRepository.findByContentContainingIgnoreCase(query)` | Content search |
| 8 | 🟢 SearchService | Combines results: `{users: [...], posts: [...]}` |
| 9 | 🔵 Frontend | Displays categorized search results |

## 3. Advanced Search Filters ⭐
**Endpoint:** `GET /api/search/posts/advanced`

| Filter | Parameter | Description |
|--------|-----------|-------------|
| Query | `query` | Text content search |
| Author | `author` | Filter by username |
| Date From | `dateFrom` | Posts after this date |
| Date To | `dateTo` | Posts before this date |
| Post Type | `postType` | TEXT, IMAGE, VIDEO, etc. |
| Min Likes | `minLikes` | Minimum like count |

**Endpoint:** `GET /api/search/users/advanced`

| Filter | Parameter | Description |
|--------|-----------|-------------|
| Query | `query` | Username/name search |
| Location | `location` | Filter by location |
| User Type | `userType` | PERSONAL, CREATOR, BUSINESS |
| Verified | `verified` | Only verified users |

## 4. Trending & Explore Page ⭐
- **Trending Posts:** `GET /api/posts/trending` — Sorted by `(likeCount + commentCount + shareCount) DESC`
- **Trending Searches:** `GET /api/search/trending` — Most popular search queries
- **Suggested Users:** `GET /api/users/suggestions` — Users not yet followed, public, verified
- **Hashtag Search:** `GET /api/posts/hashtag/{tag}` — Posts containing specific hashtag

---

# 📊 SLIDE 22: 📷 FEATURE — MEDIA UPLOAD & MANAGEMENT

## 1. Feature Overview
Users can upload images, videos, profile pictures, and cover photos. The system handles single/multiple file uploads, video processing, thumbnails, and media compression.

## 2. Key Endpoints

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/api/media/upload` | POST (multipart) | Upload single file |
| `/api/media/upload/multiple` | POST (multipart) | Upload multiple files |
| `/api/media/upload/profile-picture` | POST (multipart) | Upload + set profile picture |
| `/api/media/upload/cover-photo` | POST (multipart) | Upload + set cover photo |
| `/api/media/upload/video` | POST (multipart) | Upload video file |
| `/api/media/{id}` | GET | Get media details |
| `/api/media/{id}` | DELETE | Delete media |
| `/api/media/my` | GET | Get user's uploaded media |
| `/api/media/{id}/thumbnail` | GET | Get media thumbnail |
| `/api/media/{id}/process` | POST | Resize/compress media |

## 3. Button-Level ⭐ — "Upload Profile Picture" Button

| Step | Layer | Action |
|------|-------|--------|
| 1 | 🔵 User clicks **profile picture area** | File picker opens |
| 2 | 🔵 User selects image | `uploadProfilePicture(file)` |
| 3 | 🔵 `media.service.ts` | `POST /api/media/upload/profile-picture` with `FormData` (multipart) |
| 4 | 🟢 `MediaController.uploadProfilePicture()` | Receives `MultipartFile` |
| 5 | 🟢 `MediaService.uploadProfilePicture()` | Saves file, returns URL |
| 6 | 🟢 MediaService | Updates `user.profilePicture = newUrl` |
| 7 | 🟠 `userRepository.save(user)` | Updates profile picture URL in DB |
| 8 | 🔵 Frontend | Profile picture refreshes with new image |

### ⭐ Multipart File Upload
Angular sends files using `FormData` with `Content-Type: multipart/form-data`. Spring handles this with `@RequestParam("file") MultipartFile file`. The `consumes = MediaType.MULTIPART_FORM_DATA_VALUE` annotation on the controller explicitly declares this.

---

# 📊 SLIDE 23: 💼 FEATURE — BUSINESS/CREATOR FEATURES

## 1. Feature Overview
RevConnect supports three user types: **PERSONAL**, **CREATOR**, and **BUSINESS**. Creator and Business accounts get additional features: CTA buttons on posts, product tags, promotional content, post scheduling, and analytics.

## 2. User Types (Enum) ⭐
```
UserType {
  PERSONAL   ← Regular users
  CREATOR    ← Content creators (influencers, artists)
  BUSINESS   ← Business accounts (shops, companies)
}
```

## 3. Business-Specific Post Features

### CTA (Call-To-Action) Buttons ⭐
**Endpoints:**
- `POST /api/posts/{postId}/cta` with `{label, url}` → Adds a clickable button to a post
- `DELETE /api/posts/{postId}/cta` → Removes CTA

**How it works internally:**
CTAs are stored **inside the post content** using a special marker format:
```
Post content here
[[CTA|Buy Now|https://shop.com]]
```
The `PostService.parseMetadata()` method extracts this from content, and the response includes `ctaLabel` and `ctaUrl` as separate fields.

### Product Tags ⭐
**Endpoint:** `POST /api/posts/{postId}/product-tags` with `{tags: ["Product A", "Product B"]}`

Stored as: `[[TAGS|Product A,Product B]]` inside post content.

### Promotional Content
Stored as: `[[PROMO|PartnerName]]` inside post content.
The `PostResponse` includes `isPromotional: true` and `partnerName` fields.

### ⭐ Design Decision: Metadata in Content
Instead of adding separate database columns for CTA, tags, and promo data, they are embedded in the post content string using markers. The `PostService.parseMetadata()` method extracts them on read, and `buildContent()` reconstructs on write. This is a **schema-less** approach that avoids database migrations when adding new metadata types.

## 4. Creator Reply to Comments ⭐
**Endpoint:** `POST /api/posts/{postId}/comments/{commentId}/reply`
- Only available to CREATOR and BUSINESS account types
- Only for comments on the creator's own posts
- Reply is prefixed with `"Reply to #commentId: "` for visual distinction

---

# 📊 SLIDE 24: 🔧 CROSS-CUTTING CONCERNS

## 1. AOP Logging (Aspect-Oriented Programming) ⭐
**File:** `aspect/LoggingAspect.java`

### What is AOP?
AOP allows adding behavior (logging) to methods **without modifying them**. The `LoggingAspect` intercepts all `@Repository`, `@Service`, and `@RestController` methods automatically.

### How it works:
```java
@Aspect
@Component
public class LoggingAspect {

    @Pointcut("within(@Repository *) || within(@Service *) || within(@RestController *)")
    public void springBeanPointcut() {}

    @Around("applicationPackagePointcut() && springBeanPointcut()")
    public Object logAround(ProceedingJoinPoint joinPoint) {
        // LOG: Enter ClassName.method() with args
        Object result = joinPoint.proceed();       // Execute actual method
        // LOG: Exit ClassName.method() with result
        return result;
    }

    @AfterThrowing(pointcut = "...", throwing = "e")
    public void logAfterThrowing(JoinPoint jp, Throwable e) {
        // LOG: Exception in ClassName.method() with cause
    }
}
```

### ⭐ Key Annotations:
- `@Around` — Wraps method execution, can log before AND after
- `@AfterThrowing` — Logs exceptions automatically
- `@Pointcut` — Defines which methods to intercept

## 2. Global Exception Handling
Custom exceptions with `@ControllerAdvice` (GlobalExceptionHandler):

| Exception | HTTP Status | When Thrown |
|-----------|------------|-------------|
| `ResourceNotFoundException` | 404 | Entity not found by ID |
| `DuplicateResourceException` | 409 | Username/email already exists |
| `BadRequestException` | 400 | Invalid input, business rule violation |
| `UnauthorizedException` | 403 | User doesn't own the resource |

## 3. Generic API Response Wrapper ⭐
**File:** `dto/response/ApiResponse.java`

Every API response is wrapped in:
```json
{
  "success": true,          // or false
  "message": "Success",     // Human-readable message
  "data": { ... }           // Actual response data (generic type T)
}
```

**Benefits:**
- **Consistent format** — Frontend always knows the response shape
- **Error messages** — `success: false` with error message
- **Null safety** — If `data` is null, returns empty `Map.of()` instead

## 4. Pagination Wrapper ⭐
**File:** `dto/response/PagedResponse.java`

For list endpoints, responses include pagination metadata:
```json
{
  "content": [...],         // List of items
  "pageNumber": 0,          // Current page (0-indexed)
  "pageSize": 10,           // Items per page
  "totalElements": 150,     // Total items in DB
  "totalPages": 15,         // Total pages
  "first": true,            // Is first page?
  "last": false             // Is last page?
}
```

**Created from Spring Data `Page<E>`:**
```java
PagedResponse.fromEntityPage(page, entity -> mapper.toResponse(entity));
```

## 5. Dependency Injection ⭐
Spring uses **Constructor Injection** via `@RequiredArgsConstructor` (Lombok):
```java
@Service
@RequiredArgsConstructor  // Generates constructor for all 'final' fields
public class PostService {
    private final PostRepository postRepository;    // Injected by Spring
    private final AuthService authService;          // Injected by Spring
    private final PostMapper postMapper;            // Injected by Spring
}
```

### ⭐ Why Constructor Injection?
- Fields can be `final` → **immutable** after construction
- Easy to **unit test** — pass mock dependencies via constructor
- Spring **validates** all dependencies at startup (fail-fast)

---

# 📊 SLIDE 25: 🔄 COMPLETE END-TO-END FLOW ⭐

## Scenario: User logs in → Creates post → Someone likes it → Notification received

### Step 1: LOGIN
```
[🔵 User enters credentials, clicks "Login"]
       ↓
[🔵 POST /api/auth/login {username: "john", password: "pass123"}]
       ↓
[🟢 AuthenticationManager.authenticate() → BCrypt.matches()]
       ↓
[🟠 SELECT * FROM users WHERE username = 'john']
       ↓
[🟢 JwtTokenProvider.generateToken(user) → "eyJhbGci..."]
       ↓
[🔵 localStorage.setItem('revconnect_token', 'eyJhbGci...')]
       ↓
[🔵 router.navigate(['/feed'])]
```

### Step 2: LOAD FEED
```
[🔵 FeedPage.ngOnInit() → loadFeed()]
       ↓
[🔵 GET /api/posts/personalized?page=0&size=10]
       ↓ Authorization: Bearer eyJhbGci...
[🟢 JwtAuthenticationFilter validates token → sets SecurityContext]
       ↓
[🟢 PostController.getPersonalizedFeed()]
       ↓
[🟢 PostService.getPersonalizedFeed()]
       ↓
[🟠 SELECT following_id FROM connections WHERE follower_id = 1 AND status = 'ACCEPTED']
       ↓ Returns: [2, 5, 8, 1]
[🟠 SELECT * FROM posts WHERE user_id IN (1,2,5,8) ORDER BY created_at DESC LIMIT 10]
       ↓
[🟢 For each post: toResponseWithFullMetadata() → adds isLikedByCurrentUser]
       ↓
[🔵 posts[] populated → *ngFor renders post cards]
```

### Step 3: CREATE POST
```
[🔵 User types "Hello World! #first", clicks "Post"]
       ↓
[🔵 POST /api/posts {content: "Hello World! #first", postType: "TEXT"}]
       ↓
[🟢 PostService.createPost()]
       ↓
[🟠 INSERT INTO posts (content, user_id, post_type) VALUES ('Hello World! #first', 1, 'TEXT')]
       ↓
[🟢 hashtagService.processHashtagsFromContent("Hello World! #first")]
       ↓
[🟠 INSERT/UPDATE hashtags SET count = count + 1 WHERE name = 'first']
       ↓
[🔵 New post prepended to feed → UI updates instantly]
```

### Step 4: ANOTHER USER LIKES THE POST
```
[🔵 User "jane" clicks ❤️ on John's post (id=42)]
       ↓
[🔵 POST /api/posts/42/like]
       ↓
[🟢 InteractionService.likePost(42)]
       ↓
[🟢 Check: likeRepository.existsByUserIdAndPostId(jane.id, 42) → false]
       ↓
[🟠 INSERT INTO likes (user_id, post_id) VALUES (jane.id, 42)]
       ↓
[🟠 UPDATE posts SET like_count = like_count + 1 WHERE id = 42]
       ↓
[🟢 notificationService.notifyLike(john, jane, 42)]
       ↓
[🟠 INSERT INTO notifications (user_id, actor_id, type, message, reference_id, is_read)
    VALUES (john.id, jane.id, 'LIKE', 'jane liked your post', 42, false)]
       ↓
[🔵 Jane sees: ❤️ filled, likeCount: 1]
```

### Step 5: JOHN RECEIVES NOTIFICATION
```
[🔵 John's navbar polls GET /api/notifications/unread/count]
       ↓
[🟠 SELECT COUNT(*) FROM notifications WHERE user_id = john.id AND is_read = false]
       ↓ Returns: 1
[🔵 🔔 badge shows "1"]
       ↓
[🔵 John clicks 🔔 → navigates to /notifications]
       ↓
[🔵 GET /api/notifications]
       ↓
[🟠 SELECT * FROM notifications WHERE user_id = john.id ORDER BY created_at DESC]
       ↓
[🔵 Shows: "jane liked your post" with ❤️ icon]
       ↓
[🔵 John clicks notification → navigates to post #42]
       ↓
[🟢 PUT /api/notifications/{id}/read → is_read = true]
       ↓
[🔵 🔔 badge resets to 0]
```

---

# 📊 SLIDE 26: 📋 COMPLETE API REFERENCE

## 🔐 Authentication APIs (`/api/auth`)
| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/api/auth/register` | Register new user |
| POST | `/api/auth/login` | Login (returns JWT) |
| POST | `/api/auth/verify-email` | Verify email with OTP |
| POST | `/api/auth/resend-verification` | Resend OTP |
| POST | `/api/auth/forgot-password` | Request password reset OTP |
| POST | `/api/auth/reset-password` | Reset password with OTP |

## 📝 Post APIs (`/api/posts`)
| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/api/posts` | Create post |
| GET | `/api/posts/{id}` | Get single post |
| PUT | `/api/posts/{id}` | Update post |
| DELETE | `/api/posts/{id}` | Delete post |
| GET | `/api/posts/my` | Get my posts |
| GET | `/api/posts/user/{userId}` | Get user's posts |
| GET | `/api/posts/user/{userId}/liked` | Get user's liked posts |
| GET | `/api/posts/user/{userId}/media` | Get user's media posts |
| GET | `/api/posts/feed` | Public feed |
| GET | `/api/posts/trending` | Trending posts |
| GET | `/api/posts/personalized` | Personalized feed ⭐ |
| GET | `/api/posts/hashtag/{tag}` | Posts by hashtag |
| PATCH | `/api/posts/{id}/pin` | Toggle pin post |
| POST | `/api/posts/schedule` | Schedule post |
| GET | `/api/posts/scheduled` | Get scheduled posts |
| POST | `/api/posts/{id}/cta` | Set CTA button |
| DELETE | `/api/posts/{id}/cta` | Clear CTA |
| POST | `/api/posts/{id}/product-tags` | Set product tags |
| GET | `/api/posts/{id}/metadata` | Get post metadata |

## ❤️ Interaction APIs
| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/api/posts/{id}/like` | Like post |
| DELETE | `/api/posts/{id}/like` | Unlike post |
| GET | `/api/posts/{id}/likes` | Get post likes |
| GET | `/api/posts/{id}/liked` | Check if liked |
| POST | `/api/posts/{id}/comments` | Add comment |
| GET | `/api/posts/{id}/comments` | Get comments |
| DELETE | `/api/comments/{id}` | Delete comment |
| PUT | `/api/comments/{id}` | Edit comment |
| POST | `/api/comments/{id}/like` | Like comment |
| DELETE | `/api/comments/{id}/like` | Unlike comment |
| GET | `/api/comments/{id}/replies` | Get replies |
| POST | `/api/posts/{id}/share` | Share/repost |

## 👥 Connection APIs
| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/api/users/{id}/follow` | Follow user |
| DELETE | `/api/users/{id}/follow` | Unfollow user |
| GET | `/api/users/{id}/followers` | Get followers |
| GET | `/api/users/{id}/following` | Get following |
| GET | `/api/users/{id}/is-following` | Check follow status |
| GET | `/api/connections/requests` | Get pending requests |
| PUT | `/api/connections/{id}/accept` | Accept request |
| PUT | `/api/connections/{id}/reject` | Reject request |
| DELETE | `/api/connections/{id}` | Remove connection |
| GET | `/api/users/{id}/connection-stats` | Get stats |

## 👤 User APIs (`/api/users`)
| Method | Endpoint | Purpose |
|--------|----------|---------|
| GET | `/api/users/me` | Get my profile |
| PUT | `/api/users/me` | Update profile |
| GET | `/api/users/{id}` | Get user by ID |
| GET | `/api/users/username/{username}` | Get by username |
| GET | `/api/users/search` | Search users |
| PUT | `/api/users/me/privacy` | Update privacy |
| POST | `/api/users/{id}/block` | Block user |
| DELETE | `/api/users/{id}/block` | Unblock user |
| POST | `/api/users/{id}/report` | Report user |
| GET | `/api/users/{id}/mutual-connections` | Mutual connections |
| GET | `/api/users/suggestions` | Suggested users |

## 🔔 Notification APIs (`/api/notifications`)
| Method | Endpoint | Purpose |
|--------|----------|---------|
| GET | `/api/notifications` | Get all |
| GET | `/api/notifications/unread` | Get unread |
| GET | `/api/notifications/unread/count` | Unread count |
| PUT | `/api/notifications/{id}/read` | Mark as read |
| PUT | `/api/notifications/read-all` | Mark all read |
| DELETE | `/api/notifications/{id}` | Delete |

---

# 📊 SLIDE 27: ⭐ IMPORTANT INTERVIEW QUESTIONS (30+)

## 🏗️ Architecture & Design Questions

**Q1: Why did you choose a monolithic architecture over microservices?**
> RevConnect is an MVP-stage application. Monolithic architecture gives us **simpler deployment** (single JAR), **lower latency** (no inter-service calls), **easier debugging** (single codebase), and **faster development**. As the platform scales, specific services (like notifications or messaging) could be extracted into microservices.

**Q2: Explain the layered architecture of your application.**
> The application follows a strict **3-layer architecture**: **Controller** (receives HTTP requests, validates input, returns responses), **Service** (business logic, transaction management, authorization), **Repository** (data access via Spring Data JPA). Each layer only communicates with the layer directly below it, ensuring **separation of concerns**.

**Q3: How does Dependency Injection work in your project?**
> We use **constructor injection** via Lombok's `@RequiredArgsConstructor`. Spring's IoC container creates bean instances and injects them via constructor parameters. For example, `PostService` declares `private final PostRepository postRepository` — Spring automatically finds the `PostRepository` bean and injects it. This allows fields to be `final` (immutable) and makes unit testing easy (pass mocks via constructor).

**Q4: What is the purpose of the DTO (Data Transfer Object) pattern?**
> DTOs separate the **internal entity model** from the **API response**. The `User` entity contains sensitive data (password hash, settings). `UserResponse` DTO only includes safe fields (username, name, bio, profilePicture). This prevents **data leakage** and allows the API contract to evolve independently from the database schema.

## 🔐 Security Questions

**Q5: How does JWT authentication work in your application? ⭐**
> 1. User logs in with credentials → server validates via `AuthenticationManager` + BCrypt
> 2. Server generates JWT token containing `{sub: userId, iat: issuedAt, exp: +24h}` signed with HMAC-SHA256
> 3. Client stores token in `localStorage`
> 4. Every subsequent request includes `Authorization: Bearer <token>` header (via Angular interceptor)
> 5. `JwtAuthenticationFilter` extracts and validates the token on every request
> 6. Valid token → user loaded into `SecurityContext` → controller can access `getCurrentUser()`
> 7. Expired/invalid token → 401 Unauthorized → frontend redirects to login

**Q6: Why did you disable CSRF protection?**
> CSRF (Cross-Site Request Forgery) attacks exploit **cookie-based authentication**. Since our REST API uses **JWT tokens in the Authorization header** (not cookies), CSRF attacks are not applicable. The browser doesn't automatically attach the Authorization header like it does with cookies.

**Q7: How do you handle token expiration on the frontend?**
> The Angular `authInterceptor` intercepts all HTTP responses. If any response returns **401 Unauthorized**, it automatically clears the token from `localStorage`, removes the login timestamp from `sessionStorage`, and redirects the user to `/login`. Additionally, the `authGuard` on protected routes checks for token existence before allowing navigation.

**Q8: How are passwords stored securely?**
> Passwords are hashed using **BCrypt** (`passwordEncoder.encode()`). BCrypt automatically generates a random **salt** for each password, preventing rainbow table attacks. The original password is never stored. On login, `passwordEncoder.matches(rawPassword, storedHash)` compares the input against the stored hash.

**Q9: Explain the Spring Security filter chain in your application.**
> The filter chain processes each request in order: (1) `JwtAuthenticationFilter` runs first — extracts JWT, validates it, and sets the `SecurityContext`. (2) `SecurityFilterChain` checks authorization rules — `/api/auth/**` is public (`permitAll()`), everything else requires authentication. (3) If authenticated, the request proceeds to the controller. If not, a 401 response is returned.

## 📝 Feature-Level Questions

**Q10: How does the personalized feed work? ⭐**
> The personalized feed shows posts from users you follow plus your own posts. The `PostService.getPersonalizedFeed()` method: (1) Gets the list of followed user IDs from `connectionRepository.findFollowingUserIds()`, (2) Adds the current user's own ID, (3) Queries posts `WHERE user_id IN (followedIds)` with optional filters for `postType` and `userType`, (4) Returns paginated results sorted by `createdAt DESC`.

**Q11: How does the like system prevent duplicate likes?**
> Two-level protection: (1) **Database level**: The `likes` table has a `UNIQUE CONSTRAINT on (user_id, post_id)`, so the DB rejects duplicate inserts. (2) **Application level**: Before creating a like, `InteractionService` checks `likeRepository.existsByUserIdAndPostId()` and throws `BadRequestException` if already liked. The denormalized `likeCount` on the post is incremented/decremented atomically.

**Q12: How do comments support nested replies?**
> The `Comment` entity has a self-referencing `@ManyToOne` relationship via `parent` field. Top-level comments have `parent = null`. Replies have `parent = parentComment`. The query `findByPostIdAndParentIsNullOrderByCreatedAtDesc` fetches only top-level comments. Replies are loaded separately via `findByParentIdOrderByCreatedAtAsc`. Deletion is **recursive** — deleting a comment first deletes all its replies.

**Q13: How do connection requests work for private accounts? ⭐**
> When User A follows User B: If B's `privacy = PUBLIC`, the connection is created with `status = ACCEPTED` immediately. If B's `privacy = PRIVATE`, the connection is created with `status = PENDING`. User B sees the pending request and can `accept` (status → ACCEPTED) or `reject` (status → REJECTED). Only `ACCEPTED` connections appear in follower/following lists and are used for the personalized feed.

**Q14: How are notifications generated?**
> Notifications are created **inline** within service methods. For example, `InteractionService.likePost()` calls `notificationService.notifyLike(postOwner, actor, postId)` which creates a `Notification` entity with `type = LIKE`, `message = "actor liked your post"`, and `referenceId = postId`. The frontend polls `GET /api/notifications/unread/count` to show the badge number.

**Q15: How do stories expire after 24 hours?**
> The `Story` entity uses a `@PrePersist` lifecycle hook that sets `expiresAt = createdAt + 24 hours` when saved. The `isExpired()` method checks `LocalDateTime.now().isAfter(expiresAt)`. When fetching stories, the backend filters out expired ones. Stories marked as `isHighlight = true` bypass the expiration check and persist permanently.

**Q16: How does post scheduling work?**
> `PostService.schedulePost()` calculates the delay between now and `publishAt`, then uses `ScheduledExecutorService.schedule()` to execute the post creation after the delay. Schedule info is stored in an in-memory `ConcurrentHashMap`. Status transitions: `SCHEDULED → PUBLISHED` (on success) or `SCHEDULED → FAILED` (on error). Note: This is in-memory — scheduled posts are lost on server restart.

## 🗄️ Database Questions

**Q17: Why do you use denormalized counters (likeCount, commentCount)?**
> Instead of running `SELECT COUNT(*) FROM likes WHERE post_id = ?` on every feed load (expensive JOIN across many posts), we store the count directly on the `Post` entity. This is a **read optimization** — the counter is incremented/decremented during like/unlike operations. The trade-off is slightly more complex write logic, but feed rendering becomes much faster.

**Q18: Explain the database indexing strategy.**
> We use `@Index` annotations on frequently queried columns: `users(username, email, user_type, created_at)`, `posts(user_id, created_at)`, `comments(post_id, user_id)`, `connections(follower_id, following_id)`, `notifications(user_id, is_read, created_at)`. These indexes speed up lookups, JOINs, and ORDER BY operations at the cost of slightly slower inserts.

**Q19: What does `spring.jpa.hibernate.ddl-auto=update` do?**
> Hibernate compares the entity classes with the existing database schema and **automatically adds** new columns/tables without dropping existing data. `update` is suitable for development — it never deletes data. In production, you'd use `validate` (only checks schema matches) and manage migrations with Flyway or Liquibase.

**Q20: How do you handle the relationship between Post and Like?**
> `Post` has a `likeCount` field (denormalized). `Like` is a separate entity with `@ManyToOne` to both `User` and `Post`, plus a `UNIQUE CONSTRAINT` on `(user_id, post_id)`. When a like is created, both the `Like` entity is saved AND the `Post.likeCount` is incremented in the same `@Transactional` method, ensuring consistency.

## 🔵 Frontend Questions

**Q21: How does Angular's HTTP Interceptor work? ⭐**
> The `authInterceptor` is a function of type `HttpInterceptorFn` registered in `app.config.ts` via `withInterceptors([authInterceptor])`. It intercepts **every** outgoing HTTP request. If a JWT token exists in `localStorage`, it clones the request and adds the `Authorization: Bearer <token>` header. It also handles 401 responses by clearing the token and redirecting to login.

**Q22: Why use lazy loading for routes?**
> Each route uses `loadComponent: () => import('./path').then(m => m.Component)`. This means Angular only downloads the component code when the user navigates to that route, not on initial page load. This significantly **reduces the initial bundle size** and improves **Time to Interactive (TTI)**.

**Q23: How does the Auth Guard protect routes?**
> The `authGuard` is a `CanActivateFn` applied to protected routes via `canActivate: [authGuard]`. Before navigating to a protected route, Angular calls the guard function. It checks if `localStorage.getItem('revconnect_token')` exists. If yes, navigation proceeds. If no, the user is redirected to `/login` and navigation is blocked.

**Q24: How does the frontend know if a post is liked by the current user?**
> The backend includes `isLikedByCurrentUser: boolean` in every `PostResponse`. In `PostService.toResponseWithFullMetadata()`, it calls `likeRepository.existsByUserIdAndPostId(currentUser.getId(), post.getId())` for each post. The frontend reads this field to decide whether to show a filled ❤️ or empty 🤍 heart.

## 🔧 Technical Deep-Dive Questions

**Q25: Explain the Builder pattern used in your entities.**
> Lombok's `@Builder` generates a static `builder()` method that returns a fluent API for constructing objects: `User.builder().username("john").email("john@test.com").build()`. This avoids telescoping constructors and makes object creation readable. `@Builder.Default` sets default values when a field isn't specified (e.g., `likeCount = 0`).

**Q26: What is the purpose of `@Transactional` annotation?**
> `@Transactional` ensures that all database operations within the method execute as a **single atomic unit**. If any operation fails, all changes are **rolled back**. For example, `deletePost()` deletes comments, likes, bookmarks, and the post — if any step fails, none of the deletions persist. It also manages Hibernate's persistence context.

**Q27: How does the `ApiResponse<T>` generic wrapper work?**
> `ApiResponse<T>` is a generic class: `{success: boolean, message: String, data: T}`. The type parameter `T` can be any response type — `ApiResponse<PostResponse>`, `ApiResponse<List<UserResponse>>`, etc. Static factory methods `success(data)` and `error(message)` create consistent response objects. If `data` is null, it returns `Map.of()` to avoid null in JSON.

**Q28: How do you handle the CTA/Product Tags metadata in posts?**
> Instead of adding new database columns, metadata (CTA, tags, promo) is embedded in the post content using special markers: `[[CTA|label|url]]`, `[[TAGS|tag1,tag2]]`, `[[PROMO|partner]]`. `PostService.parseMetadata()` extracts these markers on read, and `buildContent()` reconstructs them on write. The `PostResponse` exposes `ctaLabel`, `ctaUrl`, `productTags`, `isPromotional` as separate fields. This schema-less approach avoids database migrations.

**Q29: What is AOP and how is it used in your project?**
> **Aspect-Oriented Programming** (AOP) handles cross-cutting concerns without modifying business code. Our `LoggingAspect` uses `@Around` to automatically log method entry/exit for all `@Service`, `@Repository`, and `@RestController` beans. `@AfterThrowing` catches exceptions. The `@Pointcut` annotation defines which methods are intercepted. This means adding a new service automatically gets logging without any extra code.

**Q30: How does the `PagedResponse` utility work?**
> `PagedResponse.fromEntityPage(page, mapper)` converts a Spring Data `Page<Entity>` to a `PagedResponse<DTO>`. It applies the mapper function to each entity (converting to DTO), then wraps the result list with pagination metadata: `pageNumber`, `pageSize`, `totalElements`, `totalPages`, `first`, `last`. The frontend uses these to implement pagination controls.

## 🎯 Scenario-Based Questions

**Q31: What happens if two users try to like the same post at the exact same time?**
> The `UNIQUE CONSTRAINT` on `likes(user_id, post_id)` ensures at most one like per user per post. If both reach the application layer check `existsByUserIdAndPostId()` simultaneously, one insert succeeds and the other fails with a **constraint violation**. The `likeCount` increment uses `@Transactional` which provides **row-level locking** on the post record, ensuring the count is correctly incremented.

**Q32: What happens when a user deletes their account?**
> The `User` entity has `CascadeType.ALL` on relationships to `posts`, `comments`, `likes`, `bookmarks`, `messages`, `notifications`, and `settings`. When a user is deleted, JPA cascades the deletion to all related entities. The `@OneToOne` relationships also have `orphanRemoval = true` for `settings` and `businessProfile`.

**Q33: How would you add real-time notifications (WebSocket)?**
> The project already has `WebSocketConfig.java` and `WebSocketController.java` in the codebase. To enable real-time push notifications: (1) Configure STOMP over WebSocket, (2) When `notificationService.notifyLike()` creates a notification, also send it via WebSocket: `messagingTemplate.convertAndSendToUser(userId, "/queue/notifications", notification)`, (3) Frontend subscribes to the WebSocket topic and updates the UI in real-time.

**Q34: How would you scale this application to handle millions of users?**
> (1) **Database**: Add read replicas, partition large tables (posts, notifications), use Redis for caching feed data. (2) **Backend**: Extract high-traffic services (notifications, feed) into microservices. Use message queues (Kafka/RabbitMQ) for async notification delivery. (3) **Feed**: Pre-compute and cache personalized feeds (fan-out on write). (4) **Search**: Replace LIKE queries with Elasticsearch. (5) **Media**: Use CDN (CloudFront) for media delivery. (6) **Scheduling**: Replace in-memory `ScheduledExecutorService` with persistent Quartz/Celery.

**Q35: What are the potential security vulnerabilities and how have you addressed them?**
> (1) **SQL Injection** → Prevented by using Spring Data JPA parameterized queries (`:param` syntax). (2) **XSS** → Angular sanitizes template bindings by default. (3) **CSRF** → Not applicable (JWT-based auth, no cookies). (4) **Password Exposure** → BCrypt hashing, DTOs exclude password field. (5) **JWT Theft** → Short expiry (24h), token stored in `localStorage` (not cookies). (6) **Privilege Escalation** → Ownership checks in every service method (e.g., "You can only edit your own posts").

---

# 🎯 SUMMARY

## Key Takeaways for Interview:
1. **Architecture**: Monolithic, 3-layer (Controller → Service → Repository), Spring Boot + Angular
2. **Security**: JWT + Spring Security, BCrypt passwords, stateless auth, interceptor + guard on frontend
3. **Database**: MySQL + Hibernate with auto-schema, denormalized counters, strategic indexes
4. **Features**: 12+ major features covering social media essentials
5. **Design Patterns**: Builder, DTO, Repository, Dependency Injection, AOP, Interceptor
6. **Code Quality**: Lombok (reduce boilerplate), Swagger (API docs), generic response wrappers

## Total API Count: **90+ REST endpoints** across 16 controllers
## Total Entities: **17+ JPA entities** with complex relationships
## Frontend: **Angular 19** with standalone components, lazy loading, interceptors, guards

---
*Generated for RevConnect Interview Preparation — Complete Guide*
