package com.cantecegradinita.ro;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import com.cantecegradinita.utils.DBHelperFraction;
import com.cantecegradinita.utils.Global;
import com.cantecegradinita.utils.VideoDB;
import com.kyleduo.switchbutton.SwitchButton;

public class SettingActivity extends Activity {

	private DBHelperFraction dbHelper;
	
	ArrayList<VideoDB> videoList;
//	ArrayList<VideoDB> showedList;
    
	LinearLayout ll_tab1, ll_tab2, tbl_tab2, ll_main_tb;
    TableLayout tbl_tab1;
    ScrollView sv_tab2;
    
    Button btn_setting_back, btn_setting_tab1, btn_setting_tab2, btn_setting_tab1_icon, btn_setting_tab2_icon;

    private SwitchButton sb_set1, sb_set2, sb_set3, sb_set4, sb_set5, sb_set6;
    
    boolean setting_bgmusic;
    boolean setting_soundeffect;
    boolean setting_subtitles;
    boolean setting_searchfield;
    boolean setting_shuffle;
    boolean setting_autoplay;

    boolean flag_downloadHistory;
    boolean flag_init_downloaded;
    
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    boolean isTablet = false;
//    boolean isTab1;
    @SuppressLint("NewApi") @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Global.isSettingPage = true;
        isTablet = Global.isTablet(this);
        
        if (isTablet) {
        	setContentView(R.layout.activity_setting_tablet);
        } else {
        	setContentView(R.layout.activity_setting);
        }
        
        sharedPreferences = getSharedPreferences(Global.app,Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
		flag_downloadHistory = sharedPreferences.getBoolean("downloaded", false);
		setting_bgmusic = sharedPreferences.getBoolean("s_bgmusic", true);
		setting_soundeffect = sharedPreferences.getBoolean("s_soundeffect", true);
		setting_subtitles = sharedPreferences.getBoolean("s_subtitles", true);
		setting_searchfield = sharedPreferences.getBoolean("s_searchfield", true);
		setting_shuffle = sharedPreferences.getBoolean("s_shuffle", true);
		setting_autoplay = sharedPreferences.getBoolean("s_autoplay", true);
		
		dbHelper = new DBHelperFraction(this);
        dbHelper.getWritableDatabase();
                
        setupUI();		
    	refreshSetting();
    }
    
    public void updateVideo() {
    	videoList = dbHelper.getAllVideos(false);
    }
    
    @SuppressWarnings("static-access")
	public void onChanged(boolean isTab1) {
    	if (isTab1) {
    		ll_tab1.setVisibility(View.VISIBLE);
    		ll_tab2.setVisibility(View.INVISIBLE);
    		tbl_tab1.setVisibility(View.VISIBLE);
    		sv_tab2.setVisibility(View.INVISIBLE);
    		btn_setting_tab1.setBackgroundResource(R.drawable.bg);
    		btn_setting_tab2.setBackgroundColor(new Color().TRANSPARENT);
    	} else {
    		ll_tab2.setVisibility(View.VISIBLE);
    		ll_tab1.setVisibility(View.INVISIBLE);
    		tbl_tab1.setVisibility(View.INVISIBLE);
    		sv_tab2.setVisibility(View.VISIBLE);
    		btn_setting_tab2.setBackgroundResource(R.drawable.bg);
    		btn_setting_tab1.setBackgroundColor(new Color().TRANSPARENT);
    	}
    }
    
    @SuppressLint("InflateParams") public void setupUI() {
        
        //  Setting Page
        ll_tab1 = (LinearLayout)findViewById(R.id.ll_setting_tab1);
        ll_tab2 = (LinearLayout)findViewById(R.id.ll_setting_tab2);
        tbl_tab2 = (LinearLayout)findViewById(R.id.tbl_setting_tab2);
        tbl_tab1 = (TableLayout)findViewById(R.id.tbl_setting_tab1);
        sv_tab2 = (ScrollView)findViewById(R.id.sv_setting);

        btn_setting_back = (Button)findViewById(R.id.btn_setting_back);
        btn_setting_tab1 = (Button)findViewById(R.id.btn_setting_tab1);
        btn_setting_tab2 = (Button)findViewById(R.id.btn_setting_tab2);
        btn_setting_tab1_icon = (Button)findViewById(R.id.btn_setting_tab1_icon);
        btn_setting_tab2_icon = (Button)findViewById(R.id.btn_setting_tab2_icon);
        
        //	Main Page        
        sb_set1 = (SwitchButton)findViewById(R.id.sb_md1);
        sb_set2 = (SwitchButton)findViewById(R.id.sb_md2);
        sb_set3 = (SwitchButton)findViewById(R.id.sb_md3);
        sb_set4 = (SwitchButton)findViewById(R.id.sb_md4);
        sb_set5 = (SwitchButton)findViewById(R.id.sb_md5);
        sb_set6 = (SwitchButton)findViewById(R.id.sb_md6);
        
        sb_set1.setChecked(setting_bgmusic);
        sb_set2.setChecked(setting_soundeffect);
        sb_set3.setChecked(setting_subtitles);
        sb_set4.setChecked(setting_searchfield);
        sb_set5.setChecked(setting_shuffle);
        sb_set6.setChecked(setting_autoplay);
        
        if (!isTablet) {
	        Button bt_cover_setting_back = (Button)findViewById(R.id.btn_cover_setting_back);
	        
	        bt_cover_setting_back.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					btn_setting_back.performClick();
				}
			});
        }
	        
        sb_set1.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (setting_soundeffect) Global.effect.start();
				if (isChecked) {
					editor.putBoolean("s_bgmusic", true);
					Global.music = MediaPlayer.create(getApplicationContext(), R.raw.loop_background_music);
					Global.music.start();
				} else {
					editor.putBoolean("s_bgmusic", false);
					Global.music.stop();
				}
			    editor.commit();
			    setting_bgmusic = isChecked;
			}	
		});
        sb_set2.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					editor.putBoolean("s_soundeffect", true);
					Global.effect.start();
				} else
					editor.putBoolean("s_soundeffect", false);	    
			    editor.commit();
			    setting_soundeffect = isChecked;
			}	
		});
        sb_set3.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (setting_soundeffect) Global.effect.start();
				if (isChecked)
					editor.putBoolean("s_subtitles", true);
				else
					editor.putBoolean("s_subtitles", false);	    
			    editor.commit();
			    setting_subtitles = isChecked;
			}	
		});
        sb_set4.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (setting_soundeffect) Global.effect.start();
				if (isChecked) {
					editor.putBoolean("s_searchfield", true);
				} else {
					editor.putBoolean("s_searchfield", false);
				}
			    editor.commit();
			    setting_searchfield = isChecked;
			}	
		});
        sb_set5.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (setting_soundeffect) Global.effect.start();
				if (isChecked)
					editor.putBoolean("s_shuffle", true);
				else
					editor.putBoolean("s_shuffle", false);	    
			    editor.commit();
			    setting_shuffle = isChecked;
			}	
		});
        sb_set6.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (setting_soundeffect) Global.effect.start();
				if (isChecked)
					editor.putBoolean("s_autoplay", true);
				else
					editor.putBoolean("s_autoplay", false);	    
			    editor.commit();
			    setting_shuffle = isChecked;
			}	
		});
       
        refreshSetting();
        
        onChanged(true);
                
        btn_setting_back.setOnClickListener(new OnClickListener() {
        	@Override
			public void onClick(View v) {
        		if (setting_soundeffect) Global.effect.start();
        		Global.isSettingPage = false;
				finish();
			}
        });
        
        btn_setting_tab1.setOnClickListener(new OnClickListener() {
        	@Override
			public void onClick(View v) {
        		if (setting_soundeffect) Global.effect.start();
				onChanged(true);
			}
        });
        
        btn_setting_tab1_icon.setOnClickListener(new OnClickListener() {
        	@Override
			public void onClick(View v) {
        		if (setting_soundeffect) Global.effect.start();
				onChanged(true);
			}
        });
        
        btn_setting_tab2.setOnClickListener(new OnClickListener() {
        	@Override
			public void onClick(View v) {
        		if (setting_soundeffect) Global.effect.start();
				onChanged(false);
			}
        });
        
        btn_setting_tab2_icon.setOnClickListener(new OnClickListener() {
        	@Override
			public void onClick(View v) {
        		if (setting_soundeffect) Global.effect.start();
				onChanged(false);
			}
        });
        
    }
    
    @SuppressWarnings("deprecation")
	@SuppressLint("InflateParams") public void refreshSetting() {
    	updateVideo();
    	tbl_tab2.removeAllViews();
        for (int i=0; i<videoList.size(); i++) {
        	if (videoList.get(i).paid) continue;
        	RelativeLayout tr_tmp = null;
        	LinearLayout.LayoutParams layout_param = null;
        	
        	if (isTablet) {
        		tr_tmp=(RelativeLayout)LayoutInflater.from(SettingActivity.this).inflate(R.layout.cell_setting_tablet, null);
        		layout_param= new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.FILL_PARENT,
                        Global.convertDpToPixel(68, this));
                layout_param.bottomMargin = Global.convertDpToPixel(20, this);
        	} else {
        		tr_tmp=(RelativeLayout)LayoutInflater.from(SettingActivity.this).inflate(R.layout.cell_setting, null);
        		layout_param= new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.FILL_PARENT,
                        Global.convertDpToPixel(36, this));
                layout_param.bottomMargin = Global.convertDpToPixel(12, this);
        	}
        	final RelativeLayout tr = tr_tmp;
            tr.setBackgroundResource(R.drawable.setting_item_bg);
            final VideoDB row = videoList.get(i);
            TextView tv_setting_download_title = (TextView)tr.findViewById(R.id.tv_setting_row);
            final TextView tv_setting_download_corrupted = (TextView)tr.findViewById(R.id.tv_setting_corrupted);
            final Button bt_setting_download = (Button)tr.findViewById(R.id.btn_setting_row);
            tv_setting_download_title.setText(row.name);
            if (row.fail) tv_setting_download_corrupted.setVisibility(View.VISIBLE);
            else tv_setting_download_corrupted.setVisibility(View.INVISIBLE);
            if (row.download) bt_setting_download.setBackgroundResource(R.drawable.btn_delete);
            else {
            	bt_setting_download.setBackgroundResource(R.drawable.btn_download);
            	for (int ii=0; ii<Global.downloadList.size(); ii++) {
					if (Global.downloadList.get(ii).name.equals(row.name)) {
						bt_setting_download.setBackgroundResource(R.drawable.btn_download_ds);
						tv_setting_download_corrupted.setVisibility(View.INVISIBLE);
						break;
					} 
				}
            }
            bt_setting_download.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if (setting_soundeffect) Global.effect.start();
					if (row.download) {
						dbHelper.updateData(row.name, row.paid, false, row.thumb, false, row.iap, row.etc);
						updateVideo();
						File file = new File(Global.file_dir, row.video);
						File fileSrt = new File(Global.file_dir, row.caption);
	                    if(file.exists()) { file.delete(); }
	                    if(fileSrt.exists()) { fileSrt.delete(); }
	                    
	                    refreshSetting();
					} else {
						for (int ii=0; ii<Global.downloadList.size(); ii++) {
							if (Global.downloadList.get(ii).name.equals(row.name)) {
								bt_setting_download.setBackgroundResource(R.drawable.btn_download_ds);
								tv_setting_download_corrupted.setVisibility(View.INVISIBLE);
								return;
							} 
						}
						AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
						builder.setMessage("Are you sure to download this video?")
					       .setTitle("Confirm");
						// Add the buttons
						builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						           public void onClick(DialogInterface dialog, int id) {
						               // User clicked OK button
						        	   DownloadSRTFromURL downloadSRT = new DownloadSRTFromURL();
    						   	       downloadSRT.out_filename = Global.file_dir+row.caption;
    						   	       downloadSRT.execute(Global.server_path+row.caption);
    						   	       
						        	   DownloadVideoFromURL downloadHD = new DownloadVideoFromURL();
							   	       downloadHD.out_filename = Global.file_dir+row.video;
							   	       downloadHD.videoInfo = row;
							   	       downloadHD.execute(Global.server_path+row.video);
							   	       Toast.makeText(SettingActivity.this, "Downloading "+row.name,Toast.LENGTH_LONG).show();
								   	   bt_setting_download.setBackgroundResource(R.drawable.btn_download_ds);
								   	   tv_setting_download_corrupted.setVisibility(View.INVISIBLE);
								   	   Global.downloadList.add(row);
						           }
						       });
						builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						           public void onClick(DialogInterface dialog, int id) {
						               // User cancelled the dialog
						           }
						       });

						// Create the AlertDialog
						AlertDialog dialog = builder.create();
						dialog.show();

					}
				}
			});
        	tbl_tab2.addView(tr, layout_param);
        }
    }
    /**
     * Background Async Task to download file
     * */
    
    class DownloadSRTFromURL extends AsyncTask<String, String, String> {

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

                File f = new File(Global.extern_dir, Global.app);
                if (!f.exists()) {
                    f.mkdirs();
                }
                // Output stream
                OutputStream output = new FileOutputStream(out_filename);

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
        
    class DownloadVideoFromURL extends AsyncTask<String, String, String> {

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
	
	                File f = new File(Global.extern_dir, Global.app);
	                if (!f.exists()) {
	                    f.mkdirs();
	                }
	                // Output stream
	                OutputStream output = new FileOutputStream(out_filename);
	
	                byte data[] = new byte[1024];
	
	                long total = 0;
	
	                while ((count = input.read(data)) != -1) {
	                    total += count;
	                    publishProgress("" + (int) ((total * 100) / lenghtOfFile));
	                    percentage = (int)((total * 100) / lenghtOfFile);
	                    output.write(data, 0, count);
	                    Log.e("Download: ", "+++++++++++++++++++++++++++++        "+percentage);
	                }
	
	                // flushing output
	                output.flush();
	                
	             // closing streams
	                output.close();
	                input.close();
            	}
            	
                isSuccess = false;
                
            	dbHelper.updateData(videoInfo.name, videoInfo.paid, true, videoInfo.thumb, videoInfo.fail, videoInfo.iap, videoInfo.etc);
            	if (!Global.isPlayPage) SettingActivity.this.runOnUiThread(Timer_Tick);
            	for (int ii=0; ii<Global.downloadList.size(); ii++) {
            		if (Global.downloadList.get(ii).name.equals(videoInfo.name)) {
            			Global.downloadList.remove(ii);
            			break;
            		}
            	}
            } catch (Exception e) {
                Log.e("Error: >>>>>>>>>>>>>>>>>>>>>>", "+++++++++++++++++++++++++++++"+e.getMessage());
                if (percentage > 0) {
                	dbHelper.updateData(videoInfo.name, videoInfo.paid, true, videoInfo.thumb, true, videoInfo.iap, videoInfo.etc);
                	if (!Global.isPlayPage) SettingActivity.this.runOnUiThread(Timer_Tick);
                	for (int ii=0; ii<Global.downloadList.size(); ii++) {
                		if (Global.downloadList.get(ii).name.equals(videoInfo.name)) {
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
    
    private Runnable Timer_Tick = new Runnable() {
        public void run() {
        	refreshSetting();
        }
    };
        
    @SuppressLint("NewApi") @Override
    protected void onResume() {
    	
    	dbHelper.getWritableDatabase();
		setting_shuffle = sharedPreferences.getBoolean("s_shuffle", true);
        sb_set5.setChecked(setting_shuffle);
        Global.isPlayPage = false;
        Global.isSettingPage = true;
        if (setting_bgmusic) Global.music.start();
    	super.onResume();
    }

    @Override
    protected void onPause() {
    	dbHelper.close();
    	if (setting_bgmusic) Global.music.pause();
    	Global.isSettingPage = false;
    	super.onPause();
    }
}
