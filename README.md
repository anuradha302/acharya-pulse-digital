# 📱 Digital Campus

### Smart Campus Ordering & Delivery Platform

Digital Campus is a smart campus application designed to make food, stationery, and other essential campus services more convenient for students. Instead of students walking to shops and waiting in queues during short breaks, they can place orders directly from their classrooms and choose between shop pickup or desk delivery.

## 🚀 Features

### 👨‍🎓 Student
- Student login and authentication
- Browse campus canteens and shops
- View menus and product availability
- Order food and stationery
- Shop pickup
- Desk delivery when a rider is available
- Online payment
- Cash on Delivery
- Cash at Shop
- Order preparation-time tracking
- QR-based order collection
- Real-time order status
- Order history
- Feedback and rating system

### 🏪 Shopkeeper
- Secure shopkeeper login
- Manage shop open/closed status
- Add, edit and remove products
- Update product availability
- Manage menus and items
- View incoming orders
- View payment status
- Set preparation time
- Manage delivery fee
- View pickup/collection information
- Track rider assignment

### 🛵 Rider
- Rider login
- Online/Offline availability
- View available deliveries
- Accept delivery orders
- View assigned orders
- Pick up orders
- Start delivery
- Mark orders as delivered
- Cash collection for COD orders
- Delivery status updates

## 🔄 Order Flow

Student → Select Shop → Select Products → Place Order → Choose Shop Pickup / Desk Delivery → Payment → Shopkeeper Receives Order → Preparation → Ready for Pickup / Rider Delivery → QR Collection or Delivery → Order Completed → Feedback

## 🛠️ Tech Stack

- Kotlin
- Jetpack Compose
- Android Studio
- Supabase
- PostgreSQL
- Supabase Authentication
- Git
- GitHub

## 🏗️ Application Architecture

The application consists of three main user roles:

Student → Supabase ← Shopkeeper
                 ↑
               Rider

Supabase acts as the backend and database layer for authentication, orders, products, shops, payments, and delivery information.

## 💡 Problem Statement

Students often have limited break time on campus. Walking to the canteen or stationery shop, waiting in queues, and returning to class can consume most of that time.

Digital Campus aims to solve this problem by bringing campus ordering and delivery directly to the student's classroom.

## 🎯 Objective

- Reduce waiting time for students
- Make campus ordering convenient
- Digitize campus shop management
- Provide an organized delivery system
- Improve communication between students, shops, and riders
- Create a centralized campus service platform

## 📦 Campus Services

- 🍔 Canteen
- 📚 Stationery
- 🛒 Campus Mart
- 🧴 Essential Products
- 🩷 Sanitary Products

## 🔐 Role-Based Access

| Role | Main Access |
|------|-------------|
| Student | Browse, order, pay, track orders and provide feedback |
| Shopkeeper | Manage shop, products, orders and preparation |
| Rider | Manage availability and deliveries |

## 📱 Main Screens

- Login
- Role Selection
- Student Dashboard
- Canteen
- Stationery
- Campus Mart
- Sanitary Products
- My Orders
- Shopkeeper Dashboard
- Rider Dashboard
- Payment Selection
- Order Tracking
- QR Collection
- Feedback

## 🔮 Future Improvements

- Live rider location tracking
- Push notifications
- Distance-based delivery pricing
- Advanced shopkeeper analytics
- Order scheduling
- Digital receipts
- Multiple campus support
- AI-based demand prediction
- Automated rider assignment

## 👩‍💻 Developer

**Anuradha Sharma**

Developed as an individual Android application project focused on improving campus convenience through digital ordering and delivery.

## 📄 License

This project is developed for educational and project purposes.
