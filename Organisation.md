## Our Team.
Our entire team worked really hard to pull of this project and did fantastically together. Everything was discussed in our group chat before any implementation occured. Whenever anybody was stuck we were able to have quick group communication to work past it efficiently

Cassie Burn (cbur168) - Implemented most of the base domain model and base endpoints.

Shaun Price (shaundnz) - Implemented the subscription/notification model & endpoints. Implemented signed cookies. Reviewed code.

Cameron Burton (camcamcamcamcam) - Implemented the Mapping system for Domain -> DTO objects. Implemented SQL injection protection. Reviewed code.

## Concurrency Errors
We have used Pessimistic locking as our strategy to minimise the chance of concurrency errors. Specifically we have done it to lock the queries made immediately as a booking starts to fetch the seats involved. This means that the seats involved will not be able to be accessed until the booking finishes, removing the possibility of a double booking.

## Domain Model
The Domain Model is organised as a set of JPA annotated classes for persistence in our Hybernate database. We have 5 table entities: Bookings, Concerts, Performers, Seats and Users; we also have a Join Table called Concert_Performer (which contains two foreign key columns to connect a concert and performer) and a Collection Table called Concert_Dates (which contains a foreign key column to a concert and a date column). We use Eager loading to fetch these tables, as we need to attach the chilren tables information to a serializable DTO object, we need the full reference to the object and not a proxy.

