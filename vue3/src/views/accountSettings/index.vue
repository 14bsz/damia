<template>
  <!--个人信息-->
  <Header></Header>
  <div class="red-line"></div>
  <div class="section">
    <MenuSideBar class="sidebarMenu" activeIndex="2"></MenuSideBar>
    <div class="right-section">
      <div class="breadcrumb"><span>账号设置</span></div>
      <div class="right-tab">
        <ul class="title">
          <li class="left">账号设置</li>
        </ul>
        <div class="box">
          <div class="account-info" v-for="item in accountLists">
            <ul>
              <li :class="item.nameInfoStyle">{{ item.nameInfo }}</li>
              <li class="detail">{{ item.detailInfo }}</li>
              <li class="explain">
                <router-link v-if="experienceAccountFlag != 1" :to="item.path"
                             :class="(item.explainInfo =='立即认证'||item.explainInfo =='立即绑定')? 'pathBtn':'btnColor'">
                  {{ item.explainInfo }}
                </router-link>
                <div class="btnColor" v-if="experienceAccountFlag == 1">
                    体验不支持
                </div>  
              </li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  </div>
  <Footer class="foot"></Footer>
</template>

<script setup>
import MenuSideBar from '../../components/menuSidebar/index'
import Header from '../../components/header/index'
import Footer from '../../components/footer/index'
import useUserStore from "../../store/modules/user";
import {getName, getUserIdKey} from "../../utils/auth";
import {getPersonInfoId} from '@/api/personInfo'
import {ref, reactive} from 'vue'

//体验账号标识
const experienceAccountFlag = ref(import.meta.env.VITE_EXPERIENCE_ACCOUNT_FLAG);
let accountLists = ref([])
let telNum = ref('')

const accountList = reactive([
  {
    nameInfo: '登录密码',
    detailInfo: '建议使用字母、数字和符号组合，并定期修改，提高账号安全',
    explainInfo: '修改',
    path: './editPassword',
    nameInfoStyle: 'name-info-yes'
  },
  {
    nameInfo: '邮箱验证',
    detailInfo: '验证邮箱可帮助您快速找回密码，并可接收订单、演出通知、促销活动等提醒',
    explainInfo: '立即绑定',
    path: './email',
    nameInfoStyle: 'name-info-yes'
  },
  {
    nameInfo: '手机验证',
    detailInfo: '绑定手机号可用于登录与找回密码，并接收订单与安全通知',
    explainInfo: '更换',
    path: './mobile',
    nameInfoStyle: 'name-info-yes'
  },
  {
    nameInfo: '实名认证',
    detailInfo: '认证您的实名信息，提高安全等级',
    explainInfo: '立即认证',
    path: './authentication',
    nameInfoStyle: 'name-info-yes'

  }
])


getIsVaild()

//通过id获取是否进行验证，为验证的话控制图标，按钮的显示
function getIsVaild() {
  const id = getUserIdKey()
  getPersonInfoId({id: id}).then(response => {
    let {relAuthenticationStatus, emailStatus,mobile} = response.data
    telNum.value = mobile
    //此处判断是否验证，来控制显示那种图标
    accountLists.value = accountList.map(item => {
      if (item.nameInfo == '邮箱验证') {
        if (emailStatus == "0") {
          item.nameInfoStyle = 'name-info-no'
          item.explainInfo = '立即绑定'
        } else {
          item.nameInfoStyle = 'name-info-yes'
          item.explainInfo = '更换'
        }
      } else if (item.nameInfo == '手机验证') {
        item.detailInfo = telNum.value ? `您验证的手机：${telNum.value}` : '绑定手机号可用于登录与找回密码，并接收订单与安全通知'
      } else if (item.nameInfo == '实名认证') {
        if (relAuthenticationStatus == "0") {
          item.nameInfoStyle = 'name-info-no'
          item.explainInfo = '立即认证'
        } else {
          item.nameInfoStyle = 'name-info-yes'
          item.explainInfo = '更换'
        }
      }

      return item
    })
  })
}


</script>

<style scoped lang="scss">
.red-line {
  border-bottom: 5px solid rgba(255, 55, 29, 0.85);
}

.section {
  width: 1000px;
  margin: 15px auto 0;

  .sidebarMenu {
    //width: 201px;
    float: left;
  }

  .right-section {
    width: 789px;
    float: right;

    .breadcrumb {
      border: 1px solid #efefef;
      height: 38px;
      overflow: hidden;
      background: #FF2D55 repeat-x;
      padding: 0 15px;
      line-height: 38px;
      color: #ffffff;
      margin-bottom: 15px;
    }

    .right-tab {
      margin-top: 23px;

      .title {
        border-bottom: 2px solid #FF2D55;
        height: 29px;

        li {
          width: 104px;
          height: 29px;
          overflow: hidden;
          margin-left: 5px;

        }

        li.right {
          float: right;
          color: #333;
          font-size: 14px;
          font-family: "Microsoft YaHei";
          width: 180px;
          text-align: right;
          line-height: 20px;

          span {
            color: #FF2D55;
            font-size: 28px;
            font-weight: 700;
            line-height: 25px;
          }
        }

        li.left {
          background: #FF2D55;
          display: block;
          line-height: 29px;
          text-align: center;
          font-size: 12px;
          padding: 0 2px;
          background-position: -139px -93px;
          color: #fff;
          font-weight: 700;
          float: left;
        }
      }
    }

    .box {

      .account-info {
        border: 1px solid #eef1f3;
        padding: 25px 20px 25px 25px;
        overflow: hidden;
        zoom: 1;
        margin-top: 15px;

        ul {
          list-style: none;

          li {
            float: left;
          }

          .name-info-yes {
            width: 149px;
            border-right: 1px solid #eef1f3;
            height: 30px;
            line-height: 30px;
            background: url(//assets.damai.cn/damai_v2/passport/images/right2.png) no-repeat;
            padding-left: 55px;
            font-weight: 700;
            font-size: 18px;
            font-family: 'Microsoft YaHei';
          }

          .name-info-no {
            width: 149px;
            border-right: 1px solid #eef1f3;
            height: 30px;
            line-height: 30px;
            background: url(//assets.damai.cn/damai_v2/passport/images/sec2.png) no-repeat;
            padding-left: 55px;
            font-weight: 700;
            font-size: 18px;
            font-family: 'Microsoft YaHei';
          }

          .detail {
            width: 445px;
            padding: 0 10px;
            color: #999;
            line-height: 16px;

            span {
              float: left
            }
          }

          .explain {
            float: right;
            width: 90px;
            text-align: center;
            line-height: 30px;

          .pathBtn {
            background: no-repeat scroll 0 -234px;
            width: 72px;
            height: 21px;
            line-height: 21px;
            text-align: center;
            color: #FF2D55;
            display: inline-block;
            font-size: 14px;
          }
            .btnColor {
              color: #2f97b4;
              font-size: 14px;
            }
          }
        }
      }

    }
  }

}

.foot {
  margin-top: 565px;
}

:deep(.el-input__wrapper) {
  flex-grow: 0.3
}


</style>
