import { View } from "@tarojs/components";
import { AtButton, AtRadio } from "taro-ui";
import Taro from "@tarojs/taro";
import "./index.scss";
import questions from "../../data/questions.json";
import GlobalFooter from "../../components/GlobalFooter";
// eslint-disable-next-line import/first
import { useEffect, useState } from "react";

/**
 *  做题页面
 */
export default () => {
  // 定义问题的题号对象，（动态的，一改变页面就会重新渲染）
  const [currentQuestionNum, setCurrentQuestionNum] = useState<number>(1);
  // 当前题目
  const [currentQuestion, setCurrentQuestion] = useState<Question>(questions[0]);
  // 当前答案
  const [currentAnswer, setCurrentAnswer] = useState<string>("");
  // 答案列表
  const [answerList] = useState<string[]>([]);
  // 每个问题对应的选项对象
  const questionOptions: any = currentQuestion.options.map((option: any) => {
    return {
      label: `${option.key} . ${option.value} `,
      // 这个value就是发送给后端的值
      value: option.key,
    };
  });
  //始终改变题号，然后在改变题目
  // 题目序号变换时：注意切换到对应的序号题目 和 当前回答
  useEffect(() => {
    // 注意，这里取元素的时候，记得一定要减 1，因为索引从0开始, 这里题号从 1开始
    setCurrentQuestion(questions[currentQuestionNum - 1]);
    // 题号也 决定着答案里列表，第一个题目题号1对应着答案列表 的元素0
    setCurrentAnswer(answerList[currentQuestionNum - 1]);
    // 深度监听 题号的变化
  }, [currentQuestionNum]);
  return (
    <View className="doQuestionPage">
      {JSON.stringify(answerList)}
      <View className="at-article__h2 title">
        {currentQuestionNum}、{currentQuestion.title}
      </View>
      <View className="options-wrapper">
        {/*答案的单选框，A，B两个选项选一个*/}
        <AtRadio
          options={questionOptions}
          value={currentAnswer}
          onClick={(value) => {
            setCurrentAnswer(value);
            // 记录答案
            answerList[currentQuestionNum - 1] = value;
          }}
        />
      </View>
      {currentQuestionNum < questions.length && (
        // 如果 用户不选，我们将这个按钮禁用
        <AtButton
          type="primary"
          circle
          className="controlBtn"
          disabled={!currentAnswer}
          onClick={() => {
            setCurrentQuestionNum(currentQuestionNum + 1);
          }}
        >
          下一题
        </AtButton>
      )}
      {
        // 查看结果，肯定是回答到了最后一题
        currentQuestionNum === questions.length && (
          <AtButton
            type="primary"
            circle
            className="controlBtn"
            onClick={() => {
              // 保存答案列表
              Taro.setStorageSync("answerList", answerList);
              // todo 跳转到结果页
              Taro.navigateTo({
                url: "/pages/result/index",
              });
            }}
          >
            查看结果
          </AtButton>
        )
      }
      {currentQuestionNum > 1 && (
        <AtButton
          circle
          className="controlBtn"
          disabled={!currentAnswer}
          onClick={() => {
            setCurrentQuestionNum(currentQuestionNum - 1);
          }}
        >
          上一题
        </AtButton>
      )}

      <GlobalFooter></GlobalFooter>
    </View>
  );
};
