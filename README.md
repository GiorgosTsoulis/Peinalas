# Peinalas

## 🚀 Intro
Peinalas is a university project developed using Java Swing for the front-end and MySQL for the back-end. It simulates an online food delivery system, providing core functionalities like restaurant search, order management, real-time order tracking, and user account management.

This project serves as an example of applying object-oriented programming principles, user interface design with Java Swing, and database management using MySQL.

## ✨ Features
The Peinalas application includes the following features:

- 👤 User Registration: Allows users to create accounts, with email verification to ensure security.
- 🏪 Restaurant Search: Users can search for restaurants based on their location and apply filters for a more refined search.
- 🛒 Order Management: Facilitates the addition of menu items to a cart, submission of orders, and processing of payments.
- 📊 Order History: Users can view and manage their past orders.
- 📝 Account Management: Enables users to update their personal information and change their passwords.
- 📦 Real-time Order Tracking: Users can track the status of their orders as they progress from preparation to delivery.
- 🧑‍🍳 Menu Management: Restaurant admins can add and manage menu items.
- 💸 Promotions: Admins can create and apply promotional offers.
- 📊 Report Generation: Generate sales and order reports, which can be exported for analysis.
- 🚚Delivery Management: Features for delivery personnel to manage and track deliveries.

## 🛠️ Tech Stack
- ☕ Java (Swing GUI)
- 🗄️ MySQL (Database)
- 📦 JDBC (Database Connectivity)
- 📧 JavaMail (Email Verification & Promotions)
- 🖥️ VS Code (Development)
- 🗃️ External Libraries: `mysql-connector-j`, `javax.mail`, `activation`, `swingx`

## 📚 Learnings
- 🏗️ Object-Oriented Design (OOP) & MVC patterns
- 🖱️ Event-driven programming with ActionListeners and Timers
- 🔗 Database CRUD operations via JDBC
- 📨 Integrating JavaMail for real-world email workflows
- 🧩 Modular GUI design with reusable Swing components
- 🛡️ User authentication and data validation

## 🎥 Demo / 🖼️ Screenshot
- User Login/Logout
  <br>
  ![login-logout](https://github.com/user-attachments/assets/be070313-9307-4c98-87e2-3e559f47719e)
  <br><br>
  
- Restaurant Search
  <br>
  ![restaurant-search](https://github.com/user-attachments/assets/de0cf359-7a83-4dd0-93c7-46307c9fdc9a)
  <br><br>

- Order
  <br>
  ![order](https://github.com/user-attachments/assets/3664bc78-e93c-4415-9233-95960f06e114)
  <br><br>

- Profile Edit
  <br>
  ![profile-edit](https://github.com/user-attachments/assets/e8afc521-91d8-401d-9253-9ffd580ca85e)
  <br><br>

- Staff confirms order
  <br>
  ![confirm-order](https://github.com/user-attachments/assets/71aa4745-d77a-40cc-aa0d-159962f14a82)

- Coupon Generation
  <br>
  ![coupon-generation](https://github.com/user-attachments/assets/21cef114-57df-4b25-bd95-18a5b92cd9d7)
  <br><br>

- Delivery Staff Availabilty
  <br>
  ![delivery](https://github.com/user-attachments/assets/72ba2473-70a8-4f2d-869f-133132fcb300)
  <br><br>

## ⚙️ Installation and Setup
### Prerequisites
- Java Development Kit (JDK) (version 11 or higher)
- MySQL Server
- NetBeans IDE or Eclipse IDE

## Steps
1. Clone the repository:
bash
```bash
git clone https://github.com/yourusername/Peinalas.git
```

2. Import the Project:
- Open NetBeans or Eclipse.
- Import the cloned project as a Java project.

3. Set Up the Database:
- Open MySQL Workbench.
- Run the provided foody.sql script to set up the database schema and initial data.

4. Configure Database Connection:
- In the project, locate the database connection configuration file (e.g., DBConnection.java).
- Update the database URL, username, and password to match your MySQL setup.

5. Run the Application:
- In your IDE, compile and run the project.
- The main window of the Foody application should appear, allowing you to explore its features.
