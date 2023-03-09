package cn.liuyong.smartlamp.vo;

import cn.liuyong.smartlamp.bean.Collection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatusVO {

    String state;

    Boolean allOff;

    LocalTime lastCommunication;

    Collection lamps;
}
