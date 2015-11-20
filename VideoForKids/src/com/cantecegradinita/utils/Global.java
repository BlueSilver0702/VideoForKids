package com.cantecegradinita.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import com.cantecegradinita.ro.MainActivity;
import com.cantecegradinita.ro.SettingActivity;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.util.DisplayMetrics;
import android.util.Log;

public class Global {
	public static String app = "VideoForKids";
	public static String file_url = "http://www.cantecegradinita.ro/app/Videos.plist";
	public static String server_path = "http://www.cantecegradinita.ro/app/";
//	public static File extern_dir = Environment.getExternalStorageDirectory();
//	public static String file_dir = extern_dir.toString() + "/" + app + "/";
//	public static String plist_path = file_dir + "list.plist";
	private static Global sharedObj = null;
	public static MediaPlayer music, effect;
	public static int isPage = 0;
	public static ArrayList<DownloadItem> downloadList;
	public static boolean pausePlaying = false;
	public static String pauseVideo = "";
	public static int pauseTime = 0; 
	
	public static MainActivity mMainActivity;
	public static SettingActivity mSettingActivity;
	
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
    
    public static class DownloadSRTFromURL extends AsyncTask<String, String, String> {

    	public boolean isSuccess = true;
    	public String out_filename;
    	public int percentage = 0;

        /**
         * Downloading file in background thread
         * */
        @Override
        protected String doInBackground(String... f_url) {
            int count;
            try {
            	URL url = new URL(f_url[0]);
                URLConnection conection = url.openConnection();
                conection.connect();

                // this will be useful so that you can show a tipical 0-100%
                // progress bar
                int lenghtOfFile = conection.getContentLength();

                // download the file
                InputStream input = new BufferedInputStream(url.openStream(),
                        8192);

//                File f = new File(Global.extern_dir, Global.app);
//                if (!f.exists()) {
//                    f.mkdirs();
//                }
                // Output stream
                FileOutputStream output = null; //new FileOutputStream(out_filename);
                output = mMainActivity.openFileOutput(out_filename, Context.MODE_PRIVATE);
                
                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    publishProgress("" + (int) ((total * 100) / lenghtOfFile));
                    percentage = (int)((total * 100) / lenghtOfFile);
                    output.write(data, 0, count);
                }

                // flushing output
                output.flush();
                
             // closing streams
                output.close();
                input.close();
            	
                isSuccess = false;
            } catch (Exception e) {
            }
            return null;
        }
    }
    
    public static class DownloadVideoFromURL extends AsyncTask<String, String, String> {

    	public boolean isSuccess = true;
    	public String out_filename;
    	public VideoDB videoInfo = null;
    	public int percentage = 0;

        @Override
		protected void onProgressUpdate(String... values) {
//			mProgressHUD.setMessage(values[0]);
			super.onProgressUpdate(values);
		}
        /**
         * Downloading file in background thread
         * */
        @Override
        protected String doInBackground(String... f_url) {
            int count;
            try {
        		File file = new File(out_filename);
                if(!file.exists())
                {
	            	URL url = new URL(f_url[0]);
	                URLConnection conection = url.openConnection();
	                conection.connect();
	
	                // this will be useful so that you can show a tipical 0-100%
	                // progress bar
	                int lenghtOfFile = conection.getContentLength();
	
	                // download the file
	                InputStream input = new BufferedInputStream(url.openStream(),
	                        8192);
	
	                // Output stream
	                FileOutputStream output = null;
	                output = mMainActivity.openFileOutput(out_filename, Context.MODE_PRIVATE);
	
	                byte data[] = new byte[1024];
	
	                long total = 0;
	
	                while ((count = input.read(data)) != -1) {
	                    total += count;
	                    publishProgress("" + (int) ((total * 100) / lenghtOfFile));
	                    percentage = (int)((total * 100) / lenghtOfFile);
	                    output.write(data, 0, count);
	                    Log.e("Download: ", "--------------------        "+percentage);
	                    if (Global.isPage == 0) {
	                    	for (int ii=0; ii<Global.downloadList.size(); ii++) {
	                    		if (Global.downloadList.get(ii).videoInfo.name.equals(videoInfo.name)) {
	                    			final CircleProgressBar pb_circle = Global.downloadList.get(ii).pb_circle; 
	                    			Global.downloadList.get(ii).progress = percentage;
	                    			Global.mMainActivity.runOnUiThread(new Runnable() {
	    								public void run() {
	    									pb_circle.setProgress(percentage);
	    								}
	    							});
	                    			break;
	                    		}
	                    	}
	                    } else if (Global.isPage == 1 && Global.mSettingActivity != null) {
	                    	for (int ii=0; ii<Global.downloadList.size(); ii++) {
	                    		if (Global.downloadList.get(ii).videoInfo.name.equals(videoInfo.name)) {
	                    			Global.mSettingActivity.runOnUiThread(new Runnable() {
	    								public void run() {
	    									Global.mSettingActivity.setProgress(videoInfo.name, percentage);
	    								}
	    							});
	                    			break;
	                    		}
	                    	}
	                    }
	                }
	
	                // flushing output
	                output.flush();
	                
	             // closing streams
	                output.close();
	                input.close();
            	}
            	
                isSuccess = false;
                
                if (Global.isPage == 0) {
                	mMainActivity.dbHelper.updateData(videoInfo.name, videoInfo.paid, true, videoInfo.thumb, videoInfo.fail, videoInfo.iap, videoInfo.etc);
                	mMainActivity.runOnUiThread(mMainActivity.Timer_Tick);
                } else if (Global.isPage == 1) {
                	mSettingActivity.dbHelper.updateData(videoInfo.name, videoInfo.paid, true, videoInfo.thumb, videoInfo.fail, videoInfo.iap, videoInfo.etc);
                	mSettingActivity.runOnUiThread(mSettingActivity.Timer_Tick);
                } else {
                	mMainActivity.dbHelper.updateData(videoInfo.name, videoInfo.paid, true, videoInfo.thumb, videoInfo.fail, videoInfo.iap, videoInfo.etc);
                }
            	
            	for (int ii=0; ii<Global.downloadList.size(); ii++) {
            		if (Global.downloadList.get(ii).videoInfo.name.equals(videoInfo.name)) {
            			Global.downloadList.remove(ii);
            			break;
            		}
            	}
            	
            } catch (Exception e) {
                Log.e("Error: >>>>>>>>>>>>>>>>>>>>>>", "*******************"+e.getMessage());
                if (percentage > 0) {
                	
                	if (Global.isPage == 0) {
                		mMainActivity.dbHelper.updateData(videoInfo.name, videoInfo.paid, true, videoInfo.thumb, true, videoInfo.iap, videoInfo.etc);
                		mMainActivity.runOnUiThread(mMainActivity.Timer_Tick);
                	} else if (Global.isPage == 1) {
                		mSettingActivity.dbHelper.updateData(videoInfo.name, videoInfo.paid, true, videoInfo.thumb, true, videoInfo.iap, videoInfo.etc);
                		mSettingActivity.runOnUiThread(mSettingActivity.Timer_Tick);
                	} else {
                		mMainActivity.dbHelper.updateData(videoInfo.name, videoInfo.paid, true, videoInfo.thumb, true, videoInfo.iap, videoInfo.etc);
                	}
                	 
                	for (int ii=0; ii<Global.downloadList.size(); ii++) {
                		if (Global.downloadList.get(ii).videoInfo.name.equals(videoInfo.name)) {
                			Global.downloadList.remove(ii);
                			break;
                		}
                	}
                } else {
                	
                }
            }

            return null;
        }
        
        
    }
}