DROP DATABASE IF EXISTS FoodOrderDB;
CREATE DATABASE FoodOrderDB;

USE FoodOrderDB;

CREATE TABLE Stores(
    store_id INT AUTO_INCREMENT,
    name VARCHAR(255),
    staff_count INT DEFAULT 0,
    location VARCHAR(255),
    kitchenCategory VARCHAR(255),
    priceCategory ENUM('Cheap eats', 'Mid-range', 'Fine dining'),
    phone_number VARCHAR(20),
    website VARCHAR(255),
    opening_hours VARCHAR(255),
    PRIMARY KEY(store_id)
);

CREATE TABLE Users(
    user_id INT AUTO_INCREMENT,
    username VARCHAR(50),
    password VARCHAR(255), -- Consider hashing passwords
    role ENUM('Customer', 'Staff', 'Delivery', 'Admin'),
    firstname VARCHAR(255),
    lastname VARCHAR(255),
    address VARCHAR(255),
    address_number VARCHAR(50),
    post_code VARCHAR(50),
    country VARCHAR(50),
    age INT,
    number VARCHAR(20),
    email VARCHAR(255),
    gender ENUM('Male', 'Female', 'Other'),
    store_id INT,
    position VARCHAR(50),
    PRIMARY KEY(user_id),
    FOREIGN KEY(store_id) REFERENCES Stores(store_id)
);

CREATE TABLE Staff (
    staff_id INT AUTO_INCREMENT,
    user_id INT,
    store_id INT NOT NULL,
    position VARCHAR(50) NOT NULL,
    PRIMARY KEY(staff_id),
    FOREIGN KEY(user_id) REFERENCES Users(user_id),
    FOREIGN KEY(store_id) REFERENCES Stores(store_id)
);

CREATE TABLE Delivery (
    delivery_id INT AUTO_INCREMENT,
    user_id INT,
    vehicle VARCHAR(20) NOT NULL,
    license_plate VARCHAR(20) NOT NULL,
    PRIMARY KEY(delivery_id),
    FOREIGN KEY(user_id) REFERENCES Users(user_id)
);

CREATE TABLE MenuItems (
    item_id INT AUTO_INCREMENT,
    store_id INT,
    item_name VARCHAR(255),
    description TEXT,
    price DECIMAL(10, 2),
    item_category VARCHAR(255),
    quantity_of_pieces INT DEFAULT 1,
    PRIMARY KEY(item_id),
    FOREIGN KEY(store_id) REFERENCES Stores(store_id)
);

CREATE TABLE Orders (
    order_id INT AUTO_INCREMENT,
    user_id INT, -- ID of the customer who placed the order
    store_id INT, -- ID of the store where the order was placed
    order_date DATETIME DEFAULT CURRENT_TIMESTAMP, -- When the order was placed
    status ENUM('In Progress', 'Completed', 'Cancelled') DEFAULT 'In Progress', -- Current status of the order
    total_amount DECIMAL(10, 2), -- Total amount of the order
    service_type ENUM('Takeaway', 'Dine-in') NOT NULL, -- Whether the order is for takeaway or dine-in
    PRIMARY KEY(order_id),
    FOREIGN KEY(user_id) REFERENCES Users(user_id),
    FOREIGN KEY(store_id) REFERENCES Stores(store_id)
);

CREATE TABLE OrderItems (
    order_item_id INT AUTO_INCREMENT,
    order_id INT,
    item_id INT,
    user_id INT,  -- Adding user_id to track which user ordered the item
    quantity INT,
    price DECIMAL(10, 2),
    PRIMARY KEY(order_item_id),
    FOREIGN KEY(order_id) REFERENCES Orders(order_id),
    FOREIGN KEY(item_id) REFERENCES MenuItems(item_id),
    FOREIGN KEY(user_id) REFERENCES Users(user_id)  -- Setting up the foreign key relation
);


INSERT INTO Stores(name, location, kitchenCategory, priceCategory, phone_number, website, opening_hours) VALUES
('The Cozy Corner', 'San Francisco', 'American', 'Cheap eats', '415-123-4567', 'www.cozycorner.com', '8:00 AM - 8:00 PM'),
('Bistro Bella', 'New York', 'French', 'Fine dining', '212-987-6543', 'www.bistrobella.com', '6:00 PM - 11:00 PM'),
('Sushi World', 'San Diego', 'Japanese', 'Mid-range', '619-234-5678', 'www.sushiworld.com', '11:00 AM - 10:00 PM'),
('Taco Haven', 'San Antonio', 'Mexican', 'Cheap eats', '210-876-5432', 'www.tacohaven.com', '9:00 AM - 9:00 PM'),
('Cafe Mocha', 'Seattle', 'Coffee', 'Mid-range', '206-765-4321', 'www.cafemocha.com', '7:00 AM - 7:00 PM'),
('Dragon Palace', 'Las Vegas', 'Chinese', 'Fine dining', '702-345-6789', 'www.dragonpalace.com', '5:00 PM - 12:00 AM'),
('Vegan Delight', 'Portland', 'Vegan', 'Cheap eats', '503-543-2109', 'www.vegandelight.com', '10:00 AM - 8:00 PM'),
('Pasta Paradiso', 'Chicago', 'Italian', 'Mid-range', '312-987-1234', 'www.pastaparadiso.com', '11:30 AM - 10:00 PM'),
('Tex-Mex Grill', 'Dallas', 'Mexican', 'Mid-range', '214-654-3210', 'www.texmexgrill.com', '11:00 AM - 10:00 PM'),
('Green Garden', 'Austin', 'Vegan', 'Fine dining', '512-345-6789', 'www.greengarden.com', '5:30 PM - 11:00 PM'),
('The Great Escape', 'Miami', 'American', 'Mid-range', '305-123-7890', 'www.greatescape.com', '12:00 PM - 10:00 PM'),
('Szechuan Spice', 'San Francisco', 'Chinese', 'Mid-range', '415-234-6789', 'www.szechuan-spice.com', '11:00 AM - 10:00 PM'),
('Harvest Bistro', 'Denver', 'American', 'Fine dining', '303-567-8901', 'www.harvestbistro.com', '6:00 PM - 11:00 PM'),
('El Mexicano', 'Houston', 'Mexican', 'Cheap eats', '713-987-6543', 'www.elmexicano.com', '9:00 AM - 9:00 PM'),
('The Sushi Bar', 'Los Angeles', 'Japanese', 'Cheap eats', '310-876-5432', 'www.sushibar.com', '11:00 AM - 10:00 PM'),
('Le Gourmet', 'New York', 'French', 'Mid-range', '212-456-7890', 'www.legourmet.com', '6:00 PM - 10:00 PM'),
('The Diner', 'Philadelphia', 'American', 'Cheap eats', '215-123-9876', 'www.thediner.com', '7:00 AM - 9:00 PM'),
('Seafood Shack', 'San Diego', 'Seafood', 'Fine dining', '619-567-4321', 'www.seafoodshack.com', '6:00 PM - 11:00 PM'),
('Gourmet Garden', 'San Francisco', 'Vegan', 'Mid-range', '415-678-2345', 'www.gourmetgarden.com', '12:00 PM - 9:00 PM'),
('Ristorante Roma', 'Chicago', 'Italian', 'Fine dining', '312-876-5432', 'www.ristoranteroma.com', '6:30 PM - 11:00 PM');



INSERT INTO Users(username, password, role, firstname, lastname, address, address_number, post_code, country, age, number, email, gender) VALUES
('George', '5690', 'Admin', 'George', 'Smith', '123 Main St', '1A', '12345', 'USA', 30, '1234567890', 'george@example.com', 'Male'),
('Savvas', '9780', 'Staff', 'Savvas', 'Johnson', '456 Elm St', '2B', '67890', 'USA', 25, '0987654321', 'savvas@example.com', 'Male'),
('Peter', '4560', 'Delivery', 'Peter', 'Brown', '789 Oak St', '3C', '11223', 'USA', 28, '1122334455', 'peter@example.com', 'Male'),
('Kate', '8020', 'Customer', 'Kate', 'Davis', '321 Pine St', '4D', '44556', 'USA', 22, '6677889900', 'kate@example.com', 'Female');


INSERT INTO Staff (user_id, store_id, position) VALUES
(2, 1, 'Manager');

INSERT INTO Delivery (user_id, vehicle, license_plate) VALUES
(3, 'Car', 'ABC123');



INSERT INTO MenuItems (store_id, item_name, description, price, item_category) VALUES
(1, 'Classic Burger', 'A juicy beef burger with lettuce, tomato, and cheese.', 8.99, 'Main Course'),
(1, 'Cheese Fries', 'Crispy fries topped with melted cheddar cheese.', 4.99, 'Appetizer'),
(1, 'Vanilla Milkshake', 'A creamy vanilla-flavored milkshake.', 3.99, 'Dessert'),
(2, 'Coq au Vin', 'Chicken braised with wine, mushrooms, and onions.', 18.99, 'Main Course'),
(2, 'Escargot', 'Snails cooked in garlic butter.', 12.99, 'Appetizer'),
(2, 'Crème Brûlée', 'Rich custard topped with a layer of hard caramel.', 7.99, 'Dessert'),
(3, 'Salmon Nigiri', 'Fresh salmon served over seasoned sushi rice.', 6.99, 'Main Course'),
(3, 'Tuna Roll', 'A classic tuna roll with fresh tuna and rice.', 7.99, 'Main Course'),
(3, 'Miso Soup', 'Traditional Japanese soup with tofu and seaweed.', 2.99, 'Appetizer'),
(4, 'Beef Taco', 'A soft tortilla filled with seasoned beef, lettuce, and cheese.', 3.49, 'Main Course'),
(4, 'Chicken Quesadilla', 'Grilled tortilla filled with chicken and cheese.', 6.99, 'Main Course'),
(4, 'Churros', 'Fried dough pastry sprinkled with sugar and cinnamon.', 3.99, 'Dessert'),
(5, 'Cappuccino', 'Espresso with steamed milk and a thick layer of foam.', 4.49, 'Beverage'),
(5, 'Blueberry Muffin', 'A moist muffin filled with fresh blueberries.', 2.99, 'Dessert'),
(5, 'Avocado Toast', 'Toast topped with smashed avocado and a sprinkle of chili flakes.', 5.99, 'Main Course'),
(6, 'Peking Duck', 'Crispy roasted duck served with pancakes and hoisin sauce.', 24.99, 'Main Course'),
(6, 'Sweet and Sour Pork', 'Crispy pork in a sweet and tangy sauce.', 14.99, 'Main Course'),
(6, 'Spring Rolls', 'Fried rolls filled with vegetables and meat.', 5.99, 'Appetizer'),
(7, 'Vegan Burger', 'Plant-based burger with lettuce, tomato, and vegan cheese.', 9.99, 'Main Course'),
(7, 'Quinoa Salad', 'A healthy salad with quinoa, avocado, and fresh veggies.', 8.49, 'Main Course'),
(7, 'Vegan Brownie', 'A rich and fudgy brownie made without dairy.', 3.99, 'Dessert'),
(8, 'Spaghetti Carbonara', 'Pasta with pancetta, egg, and Parmesan cheese.', 12.99, 'Main Course'),
(8, 'Lasagna', 'Layered pasta with meat sauce and cheese.', 13.99, 'Main Course'),
(8, 'Tiramisu', 'Classic Italian dessert with coffee-soaked ladyfingers.', 6.99, 'Dessert'),
(9, 'Fajitas', 'Grilled meat with onions and peppers served with tortillas.', 11.99, 'Main Course'),
(9, 'Chili Con Carne', 'Spicy stew with beef and beans.', 9.99, 'Main Course'),
(9, 'Guacamole', 'Mashed avocado with lime, onion, and cilantro.', 4.99, 'Appetizer'),
(10, 'Stuffed Peppers', 'Bell peppers stuffed with quinoa and veggies.', 10.99, 'Main Course'),
(10, 'Vegan Mac and Cheese', 'Creamy macaroni and cheese made with cashew sauce.', 8.99, 'Main Course'),
(10, 'Vegan Cheesecake', 'A smooth cheesecake made with a nut crust.', 6.99, 'Dessert'),
(11, 'BBQ Ribs', 'Slow-cooked ribs with a smoky BBQ sauce.', 15.99, 'Main Course'),
(11, 'Caesar Salad', 'Crisp romaine lettuce with Caesar dressing and croutons.', 7.99, 'Appetizer'),
(11, 'Apple Pie', 'A classic pie with spiced apples and a flaky crust.', 5.99, 'Dessert'),
(12, 'Mapo Tofu', 'Spicy tofu with minced pork and Szechuan peppercorns.', 11.99, 'Main Course'),
(12, 'Kung Pao Chicken', 'Stir-fried chicken with peanuts, vegetables, and chili peppers.', 13.99, 'Main Course'),
(12, 'Dan Dan Noodles', 'Noodles with spicy sauce and minced pork.', 9.99, 'Main Course'),
(13, 'Roast Chicken', 'Herb-roasted chicken with seasonal vegetables.', 16.99, 'Main Course'),
(13, 'Beef Wellington', 'Beef tenderloin wrapped in puff pastry.', 25.99, 'Main Course'),
(13, 'Pumpkin Soup', 'Creamy soup made with roasted pumpkin and spices.', 6.99, 'Appetizer'),
(14, 'Taco al Pastor', 'Taco with marinated pork and pineapple.', 3.99, 'Main Course'),
(14, 'Enchiladas', 'Corn tortillas rolled around a filling and covered with chili sauce.', 9.99, 'Main Course'),
(14, 'Flan', 'A creamy custard dessert with caramel sauce.', 4.99, 'Dessert'),
(15, 'California Roll', 'Crab, avocado, and cucumber roll.', 6.99, 'Main Course'),
(15, 'Tempura', 'Lightly battered and fried vegetables and seafood.', 8.99, 'Appetizer'),
(15, 'Green Tea Ice Cream', 'Creamy ice cream with a hint of green tea.', 3.99, 'Dessert'),
(16, 'Ratatouille', 'Stewed vegetable dish with tomatoes, zucchini, and eggplant.', 13.99, 'Main Course'),
(16, 'Duck Confit', 'Slow-cooked duck leg in its own fat.', 22.99, 'Main Course'),
(16, 'Madeleines', 'Small sponge cakes with a distinct shell-like shape.', 5.49, 'Dessert'),
(17, 'Pancakes', 'Fluffy pancakes served with maple syrup.', 7.99, 'Main Course'),
(17, 'Waffles', 'Crispy waffles with a side of fresh fruit.', 8.99, 'Main Course'),
(17, 'Milkshake', 'Thick milkshake made with your choice of flavor.', 4.49, 'Dessert'),
(18, 'Lobster Roll', 'Roll filled with fresh lobster meat.', 19.99, 'Main Course'),
(18, 'Clam Chowder', 'Creamy soup with clams and potatoes.', 12.99, 'Appetizer'),
(18, 'Key Lime Pie', 'Tangy pie with a graham cracker crust.', 6.99, 'Dessert'),
(19, 'Stuffed Mushrooms', 'Mushrooms filled with garlic and herbs.', 8.99, 'Appetizer'),
(19, 'Vegan Lasagna', 'Layered pasta with vegan cheese and veggies.', 12.99, 'Main Course'),
(19, 'Fruit Sorbet', 'Refreshing sorbet made with fresh fruit.', 4.99, 'Dessert'),
(20, 'Margherita Pizza', 'Classic pizza with tomato, mozzarella, and basil.', 10.99, 'Main Course'),
(20, 'Fettuccine Alfredo', 'Pasta with creamy Alfredo sauce.', 14.99, 'Main Course'),
(20, 'Gelato', 'Italian ice cream available in various flavors.', 5.99, 'Dessert');


INSERT INTO Orders (user_id, store_id, total_amount, status, service_type) VALUES
(4, 1, 17.97, 'Pending', 'Takeaway'),
(4, 2, 27.97, 'Completed', 'Dine-in'),
(4, 3, 15.97, 'In Progress', 'Takeaway'),
(4, 6, 35.97, 'Completed', 'Dine-in'),
(4, 4, 13.47, 'Pending', 'Takeaway'),
(4, 8, 33.97, 'Completed', 'Dine-in'),
(4, 7, 22.47, 'Cancelled', 'Takeaway'),
(4, 11, 29.97, 'Completed', 'Dine-in');


INSERT INTO OrderItems (order_id, item_id, user_id, quantity, price) VALUES
(1, 1, 4, 1, 8.99),
(1, 2, 4, 2, 4.99),
(1, 3, 4, 1, 3.99),
(2, 7, 4, 2, 6.99),
(2, 8, 4, 1, 7.99),
(3, 22, 4, 1, 12.99),
(3, 23, 4, 1, 13.99),
(3, 24, 4, 1, 6.99);


SELECT * FROM Users;
SELECT * FROM Stores;
SELECT * FROM MenuItems;
SELECT * FROM Staff;
SELECT * FROM Delivery;
SELECT * FROM Orders;
SELECT * FROM OrderItems;