package com.example.demo.task;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo.common.Result;
import com.example.demo.entity.Document;
import com.example.demo.mapper.CronMapper;
import com.example.demo.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

/**
 * @Description
 * @ClassName MyTask
 * @Author User
 * @date 2020.06.07 15:23
 */
@Component
@EnableScheduling
public class MyTask implements SchedulingConfigurer {

    @Autowired
    protected CronMapper cronMapper;

    @Autowired
    DocumentService documentService;

    @Override
    public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {
        scheduledTaskRegistrar.addTriggerTask(() -> process(),
                triggerContext -> {
                    String cron = cronMapper.getCron(2);
                    if (cron.isEmpty()) {
                        System.out.println("cron is null");
                    }
                    return new CronTrigger(cron).nextExecutionTime(triggerContext);
                });
        scheduledTaskRegistrar.addTriggerTask(() -> process(),
                triggerContext -> {
                    String cron = cronMapper.getCron(3);
                    if (cron.isEmpty()) {
                        System.out.println("cron is null");
                    }
                    return new CronTrigger(cron).nextExecutionTime(triggerContext);
                });
        scheduledTaskRegistrar.addTriggerTask(() -> process(),
                triggerContext -> {
                    String cron = cronMapper.getCron(4);
                    if (cron.isEmpty()) {
                        System.out.println("cron is null");
                    }
                    return new CronTrigger(cron).nextExecutionTime(triggerContext);
                });
    }

    private void process() {

        System.out.println("基于接口定时任务 ");
        // 获取当前时间
        Date date = new Date();

        // 使用指定的日期格式化类格式化时间
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = dateFormat.format(date);

        // 打印当前时间
        System.out.println("当前时间: " + formattedDate);


        // 将字符串转换为 LocalDateTime 对象

        // 获取当前时间
        LocalDateTime currentTime = LocalDateTime.now();
        //对未开放的文件记录进行扫描
        List<Document> documentToPublish = documentService.findDocumentToPublish();

        if (!documentToPublish.isEmpty()) {
            // 进行进一步的验证
            for (Document document : documentToPublish) {
                if (document.getApprovalStatus() == 1 && document.getPublishStatus() == 0 && document.getApprovalTime() != null) {
                    //此处处理的当前文件的上一个版本，而这个文件一定是开放了的 但是这个文件的作废状态也一定是未作废
                    if (documentService.selectOneLast(document) != null) {
                        //
                        Result<?> documentById1 = documentService.getDocumentById(documentService.selectOneLast(document).getId());
                        Object data = documentById1.getData();
                        if (data instanceof Document) {
                            Document document1 = (Document) data;
                            ////这里需要对时间进行处理 对被更新的记录需要把新版本文件的创建时间设置为它的更新时间

                            Date newDate = new Date();
                            //被更新文件需要更新它的更新时间
                            document1.setUpdateTime(newDate);
                            //对于被更新的文件需要设置它的启用状态为作废
                            document1.setDeleted(1);
                            Boolean aBoolean = documentService.modifyDocument(document1);
                            System.out.println("更新日期成功：" + aBoolean);
                        }

                    }
                    //如果文件通过了开放 则更新该条记录的文件开放时间 为当前时间

                    Date newDate = new Date();
                    document.setPublishTime(newDate);
                    // 验证通过的处理逻辑 把这个文件的发布状态改为发布
                    document.setPublishStatus(1);
                    documentService.publishUpdate(document);
                } else {
                    // 验证不通过的处理逻辑
                    System.out.println("扫描发现错误");
                }
            }
        } else {
            // 列表为空的处理逻辑
            System.out.println("本次扫描未发现新的该发布的文件");
        }

        //
        //// 计算时间差
        //long daysBetween = ChronoUnit.DAYS.between(targetTime, currentTime);
        //
        //// 判断时间差是否大于一天
        //if (daysBetween > 1) {
        //    return Result.error("201", "创建时间超过一天,禁止删除");
        //}else{}
    }
}
