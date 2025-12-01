<template>
  <div class="app-header">
    <div class="header">
      <router-link to="/index" class="link">
        <img :src="logo" alt="">
      </router-link>
      <div class="localHeader" v-if="isShowHeader">
        <el-icon :size="16">
          <Location/>
        </el-icon>

        <el-popover placement="bottom" @click="visible = !visible" style="height: 10px">
          <template #reference>
            <span style="margin-right: 16px" class="city-location">{{ localName }}<el-icon :size="12"> <CaretBottom/></el-icon></span>
          </template>
          <div class="city">
            <div class="now-city">
              <span class="title-city">当前城市：</span><span class="city-name select-city">{{ localName }}</span>
            </div>
            <div class="hot-city">
              <span class="title-city">热门城市：</span>
              <ul class="list-city">
                <li class="city-name" v-for="item in hotCity" :key="item.id" @click="getCityInfoList(item)">
                  {{ item.name }}
                </li>
              </ul>
            </div>
            <div class="others-city">
              <span class="title-city">其他城市：</span>
              <ul class="list-city">
                <li class="city-name" v-for="item in otherCity" :key="item.id" @click="getCityInfoList(item)">
                  {{ item.name }}
                </li>
              </ul>
            </div>
          </div>
        </el-popover>


      </div>
      <div class="recommendHeader" v-if="isShowHeader">
        <router-link to="/index" class="routeHome" tag="div">首页</router-link>
        <router-link to="/allType/index" class="routeType" tag="div">分类</router-link>
      </div>
      <div class="searchHeader" v-if="isShowHeader">
        <el-input
            v-model="iptSearch"
            placeholder="搜索明星、演出、体育赛事"
            class="input-with-search"
        >
          <template #prepend>
            <el-icon :size="20">
              <Search/>
            </el-icon>
          </template>
          <template #append>
            <el-button class="searchBtn" @click="getProgramSearchList">搜索</el-button>
          </template>
        </el-input>
      </div>
      <div class="rightHeader" v-if="isShowHeader">
        <div class="box-left">

          <el-popover :width="100">
            <template #reference>
              <span><img :src="photo" alt="" class="">
                <router-link to="/login" class="log">{{ isLoginToken }}</router-link></span>
            </template>
            <template #default>
              <ul class="loginInfo">
                <li>
                  <router-link to="/personInfo/index">个人信息</router-link>
                </li>
                <li>
                  <router-link to="/accountSettings/index">账号设置</router-link>
                </li>
                <li>
                  <router-link to="/orderManagement/index">订单管理</router-link>
                </li>
                <li @click="loginOut" class="logOut" v-if="isHasToken">
                  <span class="loginOut">退出登录</span>
                </li>
              </ul>
            </template>
          </el-popover>

        </div>
      </div>
    </div>
  </div>
</template>

<script setup>

import logo from '@/assets/login/logo1.png'
import photo from '@/assets/login/photo.png'
import {ref, reactive, onMounted, nextTick} from 'vue'
import {getToken, getUserIdKey, removeToken, removeUserIdKey, removeName} from "../../utils/auth";
import useUserStore from '@/store/modules/user'
import {getPersonInfoId} from '@/api/personInfo'
import {useRoute, useRouter} from 'vue-router'
import {getCurrentCity, getHotCity, getOtherCity, getCityInfo} from '@/api/area'
import {defineEmits} from 'vue'
import {getProgramSearch} from "@/api/allType";
import {useMitt} from "@/utils/index";

const emitter = useMitt();

const route = useRoute()
const router = useRouter()
const isLoginToken = ref('登录')
const isHasToken = ref(false)
const iptSearch = ref('')
const isShowHeader = ref(true)
const userStore = useUserStore()
const localName = ref('')
const localId = ref('')
const hotCity = ref([])
const otherCity = ref([])
const visible = ref(false)
const queryParams = ref({
  content: '',
  pageNumber: 1,
  pageSize: 10,
  timeType: 0
})
const emits = defineEmits(['updateValue'])
//初始化判断是否是登录界面，是否显示登录搜索等功能
const path = route.path
if (path == '/login') {
  isShowHeader.value = false
} else {
  isShowHeader.value = true
}

const handleOpen = (key, keyPath) => {
  console.log(key, keyPath)
}
const handleClose = (key, keyPath) => {
  console.log(key, keyPath)
}

//退出到首页,设置token为空，并且昵称变为登录
function loginOut() {
  userStore.logOut().then(() => {
    location.href = '/';
    isLoginToken.value = '登录'
    removeToken('Admin-Token')
    removeUserIdKey('userId')
    removeName('userName')
    isHasToken.value = false
  })

}

//初始化如果cookie存在id，通过id获取昵称，回显到登录位置
if (getUserIdKey()) {
  getNickName()
}

function getNickName() {
  const id = getUserIdKey()
  getPersonInfoId({id: id}).then(response => {
    if (response.data != null) {
      let {name} = response.data
      isLoginToken.value = name
      if (isLoginToken.value && isLoginToken.value.length > 2) {
        isLoginToken.value = isLoginToken.value.slice(0,2)+"..."
      }
      isHasToken.value = true
    }
  })
}

onMounted(() => {
  getCurrent()
  getHot()
  getOther()
})

//当前城市
function getCurrent() {
  getCurrentCity().then(response => {
    let {name, parentId, id, type} = response.data
    localName.value = name
    localId.value = id
    emits('updateValue', localId.value)
  })

}


//热门城市
function getHot() {
  getHotCity().then(response => {
    hotCity.value = response.data

  })

}

//其他城市
function getOther() {
  getOtherCity().then(response => {
    otherCity.value = response.data
  })
}

/**
 * 点击改变当前地点后，获取初始化接口更新地点
 * @param params
 */
function getCityInfoList(params) {
  getCityInfo({id: params.id}).then(response => {
    let {name, parentId, id, type} = response.data
    localName.value = name
    localId.value = id
    getCurrent()
    getHot()
    getOther()
  })
}
function getProgramSearchList() {
  queryParams.value.content = iptSearch.value
  getProgramSearch(queryParams.value).then(response => {
    emitter.emit('searchList',response.data)
    router.push({path: "/allType/index"});
  })
}

</script>

<style scoped lang="scss">
.app-header {
  width: 100%;
  height: 72px;
  box-shadow: 0 2px 16px 0 rgba(220, 220, 220, .5);

  .header {
    width: 1200px;
    margin: 0 auto;
    height: 72px;

    .link {
      img {
        float: left;
        margin-top: 10px;
        width: 108px;
        height: 48px;
      }
    }

    .localHeader {
      width: 66px;
      height: 100%;
      float: left;
      position: relative;
      margin-left: 54px;
      line-height: 72px;
      white-space: nowrap;
      cursor: pointer;

      .city-location {
        max-width: 30px;
        font-size: 16px;
        color: #2D2D2D;
        display: inline-block;
        vertical-align: middle;
        margin-left: 5px;
        margin-right: 5px;
        //white-space: nowrap;
        //overflow: hidden;
        //text-overflow: ellipsis;
        border: none;

        &:hover {
          background: none;
        }
      }


    }

      .recommendHeader {
      max-width: 220px;
      height: 100%;
      float: left;
      margin-right: 0;
      margin-left: 40px;
      line-height: 72px;
      overflow: hidden;

      .routeHome {
        display: inline-block;
        font-size: 16px;
        margin-right: 32px;
        overflow: hidden;

      }

      .routeHome.router-link-active {
        color: #FF2D55;
      }

      .routeType {
        display: inline-block;
        font-size: 16px;
        color: #000;
        margin-right: 32px;
        overflow: hidden;

      }

      .routeType.router-link-active {
        color: rgba(255, 55, 29, 0.85);
      }
    }

    .searchHeader {
      width: 32%;
      height: 46px;
      margin-top: 12px;
      margin-left: 286px;
      line-height: 46px;
      float: left;
      position: relative;

      .input-with-search {
        width: 353px;
        height: 44px;
        position: absolute;
        left: 0;
        top: 0;
        z-index: 10;
        font-size: 16px;
        outline: 0;
        -webkit-appearance: none;
        border: 0;
        border-top-left-radius: 46px;
        border-bottom-left-radius: 46px;
        background-color: #f8f8f8;
        border-right-color: #FF2D55;
        box-sizing: content-box;

        :deep .el-input-group__prepend {
          box-shadow: none;
          border-radius: 27px 0 0 27px;
        }

        :deep .el-input__wrapper {
          box-shadow: none !important;
          background-color: #f8f8f8 !important;
        }
      }

      .searchBtn {
        width: 82px;
        height: 100%;
        position: absolute;
        right: 0;
        top: 0;
        background: #FF2D55;
        font-size: 16px;
        text-align: center;
        color: #FFF;
        border-radius: 0 27px 27px 0;
        z-index: 11;
        letter-spacing: 1px;
        cursor: pointer;

        :deep(span) {
          font-weight: 600;
          letter-spacing: 1px;
        }
      }
    }

    .rightHeader {
      min-width: 55px;
      height: 100%;
      position: relative;
      float: right;
      line-height: 72px;

      .box-left {
        height: 100%;
        display: inline-block;
        line-height: 72px;
        cursor: pointer;
        position: relative;
        margin-left: 20px;

        &:hover {
          color: rgba(255, 55, 29, 0.85);
        }

        img {
          width: 26px;
          z-index: 20000;
          display: inline-block;
          margin-right: 4px;
          vertical-align: middle;
        }

        .log {
          max-width: 48px;
          white-space: nowrap;
          overflow: hidden; /* 超出容器部分隐藏 */
          text-overflow: ellipsis;
        }

      }

      .box-right {
        height: 100%;
        display: inline-block;
        line-height: 72px;
        cursor: pointer;
        position: relative;
        margin-left: 20px;

        &:hover {
          color: rgba(255, 55, 29, 0.85);
        }

        img {
          width: 26px;
          z-index: 20000;
          display: inline-block;
          margin-right: 4px;
          vertical-align: middle;
        }


      }
    }
  }
}

.loginInfo {
  width: 100px;
  list-style-type: none;
  padding-left: 32px;

  li {
    height: 50px;
    line-height: 50px;

    a {
      width: 100px;
      height: 50px;
      display: block;
      font-size: 16px;
    }
  }

  .logOut {
    cursor: pointer;

    .loginOut {
      width: 100px;
      height: 50px;
      display: block;
      font-size: 16px;
    }
  }
}


.city {
  width: 600px;
  position: relative;
  left: -115px;
  top: -72px;
  margin-top: 60px;
  z-index: 999;
  background: #ffffff;
  border: 1px solid #ebebeb;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
  border-radius: 12px;
  padding: 24px;
  box-sizing: border-box;

  .now-city, .hot-city, .others-city {
    display: flex;
    align-items: flex-start;
  }

  .title-city {
    width: 80px;
    font-size: 14px;
    color: #909399;
    line-height: 32px;
    flex-shrink: 0;
    font-weight: 400;
  }

  .now-city {
    margin-bottom: 12px;
    .city-name {
      display: inline-block;
      font-size: 14px;
      line-height: 32px;
      padding: 0 16px;
      border-radius: 16px;
      
      &.select-city {
        background: #FF2D55;
        color: #fff;
        font-weight: 500;
        box-shadow: 0 4px 12px rgba(255, 45, 85, 0.3);
      }
    }
  }

  .list-city {
    flex: 1;
    list-style: none;
    padding: 0;
    margin: 0;
    display: flex;
    flex-wrap: wrap;
    gap: 4px;

    .city-name {
      font-size: 14px;
      color: #303133;
      padding: 0 6px;
      line-height: 28px;
      border-radius: 4px;
      cursor: pointer;
      transition: all 0.2s ease;

      &:hover {
        color: #FF2D55;
        background-color: rgba(255, 45, 85, 0.06);
      }
    }
  }

  .others-city {
    margin-top: 16px;
    padding-top: 16px;
    border-top: 1px solid #f2f2f2;
  }
}


</style>
