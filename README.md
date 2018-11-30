## WebView控件的坑
**Android开发加载H5页面的WebView控件就是一个深坑的存在：  
默认不允许执行JavaScript脚本  
H5页面宽度不适配手机  
H5的视频播放不能全屏  
H5中上传文件标签无响应导致不会拉起系统拍照与选择相册功能  
H5的localStorage本地存储无效果  
......等等一堆问题。**
## 解决方法
**博主封装WebView基类页面，继承基类即可实现：
允许执行JavaScript脚本功能、  
H5页面自适应屏幕功能、  
H5视频播放与全屏功能、  
上传文件标签的响应拉起系统拍照与选择相册功能（兼容Android7.0以上系统）、  
开启H5的localStorage本地存储功能、自定义UserAgent功能、WebView支持文件下载功能、  
自动播放音频autoplay功能、  
开启自动化测试功能等等  
以及JS交互功能演示。**  
**此外项目中还封装了自定义Dialog，自己可根据需求增加构造形参实现【弹窗标题是否展示、取消与确定按钮的展示内容、取消与确定按钮是否展示、取消与确定按钮颜色是否高亮】等动态配置功能。**
## 项目地址
**链接: [WebViewDemo](https://github.com/zang-chen/WebViewDemo)**
## 项目用到的依赖
**权限管理**  
**链接: [AndPermission](https://github.com/yanzhenjie/AndPermission)**  
**图片选择器**  
**链接: [PictureSelector](https://github.com/LuckSiege/PictureSelector)**  
## 其他
**第三方浏览器内核**  
**链接: [腾讯X5](https://x5.tencent.com/)**  
**链接: [Crosswalk](https://crosswalk-project.org/)**
