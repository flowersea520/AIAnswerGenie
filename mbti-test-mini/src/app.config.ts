export default defineAppConfig({
  pages: [
    // 在小程序中，路由的优先级越高，则优先展示哪个路由的内容
    "pages/index/index", "pages/result/index", "pages/doQuestion/index"
  ],
  window: {
    backgroundTextStyle: 'light',
    navigationBarBackgroundColor: '#fff',
    navigationBarTitleText: 'WeChat',
    navigationBarTextStyle: 'black'
  }
})
