package com.cantecegradinita.ro;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;

import com.cantecegradinita.utils.DBHelperFraction;
import com.cantecegradinita.utils.Global;
import com.cantecegradinita.utils.VideoDB;

import android.annotation.SuppressLint;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.VideoView;

public class PlayerActivity extends Activity {

	RelativeLayout rl_play_main;
	Button btn_play_back, btn_play_repeat, btn_play_order, btn_play_start, btn_play_prev, btn_play_next;
	SeekBar sb_progress;
	LinearLayout ll_bottom;
	boolean is_repeat, is_order, is_play;
	TextView tv_player_time;
	Timer myTimer;
	VideoDB videoInfo;
	ArrayList<VideoDB> videoList;
	ArrayList<VideoDB> shuffleVideoList;
	int videoIndex = 0;
	boolean is_touched = false;
	
	private DBHelperFraction dbHelper;
	public VideoView mVideoView;
	MediaController mediaController;
	
    boolean setting_bgmusic;
    boolean setting_soundeffect;
    boolean setting_subtitles;
    boolean setting_searchfield;
    boolean setting_shuffle;
    boolean setting_autoplay;
	
    @SuppressLint("NewApi") @SuppressWarnings("deprecation")
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        
        Bundle extraBundle;
	    extraBundle=getIntent().getBundleExtra("databundle");
	    String videoName = extraBundle.getString("video");
	    
	    SharedPreferences sharedPreferences = getSharedPreferences(Global.app,Context.MODE_PRIVATE);
		setting_bgmusic = sharedPreferences.getBoolean("s_bgmusic", true);
		setting_soundeffect = sharedPreferences.getBoolean("s_soundeffect", true);
		setting_subtitles = sharedPreferences.getBoolean("s_subtitles", true);
		setting_searchfield = sharedPreferences.getBoolean("s_searchfield", true);
		setting_shuffle = sharedPreferences.getBoolean("s_shuffle", true);
		setting_autoplay = sharedPreferences.getBoolean("s_autoplay", true);
	    
	    dbHelper = new DBHelperFraction(this);
        dbHelper.getWritableDatabase();
        videoList = dbHelper.getAllVideos(true);
        shuffleVideoList = dbHelper.getAllVideos(true);
        Collections.shuffle(shuffleVideoList);
        for (int ii=0; ii<videoList.size(); ii++) {
        	if (videoName.equals(videoList.get(ii).name)) videoIndex = ii;
        }
        
        videoInfo = dbHelper.getData(videoName);
        if (!videoInfo.download) finish();
        
        is_play = true;
        btn_play_back = (Button)findViewById(R.id.btn_play_back);
        btn_play_repeat = (Button)findViewById(R.id.btn_player_repeat);
        btn_play_order = (Button)findViewById(R.id.btn_player_order);
        btn_play_start = (Button)findViewById(R.id.btn_player_start);
        btn_play_prev = (Button)findViewById(R.id.btn_play_prev);
        btn_play_next = (Button)findViewById(R.id.btn_play_next);
        sb_progress = (SeekBar)findViewById(R.id.sb_progress);
        tv_player_time = (TextView)findViewById(R.id.tv_player_time);
        
        rl_play_main = (RelativeLayout)findViewById(R.id.rl_play_main);
        ll_bottom = (LinearLayout)findViewById(R.id.ll_bottom);
        
        mVideoView = (VideoView)findViewById(R.id.vv_play_2);
        mediaController = new MediaController (this);
		mediaController.setAnchorView(mVideoView);
//		Uri video = Uri.parse("android.resource://com.cantecegradinita.ro/raw/in_padurea_cu_alune");
	    mVideoView.setMediaController(mediaController);
	    mVideoView.setOnCompletionListener(new OnCompletionListener() {
			
			@Override
			public void onCompletion(MediaPlayer mp) {
				// TODO Auto-generated method stub
				if (is_repeat) {
					mVideoView.start();
					return;
				}
				videoIndex ++;
				if (videoIndex == videoList.size()) videoIndex = 0;
		       
		        if (setting_autoplay) {
		        	mVideoView.suspend();
			        String videoStr = "";
			        String videoName = "";
			        if (is_order) {
			        	videoStr = shuffleVideoList.get(videoIndex).video;
			        	videoName = shuffleVideoList.get(videoIndex).name;
			        }
			        else {
			        	videoStr = videoList.get(videoIndex).video;
			        	videoName = videoList.get(videoIndex).name;
			        }
			        File f = loadFile(videoStr);
			        if (f== null){
			        	err(videoName);
			        	return;
			        }
			        
					Uri video = Uri.fromFile(f);
				    mVideoView.setVideoURI(video);
				    mVideoView.start();
				    btn_play_start.setBackgroundResource(R.drawable.btn_play_play);
				    if (setting_bgmusic) Global.music.pause();
					is_play = true;
		        } else {
		        	is_play = false;
		        	btn_play_start.setBackgroundResource(R.drawable.btn_play_play);
		        	mVideoView.seekTo(0);
		        	if (setting_bgmusic) Global.music.start();
		        }
			}
		});
	    mVideoView.setMediaController(null);
        
//        ll_bottom.removeAllViews();
        
        for (int i=0; i<videoList.size(); i++) {
        	final VideoDB row = videoList.get(i);
        	final int index = i;
        	Button btn_downloaded = new Button(this);
        	LinearLayout.LayoutParams btnLayout = new LinearLayout.LayoutParams(Global.convertDpToPixel(120, this), LayoutParams.MATCH_PARENT);
        	btnLayout.setMargins(Global.convertDpToPixel(5, this), Global.convertDpToPixel(5, this), 0, Global.convertDpToPixel(5, this));
        	btn_downloaded.setLayoutParams(btnLayout);
        	Resources res = getResources();
        	Bitmap bitmap = BitmapFactory.decodeFile(Global.file_dir + row.image);
            BitmapDrawable bd = new BitmapDrawable(res, bitmap);
            btn_downloaded.setBackgroundDrawable(bd);
            btn_downloaded.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if (setting_soundeffect) Global.effect.start();
					if (setting_bgmusic) Global.music.pause();
			        mVideoView.suspend();
			        videoIndex = index;
					
			        File f = loadFile(row.video);
			        if (f== null){
			        	err(row.name);
			        	return;
			        }
			        Uri video = Uri.fromFile(f);
				    mVideoView.setVideoURI(video);
				    mVideoView.start();
				}
			});
        	ll_bottom.addView(btn_downloaded);

        }
        
        btn_play_prev.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (setting_soundeffect) Global.effect.start();
				videoIndex --;
				if (videoIndex < 0) videoIndex = videoList.size()-1;
				String videoStr = "";
				String videoName = "";
		        if (is_order) {
		        	videoStr = shuffleVideoList.get(videoIndex).video;
		        	videoName = shuffleVideoList.get(videoIndex).name;
		        } else {
		        	videoStr = videoList.get(videoIndex).video;
		        	videoName = videoList.get(videoIndex).name;
		        }
		        File f = loadFile(videoStr);
		        if (f== null){
		        	err(videoName);
		        	
		        } else {
		        	Uri video = Uri.fromFile(f);
					mVideoView.suspend();
				    mVideoView.setVideoURI(video);
				    mVideoView.start();
				    if (setting_bgmusic) Global.music.pause();
		        }
				
			}
		});
        
        btn_play_next.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (setting_soundeffect) Global.effect.start();
				videoIndex ++;
				if (videoIndex == videoList.size()) videoIndex = 0;
		        mVideoView.suspend();
		        String videoStr = "";
		        String videoName = "";
		        if (is_order) {
		        	videoStr = shuffleVideoList.get(videoIndex).video;
		        	videoName = shuffleVideoList.get(videoIndex).name;
		        } else {
		        	videoStr = videoList.get(videoIndex).video;
		        	videoName = videoList.get(videoIndex).name;
		        }
		        File f = loadFile(videoStr);
		        if (f== null){
		        	err(videoName);
		        	return;
		        }
				Uri video = Uri.fromFile(f);
			    mVideoView.setVideoURI(video);
			    mVideoView.start();
			    if (setting_bgmusic) Global.music.pause();
			}
		});
        
        btn_play_back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (setting_soundeffect) Global.effect.start();
				finish();
			}
		});
        
        
        btn_play_repeat.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (setting_soundeffect) Global.effect.start();
				if (is_repeat) {
					btn_play_repeat.setBackgroundResource(R.drawable.btn_left_1);
					is_repeat = false;
				} else {
					btn_play_repeat.setBackgroundResource(R.drawable.btn_left_1_selected);
					is_repeat = true;
				}
			}
		});
        
        btn_play_order.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (setting_soundeffect) Global.effect.start();
				if (is_order) {
					btn_play_order.setBackgroundResource(R.drawable.btn_left_2);
					is_order = false;
					setting_shuffle = false;
					SharedPreferences sharedPreferences = getSharedPreferences(Global.app,Context.MODE_PRIVATE);
					SharedPreferences.Editor editor = sharedPreferences.edit();
					editor.putBoolean("s_shuffle", false);	    
				    editor.commit();
				}
				else {
					btn_play_order.setBackgroundResource(R.drawable.btn_left_2_selected);
					is_order = true;
					setting_shuffle = true;
					SharedPreferences sharedPreferences = getSharedPreferences(Global.app,Context.MODE_PRIVATE);
					SharedPreferences.Editor editor = sharedPreferences.edit();
					editor.putBoolean("s_shuffle", true);	    
				    editor.commit();
				}
			}
		});
        
        btn_play_start.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (setting_soundeffect) Global.effect.start();
				if (is_play) {
					btn_play_start.setBackgroundResource(R.drawable.btn_play_play);
					is_play = false;
					mVideoView.pause();
					if (setting_bgmusic) Global.music.start();
				}
				else {
					btn_play_start.setBackgroundResource(R.drawable.btn_play_bg);
					is_play = true;
					mVideoView.start();
					if (setting_bgmusic) Global.music.pause();
				}
			}
		});
        
        File f = loadFile(videoInfo.video);
        if (f== null){
        	err(videoInfo.name);
        	return;
        }
        
		Uri video = Uri.fromFile(f);
	    mVideoView.setVideoURI(video);
	    mVideoView.start();
	    
	    rl_play_main.setOnTouchListener(new OnTouchListener() {
			@SuppressLint("ClickableViewAccessibility") @Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				zoom(true);
				return false;
			}
		});
	    
	    mVideoView.setOnTouchListener(new OnTouchListener() {
			@SuppressLint("ClickableViewAccessibility") @Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				zoom(false);
				return false;
			}
		});
	    
	    mVideoView.setOnPreparedListener(new OnPreparedListener() {

			@Override
			public void onPrepared(MediaPlayer mp) {
				// TODO Auto-generated method stub
				sb_progress.setMax(mp.getDuration());
				sb_progress.setProgress(0);
				btn_play_start.setBackgroundResource(R.drawable.btn_play_bg);
				if (setting_bgmusic) Global.music.pause();
				is_play = true;
			}
	    	
	    });
	    
	    sb_progress.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// TODO Auto-generated method stub
				if (is_touched) mVideoView.seekTo(seekBar.getProgress());
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				is_touched = true;
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				is_touched = false;
				if (setting_soundeffect) Global.effect.start();
			}
		});
	    
	    zoom(true);
	    
	    myTimer = new Timer();
        myTimer.schedule(new TimerTask() {          
            @Override
            public void run() {
            	TimerMethod();
            }

        }, 0, 1000);
	    
    }
    
    private void TimerMethod()
    {
        //This method is called directly by the timer
        //and runs in the same thread as the timer.

        //We call the method that will work with the UI
        //through the runOnUiThread method.
        this.runOnUiThread(Timer_Tick);
    }


    private Runnable Timer_Tick = new Runnable() {
        public void run() {

        //This method runs in the same thread as the UI.               

        //Do something to the UI thread here
        	String time = (getDate(mVideoView.getCurrentPosition(), "mm:ss"));
        	tv_player_time.setText(time);
        	sb_progress.setProgress(mVideoView.getCurrentPosition());
        }
    };
    
    private void moveToBack(View currentView) {
        ViewGroup vg = ((ViewGroup) currentView.getParent());
        for(int i=0;i<vg.getChildCount();i++){
            View v=vg.getChildAt(i);
            if(!v.equals(currentView))
            {
                vg.bringChildToFront(v);
                break;
            }
        }
    }
    
    public void zoom(boolean in) {
    	if (in) {
    		mVideoView.bringToFront();
			RelativeLayout.LayoutParams layout_param= new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.MATCH_PARENT,
					RelativeLayout.LayoutParams.MATCH_PARENT);
            layout_param.rightMargin = 0;
            layout_param.leftMargin = 0;
            layout_param.topMargin = 0;
            layout_param.bottomMargin = 0;
            mVideoView.setLayoutParams(layout_param);
    	} else {
    		RelativeLayout.LayoutParams layout_param= new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.MATCH_PARENT,
					RelativeLayout.LayoutParams.MATCH_PARENT);
            layout_param.rightMargin = Global.convertDpToPixel(75, this);
            layout_param.leftMargin = Global.convertDpToPixel(75, this);
            layout_param.topMargin = Global.convertDpToPixel(18, this);
            layout_param.bottomMargin = Global.convertDpToPixel(5, this);
            layout_param.addRule(RelativeLayout.ABOVE, R.id.rl_bottom);
            mVideoView.setLayoutParams(layout_param);
			moveToBack(mVideoView);
			
			
    	}
    }
    
    public File loadFile (final String video) {
    	File f = new File(Global.file_dir, video);
        if (!f.exists()) {
        	
			return null;
        }
        return f;
    }
    
    public void err (String video) {
    	final String videoStr = video;
    	AlertDialog.Builder builder = new AlertDialog.Builder(PlayerActivity.this);
		builder.setMessage("Couldn't find downloaded Video File.")
	       .setTitle("Sorry");
		// Add the buttons
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		               // User cancelled the dialog
		        	   removeDeletedItem(videoStr);
		           }
		       });
		AlertDialog dialog = builder.create();
		dialog.show();
    }
    
    public void removeDeletedItem(String video) {
    	Log.println(1, "hello", "uploading....  " + video);
 	    VideoDB row = dbHelper.getData(video);
 	    dbHelper.updateData(video, row.paid, false, row.thumb, false);
 	    finish();
    }
    @SuppressLint("SimpleDateFormat") 
    public static String getDate(long milliSeconds, String dateFormat)
    {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
        // Create a calendar object that will convert the time value in milliseconds to date. 
         Calendar calendar = Calendar.getInstance();
         calendar.setTimeInMillis(milliSeconds);
         return formatter.format(calendar.getTime());
    }
    
    protected void onDestroy() {
    	super.onDestroy();
    	if(myTimer != null) {
	         myTimer.cancel();
	         myTimer = null;
	     }
    }
    
}
