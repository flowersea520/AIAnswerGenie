package com.lxc.AIAnswerGenien.scoring;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.lxc.AIAnswerGenien.model.dto.question.QuestionContentDTO;
import com.lxc.AIAnswerGenien.model.entity.App;
import com.lxc.AIAnswerGenien.model.entity.Question;
import com.lxc.AIAnswerGenien.model.entity.ScoringResult;
import com.lxc.AIAnswerGenien.model.entity.UserAnswer;
import com.lxc.AIAnswerGenien.model.vo.QuestionVO;
import com.lxc.AIAnswerGenien.service.QuestionService;
import com.lxc.AIAnswerGenien.service.ScoringResultService;

import javax.annotation.Resource;
import java.util.List;

/**
 *  自定义 打分类 应用评分策略
 * @author mortal
 * @date 2024/7/14 3:39
 */
@ScoringStrategyConfig(appType = 0, scoringStrategy = 0)
public class CustomScoreScoringStrategy implements ScoringStrategy {

	@Resource
	private QuestionService questionService;

	@Resource
	private ScoringResultService scoringResultService;

	@Override
	public UserAnswer doScore(List<String> choices, App app) throws Exception {
		Long appId = app.getId();
		// 1. 根据题目的id查询到题目和题目的 评分结果信息
		Question question = questionService.getOne(
				// Question::getAppId一个方法引用，
				// 注意：mybatisplus的方法应用就是定位字段名的（不是和集合一样取值）
				Wrappers.lambdaQuery(Question.class).eq(Question::getAppId, appId)
		);
		// 根据应用id，查评分结果表可以查到多条记录，这里用集合
		// 这里记得根据结果评分范围降序排（因为我们这个是 打分类）
		List<ScoringResult> scoringResultList = scoringResultService.list(
				Wrappers.lambdaQuery(ScoringResult.class)
						.eq(ScoringResult::getAppId, appId)
						.orderByDesc(ScoringResult::getResultScoreRange)
		);
		// 2. 统计用户的总得分
		// 初始化一个对象，用于存储每个选项的计数
		Integer totalScore = 0;
		QuestionVO questionVO = QuestionVO.objToVo(question);
		// 创建QuestionVO就是为了方便那 答案列表
		List<QuestionContentDTO> questionContentDTOList = questionVO.getQuestionContent();
		// 每个DTO中的题目对象（嵌套了一个Options数组）：    {
		//        "options": [
		//            {
//						  "score":10,   -- 打分类会有分数
//                		  "result": "I",
		//                "value": "独自工作",
		//                "key": "A"
		//            },
		//            {
//						  "score":0,
		//                "result": "E",
		//                "value": "与他人合作",
		//                "key": "B"
		//            }
		//        ],
		//        "title": "1. 你通常更喜欢"
		//    },
		// 遍历题目列表（增强for遍历）
		for (QuestionContentDTO questionContentDTO : questionContentDTOList) {
			// 遍历答案列表
			for (String answer : choices) {
				// 遍历题目中的选项（每个元素的类型是DTO中嵌套类的类型
				for (QuestionContentDTO.Option option : questionContentDTO.getOptions()) {
					// 如果用户的答案answer  和选项列表中元素的 选项key匹配
					if (option.getKey().equals(answer)) {
						// 如果选项option对象中没有分数， option.getScore() 返回的值为null，那么 defaultIfNull() 方法将返回你提供的默认值（这里是0）
						Integer score = ObjUtil.defaultIfNull(option.getScore(), 0);
						// 做好了其option.getScore()不为null，有值之后，我们直接 累加到积分器totalScore
						totalScore += score;

					}
				}
			}
		}
		// 3. 遍历得分结果，找到 第一个用户 分数大于得分范围的结果，作为最终得分结果
		// 先初始一个默认的得分结果
		ScoringResult maxScoringResult = scoringResultList.get(0);
		for (ScoringResult scoringResult : scoringResultList) {
			if (totalScore >= scoringResult.getResultScoreRange()) {
				maxScoringResult = scoringResult;
			}
			break;
		}
		// 4. 构造返回值，填充答案对象的属性
		UserAnswer userAnswer = new UserAnswer();
		userAnswer.setAppId(appId);
		userAnswer.setAppType(app.getAppType());
		userAnswer.setScoringStrategy(app.getScoringStrategy());
		userAnswer.setChoices(JSONUtil.toJsonStr(choices));
		userAnswer.setResultId(maxScoringResult.getId());
		userAnswer.setResultName(maxScoringResult.getResultName());
		userAnswer.setResultDesc(maxScoringResult.getResultDesc());
		userAnswer.setResultPicture(maxScoringResult.getResultPicture());
		userAnswer.setResultScore(totalScore);

		return userAnswer;
	}
}
