package asg.concert.service.mapper;

import asg.concert.common.dto.SeatDTO;
import asg.concert.service.domain.Seat;

public class SeatMapper {

    public static SeatDTO seatDTO(Seat seat) {
        return new SeatDTO(seat.getLabel(), seat.getPrice());
    }

}
