# Tiebachengfen

一个用于查贴吧成员成分的Java程序

# 事前准备
请下载最新Chrome浏览器并安装<a href="https://googlechromelabs.github.io/chrome-for-testing/#stable">ChromeDriver</a>,并将ChromeDriver放入E:\Downloads\chromedriver-win64\(什么?你的电脑没有E盘,天呐,手动输入路径我可能会在下次优化代码的时候做--那可能得很久以后了.当然,如果你能请一顿饭,我荣幸为您效劳--或者,告诉我有一个获取公开关注用户的所有关注贴吧的百度官方接口与它的用法(其实,获取所有贴吧--这就是下次更新代码的内容))文件夹下

# 食用方法
运行项目,在控制台中根据引导输入贴吧名与查询页数,每页24个用户,最多查询500页(红颜的锅),注意,<s>用户需手动处理百度安全验证</s>这已经是过去式了,现在程序将会自动处理安全验证,如果你想查询1w2个用户,不必守在你的个人电脑的屏幕面前了,把PC开着,打个盹,吃个饭,如此自由.

# 项目讲解
爬取/bawu2/platform/listMemberInfo?word={forumName}&pn={page}"页面中的用户容器后依次爬取每一个用户的主页,如果用户公开关注则直接获取显示在PC端网页中的八个吧,如果用户不公开关注,则爬取/home/get/panel?un={username}页面中返回的JSON数据,从中获取该用户等级在6以及以上的吧.若两次查询均失败,则为无效数据