src/
├── main/
│   ├── java/
│   │   └── com/erick/freelance/        # Root Package
│   │       │
│   │       ├── config/                 # ⚙️ Mipangilio ya System
│   │       │   ├── SecurityConfig.java # JWT & Public Endpoints
│   │       │   ├── WebSocketConfig.java # Kwa ajili ya Chat
│   │       │   ├── OpenAPIConfig.java  # Swagger Documentation
│   │       │   └── CorsConfig.java     # Kuruhusu Next.js iongee na Backend
│   │       │
│   │       ├── controller/             # 🎮 API Endpoints (RestControllers)
│   │       │   ├── AuthController.java
│   │       │   ├── JobController.java
│   │       │   ├── ProposalController.java
│   │       │   ├── PaymentController.java # M-Pesa Callbacks
│   │       │   └── ChatController.java
│   │       │
│   │       ├── dto/                    # 📦 Data Transfer Objects (Usitumie Entity)
│   │       │   ├── request/            # Data zinazoingia (Inputs)
│   │       │   │   ├── LoginRequest.java
│   │       │   │   ├── JobPostRequest.java
│   │       │   │   └── HireFreelancerRequest.java
│   │       │   └── response/           # Data zinazotoka (Outputs)
│   │       │       ├── ApiResponse.java # Standard Wrapper
│   │       │       ├── JobResponse.java
│   │       │       └── UserProfileResponse.java
│   │       │
│   │       ├── entity/                 # 🗄️ Database Tables (JPA Entities)
│   │       │   ├── User.java
│   │       │   ├── Job.java
│   │       │   ├── Proposal.java
│   │       │   ├── Contract.java       # Hii ndio Escrow Table
│   │       │   ├── Wallet.java
│   │       │   ├── Transaction.java
│   │       │   └── ChatMessage.java
│   │       │
│   │       ├── repository/             # 🔍 Database Access (Interfaces)
│   │       │   ├── UserRepository.java
│   │       │   ├── JobRepository.java
│   │       │   └── ContractRepository.java
│   │       │
│   │       ├── service/                # 🧠 Business Logic (Hapa ndipo 'Akili' ilipo)
│   │       │   ├── UserService.java
│   │       │   ├── JobService.java
│   │       │   ├── PaymentService.java # Logic ya Escrow inakaa hapa
│   │       │   └── impl/               # Implementation Classes
│   │       │       ├── JobServiceImpl.java
│   │       │       └── PaymentServiceImpl.java
│   │       │
│   │       ├── mapper/                 # 🗺️ MapStruct Mappers (Entity <-> DTO)
│   │       │   ├── JobMapper.java
│   │       │   └── UserMapper.java
│   │       │
│   │       ├── exception/              # ⚠️ Global Error Handling
│   │       │   ├── GlobalExceptionHandler.java
│   │       │   ├── ResourceNotFoundException.java
│   │       │   └── InsufficientFundsException.java
│   │       │
│   │       └── security/               # 🔒 JWT Logic
│   │           ├── JwtService.java
│   │           ├── JwtAuthFilter.java
│   │           └── CustomUserDetailsService.java
│   │
│   └── resources/
│       ├── application.yml             # Database Creds & JWT Secret
│       └── messages.properties         # Error messages (Swahili/English support)