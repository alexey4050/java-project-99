package hexlet.code.service;

import hexlet.code.model.User;
import hexlet.code.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsManager {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User with email " + email + " not found"));
        return user;
    }

    @Override
    public void createUser(UserDetails userData) {
        var user = new User();
        user.setEmail(userData.getUsername());
        var hashedPassword = passwordEncoder.encode(userData.getPassword());
        user.setPassword(hashedPassword);
        userRepository.save(user);
    }

    @Override
    public void updateUser(UserDetails user) {
        throw new UnsupportedOperationException("Unimplemented method 'updateUser'");
    }

    @Override
    public void deleteUser(String username) {
        throw new UnsupportedOperationException("Unimplemented method 'deleteUser'");
    }

    @Override
    public void changePassword(String oldPassword, String newPassword) {
        throw new UnsupportedOperationException("Unimplemented method 'changePassword'");
    }

    @Override
    public boolean userExists(String username) {
        throw new UnsupportedOperationException("Unimplemented method 'userExists'");
    }

//    public UserDTO getById(Long id) {
//        var user = userRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
//        return userMapper.map(user);
//    }
//
//    public List<UserDTO> getAll() {
//        return userRepository.findAll().stream()
//                .map(userMapper::map)
//                .toList();
//    }
//
//    public UserDTO create(UserCreateDTO userData) {
//        if (userRepository.existsByEmail(userData.getEmail())) {
//            throw new ValidationException("Email already exist");
//        }
//
//        var user = userMapper.map(userData);
//        var savedUser = userRepository.save(user);
//        return userMapper.map(savedUser);
//    }
//
//    public UserDTO update(Long id, UserUpdateDTO userData) {
//        var user = userRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
//
//        if (userData.getEmail() != null && userData.getEmail().isPresent()
//                && !userData.getEmail().get().equals(user.getEmail())) {
//            if (userRepository.existsByEmail(userData.getEmail().get())) {
//                throw new ValidationException("Email already exist");
//            }
//        }
//
//        userMapper.update(userData, user);
//        var updateUser = userRepository.save(user);
//        return userMapper.map(updateUser);
//    }
//
//    public void delete(Long id) {
//        userRepository.deleteById(id);
//    }
}
