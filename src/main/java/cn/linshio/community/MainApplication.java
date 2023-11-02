package cn.linshio.community;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class MainApplication {

    //解决netty启动冲突问题
    @PostConstruct
    public void init(){
        //see Netty4Utils.setAvailableProcessors
        System.setProperty("es.set.netty.runtime.available.processors","false");
    }
    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class,args);
    }
}
