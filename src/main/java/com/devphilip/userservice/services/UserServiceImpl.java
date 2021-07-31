package com.devphilip.userservice.services;

import com.devphilip.userservice.entities.User;
import com.devphilip.userservice.exceptions.ResourceNotFoundException;
import com.devphilip.userservice.repositories.UserRepository;
import com.devphilip.userservice.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User with email: '" + email + "' Not Found"));
        return UserDetailsImpl.build(user);
    }

    public Optional<User> getUser(Long id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) resourceNotFoundException(id);
        return userRepository.findById(id);
    }

    public Page<User> findAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public List<User> findAllUsersUnpaged() {
        return userRepository.findAll();
    }

    @Cacheable(value = "userCache")
    public User updateUser(Long id, User user) {
        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isEmpty())  resourceNotFoundException(id);
        userRepository.findById(id);
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isEmpty()) resourceNotFoundException(id);
        userRepository.deleteById(id);
    }

    private void resourceNotFoundException(Long id) {
        throw new ResourceNotFoundException(String.format("Could not find user with id %s.", id));
    }

}
