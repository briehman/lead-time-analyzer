package com.briehman.leadtimeanalyzer.service;

import com.briehman.leadtimeanalyzer.dto.UserDto;
import com.briehman.leadtimeanalyzer.repository.UserRepository;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<UserDto> getUsers() {
        return userRepository.findAll().stream()
                .map(user -> new UserDto(user.getUsername()))
                .sorted(Comparator.comparing(UserDto::getName))
                .collect(Collectors.toList());

    }
}
