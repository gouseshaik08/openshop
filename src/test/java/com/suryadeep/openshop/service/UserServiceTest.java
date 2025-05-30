package com.suryadeep.openshop.service;

import com.suryadeep.openshop.dto.request.AddressRequest;
import com.suryadeep.openshop.dto.request.UserRegisterRequest;
import com.suryadeep.openshop.dto.response.AddressResponse;
import com.suryadeep.openshop.dto.response.UserResponse;
import com.suryadeep.openshop.entity.Address;
import com.suryadeep.openshop.entity.User;
import com.suryadeep.openshop.mapper.EntityMapper;
import com.suryadeep.openshop.repository.AddressRepository;
import com.suryadeep.openshop.repository.UserRepository;
import com.suryadeep.openshop.service.implementation.UserServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private EntityMapper entityMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Mock SecurityContext
        var authentication = mock(Authentication.class);
        var securityContext = mock(SecurityContext.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(new org.springframework.security.core.userdetails.User(
                "john.doe@example.com", "password", new ArrayList<>()
        ));

        SecurityContextHolder.setContext(securityContext);
    }


    @Test
    void testGetCurrentUser() {
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setName("John Doe");
        mockUser.setEmail("john.doe@example.com");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(mockUser));
        when(entityMapper.toUserResponse(any(User.class))).thenReturn(new UserResponse());

        UserResponse userResponse = userService.getCurrentUser();

        assertNotNull(userResponse);
        verify(userRepository, times(1)).findByEmail(anyString());
        verify(entityMapper, times(1)).toUserResponse(any(User.class));
    }

    @Test
    void testGetCurrentUser_UserNotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> userService.getCurrentUser());

        assertEquals("User not found", exception.getMessage());
        verify(userRepository, times(1)).findByEmail(anyString());
        verify(entityMapper, never()).toUserResponse(any(User.class));
    }

    @Test
    void testGetCurrentUser_NullPrincipal() {
        var securityContext = SecurityContextHolder.getContext();
        when(securityContext.getAuthentication().getPrincipal()).thenReturn(null);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> userService.getCurrentUser());

        assertEquals("Principal cannot be null", exception.getMessage());
        verify(userRepository, never()).findByEmail(anyString());
        verify(entityMapper, never()).toUserResponse(any(User.class));
    }

    @Test
    void testUpdateCurrentUser() {
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setName("John Doe");
        mockUser.setEmail("john.doe@example.com");

        UserRegisterRequest userRequest = new UserRegisterRequest();
        userRequest.setUsername("Jane Doe");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(mockUser));
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(entityMapper.toUserResponse(any(User.class))).thenReturn(new UserResponse());

        UserResponse userResponse = userService.updateCurrentUser(userRequest);

        assertNotNull(userResponse);
        verify(userRepository, times(1)).findByEmail(anyString());
        verify(userRepository, times(1)).save(any(User.class));
        verify(entityMapper, times(1)).toUserResponse(any(User.class));
    }

    @Test
    void testGetAddressess() {
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setName("John Doe");
        mockUser.setEmail("john.doe@example.com");
        mockUser.setAddresses(new ArrayList<>());

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(mockUser));
        when(entityMapper.toAddressResponseList(anyList())).thenReturn(new ArrayList<>());

        List<AddressResponse> addressResponses = userService.getAddressess();

        assertNotNull(addressResponses);
        verify(userRepository, times(1)).findByEmail(anyString());
        verify(entityMapper, times(1)).toAddressResponseList(anyList());
    }

    @Test
    void testAddAddress() {
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setName("John Doe");
        mockUser.setEmail("john.doe@example.com");
        mockUser.setAddresses(new ArrayList<>());

        AddressRequest addressRequest = new AddressRequest();
        Address mockAddress = new Address();
        AddressResponse mockAddressResponse = new AddressResponse();

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(mockUser));
        when(entityMapper.toAddressEntity(any(AddressRequest.class))).thenReturn(mockAddress);
        when(addressRepository.save(any(Address.class))).thenReturn(mockAddress);
        when(entityMapper.toAddressResponse(any(Address.class))).thenReturn(mockAddressResponse);

        AddressResponse addressResponse = userService.addAddress(addressRequest);

        assertNotNull(addressResponse);
        verify(userRepository, times(1)).findByEmail(anyString());
        verify(addressRepository, times(1)).save(any(Address.class));
        verify(entityMapper, times(1)).toAddressResponse(any(Address.class));
    }

    @Test
    void testUpdateUserAddress() {
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setName("John Doe");
        mockUser.setEmail("john.doe@example.com");

        Address existingAddress = new Address();
        existingAddress.setId(1L);
        mockUser.getAddresses().add(existingAddress);

        AddressRequest addressRequest = new AddressRequest();
        addressRequest.setAddressLine("New Address Line");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(mockUser));
        when(addressRepository.save(any(Address.class))).thenReturn(existingAddress);
        when(entityMapper.toAddressResponse(any(Address.class))).thenReturn(new AddressResponse());

        AddressResponse addressResponse = userService.updateUserAddress(1L, addressRequest);

        assertNotNull(addressResponse);
        verify(userRepository, times(1)).findByEmail(anyString());
        verify(addressRepository, times(1)).save(any(Address.class));
        verify(entityMapper, times(1)).toAddressResponse(any(Address.class));
    }

    @Test
    void testDeleteUserAddress() {
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setName("John Doe");
        mockUser.setEmail("john.doe@example.com");

        Address existingAddress = new Address();
        existingAddress.setId(1L);
        mockUser.getAddresses().add(existingAddress);

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(mockUser));

        userService.deleteUserAddress(1L);

        verify(userRepository, times(1)).findByEmail(anyString());
        verify(userRepository, times(1)).save(any(User.class));
        verify(addressRepository, times(1)).delete(any(Address.class));
    }
}
