package com.cantecegradinita.ro;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        Button btn_setting = (Button)findViewById(R.id.Button03);
        Button btn_setting_back = (Button)findViewById(R.id.btn_setting_back);
        
        btn_setting.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				System.out.println(">>>>>>>>>>>>>>>>>> : ahahah");
			}
		});
        
        btn_setting_back.setOnClickListener(new OnClickListener() {
        	@Override
			public void onClick(View v) {
				System.out.println(">>>>>>>>>>>>>>>>>> : ahahah");
			}
        });
    }
}
