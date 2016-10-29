package com.example.hua.coolweather.until;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

/**
 * Created by Administrator on 2016/10/28.
 *  InputStream转换成String 方法合集
 */

public class Inputstr2Str {
    /**
     * 利用BufferedReader实现Inputstream转换成String <功能详细描述>
     *
     * @param in
     * @return String
     */

    public static String Inputstr2Str_Reader(InputStream in, String encode)
    {

        String str = "";
        try
        {
            if (encode == null || encode.equals(""))
            {
                // 默认以utf-8形式
                encode = "utf-8";
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, encode));
            StringBuilder sb = new StringBuilder();

            while ((str = reader.readLine()) != null)
            {
                sb.append(str);
            }
            return sb.toString();
        }
        catch (UnsupportedEncodingException e1)
        {
            e1.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        LogUntil.w("coolweather", str);
        return str;
    }

    /**
     * 利用byte数组转换InputStream------->String <功能详细描述>
     *
     * @param in
     * @return
     * @see [类、类#方法、类#成员]
     */

    public static String Inputstr2Str_byteArr(InputStream in, String encode)
    {
        StringBuffer sb = new StringBuffer();
        byte[] b = new byte[1024];
        int len = 0;
        try
        {
            if (encode == null || encode.equals(""))
            {
                // 默认以utf-8形式
                encode = "utf-8";
            }
            while ((len = in.read(b)) != -1)
            {
                sb.append(new String(b, 0, len, encode));
            }
            return sb.toString();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return "";

    }

    /**
     * 利用ByteArrayOutputStream：Inputstream------------>String <功能详细描述>
     *
     * @param in
     * @return
     * @see [类、类#方法、类#成员]
     */
    public static String Inputstr2Str_ByteArrayOutputStream(InputStream in,String encode)
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] b = new byte[1024];
        int len = 0;
        try
        {
            if (encode == null || encode.equals(""))
            {
                // 默认以utf-8形式
                encode = "utf-8";
            }
            while ((len = in.read(b)) > 0)
            {
                out.write(b, 0, len);
            }
            return out.toString(encode);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return "";
    }
}
