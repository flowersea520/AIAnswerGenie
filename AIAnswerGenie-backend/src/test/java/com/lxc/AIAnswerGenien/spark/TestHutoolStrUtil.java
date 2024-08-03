package com.lxc.AIAnswerGenien.spark;

import cn.hutool.core.util.StrUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author mortal
 * @date 2024/7/24 8:05
 */
@SpringBootTest
public class TestHutoolStrUtil {
	private String result = "aaabbb";

	@Test
	void test() {

		int start = StrUtil.indexOf(result, 'a');
		// 注意：字符串中的lastIndexOf方法是从后往前搜索字符串最后出现的位置的（还是正常肉眼看到的最后出现的，而不是从后往前最后出现的）
		int end = StrUtil.lastIndexOf(result, "b", result.length() - 1, true);
		System.out.println(end); // 5
		String json = result.substring(start, end + 1); // 包左不包右

		System.out.println(json);
	}

}
