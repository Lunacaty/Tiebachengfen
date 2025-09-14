# Tiebachengfen

一个用于查贴吧成员成分的Java程序

# 事前准备
1. 请下载最新Chrome浏览器并安装<a href="https://googlechromelabs.github.io/chrome-for-testing/#stable">ChromeDriver</a>,并将ChromeDriver.exe的路径添加到环境变量CHROME_DRIVER_PATH中.
2. 获取贴吧Cookie-BDUSS的值并放入环境变量BDUSS中

# 食用方法
运行项目,在控制台中根据引导输入贴吧名与查询页数,每页24个用户,最多查询500页(红颜的锅),注意,<s>用户需手动处理百度安全验证</s><s>这已经是过去式了,现在程序将会自动处理安全验证</s>这也已经是过去式了,现在程序不会遇到安全验证,如果你想查询1w2个用户,不必守在你的个人电脑的屏幕面前了,把PC开着,打个盹,吃个饭,如此自由.

# 项目讲解
爬取/bawu2/platform/listMemberInfo?word={forumName}&pn={page}"页面中的用户容器后依次爬取每一个用户