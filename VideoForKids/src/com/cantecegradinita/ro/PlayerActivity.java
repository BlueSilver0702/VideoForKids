package com.cantecegradinita.ro;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;

import com.cantecegradinita.utils.DBHelperFraction;
import com.cantecegradinita.utils.Global;
import com.cantecegradinita.utils.VideoDB;
import com.sri.subtitlessupport.utils.Caption;
import com.sri.subtitlessupport.utils.FormatSRT;
import com.sri.subtitlessupport.utils.TimedTextObject;

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
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
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

@SuppressLint("InflateParams") public class PlayerActivity extends Activity {

	RelativeLayout rl_play_main;
	Button btn_play_back, btn_play_repeat, btn_play_order, btn_play_start, btn_play_prev, btn_play_next;
	SeekBar sb_progress;
	LinearLayout ll_bottom;
	boolean is_repeat, is_order, is_play;
	TextView tv_player_time, tv_player_title;
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
    
    private SubtitleProcessingTask subsFetchTask;
    public TimedTextObject srt;
    
    boolean isTablet = false;
    boolean isPaused = false;
	
    @SuppressLint("NewApi") @SuppressWarnings("deprecation")
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isTablet = Global.isTablet(this);
        
        if (isTablet) {
        	setContentView(R.layout.activity_play_tablet);
        } else {
        	setContentView(R.layout.activity_play);
        }
        
        Bundle extraBundle;
	    extraBundle=getIntent().getBundleExtra("databundle");
	    String videoName = extraBundle.getString("video");
	    
	    Global.isPlayPage = true;
	    
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
        if (!videoInfo.download) {finish();Global.isPlayPage = false;}
        
        is_play = true;
        btn_play_back = (Button)findViewById(R.id.btn_play_back);
        btn_play_repeat = (Button)findViewById(R.id.btn_player_repeat);
        btn_play_order = (Button)findViewById(R.id.btn_player_order);
        btn_play_start = (Button)findViewById(R.id.btn_player_start);
        btn_play_prev = (Button)findViewById(R.id.btn_play_prev);
        btn_play_next = (Button)findViewById(R.id.btn_play_next);
        sb_progress = (SeekBar)findViewById(R.id.sb_progress);
        tv_player_time = (TextView)findViewById(R.id.tv_player_time);
        tv_player_title = (TextView)findViewById(R.id.tv_play_title);
        
        rl_play_main = (RelativeLayout)findViewById(R.id.rl_play_main);
        ll_bottom = (LinearLayout)findViewById(R.id.ll_bottom);
        
        mVideoView = (VideoView)findViewById(R.id.vv_play_2);
        mediaController = new MediaController (this);
		mediaController.setAnchorView(mVideoView);
//		Uri video = Uri.parse("android.resource://com.cantecegradinita.ro/raw/in_padurea_cu_alune");
	    mVideoView.setMediaController(mediaController);
	    
	    mVideoView.setOnErrorListener(new OnErrorListener() {
			
			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {
				// TODO Auto-generated method stub
				System.out.println(">>>>>>>>>>>>>>>>>>errororororoor" );
				
				if (is_repeat) {
					mVideoView.suspend();
		        	tv_player_title.setVisibility(View.INVISIBLE);
			        
			        File f = loadFile(videoInfo.video);
			        if (f== null){
			        	err(videoInfo.video);
			        	return true;
			        }
			        
					Uri video = Uri.fromFile(f);
				    mVideoView.setVideoURI(video);
				    mVideoView.start();
				    btn_play_start.setBackgroundResource(R.drawable.btn_play_play);
				    if (setting_bgmusic) Global.music.pause();
					is_play = true;
					
					subsFetchTask = new SubtitleProcessingTask();
					subsFetchTask.srtPath = videoInfo.caption;
					subsFetchTask.execute();
					return true;
				}
				videoIndex ++;
				if (videoIndex == videoList.size()) videoIndex = 0;
		       
		        if (setting_autoplay) {
		        	mVideoView.suspend();
		        	tv_player_title.setVisibility(View.INVISIBLE);
			        String videoStr = "";
			        String videoName = "";
			        if (is_order) {
			        	videoStr = shuffleVideoList.get(videoIndex).video;
			        	videoName = shuffleVideoList.get(videoIndex).name;
			        	videoInfo = shuffleVideoList.get(videoIndex);
			        }
			        else {
			        	videoStr = videoList.get(videoIndex).video;
			        	videoName = videoList.get(videoIndex).name;
			        	videoInfo = videoList.get(videoIndex);
			        }
			        File f = loadFile(videoStr);
			        if (f== null){
			        	err(videoName);
			        	return true;
			        }
			        
					Uri video = Uri.fromFile(f);
				    mVideoView.setVideoURI(video);
				    mVideoView.start();
				    btn_play_start.setBackgroundResource(R.drawable.btn_play_play);
				    if (setting_bgmusic) Global.music.pause();
					is_play = true;
					subsFetchTask = new SubtitleProcessingTask();
					subsFetchTask.srtPath = videoInfo.caption;
					subsFetchTask.execute();
		        } else {
		        	is_play = false;
		        	btn_play_start.setBackgroundResource(R.drawable.btn_play_play);
		        	mVideoView.seekTo(0);
		        	if (setting_bgmusic) Global.music.start();
		        }
		        
				return true;
			}
		});
	    
	    mVideoView.setOnCompletionListener(new OnCompletionListener() {
			
			@Override
			public void onCompletion(MediaPlayer mp) {
				// TODO Auto-generated method stub
				if (is_repeat) {
					mVideoView.start();
					tv_player_title.setVisibility(View.INVISIBLE);
					subsFetchTask = new SubtitleProcessingTask();
					subsFetchTask.srtPath = videoInfo.caption;
					subsFetchTask.execute();
					return;
				}
				videoIndex ++;
				if (videoIndex == videoList.size()) videoIndex = 0;
		       
		        if (setting_autoplay) {
		        	mVideoView.suspend();
		        	tv_player_title.setVisibility(View.INVISIBLE);
			        String videoStr = "";
			        String videoName = "";
			        if (is_order) {
			        	videoStr = shuffleVideoList.get(videoIndex).video;
			        	videoName = shuffleVideoList.get(videoIndex).name;
			        	videoInfo = shuffleVideoList.get(videoIndex);
			        }
			        else {
			        	videoStr = videoList.get(videoIndex).video;
			        	videoName = videoList.get(videoIndex).name;
			        	videoInfo = videoList.get(videoIndex);
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
					subsFetchTask = new SubtitleProcessingTask();
					subsFetchTask.srtPath = videoInfo.caption;
					subsFetchTask.execute();
		        } else {
		        	is_play = false;
		        	btn_play_start.setBackgroundResource(R.drawable.btn_play_play);
		        	mVideoView.seekTo(0);
		        	if (setting_bgmusic) Global.music.start();
		        }
			}
		});
	    mVideoView.setMediaController(null);
	    
	    if (!isTablet) {
		    Button btn_cover_pl_back = (Button)findViewById(R.id.btn_cover_pl_back);
		    Button btn_cover_pl_next = (Button)findViewById(R.id.btn_cover_pl_next);
		    Button btn_cover_pl_prev = (Button)findViewById(R.id.btn_cover_pl_prev);
		    Button btn_cover_pl_play = (Button)findViewById(R.id.btn_cover_pl_play);
		    Button btn_cover_pl_shuffle = (Button)findViewById(R.id.btn_cover_pl_shuffle);
		    Button btn_cover_pl_repeat = (Button)findViewById(R.id.btn_cover_pl_repeat);
		    
		    btn_cover_pl_back.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {btn_play_back.performClick();}
			});
		    btn_cover_pl_next.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {btn_play_next.performClick();}
			});
		    btn_cover_pl_prev.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {btn_play_prev.performClick();}
			});
		    btn_cover_pl_play.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {btn_play_start.performClick();}
			});
		    btn_cover_pl_shuffle.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {btn_play_order.performClick();}
			});
		    btn_cover_pl_repeat.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {btn_play_repeat.performClick();}
			});
	    }
	    
	    if (isTablet) {
			TextView tv_videoTitle = (TextView)findViewById(R.id.tv_video_title);
			tv_videoTitle.setText(videoInfo.name);
		}
        
        for (int i=0; i<videoList.size(); i++) {
        	final VideoDB row = videoList.get(i);
        	final int index = i;
        	if (isTablet) {
        		RelativeLayout tr=(RelativeLayout)LayoutInflater.from(PlayerActivity.this).inflate(R.layout.cell_play_tablet, null);
        		LinearLayout.LayoutParams layout_param= new LinearLayout.LayoutParams(
                        Global.convertDpToPixel(200, this),
                        LinearLayout.LayoutParams.MATCH_PARENT);
        		layout_param.setMargins(Global.convertDpToPixel(8, this), Global.convertDpToPixel(8, this), Global.convertDpToPixel(8, this), Global.convertDpToPixel(3, this));
        		Button btn_downloaded = (Button)tr.findViewById(R.id.btn_play);
        		TextView tv_play = (TextView)tr.findViewById(R.id.tv_play);
        		tv_play.setText(row.name);
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
    					
    			        videoInfo = row;
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
                
//                layout_param.width = (int)(ll_bottom.getMeasuredHeight() * 1.7734375 / 1.3);
            	ll_bottom.addView(tr, layout_param);
        	} else {
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
    					
    			        videoInfo = row;
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
//                layout_param.width = (int)(tr.getMeasuredHeight() * 1.7734375 / 1.3);
            	ll_bottom.addView(btn_downloaded);
        	}
        }
        
        btn_play_prev.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				isPaused = false;
				if (setting_soundeffect) Global.effect.start();
				videoIndex --;
				if (videoIndex < 0) videoIndex = videoList.size()-1;
				String videoStr = "";
				String videoName = "";
		        if (is_order) {
		        	videoStr = shuffleVideoList.get(videoIndex).video;
		        	videoName = shuffleVideoList.get(videoIndex).name;
		        	videoInfo = shuffleVideoList.get(videoIndex);
		        } else {
		        	videoStr = videoList.get(videoIndex).video;
		        	videoName = videoList.get(videoIndex).name;
		        	videoInfo = videoList.get(videoIndex);
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
				isPaused = false;
				if (setting_soundeffect) Global.effect.start();
				videoIndex ++;
				if (videoIndex == videoList.size()) videoIndex = 0;
		        mVideoView.suspend();
		        String videoStr = "";
		        String videoName = "";
		        if (is_order) {
		        	videoStr = shuffleVideoList.get(videoIndex).video;
		        	videoName = shuffleVideoList.get(videoIndex).name;
		        	videoInfo = shuffleVideoList.get(videoIndex);
		        } else {
		        	videoStr = videoList.get(videoIndex).video;
		        	videoName = videoList.get(videoIndex).name;
		        	videoInfo = videoList.get(videoIndex);
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
				if (setting_bgmusic) Global.music.start();
				
				Global.isPlayPage = false;
				
				if (is_play) {
					Global.pausePlaying = true;
					Global.pauseVideo = videoInfo.name;
					Global.pauseTime = mVideoView.getCurrentPosition();
					is_play = false;
				} else {
					Global.pausePlaying = false;
					Global.pauseTime = 0;
					Global.pauseVideo = "";
				}
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
					cleanUp();
					if (setting_bgmusic) Global.music.start();
				}
				else {
					btn_play_start.setBackgroundResource(R.drawable.btn_play_bg);
					is_play = true;
					mVideoView.start();
					
					subsFetchTask = new SubtitleProcessingTask();
					subsFetchTask.srtPath = videoInfo.caption;
					subsFetchTask.execute();
					
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
	    
	    if (Global.pausePlaying && videoInfo.name.equals(Global.pauseVideo)) {
    		btn_play_start.setBackgroundResource(R.drawable.btn_play_bg);
    		is_play = true;
    		mVideoView.seekTo(Global.pauseTime);
    		mVideoView.start();
    		zoom(false);
    		String time = (getDate(Global.pauseTime, "mm:ss"));
        	tv_player_time.setText(time);
        	sb_progress.setProgress(Global.pauseTime);
        	
    		cleanUp();
    		Global.pausePlaying = false;
    		isPaused = true;
    	} else {
    		mVideoView.start();
    		zoom(true);
    	}
	    
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
	    
	    RelativeLayout rl_bottom_cover = (RelativeLayout) findViewById(R.id.play_bottom_cover);
	    rl_bottom_cover.setOnTouchListener(new OnTouchListener() {
			
			@SuppressLint("ClickableViewAccessibility") @Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				return true;
			}
		});
	    
	    mVideoView.setOnPreparedListener(new OnPreparedListener() {

			@Override
			public void onPrepared(MediaPlayer mp) {
				// TODO Auto-generated method stub
				sb_progress.setMax(mp.getDuration());
				sb_progress.setProgress(0);
				tv_player_title.setVisibility(View.INVISIBLE);
				subsFetchTask = new SubtitleProcessingTask();
				subsFetchTask.srtPath = videoInfo.caption;
				subsFetchTask.execute();
				
				if (setting_bgmusic) Global.music.pause();
				
				if (!isPaused) {
					btn_play_start.setBackgroundResource(R.drawable.btn_play_bg);
					is_play = true;
					isPaused = false;
				}
				
				if (isTablet) {
					TextView tv_videoTitle = (TextView)findViewById(R.id.tv_video_title);
					tv_videoTitle.setText(videoInfo.name);
				}
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
    
    private void moveToBack(View currentTopView, View currentView) {
        ViewGroup vg = ((ViewGroup) currentView.getParent());
        for(int i=0;i<vg.getChildCount();i++){
            View v=vg.getChildAt(i);
            if(!v.equals(currentView) && !v.equals(currentTopView))
            {
                vg.bringChildToFront(v);
                break;
            }
        }
    }
    
    @SuppressLint("NewApi") @SuppressWarnings("deprecation")
	public void zoom(boolean in) {
    	if (in) {
    		mVideoView.bringToFront();
    		tv_player_title.bringToFront();
			RelativeLayout.LayoutParams layout_param= new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.MATCH_PARENT,
					RelativeLayout.LayoutParams.FILL_PARENT);
            layout_param.rightMargin = 0;
            layout_param.leftMargin = 0;
            layout_param.topMargin = 0;
            layout_param.bottomMargin = 0;
            
            layout_param.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            layout_param.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            layout_param.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            layout_param.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            mVideoView.setLayoutParams(layout_param);
            
            RelativeLayout.LayoutParams layout_tv_param= new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.WRAP_CONTENT,
					RelativeLayout.LayoutParams.WRAP_CONTENT);
            if (isTablet) {
            	layout_tv_param.bottomMargin = Global.convertDpToPixel(80, this);
                tv_player_title.setTextSize(17);
            } else {
            	layout_tv_param.bottomMargin = Global.convertDpToPixel(30, this);
                tv_player_title.setTextSize(15);
            }
            
            layout_tv_param.addRule(RelativeLayout.CENTER_HORIZONTAL);
            layout_tv_param.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.vv_play_2);
            tv_player_title.setLayoutParams(layout_tv_param);
            
    	} else {
    		RelativeLayout.LayoutParams layout_param= new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.MATCH_PARENT,
					RelativeLayout.LayoutParams.MATCH_PARENT);
    		if (isTablet) {
    			layout_param.rightMargin = Global.convertDpToPixel(200, this);
                layout_param.leftMargin = Global.convertDpToPixel(200, this);
                layout_param.topMargin = Global.convertDpToPixel(90, this);
                layout_param.bottomMargin = Global.convertDpToPixel(10, this);
    		} else {
    			layout_param.rightMargin = Global.convertDpToPixel(75, this);
                layout_param.leftMargin = Global.convertDpToPixel(75, this);
                layout_param.topMargin = Global.convertDpToPixel(18, this);
                layout_param.bottomMargin = Global.convertDpToPixel(5, this);
    		}
            
            layout_param.addRule(RelativeLayout.ABOVE, R.id.rl_bottom);
            layout_param.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            layout_param.removeRule(RelativeLayout.ALIGN_PARENT_LEFT);
            layout_param.removeRule(RelativeLayout.ALIGN_PARENT_TOP);
            layout_param.removeRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            mVideoView.setLayoutParams(layout_param);
			moveToBack(tv_player_title, mVideoView);
			
			RelativeLayout.LayoutParams layout_tv_param= new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.WRAP_CONTENT,
					RelativeLayout.LayoutParams.WRAP_CONTENT);
			if (isTablet) {
				tv_player_title.setTextSize(13);
	            layout_tv_param.bottomMargin = Global.convertDpToPixel(100, this);
			} else {
				tv_player_title.setTextSize(10);
	            layout_tv_param.bottomMargin = Global.convertDpToPixel(70, this);
			}
			
            layout_tv_param.addRule(RelativeLayout.CENTER_HORIZONTAL);
            layout_tv_param.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.vv_play_2);
            tv_player_title.setLayoutParams(layout_tv_param);
//			moveToBack(mVideoView);
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
 	    dbHelper.updateData(video, row.paid, false, row.thumb, false, row.iap, row.etc);
 	    finish();
 	    Global.isPlayPage = false;
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
    	cleanUp();
    }  
    
    protected void onResume() {
    	if (Global.pausePlaying && videoInfo.name.equals(Global.pauseVideo)) {
    		btn_play_start.setBackgroundResource(R.drawable.btn_play_bg);
    		is_play = true;
    		mVideoView.start();
    		mVideoView.seekTo(Global.pauseTime);
    		
    		zoom(false);
    		String time = (getDate(Global.pauseTime, "mm:ss"));
        	tv_player_time.setText(time);
        	sb_progress.setProgress(Global.pauseTime);
        	
    		cleanUp();
    		Global.pausePlaying = false;
    		isPaused = true;
    	}
    	super.onResume();
    }
    
    @Override
    protected void onPause() {
    	if (is_play) {
    		Global.pausePlaying = true;
    		Global.pauseVideo = videoInfo.name;
    		mVideoView.pause();
    		Global.pauseTime = mVideoView.getCurrentPosition();
    		is_play = false;
    	}
				
    	super.onPause();
    }
    
    private void cleanUp() {
    	tv_player_title.setVisibility(View.INVISIBLE);
		if (subtitleDisplayHandler != null) {
			subtitleDisplayHandler.removeCallbacks(subtitleProcessesor);
			subtitleDisplayHandler = null;
			if (subsFetchTask != null)
				subsFetchTask.cancel(true);
		}
	}
    
    
	private Runnable subtitleProcessesor = new Runnable() {

		@Override
		public void run() {
			if (mVideoView.isPlaying()) {
				int currentPos = mVideoView.getCurrentPosition();
				Collection<Caption> subtitles = srt.captions.values();
				for (Caption caption : subtitles) {
					if (currentPos >= caption.start.mseconds
							&& currentPos <= caption.end.mseconds) {
						onTimedText(caption);
						break;
					} else if (currentPos > caption.end.mseconds) {
						onTimedText(null);
					}
				}
			}
			subtitleDisplayHandler.postDelayed(this, 100);
		}
	};
	
	private Handler subtitleDisplayHandler = new Handler();

	public class SubtitleProcessingTask extends AsyncTask<Void, Void, Void> {

		public String srtPath = "";
		@Override
		protected void onPreExecute() {
			tv_player_title.setText("Loading subtitles..");
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... params) {
			// int count;
			try {
				/*
				 * if you want to download file from Internet, use commented
				 * code.
				 */
				File file = new File(Global.file_dir + srtPath);
				FileInputStream fileInputStream = new FileInputStream(file);
				FormatSRT formatSRT = new FormatSRT();
				srt = formatSRT.parseFile("sample.srt", fileInputStream);

			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (null != srt) {
				tv_player_title.setText("");
				if (subtitleProcessesor != null) {
					if (subtitleDisplayHandler == null) {
						subtitleDisplayHandler = new Handler(); 
					}
					subtitleDisplayHandler.post(subtitleProcessesor);
				}
			}
			super.onPostExecute(result);
		}
	}

	public void onTimedText(Caption text) {
		if (text == null) {
			tv_player_title.setVisibility(View.INVISIBLE);
			return;
		}
		
		tv_player_title.setText(removeHTML(Html.fromHtml(text.content).toString()));
		if (setting_subtitles)
			tv_player_title.setVisibility(View.VISIBLE);
		else 
			tv_player_title.setVisibility(View.INVISIBLE);
	}
	
	public static String removeHTML(String htmlString) {
		  // Remove HTML tag from java String    
		String noHTMLString = htmlString.replaceAll("\\<.*?\\>", "");
	
		// Remove Carriage return from java String
		noHTMLString = noHTMLString.replaceAll("\r", "<br/>");
		noHTMLString = noHTMLString.replaceAll("<([bip])>.*?</\1>", "");
		// Remove New line from java string and replace html break
		noHTMLString = noHTMLString.replaceAll("\n", " ");
		noHTMLString = noHTMLString.replaceAll("\"", "&quot;");
		noHTMLString = noHTMLString.replaceAll("<(.*?)\\>"," ");//Removes all items in brackets
		noHTMLString = noHTMLString.replaceAll("<(.*?)\\\n"," ");//Must be undeneath
		noHTMLString = noHTMLString.replaceFirst("(.*?)\\>", " ");
		noHTMLString = noHTMLString.replaceAll("&nbsp;"," ");
		noHTMLString = noHTMLString.replaceAll("&amp;"," ");
		return noHTMLString;
	}
}
