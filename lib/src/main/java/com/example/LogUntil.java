package com.example;

/**
 * 日志管理类 @zzh
 *
 */

public class LogUntil {
    private static final  int VERBOSE = 1;
    private static final  int DEBUG = 2;
    private static final  int INFO = 3;
    private static final  int WARN = 4;
    private static final  int ERROR = 5;
    private static final  int NOTHING = 6;
    private static final  int LEVEL = VERBOSE;

    public static void v(String tag,String msg){
        if(LEVEL <= VERBOSE){
            System.out.println(msg);
        }
    }

    public static void d(String tag,String msg){
        if(LEVEL <= DEBUG){
            System.out.println(msg);
        }
    }

    public static void i(String tag,String msg){
        if(LEVEL <= INFO){
            System.out.println(msg);
        }
    }

    public static void w(String tag,String msg){
        if(LEVEL <= WARN){
            System.out.println(msg);
        }
    }

    public static void e(String tag,String msg){
        if(LEVEL <= ERROR){
            System.out.println(msg);
        }
    }
}

