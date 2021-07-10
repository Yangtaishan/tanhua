package com.tanhua.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecommendUserQueryParam implements Serializable {
    private Integer page;
    private Integer pagesize;
    private String gender;
    private String lastLogin;
    private Integer age;
    private String city;
    private String education;
}
