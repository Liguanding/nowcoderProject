package com.newcoder.community.controller;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.newcoder.community.annotation.LoginRequired;
import com.newcoder.community.entity.User;
import com.newcoder.community.service.FollowService;
import com.newcoder.community.service.LikeService;
import com.newcoder.community.service.OssService;
import com.newcoder.community.service.UserService;
import com.newcoder.community.util.CommunityConstant;
import com.newcoder.community.util.CommunityUtil;
import com.newcoder.community.util.ConstantPropertiesUtils;
import com.newcoder.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController {

    private static final Logger  logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    @Autowired
    private OssService ossService;

    @LoginRequired
    @RequestMapping(path = "/setting",method = RequestMethod.GET)
    public String getSettingPage(Model model){

        return "/site/setting";
    }

    @LoginRequired
    @RequestMapping(path = "/upload/oss",method = RequestMethod.POST)
    public String uploadHeaderOss(MultipartFile headerImage, Model model, RedirectAttributes attr){
        if(headerImage == null){
            model.addAttribute("error","????????????????????????");
            return "/site/setting";
        }


        String endpoint = ConstantPropertiesUtils.END_POINT;
        String accessKeyId = ConstantPropertiesUtils.ACCESS_KEY_ID;
        String accessKeySecret = ConstantPropertiesUtils.ACCESS_KEY_SECRET;
        String bucketName = ConstantPropertiesUtils.HEADER_BUCKET_NAME;

        try {
            // ??????OSS?????????
            OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

            //???????????????????????????
            InputStream inputStream = headerImage.getInputStream();
            //??????????????????
            String fileName = headerImage.getOriginalFilename();

            //1 ??????????????????????????????????????????
            fileName = CommunityUtil.generateUUID() + fileName;

            //2 ?????????????????????????????????
            //??????????????????(??????????????????????????????
            //   2019/11/12
            String datePath = new DateTime().toString("yyyy/MM/dd");
            //??????
            fileName = datePath + "/" + fileName;

            //??????oss??????????????????
            //???????????????  Bucket??????
            //???????????????  ?????????oss???????????????????????????   aa/bb/1.jpg
            //???????????????  ?????????????????????
            ossClient.putObject(bucketName, fileName, inputStream);

            // ??????OSSClient???
            ossClient.shutdown();
            // ??????????????????
            User user = hostHolder.getUser();

            //?????????????????????????????????
            //???????????????????????????oss????????????????????????
            //  https://edu-guli-1010.oss-cn-beijing.aliyuncs.com/01.jpg
            String headerUrl = "https://" + bucketName + "." + endpoint + "/" + fileName;

            int rows = userService.updateHeader(user.getId(), headerUrl);
            https://community-header-lgd.oss-cn-beijing.aliyuncs.com/2022/03/12/13df2c846e624f98a3c347e28f9eea171.png
            if (rows > 0) {
                // ?????????????????????
                attr.addFlashAttribute("uploadMsg", "??????????????????");
            }
            return "redirect:/user/setting";
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    //??????
    @LoginRequired
    @RequestMapping(path = "/upload",method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model){
        if(headerImage == null){
            model.addAttribute("error","????????????????????????!");
            return "/site/setting";
        }
        String filename = headerImage.getOriginalFilename();
        String suffix = filename.substring(filename.lastIndexOf("."));
        if(StringUtils.isBlank(suffix)){
            model.addAttribute("error","????????????????????????!");
        }

        //?????????????????????
        filename = CommunityUtil.generateUUID() + suffix;
        //??????????????????
        File dest = new File(uploadPath + "/" + filename);
        try {
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("??????????????????: " + e.getMessage());
            throw new RuntimeException("??????????????????,?????????????????????!",e);
        }

        //??????????????????????????????
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/" + filename;
        userService.updateHeader(user.getId(),headerUrl);
        return "redirect:/index";
    }

    //??????
    @RequestMapping(path = "/header/{fileName}",method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response){
        //?????????????????????
        fileName = uploadPath + "/" + fileName;
        //????????????
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        //????????????
        response.setContentType("image/" + suffix);
        try(
            OutputStream os = response.getOutputStream();
            FileInputStream fis = new FileInputStream(fileName);
            ) {
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fis.read(buffer)) != -1){
                os.write(buffer,0,b);
            }
        }catch (IOException e){
            logger.error("??????????????????: " + e.getMessage());
        }
    }

    @LoginRequired
    @RequestMapping(path = "/updatePassword",method = RequestMethod.POST)
    public String resetPassword(Model model,String oldPassword,String newPassword){
        User user = hostHolder.getUser();
        Map<String,Object> map = userService.resetPassword(user,oldPassword,newPassword);

        if(map == null || map.isEmpty()){
            return "redirect:/logout";
        }else{
            model.addAttribute("oldPwdMsg", map.get("oldPwdMsg"));
            return "/site/setting";
        }
    }

    //????????????
    @RequestMapping(path = "/profile/{userId}",method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId") int userId,Model model){
        User user = userService.findUserById(userId);
        if(user == null){
            throw new RuntimeException("??????????????????!");
        }

        //??????
        model.addAttribute("user",user);
        //????????????
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount",likeCount);
        //????????????
        long followeeCount = followService.findFolloweeCount(userId, CommunityConstant.ENTITY_TYPE_USER);
        model.addAttribute("followeeCount",followeeCount);
        //????????????
        long followerCount = followService.findFollowerCount(CommunityConstant.ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount",followerCount);
        //???????????????
        boolean hasFollowed = false;
        if(hostHolder.getUser() != null){
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(),CommunityConstant.ENTITY_TYPE_USER,userId);
        }
        model.addAttribute("hasFollowed",hasFollowed);

        return "/site/profile";
    }
}
