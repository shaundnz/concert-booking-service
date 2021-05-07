package asg.concert.service.services;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.*;
import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.*;

import asg.concert.common.dto.*;
import asg.concert.service.mapper.*;
import asg.concert.service.jaxrs.ConcertSubscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asg.concert.service.domain.*;
import asg.concert.service.jaxrs.LocalDateTimeParam;

import java.security.MessageDigest;

@Produces({
        MediaType.APPLICATION_JSON,
})
@Consumes({
        MediaType.APPLICATION_JSON,
})
@Path("/concert-service")
public class ConcertResource {

    private static Logger LOGGER = LoggerFactory.getLogger(ConcertResource.class);
    private EntityManager em = PersistenceManager.instance().createEntityManager();

    private static final ConcurrentHashMap<Long, ArrayList<ConcertSubscription>> concertSubscriptions = new ConcurrentHashMap<>();
    private static final String AUTH_COOKIE = "auth";
    private static final String SECRET = "OJ2eAg4Rag8qH8Imiwn0";

    /**
     * GET /concerts/{id}
     * Get concert with specified ID
     *
     * Path Parameters:
     *  id: The ID of the concert to be retrieved
     *
     * Responses:
     *  200: Concert with ID returned
     *  404: Concert with specified ID does not exist
     */
    @GET
    @Path("/concerts/{id}")
    public Response getSingleConcert(
            @PathParam("id") Long id) {
        em.getTransaction().begin();
        Concert concert = em.find(Concert.class, id);
        em.getTransaction().commit();
        em.close();
        if (concert != null) {
            return Response
                    .ok(ConcertMapper.concertDTO(concert))
                    .build();
        } else {
            return Response
                    .status(404)
                    .build();
        }
    }

    /**
     * GET /concerts
     * Get all concerts in the database
     *
     * Responses:
     *  200: Return all concerts
     */
    @GET
    @Path("/concerts")
    public Response getAllConcerts() {
        em.getTransaction().begin();
        TypedQuery<Concert> concertQuery = em.createQuery("select c from Concert c", Concert.class);
        List<Concert> concertList = concertQuery.getResultList();
        em.getTransaction().commit();
        em.close();

        List<ConcertDTO> entity = new ArrayList<ConcertDTO>();
        for (Concert c : concertList) {
            entity.add(ConcertMapper.concertDTO(c));
        }
        
        return Response
                .ok(entity)
                .build();
    }

    /**
     * GET /concerts/summaries
     * Get summaries for all concerts, a concert summary includes only id, title and image name
     *
     * Responses:
     *  200: Return summaries for all concerts
     */
    @GET
    @Path("/concerts/summaries")
    public Response getConcertSummaries() {
        em.getTransaction().begin();
        TypedQuery<ConcertSummary> concertQuery = em.createQuery("select new asg.concert.service.domain.ConcertSummary(c.id, c.title, c.imageName) from Concert c", ConcertSummary.class);
        List<ConcertSummary> concertList = concertQuery.getResultList();
        em.getTransaction().commit();
        em.close();
        
        List<ConcertSummaryDTO> entity = new ArrayList<ConcertSummaryDTO>();
        for (ConcertSummary cs : concertList) {
            entity.add(ConcertSummaryMapper.concertSummaryDTO(cs));
        }

        return Response
                .ok(entity)
                .build();
    }

    /**
     * GET /performers/{id}
     * Get performer with specified ID
     *
     * Path Parameters:
     *  id: The ID of the performer to be retrieved
     *
     * Responses:
     *  200: Performer with ID returned
     *  404: Performer with specified ID does not exist
     */
    @GET
    @Path("/performers/{id}")
    public Response getSinglePerformer(
            @PathParam("id") Long id) {
        em.getTransaction().begin();
        Performer performer = em.find(Performer.class, id);
        em.getTransaction().commit();
        em.close();
        if (performer != null) {
            return Response
                    .ok(PerformerMapper.performerDTO(performer))
                    .build();
        } else {
            return Response
                    .status(404)
                    .build();
        }
    }

    /**
     * GET /performers
     * Get all performers in the database
     *
     * Responses:
     *  200: Return all performers
     */
    @GET
    @Path("/performers")
    public Response getAllPerformers() {
        em.getTransaction().begin();
        TypedQuery<Performer> performerQuery = em.createQuery("select p from Performer p", Performer.class);
        List<Performer> performerList = performerQuery.getResultList();
        em.getTransaction().commit();
        em.close();

        List<PerformerDTO> entity = new ArrayList<PerformerDTO>();
        for (Performer p : performerList) {
            entity.add(PerformerMapper.performerDTO(p));
        }

        return Response
                .ok(entity)
                .build();
    }

    /**
     * POST /login
     * Login user to the concert booking system, creates a signed cookie named auth if successful that a user
     * can send with future requests to authenticate themselves
     *
     * Request Body:
     *  UserDTO: Contains username and password
     *
     * Responses:
     *  200: Login successful, signed cookie for user generated and returned with response
     *  401: Unauthorized, incorrect credentials supplied
     */
    @POST
    @Path("/login")
    public Response attemptLogin(UserDTO user) {
        User found_user = null;
        try {
            em.getTransaction().begin();
            TypedQuery<User> userQuery = em.createQuery("SELECT u FROM User u WHERE u.username = :username AND u.password = :password", User.class);
            found_user = userQuery.setParameter("username", user.getUsername()).setParameter("password", user.getPassword()).getSingleResult();
        } catch (Exception e) {
            em.getTransaction().commit();
            em.close();
            return Response
                    .status(401)
                    .build();
        }
        NewCookie signedCookie = createSignedCookie(found_user.getId().toString());
        em.getTransaction().commit();
        em.close();
        return Response
                .ok()
                .cookie(signedCookie)
                .build();
    }

    /**
     * POST /bookings
     * Allow authenticated users to make a booking for a concert on a date. A booking can contain multiple seats.
     * Creates endpoint for booking at /bookings/{id}
     *
     * Cookies:
     *  auth: Signed cookie containing current logged in user id and signature
     *
     * Request Body:
     *  BookingDTO: Contains concert ID, date and list of seats for booking
     *
     * Responses:
     *  201: Booking successful, create new booking endpoint
     *  401: Unauthorized user, no booking made
     */
    @POST
    @Path("/bookings")
    public Response createBooking(
            @CookieParam("auth") Cookie authCookie,
            BookingRequestDTO bReq) {
        if (!isUserAuthenticated(authCookie)) {
            return Response
                    .status(401)
                    .build();
        }
        try {
            em.getTransaction().begin();
            TypedQuery<Concert> concertQuery = em.createQuery("SELECT c FROM Concert c WHERE c.id = :id", Concert.class);
            Concert found_concert = concertQuery.setParameter("id", bReq.getConcertId()).getSingleResult();
            if (!found_concert.getDates().contains(bReq.getDate())) {
                throw new NotFoundException("Concert not found");
            }
        } catch (Exception e) {
            em.getTransaction().commit();
            em.close();
            return Response
                    .status(400)
                    .build();
        }

        List<Seat> bookedList = new ArrayList<Seat>();
        List<Seat> toBook = new ArrayList<Seat>();
        for (String s : bReq.getSeatLabels()) {
            TypedQuery<Seat> bookedQuery = em.createQuery("SELECT s FROM Seat s WHERE s.label = :label AND s.date = :date AND s.isBooked = true", Seat.class);
            bookedList.addAll(bookedQuery.setParameter("label", s).setParameter("date", bReq.getDate()).getResultList());
            TypedQuery<Seat> freeQuery = em.createQuery("SELECT s FROM Seat s WHERE s.label = :label AND s.date = :date AND s.isBooked = false", Seat.class);
            toBook.addAll(freeQuery.setParameter("label", s).setParameter("date", bReq.getDate()).getResultList());
        }

        if (!bookedList.isEmpty()) {
            return Response
                    .status(403)
                    .build();
        }

        for (Seat s : toBook) {
            s.setIsBooked(true);
            em.merge(s);
        }


        TypedQuery<User> userQuery = em.createQuery("SELECT u FROM User u WHERE u.id = :id", User.class);
        User user = userQuery.setParameter("id", Long.parseLong(getUserId(authCookie))).getSingleResult();

        Booking newBooking = new Booking(bReq.getConcertId(), bReq.getDate(), toBook, Long.valueOf(getUserId(authCookie)));
        em.persist(newBooking);

        em.getTransaction().commit();

        processConcertNotification(bReq.getConcertId(), bReq.getDate());

        em.close();

        return Response
                .status(201)
                .header("location", "http://localhost:10000/services/concert-service/bookings/" + newBooking.getId())
                .build();
    }

    /**
     * GET /bookings
     * For an authenticated user, get all bookings made by the user
     *
     * Cookies:
     *  auth: Signed cookie containing current logged in user id and signature
     *
     * Responses:
     *  200: Response contains all booking information made by user
     *  401: Unauthenticated user, no booking information returned
     */
    @GET
    @Path("/bookings")
    public Response getAllBookingsForUser(
            @CookieParam("auth") Cookie authCookie) {
        if (!isUserAuthenticated(authCookie)) {
            return Response
                    .status(401)
                    .build();
        }
        em.getTransaction().begin();
        TypedQuery<Booking> bookingQuery = em.createQuery("SELECT b FROM Booking b WHERE b.userId = :id", Booking.class);
        List<Booking> bookingList = bookingQuery.setParameter("id", Long.parseLong(getUserId(authCookie))).getResultList();
        em.getTransaction().commit();
        em.close();

        List<BookingDTO> entity = new ArrayList<BookingDTO>();
        for (Booking b : bookingList) {
            entity.add(BookingMapper.bookingDTO(b));
        }

        return Response
                .ok(entity)
                .build();
    }

    /**
     * GET /bookings/{id}
     * Get booking information for specified ID, only allow if user is authenticated and made the booking
     *
     * Path Parameters:
     *  id: The id of the booking to be retrieved
     *
     * Cookies:
     *  auth: Signed cookie containing current logged in user id and signature
     *
     * Responses:
     *  200: Booking with id, made by user returned
     *  404: Booking does not exist, client is unauthorized to see booking or client is unauthenticated. 404 returned
     *       for all cases to prevent client seeing information about the existence of other bookings.
     */
    @GET
    @Path("/bookings/{id}")
    public Response getBookingById(
            @CookieParam("auth") Cookie authCookie,
            @PathParam("id") Long id) {

        Booking booking = null;
        try {
            em.getTransaction().begin();
            TypedQuery<Booking> bookingQuery = em.createQuery("SELECT b FROM Booking b WHERE b.id = :id", Booking.class);
            booking = bookingQuery.setParameter("id", id).getSingleResult();
        } catch (Exception e) {
            return Response
                    .status(404)
                    .build();
        } finally {
            em.getTransaction().commit();
            em.close();
        }


        if (!booking.getUserId().toString().equals(getUserId(authCookie))) {
            return Response
                    .status(403)
                    .build();
        } 

        else {
            return Response
                    .ok(BookingMapper.bookingDTO(booking))
                    .build();
        }

    }

    /**
     * GET seats/{date}
     * Get seats for a particular concert on a particular date, by seat status
     *
     * Path Parameters:
     *  date: LocalDateTime of the concert seat information requested
     *
     * Query Parameters:
     *  status: Equal to "Unbooked", "Booked", "Any", represents seat status
     *
     * Responses:
     *  200: Returns seats with specified booking status for concert on date
     *  404: Invalid date, no concert exists at this time
     */
    // #TODO Return 404 (Or 400?) for a date that has no concerts
    @GET
    @Path("/seats/{date}")
    public Response getSeatByDate(
            @PathParam("date") LocalDateTimeParam dateParam,
            @QueryParam("status") String status
    ) {

        em.getTransaction().begin();
        TypedQuery<Seat> seatQuery = null;
        switch (status) {
            case ("Unbooked") : {
                seatQuery = em.createQuery("SELECT s FROM Seat s WHERE s.date = :date AND s.isBooked = 'false'", Seat.class);
                break;
            }

            case ("Booked") : {
                seatQuery = em.createQuery("SELECT s FROM Seat s WHERE s.date = :date AND s.isBooked = 'true'", Seat.class);
                break;
            }

            case ("Any") : {
                seatQuery = em.createQuery("SELECT s FROM Seat s WHERE s.date = :date", Seat.class);
                break;
            }
        }
        
        List<Seat> seatList = seatQuery.setParameter("date", dateParam.getLocalDateTime()).getResultList();
        em.getTransaction().commit();
        em.close();

        List<SeatDTO> entity = new ArrayList<SeatDTO>();
        for (Seat s : seatList) {
            entity.add(SeatMapper.seatDTO(s));
        }

        return Response
                .ok(entity)
                .build();
    }

    /**
     * POST /subscribe/concertInfo
     * Subscribes authenticated user to concert notifications. Get notified when percentage of booked seats for
     * specified concert passes the specified threshold
     *
     * Cookies:
     *  auth: Signed cookie containing current logged in user id and signature
     *
     * Request Body:
     *  ConcertInfoSubscriptionDTO: Contains concert ID, date, and percentage that user will be notified if booked seats
     *                              for this concert exceed this percentage
     *
     * Responses:
     *  400: Bad request, at least one of concert ID and date is non existent
     *  401: Unauthenticated user, user is prevented from subscribing
     */
    @POST
    @Path("/subscribe/concertInfo")
    public void subscribeToConcert(
            ConcertInfoSubscriptionDTO concertSubInfo,
            @CookieParam("auth") Cookie authCookie,
            @Suspended AsyncResponse sub) {
        // Unauthorized users prevented from subscribing, return 401
        if (!isUserAuthenticated(authCookie)) {
            sub.resume(Response
                    .status(401)
                    .build());
            return;
        }

        // Trying to subscribe to a non existent concert or concert date, return 400
        try {
            em.getTransaction().begin();
            Concert dbConcertInstance = em.find(Concert.class, concertSubInfo.getConcertId());
            if (dbConcertInstance == null) {
                throw new EntityNotFoundException();
            }

            if (!dbConcertInstance.getDates().contains(concertSubInfo.getDate())) {
                throw new EntityNotFoundException();
            }
        } catch (EntityNotFoundException e) {
            sub.resume(Response.status(400).build());
            return;
        } finally {
            em.getTransaction().commit();
            em.close();
        }

        // Input validated, subscribe user

        Long concertId = concertSubInfo.getConcertId();

        concertSubscriptions.putIfAbsent(concertId, new ArrayList<>());
        concertSubscriptions.get(concertId).add(new ConcertSubscription(sub, concertSubInfo));
    }

    /**
     * Iterate through subscribers hashmap, if concert ID and date exists and have subscribers, check if percentage
     * threshold for bookings has been exceeded, if true, resume the response and send notification to subscriber,
     * else do nothing
     */
    private void processConcertNotification(Long concertId, LocalDateTime date) {

        ArrayList<ConcertSubscription> concertSubs = concertSubscriptions.get(concertId);

        if (concertSubs == null) {
            return;
        }

        em.getTransaction().begin();

        for (ConcertSubscription sub : concertSubs) {


            if (sub.concertSubscription.getDate().equals(date)) {


                int numBookedSeats = em.createQuery("SELECT s FROM Seat s WHERE s.isBooked = 'true'", Seat.class).getResultList().size();

                int numSeatsRemaining = 120 - numBookedSeats;

                double percentageBooked = (numBookedSeats / (double) 120) * 100;

                if (percentageBooked > sub.concertSubscription.getPercentageBooked()) {
                    sub.response.resume(Response.ok(new ConcertInfoNotificationDTO(numSeatsRemaining)).build());
                }
            }
        }
        em.getTransaction().commit();
    }


    /**
     * Create signed cookie for specified user ID, then return this cookie
     * Cookie value is userID|Signature
     */
    private NewCookie createSignedCookie(String userID) {
        try {
            String userHash = generateUserHash(userID);
            return new NewCookie(AUTH_COOKIE, userID + "|" + userHash);
        } catch (NoSuchAlgorithmException e){
            return null;
        }
    }

    /**
     * Test if user us authenticated, check for existence of "auth" cookie. If yes then compare cookie signature with
     * expected signature. If they match, user is authenticated and return true, else return false
     */
    private boolean isUserAuthenticated(Cookie authCookie) {
        if (authCookie == null) {
            return false;
        }
        String[] cookieParts = authCookie.getValue().split("\\|");
        String userID = cookieParts[0];
        String userSecret = cookieParts[1];

        try {
            String expectedSecret = generateUserHash(userID);
            return userSecret.equals(expectedSecret);
        } catch (NoSuchAlgorithmException e) {
            return false;
        }
    }

    /**
     * Extract the user ID from the auth cookie
     */
    private String getUserId(Cookie authCookie) {
        if (authCookie == null) {
            return null;
        }
        String[] cookieParts = authCookie.getValue().split("\\|");
        return cookieParts[0];
    }

    /**
     * Combine user ID and server side secret to generate hash for user which is used to sign cookie
     */
    private String generateUserHash(String userID) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        String userString = userID + SECRET;
        byte[] userHash = digest.digest(userString.getBytes(StandardCharsets.UTF_8));

        StringBuffer hexString = new StringBuffer();

        for (int i= 0; i < userHash.length; i ++) {
            String hex = Integer.toHexString(0xFF & userHash[i]);
            if (hex.length() == 1) {
                hexString.append("0");
            }
            hexString.append(hex);
        }

        return hexString.toString();
    }
}


