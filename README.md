# Tiebachengfen

一个用于查贴吧成员成分的Java程序

# 事前准备
请下载最新Chrome浏览器并安装<a href="https://googlechromelabs.github.io/chrome-for-testing/#stable">ChromeDriver</a>,并将ChromeDriver放入E:\Downloads\chromedriver-win64\文件夹下

# 食用方法
运行项目,在控制台中根据引导输入贴吧名与查询页数,每页24个用户,最多查询500页,注意,用户需手动处理百度安全验证

# 项目讲解
爬取/bawu2/platform/listMemberInfo?word={forumName}&pn={page}"页面中的用户容器后依次爬取每一个用户的主页,如果用户公开关注则直接获取显示在PC端网页中的八个吧,如果用户不公开关注,则爬取/home/get/panel?un={username}页面中返回的JSON数据,从中获取该用户等级在6以及以上的吧.若两次查询均失败,则为无效数据