<template>
  <div class="app-container">

    <el-row class="sidebar">
      <el-col :span="24">
        <h5 class="mb-2">我的票务平台</h5>
        <div class="menu_border">
          <el-menu
              :default-openeds="defaultOpened1"
              class="el-menu-vertical-demo"
              @open="handleOpen"
              @close="handleClose"

          >
            <el-sub-menu index="1" class="titleMenu">
              <template #title>
                <span>交易中心</span>
              </template>
              <el-menu-item index="5" :class="{ 'is-active': activeIndex === '5'}"><router-link to="/orderManagement/index">订单管理</router-link></el-menu-item>
            </el-sub-menu>

          </el-menu>
          <el-menu
              :default-openeds="defaultOpened2"
              class="el-menu-vertical-demo"
              @open="handleOpen"
              @close="handleClose"
          >
            <el-sub-menu index="1">
              <template #title>
                <span>账户中心</span>
              </template>
<!--              <el-menu-item index="1" :class="{ 'is-active': props.activeIndex === '1' }">收货地址</el-menu-item>-->
              <el-menu-item index="2" :class="{ 'is-active': props.activeIndex === '2' }"><router-link to="/accountSettings/index">账号设置</router-link></el-menu-item>
              <el-menu-item index="3" :class="{ 'is-active': props.activeIndex === '3' }"><router-link to="/personInfo/index">个人信息</router-link></el-menu-item>
              <el-menu-item index="4" :class="{ 'is-active': props.activeIndex === '4' }"><router-link to="/personInfo/ticketUser">常用购票人</router-link></el-menu-item>
            </el-sub-menu>

          </el-menu>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import {Document, Menu as IconMenu, Location, Setting} from '@element-plus/icons-vue'
import {ref, defineProps} from 'vue'
// index: 需要打开的 sub-menu 的 index
const handleOpen = (key, keyPath) => {
  console.log(key, keyPath)
}
const handleClose = (key, keyPath) => {
}
//默认所有项都打开
const defaultOpened1 = ref(['1'])
const defaultOpened2 = ref(['1', '2', '3', '4'])
const props = defineProps({
  activeIndex: String
});


</script>

<style scoped lang="scss">
.app-container {
  //width: 100%;
  //height: 100%;
  //position: absolute;
  //background: #ffffff;
  .sidebar {
    width: 201px;
  }

}

.el-menu--vertical:not(.el-menu--collapse):not(.el-menu--popup-container) .el-menu-item,
.el-menu--vertical:not(.el-menu--collapse):not(.el-menu--popup-container) .el-menu-item-group__title,
.el-menu--vertical:not(.el-menu--collapse):not(.el-menu--popup-container) .el-sub-menu__title {
  padding-left: 20px;
}

:deep(.menu_border) {
  border: 1px solid #ebeef5;
  border-top: none;
  border-radius: 0 0 8px 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  background: #fff;
  overflow: hidden;
  min-height: 700px;
}

:deep(.el-menu) {
  border-right: none;
}

:deep(.el-sub-menu .el-menu-item) {
  height: 50px;
  line-height: 50px;
  font-size: 15px;
  margin: 4px 0;
}

:deep(.el-sub-menu .el-menu-item.is-active) {

}

:deep(.el-sub-menu .el-menu-item:hover),
:deep(.el-sub-menu .el-menu-item.is-active) {
  border-left: 4px solid #FF2D55;
  background: #fff5f7 url(//assets.damai.cn/damai_v2/passport/images/i.png) no-repeat scroll 170px center;
  height: 50px;
  margin-left: 0;
  position: relative;
  padding-left: 32px;
  text-decoration: none;
  color: #FF2D55;
  font-weight: 600;
}

:deep(.el-menu .el-sub-menu .el-sub-menu__title) {
  background: #fafafa;
  border-bottom: 1px solid #f0f0f0;
  height: 45px;
  line-height: 45px;
  padding-left: 20px;
  font-family: "Microsoft YaHei";
  color: #333;
  font-weight: 700;
  font-size: 16px;
}

/* 隐藏Element Plus侧边菜单展开/折叠箭头 */
:deep(.el-sub-menu .el-sub-menu__icon-arrow) {
  display: none;
}

:deep(.el-submenu__title .el-submenu__icon-arrow) {
  display: none;
}

.mb-2 {
  background: linear-gradient(90deg, #FF2D55 0%, #ff6b88 100%);
  width: 100%;
  height: 40px;
  line-height: 40px;
  padding-left: 20px;
  box-sizing: border-box;
  font-family: "Microsoft YaHei";
  font-size: 15px;
  color: #fff;
  font-weight: 700;
  border-radius: 8px 8px 0 0;
  margin: 0;
}

:deep(.el-sub-menu .el-menu-item a){
  width: 100%;
  height: 100%;
  display: block;
}

</style>
