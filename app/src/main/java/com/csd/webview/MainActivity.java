package com.csd.webview;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

/**
 * 类名称: MainActivity
 * 类描述: 主页
 * 创建人: 陈书东
 * 创建时间: 2018/8/8 08:08
 */
public class MainActivity extends BaseActivity {

    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("主页");
        textView = (TextView) findViewById(R.id.textView);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyWebViewActivity.startActivity(MainActivity.this, "测试H5页面", "file:///android_asset/demo.html");
            }
        });
    }
}
