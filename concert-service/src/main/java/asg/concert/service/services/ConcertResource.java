package asg.concert.service.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.persistence.*;
import javax.ws.rs.*;
import javax.ws.rs.core.*;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asg.concert.service.domain.*;

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

    @GET
    @Path("/concerts/{id}")
    public Response getSingleConcert(
        @PathParam("id") Long id) {
        em.getTransaction().begin();
        Concert concert = em.find(Concert.class, id);
        em.close();
        if (concert != null) {
            return Response
                .ok(concert)
                .build();
        }
        else {
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
        em.close();
        return Response
            .ok(concertList)
            .build();
    }

    @GET
    @Path("/concerts/summaries")
    public Response getConcertSummaries() {
        em.getTransaction().begin();
        TypedQuery<ConcertSummary> concertQuery = em.createQuery("select new asg.concert.service.domain.ConcertSummary(c.id, c.title, c.imageName) from Concert c", ConcertSummary.class);
        List<ConcertSummary> concertList = concertQuery.getResultList();
        em.close();
        return Response
            .ok(concertList)
            .build();
    }    

    @GET
    @Path("/performers/{id}")
    public Response getSinglePerformer(
        @PathParam("id") Long id) {
        em.getTransaction().begin();
        Performer performer = em.find(Performer.class, id);
        em.close();
        if (performer != null) {
            return Response
                .ok(performer)
                .build();
        }
        else {
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
        em.close();
        return Response
            .ok(performerList)
            .build();
    }

    @POST
    @Path("/login")
    public Response attemptLogin(User user) {
        try {
            em.getTransaction().begin();
            TypedQuery<User> userQuery = em.createQuery("SELECT u FROM User u WHERE u.username = '" + user.getUsername() + "' AND u.password = '" + user.getPassword() + "'", User.class);
            User found_user = userQuery.getSingleResult();
            em.close();
        }
        catch (Exception e) {
            em.close();
            return Response
                .status(401)
                .build();
        }
    
        return Response
            .ok()
            .cookie(new NewCookie("auth", UUID.randomUUID().toString()))
            .build();
    }

}
