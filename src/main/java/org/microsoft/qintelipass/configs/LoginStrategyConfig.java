package org.microsoft.qintelipass.configs;

import org.microsoft.qintelipass.ILoginStrategy;
import org.microsoft.qintelipass.services.auth.SmsServiceImpl;
import org.microsoft.qintelipass.services.logins.EmailPasswordStrategy;
import org.microsoft.qintelipass.services.logins.MobileCodeLoginStrategy;
import org.microsoft.qintelipass.services.logins.MobilePasswordStrategy;
import org.microsoft.qintelipass.services.logins.WechatLoginStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoginStrategyConfig {
    @Bean("mobile")
    public ILoginStrategy smsLoginStrategy(SmsServiceImpl smsService) {
        return new MobileCodeLoginStrategy(smsService);
    }
    @Bean("MOBILE_PWD")
    public ILoginStrategy mobilePassword() {
        return new MobilePasswordStrategy();
    }

    @Bean("wechatLogin")
    public ILoginStrategy wechatLogin(){
        return new WechatLoginStrategy();
    }
    @Bean("EMAIL_PWD")
    public ILoginStrategy emailPasswordLogin(){
        return new EmailPasswordStrategy();
    }
}
