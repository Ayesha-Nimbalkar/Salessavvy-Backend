# 🛒 SalesSavvy Backend

A robust and scalable Spring Boot backend powering the **SalesSavvy E-Commerce Platform** — designed to handle secure authentication, product management, cart operations, order processing, payment integration, and role-based admin management.

Built with a focus on clean architecture, RESTful APIs, real-world business flow handling, and production-ready backend practices.

---

# 🚀 Features

## 🔐 Authentication & Authorization
- JWT-based authentication
- Secure login & registration
- Cookie-based session handling
- Role-based access control
- Admin/User authorization filters

---

## 🛍️ Product Management
- Add products
- Delete products
- Manage stock inventory
- Product image support
- Category-based product organization

---

## 🛒 Cart System
- Add to cart
- Remove from cart
- Quantity updates
- User-specific cart persistence

---

## 📦 Order Management
- Order placement workflow
- Order item generation
- User order history
- Payment verification integration

---

## 💳 Cashfree Payment Gateway Integration
- Sandbox payment integration
- Payment verification APIs
- Success & failure handling
- Secure order confirmation flow

---

## 👨‍💼 Admin Dashboard Support
- Product management APIs
- User management APIs
- Revenue analytics APIs
- Business monitoring endpoints

---

## 📊 Business Analytics APIs
- Daily business metrics
- Monthly revenue analysis
- Yearly business reports
- Overall sales statistics

---

# 🛠️ Tech Stack

| Technology | Usage |
|---|---|
| Java 17 | Core backend language |
| Spring Boot | Backend framework |
| Spring Security | Authentication & authorization |
| Spring Data JPA | Database ORM |
| MySQL | Relational database |
| JWT | Secure authentication |
| Maven | Dependency management |
| Cashfree API | Payment gateway |
| REST APIs | Client-server communication |

---

# 📂 Project Structure

```bash
src/main/java/com/example/demo
│
├── admincontrollers
├── adminservices
├── authentication
├── controllers
├── entity
├── repository
├── services
├── security
└── config
