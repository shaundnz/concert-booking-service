package asg.concert.service.mapper;

import asg.concert.common.dto.ConcertSummaryDTO;
import asg.concert.service.domain.ConcertSummary;

public class ConcertSummaryMapper {

    public static ConcertSummaryDTO concertSummaryDTO(ConcertSummary c) {
        return new ConcertSummaryDTO(c.getId(), c.getTitle(), c.getImageName());
    }

}
