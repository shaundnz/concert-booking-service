package asg.concert.service.mapper;

import asg.concert.common.dto.BookingDTO;
import asg.concert.common.dto.SeatDTO;
import asg.concert.service.domain.Booking;
import asg.concert.service.domain.Seat;

import java.util.ArrayList;
import java.util.List;

public class BookingMapper {

    public static BookingDTO bookingDTO(Booking b) {

        List<SeatDTO> seats = new ArrayList<>();
        for (Seat seat : b.getSeats()) {
            seats.add(SeatMapper.seatDTO(seat));
        }
        
        return new BookingDTO(b.getConcertId(), b.getDate(), seats);
    }

}
