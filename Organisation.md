## Our Team.
Our entire team worked really hard to pull of this project and did fantastically together. Everything was discussed in our group chat before any implementation occurred, at every stage of this project all group members provided their input into how features should be implemented and all options were discussed to determine the most appropiate path to take. Whenever anybody was stuck we were able to have quick group communication to work past it efficiently, the github repository facilitated this greatly as it allowed other team members to pull code and work concurrently on their own local copy to diagnose and fix problems. Once a feature was deemed complete, changes were pushed to the master branch and other group members would review the code to check for any mistakes or ways to improve it.

Cassie Burn (cbur168) - Implemented most of the base domain model and base endpoints.

Shaun Price (shaundnz) - Implemented the subscription/notification model & endpoints. Implemented signed cookies. Reviewed code.

Cameron Burton (camcamcamcamcam) - Implemented the Mapping system for Domain -> DTO objects. Implemented SQL injection protection. Reviewed code.

## Concurrency Errors
We have used Pessimistic locking as our strategy to minimise the chance of concurrency errors. Specifically we have done it to lock the queries made immediately as a booking starts to fetch the seats involved. This means that the seats involved will not be able to be accessed until the booking finishes, removing the possibility of a double booking. We have not considered locking strategies for other aspects of database, such as Concerts, Performers and Users. This is due to the fact that there is no way to modify these tables after the database has been initilized and all accesses to the database by the application are read operations. If the project was extended to provide options to create, update and delete Concerts, Performers and Users then locking strategies will have to be considered.

## Domain Model
The Domain Model is organised as a set of JPA annotated classes for persistence in our Hibernate database. We have 5 table entities: Bookings, Concerts, Performers, Seats and Users; we also have a Join Table called Concert_Performer (which contains two foreign key columns to connect a concert and performer in a many to many relationship) and a Collection Table called Concert_Dates (which contains a foreign key column to a concert and a date column to connect a concert and its dates in a one to many relationship). We use Eager loading to fetch from these tables, as we need to attach the children tables information to a serializable DTO object, we need the full reference to the object and not a proxy.

