package com.csd.webview;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.yanzhenjie.permission.Permission;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 类名称: BaseWebViewActivity
 * 类描述: 封装的WebView加载H5页面的基类
 * 创建人: 陈书东
 * 创建时间: 2018/8/8 08:08
 */
public class BaseWebViewActivity extends BaseActivity {

    protected FrameLayout videoView;
    protected WebView webView;
    protected WebChromeClient chromeClient;
    protected WebViewClient webViewClient;
    protected ProgressBar progressBar;

    public boolean islandport = false;
    public ValueCallback<Uri> mUploadMessage;// 表单的数据信息
    public ValueCallback<Uri[]> mUploadCallbackAboveL;
    public Uri imageUri;
    public final static int FILECHOOSER_REQUESTCODE = 1;// 表单的结果回调</span>

    public final static int CAMERACHOOSE_REQUESTCODE = 2;
    public final static int IMAGECHOOSE_REQUESTCODE = 3;
    public final static int VIDEOCHOOSE_REQUESTCODE = 4;
    public final static int AUDIOCHOOSE_REQUESTCODE = 5;

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        videoView = (FrameLayout) findViewById(R.id.videoView);
        webView = (WebView) findViewById(R.id.webView);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        if (webView != null) {
            initWebView();
            initWebChromeClient();
            initWebViewClient();
        }
    }

    private void initWebView() {
        WebSettings setting = webView.getSettings();

        //开启自动化测试
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webView.setWebContentsDebuggingEnabled(true);
        }

        //自定义UA
        String userAgent = setting.getUserAgentString();
        userAgent = userAgent + "WebViewDemo";
        setting.setUserAgentString(userAgent);

        /**
         *  Webview在安卓5.0之前默认允许其加载混合网络协议内容
         *  在安卓5.0之后，默认不允许加载http与https混合内容，需要设置webview允许其加载混合网络协议内容
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setting.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        //自动播放音频autoplay
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            setting.setMediaPlaybackRequiresUserGesture(false);
        }

        setting.setJavaScriptEnabled(true);//设置WebView是否允许执行JavaScript脚本,默认false
        setting.setSupportZoom(true);//WebView是否支持使用屏幕上的缩放控件和手势进行缩放,默认值true
        setting.setBuiltInZoomControls(true);//是否使用内置的缩放机制
        setting.setDisplayZoomControls(false);//使用内置的缩放机制时是否展示缩放控件,默认值true

        setting.setUseWideViewPort(true);//是否支持HTML的“viewport”标签或者使用wide viewport
        setting.setLoadWithOverviewMode(true);//是否允许WebView度超出以概览的方式载入页面,默认false
        setting.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);//设置布局,会引起WebView的重新布局(relayout),默认值NARROW_COLUMNS

        setting.setRenderPriority(WebSettings.RenderPriority.HIGH);//线程优先级(在API18以上已废弃。不建议调整线程优先级，未来版本不会支持这样做)
        setting.setEnableSmoothTransition(true);//已废弃,将来会成为空操作（no-op）,设置当panning或者缩放或者持有当前WebView的window没有焦点时是否允许其光滑过渡,若为true,WebView会选择一个性能最大化的解决方案。例如过渡时WebView的内容可能不更新。若为false,WebView会保持精度（fidelity）,默认值false。
        setting.setCacheMode(WebSettings.LOAD_NO_CACHE);//重写使用缓存的方式，默认值LOAD_DEFAULT
        setting.setPluginState(WebSettings.PluginState.ON);//在API18以上已废弃。未来将不支持插件,不要使用
        setting.setJavaScriptCanOpenWindowsAutomatically(true);//让JavaScript自动打开窗口,默认false

        //webview 中localStorage无效的解决方法
        setting.setDomStorageEnabled(true);//DOM存储API是否可用,默认false
        setting.setAppCacheMaxSize(1024 * 1024 * 8);//设置应用缓存内容的最大值
        String appCachePath = getApplicationContext().getCacheDir().getAbsolutePath();
        setting.setAppCachePath(appCachePath);//设置应用缓存文件的路径
        setting.setAllowFileAccess(true);//是否允许访问文件,默认允许
        setting.setAppCacheEnabled(true);//应用缓存API是否可用,默认值false,结合setAppCachePath(String)使用


        //支持文件下载
        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                try {
                    Uri uri = Uri.parse(url);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        webView.addJavascriptInterface(new AndroidJavaScript(), "csd");
    }

    protected void initWebViewClient() {
        webViewClient = new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return super.shouldOverrideUrlLoading(view, url);
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
//                注意：super句话一定要删除，或者注释掉，否则又走handler.cancel() 默认的不支持https的了。
//                super.onReceivedSslError(view, handler, error);
                handler.proceed();// 接受所有网站的证书
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }
        };
        webView.setWebViewClient(webViewClient);
    }

    protected void initWebChromeClient() {
        chromeClient = new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if (progressBar != null) {
                    if (newProgress == 100) {
                        progressBar.setVisibility(View.GONE);
                    } else {
                        progressBar.setVisibility(View.VISIBLE);
                        progressBar.setProgress(newProgress);
                    }
                }
            }


            // For Android >= 5.0
            @Override
            public boolean onShowFileChooser(WebView webView,
                                             ValueCallback<Uri[]> filePathCallback,
                                             FileChooserParams fileChooserParams) {
                mUploadCallbackAboveL = filePathCallback;
                openFile();
                return true;
            }

            // For Android < 3.0
            public void openFileChooser(ValueCallback<Uri> uploadMsg) {
                mUploadMessage = uploadMsg;
                openFile();
            }

            // For Android  >= 3.0
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
                mUploadMessage = uploadMsg;
                openFile();
            }

            //For Android  >= 4.1
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
                mUploadMessage = uploadMsg;
                openFile();
            }

            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                super.onShowCustomView(view, callback);
                if (islandport) {
                    return;
                }
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                webView.setVisibility(View.GONE);
                if (myView != null) {
                    callback.onCustomViewHidden();
                    return;
                }

                videoView.addView(view);
                myView = view;
                myCallback = callback;
                videoView.setVisibility(View.VISIBLE);
            }

            public View myView = null;
            public CustomViewCallback myCallback = null;

            @Override
            public void onHideCustomView() {
                super.onHideCustomView();
                if (myView == null) {
                    return;
                } else {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    myView.setVisibility(View.GONE);
                    videoView.removeView(myView);
                    videoView.setVisibility(View.GONE);
                    webView.setVisibility(View.VISIBLE);
                    myCallback.onCustomViewHidden();
                    myView = null;

                }
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
            }
        };
        webView.setWebChromeClient(chromeClient);
    }

    public class AndroidJavaScript {

        @JavascriptInterface
        public void getCamera(String s) {
            //注意:此处JS交互非UI线程
            Log.i("CSD", "getCamera:JS传递过来的值为" + s);
            openCamera();

        }

        @JavascriptInterface
        public void getImage(String s) {
            //注意:此处JS交互非UI线程
            Log.i("CSD", "getImage:JS传递过来的值为" + s);
            openImage();

        }

        @JavascriptInterface
        public void getVideo(String s) {
            //注意:此处JS交互非UI线程
            Log.i("CSD", "getVideo:JS传递过来的值为" + s);
            openVideo();

        }

        @JavascriptInterface
        public void getAudio(String s) {
            //注意:此处JS交互非UI线程
            Log.i("CSD", "getAudio:JS传递过来的值为" + s);
            openAudio();

        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            islandport = true;
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            islandport = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (webView != null) {
            webView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (webView != null) {
            webView.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webView != null) {
            webView.setVisibility(View.GONE);
            webView.loadUrl("about:blank");
            webView.stopLoading();
            webView.setWebChromeClient(null);
            webView.setWebViewClient(null);
            webView.destroy();
            webView = null;
        }
    }

    private void openFile() {
        //申请权限
        requestPermission(Constant.permission_CAMERA_READ_WRITE_EXTERNAL_STORAGE, Permission.CAMERA, Permission.READ_EXTERNAL_STORAGE, Permission.WRITE_EXTERNAL_STORAGE);

    }

    private void openCamera() {
        //申请权限
        requestPermission(Constant.permission_CAMERA, Permission.CAMERA, Permission.READ_EXTERNAL_STORAGE, Permission.WRITE_EXTERNAL_STORAGE);

    }

    private void openImage() {
        //申请权限
        requestPermission(Constant.permission_IMAGE, Permission.CAMERA, Permission.READ_EXTERNAL_STORAGE, Permission.WRITE_EXTERNAL_STORAGE);

    }

    private void openVideo() {
        //申请权限
        requestPermission(Constant.permission_VIDEO, Permission.CAMERA, Permission.READ_EXTERNAL_STORAGE, Permission.WRITE_EXTERNAL_STORAGE);

    }

    private void openAudio() {
        //申请权限
        requestPermission(Constant.permission_AUDIO, Permission.RECORD_AUDIO, Permission.READ_EXTERNAL_STORAGE, Permission.WRITE_EXTERNAL_STORAGE);

    }

    @Override
    protected void cameraReadWriteStorage() {
        File imageStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MyApp");
        if (!imageStorageDir.exists()) {
            imageStorageDir.mkdirs();
        }
        File file = new File(imageStorageDir + File.separator + "IMG_" + String.valueOf(System.currentTimeMillis()) + ".jpg");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            imageUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", file);
        } else {
            imageUri = Uri.fromFile(file);
        }

        final List<Intent> cameraIntents = new ArrayList<Intent>();
        final Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        final PackageManager packageManager = getPackageManager();
        final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for (ResolveInfo res : listCam) {
            final String packageName = res.activityInfo.packageName;
            final Intent i = new Intent(captureIntent);
            i.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            i.setPackage(packageName);
            i.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            cameraIntents.add(i);

        }
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");
        Intent chooserIntent = Intent.createChooser(i, "Image Chooser");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[]{}));
        startActivityForResult(chooserIntent, FILECHOOSER_REQUESTCODE);
    }

    @Override
    protected void camera() {
        Log.i("CSD", "camera方法走了");
        PictureSelector.create(BaseWebViewActivity.this)
                .openCamera(PictureMimeType.ofImage())
                .forResult(CAMERACHOOSE_REQUESTCODE);
    }

    @Override
    protected void chooseImage() {
        Log.i("CSD", "chooseImage方法走了");
        PictureSelector.create(BaseWebViewActivity.this)
                .openGallery(PictureMimeType.ofImage())
                .forResult(IMAGECHOOSE_REQUESTCODE);
    }

    @Override
    protected void chooseVideo() {
        Log.i("CSD", "chooseVideo方法走了");
        PictureSelector.create(BaseWebViewActivity.this)
                .openGallery(PictureMimeType.ofVideo())
                .forResult(VIDEOCHOOSE_REQUESTCODE);
    }

    @Override
    protected void chooseAudio() {
        Log.i("CSD", "chooseAudio方法走了");
        PictureSelector.create(BaseWebViewActivity.this)
                .openGallery(PictureMimeType.ofAudio())
                .forResult(AUDIOCHOOSE_REQUESTCODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == FILECHOOSER_REQUESTCODE) {
                if (null == mUploadMessage && null == mUploadCallbackAboveL) {
                    return;
                }
                Uri result = data == null || resultCode != RESULT_OK ? null : data.getData();
                if (mUploadCallbackAboveL != null) {
                    onActivityResultAboveL(requestCode, resultCode, data);
                } else if (mUploadMessage != null) {
                    if (result != null) {
                        String path = getPath(getApplicationContext(), result);
                        Uri uri = Uri.fromFile(new File(path));
                        mUploadMessage.onReceiveValue(uri);
                    } else {
                        mUploadMessage.onReceiveValue(imageUri);
                    }
                    mUploadMessage = null;
                }
            } else if (requestCode == CAMERACHOOSE_REQUESTCODE && data != null) {
                StringBuffer stringBuffer = new StringBuffer();
                // 图片、视频、音频选择结果回调
                List<LocalMedia> selectList = PictureSelector.obtainMultipleResult(data);
                // 例如 LocalMedia 里面返回三种path
                // 1.media.getPath(); 为原图path
                // 2.media.getCutPath();为裁剪后path，需判断media.isCut();是否为true  注意：音视频除外
                // 3.media.getCompressPath();为压缩后path，需判断media.isCompressed();是否为true  注意：音视频除外
                // 如果裁剪并压缩了，以取压缩路径为准，因为是先裁剪后压缩的
                for (int i = 0; i < selectList.size(); i++) {
                    Log.i("CSD", "拍照文件路径:" + selectList.get(i).getPath());
                    stringBuffer.append("file://" + selectList.get(i).getPath() + ",");
                }
                stringBuffer.deleteCharAt(stringBuffer.length() - 1);
                String result = stringBuffer.toString();
                //与JS方法 setCamera (camera) 交互
                webView.loadUrl("javascript:setCamera('" + result + "')");
            } else if (requestCode == IMAGECHOOSE_REQUESTCODE && data != null) {
                StringBuffer stringBuffer = new StringBuffer();
                // 图片、视频、音频选择结果回调
                List<LocalMedia> selectList = PictureSelector.obtainMultipleResult(data);
                // 例如 LocalMedia 里面返回三种path
                // 1.media.getPath(); 为原图path
                // 2.media.getCutPath();为裁剪后path，需判断media.isCut();是否为true  注意：音视频除外
                // 3.media.getCompressPath();为压缩后path，需判断media.isCompressed();是否为true  注意：音视频除外
                // 如果裁剪并压缩了，以取压缩路径为准，因为是先裁剪后压缩的
                for (int i = 0; i < selectList.size(); i++) {
                    Log.i("CSD", "图片文件路径:" + selectList.get(i).getPath());
                    stringBuffer.append("file://" + selectList.get(i).getPath() + ",");
                }
                stringBuffer.deleteCharAt(stringBuffer.length() - 1);
                String result = stringBuffer.toString();
                //与JS方法 setImage (image) 交互
                webView.loadUrl("javascript:setImage('" + result + "')");
            } else if (requestCode == VIDEOCHOOSE_REQUESTCODE && data != null) {
                StringBuffer stringBuffer = new StringBuffer();
                // 图片、视频、音频选择结果回调
                List<LocalMedia> selectList = PictureSelector.obtainMultipleResult(data);
                // 例如 LocalMedia 里面返回三种path
                // 1.media.getPath(); 为原图path
                // 2.media.getCutPath();为裁剪后path，需判断media.isCut();是否为true  注意：音视频除外
                // 3.media.getCompressPath();为压缩后path，需判断media.isCompressed();是否为true  注意：音视频除外
                // 如果裁剪并压缩了，以取压缩路径为准，因为是先裁剪后压缩的
                for (int i = 0; i < selectList.size(); i++) {
                    Log.i("CSD", "视频文件路径:" + selectList.get(i).getPath());
                    stringBuffer.append("file://" + selectList.get(i).getPath() + ",");
                }
                stringBuffer.deleteCharAt(stringBuffer.length() - 1);
                String result = stringBuffer.toString();
                //与JS方法 setVideo (video) 交互
                webView.loadUrl("javascript:setVideo('" + result + "')");
            } else if (requestCode == AUDIOCHOOSE_REQUESTCODE && data != null) {
                StringBuffer stringBuffer = new StringBuffer();
                // 图片、视频、音频选择结果回调
                List<LocalMedia> selectList = PictureSelector.obtainMultipleResult(data);
                // 例如 LocalMedia 里面返回三种path
                // 1.media.getPath(); 为原图path
                // 2.media.getCutPath();为裁剪后path，需判断media.isCut();是否为true  注意：音视频除外
                // 3.media.getCompressPath();为压缩后path，需判断media.isCompressed();是否为true  注意：音视频除外
                // 如果裁剪并压缩了，以取压缩路径为准，因为是先裁剪后压缩的
                for (int i = 0; i < selectList.size(); i++) {
                    Log.i("CSD", "音频文件路径:" + selectList.get(i).getPath());
                    stringBuffer.append("file://" + selectList.get(i).getPath() + ",");
                }
                stringBuffer.deleteCharAt(stringBuffer.length() - 1);
                String result = stringBuffer.toString();
                //与JS方法 setAudio (audio) 交互
                webView.loadUrl("javascript:setAudio('" + result + "')");
            }
        } else {
            if (requestCode == FILECHOOSER_REQUESTCODE) {
                if (mUploadCallbackAboveL != null) {
                    mUploadCallbackAboveL.onReceiveValue(null);
                    mUploadCallbackAboveL = null;
                } else if (mUploadMessage != null) {
                    mUploadMessage.onReceiveValue(null);
                    mUploadMessage = null;
                }
            }
        }
    }

    @SuppressWarnings("null")
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void onActivityResultAboveL(int requestCode, int resultCode, Intent data) {
        if (requestCode != FILECHOOSER_REQUESTCODE || mUploadCallbackAboveL == null) {
            return;
        }
        Uri[] results = null;
        if (resultCode == Activity.RESULT_OK) {
            if (data == null) {
                results = new Uri[]{imageUri};
            } else {
                String dataString = data.getDataString();
                ClipData clipData = data.getClipData();

                if (clipData != null) {
                    results = new Uri[clipData.getItemCount()];
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        ClipData.Item item = clipData.getItemAt(i);
                        results[i] = item.getUri();
                    }
                }

                if (dataString != null)
                    results = new Uri[]{Uri.parse(dataString)};
            }
        }
        if (results != null) {
            mUploadCallbackAboveL.onReceiveValue(results);
            mUploadCallbackAboveL = null;
        } else {
            results = new Uri[]{imageUri};
            mUploadCallbackAboveL.onReceiveValue(results);
            mUploadCallbackAboveL = null;
        }
        return;
    }


    @SuppressLint("NewApi")
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String getPath(final Context context, final Uri uri) {
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }


    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
}
