package com.church.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class SermonRequest {

    @NotBlank(message = "제목은 필수 입력 항목입니다.")
    private String title;

    @NotNull(message = "날짜는 필수 입력 항목입니다.")
    private LocalDate date;

    private String videoUrl;

    private String description;

    private String preacher;
}
