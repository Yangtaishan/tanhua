package com.tanhua.domain.db;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User extends BasePojo {
    private Long id;
    private String mobile;//手机号
    private String password;//密码
    //private Date created;
    //private Date updated;

}
