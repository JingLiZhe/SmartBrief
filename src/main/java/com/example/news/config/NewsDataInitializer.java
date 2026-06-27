package com.example.news.config;

import com.example.news.service.HomeNewsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NewsDataInitializer implements CommandLineRunner {

    private final HomeNewsService homeNewsService;

    /**
     * 应用启动时执行：如果新闻数据为空，则从API拉取
     */
    @Override
    public void run(String... args) throws Exception {
        log.info("【启动检测】检查新闻数据...");
        checkAndRefreshNews();
        // 启动时也初始化轮播图
        refreshCarousel();
    }

    /**
     * 每天 8:01 执行：清理任务（8:00）之后，检查数据是否为空，若为空则补充
     */
    @Scheduled(cron = "0 1 8 * * ?")
    public void scheduledRefresh() {
        log.info("【定时任务】8:01 检查新闻数据...");
        checkAndRefreshNews();
        // 轮播图每天也更新一次（可根据需要调整）
        refreshCarousel();
    }

    /**
     * 公共方法：检查新闻数量，若为0则刷新全部新闻
     */
    private void checkAndRefreshNews() {
        try {
            int count = homeNewsService.getNewsCount();
            if (count == 0) {
                log.info("新闻数据为空，开始从API刷新...");
                homeNewsService.refreshAllNews();
                log.info("新闻数据刷新完成");
            } else {
                log.info("新闻数据存在 {} 条，无需刷新", count);
            }
        } catch (Exception e) {
            log.error("刷新新闻数据失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 公共方法：刷新轮播图数据（可独立控制）
     */
    private void refreshCarousel() {
        try {
            homeNewsService.refreshCarouselNews();
            log.info("轮播图数据更新完成");
        } catch (Exception e) {
            log.error("更新轮播图数据失败: {}", e.getMessage(), e);
        }
    }
}