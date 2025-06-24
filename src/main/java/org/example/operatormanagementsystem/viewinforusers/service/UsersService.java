package org.example.operatormanagementsystem.viewinforusers.service;

import org.example.operatormanagementsystem.viewinforusers.dto.UsersRequestDto;
import org.example.operatormanagementsystem.viewinforusers.dto.UsersResponseDto;

public interface UsersService {
     UsersResponseDto getUserInfoById(UsersRequestDto requestDto);
}