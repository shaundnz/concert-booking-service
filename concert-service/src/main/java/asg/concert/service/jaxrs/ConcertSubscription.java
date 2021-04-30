package asg.concert.service.jaxrs;

import asg.concert.common.dto.ConcertInfoSubscriptionDTO;

import javax.ws.rs.container.AsyncResponse;

public class ConcertSubscription {
    public final AsyncResponse response;
    public final ConcertInfoSubscriptionDTO concertSubscription;

    public ConcertSubscription(AsyncResponse response, ConcertInfoSubscriptionDTO concertSubscription) {
        this.response = response;
        this.concertSubscription = concertSubscription;
    }
}
