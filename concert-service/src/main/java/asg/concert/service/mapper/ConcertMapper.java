package asg.concert.service.mapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import asg.concert.common.dto.ConcertDTO;
import asg.concert.common.dto.PerformerDTO;
import asg.concert.service.domain.Concert;
import asg.concert.service.domain.Performer;

public class ConcertMapper {

    public static ConcertDTO concertDTO(Concert c) {
        ConcertDTO concert = new ConcertDTO(c.getId(), c.getTitle(), c.getImageName(), c.getBlurb());
        concert.setDates(new ArrayList<LocalDateTime>(c.getDates()));
        
        List<PerformerDTO> performers = new ArrayList<PerformerDTO>();
        for (Performer p : c.getPerformers()) {
            performers.add(PerformerMapper.performerDTO(p));
        }

        concert.setPerformers(performers);
        
        return concert;
    }

}
