package asg.concert.service.mapper;

import asg.concert.common.dto.UserDTO;
import asg.concert.service.domain.User;

public class UserMapper {

    public static UserDTO userDTO(User user) {
        return new UserDTO(user.getUsername(), user.getPassword());
    }

}
