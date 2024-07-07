import { View, Image } from "@tarojs/components";
import { AtButton } from "taro-ui";
import Taro from "@tarojs/taro";
import headerBg from "../../assets/headerBg.jpg";
import "./index.scss";
import GlobalFooter from "../../components/GlobalFooter";


/**
 *  主页
 */
export default () => {
  return (
    <View className="indexPage">
      <View className="at-article__h1 title">“终于被理解的感觉真好。”</View>
      <View className="at-article__h2 subTitle">
        只需10分钟，就能“惊人般准确”地描述出你是谁，以及你为何以这样的方式行事。
      </View>

      {/*点击开始测试按钮，我们跳转到 答题页面*/}
      <AtButton
        type="primary"
        circle
        className="enterBtn"
        onClick={() => {
          Taro.navigateTo({
            url: "/pages/doQuestion/index",
          });
        }}
      >
        参加测试 -{">"}
      </AtButton>
      <Image className="headerBg" src={headerBg}></Image>
      <GlobalFooter></GlobalFooter>
    </View>
  );
};
