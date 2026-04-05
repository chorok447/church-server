package com.church.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class NoticeRequest {

    @NotBlank(message = "제목은 필수 입력 항목입니다.")
    private String title;

    @NotNull(message = "날짜는 필수 입력 항목입니다.")
    private LocalDate date;

    private String content;

    private Boolean pinned;

    private List<Long> attachmentIds;
}
