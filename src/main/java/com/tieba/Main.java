package com.tieba;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tieba.request.API;
import com.tieba.request.Response;
import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.JavascriptException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class Main {
    Map<String, Integer> bars = new HashMap<>();
    Map<String, Integer> barsForQualified = new HashMap<>();
    int count = 0;
    static Scanner scanner = new Scanner(System.in);
    
    {
        bars.put("无效数据", 0);
        bars.put("有效数据", 0);
    }
    
    public static void main(String[] args) {
        Main main = new Main();
        //在IDEA中运行时，自行设置环境变量
        boolean isIdea = System.getenv("RUNNING_IN_IDEA") == null ? false : true;
        String encoding = isIdea ? "UTF-8" : "GBK";
        System.out.println((isIdea ? "" : "非") + "IDEA环境" + "选择编码: " + encoding);
        System.out.println("请输入贴吧名称:");
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(System.in, encoding)
            );
        } catch (UnsupportedEncodingException e) {
            System.out.println("编码错误");
            return;
        }
        final String forumName;
        try {
            forumName = reader.readLine();
        } catch (IOException e) {
            System.out.println("输入错误");
            return;
        }
        
        System.out.println("将查询 " + forumName + " 吧");
        System.out.println("请输入页数:");
        int page = scanner.nextInt();
        scanner.nextLine();
        try {
            
            ChromeOptions options = new ChromeOptions();
            
            options.setPageLoadStrategy(PageLoadStrategy.NONE);
            options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36...");
            Map<String, Element> map = main.getPages(page, forumName);
            System.setProperty("webdriver.chrome.driver", "E:\\Downloads\\chromedriver-win64\\chromedriver.exe");
            WebDriver driver = new ChromeDriver(options);
            JavascriptExecutor js = (JavascriptExecutor) driver;
            
            map.forEach((k, v) -> {
                System.out.println("正在查询第" + k + "页数据...");
                main.getBars(v, driver, js, forumName);
            });
            driver.quit();
            
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<Map.Entry<String, Integer>> list = new ArrayList<>(main.bars.entrySet());
        list.sort((entry1, entry2) -> -entry2.getValue().compareTo(entry1.getValue()));
        List<Map.Entry<String, Integer>> listForQualified = new ArrayList<>(main.barsForQualified.entrySet());
        listForQualified.sort((entry1, entry2) -> -entry2.getValue().compareTo(entry1.getValue()));
        for (Map.Entry<String, Integer> entry : list) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
        System.out.println("查询完成,键入回车查看偏爱该吧用户(查到的成分中含有" + forumName + "吧)成分...");
        scanner.nextLine();
        for (Map.Entry<String, Integer> entry : listForQualified) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
        System.out.println("查询完成,键入回车退出...");
        scanner.nextLine();
        scanner.close();
    }
    
    
    @SneakyThrows
    public void getBars(Element container, WebDriver driver, JavascriptExecutor js, String forumName) {
        Elements users = container.select("span.member");
        Scanner inputScanner = new Scanner(System.in);
        Boolean flag = false;
        API api = new API();
        for (Element user : users) {
            
            System.out.println("正在查询第" + (count + 1) + "个用户");
            count++;
            Element username = user.selectFirst(".user_name");
            String homeUrl = username.attr("href");
            Response res = null;
            if (!flag) {
                res = api.GET(homeUrl);
            }
            String html = null;
            if (flag || res.getData().toString().contains("ç½\u0091ç»\u009Cä¸\u008Dç»\u0099å\u008A\u009Bï¼\u008Cè¯·ç¨\u008Då\u0090\u008Eé\u0087\u008Dè¯\u0095")) {
                flag = true;
                driver.get(api.serverUrl + homeUrl);
                while (true) {
                    try {
                        if (driver.getPageSource().contains("百度安全验证")) {
                            System.out.println("检测到百度安全验证，尝试自动处理...");
                            do {
                                System.out.println("模拟滑动");
                                js.executeScript("""
                                        console.log('开始模拟滑动验证')
                                                                                    function simulateSlideVerification(
                                                                                      selector = '.passMod_slide-btn.passMod_slide-btn-loading',
                                                                                      baseX = 395,
                                                                                      baseY = 565,
                                                                                      maxDistance = 240,
                                                                                      minSpeed = 3000,
                                                                                      maxSpeed = 4000,
                                                                                      floatRange = 10,
                                                                                      verticalRange = 20
                                                                                    ) {
                                                                                      const targetElement = document.querySelector(selector);
                                                                                      if (!targetElement) {
                                                                                        console.error('未找到目标元素:', selector);
                                                                                        return;
                                                                                      }
                                                                                    \s
                                                                                      const startX = baseX + Math.floor(Math.random() * (floatRange * 2 + 1)) - floatRange;
                                                                                      const startY = baseY + Math.floor(Math.random() * (floatRange * 2 + 1)) - floatRange;
                                                                                    \s
                                                                                      const moveDistance = Math.floor(Math.random() * (maxDistance + 1));
                                                                                    \s
                                                                                      const speed = minSpeed + Math.random() * (maxSpeed - minSpeed);
                                                                                    \s
                                                                                      const duration = (moveDistance / speed) * 1000;
                                                                                    \s
                                                                                      const pathPoints = [];
                                                                                      const steps = 30; // 移动步数
                                                                                      let currentX = startX;
                                                                                      let currentY = startY;
                                                                                    \s
                                                                                      for (let i = 0; i <= steps; i++) {
                                                                                        const progress = i / steps;
                                                                                        const targetX = startX + moveDistance * progress;
                                                                                      \s
                                                                                        const verticalOffset = Math.floor(Math.random() * (verticalRange * 2 + 1)) - verticalRange;
                                                                                        currentY = startY + verticalOffset;
                                                                                      \s
                                                                                        const easeProgress = progress < 0.5
                                                                                          ? 2 * progress * progress
                                                                                          : -1 + (4 - 2 * progress) * progress;
                                                                                        currentX = startX + moveDistance * easeProgress;
                                                                                      \s
                                                                                        pathPoints.push({ x: currentX, y: currentY });
                                                                                      }
                                                                                    \s
                                                                                    \s
                                                                                      function createMouseEvent(type, x, y) {
                                                                                        return new MouseEvent(type, {
                                                                                          bubbles: true,
                                                                                          cancelable: true,
                                                                                          view: window,
                                                                                          clientX: x,
                                                                                          clientY: y,
                                                                                          button: 0,
                                                                                          buttons: 1,
                                                                                          relatedTarget: null
                                                                                        });
                                                                                      }
                                                                                    \s
                                                                                      return new Promise((resolve) => {
                                                                                        const mouseDownEvent = createMouseEvent('mousedown', startX, startY);
                                                                                        targetElement.dispatchEvent(mouseDownEvent);
                                                                                      \s
                                                                                        let stepIndex = 0;
                                                                                      \s
                                                                                        function moveToNextPoint() {
                                                                                          console.log('执行移动')
                                                                                          if (stepIndex >= pathPoints.length) {
                                                                                            const mouseUpEvent = createMouseEvent('mouseup', currentX, currentY);
                                                                                            targetElement.dispatchEvent(mouseUpEvent);
                                                                                            resolve();
                                                                                            return;
                                                                                          }
                                                                                        \s
                                                                                          const point = pathPoints[stepIndex];
                                                                                          const mouseMoveEvent = createMouseEvent('mousemove', point.x, point.y);
                                                                                          targetElement.dispatchEvent(mouseMoveEvent);
                                                                                        \s
                                                                                        \s
                                                                                          stepIndex++;
                                                                                        \s
                                                                                          const nextPoint = pathPoints[stepIndex];
                                                                                          const timeToNextPoint = nextPoint
                                                                                            ? (Math.sqrt(Math.pow(nextPoint.x - point.x, 2) + Math.pow(nextPoint.y - point.y, 2)) / speed) * 1000
                                                                                            : 0;
                                                                                        \s
                                                                                          setTimeout(moveToNextPoint, Math.max(5, timeToNextPoint));
                                                                                        }
                                                                                      \s
                                                                                        setTimeout(moveToNextPoint, 200);
                                                                                      });
                                                                                    }
                                                                                    \s
                                                                                    simulateSlideVerification();
                                        """);
                                Thread.sleep(1500);
                            } while (driver.getPageSource().contains("百度安全验证"));
                            System.out.println("自动处理完成");
                        }
                        break;
                    } catch (JavascriptException e) {
                        System.out.println("JS错误,0.1s后重试...");
                        Thread.sleep(100);
                    }
                }
                html = driver.getPageSource();
            } else {
                html = res.getData().toString();
            }
            Document doc = Jsoup.parse(html);
            Element likeForums = doc.selectFirst("#forum_group_wrap");
            String nickname = username.text();
            final StringBuilder forumResult = new StringBuilder(nickname + ":");
            if (likeForums == null) {
                String usernameStr = username.attr("title");
                if (!usernameStr.isEmpty() && usernameStr.length() > 0) {
                    usernameStr = URLEncoder.encode(usernameStr, StandardCharsets.UTF_8.toString());
                    Response res1 = api.GET("/home/get/panel?un=" + usernameStr);
                    ObjectMapper mapper = new ObjectMapper();
                    LinkedHashMap<String, Object> result = null;
                    try {
                        result = mapper.readValue(res1.getData().toString(), new TypeReference<LinkedHashMap<String, Object>>() {
                        });
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                    
                    Map<String, Object> grades = null;
                    try {
                        Map<String, Object> data = (LinkedHashMap<String, Object>) result.get("data");
                        Map<String, Object> honor = (LinkedHashMap<String, Object>) data.get("honor");
                        grades = (LinkedHashMap<String, Object>) honor.get("grade");
                    } catch (NullPointerException e) {
                        try {
                            driver.get(api.serverUrl + "/home/get/panel?un=" + usernameStr);
                            html = driver.getPageSource();
                            doc = Jsoup.parse(html);
                            Element pre = doc.selectFirst("pre");
                            result = mapper.readValue(pre.text(), new TypeReference<LinkedHashMap<String, Object>>() {
                            });
                            Map<String, Object> data = (LinkedHashMap<String, Object>) result.get("data");
                            Map<String, Object> honor = (LinkedHashMap<String, Object>) data.get("honor");
                            grades = (LinkedHashMap<String, Object>) honor.get("grade");
                            if (grades == null) {
                                this.bars.put("无效数据", this.bars.get("无效数据") == null ? 1 : this.bars.get("无效数据") + 1);
                                System.out.print("昵称:" + nickname + " 活跃查询失败--无数据\n");
                                continue;
                            }
                        } catch (Exception ex) {
                            this.bars.put("无效数据", this.bars.get("无效数据") == null ? 1 : this.bars.get("无效数据") + 1);
                            System.out.print("昵称:" + nickname + " 活跃查询失败--空指针\n");
                            continue;
                        }
                    } catch (ClassCastException e) {
                        try {
                            driver.get(api.serverUrl + "/home/get/panel?un=" + usernameStr);
                            html = driver.getPageSource();
                            doc = Jsoup.parse(html);
                            Element pre = doc.selectFirst("pre");
                            result = mapper.readValue(pre.text(), new TypeReference<LinkedHashMap<String, Object>>() {
                            });
                            Map<String, Object> data = (LinkedHashMap<String, Object>) result.get("data");
                            Map<String, Object> honor = (LinkedHashMap<String, Object>) data.get("honor");
                            grades = (LinkedHashMap<String, Object>) honor.get("grade");
                            if (grades == null) {
                                this.bars.put("无效数据", this.bars.get("无效数据") == null ? 1 : this.bars.get("无效数据") + 1);
                                System.out.print("昵称:" + nickname + " 活跃查询失败--无数据\n");
                                continue;
                            }
                        } catch (Exception ex) {
                            this.bars.put("无效数据", this.bars.get("无效数据") == null ? 1 : this.bars.get("无效数据") + 1);
                            System.out.print("昵称:" + nickname + " 活跃查询失败--类型转化\n");
                            continue;
                        }
                    }
                    
                    
                    if (grades == null) {
                        this.bars.put("无效数据", this.bars.get("无效数据") == null ? 1 : this.bars.get("无效数据") + 1);
                        System.out.print("昵称:" + nickname + " 活跃查询失败--空值\n");
                        continue;
                    }
                    AtomicReference<Boolean> isQualification = new AtomicReference<>(false);
                    grades.forEach((k, v) -> {
                        List<String> forumList = (List<String>) ((LinkedHashMap<String, Object>) (v)).get("forum_list");
                        for (String bar : forumList) {
                            if (bar.equals(forumName)) {
                                isQualification.set(true);
                                System.out.println("该用户是偏爱该吧用户");
                                break;
                            }
                        }
                    });
                    grades.forEach((k, v) -> {
                        List<String> forumList = (List<String>) ((LinkedHashMap<String, Object>) (v)).get("forum_list");
                        for (String forum : forumList) {
                            forumResult.append(" " + forum);
                            this.bars.put(forum, this.bars.get(forum) == null ? 1 : this.bars.get(forum) + 1);
                            if (isQualification != null && isQualification.get()) {
                                this.barsForQualified.put(forum, this.barsForQualified.get(forum) == null ? 1 : this.barsForQualified.get(forum) + 1);
                            }
                        }
                    });
                    System.out.println(forumResult.toString());
                    this.bars.put("有效数据", this.bars.get("有效数据") == null ? 1 : this.bars.get("有效数据") + 1);
                    System.out.print("昵称:" + nickname + " 活跃查询成功\n");
                    continue;
                }
                this.bars.put("无效数据", this.bars.get("无效数据") == null ? 1 : this.bars.get("无效数据") + 1);
                System.out.print("昵称:" + nickname + " 关注查询失败\n");
                continue;
            }
            Elements bars = likeForums.select(".u-f-item.unsign");
            AtomicReference<Boolean> isQualification = new AtomicReference<>(false);
            for (Element bar : bars) {
                String barName = bar.selectFirst("span").text();
                if (barName.equals(forumName)) {
                    isQualification.set(true);
                    System.out.println("该用户是偏爱该吧用户");
                    break;
                }
            }
            for (Element bar : bars) {
                String barName = bar.selectFirst("span").text();
                forumResult.append(" " + barName);
                this.bars.put(barName, this.bars.get(barName) == null ? 1 : this.bars.get(barName) + 1);
                if (isQualification != null && isQualification.get()) {
                    this.barsForQualified.put(barName, this.barsForQualified.get(barName) == null ? 1 : this.barsForQualified.get(barName) + 1);
                }
            }
            System.out.println(forumResult.toString());
            System.out.print("昵称:" + nickname + " 关注查询成功\n");
            this.bars.put("有效数据", this.bars.get("有效数据") == null ? 1 : this.bars.get("有效数据") + 1);
        }
    }
    
    
    public Map<String, Element> getPages(int page, String forumName1) {
        System.out.println("正在获取第" + 1 + "页HTML...");
        //获取HTML
        API api = new API();
        String forumName = null;
        try {
            System.out.println("将吧名" + forumName1 + "按GBK编码为:" + URLEncoder.encode(forumName1, "GBK"));
            forumName = URLEncoder.encode(forumName1, "GBK");
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
            if (page > 500) {
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
            Response response = api.GET("/bawu2/platform/listMemberInfo?word=" + forumName + "&pn=" + nowPage);
            doc = Jsoup.parse(response.getData().toString());
            
            Element newContainer = doc.selectFirst(".forum_info_section.member_wrap.clearfix.bawu-info");
            
            containers.put(nowPage + "", newContainer);
            nowPage++;
        }
        
        return containers;
    }
}