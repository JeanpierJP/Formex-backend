package com.superinka.formex.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class UserMeResponse {

    private Long id;
    private String email;
    private String fullName;
    private List<String> roles;
}

