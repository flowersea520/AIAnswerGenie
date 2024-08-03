package com.lxc.AIAnswerGenien;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.concurrent.TimeUnit;


/**
 * @author mortal
 * @date 2024/7/27 12:43
 */
@SpringBootTest
public class RxJavaTest {
	@Test
	void testRxJavaDemo() throws Exception{
//		.subscribeOn(Schedulers.io()) 用于指定 Flowable 在哪个线程上进行订阅（即在哪个线程上开始执行）。
		Flowable<Long> flowable = Flowable.interval(1, TimeUnit.SECONDS)
//				map数据处理（映射）：对数据进行某些处理，
				.map(i -> i + 1).subscribeOn(Schedulers.io());// 指定创建流的线程池
// 订阅 Flowable 流，并打印每个接受到的数字
		flowable.observeOn(Schedulers.io()).doOnNext(item -> System.out.println(item.toString())).subscribe();
		// 让主线程睡眠，以便观察输出
		Thread.sleep(10000L);
	}
}
