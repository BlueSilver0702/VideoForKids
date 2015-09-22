package com.cantecegradinita.ro;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

public class PlayerActivity extends Activity {

	RelativeLayout rl_play_main;
	Button btn_play_back, btn_play_repeat, btn_play_order, btn_play_start;
	boolean is_repeat, is_order, is_play;
	TextView tv_player_time;
	Timer myTimer;
	
	public VideoView mVideoView;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        
        is_play = true;
        btn_play_back = (Button)findViewById(R.id.btn_play_back);
        btn_play_repeat = (Button)findViewById(R.id.btn_player_repeat);
        btn_play_order = (Button)findViewById(R.id.btn_player_order);
        btn_play_start = (Button)findViewById(R.id.btn_player_start);
        tv_player_time = (TextView)findViewById(R.id.tv_player_time);
        
        rl_play_main = (RelativeLayout)findViewById(R.id.rl_play_main);
        
        
        
        btn_play_back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
        
        
        btn_play_repeat.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
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
				if (is_order) {
					btn_play_order.setBackgroundResource(R.drawable.btn_left_2);
					is_order = false;
				}
				else {
					btn_play_order.setBackgroundResource(R.drawable.btn_left_2_selected);
					is_order = true;
				}
			}
		});
        
        btn_play_start.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (is_play) {
					btn_play_start.setBackgroundResource(R.drawable.btn_play_play);
					is_play = false;
					mVideoView.pause();
				}
				else {
					btn_play_start.setBackgroundResource(R.drawable.btn_play_bg);
					is_play = true;
					mVideoView.start();
				}
			}
		});
        
        mVideoView = (VideoView)findViewById(R.id.vv_play_2);
        
		MediaController mediaController = new MediaController (this);
		mediaController.setAnchorView(mVideoView);
		Uri video = Uri.parse("android.resource://com.cantecegradinita.ro/raw/in_padurea_cu_alune");
	    mVideoView.setMediaController(mediaController);
	    mVideoView.setMediaController(null);
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
	    
	    mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
            	if (is_repeat)
            		mp.start();
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
    
    public int convertDpToPixel(int dp){
        Resources resources = this.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return (int)px;
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
            layout_param.rightMargin = convertDpToPixel(75);
            layout_param.leftMargin = convertDpToPixel(75);
            layout_param.topMargin = convertDpToPixel(18);
            layout_param.bottomMargin = convertDpToPixel(5);
            layout_param.addRule(RelativeLayout.ABOVE, R.id.rl_bottom);
            mVideoView.setLayoutParams(layout_param);
			moveToBack(mVideoView);
			
			
    	}
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
