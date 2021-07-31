package com.devphilip.userservice.services;

import com.devphilip.userservice.entities.User;
import com.devphilip.userservice.exceptions.UserNotFoundException;
import com.devphilip.userservice.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    UserRepository repository;

    public Optional<User> getUser(Long id) {
        Optional<User> user = repository.findById(id);
        if (user.isEmpty()) {
            throw new UserNotFoundException(id);
        } else {
            return repository.findById(id);
        }
    }

    public Page<User> findAllUsers(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public List<User> findAllUsersUnpaged() {
        return repository.findAll();
    }

    public User updateUser(Long id, User user) {
        Optional<User> userOptional = repository.findById(id);
        if (userOptional.isEmpty()) {
            throw new UserNotFoundException(id);
        } else {
            repository.findById(id);
            return repository.save(user);
        }
    }

    public void deleteUser(Long id) {
        Optional<User> userOptional = repository.findById(id);
        if (userOptional.isEmpty()) {
            throw new UserNotFoundException(id);
        } else {
            repository.deleteById(id);
        }
    }

}
