package asg.concert.service.mapper;

import asg.concert.common.dto.BookingRequestDTO;
import asg.concert.service.domain.BookingRequest;

public class BookingRequestMapper {

    public static BookingRequestDTO bookingRequestDTO(BookingRequest b) {
        return new BookingRequestDTO(b.getConcertId(), b.getDate());
    }

}
