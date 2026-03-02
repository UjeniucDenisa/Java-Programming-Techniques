# Java Programming Techniques

This repository contains Java projects developed during the university course **Fundamental Programming Techniques** at the Technical University of Cluj-Napoca.

The projects emphasize Object-Oriented Programming, clean architectural design, GUI development, reflection, serialization, and database interaction.

---

## PT_Assignment1 – Task Management System

A desktop application designed to manage tasks assigned to employees in a software company.

### Architecture & Design
- Object-Oriented Design
- Sealed abstract classes
- Composite Design Pattern (ComplexTask composed of SimpleTask)
- Java Swing graphical user interface
- Data persistence using Serialization

### Core Concepts
- Inheritance and polymorphism
- Collections Framework (Map, List)
- Status-based task processing
- Modular class structure

### Functionalities
- Add employees
- Create simple and complex tasks
- Assign tasks to employees
- Modify task status (Completed / Uncompleted)
- Calculate employee work duration
- Generate task statistics per employee
- Filter employees with workload greater than 40 hours

---

## PT_Assignment2 – UML-Based Object-Oriented Application

A Java desktop application designed to manage and process domain-specific entities through a graphical user interface, following a structured object-oriented architecture.

### Architecture & Design
- Structured object-oriented architecture
- Clear separation of responsibilities between components
- Modular package organization
- GUI-driven interaction layer

### Core Concepts
- Encapsulation and abstraction
- Class relationships (associations and compositions)
- Separation between data handling and application logic
- Maintainable and extensible code structure

### Functionalities
- User interaction through graphical interface
- Entity management according to UML specifications
- Structured data processing through well-defined class responsibilities


---

## PT_Assignment3 – Orders Management System

A layered Java application for managing client orders within a warehouse environment, integrated with a relational database.

### Architecture
- Layered Architecture:
  - model
  - businessLayer
  - dataAccessLayer
  - presentation
- MySQL relational database
- JDBC connectivity

### Advanced Concepts
- Generic Data Access Layer using Java Reflection
- Dynamic query generation
- Immutable domain model using Java records (Bill entity)
- Lambda expressions and Streams
- JTable dynamic table generation
- Transaction handling for order processing

### Functionalities
- Add / Edit / Delete Clients
- Add / Edit / Delete Products
- Create Orders with stock validation
- Automatic stock update after order placement
- Bill generation and storage in a dedicated Log table

---

## Technologies Used
- Java
- Swing
- MySQL
- JDBC
- Reflection
- Java Streams & Lambda Expressions
- Serialization
- Object-Oriented Programming# Java-Programming-Techniques

