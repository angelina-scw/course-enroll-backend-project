package com.mycompany.myapp.service.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CourseDTO {

    private String courseName;
    private String courseContent;
    private String courseLocation;
    private Long teacherId;
}
