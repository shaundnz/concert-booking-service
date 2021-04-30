package asg.concert.service.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import asg.concert.common.jackson.LocalDateTimeDeserializer;
import asg.concert.common.jackson.LocalDateTimeSerializer;

@Entity
@Table(name = "Bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private Long concertId;

    private LocalDateTime date;
    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "BOOKING_ID", referencedColumnName = "id")
    private List<Seat> seats = new ArrayList<>();

    public Booking() {
    }

    public Booking(Long concertId, LocalDateTime date, List<Seat> seats) {
        this.id = null;
        this.concertId = concertId;
        this.date = date;
        this.seats = seats;
    }

    public Booking(Long concertId, LocalDateTime date, List<Seat> seats, Long userId) {
        this.id = null;
        this.concertId = concertId;
        this.date = date;
        this.seats = seats;
        this.userId = userId;
    }
       
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getConcertId() {
        return concertId;
    }

    public void setConcertId(Long concertId) {
        this.concertId = concertId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public List<Seat> getSeats() {
        return seats;
    }

    public void setSeats(List<Seat> seats) {
        this.seats = seats;
    }
}

