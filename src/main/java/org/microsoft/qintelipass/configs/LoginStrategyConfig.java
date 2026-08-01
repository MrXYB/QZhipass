package org.microsoft.qintelipass.configs;

import org.microsoft.qintelipass.ILoginStrategy;
import org.microsoft.qintelipass.logins.MobileCodeLoginStrategy;
import org.microsoft.qintelipass.logins.MobilePasswordStrategy;
import org.microsoft.qintelipass.services.SmsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoginStrategyConfig {
    @Bean("smsStrategy")
    public ILoginStrategy smsLoginStrategy(SmsServiceImpl smsService) {
        return new MobileCodeLoginStrategy(smsService);
    }
    @Bean("MOBILE_PWD")
    public ILoginStrategy mobilePassword() {
        return new MobilePasswordStrategy();
    }
}
