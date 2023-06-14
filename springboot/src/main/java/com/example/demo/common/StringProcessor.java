package com.example.demo.common;

import java.util.regex.*;

public class StringProcessor {

    public static String processString(String inputStr) {
        // 去掉空格
        String processedInputStr = inputStr.replaceAll("\\s+", "");

        // 提取前两个子串
        String[] substrings = processedInputStr.split("-", 3);
        String firstSubstring = substrings[0];
        String secondSubstring = substrings.length > 1 ? substrings[1] : "";

        // 处理第二个子串
        Pattern pattern = Pattern.compile("[^a-zA-Z0-9-]+");
        Matcher matcher = pattern.matcher(secondSubstring);
        String processedSecondSubstring = matcher.replaceAll("");
        if (processedSecondSubstring.contains("-")) {
            processedSecondSubstring = processedSecondSubstring.split("-")[0];
        }

        // 将结果拼接成一个字符串
        return String.join("-", firstSubstring, processedSecondSubstring);
    }
}