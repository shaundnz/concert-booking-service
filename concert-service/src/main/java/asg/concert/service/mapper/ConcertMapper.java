package asg.concert.service.mapper;

import asg.concert.common.dto.ConcertDTO;
import asg.concert.service.domain.Concert;

public class ConcertMapper {

    public static ConcertDTO concertDTO(Concert c) {
        return new ConcertDTO(c.getId(), c.getTitle(), c.getImageName(), c.getBlurb());
    }

}
