import axios from "axios";
import message from "@arco-design/web-vue/es/message";

const myAxios = axios.create({
  baseURL: "http://localhost:8081",
  // 如果调用了ai，可以让超时时间长一些，因为ai要生成内容
  timeout: 600000,
  // `withCredentials` 表示跨域请求时是否需要使用凭证
  withCredentials: true,
});

// 添加请求拦截器
myAxios.interceptors.request.use(
  function (config) {
    // 在发送请求之前做些什么
    return config;
  },
  function (error) {
    // 对请求错误做些什么
    return Promise.reject(error);
  }
);

// 添加响应拦截器
myAxios.interceptors.response.use(
  function (response) {
    // 2xx 范围内的状态码都会触发该函数。
    // 对响应数据做点什么
    // 我们这里将相应的response抽出来data（这个data就是完完整整后端给的数据 -- 包括请求头信息）
    const { data } = response;
    // 如果后端的响应状态码是40100，那么就是未登录，所以我们将其跳转到登录页面
    if (data.code === 40100) {
      // 如果 当前页面既不是登录页面也不是请求登录相关资源的页面
      if (
        !response.request.responseURL.includes("/user/get/login") &&
        !window.location.pathname.includes("/user/login")
      ) {
        message.warning("请先登录");
        // 重定向到登录页
        //  ${window.location.href} 是用来获取 跳转到 登录页面 之前 用户所在的页面的完整 URL。
        // 这样做通常是为了在用户成功登录后能够知道 从哪里 重定向 回原来的页面。这就是所谓的“记住返回路径”的功能
        // 左边的window.location.href是重定向到新url【登录页】，
        // 右边这个window.location.href是用来获取 当前页面的url【为跳转前的url】
        window.location.href = `/user/login?redirect=${window.location.href}`;
      }
    }

    return response;
  },
  function (error) {
    // 超出 2xx 范围的状态码都会触发该函数。
    // 对响应错误做点什么
    return Promise.reject(error);
  }
);
// 如果模块使用 export default 导出一个值，那么导入时不需要使用 {}
export default myAxios;
