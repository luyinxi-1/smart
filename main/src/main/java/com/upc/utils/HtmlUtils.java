package com.upc.utils;

import org.jsoup.Jsoup;

/**
 * HTML工具类
 */
public class HtmlUtils {
    
    /**
     * 去除HTML标签，只保留纯文本
     * @param html 包含HTML标签的字符串
     * @return 纯文本
     */
    public static String stripHtml(String html) {
        if (html == null || html.isEmpty()) {
            return html;
        }
        return Jsoup.parse(html).text();
    }
}