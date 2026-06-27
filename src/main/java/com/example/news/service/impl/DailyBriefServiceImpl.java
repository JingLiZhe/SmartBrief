package com.example.news.service.impl;

import com.example.news.entity.DailyBrief;
import com.example.news.mapper.DailyBriefMapper;
import com.example.news.service.DailyBriefService;
import com.example.news.service.DeepSeekService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;

@Service
public class DailyBriefServiceImpl implements DailyBriefService {

    private final DailyBriefMapper dailyBriefMapper;
    private final DeepSeekService deepSeekService;
    private final RestTemplate restTemplate;

    @Value("${newsapi.key}")
    private String newsApiKey;
    
    @Value("${newsapi.url.domestic}")
    private String domesticNewsUrl;
    
    @Value("${newsapi.url.international}")
    private String internationalNewsUrl;

    public DailyBriefServiceImpl(DailyBriefMapper dailyBriefMapper, DeepSeekService deepSeekService, RestTemplate restTemplate) {
        this.dailyBriefMapper = dailyBriefMapper;
        this.deepSeekService = deepSeekService;
        this.restTemplate = restTemplate;
    }

    @Override
    public DailyBrief generateBrief(LocalDate date, String lang) {
        return generateBrief(date, lang, "domestic");
    }
    
    @Override
    public DailyBrief generateBrief(LocalDate date, String lang, String category) {
        String headlines = fetchHeadlines(category);
        String categoryName = getCategoryName(category);
        String dateStr = date.toString();
        String systemPrompt = "请根据以下新闻头条（日期：" + dateStr + "），筛选出属于" + categoryName + "的新闻，并生成一份"
                + (lang.equals("zh-CN") ? "中文" : "英文") + "每日简报，格式清晰，内容简洁。"
                + "如果某条新闻不属于" + categoryName + "，请忽略它。所有新闻均为" + dateStr + "当天的新闻。";
        String content = deepSeekService.chat(systemPrompt, headlines);

        DailyBrief existing = dailyBriefMapper.findByDateLangAndCategory(date, lang, categoryName);
        if (existing != null) {
            existing.setContent(content);
            dailyBriefMapper.update(existing);
            return existing;
        }

        DailyBrief brief = new DailyBrief();
        brief.setBriefDate(date);
        brief.setLanguage(lang);
        brief.setContent(content);
        brief.setCategory(categoryName);
        dailyBriefMapper.insert(brief);
        return brief;
    }
    
    private String getCategoryName(String category) {
        switch (category.toLowerCase()) {
            case "domestic":
                return "国内";
            case "international":
                return "国际";
            default:
                return "国内";
        }
    }
    
    private String getNewsApiUrl(String category) {
        switch (category.toLowerCase()) {
            case "domestic":
                return domesticNewsUrl;
            case "international":
                return internationalNewsUrl;
            default:
                return domesticNewsUrl;
        }
    }

    private String fetchHeadlines(String category) {
        try {
            String url = getNewsApiUrl(category) + "?key=" + newsApiKey;
            String response = restTemplate.getForObject(url, String.class);
            System.out.println("天API响应(" + getCategoryName(category) + "): " + response);
            
            // 解析JSON响应，提取新闻标题和时间
            StringBuilder headlines = new StringBuilder();
            
            // 找到 newslist 数组
            int newslistStart = response.indexOf("\"newslist\":[");
            if (newslistStart > 0) {
                int newslistEnd = response.indexOf("]", newslistStart);
                if (newslistEnd > newslistStart) {
                    String newslistStr = response.substring(newslistStart + 11, newslistEnd);
                    // 分割每个新闻项
                    String[] newsItems = newslistStr.split("},\\{");
                    int count = Math.min(newsItems.length, 10);
                    for (int i = 0; i < count; i++) {
                        String item = newsItems[i];
                        // 提取标题
                        int titleStart = item.indexOf("\"title\":");
                        if (titleStart > 0) {
                            titleStart = item.indexOf("\"", titleStart + 8) + 1;
                            int titleEnd = item.indexOf("\"", titleStart);
                            if (titleEnd > titleStart) {
                                String title = item.substring(titleStart, titleEnd);
                                
                                // 提取发布时间
                                String pubDate = "";
                                int pubDateStart = item.indexOf("\"pubDate\":");
                                if (pubDateStart > 0) {
                                    pubDateStart = item.indexOf("\"", pubDateStart + 10) + 1;
                                    int pubDateEnd = item.indexOf("\"", pubDateStart);
                                    if (pubDateEnd > pubDateStart) {
                                        pubDate = item.substring(pubDateStart, pubDateEnd);
                                    }
                                }
                                
                                if (pubDate.length() > 0) {
                                    headlines.append(i + 1).append(". [").append(pubDate).append("] ").append(title).append("\n");
                                } else {
                                    headlines.append(i + 1).append(". ").append(title).append("\n");
                                }
                            }
                        }
                    }
                }
            }
            
            if (headlines.length() > 0) {
                return headlines.toString();
            }
        } catch (Exception e) {
            System.out.println("天API 调用失败(" + category + "): " + e.getMessage());
        }
        // 返回对应类型的模拟数据
        return getMockHeadlines(category);
    }
    
    private String getMockHeadlines(String category) {
        switch (category.toLowerCase()) {
            case "domestic":
                return "1. 我国成功发射新一代通信卫星\n" +
                       "2. 全国多地出台稳经济增长新政策\n" +
                       "3. 医保电子凭证实现全国覆盖\n" +
                       "4. 新能源汽车下乡活动启动\n" +
                       "5. 高校毕业生就业服务行动全面展开";
            case "international":
                return "1. 联合国气候变化大会达成共识\n" +
                       "2. 全球数字经济峰会在新加坡举行\n" +
                       "3. 多国签署自由贸易协定\n" +
                       "4. 国际空间站迎来新一批宇航员\n" +
                       "5. 全球粮食安全合作取得新进展";
            case "social":
                return "1. 社区志愿服务活动广泛开展\n" +
                       "2. 全民健身运动蓬勃发展\n" +
                       "3. 传统文化传承创新成果显著\n" +
                       "4. 乡村振兴战略深入实施\n" +
                       "5. 文明城市创建工作成效显著";
            case "tech":
            default:
                return "1. 科技巨头发布新一代人工智能芯片，性能大幅提升\n" +
                       "2. 5G商用进程加速推进\n" +
                       "3. 量子计算研究取得新突破\n" +
                       "4. 新能源技术创新成果丰硕\n" +
                       "5. 数字人民币应用场景持续扩展";
        }
    }

    @Override
    public DailyBrief getBriefByDateAndLang(LocalDate date, String lang) {
        return dailyBriefMapper.findByDateAndLang(date, lang);
    }

    @Override
    public List<DailyBrief> getBriefsByDate(LocalDate date) {
        return dailyBriefMapper.findByDate(date);
    }

    @Override
    public List<DailyBrief> getAllBriefs() {
        return dailyBriefMapper.findAll();
    }

    @Override
    public void deleteBrief(Long id) {
        dailyBriefMapper.deleteById(id);
    }
}
