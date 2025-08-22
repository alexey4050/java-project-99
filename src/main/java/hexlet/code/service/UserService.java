package hexlet.code.service;

import hexlet.code.dto.user.UserCreateDTO;
import hexlet.code.dto.user.UserDTO;
import hexlet.code.dto.user.UserUpdateDTO;

import java.util.List;

public interface UserService {
    List<UserDTO> getAll();
    UserDTO findById(Long id);
    UserDTO create(UserCreateDTO userCreateDTO);
    UserDTO update(UserUpdateDTO userUpdateDTO, Long id);
    void delete(Long id);
}
