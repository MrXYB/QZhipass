package org.microsoft.qintelipass.configs;

import org.microsoft.qintelipass.ILoginStrategy;
import org.microsoft.qintelipass.logins.EmailPasswordStrategy;
import org.microsoft.qintelipass.logins.MobileCodeLoginStrategy;
import org.microsoft.qintelipass.logins.MobilePasswordStrategy;
import org.microsoft.qintelipass.logins.WechatLoginStrategy;
import org.microsoft.qintelipass.services.RedisService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoginStrategyConfig {
    @Bean("mobile")
    public ILoginStrategy smsLoginStrategy(RedisService redisService) {
        return new MobileCodeLoginStrategy(redisService);
    }
    @Bean("MOBILE_PWD")
    public ILoginStrategy mobilePassword(){
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
