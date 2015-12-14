package com.shgc.htmlparseTest.util;

import java.util.regex.Pattern;

/**
 * Created by Administrator on 2015/12/14.
 */
public class Re {
    public static void main(String[] args){
        Pattern pattern = Pattern.compile(".*\\|2015\\d{10}.*");
        String b = "autohome|悦翔######|20091105221136|http://club.autohome.com.cn/bbs/thread-c-705-4826001-1.html|17";
        String b1 = "autohome|悦翔######|20151105221136|http://club.autohome.com.cn/bbs/thread-c-705-4826001-1.html|17";

        System.out.println(pattern.matcher(b).matches());
        System.out.println(pattern.matcher(b1).matches());

    }
}
