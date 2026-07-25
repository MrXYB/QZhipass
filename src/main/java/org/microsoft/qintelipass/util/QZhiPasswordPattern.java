package org.microsoft.qintelipass.util;

import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

public class QZhiPasswordPattern {
    private static final Pattern USER_PATTERN = Pattern.compile("[A-Za-z0-9]{8,20}");
    public static class Generator {
        private static final String charSequence = "1234567890qwertyuiopasdfghjklzxcvbnm";
        private StringBuilder stringBuilder;
        public String generate(){
            this.stringBuilder = new StringBuilder();
            ThreadLocalRandom
                    .current()
                    .ints(0,charSequence.length())
                    .limit(8)
                    .forEach((i)-> stringBuilder.append(charSequence.charAt(i))
            );
            return stringBuilder.toString();
        }
    }

    public static boolean validate(String password){
        return USER_PATTERN.matcher(password).matches();
    }
}
