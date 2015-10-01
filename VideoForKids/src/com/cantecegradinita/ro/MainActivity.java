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

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView.OnEditorActionListener;
import android.widget.ViewAnimator;

import com.cantecegradinita.utils.DBHelperFraction;
import com.cantecegradinita.utils.Global;
import com.cantecegradinita.utils.Video;
import com.cantecegradinita.utils.VideoDB;
import com.kyleduo.switchbutton.SwitchButton;
import com.tekle.oss.android.animation.AnimationFactory;
import com.tekle.oss.android.animation.AnimationFactory.FlipDirection;

public class MainActivity extends Activity {

	private DBHelperFraction dbHelper;
	
	ArrayList<VideoDB> videoList;
	ViewAnimator viewAnimator;
    
	LinearLayout ll_tab1, ll_tab2, tbl_tab2, ll_main_tb;
    TableLayout tbl_tab1;
    ScrollView sv_tab2;
    
    Button btn_setting, btn_setting_back, btn_setting_tab1, btn_setting_tab2, btn_setting_tab1_icon, btn_setting_tab2_icon;
    Button btn_main_total, btn_main_some, btn_main_total_icon, btn_main_some_icon, btn_search_cancel;
    EditText et_search;
    private SwitchButton sb_set1, sb_set2, sb_set3, sb_set4, sb_set5, sb_set6;
    
    boolean is_download = false;
    boolean setting_bgmusic;
    boolean setting_soundeffect;
    boolean setting_subtitles;
    boolean setting_searchfield;
    boolean setting_shuffle;
    boolean setting_autoplay;
    
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
//    boolean isTab1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        sharedPreferences = getSharedPreferences(Global.app,Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
		boolean downloaded = sharedPreferences.getBoolean("downloaded", false);
		setting_bgmusic = sharedPreferences.getBoolean("s_bgmusic", true);
		setting_soundeffect = sharedPreferences.getBoolean("s_soundeffect", true);
		setting_subtitles = sharedPreferences.getBoolean("s_subtitles", true);
		setting_searchfield = sharedPreferences.getBoolean("s_searchfield", true);
		setting_shuffle = sharedPreferences.getBoolean("s_shuffle", true);
		setting_autoplay = sharedPreferences.getBoolean("s_autoplay", true);
		
		dbHelper = new DBHelperFraction(this);
        dbHelper.getWritableDatabase();
        
        
        
        videoList = dbHelper.getAllVideos(false);
        
        setupUI();
        
		if (!downloaded) {
			DownloadFileFromURL downloadHD = new DownloadFileFromURL();
	        downloadHD.out_filename = Global.plist_path;
	        downloadHD.execute(Global.file_url);
	        while(downloadHD.isSuccess);
		    editor.putBoolean("downloaded", true);	    
		    editor.commit();
			
			List<Video> listArr = null;
			ProductsPlistParsing parseHandler = new ProductsPlistParsing(this);
			try {
				listArr = parseHandler.getProductsPlistValues(Global.plist_path);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (XmlPullParserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			dbHelper.emptyTable();
			for (int i=0; i<listArr.size(); i++) {
				VideoDB row = new VideoDB();
				Video videoRow = listArr.get(i);
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
		
		for (int j=0; j<videoList.size(); j++) {
			VideoDB row = videoList.get(j);
			if (!row.thumb) {
				DownloadFileFromURL thumbHD = new DownloadFileFromURL();
				thumbHD.out_filename = Global.file_dir + row.image;
//				thumbHD.out_filename.replace("-", "_");
		        thumbHD.execute(Global.server_path+row.image);
		        while(thumbHD.isSuccess);
		        dbHelper.updateData(row.name, row.paid, row.download, true, row.fail);
			}
		}
		
		videoList = dbHelper.getAllVideos(false);
        refreshVideos();
    }
    
    @SuppressWarnings("deprecation")
	@SuppressLint({ "InflateParams", "NewApi" }) public void refreshVideos() {
    	
    	updateVideo();
    	ll_main_tb.removeAllViews();
    	
    	for (int j=0; j<Math.round(videoList.size()/2.0); j++) {
        	final LinearLayout tr=(LinearLayout)LayoutInflater.from(MainActivity.this).inflate(R.layout.cell_main, null);    
			LinearLayout.LayoutParams layout_param= new LinearLayout.LayoutParams(
					Global.convertDpToPixel(170, this),
					LinearLayout.LayoutParams.MATCH_PARENT);
            layout_param.rightMargin = Global.convertDpToPixel(16, this);
            
            final VideoDB videoRow1 = videoList.get(j*2);
            RelativeLayout rl_bg1 = (RelativeLayout)tr.findViewById(R.id.rl_main_img1);
            Resources res1 = getResources();
            Bitmap bitmap1 = BitmapFactory.decodeFile(Global.file_dir+videoRow1.image);
            BitmapDrawable bd1 = new BitmapDrawable(res1, bitmap1);
//            bitmap1.recycle();

            rl_bg1.setBackgroundDrawable(bd1);
            Button tempb1 = (Button)tr.findViewById(R.id.btn_main_cell_1);
            if (videoRow1.download) {
            	tempb1.setVisibility(View.INVISIBLE);
            	rl_bg1.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
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
            }
            TextView tv_main1 = (TextView)tr.findViewById(R.id.tv_main_text1);
            tv_main1.setText(videoRow1.name);
            
            tempb1.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
					builder.setMessage("Are you sure to download this video?")
				       .setTitle("Confirm");
					// Add the buttons
					builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					           public void onClick(DialogInterface dialog, int id) {
					               // User clicked OK button
					        	   DownloadFileFromURL downloadHD = new DownloadFileFromURL();
						   	       downloadHD.out_filename = Global.file_dir+videoRow1.video;
						   	       downloadHD.videoInfo = videoRow1;
						   	       downloadHD.execute(Global.server_path+videoRow1.video);
						   	       Toast.makeText(MainActivity.this, "Downloading "+videoRow1.name,Toast.LENGTH_LONG).show();
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
			});
            
            if (j*2+1 < videoList.size()) {
            	final VideoDB videoRow2 = videoList.get(j*2+1);
                RelativeLayout rl_bg2 = (RelativeLayout)tr.findViewById(R.id.rl_main_img2);
                Resources res2 = getResources();
                Bitmap bitmap2 = BitmapFactory.decodeFile(Global.file_dir+videoRow2.image);
                BitmapDrawable bd2 = new BitmapDrawable(res2, bitmap2);
//                bitmap2.recycle();

                rl_bg2.setBackgroundDrawable(bd2);

                Button tempb2 = (Button)tr.findViewById(R.id.btn_main_cell_2);
                if (videoRow2.download) {
                	tempb2.setVisibility(View.INVISIBLE);
                	rl_bg2.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
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
                }
                TextView tv_main2 = (TextView)tr.findViewById(R.id.tv_main_text2);
                tv_main2.setText(videoRow2.name);
                
                tempb2.setOnClickListener(new OnClickListener() {
    				@Override
    				public void onClick(View v) {
    					AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
    					builder.setMessage("Are you sure to download this video?")
    				       .setTitle("Confirm");
    					// Add the buttons
    					builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
    					           public void onClick(DialogInterface dialog, int id) {
    					               // User clicked OK button
    					        	   DownloadFileFromURL downloadHD = new DownloadFileFromURL();
    						   	       downloadHD.out_filename = Global.file_dir+videoRow2.video;
    						   	       downloadHD.videoInfo = videoRow2;
    						   	       downloadHD.execute(Global.server_path+videoRow2.video);
    						   	       Toast.makeText(MainActivity.this, "Downloading "+videoRow2.name,Toast.LENGTH_LONG).show();
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

//    					Intent sd=new Intent(MainActivity.this,PlayerActivity.class);
//    			        startActivity(sd);
    				}
    			});
            } else {
            	RelativeLayout rl_second = (RelativeLayout)tr.findViewById(R.id.rl_main_second);
            	rl_second.setVisibility(View.INVISIBLE);
            }
            
        	ll_main_tb.addView(tr, layout_param);

        }
    }
    
    public void updateVideo() {
    	if (is_download)
    		videoList = dbHelper.searchData(et_search.getText().toString(), true);
    	else 
    		videoList = dbHelper.searchData(et_search.getText().toString(), false);
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
        viewAnimator = (ViewAnimator)this.findViewById(R.id.viewFlipper);
        
        //  Setting Page
        ll_tab1 = (LinearLayout)findViewById(R.id.ll_setting_tab1);
        ll_tab2 = (LinearLayout)findViewById(R.id.ll_setting_tab2);
        tbl_tab2 = (LinearLayout)findViewById(R.id.tbl_setting_tab2);
        ll_main_tb = (LinearLayout)findViewById(R.id.ll_main_tb);
        tbl_tab1 = (TableLayout)findViewById(R.id.tbl_setting_tab1);
        sv_tab2 = (ScrollView)findViewById(R.id.sv_setting);
        btn_setting = (Button)findViewById(R.id.btn_main_setting);
        btn_setting_back = (Button)findViewById(R.id.btn_setting_back);
        btn_setting_tab1 = (Button)findViewById(R.id.btn_setting_tab1);
        btn_setting_tab2 = (Button)findViewById(R.id.btn_setting_tab2);
        btn_setting_tab1_icon = (Button)findViewById(R.id.btn_setting_tab1_icon);
        btn_setting_tab2_icon = (Button)findViewById(R.id.btn_setting_tab2_icon);
        
        //	Main Page
        btn_main_total = (Button)findViewById(R.id.btn_main_total);
        btn_main_some = (Button)findViewById(R.id.btn_main_some);
        btn_main_total_icon = (Button)findViewById(R.id.btn_main_total_icon);
        btn_main_some_icon = (Button)findViewById(R.id.btn_main_some_icon);
        btn_search_cancel = (Button)findViewById(R.id.btn_search_cancel);
        et_search = (EditText)findViewById(R.id.et_search);
        
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

        sb_set1.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked)
					editor.putBoolean("s_bgmusic", true);
				else
					editor.putBoolean("s_bgmusic", false);	    
			    editor.commit();
			}	
		});
        sb_set2.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked)
					editor.putBoolean("s_soundeffect", true);
				else
					editor.putBoolean("s_soundeffect", false);	    
			    editor.commit();
			}	
		});
        sb_set3.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked)
					editor.putBoolean("s_subtitles", true);
				else
					editor.putBoolean("s_subtitles", false);	    
			    editor.commit();
			}	
		});
        sb_set4.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked)
					editor.putBoolean("s_searchfield", true);
				else
					editor.putBoolean("s_searchfield", false);	    
			    editor.commit();
			}	
		});
        sb_set5.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked)
					editor.putBoolean("s_shuffle", true);
				else
					editor.putBoolean("s_shuffle", false);	    
			    editor.commit();
			}	
		});
        sb_set6.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked)
					editor.putBoolean("s_autoplay", true);
				else
					editor.putBoolean("s_autoplay", false);	    
			    editor.commit();
			}	
		});
        
        for (int i=0; i<videoList.size(); i++) {
        	final RelativeLayout tr=(RelativeLayout)LayoutInflater.from(MainActivity.this).inflate(R.layout.cell_setting, null);    

            @SuppressWarnings("deprecation")
			LinearLayout.LayoutParams layout_param= new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.FILL_PARENT,
                    Global.convertDpToPixel(36, this));
            layout_param.bottomMargin = Global.convertDpToPixel(12, this);
            tr.setBackgroundResource(R.drawable.setting_item_bg);
            final VideoDB row = videoList.get(i);
            TextView tv_setting_download_title = (TextView)tr.findViewById(R.id.tv_setting_row);
            final TextView tv_setting_download_corrupted = (TextView)tr.findViewById(R.id.tv_setting_corrupted);
            final Button bt_setting_download = (Button)tr.findViewById(R.id.btn_setting_row);
            tv_setting_download_title.setText(row.name);
            if (row.fail) tv_setting_download_corrupted.setVisibility(View.VISIBLE);
            else tv_setting_download_corrupted.setVisibility(View.INVISIBLE);
            if (row.download) bt_setting_download.setBackgroundResource(R.drawable.btn_delete);
            else bt_setting_download.setBackgroundResource(R.drawable.btn_download);
            bt_setting_download.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if (row.download) {
						dbHelper.updateData(row.name, row.paid, false, row.thumb, false);
						updateVideo();
						refreshVideos();
						bt_setting_download.setBackgroundResource(R.drawable.btn_download);
						tv_setting_download_corrupted.setVisibility(View.INVISIBLE);
					} else {
						AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
						builder.setMessage("Are you sure to download this video?")
					       .setTitle("Confirm");
						// Add the buttons
						builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						           public void onClick(DialogInterface dialog, int id) {
						               // User clicked OK button
						        	   DownloadFileFromURL downloadHD = new DownloadFileFromURL();
							   	       downloadHD.out_filename = Global.file_dir+row.video;
							   	       downloadHD.videoInfo = row;
							   	       downloadHD.execute(Global.server_path+row.video);
							   	       Toast.makeText(MainActivity.this, "Downloading "+row.name,Toast.LENGTH_LONG).show();
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
        
                
        onChanged(true);
        
    	btn_setting.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				System.out.println(">>>>>>>>>>>>>>>>>> : ahahah");
				AnimationFactory.flipTransition(viewAnimator, FlipDirection.RIGHT_LEFT);
			}
		});
        
        btn_setting_back.setOnClickListener(new OnClickListener() {
        	@Override
			public void onClick(View v) {
				System.out.println(">>>>>>>>>>>>>>>>>> : ahahah");
				AnimationFactory.flipTransition(viewAnimator, FlipDirection.RIGHT_LEFT);
			}
        });
        
        btn_setting_tab1.setOnClickListener(new OnClickListener() {
        	@Override
			public void onClick(View v) {
				onChanged(true);
			}
        });
        
        btn_setting_tab1_icon.setOnClickListener(new OnClickListener() {
        	@Override
			public void onClick(View v) {
				onChanged(true);
			}
        });
        
        btn_setting_tab2.setOnClickListener(new OnClickListener() {
        	@Override
			public void onClick(View v) {
				onChanged(false);
			}
        });
        
        btn_setting_tab2_icon.setOnClickListener(new OnClickListener() {
        	@Override
			public void onClick(View v) {
				onChanged(false);
			}
        });
        
        btn_main_total.setOnClickListener(new OnClickListener() {
        	@Override
			public void onClick(View v) {
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
				btn_search_cancel.setVisibility(View.VISIBLE);
				if (actionId == EditorInfo.IME_ACTION_DONE) {
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
    }
    /**
     * Background Async Task to download file
     * */
    class DownloadFileFromURL extends AsyncTask<String, String, String> {

    	public boolean isSuccess = true;
    	public String out_filename;
    	public VideoDB videoInfo = null;
    	public int percentage = 0;
        /**
         * Before starting background thread Show Progress Bar Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//            showDialog(progress_bar_type);
        }

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
                    // publishing the progress....
                    // After this onProgressUpdate will be called
                    publishProgress("" + (int) ((total * 100) / lenghtOfFile));
                    percentage = (int)((total * 100) / lenghtOfFile);
                    Log.println(1, "hello", "uploading....  " + percentage);
                    // writing data to file
                    output.write(data, 0, count);
                }

                // flushing output
                output.flush();
                
                isSuccess = false;
                if (videoInfo != null) {
                	dbHelper.updateData(videoInfo.name, videoInfo.paid, true, videoInfo.thumb, videoInfo.fail);
                	refreshVideos();
//                	Toast.makeText(MainActivity.this, "Downloading Completed!",Toast.LENGTH_LONG).show();
                }
                // closing streams
                output.close();
                input.close();

            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
                if (percentage > 0 && videoInfo != null) {
                	dbHelper.updateData(videoInfo.name, videoInfo.paid, true, videoInfo.thumb, true);
                	refreshVideos();
//                	Toast.makeText(MainActivity.this, "Downloading Failed!",Toast.LENGTH_LONG).show();
                }
            }

            return null;
        }
    }
    
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
    
    @Override
    protected void onResume() {
    	dbHelper.getWritableDatabase();
    	super.onResume();
    }

    @Override
    protected void onPause() {
    	dbHelper.close();
    	super.onPause();
    }
}
