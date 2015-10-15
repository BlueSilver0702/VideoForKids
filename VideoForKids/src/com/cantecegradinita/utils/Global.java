package com.cantecegradinita.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.os.Environment;
import android.util.DisplayMetrics;



public class Global {
	public static String app = "VideoForKids";
	public static String file_url = "http://www.cantecegradinita.ro/app/Videos.plist";
	public static String server_path = "http://www.cantecegradinita.ro/app/";
	public static File extern_dir = Environment.getExternalStorageDirectory();
	public static String file_dir = extern_dir.toString() + "/" + app + "/";
	public static String plist_path = file_dir + "list.plist";
	private static Global sharedObj = null;
	public static MediaPlayer music, effect;
	public static boolean isPlayPage = false;
	public static boolean isSettingPage = false;
	public static ArrayList<VideoDB> downloadList;
	public static boolean pausePlaying = false;
	public static String pauseVideo = "";
	public static int pauseTime = 0; 
	
	public static Global sharedInstance() {
		if (sharedObj == null) sharedObj = new Global();
		
		return sharedObj;
	}
	
	public static String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
          sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }

    public static String getStringFromFile (String filePath) throws Exception {
        File fl = new File(filePath);
        FileInputStream fin = new FileInputStream(fl);
        String ret = convertStreamToString(fin);
        //Make sure you close all streams.
        fin.close();        
        return ret;
    }
    
    public static int convertDpToPixel(int dp, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return (int)px;
    }
    
    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }
}