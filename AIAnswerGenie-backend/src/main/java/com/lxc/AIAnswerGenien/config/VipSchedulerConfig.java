package com.lxc.AIAnswerGenien.config;



import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import lombok.Data;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * VipSchedulerConfig 那这个类我可以理解为vip管理线程池对象的配置
 * 返回一个管理线程池的Scheduler，第三方bean
 * @author mortal
 * @date 2024/8/2 17:00
 */
@Configuration

public class VipSchedulerConfig {

	@Bean
	// Scheduler 对象：就是控制线程的对象
	public Scheduler vipScheduler() {
		ThreadFactory threadFactory = new ThreadFactory() {
			// 并发的原子属性，用来当计数器
			private final AtomicInteger threadNumber = new AtomicInteger(1);

			@Override
			public Thread newThread(Runnable r) {
//				getAndIncrement()：原子性地将当前值加一，但返回的是增加之前的值。
				// 相当于给其线程一个编号
				Thread thread = new Thread(r, "VIPThread-" + threadNumber.getAndIncrement());
				// 非守护性线程：是正常的用户线程，执行的是主任务或业务逻辑。（设置false）-- 非守护线程不会自动退
				// 守护性线程是执行辅助任务的（会自动退）
				thread.setDaemon(false);
				return thread;
			}
		};
		// 用一个线程池的工具类Executors，创建了操作线程池的对象ExecutorService
//		ThreadPoolExecutor: 是 ExecutorService 的一个实现
		ExecutorService executorService = Executors.newScheduledThreadPool(10, threadFactory);
//		将已有的 ExecutorService 转换为 Scheduler，从而在 RxJava 中使用它来管理线程。
		return Schedulers.from(executorService);
	}




















}
