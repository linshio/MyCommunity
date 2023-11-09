package cn.linshio.community.actuator;

import cn.linshio.community.util.CommunityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Connection;

//自定义的数据监控对外暴露端点
@Component
@Endpoint(id = "database")
@Slf4j
public class DatabaseEndPoint {

    @Resource
    private DataSource dataSource;

    @ReadOperation//表示该端点使用get请求来访问
    public String checkConnection() {
        try(Connection connection = dataSource.getConnection()) {
            return CommunityUtil.getJSONString(200,"数据库连接正常");
        } catch (Exception e) {
            log.error("数据库连接异常", e);
            return CommunityUtil.getJSONString(500,"数据库连接异常");
        }
    }

}
