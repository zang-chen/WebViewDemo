package com.csd.webview.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.csd.webview.R;

/**
 * 类名称: CSDDialogwithBtn
 * 类描述: 自定义Dialog
 * 创建人: 陈书东
 * 创建时间: 2016/9/12 15:14
 * 修改人: 无
 * 修改时间: 无
 * 修改备注: 无
 */
public class CustomDialogwithBtn extends Dialog {

    private Context context;
    private LinearLayout ll_title;
    private TextView tv_title;
    private TextView tv_content;
    private TextView tv_cancel;
    private TextView tv_ok;

    private String title;
    private String content;
    private String cancel;
    private String ok;
    private boolean cancelShow = true;//是否显示取消按键
    private boolean okShow = true;//是否确认取消按键
    private boolean cancelShowColor;//取消按键是否变为蓝色字体
    private boolean okShowColor;//确认按键是否变为蓝色字体
    private boolean cancelable = true;//点击物理返回键dialog是否消失
    private boolean canceledOnTouchOutside = true;//如果为false:点击屏幕dialog不消失
    private View.OnClickListener cancelListener;//取消按键点击事件
    private View.OnClickListener okListener;//确认按键点击事件
    private ImageView iv_line;

    public CustomDialogwithBtn(Context context) {
        super(context, R.style.MineMyDialogStyleBottom);
        this.context = context;
    }

    public CustomDialogwithBtn(Context context, String title, String content, String cancel, String ok, boolean cancelShow, boolean okShow, boolean cancelShowColor, boolean okShowColor) {
        this(context);
        this.title = title;
        this.content = content;
        this.cancel = cancel;
        this.ok = ok;
        this.cancelShow = cancelShow;
        this.okShow = okShow;
        this.cancelShowColor = cancelShowColor;
        this.okShowColor = okShowColor;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialogwithbtn);
        initView();
        initData();
//        getWindow().setGravity(Gravity.CENTER);
        WindowManager m = getWindow().getWindowManager();
        Display d = m.getDefaultDisplay();
        WindowManager.LayoutParams p = getWindow().getAttributes();
//        p.height = (int) (d.getHeight() * 0.8);
        p.width = (int) (d.getWidth() * 0.8); //设置dialog的宽度为当前手机屏幕的宽度
        getWindow().setAttributes(p);
    }

    private void initView() {
        ll_title = (LinearLayout) findViewById(R.id.ll_title);
        tv_title = (TextView) findViewById(R.id.tv_title);
        tv_content = (TextView) findViewById(R.id.tv_content);
        tv_cancel = (TextView) findViewById(R.id.tv_cancel);
        tv_ok = (TextView) findViewById(R.id.tv_ok);
        iv_line = (ImageView) findViewById(R.id.iv_line);
    }

    private void initData() {
        if (TextUtils.isEmpty(title)) {
            ll_title.setVisibility(View.GONE);
        } else {
            tv_title.setText(title);
        }
        tv_content.setMovementMethod(ScrollingMovementMethod.getInstance());
        tv_content.setText(content);
        tv_content.post(new Runnable() {
            @Override
            public void run() {
                int txtPart = tv_content.getLineCount();
                if (txtPart > 1) {
                    tv_content.setGravity(Gravity.START);
                } else {
                    tv_content.setGravity(Gravity.CENTER);
                }
            }
        });
        if (cancelShow) {
            tv_cancel.setText(cancel);
            tv_cancel.setOnClickListener(getCancelListener());
        } else {
            iv_line.setVisibility(View.GONE);
            tv_cancel.setVisibility(View.GONE);
        }
        if (okShow) {
            tv_ok.setText(ok);
            tv_ok.setOnClickListener(getOkListener());
        } else {
            tv_ok.setVisibility(View.GONE);
        }
        if (cancelShowColor) {
            tv_cancel.setTextColor(context.getResources().getColor(R.color.home_blue));
        }
        if (okShowColor) {
            tv_ok.setTextColor(context.getResources().getColor(R.color.home_blue));
        }

        setCancelable(cancelable);
        setCanceledOnTouchOutside(canceledOnTouchOutside);
    }

    public void setCancelListener(View.OnClickListener cancelListener) {
        this.cancelListener = cancelListener;
    }

    public View.OnClickListener getOkListener() {
        return okListener;
    }

    public void setOkListener(View.OnClickListener okListener) {
        this.okListener = okListener;
    }

    public View.OnClickListener getCancelListener() {
        return cancelListener;
    }
}
