package com.devphilip.userservice.services;

import com.devphilip.userservice.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;

public interface UserService extends UserDetailsService {

    UserDetails loadUserByUsername(String email) throws UsernameNotFoundException;

    Optional<User> getUser(Long id);

    Page<User> findAllUsers(Pageable pageable);

    public List<User> findAllUsersUnpaged();

    public User updateUser(Long id, User user);

    public void deleteUser(Long id);

}
