package asg.concert.service.services;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.security.RolesAllowed;
import javax.persistence.*;
import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.*;

import asg.concert.common.dto.*;
import asg.concert.service.mapper.*;
import asg.concert.service.jaxrs.ConcertSubscription;
import org.hibernate.annotations.NotFound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asg.concert.service.domain.*;
import asg.concert.service.jaxrs.LocalDateTimeParam;

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

    private static final ConcurrentHashMap<Long, ArrayList<ConcertSubscription>> concertSubscriptons = new ConcurrentHashMap<>();

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

    @POST
    @Path("/login")
    public Response attemptLogin(UserDTO user) {
        User found_user = null;
        try {
            em.getTransaction().begin();
            TypedQuery<User> userQuery = em.createQuery("SELECT u FROM User u WHERE u.username = '" + user.getUsername() + "' AND u.password = '" + user.getPassword() + "'", User.class);
            found_user = userQuery.getSingleResult();
        } catch (Exception e) {
            em.getTransaction().commit();
            em.close();
            return Response
                    .status(401)
                    .build();
        }
        String authToken = UUID.randomUUID().toString();
        found_user.setAuthToken(authToken);
        em.merge(found_user);
        em.getTransaction().commit();
        em.close();
        return Response
                .ok()
                .cookie(new NewCookie("auth", authToken), new NewCookie("userId", found_user.getId().toString()))
                .build();
    }

    @POST
    @Path("/bookings")
    public Response createBooking(
            @CookieParam("userId") Cookie userId,
            @CookieParam("auth") Cookie authCookie,
            BookingRequestDTO bReq) {
        if (authCookie == null) {
            return Response
                    .status(401)
                    .build();
        }
        try {
            em.getTransaction().begin();
            TypedQuery<Concert> concertQuery = em.createQuery("SELECT c FROM Concert c WHERE c.id = '" + bReq.getConcertId() + "'", Concert.class);
            Concert found_concert = concertQuery.getSingleResult();
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
            TypedQuery<Seat> bookedQuery = em.createQuery("SELECT s FROM Seat s WHERE s.label = '" + s + "' AND s.date = '" + bReq.getDate() + "' AND s.isBooked = true", Seat.class);
            bookedList.addAll(bookedQuery.getResultList());
            TypedQuery<Seat> freeQuery = em.createQuery("SELECT s FROM Seat s WHERE s.label = '" + s + "' AND s.date = '" + bReq.getDate() + "' AND s.isBooked = false", Seat.class);
            toBook.addAll(freeQuery.getResultList());
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


        TypedQuery<User> userQuery = em.createQuery("SELECT u FROM User u WHERE u.id = '" + userId.getValue() + "'", User.class);
        User user = userQuery.getSingleResult();

        Booking newBooking = new Booking(bReq.getConcertId(), bReq.getDate(), toBook, Long.valueOf(userId.getValue()));
        em.persist(newBooking);

        em.getTransaction().commit();

        processConcertNotification(bReq.getConcertId(), bReq.getDate());

        em.close();

        return Response
                .status(201)
                .header("location", "http://localhost:10000/services/concert-service/bookings/" + newBooking.getId())
                .build();
    }

    @GET
    @Path("/bookings")
    public Response getAllBookingsForUser(
            @CookieParam("userId") Cookie userId,
            @CookieParam("auth") Cookie authCookie) {
        if (authCookie == null) {
            return Response
                    .status(401)
                    .build();
        }
        em.getTransaction().begin();
        TypedQuery<Booking> bookingQuery = em.createQuery("SELECT b FROM Booking b WHERE b.userId = '" + userId.getValue() + "'", Booking.class);
        List<Booking> bookingList = bookingQuery.getResultList();
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

    @GET
    @Path("/bookings/{id}")
    public Response getBookingById(
            @CookieParam("userId") Cookie userId,
            @PathParam("id") Long id) {

        Booking booking = null;
        try {
            em.getTransaction().begin();
            TypedQuery<Booking> bookingQuery = em.createQuery("SELECT b FROM Booking b WHERE b.id = '" + id + "'", Booking.class);
            booking = bookingQuery.getSingleResult();
        } catch (Exception e) {
            return Response
                    .status(404)
                    .build();
        } finally {
            em.getTransaction().commit();
            em.close();
        }


        if (!booking.getUserId().toString().equals(userId.getValue())) {
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

    @GET
    @Path("/seats/{date}")
    public Response getSeatByDate(
            @PathParam("date") LocalDateTimeParam dateParam,
            @QueryParam("status") String status
    ) {
        Map bookedStatus = Map.of("Unbooked", false, "Booked", true, "Any", "%");

        em.getTransaction().begin();
        TypedQuery<Seat> seatQuery = em.createQuery("SELECT s FROM Seat s WHERE s.date = '" + dateParam.getLocalDateTime() + "' AND s.isBooked LIKE '" + bookedStatus.get(status) + "'", Seat.class);
        List<Seat> seatList = seatQuery.getResultList();
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

    @POST
    @Path("/subscribe/concertInfo")
    public void subscribeToConcert(
            ConcertInfoSubscriptionDTO concertSubInfo,
            @CookieParam("auth") Cookie authCookie,
            @Suspended AsyncResponse sub) {
        // Unauthorized users prevented from subscribing, return 401
        if (authCookie == null) {
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

        concertSubscriptons.putIfAbsent(concertId, new ArrayList<>());
        concertSubscriptons.get(concertId).add(new ConcertSubscription(sub, concertSubInfo));
    }

    private void processConcertNotification(Long concertId, LocalDateTime date) {

        ArrayList<ConcertSubscription> concertSubs = concertSubscriptons.get(concertId);

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

}


