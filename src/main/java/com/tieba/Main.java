package com.tieba;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tieba.request.API;
import com.tieba.request.Response;
import lombok.SneakyThrows;
import org.htmlunit.WebClient;
import org.htmlunit.html.HtmlDivision;
import org.htmlunit.html.HtmlElement;
import org.htmlunit.html.HtmlPage;
import org.htmlunit.html.HtmlSpan;
import org.htmlunit.jetty.util.UrlEncoded;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

public class Main {
    Map<String, Integer> bars = new HashMap<>();
    int count = 0;
    static Scanner scanner = new Scanner(System.in);
    
    {
        bars.put("无效数据", 0);
        bars.put("有效数据", 0);
    }
    
    public static void main(String[] args) {
        Main main = new Main();
        System.out.println("请输入贴吧名称:");
        String forumName = scanner.nextLine();
        System.out.println("请输入页数:");
        int page = scanner.nextInt();
        scanner.nextLine();
        try {
            
            ChromeOptions options = new ChromeOptions();
            
            options.addArguments("--disable-javascript");
            options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36...");
            Map<String, Element> map = main.getPages(page, forumName);
            System.setProperty("webdriver.chrome.driver","E:\\Downloads\\chromedriver-win64\\chromedriver.exe");
            WebDriver driver = new ChromeDriver(options);
            map.forEach((k, v) -> {
                System.out.println("正在查询第"+k+"页数据...");
                main.getBars(v,driver);
            });
            driver.quit();
            
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<Map.Entry<String, Integer>> list = new ArrayList<>(main.bars.entrySet());
        list.sort((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()));
        for (Map.Entry<String, Integer> entry : list) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
        System.out.println("查询完成,键入回车退出...");
        scanner.nextLine();
        scanner.close();
    }
    
    
    @SneakyThrows
    public void getBars(Element container,WebDriver driver) {
        Elements users = container.select("span.member");
        Scanner inputScanner = new Scanner(System.in);
        Boolean flag = false;
        API api = new API();
        for (Element user : users) {
            System.out.println("正在查询第"+(count+1)+"个用户");
            count++;
            Element username = user.selectFirst(".user_name");
            String homeUrl = username.attr("href");
            Response res = null;
            if(!flag){
                res = api.GET(homeUrl);
            }
            String html = null;
            if(flag||res.getData().toString().contains("ç½\u0091ç»\u009Cä¸\u008Dç»\u0099å\u008A\u009Bï¼\u008Cè¯·ç¨\u008Då\u0090\u008Eé\u0087\u008Dè¯\u0095")){
                flag = true;
                driver.get(api.serverUrl + homeUrl);
                if(driver.getPageSource().contains("百度安全验证")){
                    System.out.println("检测到百度安全验证，请手动完成验证后按下回车键继续...");
                    scanner.nextLine();;
                }
                html = driver.getPageSource();
            } else {
                html = res.getData().toString();
            }
            Document doc = Jsoup.parse(html);
            Element likeForums = doc.selectFirst("#forum_group_wrap");
            String nickname = username.text();
            if (likeForums == null) {
                String usernameStr = username.attr("title");
                if (!usernameStr.isEmpty() && usernameStr.length() > 0) {
                    usernameStr = URLEncoder.encode(usernameStr, StandardCharsets.UTF_8.toString());
                    Response res1 = api.GET("/home/get/panel?un=" + usernameStr);
                    ObjectMapper mapper = new ObjectMapper();
                    LinkedHashMap<String, Object> result = null;
                    try {
                        result = mapper.readValue(res1.getData().toString(), new TypeReference<LinkedHashMap<String, Object>>() {});
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                    
                    Map<String, Object> grades = null;
                    try {
                        Map<String, Object> data = (LinkedHashMap<String, Object>) result.get("data");
                        Map<String, Object> honor = (LinkedHashMap<String, Object>) data.get("honor");
                        grades = (LinkedHashMap<String, Object>) honor.get("grade");
                    } catch (NullPointerException e) {
                        this.bars.put("无效数据", this.bars.get("无效数据") == null ? 1 : this.bars.get("无效数据") + 1);
                        System.out.print("昵称:" + nickname + " 活跃查询失败--空指针\n");
                        continue;
                    } catch (ClassCastException e){
                        this.bars.put("无效数据", this.bars.get("无效数据") == null ? 1 : this.bars.get("无效数据") + 1);
                        System.out.print("昵称:" + nickname + " 活跃查询失败--类型转换\n");
                        continue;
                    }
                    
                    
                    if(grades == null){
                        this.bars.put("无效数据", this.bars.get("无效数据") == null ? 1 : this.bars.get("无效数据") + 1);
                        System.out.print("昵称:" + nickname + " 活跃查询失败--空值\n");
                        continue;
                    }
                    grades.forEach((k, v) -> {
                        List<String> forumList = (List<String>) ((LinkedHashMap<String, Object>) (v)).get("forum_list");
                        for (String forum : forumList) {
                            this.bars.put(forum, this.bars.get(forum) == null ? 1 : this.bars.get(forum) + 1);
                        }
                    });
                    this.bars.put("有效数据", this.bars.get("有效数据") == null ? 1 : this.bars.get("有效数据") + 1);
                    System.out.print("昵称:" + nickname + " 活跃查询成功\n");
                    continue;
                }
                this.bars.put("无效数据", this.bars.get("无效数据") == null ? 1 : this.bars.get("无效数据") + 1);
                System.out.print("昵称:" + nickname + " 关注查询失败\n");
                continue;
            }
            Elements bars = likeForums.select(".u-f-item.unsign");
            for (Element bar : bars) {
                String barName = bar.selectFirst("span").text();
                this.bars.put(barName, this.bars.get(barName) == null ? 1 : this.bars.get(barName) + 1);
            }
            System.out.print("昵称:" + nickname + " 关注查询成功\n");
            this.bars.put("有效数据", this.bars.get("有效数据") == null ? 1 : this.bars.get("有效数据") + 1);
        }
    }
    
    
    public Map<String, Element> getPages(int page, String forumName) {
        System.out.println("正在获取第" + 1 + "页HTML...");
        //获取HTML
        API api = new API();
        try {
            forumName = URLEncoder.encode(forumName, "GBK");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Response res = api.GET("/bawu2/platform/listMemberInfo?word=" + forumName + "&pn=1");
        String html = res.getData().toString();
        
        Document doc = Jsoup.parse(html);
        
        //获取总页数并修正page
        Element totalPage = doc.selectFirst(".tbui_total_page");
        if (totalPage != null) {
            StringBuilder sb = new StringBuilder(totalPage.text());
            sb.deleteCharAt(0);
            sb.deleteCharAt(sb.length() - 1);
            if (page > Integer.parseInt(sb.toString())) {
                page = Integer.parseInt(sb.toString());
                System.out.println("页数过大,修正为：" + sb.toString());
            }
            if(page > 500){
                System.out.println("页数超过500,修正为500");
                page = 500;
            }
        } else {
            page = 1;
        }
        
        
        //获取会员容器
        Element container = doc.selectFirst(".forum_info_section.member_wrap.clearfix.bawu-info");
        Map<String, Element> containers = new HashMap<>();
        containers.put(1 + "", container);
        
        int nowPage = 2;
        while (nowPage <= page) {
            System.out.println("正在获取第" + nowPage + "页HTML...");
            Response response = api.GET("/bawu2/platform/listMemberInfo?word="+ forumName +"&pn=" + nowPage);
            doc = Jsoup.parse(response.getData().toString());
            
            Element newContainer = doc.selectFirst(".forum_info_section.member_wrap.clearfix.bawu-info");
            
            containers.put(nowPage + "", newContainer);
            nowPage++;
        }
        
        return containers;
    }
}