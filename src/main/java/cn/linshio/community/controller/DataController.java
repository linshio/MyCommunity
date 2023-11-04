package cn.linshio.community.controller;

import cn.linshio.community.service.DataService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import java.util.Date;

@Controller
public class DataController {

    @Resource
    private DataService dataService;

    //统计页面
    @RequestMapping(path = "/data",method = {RequestMethod.GET,RequestMethod.POST})
    public String getDataPage(){
        return "/site/admin/data";
    }
    //统计网站UV
    @PostMapping("/data/uv")
    public String getUV(@DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
                        @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
                        Model model){
        long uv = dataService.calculateUV(startDate, endDate);
        model.addAttribute("uvResult",uv);
        model.addAttribute("uvStartDate",startDate);
        model.addAttribute("uvEndDate",endDate);
        return "forward:/data";
    }
    //统计用户活跃量DAU
    @PostMapping("/data/dau")
    public String getDAU(@DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
                        @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
                        Model model){
        long dau = dataService.calculateDAU(startDate, endDate);
        model.addAttribute("dauResult",dau);
        model.addAttribute("dauStartDate",startDate);
        model.addAttribute("dauEndDate",endDate);
        return "forward:/data";
    }
}
