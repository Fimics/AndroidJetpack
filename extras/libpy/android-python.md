### Chaquopy 理论上支持所有纯 Python 库和部分 C 扩展库，但有架构限制
### Chaquopy 使用的是 CPython（官方 C 实现），要编需译成 Android 可运行的 .so 文件、


# 最佳实践建议
┌─────────────────────────────────┐
│    UI 层 (Kotlin/Jetpack)       │
├─────────────────────────────────┤
│  性能层 (JNI/C++)  │  逻辑层 (Python) │
│  - 图像处理         │  - 业务规则      │
│  - 音频编解码       │  - 数据分析      │
│  - 加密运算         │  - 算法原型      │
└─────────────────────────────────┘



````python
数据科学 & 数学
numpy          # 数组计算（已适配 ARM）
pandas         # 数据分析
scipy          # 科学计算
matplotlib     # 数据可视化
pillow         # 图像处理
opencv-python  # 计算机视觉（需特殊配置）
scikit-learn   # 机器学习
statsmodels    # 统计建模
sympy          # 符号数学
````

````python
数据科学 & 数学
numpy          # 数组计算（已适配 ARM）
pandas         # 数据分析
scipy          # 科学计算
matplotlib     # 数据可视化
pillow         # 图像处理
opencv-python  # 计算机视觉（需特殊配置）
scikit-learn   # 机器学习
statsmodels    # 统计建模
sympy          # 符号数学
````

````python
网络 & API
requests       # HTTP 客户端
urllib3        # HTTP 底层库
httpx          # 异步 HTTP
aiohttp        # 异步 HTTP 框架
beautifulsoup4 # HTML 解析
lxml           # XML/HTML 解析
html5lib       # HTML5 解析
selenium       # 浏览器自动化（需配合 WebView）
pywebview      # Web 界面（受限）

````


````python
数据库
sqlite3        # SQLite（内置）
sqlalchemy     # ORM 框架
peewee         # 轻量 ORM
pymongo        # MongoDB 客户端
redis          # Redis 客户端

````

````python
数据科学 & 数学
````