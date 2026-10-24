# Zam Study Point 📚

**A Full-Stack Digital Study Notes & E-Learning Platform**

Zam Study Point is a web-based platform where students can explore study materials, view course details, purchase digital notes, and securely download PDF resources after successful payment.

🔗 **Live Website:** https://zam-study-point.onrender.com  
💻 **GitHub Repository:** https://github.com/shahidzamali/zam-study-point

## ✨ Features

- 📚 Browse courses and digital study materials
- 🛍️ View product details and pricing
- 🔐 Admin panel for managing products
- 📤 Upload product PDFs and related images
- 💳 Razorpay payment gateway integration
- 📥 Secure PDF downloads after successful payment verification
- 📩 Contact form with email notifications
- 📱 Responsive user interface for desktop and mobile devices
- ☁️ Cloud-based media storage integration

## 🛠️ Technology Stack

| Technology | Purpose |
|---|---|
| Java 21 | Backend programming |
| Spring Boot | Backend application and REST APIs |
| Spring Data JPA / Hibernate | Database operations and ORM |
| MySQL | Relational database |
| Maven | Dependency management and build |
| HTML, CSS, JavaScript | Frontend development |
| Razorpay | Payment gateway integration |
| Cloudinary | Media storage integration |
| Resend API | Contact form email delivery |
| Docker | Containerization |
| Render | Application deployment |
| Aiven | Cloud-hosted MySQL database |

## ⚙️ Application Workflow

1. Students open the website and browse available study materials.
2. Students select a product and review its price and details.
3. The payment process is handled through the Razorpay integration.
4. The backend verifies payment status before allowing protected downloads.
5. Authorized customers can download their purchased PDF materials.

## 🚀 Getting Started

### Prerequisites

- Java 21
- Maven
- MySQL
- Git
- A code editor such as VS Code or IntelliJ IDEA

### 1. Clone the repository

```bash
git clone https://github.com/shahidzamali/zam-study-point.git
cd zam-study-point
```

### 2. Configure environment variables

Configure the required database, payment gateway, email service, and media storage settings using environment variables.

Example variable names:

```text
SPRING_DATASOURCE_URL
SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD
RAZORPAY_KEY_ID
RAZORPAY_KEY_SECRET
RESEND_API_KEY
RESEND_TO_EMAIL
CLOUDINARY_CLOUD_NAME
CLOUDINARY_API_KEY
CLOUDINARY_API_SECRET
```

Use your own credentials and keep all secrets private. Do not commit API keys, passwords, or production environment files to GitHub.

### 3. Run the application

Use Maven to build the project:

```bash
mvn clean package
```

Run the generated JAR file:

```bash
java -jar target/*.jar
```

Configure the required environment variables and database connection before starting the application.

## 🔒 Security

- Payment verification must be performed on the backend.
- Protected PDF files should only be accessible after successful payment verification.
- API secrets and database credentials should be stored securely as environment variables.
- Never upload customer credentials or private production configuration to a public repository.

## 🌐 Deployment

The application is deployed using Docker on Render, with a cloud-hosted MySQL database.

Live URL: https://zam-study-point.onrender.com

## 🎯 Project Objective

The objective of Zam Study Point is to provide students with a convenient platform for accessing digital study resources through a responsive interface, online payments, and secure file delivery.

## 👨‍💻 Author

**Md. Shahid Zamali**

- GitHub: [@shahidzamali](https://github.com/shahidzamali)
- LinkedIn: [Connect on LinkedIn](https://www.linkedin.com/in/md-shahid-zamali-6b0095313/)
- Portfolio: [Visit Portfolio](https://portfolio-mdshahidzamali.me)

---

⭐ If you find this project useful, consider giving the repository a star!
