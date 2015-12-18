package com.shgc.htmlparseTest.util;

import java.util.regex.Pattern;

/**
 * Created by Administrator on 2015/12/14.
 */
public class Re {
    public static void main(String[] args){
        Pattern pattern = Pattern.compile(".*\\|2015\\d{10}.*");
        Pattern pattern1 = Pattern.compile("^.*\\|" + "长安" + "\\|.+\\|2015\\d{10}\\|.*");
        Pattern pattern2 = Pattern.compile("^.*\\|" + "长安" + "\\|" + "奔奔" + "\\|2015\\d{10}\\|.*");
        String b = "autohome|悦翔######|20091105221136|http://club.autohome.com.cn/bbs/thread-c-705-4826001-1.html|17";
        String b1 = "autohome|悦翔######|20151105221136|http://club.autohome.com.cn/bbs/thread-c-705-4826001-1.html|17";
        String b2 = "autohome|长安|2|20151105221136|http://club.autohome.com.cn/bbs/thread-c-705-4826001-1.html|17";
        String b3 = "autohome|长安|奔奔|20151105221136|http://club.autohome.com.cn/bbs/thread-c-705-4826001-1.html|17";

        System.out.println(pattern.matcher(b).matches());
        System.out.println(pattern.matcher(b1).matches());
        System.out.println(pattern1.matcher(b2).matches());
        System.out.println(pattern2.matcher(b3).matches());


    }
}
