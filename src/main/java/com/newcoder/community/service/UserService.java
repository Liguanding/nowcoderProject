package com.newcoder.community.service;

import com.newcoder.community.dao.LoginTicketMapper;
import com.newcoder.community.dao.UserMapper;
import com.newcoder.community.entity.LoginTicket;
import com.newcoder.community.entity.User;
import com.newcoder.community.util.CommunityUtil;
import com.newcoder.community.util.MailClient;
import com.newcoder.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import static com.newcoder.community.util.CommunityConstant.*;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

//    @Autowired
//    private LoginTicketMapper loginTicketMapper;

    public User findUserById(int id){
        User user = getCache(id);
        if(user == null){
            user = initCache(id);
        }
        return user;
    }

    public Map<String,Object> register(User user){
        Map<String,Object> map = new HashMap<>();

        if(user == null){
            throw new IllegalArgumentException("参数不能为空!");
        }
        if(StringUtils.isBlank(user.getUsername())){
            map.put("usernameMsg","账号不能为空！");
            return map;
        }
        if(StringUtils.isBlank(user.getPassword())){
            map.put("passwordMsg","密码不能为空！");
            return map;
        }
        if(StringUtils.isBlank(user.getEmail())){
            map.put("EmailMsg","邮箱不能为空！");
            return map;
        }

        //验证账号
        User u = userMapper.selectByName(user.getUsername());

        if(u != null){
            map.put("usernameMsg","该账号已存在!");
            return map;
        }

        u = userMapper.selectByEmail(user.getEmail());
        if(u != null){
            map.put("EmailMsg","邮箱已被注册！");
            return map;
        }
        //注册
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("http://image.newcoder.com/head/%dt.png",new Random().nextInt(1000)));
        user.setCreateTime(new Date());

        userMapper.insertUser(user);

        //激活邮件
        Context context = new Context();
        context.setVariable("email",user.getEmail());
        String url = domain + contextPath + "/activation/" + user.getId() + user.getActivationCode();
        context.setVariable("url",url);

        String content = templateEngine.process("/mail/activation",context);
        mailClient.sendMail(user.getEmail(),"激活账号",content);

        return map;
    }

    public int activation(int userId,String code){
        User user = userMapper.selectById(userId);
        if (user.getStatus() == 1) {
            return ACTIVATION_REPEAT;
        } else if (user.getActivationCode().equals(code)) {
            userMapper.updateStatus(userId, 1);
            clearCache(userId);
            return ACTIVATION_SUCCESS;
        } else {
            return ACTIVATION_FAILURE;
        }
    }

    public Map<String,Object> login(String username,String password,int expiredSeconds){
        Map<String,Object> map = new HashMap<>();
        if(StringUtils.isBlank(username)){
            map.put("usernameMsg","账号不能为空!");
            return map;
        }
        if(StringUtils.isBlank(password)){
            map.put("passwordMsg","密码不为空!");
            return map;
        }

        User user = userMapper.selectByName(username);

        if(user == null){
            map.put("usernameMsg","该账号不存在！");
            return map;
        }
        if(user.getStatus() == 0){
            map.put("usernameMsg","该账号未激活!");
            return map;
        }

        password = CommunityUtil.md5(password + user.getSalt());
        if(!user.getPassword().equals(password)){
            map.put("passwordMsg","密码不正确!");
            return map;
        }

        //生成登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));
//        loginTicketMapper.insertLoginTicket(loginTicket);
        String redisKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(redisKey,loginTicket);

        map.put("ticket",loginTicket.getTicket());
        return map;
    }

    public void logout(String ticket){
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(redisKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(redisKey,loginTicket);
//        loginTicketMapper.updateStatus(ticket,1);
    }

    public Map<String,Object> forget(String email){
        Map<String,Object> map = new HashMap<>();
        User user = userMapper.selectByEmail(email);

        if(user == null){
            return map;
        }else{
            //验证邮件
            Context context = new Context();
            context.setVariable("email",user.getEmail());
//            String url = domain + contextPath + "/activation/" + user.getId() + user.getActivationCode();
//            context.setVariable("url",url);
            String code = CommunityUtil.generateUUID().substring(0,5);
            context.setVariable("code",code);

            String content = templateEngine.process("/mail/forget",context);
            mailClient.sendMail(user.getEmail(),"重置密码的验证码",content);
            map.put("code",code);
            return map;
        }
    }

    public void resetPassword(String email,String newPassword){
        User user = userMapper.selectByEmail(email);
        clearCache(user.getId());
        userMapper.updatePassword(user.getId(),CommunityUtil.md5(newPassword + user.getSalt()));
    }

    public LoginTicket findLoginTicket(String ticket){
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(redisKey);
    }

    public int updateHeader(int userId,String headerUrl){

        int rows = userMapper.updateHeader(userId, headerUrl);
        clearCache(userId);
        return rows;
    }

    public Map<String,Object> resetPassword(User user,String oldPassword, String newPassword){
        Map<String,Object> map = new HashMap<>();

        if(StringUtils.isBlank(oldPassword)){
            map.put("oldPwdMsg","密码不为空!");
            return map;
        }
        oldPassword = CommunityUtil.md5(oldPassword + user.getSalt());
        if(!user.getPassword().equals(oldPassword)){
            map.put("oldPwdMsg","密码错误!");
            return map;
        }
        userMapper.updatePassword(user.getId(),CommunityUtil.md5(newPassword + user.getSalt()));
        clearCache(user.getId());
        return map;
    }

    public User findUserByName(String username){
        return userMapper.selectByName(username);
    }
    //1.优先从缓存中取值
    private User getCache(int userId){
        String redisKey = RedisKeyUtil.getUserKey(userId);
        User user = (User)redisTemplate.opsForValue().get(redisKey);
        return user;
    }

    //2.取不到时初始化缓存数据
    private User initCache(int userId){
        User user = userMapper.selectById(userId);
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(redisKey,user,3600, TimeUnit.SECONDS);
        return user;
    }
    //3。数据变更时删除缓存数据
    private void clearCache(int userId){
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(redisKey);
    }
}
