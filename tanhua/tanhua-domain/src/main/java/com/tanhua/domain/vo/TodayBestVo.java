package com.tanhua.domain.vo;

import lombok.Data;

import java.io.Serializable;

/**今日佳人
 * 封装返回数据给前端
 */
@Data
public class TodayBestVo implements Serializable {
    private Integer id;
    private String avatar;
    private String nickname;
    private String gender; //性别 man woman
    private Integer age;
    private String[] tags;
    private Integer fateValue; //缘分值
}
