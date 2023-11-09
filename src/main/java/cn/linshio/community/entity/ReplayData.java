package cn.linshio.community.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

//封装的回复数据
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReplayData {
    //帖子的id
    private int id;
    // 回复的标题
    private String title;
    // 回复的内容
    private String content;
    // 回复的时间
    private Date createTime;
}
