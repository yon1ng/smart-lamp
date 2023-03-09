package cn.liuyong.smartlamp.controller;

import cn.liuyong.smartlamp.bean.Collection;
import cn.liuyong.smartlamp.bean.Lamp;
import cn.liuyong.smartlamp.component.Controller;
import cn.liuyong.smartlamp.component.LampStore;
import cn.liuyong.smartlamp.vo.ResultVO;
import cn.liuyong.smartlamp.vo.StatusVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@RestController
@Slf4j
public class LampController {

    @Autowired
    Controller controller;
    @Autowired
    LampStore lampStore;

    @PostConstruct
    private void init(){
        log.info("初始化程序！");
        controller.getEventManager().addExceptionListener((event) -> {
            log.error("捕获到异常！",event.getException());
        });

        controller.getEventManager().addGatewayConnectedListener((event) -> {
            log.info("网关连接成功！");
            Collection lamps = controller.getLamps();
            if (lamps.getRaw() != null){
                log.info("初始化灯泡列表！");
                Collection original = lamps.copy();
                for (Lamp lamp : lamps) {
                    lamp.setRGBI(255, 255, 255, 255);
                }
                controller.updateLamps(lamps);
                controller.updateLamps(original);
            }else{
                log.info("网关内未发现灯泡，停止初始化！");
            }

        });
    }

    @RequestMapping("/status")
    public ResultVO status(){
        Boolean allOff = controller.areAllLightsOff() ? true : false;
        LocalTime lastCommunication = controller.getLastCommunication();
        StatusVO statusVO = new StatusVO();
        statusVO.setState("CONNECTED");
        statusVO.setAllOff(allOff);
        statusVO.setLastCommunication(lastCommunication);
        Collection lamps = controller.getLamps();
        if (lamps != null && lamps.getRaw() != null){
            statusVO.setLamps(lamps);
        }

        return ResultVO.success(statusVO,"success");
    }

    @RequestMapping("/on")
    public ResultVO on(@RequestParam(required = false,name = "deviceId") Integer deviceId, @RequestParam(required = false,name = "intensity")Integer intensity
            ,@RequestParam(required = false,name = "r")Integer r,@RequestParam(required = false,name = "g")Integer g,@RequestParam(required = false,name = "b")Integer b){
        Collection lamps = controller.getLamps();
        intensity = intensity == null ? 255 : intensity;
        r = r == null ? 255 : r;
        g = g == null ? 255 : g;
        b = b == null ? 255 : b;
        if (deviceId == null || deviceId == 0){
            for (Lamp lamp : lamps) {
                lamp.setRGBI(r, g, b, intensity);
            }
        }else{
           for (Lamp lamp : lamps) {
                if (deviceId.equals(lamp.getId())){
                    lamp.setRGBI(r, g, b, intensity);
                }
            }
        }
        controller.updateLamps(lamps);
        return ResultVO.success(null,"success");
    }

    @RequestMapping("/off")
    public ResultVO off(@RequestParam(required = false,name = "deviceId") Integer deviceId){
        Collection lamps = controller.getLamps();
        if (deviceId == null || deviceId == 0){
            for (Lamp lamp : lamps) {
                lamp.setIntensity(0);
            }
        }else{
            for (Lamp lamp : lamps) {
                if (deviceId.equals(lamp.getId())){
                    lamp.setIntensity(0);
                }
            }
        }
        controller.updateLamps(lamps);
        return ResultVO.success(null,"success");
    }
}
