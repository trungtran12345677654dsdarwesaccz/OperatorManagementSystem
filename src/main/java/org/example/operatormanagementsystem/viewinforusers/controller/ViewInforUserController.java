package org.example.operatormanagementsystem.viewinforusers.controller;

import org.example.operatormanagementsystem.viewinforusers.dto.UsersRequestDto;
import org.example.operatormanagementsystem.viewinforusers.dto.UsersResponseDto;
import org.example.operatormanagementsystem.viewinforusers.service.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class ViewInforUserController {

    @Autowired
    private UsersService usersService;

    @GetMapping("/api/view-users/{id}")
    public ResponseEntity<?> getUserInfoById(@PathVariable Integer id) {
        try {
            UsersRequestDto requestDto = new UsersRequestDto();
            requestDto.setId(id);
            UsersResponseDto responseDto = usersService.getUserInfoById(requestDto);
            return ResponseEntity.ok(responseDto);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(404).body(ex.getMessage());
        }
    }
}
