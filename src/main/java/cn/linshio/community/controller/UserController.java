package cn.linshio.community.controller;

import cn.linshio.community.annotation.LoginRequired;
import cn.linshio.community.entity.User;
import cn.linshio.community.service.FollowService;
import cn.linshio.community.service.LikeService;
import cn.linshio.community.service.UserService;
import cn.linshio.community.util.CommunityConstant;
import cn.linshio.community.util.CommunityUtil;
import cn.linshio.community.util.HostHolder;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

@Controller
@Slf4j
@RequestMapping("/user")
public class UserController implements CommunityConstant {

    @Value("${community.path.domain}")
    private String domain;

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Resource
    private HostHolder hostHolder;

    @Resource
    private UserService userService;

    @Resource
    private LikeService likeService;

    @Resource
    private FollowService followService;

    @Value("${qiniu.key.access}")
    private String accessKey;

    @Value("${qiniu.key.secret}")
    private String secretKey;

    @Value("${qiniu.bucker.header.name}")
    private String headerBucketName;

    @Value("${qiniu.bucker.header.url}")
    private String headerBucketUrl;

    /**
     * 访问用户设置页面
     * @return
     */
    @LoginRequired
    @GetMapping("/setting")
    public String getSettings(Model model){
        //上传文件的名称
        String fileName = CommunityUtil.getRandomUUID();
        //设置响应的信息
        StringMap policy = new StringMap();
        policy.put("returnBody",CommunityUtil.getJSONString(200));
        //生成上传的凭证
        Auth auth = Auth.create(accessKey, secretKey);
        String uploadToken = auth.uploadToken(headerBucketName, fileName, 3600, policy);

        model.addAttribute("uploadToken", uploadToken);
        model.addAttribute("fileName", fileName);

        return "/site/setting";
    }

    //更新数据库中的头像的路径
    @PostMapping("/header/url")
    @ResponseBody
    public String updateHeaderUrl(String fileName){
        if (StringUtils.isBlank(fileName)){
            return CommunityUtil.getJSONString(500,"文件名不能为空");
        }
        //获取头像的url
        String url = headerBucketUrl + "/" + fileName;
        userService.updateHeaderUrl(hostHolder.getUser().getId(), url);
        return CommunityUtil.getJSONString(200,"头像路径更新成功");
    }


    /**
     *  修改用户头像(废弃)
     * @param headerImage  文件上传下载对象
     * @param model 视图控制对象
     * @return
     */
    @LoginRequired
    @Deprecated
    @PostMapping("/upload")
    public String uploadHeaderImg(MultipartFile headerImage, Model model){
        // 判空
        if (headerImage==null){
            model.addAttribute("fileNotExist","未选定文件，文件不存在");
            return "/site/setting";
        }
        //截取后缀
        String originalFilename = headerImage.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        //判断后缀
        if (StringUtils.isBlank(suffix)){
            model.addAttribute("fileFormatIsIncorrect","文件的格式不正确");
            return "/site/setting";
        }
        //生成随机前缀
        String filename = CommunityUtil.getRandomUUID() + suffix;
        //确定文件存放的路径
        File file = new File(uploadPath+"/"+filename);
        try {
            //存储文件
            headerImage.transferTo(file);
        } catch (IOException e) {
            log.error("文件上传失败，服务器发生错误"+e.getMessage());
            throw new RuntimeException(e);
        }
        //更新当前用户头像的路径 web访问的路径
        //http://localhost:8080/community/user/header/3bd4c0d557374dbd94d0b166c5fe3fe2.jpg
        String headUrl = domain+contextPath+"/user/header/" + filename;
        User user = hostHolder.getUser();
        userService.updateHeaderUrl(user.getId(),headUrl);
        return "redirect:/index";
    }

    /**
     * 向浏览器响应图片(废弃)
     * @param filename  文件名
     * @param response 响应对象
     */
    @GetMapping("/header/{filename}")
    @Deprecated
    public void getHeader(@PathVariable("filename") String filename, HttpServletResponse response){
        //服务器存放的文件路径
        filename = uploadPath +"/"+ filename;
        //文件后缀
        String suffix = filename.substring(filename.lastIndexOf("."));
        //响应图片
        response.setContentType("image/"+suffix);

        //自动关流
        try (
            FileInputStream fis = new FileInputStream(filename);
            OutputStream outputStream = response.getOutputStream()
        ){//缓冲流
            byte[] bytes = new byte[1024];
            int b = 0;
            while ((b = fis.read(bytes))!= -1){
                outputStream.write(bytes,0,b);
            }
        } catch (IOException e) {
            log.error("读取服务器头像失败"+e.getMessage());
            throw new RuntimeException(e);
        }
    }

    // todo：修改密码功能
    //1、在账号设置页面，填写原密码以及新密码，点击保存时将数据提交给服务器。@LoginRequired
    //2、服务器检查原密码是否正确，若正确则将密码修改为新密码，并重定向到退出功能，强制用户重新登录。若错误则返回到账号设置页面，给与相应提示。

    //个人主页数据
    @GetMapping("/profile/{userId}")
    public String getProfilePage(@PathVariable("userId") int userId,Model model){
        User user = userService.selectUserById(userId);
        if (user == null){
            throw new RuntimeException("该用户不存在");
        }
        model.addAttribute("user",user);
        //点赞的数量
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount",likeCount);
        //关注数量
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount",followeeCount);
        //粉丝数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount",followerCount);
        //是否已经关注
        boolean hasFollowed = false;
        //要登录才能显示是否关注
        if (hostHolder.getUser()!=null){
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(),ENTITY_TYPE_USER,userId);
        }
        model.addAttribute("hasFollowed",hasFollowed);
        return "site/profile";
    }

}
