# Peinalas

## Overview
Peinalas is a university project developed using Java Swing for the front-end and MySQL for the back-end. It simulates an online food delivery system, providing core functionalities like restaurant search, order management, real-time order tracking, and user account management.

This project serves as an example of applying object-oriented programming principles, user interface design with Java Swing, and database management using MySQL.

## Features
The Peinalas application includes the following features:

- User Registration: Allows users to create accounts, with email verification to ensure security.
- Restaurant Search: Users can search for restaurants based on their location and apply filters for a more refined search.
- Order Management: Facilitates the addition of menu items to a cart, submission of orders, and processing of payments.
- Order History: Users can view and manage their past orders.
- Account Management: Enables users to update their personal information and change their passwords.
- Real-time Order Tracking: Users can track the status of their orders as they progress from preparation to delivery.
- Menu Management: Restaurant admins can add and manage menu items.
- Promotions: Admins can create and apply promotional offers.
- Report Generation: Generate sales and order reports, which can be exported for analysis.
- Delivery Management: Features for delivery personnel to manage and track deliveries.

## Technology Stack
- Front-end: Java Swing
- Back-end: Java Swing, MySQL
- Development Environment: VS Code
- Database Management: MySQL Workbench

## Installation and Setup
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

## Showcase
### User Interface
- The UI is built using Java Swing, featuring intuitive forms and panels for user interactions, such as registration, searching for restaurants, and managing orders.

### Database Management
- MySQL handles data storage for users, orders, restaurants, and more. The project demonstrates how to perform CRUD operations efficiently within a Java application.

### Core Functionalities
- Restaurant Search: Search functionality is enhanced with filters like location and cuisine.
- Order Process: Users can add items to their cart, place orders, and track them in real-time.
- Admin Features: Restaurant admins have access to menu management, promotions, and order processing.
