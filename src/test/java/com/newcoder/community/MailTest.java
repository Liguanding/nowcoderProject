package com.newcoder.community;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import com.newcoder.community.util.MailClient;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MailTest {

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

//    @Test
//    public void testTestMail(){
//        mailClient.sendMail("liguanding@126.com","Test","Welcome");
//    }
//
//    @Test
//    public void testHtmlMail(){
//        Context context = new Context();
//        context.setVariable("username","sunday");
//
//        String content = templateEngine.process("/mail/demo", context);
//        System.out.println(content);
//
//        mailClient.sendMail("liguanding@126.com","HTML",content);
//    }
}
