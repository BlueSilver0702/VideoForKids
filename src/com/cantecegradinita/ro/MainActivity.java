package com.cantecegradinita.ro;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.ViewAnimator;

import com.tekle.oss.android.animation.AnimationFactory;
import com.tekle.oss.android.animation.AnimationFactory.FlipDirection;

@SuppressLint("InflateParams") public class MainActivity extends Activity {

	ViewAnimator viewAnimator;
    
	LinearLayout ll_tab1, ll_tab2, tbl_tab2;
    TableLayout tbl_tab1;
    ScrollView sv_tab2;
    
    Button btn_setting, btn_setting_back, btn_setting_tab1, btn_setting_tab2, btn_setting_tab1_icon, btn_setting_tab2_icon;
//    boolean isTab1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        viewAnimator = (ViewAnimator)this.findViewById(R.id.viewFlipper);
        ll_tab1 = (LinearLayout)findViewById(R.id.ll_setting_tab1);
        ll_tab2 = (LinearLayout)findViewById(R.id.ll_setting_tab2);
        tbl_tab2 = (LinearLayout)findViewById(R.id.tbl_setting_tab2);
        tbl_tab1 = (TableLayout)findViewById(R.id.tbl_setting_tab1);
        sv_tab2 = (ScrollView)findViewById(R.id.sv_setting);
        
        btn_setting = (Button)findViewById(R.id.btn_main_setting);
        btn_setting_back = (Button)findViewById(R.id.btn_setting_back);
        btn_setting_tab1 = (Button)findViewById(R.id.btn_setting_tab1);
        btn_setting_tab2 = (Button)findViewById(R.id.btn_setting_tab2);
        btn_setting_tab1_icon = (Button)findViewById(R.id.btn_setting_tab1_icon);
        btn_setting_tab2_icon = (Button)findViewById(R.id.btn_setting_tab2_icon);
        
        for (int i=0; i<12; i++) {
        	final RelativeLayout tr=(RelativeLayout)LayoutInflater.from(MainActivity.this).inflate(R.layout.cell_setting, null);    

            @SuppressWarnings("deprecation")
			LinearLayout.LayoutParams layout_param= new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.FILL_PARENT,
                    this.convertDpToPixel(30));
            layout_param.bottomMargin = this.convertDpToPixel(7);
            tr.setBackgroundResource(R.drawable.setting_item_bg);
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
    
    public int convertDpToPixel(int dp){
        Resources resources = this.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return (int)px;
    }
}
