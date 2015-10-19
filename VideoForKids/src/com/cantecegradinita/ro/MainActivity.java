package com.cantecegradinita.ro;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.OnEditorActionListener;
import android.widget.ViewAnimator;

import com.cantecegradinita.utils.DBHelperFraction;
import com.cantecegradinita.utils.Global;
import com.cantecegradinita.utils.Video;
import com.cantecegradinita.utils.VideoDB;
import com.example.android.trivialdrivesample.util.IabHelper;
import com.example.android.trivialdrivesample.util.IabResult;
import com.example.android.trivialdrivesample.util.Inventory;
import com.example.android.trivialdrivesample.util.Purchase;
import com.walnutlabs.android.ProgressHUD;

public class MainActivity extends Activity implements OnCancelListener {

	private DBHelperFraction dbHelper;
	
	ArrayList<VideoDB> videoList;
//	ArrayList<VideoDB> showedList;
	ViewAnimator viewAnimator;
    
	LinearLayout ll_main_tb;
    
    Button btn_setting;
    Button btn_main_total, btn_main_some, btn_main_total_icon, btn_main_some_icon, btn_search_cancel;
    EditText et_search;
    ImageView iv_search;    
    ProgressHUD mProgressHUD;
    
    boolean is_download = false;
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

	// In-app billing constants
	final String IAB_MONTH = "monthly";
	final String IAB_YEAR = "yearly";
	boolean mSubscribedToMonthly = false;
	boolean mSubscribedToYearly = false;
    // (arbitrary) request code for the purchase flow
    final int RC_REQUEST = 10001;
    final String TAG = "TrivialDrive";
    
    IabHelper mHelper;
	
    @SuppressLint("NewApi") @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        isTablet = Global.isTablet(this);
        
        if (isTablet) {
        	setContentView(R.layout.activity_main_tablet);
        } else {
        	setContentView(R.layout.activity_main);
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
        
        Global.isPlayPage = false;
        Global.downloadList = new ArrayList<VideoDB>();
        
        Global.music = MediaPlayer.create(getApplicationContext(), R.raw.loop_background_music);
        Global.effect = MediaPlayer.create(getApplicationContext(), R.raw.click_sound);
        if (setting_bgmusic) Global.music.start();
        Global.music.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				// TODO Auto-generated method stub
				Global.music.reset();
				Global.music.start();
			}
		});
        
        setupUI();
			
		mProgressHUD = ProgressHUD.show(MainActivity.this,"Loading ...", true,false,this);
		
		DownloadFileFromURL downloadHD = new DownloadFileFromURL();
        downloadHD.out_filename = Global.plist_path;
        downloadHD.execute(Global.file_url);
        
//        refreshVideos();
        
        String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAq7a/1O7Onb2AjCvb1n83lRuNODL2lt2Ce/aa4ywqCtaZorwOguEAn4A28H+2MKY5VA+fNcU3hyHGT1v8RUSAl/nyBVAUb9mX+kddahbg9+viiKb00ALo/XRMx4gvxT/79c9LN1SSDS0/nk3FGpdwxZY/DaaSenDc/ve8ePHx6of45SdfY2z4zBAmXQp/E6gNoJiYASY4pLg3I8l26nLN9XnlGaZHOu1V8BXtw/XnBbQZBVeC9Vg2rNRl5WqeBzWJWqR9XMB4qY3umS4yQojyDpIauJQwmz1tgnoFmZNojewJiovh5RUmyuH4h29M4ffmQTmEkPhYUR0CkRBOG21OMwIDAQAB";
        
        // Create the helper, passing it our context and the public key to verify signatures with
        Log.d(TAG, "Creating IAB helper.");
        mHelper = new IabHelper(this, base64EncodedPublicKey);
        
        // enable debug logging (for a production application, you should set this to false).
        mHelper.enableDebugLogging(true);

        // Start setup. This is asynchronous and the specified listener
        // will be called once setup completes.
        Log.d(TAG, "Starting setup.");
        
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                Log.d(TAG, "Setup finished.");

                if (!result.isSuccess()) {
                    // Oh noes, there was a problem.
                    complain("Problem setting up in-app billing: " + result);
                    return;
                }

                // Hooray, IAB is fully set up. Now, let's get an inventory of stuff we own.
                Log.d(TAG, "Setup successful. Querying inventory.");
                mHelper.queryInventoryAsync(mGotInventoryListener);
            }
        });
    }

    
    @SuppressWarnings("deprecation")
	@SuppressLint({ "InflateParams", "NewApi" }) public void refreshVideos() {
    	
    	updateVideo();
    	ll_main_tb.removeAllViews();
    	ArrayList<VideoDB> thumbList = new ArrayList<VideoDB>();
    	for (int k=0; k<videoList.size(); k++) {
    		if (videoList.get(k).thumb) thumbList.add(videoList.get(k));
    	}
    	
    	for (int j=0; j<Math.round(thumbList.size()/2.0); j++) {
        	LinearLayout tr_temp = null;
        	if (isTablet)
        		tr_temp =(LinearLayout)LayoutInflater.from(MainActivity.this).inflate(R.layout.cell_main_tablet, null);
        	else 
        		tr_temp =(LinearLayout)LayoutInflater.from(MainActivity.this).inflate(R.layout.cell_main, null);
        	final LinearLayout tr = tr_temp;
			LinearLayout.LayoutParams layout_param = null;
			if (isTablet) {
				layout_param = new LinearLayout.LayoutParams(
						Global.convertDpToPixel(350, this),
						LinearLayout.LayoutParams.MATCH_PARENT);
				layout_param.rightMargin = Global.convertDpToPixel(7, this);
			} else {
				layout_param = new LinearLayout.LayoutParams(
						Global.convertDpToPixel(180, this),
						LinearLayout.LayoutParams.MATCH_PARENT);
				layout_param.rightMargin = Global.convertDpToPixel(0, this);
			}
			
            final VideoDB videoRow1 = thumbList.get(j*2);
            RelativeLayout rl_bg1 = (RelativeLayout)tr.findViewById(R.id.rl_main_img1);
            Resources res1 = getResources();
            Bitmap bitmap1 = BitmapFactory.decodeFile(Global.file_dir+videoRow1.image);
            BitmapDrawable bd1 = new BitmapDrawable(res1, bitmap1);
//            bitmap1.recycle();

            rl_bg1.setBackgroundDrawable(bd1);
            final Button tempb1 = (Button)tr.findViewById(R.id.btn_main_cell_1);
            if (videoRow1.download) {
            	tempb1.setVisibility(View.INVISIBLE);
            	rl_bg1.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						if (setting_soundeffect) Global.effect.start();
						Bundle extradataBundle=new Bundle();
						extradataBundle.putString("video", videoRow1.name);
						Bundle bundle=new Bundle();
						bundle.putBundle("databundle",extradataBundle);
						Intent sd=new Intent(MainActivity.this,PlayerActivity.class);
						sd.putExtras(bundle);
				        startActivity(sd);
					}
				});
            } else if (videoRow1.paid) {
            	tempb1.setVisibility(View.VISIBLE);
            	tempb1.setBackgroundResource(R.drawable.lock);
            	rl_bg1.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						tempb1.performClick();
					}
				});
            } else {
            	rl_bg1.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						tempb1.performClick();
					}
				});
            }
            TextView tv_main1 = (TextView)tr.findViewById(R.id.tv_main_text1);
            tv_main1.setText(videoRow1.name);
            
            tempb1.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (setting_soundeffect) Global.effect.start();
					if (!videoRow1.paid) {
						for (int jj=0; jj<Global.downloadList.size(); jj++) {
		        		   if (Global.downloadList.get(jj).name.equals(videoRow1.name)) return;
		        	    }
		        	   
						AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
						builder.setMessage("Are you sure to download this video?")
					       .setTitle("Confirm");
						// Add the buttons
						builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						           public void onClick(DialogInterface dialog, int id) {
						               // User clicked OK button
						        	   Global.downloadList.add(videoRow1);
						        	   Toast.makeText(MainActivity.this, "Downloading "+videoRow1.name,Toast.LENGTH_LONG).show();
						        	   
						        	   DownloadSRTFromURL downloadSRT = new DownloadSRTFromURL();
							   	       downloadSRT.out_filename = Global.file_dir+videoRow1.caption;
							   	       downloadSRT.execute(Global.server_path+videoRow1.caption);
							   	       
						        	   DownloadVideoFromURL downloadHD = new DownloadVideoFromURL();
							   	       downloadHD.out_filename = Global.file_dir+videoRow1.video;
							   	       downloadHD.videoInfo = videoRow1;
							   	       downloadHD.execute(Global.server_path+videoRow1.video);
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
					} else {
						showCalcDailogBox(videoRow1);
					}
				}
			});
            
            if (j*2+1 < thumbList.size()) {
            	final VideoDB videoRow2 = thumbList.get(j*2+1);
                RelativeLayout rl_bg2 = (RelativeLayout)tr.findViewById(R.id.rl_main_img2);
                Resources res2 = getResources();
                Bitmap bitmap2 = BitmapFactory.decodeFile(Global.file_dir+videoRow2.image);
                BitmapDrawable bd2 = new BitmapDrawable(res2, bitmap2);
//                bitmap2.recycle();

                rl_bg2.setBackgroundDrawable(bd2);

                final Button tempb2 = (Button)tr.findViewById(R.id.btn_main_cell_2);
                if (videoRow2.download) {
                	tempb2.setVisibility(View.INVISIBLE);
                	rl_bg2.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							if (setting_soundeffect) Global.effect.start();
							Bundle extradataBundle=new Bundle();
    						extradataBundle.putString("video", videoRow2.name);
    						Bundle bundle=new Bundle();
    						bundle.putBundle("databundle",extradataBundle);
    						Intent sd=new Intent(MainActivity.this,PlayerActivity.class);
    						sd.putExtras(bundle);
    				        startActivity(sd);
						}
					});
                } else if (videoRow2.paid) {
                	tempb2.setVisibility(View.VISIBLE);
                	tempb2.setBackgroundResource(R.drawable.lock);
                	rl_bg2.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							tempb2.performClick();
						}
					});
                } else {
                	rl_bg2.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							tempb2.performClick();
						}
					});
                }
                TextView tv_main2 = (TextView)tr.findViewById(R.id.tv_main_text2);
                tv_main2.setText(videoRow2.name);
                
                tempb2.setOnClickListener(new OnClickListener() {
    				@Override
    				public void onClick(View v) {
    					if (setting_soundeffect) Global.effect.start();
    					if (!videoRow2.paid) {
    						
    						for (int jj=0; jj<Global.downloadList.size(); jj++) {
			        		   if (Global.downloadList.get(jj).name.equals(videoRow2.name)) return;
			        	    }
						
    						AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        					builder.setMessage("Are you sure to download this video?")
        				       .setTitle("Confirm");
        					// Add the buttons
        					builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
        					           public void onClick(DialogInterface dialog, int id) {
        					               // User clicked OK button
        					        	   
        					        	   Global.downloadList.add(videoRow2);
        					        	   
        					        	   Toast.makeText(MainActivity.this, "Downloading "+videoRow2.name,Toast.LENGTH_LONG).show();
        					        	   
        					        	   DownloadSRTFromURL downloadSRT = new DownloadSRTFromURL();
        						   	       downloadSRT.out_filename = Global.file_dir+videoRow2.caption;
        						   	       downloadSRT.execute(Global.server_path+videoRow2.caption);
        						   	       
        					        	   DownloadVideoFromURL downloadHD = new DownloadVideoFromURL();
        						   	       downloadHD.out_filename = Global.file_dir+videoRow2.video;
        						   	       downloadHD.videoInfo = videoRow2;
        						   	       downloadHD.execute(Global.server_path+videoRow2.video);
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
    					} else {
    						showCalcDailogBox(videoRow2);
    					}
    				}
    			});
            } else {
            	RelativeLayout rl_second = (RelativeLayout)tr.findViewById(R.id.rl_main_second);
            	rl_second.setVisibility(View.INVISIBLE);
            }
            
            layout_param.width = (int)(ll_main_tb.getMeasuredHeight() * 1.7734375 / 2.3);
        	ll_main_tb.addView(tr, layout_param);

        }
    }
    
    public void updateVideo() {
    	if (is_download)
    		videoList = dbHelper.searchData(et_search.getText().toString(), true);
    	else 
    		videoList = dbHelper.searchData(et_search.getText().toString(), false);
    }
        
    @SuppressLint("InflateParams") public void setupUI() {        
        //  Setting Page
        btn_setting = (Button)findViewById(R.id.btn_main_setting);
        ll_main_tb = (LinearLayout)findViewById(R.id.ll_main_tb);
        
        //	Main Page
        btn_main_total = (Button)findViewById(R.id.btn_main_total);
        btn_main_some = (Button)findViewById(R.id.btn_main_some);
        btn_main_total_icon = (Button)findViewById(R.id.btn_main_total_icon);
        btn_main_some_icon = (Button)findViewById(R.id.btn_main_some_icon);
        btn_search_cancel = (Button)findViewById(R.id.btn_search_cancel);
        et_search = (EditText)findViewById(R.id.et_search);
        iv_search = (ImageView)findViewById(R.id.iv_search_back);
        
        if (!setting_searchfield) et_search.setVisibility(View.INVISIBLE);
        if (!setting_searchfield) iv_search.setVisibility(View.INVISIBLE);
        if (!setting_searchfield) btn_search_cancel.setVisibility(View.INVISIBLE);
        
        if (!isTablet) {
	        Button bt_cover_total = (Button)findViewById(R.id.bt_cover_total);
	        Button bt_cover_some = (Button)findViewById(R.id.bt_cover_some);
	        Button bt_cover_setting = (Button)findViewById(R.id.bt_cover_setting);
	        
	        bt_cover_total.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					btn_main_total.performClick();
				}
			});
	        bt_cover_some.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					btn_main_some.performClick();
				}
			});
	        bt_cover_setting.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					btn_setting.performClick();
				}
			});
        }
	                
    	btn_setting.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (setting_soundeffect) Global.effect.start();
				System.out.println(">>>>>>>>>>>>>>>>>> : ahahah");
				Intent sd=new Intent(MainActivity.this,SettingActivity.class);
		        startActivity(sd);
			}
		});
        
        btn_main_total.setOnClickListener(new OnClickListener() {
        	@Override
			public void onClick(View v) {
        		if (setting_soundeffect) Global.effect.start();
				btn_main_total_icon.setBackgroundResource(R.drawable.btn_totalsong_selected);
				btn_main_some_icon.setBackgroundResource(R.drawable.btn_mysong);
				videoList = dbHelper.getAllVideos(false);
				is_download = false;
				refreshVideos();
			}
        });
        btn_main_total_icon.setOnClickListener(new OnClickListener() {
        	@Override
			public void onClick(View v) {
        		if (setting_soundeffect) Global.effect.start();
        		btn_main_total_icon.setBackgroundResource(R.drawable.btn_totalsong_selected);
				btn_main_some_icon.setBackgroundResource(R.drawable.btn_mysong);
				videoList = dbHelper.getAllVideos(false);
				is_download = false;
				refreshVideos();
			}
        });
        
        btn_main_some.setOnClickListener(new OnClickListener() {
        	@Override
			public void onClick(View v) {
        		if (setting_soundeffect) Global.effect.start();
        		btn_main_total_icon.setBackgroundResource(R.drawable.btn_totalsong);
				btn_main_some_icon.setBackgroundResource(R.drawable.btn_mysong_selected);
				videoList = dbHelper.getAllVideos(true);
				is_download = true;
				refreshVideos();
			}
        });
        btn_main_some_icon.setOnClickListener(new OnClickListener() {
        	@Override
			public void onClick(View v) {
        		if (setting_soundeffect) Global.effect.start();
        		btn_main_total_icon.setBackgroundResource(R.drawable.btn_totalsong);
				btn_main_some_icon.setBackgroundResource(R.drawable.btn_mysong_selected);
				videoList = dbHelper.getAllVideos(true);
				is_download = true;
				refreshVideos();
			}
        });
        btn_search_cancel.setOnClickListener(new OnClickListener() {
        	@Override
			public void onClick(View v) {
        		if (setting_soundeffect) Global.effect.start();
        		et_search.setText("");
        		videoList = dbHelper.getAllVideos(false);
        		refreshVideos();
        		btn_search_cancel.setVisibility(View.INVISIBLE);
			}
        });
        et_search.setOnEditorActionListener(new OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				// TODO Auto-generated method stub
				if (et_search.getText().toString().length() > 0) btn_search_cancel.setVisibility(View.VISIBLE);
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					if (setting_soundeffect) Global.effect.start();
					if (is_download)
						videoList = dbHelper.searchData(et_search.getText().toString(), true);
					else
						videoList = dbHelper.searchData(et_search.getText().toString(), false);
					refreshVideos();
	                return false;
	            }
				return false;
			}
		});
        
		et_search.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				if (et_search.getText().toString().length() > 0) btn_search_cancel.setVisibility(View.VISIBLE);
				if (setting_soundeffect) Global.effect.start();
				if (is_download)
					videoList = dbHelper.searchData(et_search.getText().toString(), true);
				else
					videoList = dbHelper.searchData(et_search.getText().toString(), false);
				refreshVideos();
			}
		});
    }
    
    /**
     * Background Async Task to download file
     * */
    class DownloadFileFromURL extends AsyncTask<String, String, String> {

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
                
        	    editor.putBoolean("downloaded", true);	    
        	    editor.commit();
        		
        		List<Video> listArr = null;
        		ProductsPlistParsing parseHandler = new ProductsPlistParsing(MainActivity.this);
        		try {
        			listArr = parseHandler.getProductsPlistValues(Global.plist_path);
        		} catch (FileNotFoundException e) {
        			// TODO Auto-generated catch block
        			e.printStackTrace();
        		} catch (XmlPullParserException e) {
        			// TODO Auto-generated catch block
        			e.printStackTrace();
        		}
        		//dbHelper.emptyTable();
        		for (int i=0; i<listArr.size(); i++) {
        			Video videoRow = listArr.get(i);
        			VideoDB xyz = dbHelper.getData(videoRow.name);
        			if (xyz == null) {
        				VideoDB row = new VideoDB();
            			row.caption = videoRow.caption;
            			row.image = videoRow.image;
            			row.name = videoRow.name;
            			row.video = videoRow.video;
            			if (videoRow.paid.endsWith("1")) 
            				row.paid = true;
            			else 
            				row.paid = false;
            			dbHelper.insertData(row);
        			}
        		}
        	
        		videoList = dbHelper.getAllVideos(false);

        		VideoDB row = videoList.get(0);
        		DownloadThumbFromURL thumbHD = new DownloadThumbFromURL();
        		thumbHD.out_filename = Global.file_dir + row.image;
        		thumbHD.videoInfo = row;
                thumbHD.execute(Global.server_path+row.image);

            } catch (Exception e) {
                if (percentage > 0) {
                	DownloadFileFromURL downloadHD = new DownloadFileFromURL();
                    downloadHD.out_filename = Global.plist_path;
                    downloadHD.execute(Global.file_url);
                } else if (flag_downloadHistory) {
                	MainActivity.this.runOnUiThread(Timer_Tick);
                	mProgressHUD.dismiss();
                } else {
                	MainActivity.this.runOnUiThread(Timer_Error);
                }
            }
            return null;
        }
    }
    
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
    
    class DownloadThumbFromURL extends AsyncTask<String, String, String> {

    	public boolean isSuccess = true;
    	public String out_filename;
    	public VideoDB videoInfo = null;
    	public int percentage = 0;

    	@Override
		protected void onProgressUpdate(String... values) {
//			mProgressHUD.setMessage(values[0]);
			super.onProgressUpdate(values);
		}

        @Override
        protected String doInBackground(String... f_url) {
            int count;
            if (videoInfo == null) return "";
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
	                }
	
	                // flushing output
	                output.flush();
	                
	             // closing streams
	                output.close();
	                input.close();
            	}
            	
                isSuccess = false;
            	dbHelper.updateData(videoInfo.name, videoInfo.paid, videoInfo.download, true, videoInfo.fail, videoInfo.iap, videoInfo.etc);
            	if (videoInfo.caption.equals("in_padurea_cu_alune.srt")) {
            		InputStream in = getResources().openRawResource(R.raw.in_padurea_cu_alune);
            		FileOutputStream out = new FileOutputStream(Global.file_dir+"in_padurea_cu_alune.mp4");
            		byte[] buff = new byte[1024];
            		int read = 0;

            		try {
            		   while ((read = in.read(buff)) > 0) {
            		      out.write(buff, 0, read);
            		   }
            		} finally {
            		     in.close();
            		     out.close();
            		}
            		
            		InputStream in1 = getResources().openRawResource(R.raw.in_padurea_cu_alune_srt);
            		FileOutputStream out1 = new FileOutputStream(Global.file_dir+"in_padurea_cu_alune.srt");
            		byte[] buff1 = new byte[1024];
            		int read1 = 0;

            		try {
            		   while ((read1 = in1.read(buff1)) > 0) {
            		      out1.write(buff1, 0, read1);
            		   }
            		} finally {
            		     in1.close();
            		     out1.close();
            		}

            		dbHelper.updateData(videoInfo.name, videoInfo.paid, true, true, false, videoInfo.iap, videoInfo.etc);
            	}
            	videoList = dbHelper.getAllVideos(false);
            	
            	boolean in_download = false;
            	for (int i=0; i<videoList.size(); i++) {
            		VideoDB videoRow = videoList.get(i);
            		if (!videoRow.thumb) {
            			DownloadThumbFromURL thumbHD = new DownloadThumbFromURL();
    					thumbHD.videoInfo = videoList.get(i);
    					thumbHD.out_filename = Global.file_dir + thumbHD.videoInfo.image;
    			        thumbHD.execute(Global.server_path+thumbHD.videoInfo);
    			        in_download = true;
    			        if (i == 10) {
    			        	mProgressHUD.dismiss();			
                    		MainActivity.this.runOnUiThread(Timer_Tick);
    			        }
    			        publishProgress("downloaded+"+i);
    			        Log.println(1, "!!!!!!!!!!!!!!", "download --------------  "+i);
            			break;
            		}
            	}
            	if (!in_download) {
            		mProgressHUD.dismiss();			
            		MainActivity.this.runOnUiThread(Timer_Tick);
            	}
            } catch (Exception e) {
                Log.e("Error: >>>>>>>>>>>>>>>>>>>>>>", "+++++++++++++++++++++++++++++"+e.getMessage());
            	DownloadThumbFromURL thumbHD = new DownloadThumbFromURL();
				thumbHD.out_filename = Global.file_dir + videoInfo.image;
				thumbHD.videoInfo = videoInfo;
		        thumbHD.execute(Global.server_path+videoInfo.image);
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
            	if (!Global.isPlayPage) MainActivity.this.runOnUiThread(Timer_Tick);
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
                	if (!Global.isPlayPage) MainActivity.this.runOnUiThread(Timer_Tick);
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
        	refreshVideos();
        }
    };
    
    private Runnable Timer_Error = new Runnable() {
        public void run() {
        	AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
			builder.setMessage("Couldn't connect server.")
		       .setTitle("Error!");
			// Add the buttons
			builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			               // User cancelled the dialog
			           }
			       });

			// Create the AlertDialog
			AlertDialog dialog = builder.create();
			dialog.show();

        }
    };
    
    public class ProductsPlistParsing {   
    	Context context;
	   // constructor for  to get the context object from where you are using this plist parsing
	    public ProductsPlistParsing(Context ctx) {

	        context = ctx;
	    }

	    public List<Video> getProductsPlistValues(String filePath) throws FileNotFoundException, XmlPullParserException {

	       // specifying the  your plist file.And Xml ResourceParser is an event type parser for more details Read android source
	    	File file = new File (filePath);
	    	FileInputStream fis = new FileInputStream(file);
	    	XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
	    	factory.setNamespaceAware(true);
	    	XmlPullParser parser = factory.newPullParser();

	    	parser.setInput(fis, "UTF-8");// or whatever encoding suites you


	          // flag points to find key and value tags .
	        boolean keytag = false;
	        boolean valuetag = false;
	        String keyString = null;
	        String valueString = null;

	        Video videoItem = null;
	        List<Video> listResult = new ArrayList<Video>();
	        int event;
	        try {
	            event = parser.getEventType();

	            // repeting the loop at the end of the doccument 
	            while (event != XmlPullParser.END_DOCUMENT) {

	               switch (event) {
	                       //use switch case than the if ,else statements  
	                case 0:
	                    break;
	                case 1:
	                    break;
	                case 2:
	                	if (parser.getName().equals("dict")) {
	                		valuetag = false;
	                        keytag = false;
//	                        videoItem = null;
	                		videoItem = new Video();
	                	} else if (parser.getName().equals("key")) {
	                        keytag = true;
	                        valuetag = false;
	                    } else if (parser.getName().equals("string")) {
	                        valuetag = true;
	                        keytag = false;
	                    }
	                    break;
	                case 3:
	                    if (parser.getName().equals("dict")) {
	                        listResult.add(videoItem);
	                    }
	                    break;
	                case 4:
	                	String sss = parser.getText();
//	                	System.out.println(">>>>>>>>>>>>>>>>>>" + sss);
	                    if (keytag) {
	                    	keyString = sss;
	                    	keytag = false;
	                    } else if (valuetag) {
	                    	valueString = sss;
	                    	switch (keyString) {
	                    	case "Caption":
	                    		videoItem.caption = valueString;
	                    		break;
	                    	case "Image":
	                    		videoItem.image = valueString;
	                    		break;
	                    	case "Name":
	                    		videoItem.name = valueString;
	                    		break;
	                    	case "Paid":
	                    		videoItem.paid = valueString;
	                    		break;
	                    	case "Video":
	                    		videoItem.video = valueString;
	                    		break;
	                    	default:
	                    		break;
	                    	}
	                    	keyString = "";
	                    	valueString = "";
	                    	keytag = false;
	                    	valuetag = false;
	                    }
	                    break;
	                default:
	                    break;
	                }
	                event = parser.next();
	            }
	        } catch (XmlPullParserException e) {
	            e.printStackTrace();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	        
	        //here you get the plistValues.
	        return listResult;
	    }
	}
    
    @SuppressLint("NewApi") @Override
    protected void onResume() {
    	
    	dbHelper.getWritableDatabase();
		setting_shuffle = sharedPreferences.getBoolean("s_shuffle", true);
        Global.isPlayPage = false;
        Global.isSettingPage = false;
        refreshVideos();
        if (setting_bgmusic) Global.music.start();
    	super.onResume();
    }

    @Override
    protected void onPause() {
    	dbHelper.close();
    	if (setting_bgmusic) Global.music.pause();
    	super.onPause();
    }
    
    @Override
	public void onCancel(DialogInterface dialog) {
//		this.cancel(true);
		mProgressHUD.dismiss();
	}
    
    private void showCalcDailogBox(final VideoDB video){
		final Dialog dialog =new Dialog(this);
		 
        //tell the Dialog to use the dialog.xml as it's layout description
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dg_calc);
        dialog.setCanceledOnTouchOutside(false);
        
        Random r = new Random();
        final int i1 = r.nextInt(7)+1;
        final int i2 = r.nextInt(7)+1;
        
        String title="How much is "+ i1 + " + " + i2 + " ?";
        final EditText et_answer = (EditText) dialog.findViewById(R.id.et_answer);
        TextView tv_random_calc = (TextView) dialog.findViewById(R.id.tv_random_calc);
        tv_random_calc.setText(title);
        
        ImageView iv_ok = (ImageView) dialog.findViewById(R.id.iv_ok);
        ImageView iv_cancel = (ImageView) dialog.findViewById(R.id.iv_cancel);
        
        iv_ok.setOnClickListener(new View.OnClickListener() {
	        @Override
	        public void onClick(View v) {
	        	String strAnswer = et_answer.getText().toString();
	            if (strAnswer == null || !strAnswer.equals(""+(i1+i2))) {
	            	AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
	    			builder.setMessage("Wrong Answer!")
	    		       .setTitle("Warning!");
	    			// Add the buttons
	    			builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface oDialog, int id) {
			               // User cancelled the dialog
			        	   dialog.dismiss();
			           }
			       });

	    			// Create the AlertDialog
	    			AlertDialog warnDialog = builder.create();
	    			warnDialog.show();
	            } else {
	            	dialog.dismiss();
		            showPayDailogBox(video);
	            }
            }
    	});
        iv_cancel.setOnClickListener(new View.OnClickListener() {
	        @Override
	        public void onClick(View v) {
	            dialog.dismiss();	 
            }
    	});

        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();
	}
    
    private void showPayDailogBox(VideoDB video){
		final Dialog dialog =new Dialog(this);
		 
        //tell the Dialog to use the dialog.xml as it's layout description
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dg_payment);
        dialog.setCanceledOnTouchOutside(false);
    //    String title="Total: "+ PaymentSettings.CURRENCY_SIGN+totalbillAmount;
        ImageView iv_cancel = (ImageView) dialog.findViewById(R.id.iv_cancel);
        TextView tv_pay_some = (TextView) dialog.findViewById(R.id.tv_pay_some);
        TextView tv_pay_all = (TextView) dialog.findViewById(R.id.tv_pay_all);
        Button btn_restore = (Button) dialog.findViewById(R.id.btn_restore);
        iv_cancel.setOnClickListener(new View.OnClickListener() {
	        @Override
	        public void onClick(View v) {
	            dialog.dismiss();
	 
            }
    	});
        
        tv_pay_some.setOnClickListener(new View.OnClickListener() {
	        @Override
	        public void onClick(View v) {
	        	Log.d(TAG, "Monthly button clicked.");
	            
	            
	            if (!mHelper.subscriptionsSupported()) {
	                complain("Subscriptions not supported on your device yet. Sorry!");
	                return;
	            }
	            String payload = ""; 
	            setWaitScreen(true);
	            Log.d(TAG, "Launching purchase flow for monthly subscription.");
	            mHelper.launchPurchaseFlow(MainActivity.this,
	                    IAB_MONTH, IabHelper.ITEM_TYPE_SUBS, 
	                    RC_REQUEST, mPurchaseFinishedListener, payload);    
            }
    	});
        
        tv_pay_all.setOnClickListener(new View.OnClickListener() {
	        @Override
	        public void onClick(View v) {
	        	Log.d(TAG, "Yearly button clicked.");
	            
	            if (!mHelper.subscriptionsSupported()) {
	                complain("Subscriptions not supported on your device yet. Sorry!");
	                return;
	            }
	            String payload = ""; 
	            setWaitScreen(true);
	            Log.d(TAG, "Launching purchase flow for yearly subscription.");
	            mHelper.launchPurchaseFlow(MainActivity.this,
	                    IAB_YEAR, IabHelper.ITEM_TYPE_SUBS, 
	                    RC_REQUEST, mPurchaseFinishedListener, payload);	 
            }
    	});
        
        btn_restore.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}
		});
        
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();
	}

    ////////////////////////////////         purchase setup
    // Listener that's called when we finish querying the items and subscriptions we own
    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            Log.d(TAG, "Query inventory finished.");
            if (result.isFailure()) {
                complain("Failed to query inventory: " + result);
                return;
            }

            Log.d(TAG, "Query inventory was successful.");
            
            // Do we have the monthly pay plan?
            Purchase monthlyPurchase = inventory.getPurchase(IAB_MONTH);
            mSubscribedToMonthly = (monthlyPurchase != null && 
                    verifyDeveloperPayload(monthlyPurchase));
            Log.d(TAG, "User " + (mSubscribedToMonthly ? "HAS" : "DOES NOT HAVE") 
                        + " monthly subscription.");
            
         // Do we have the yearly pay plan?
            Purchase yearlyPurchase = inventory.getPurchase(IAB_MONTH);
            mSubscribedToYearly = (yearlyPurchase != null && 
                    verifyDeveloperPayload(yearlyPurchase));
            Log.d(TAG, "User " + (mSubscribedToYearly ? "HAS" : "DOES NOT HAVE") 
                        + " yearly subscription.");

            updateUi();
            setWaitScreen(false);
            Log.d(TAG, "Initial inventory query finished; enabling main UI.");
        }
    };
    
    /** Verifies the developer payload of a purchase. */
    boolean verifyDeveloperPayload(Purchase p) {
        String payload = p.getDeveloperPayload();
        Log.d(TAG, "Developer payload:  " + payload);
        return true;
    }

    // Callback for when a purchase is finished
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            Log.d(TAG, "Purchase finished: " + result + ", purchase: " + purchase);
            if (result.isFailure()) {
                complain("Error purchasing: " + result);
                setWaitScreen(false);
                return;
            }
            if (!verifyDeveloperPayload(purchase)) {
                complain("Error purchasing. Authenticity verification failed.");
                setWaitScreen(false);
                return;
            }

            Log.d(TAG, "Purchase successful.");

            if (purchase.getSku().equals(IAB_MONTH)) {
                // bought the monthly subscription
                Log.d(TAG, "Monthly subscription purchased.");
                alert("Thank you for subscribing to Monthly!");
                mSubscribedToMonthly = true;
                updateUi();
                setWaitScreen(false);
            } else if (purchase.getSku().equals(IAB_MONTH)) {
                // bought the yearly subscription
                Log.d(TAG, "Yearly subscription purchased.");
                alert("Thank you for subscribing to Yearly!");
                mSubscribedToYearly = true;
                updateUi();
                setWaitScreen(false);
            }
        }
    };

    // Called when consumption is complete
    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
        @Override
    	public void onConsumeFinished(Purchase purchase, IabResult result) {
            Log.d(TAG, "Consumption finished. Purchase: " + purchase + ", result: " + result);

            if (result.isSuccess()) {
                Log.d(TAG, "Consumption successful. Provisioning.");
                alert("You filled 1/4 tank. Your tank is now " + "/4 full!");
            }
            else {
                complain("Error while consuming: " + result);
            }
            updateUi();
            setWaitScreen(false);
            Log.d(TAG, "End consumption flow.");
        }
    };   
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);

        // Pass on the activity result to the helper for handling
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            // not handled, so handle it ourselves (here's where you'd
            // perform any handling of activity results not related to in-app
            // billing...
            super.onActivityResult(requestCode, resultCode, data);
        }
        else {
            Log.d(TAG, "onActivityResult handled by IABUtil.");
        }
    }
    
    void complain(String message) {
        Log.e(TAG, "**** TrivialDrive Error: " + message);
        alert("Error: " + message);
    }

    void alert(String message) {
        AlertDialog.Builder bld = new AlertDialog.Builder(this);
        bld.setMessage(message);
        bld.setNeutralButton("OK", null);
        Log.d(TAG, "Showing alert dialog: " + message);
        bld.create().show();
    }
    
    // updates UI to reflect model
    public void updateUi() {
        
    }
    
    void setWaitScreen(boolean set) {
        if (set) {
        	mProgressHUD = ProgressHUD.show(MainActivity.this,"Please wait ...", true,false,this);
        } else {
        	mProgressHUD.dismiss();
        }
    }
}
