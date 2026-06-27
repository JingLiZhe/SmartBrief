package com.example.news.config;

import com.example.news.entity.Comment;
import com.example.news.mapper.DailyBriefMapper;
import com.example.news.mapper.HomeNewsMapper;
import com.example.news.mapper.SearchHistoryMapper;
import com.example.news.mapper.SummaryMapper;
import com.example.news.mapper.UserTextMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduledTasks {

    private final HomeNewsMapper homeNewsMapper;
    private final DailyBriefMapper dailyBriefMapper;
    private final SearchHistoryMapper searchHistoryMapper;
    private final SummaryMapper summaryMapper;
    private final UserTextMapper userTextMapper;
    private final com.example.news.mapper.NotificationMapper notificationMapper;
    private final com.example.news.mapper.CommentMapper commentMapper;
    private final com.example.news.mapper.CommentFileMapper commentFileMapper;
    private final com.example.news.mapper.CommentLikeMapper commentLikeMapper;
    private final com.example.news.mapper.UserMapper userMapper;

    // 可选：内存标记，避免一天内重复执行（例如手动调用）
    private volatile boolean dailyCleanDone = false;
    private volatile boolean weeklyCleanDone = false;

    /**
     * 每天 8:00 清空新闻数据
     */
    @Scheduled(cron = "0 0 8 * * ?")
    public void clearNewsData() {
        if (dailyCleanDone) {
            log.info("今日每日清理已执行过，跳过");
            return;
        }
        performDailyClean();
        dailyCleanDone = true;
    }

    /**
     * 每周一 8:00 清理历史数据
     */
    @Scheduled(cron = "0 0 8 * * MON")
    public void weeklyCleanup() {
        if (weeklyCleanDone) {
            log.info("本周周清理已执行过，跳过");
            return;
        }
        performWeeklyClean();
        weeklyCleanDone = true;
    }

    private void performDailyClean() {
        log.info("========== 开始执行每日清理（清空新闻数据） ==========");
        int homeNewsCount = homeNewsMapper.count();
        homeNewsMapper.clearAll();
        log.info("已清空 home_news 表，共 {} 条数据", homeNewsCount);
        dailyBriefMapper.clearAll();
        log.info("已清空 daily_brief 表");
        // 轮播图数据不清空，由定时任务单独刷新
        log.info("========== 每日清理完成 ==========");
    }

    private void performWeeklyClean() {
        log.info("========== 开始执行每周清理（清空历史数据） ==========");
        searchHistoryMapper.clearAll();
        log.info("已清空 search_history 表");
        summaryMapper.clearAll();
        log.info("已清空 summary 表");
        userTextMapper.clearAll();
        log.info("已清空 user_text 表");
        log.info("========== 每周清理完成 ==========");
    }

    /**
     * 每月1号 8:00 清理通知、评论、评论文件数据
     */
    @Scheduled(cron = "0 0 8 1 * ?")
    public void monthlyCleanup() {
        log.info("========== 开始执行月度清理（清空通知、评论、评论文件） ==========");
        performMonthlyClean();
        log.info("========== 月度清理完成 ==========");
    }

    private void performMonthlyClean() {
        log.info("========== 开始执行月度清理（清空通知、评论、评论文件） ==========");
        int notificationCount = notificationMapper.countAll();
        notificationMapper.clearAll();
        log.info("已清空 notification 表，共 {} 条数据", notificationCount);

        int commentFileCount = commentFileMapper.countAll();
        commentFileMapper.clearAll();
        log.info("已清空 comment_file 表，共 {} 条数据", commentFileCount);

        int commentCount = commentMapper.countAll();
        commentMapper.clearAll();
        log.info("已清空 comment 表，共 {} 条数据", commentCount);

        log.info("========== 月度清理完成 ==========");
    }

    /**
     * 每周一 8:00 统计每个用户收到的点赞数并更新 user.like_count，然后清理 comment_like 表
     */
    @Scheduled(cron = "0 0 8 * * MON")
    public void weeklyLikeCountCleanup() {
        log.info("========== 开始执行每周点赞统计与清理 ==========");
        performWeeklyLikeCountCleanup();
        log.info("========== 每周点赞统计与清理完成 ==========");
    }

    private void performWeeklyLikeCountCleanup() {
        // 1. 获取所有评论及其所属用户
        List<Comment> allComments = commentMapper.findAll();
        // 按 userId 分组统计点赞数
        java.util.Map<Long, Integer> likeCountMap = new java.util.HashMap<>();
        for (com.example.news.entity.Comment comment : allComments) {
            Long userId = comment.getUserId();
            if (userId == null) continue;
            // 统计该评论收到的点赞数
            int count = commentLikeMapper.countByCommentId(comment.getId());
            likeCountMap.merge(userId, count, Integer::sum);
        }

        // 2. 更新 user 表的 like_count
        for (java.util.Map.Entry<Long, Integer> entry : likeCountMap.entrySet()) {
            userMapper.updateLikeCount(entry.getKey(), entry.getValue());
            log.info("用户 {} 点赞数更新为 {}", entry.getKey(), entry.getValue());
        }

        // 3. 清理 comment_like 表中创建时间早于今天的数据
        java.time.LocalDateTime todayStart = java.time.LocalDate.now().atStartOfDay();
        int deleted = commentLikeMapper.deleteBeforeDate(todayStart);
        log.info("已清理 comment_like 表 {} 条数据", deleted);
    }
}
