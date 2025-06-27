package org.example.operatormanagementsystem.viewinforusers.service.impl;

import org.example.operatormanagementsystem.viewinforusers.dto.UsersRequestDto;
import org.example.operatormanagementsystem.viewinforusers.dto.UsersResponseDto;
import org.example.operatormanagementsystem.entity.Users;
import org.example.operatormanagementsystem.viewinforusers.repository.InforUsersRepository;
import org.example.operatormanagementsystem.viewinforusers.service.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UsersServiceImpl implements UsersService {

    @Autowired
    private InforUsersRepository inforUsersRepository;

    @Override
    public UsersResponseDto getUserInfoById(UsersRequestDto requestDto) {
        Optional<Users> optionalUser = inforUsersRepository.findById(requestDto.getId());

        if (optionalUser.isEmpty()) {
            throw new RuntimeException("User not found with ID: " + requestDto.getId());
        }

        Users user = optionalUser.get();

        UsersResponseDto responseDto = new UsersResponseDto();
        responseDto.setId(user.getId());
        responseDto.setFullName(user.getFullName());
        responseDto.setEmail(user.getEmail());
        responseDto.setPhone(user.getPhone());
        responseDto.setAddress(user.getAddress());
        responseDto.setStatus(user.getStatus().name());

        return responseDto;
    }
}