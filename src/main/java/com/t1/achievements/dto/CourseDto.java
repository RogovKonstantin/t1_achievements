package com.t1.achievements.dto;


import lombok.*;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseDto {
    private Long id;
    private UserDto user;
    private String courseName;
    private Date completionDate;
}

