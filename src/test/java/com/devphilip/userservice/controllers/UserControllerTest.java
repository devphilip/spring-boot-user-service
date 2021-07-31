package com.devphilip.userservice.controllers;

import com.devphilip.userservice.entities.User;
import com.devphilip.userservice.repositories.UserRepository;
import com.devphilip.userservice.services.UserService;
import com.devphilip.userservice.services.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.atLeast;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private final UserService userService = new UserServiceImpl();

    @Test
    void getAllUsers_should_return_a_list_of_users() {
        List<User> users = new ArrayList<>();
        users.add(new User());
        given(userRepository.findAll()).willReturn(users);
        List<User> expected = userService.findAllUsersUnpaged();
        assertEquals(expected, users);
        verify(userRepository).findAll();
    }

    @Test
    void getUser_should_return_user_when_found() {
        User user = new User("fname1", "lname1", "email@me.com", "password");
        user.setId(89L);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        User expectedUser = userService.getUser(user.getId()).get();

        assertSame(expectedUser, user);
        verify(userRepository, atLeast(1)).findById(user.getId());
    }


    @Test
    public void getUser_should_throw_ex_when_user_doesnt_exist() {
        Exception exception = assertThrows(RuntimeException.class, () -> {
            User user = new User("fname1", "lname1", "email@me.com", "password");
            user.setId(89L);
            given(userRepository.findById(anyLong())).willReturn(Optional.empty());
            userService.getUser(user.getId());
        });
    }

    @Test
    void updateUser_should_update_user_when_found() {
        User user = new User("fname1", "lname1", "email@me.com", "password");
        user.setId(89L);
        User newUser = new User();
        newUser.setFirstName("new_fname1");
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        userService.updateUser(user.getId(), newUser);
        verify(userRepository, atLeast(1)).save(newUser);
        verify(userRepository, atLeast(1)).findById(user.getId());
    }

    @Test()
    public void updateUser_should_throw_exception_when_user_doesnt_exist() {
        Exception exception = assertThrows(RuntimeException.class, () -> {
            User user = new User("fname1", "lname1", "email@me.com", "password");
            user.setId(89L);
            User newUser = new User();
            user.setFirstName("new_fname1");
            given(userRepository.findById(anyLong())).willReturn(Optional.empty());
            userService.updateUser(user.getId(), newUser);
        });
    }

    @Test
    void deleteUser_should_delete_user_when_found() {
        User user = new User("fname1", "lname1", "email@me.com", "password");
        user.setId(1L);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        userService.deleteUser(user.getId());
        verify(userRepository).deleteById(user.getId());
    }
}
