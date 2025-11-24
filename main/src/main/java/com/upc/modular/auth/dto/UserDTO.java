package com.upc.modular.auth.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserDTO {
    private Long id;
    private String username;
    private String nickname;
    private Integer userType;
    private Integer status;
    private Long institutionId;
    private String userPicture;
    private LocalDateTime addDatetime;
    private String casSub;
    private String casName;
    private List<String> roles;
}