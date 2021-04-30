package asg.concert.service.mapper;

import asg.concert.common.dto.PerformerDTO;
import asg.concert.service.domain.Performer;

public class PerformerMapper {

    public static PerformerDTO performerDTO(Performer p) {
        return new PerformerDTO(p.getId(), p.getName(), p.getImageName(), p.getGenre(), p.getBlurb());
    }

}
