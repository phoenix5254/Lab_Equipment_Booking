Campus Lab & Equipment Booking with Real-Time Dispatch(CLEB)

This project is a Java-based Client/Server software system designed for the Faculty of Engineering & Computing (FENC) at the University of Technology, Jamaica (UTech, Ja.). The system, known as CLEB, allows students and staff to book and manage laboratory seats and specialized equipment across the Faculty of Engineering and Computing.

Core Functionality
  
  Booking System: Users can reserve specific lab workstations (seats) and specialized equipment like 3D printers and oscilloscopes.
  
  Real-Time Dispatch: The server pushes live status updates to all clients when reservations are approved, rejected, or cancelled.
  
  Role-Based Access: The system enforces specific permissions for STUDENT, TECHNICIAN, and ADMIN roles.

Technical Architecture
  
  Networking: A TCP/IP socket-based Client/Server system using blocking sockets and a server-side thread pool.
Persistence Layer: Implements both Native JDBC and Hibernate for database communication, which only the server can perform.
  
  Security: Features hashed passwords using PBKDF2 with per-user salts and serialized communication using UUID correlation IDs.
  
  Frontend: A Swing-based Multiple Document Interface (MDI) application featuring a listener thread to prevent GUI blocking.

Project Details
 
  Target Facilities: Lab spaces within the School of Computing & Information Technology (SCIT) and the School of Engineering (SOE).
 
  Development Tools: Built using Java 21, Maven, and Log4J2 for rolling file logs.
 
  Timeline: The project follows a four-phase milestone plan including design, networking core, GUI implementation, and final documentation
