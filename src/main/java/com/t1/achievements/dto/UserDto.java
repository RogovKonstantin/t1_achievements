package com.t1.achievements.dto;


import lombok.*;
import java.util.Date;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
    private UUID id;
    private String fullName;
    private String position;
    private String department;
    private Integer grade;
    private Date hireDate;
    private String username;
    private String role;
}

