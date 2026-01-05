### 项目架构介绍

> 本项目采用`MVP`架构模式,使用`Dagger2`进行`Presenter`与`Model`的注入解耦,使用`Retrofit`+`OkHttp`+`RxJava`进行网络请求封装,使用`butterknife`注解框架来代替`findViewById`减少劳动力,使用`Glide`来进行图片加载操作,关于使用到的技术请自行学习.

- [MVP模式讲解](https://www.jianshu.com/p/479aca31d993)
- [Dagger讲解](https://www.jianshu.com/p/3ad9a110fdb5)
- [Retrofit讲解](https://www.jianshu.com/p/0fda3132cf98)
- [OkHttp讲解](https://github.com/square/okhttp)
- [RxJava讲解](https://juejin.im/post/5b17560e6fb9a01e2862246f)
- [Butterknife讲解](https://www.jianshu.com/p/3678aafdabc7)
- [Glide讲解](https://www.jianshu.com/p/34cac7ec531e)

### 项目中所用的第三方库

> 为了减少开发周期,本项目中使用到了`Github`优秀的第三方库来进行功能实现,以下列举本项目中使用到的第三方库以及`Github`地址,关于具体的使用细节,请自行查看`Github`链接中的`ReadMe`文档;

- **响应式编程库** [RxJava 2](https://github.com/ReactiveX/RxJava) - 基于观察者模式的异步编程库
- **Android响应式扩展** [RxAndroid](https://github.com/ReactiveX/RxAndroid) - 为Android提供RxJava的调度器
- **JSON解析库** [Gson](https://github.com/google/gson) - Google开发的Java对象与JSON相互转换库
- **CSV文件处理** [Apache Commons CSV](https://commons.apache.org/proper/commons-csv/) - Apache Commons项目的CSV文件读写库
- **串口通信库** [Android-SerialPort](https://github.com/licheedev/Android-SerialPort) - Android平台串口通信工具库
- **HTTP 网络请求库** [RxHttp](https://github.com/liujingxing/rxhttp/blob/master/README.md) - 基于 RxJava 和 Kotlin 协程的网络请求框架
- **HTTP 客户端** [OkHttp](https://square.github.io/okhttp/) - Square 公司开源的 HTTP 客户端
- **图片加载库** [Glide](https://github.com/bumptech/glide) - 高效的 Android 图片加载和缓存库
- **屏幕适配方案** [AndroidAutoSize](https://github.com/JessYanCoding/AndroidAutoSize) - JessYan 的屏幕适配方案
- **GIF 图片支持** [android-gif-drawable](https://github.com/koral--/android-gif-drawable) - Android 上的 GIF 图片显示库
- **RecyclerView 适配器** [BaseRecyclerViewAdapterHelper](https://github.com/CymChad/BaseRecyclerViewAdapterHelper) - 强大的 RecyclerView 适配器框架
- **轮播图组件** [banner](https://github.com/youth5201314/banner) - Android 图片轮播控件
- **Flexbox 布局** [flexbox-layout](https://github.com/google/flexbox-layout) - Google 的 Flexbox 布局实现
- **二维码扫描** [zxing-library](https://github.com/yipianfengye/android-zxingLibrary) - 基于 ZXing 的二维码扫描库
- **视频播放器** [ExoPlayer](https://github.com/google/ExoPlayer) - Google 开源的媒体播放器
- **Markdown 渲染** [Markwon](https://github.com/noties/Markwon) - Android 上的 Markdown 渲染库
- **相机 API** [CameraX](https://developer.android.com/training/camerax) - Android Jetpack 相机库
- **WebSocket 客户端** [Java-WebSocket](https://github.com/TooTallNate/Java-WebSocket) - Java 实现的 WebSocket 客户端/服务器
- **媒体管道解决方案** [MediaPipe](https://github.com/google/mediapipe) - Google 的跨平台机器学习解决方案
- **面部网格模型** [MediaPipe Face Mesh](https://google.github.io/mediapipe/solutions/face_mesh) - 面部关键点检测模型

### 项目目录结构介绍

- **[app](app)**(项目功能实现`Module`)
	- **app**(项目的全局生命周期配置)
	- **constant**(项目中的全局常量管理)
	- **mvp**(项目mvp模式功能实现)
	- **receiver**(项目中广播管理)
	- **utils**(项目中所用的工具类)
	- **widgets**(项目中所用的自定义控件)

- **[basemvp](basemvp)**(底层`MVP`模式的封装`Module`)
	- **base**(关于`BaseApplication/BaseActivity/BaseFragment`的封装)
	- **di**(存放着全局的`Dagger2`注入)
	- **integration**(关于全局统一配置/所有Activity的实例管理)
	- **mvp**(mvp架构封装)
	- **util**(工具类)

- **[native](native)**(SDK核心模块)
	- **cae**(CAE相关功能实现)
	- **callback**(回调接口定义)
	- **constant**(常量定义)
	- **data**(数据模型与处理)
	- **net**(网络请求相关)
	- **noetix**(Noetix组件实现)
	- **robot**(机器人相关功能)
	- **serial**(串口通信相关)
	- **MainSDK**(SDK主入口与核心逻辑)

- **[library](library)**(核心库模块)
	- **app**(全局生命周期与配置管理)
	- **arouter**(路由组件相关)
	- **bean**(数据实体类)
	- **constant**(全局常量管理)
	- **listener**(监听器接口定义)
	- **mvp**(MVP模式功能实现)
	- **net**(网络请求相关)
	- **noetix**(Noetix组件实现)
	- **receiver**(广播接收器管理)
	- **utils**(工具类集合)
	- **widget**(自定义控件)

- **[module_reception](module_reception)**(接待功能模块)
	- **bean**(数据实体类)
	- **mvp**(MVP模式功能实现)
		- **contract**(MVP契约接口定义)
		- **di.component**(依赖注入组件)
		- **presenter**(Presenter层实现)
		- **ui**(View层界面实现)
	- **net**(网络请求相关)

- **[module_explain](module_explain)**(讲解功能模块)
	- **bean**(数据实体类)
	- **mvp**(MVP模式功能实现)
		- **contract**(MVP契约接口定义)
		- **di.component**(依赖注入组件)
		- **presenter**(Presenter层实现)
		- **ui**(View层界面实现)
	- **net**(网络请求相关)

- **[module_setting](module_setting)**(设置功能模块)
	- **bean**(数据实体类)
	- **mvp**(MVP模式功能实现)
		- **contract**(MVP契约接口定义)
		- **di.component**(依赖注入组件)
		- **presenter**(Presenter层实现)
		- **ui**(View层界面实现)
	- **net**(网络请求相关)
	- **widget**(自定义控件)