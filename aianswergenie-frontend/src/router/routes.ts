import { RouteRecordRaw } from "vue-router";
import HomePage from "@/views/HomePage.vue";
import AboutView from "@/views/AboutView.vue";
import UserLayout from "@/layouts/UserLayout.vue";
import NoAuthPage from "@/views/NoAuthPage.vue";
import ACCESS_ENUM from "@/access/accessEnum";
import UserLoginPage from "@/views/user/UserLoginPage.vue";
import UserRegisterPage from "@/views/user/UserRegisterPage.vue";
import AdminUserPage from "@/views/admin/AdminUserPage.vue";
import AdminAppPage from "@/views/admin/AdminAppPage.vue";
import AdminQuestionPage from "@/views/admin/AdminQuestionPage.vue";
import AdminScoringResultPage from "@/views/admin/AdminScoringResultPage.vue";
import AdminUserAnswerPage from "@/views/admin/AdminUserAnswerPage.vue";
import AppDetailPage from "@/views/app/appDetailPage.vue";
import AddAppPage from "@/views/add/AddAppPage.vue";
import AddQuestionPage from "@/views/add/AddQuestionPage.vue";
import AddScoringResultPage from "@/views/add/AddScoringResultPage.vue";
import DoAnswerPage from "@/views/answer/DoAnswerPage.vue";
import MyAnswerPage from "@/views/answer/MyAnswerPage.vue";
import AnswerResultPage from "@/views/answer/AnswerResultPage.vue";
import AppStatisticPage from "@/views/admin/AppStatisticPage.vue";

export const routes: Array<RouteRecordRaw> = [
  {
    path: "/",
    name: "主页",
    component: HomePage,
  },
  /**
   * 应用模块
   */
  {
    path: "/add/app",
    name: "创建应用",
    component: AddAppPage,
  },
  {
    path: "/add/app/:id",
    name: "修改应用",
    props: true,
    component: AddAppPage,
    meta: {
      hideInMenu: true,
    },
  },
  {
    // 这里用的动态路由，因为我们点击app的详情页的时候肯定要携带对应app的id
    path: "/app/detail/:id",
    name: "应用详情页",
    // 路由组件配置props: true时，
    // 意味着您想要启用路由参数的模式传递，该模式会允许路由参数通过props传递给子组件。
    props: true,
    component: AppDetailPage,
    // 不用再菜单栏显示详情页
    meta: {
      hideInMenu: true,
    },
  },
  /**
   *  题目模块
   */
  {
    path: "/add/question/:appId",
    name: "创建题目",
    component: AddQuestionPage,
    props: true,
    meta: {
      hideInMenu: true,
    },
  },
  /**
   *  评分结果模块
   */
  {
    path: "/add/scoring_result/:appId",
    name: "创建评分",
    component: AddScoringResultPage,
    props: true,
    meta: {
      hideInMenu: true,
    },
  },

  /**
   * 答题模块
   */
  // 路由，注意权限控制，仅登录用户才可以答题：
  {
    path: "/answer/do/:appId",
    name: "答题",
    component: DoAnswerPage,
    props: true,
    meta: {
      hideInMenu: true,
      access: ACCESS_ENUM.USER,
    },
  },
  {
    path: "/answer/result/:id",
    name: "答题结果",
    component: AnswerResultPage,
    props: true,
    meta: {
      hideInMenu: true,
      access: ACCESS_ENUM.USER,
    },
  },
  {
    path: "/answer/my",
    name: "我的答题",
    component: MyAnswerPage,
    meta: {
      access: ACCESS_ENUM.USER,
    },
  },
  /**
   * 管理模块（这些页面只能给管理员看的）
   */
  {
    path: "/admin/app",
    name: "应用管理",
    component: AdminAppPage,
    meta: {
      access: ACCESS_ENUM.ADMIN,
    },
  },
  {
    path: "/admin/user",
    name: "用户管理页面",
    component: AdminUserPage,
    //  meta 属性用来定义路由的一些自定义属性。在这个例子中，meta 属性里的 access 表示这个路由页面的访问权限
    meta: {
      access: ACCESS_ENUM.ADMIN,
    },
  },
  {
    path: "/admin/question",
    name: "题目管理",
    component: AdminQuestionPage,
    meta: {
      access: ACCESS_ENUM.ADMIN,
    },
  },
  {
    path: "/admin/scoring_result",
    name: "评分管理",
    component: AdminScoringResultPage,
    meta: {
      access: ACCESS_ENUM.ADMIN,
    },
  },
  {
    path: "/admin/user_answer",
    name: "回答管理",
    component: AdminUserAnswerPage,
    meta: {
      access: ACCESS_ENUM.ADMIN,
    },
  },
  /**
   *  应用统计分析页面
   */
  {
    path: "/app_statistic",
    name: "应用统计",
    component: AppStatisticPage,
    meta: {
      access: ACCESS_ENUM.ADMIN,
    },
  },

  {
    path: "/noAuth",
    name: "无权限",
    component: NoAuthPage,
    meta: {
      // 当此 路由生成菜单项的时候，将其隐藏（页面文件在js中记得配合filter方法将其过滤出去）
      hideInMenu: true,
    },
  },
  // 这种结构在前端路由中被称为嵌套路由或子路由。您配置了一个主路由路径为 /user，并为其指定了一个布局组件 UserLayout。
  // 然后，在这个主路由下，您定义了两个子路由，分别是 /user/login 和 /user/register。
  // 这样的设计允许您在同一个布局下展示不同的子页面，从而实现页面的结构化组织。
  {
    path: "/user",
    name: "用户",
    component: UserLayout,
    children: [
      // 当访问path: "/user/login",路由的时候，会将组件UserLoginView,嵌套在父级的UserLayout中
      {
        path: "/user/login",
        name: "用户登录",
        component: UserLoginPage,
      },
      {
        path: "/user/register",
        name: "用户注册",
        component: UserRegisterPage,
      },
    ],
    meta: {
      // 当此 路由生成菜单项的时候，将其隐藏（页面文件在js中记得配合filter方法将其过滤出去）
      hideInMenu: true,
    },
  },

  {
    path: "/hide",
    name: "hide",
    component: HomePage,
    meta: {
      // 当此 路由生成菜单项的时候，将其隐藏（页面文件在js中记得配合filter方法将其过滤出去）
      hideInMenu: true,
    },
  },
];
