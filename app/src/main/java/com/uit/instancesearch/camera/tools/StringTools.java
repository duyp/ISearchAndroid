package com.uit.instancesearch.camera.tools;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class StringTools {
	private static final String ALLOWED_CHARACTERS ="0123456789qwertyuiopasdfghjklzxcvbnm";
	private static final String ALLOWED_NUMBERS="0123456789";
	
	public static String getRandomString(final int sizeOfRandomString)
	  {
	  final Random random=new Random();
	  final StringBuilder sb=new StringBuilder();
	  for(int i=0;i<sizeOfRandomString;++i)
	    sb.append(ALLOWED_CHARACTERS.charAt(random.nextInt(ALLOWED_CHARACTERS.length())));
	  return sb.toString();
	  }
	
	public static String getRandomNumberString(final int sizeOfRandomString)
	  {
	  final Random random=new Random();
	  final StringBuilder sb=new StringBuilder();
	  for(int i=0;i<sizeOfRandomString;++i)
	    sb.append(ALLOWED_CHARACTERS.charAt(random.nextInt(ALLOWED_NUMBERS.length())));
	  return sb.toString();
	  }
	
	public static byte[] compress(String str) throws IOException {
        if (str == null || str.length() == 0) {
            return null;
        }
        ByteArrayOutputStream obj=new ByteArrayOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(obj);
        gzip.write(str.getBytes("UTF-8"));
        gzip.close();
        return obj.toByteArray();
     }
	
	public static String decompress(byte[] bytes) throws Exception {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(bytes));
        BufferedReader bf = new BufferedReader(new InputStreamReader(gis, "UTF-8"));
        String outStr = "";
        String line;
        while ((line=bf.readLine())!=null) {
          outStr += line;
        }
        return outStr;
     }
	
}
