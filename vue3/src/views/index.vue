<template>
  <Header @updateValue="handleUpdate"></Header>
  <div class="app-container">
    <!--    轮播图-->
    <el-carousel :interval="5000" arrow="always" class="carousel-lamp">
      <el-carousel-item v-for="item in picArr" :key="item">
        <img :src="item" alt="">
      </el-carousel-item>
    </el-carousel>
    <!--    中间各类型-->
    <div class="category">
      <ul>
        <li v-for="(item,ind) in categoryArr">
          <router-link   :to="{ path: '/allType/index', query: {type:item.type,name:item.name,id:item.id} }">
            <i :class="['sprit','sprit'+(ind+1)]"></i>
            <span>{{ item.name }}</span>
          </router-link>
        </li>
      </ul>
    </div>

    <div class="diffrentType" v-for="(item,index) in  programList">
      <div>
        <div class="name">
          <span>{{ item.categoryName }}</span>
          <router-link :to="{ path: '/allType/index', query: {type:1,name:item.categoryName,id:item.categoryId} }" class="more">查看全部</router-link>
        </div>
        <div class="box">
          <div class="box-left" v-if="item.programListVoList && item.programListVoList.length">
            <router-link :to="{ name: 'detial', params: { id: item.programListVoList[0].id }}"><img :src="item.programListVoList[0].itemPicture" alt=""></router-link>
          </div>

          <div class="box-right">
            <div class="rtLink" v-for="(dict,ind) in item.programListVoList.slice(1, 7)">
              <router-link  :to="{ name: 'detial', params: { id: dict.id }}" >
                <img :src="dict.itemPicture" alt="">
                <div class="info">
                  <div class="img-title">{{ dict.title }}</div>
                  <div class="local">{{ dict.place }}</div>
                  <div class="showTime">{{ dict.showTime }}{{ dict.showWeekTime }}</div>
                  <div class="price">{{ dict.minPrice }} <span class="rise">起</span></div>
                </div>
              </router-link>
            </div>
          </div>

        </div>

      </div>
    </div>
    <Footer></Footer>
  </div>
</template>

<script setup>
import Header from '@/components/header/index'
import swiperPic1 from '@/assets/section/javaup1.png'
import gujuji from '@/assets/section/lunbo1.png'
import detail from '@/assets/section/lunbo2.png'
import small from '@/assets/section/small.jpg'
import {onMounted, ref} from 'vue'
import Footer from '@/components/footer/index'
import {getcategoryType, getMainCategory} from '@/api/index'
//轮播图目前固定一张
const picArr = [swiperPic1, gujuji, detail]

const categoryArr = ref([])
const programList = ref([])
const queryParams = ref({
  "areaId": 0,
  "parentProgramCategoryIds": []
})
const programListVoList = ref([])
//     [
//   {name: '演唱会', setClass: 'sprit1', url: '/allType/index'},
//   {name: '话剧歌剧', setClass: 'sprit2', url: '/allType/index'},
//   {name: '体育', setClass: 'sprit3', url: '/allType/index'},
//   {name: '儿童亲子', setClass: 'sprit4', url: '/allType/index'},
//   {name: '展览休闲', setClass: 'sprit5', url: '/allType/index'},
//   {name: '音乐会', setClass: 'sprit6', url: '/allType/index'},
//   {name: '曲苑杂坛', setClass: 'sprit7', url: '/allType/index'},
//   {name: '舞蹈芭蕾', setClass: 'sprit8', url: '/allType/index'},
//   {name: '二次元', setClass: 'sprit9', url: '/allType/index'},
//   {name: '旅游展览', setClass: 'sprit10', url: '/allType/index'}
// ]
//获取中间的类目信息
onMounted(() => {
  getgetcategoryList()

})

//查询节目类型
function getgetcategoryList() {
  getcategoryType({type: 1}).then(response => {
    const list = Array.isArray(response.data) ? response.data : []
    categoryArr.value = list
    getMainCategoryList()
  }).catch(() => {
    categoryArr.value = []
    programList.value = []
  })
}

function handleUpdate(value) {
  queryParams.value.areaId = value
  if (categoryArr.value && categoryArr.value.length) {
    getMainCategoryList()
  }
}

function getMainCategoryList() {
  queryParams.value.parentProgramCategoryIds = []
  for (let i = 0; i < 4 && i < categoryArr.value.length; i++) {
    queryParams.value.parentProgramCategoryIds.push(categoryArr.value[i].id)
  }
  if (!queryParams.value.parentProgramCategoryIds.length) {
    programList.value = []
    return
  }
  getMainCategory(queryParams.value).then(response => {
    const data = Array.isArray(response.data) ? response.data : []
    programList.value = data
  }).catch(() => {
    programList.value = []
  })
}



</script>
<style scoped lang="scss">
.app-container {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding-bottom: 40px;

  .carousel-lamp {
    width: 100%;
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 8px 16px rgba(0, 0, 0, 0.08);
    img {
      width: 100%;
      height: 300px;
      object-fit: cover;
    }
  }

  .category {
    margin-top: 24px !important;
    padding: 24px 0;
    border: 1px solid #f0f0f0;
    border-radius: 12px;
    background: #fff;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.04);

    ul {
      list-style-type: none;
      margin: 0;
      padding: 0 20px;
      width: 100%;
      height: auto;
      display: flex;
      justify-content: space-around;
      align-items: center;
      box-sizing: border-box;

      li {
        display: block;
        width: auto;
        text-align: center;
        transition: transform 0.3s ease;

        &:hover {
          transform: translateY(-4px);
        }

        a {
          display: flex;
          flex-direction: column;
          align-items: center;
          width: 110px;
          height: auto;
          text-decoration: none;

          span {
            margin-top: 8px;
            font-size: 16px;
            color: #333;
            text-align: center;
            transition: color 0.3s;
          }

          &:hover span {
             color: #ff371d;
          }
        }

        .sprit {
          display: block;
          width: 48px;
          height: 48px;
          margin: 0 auto;
          background: url("@/assets/section/sprit.png") no-repeat;
          background-size: 100% auto;
        }

        .sprit1 { background-position: 0 0; }
        .sprit2 { background-position: 0 -64px; }
        .sprit3 { background-position: 0 -120px; }
        .sprit4 { background-position: 0 -180px; }
        .sprit5 { background-position: 0 -240px; }
        .sprit6 { background-position: 0 -297px; }
        .sprit7 { background-position: 0 -360px; }
        .sprit8 { background-position: 0 -420px; }
        .sprit9 { background-position: 0 -480px; }
        .sprit10 { background-position: 0 -540px; }
      }
    }
  }

  .diffrentType {
    width: 100%;
    position: relative;
    padding: 24px;
    background: #fff;
    border: 1px solid #f0f0f0;
    border-radius: 12px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
    margin-top: 24px;
    display: flex;
    flex-direction: column;
    box-sizing: border-box;

    .name {
      font-size: 24px;
      font-weight: 600;
      color: #333;
      width: 100%;
      height: 40px;
      line-height: 40px;
      margin-bottom: 20px;
      display: flex;
      justify-content: space-between;
      align-items: center;
      border-left: 5px solid #ff371d;
      padding-left: 16px;
      box-sizing: border-box;

      span {
        flex: 1;
      }

      .more {
        font-size: 14px;
        color: #999;
        text-decoration: none;
        transition: color 0.3s;
        text-align: right;
        min-width: 60px;

        &:hover {
          color: #ff371d;
        }
      }
    }

    .box {
      margin-top: 0;
      display: flex;
      justify-content: space-between;

      .box-left {
        width: 270px;
        height: 360px;
        position: relative;
        overflow: hidden;
        border-radius: 8px;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
        flex-shrink: 0;

        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
          transition: transform 0.5s ease;
        }

        &:hover img {
          transform: scale(1.05);
        }
      }

      .box-right {
        flex: 1;
        margin-left: 20px;
        height: 360px;
        // 移除 overflow: hidden 以允许 hover 浮动效果不被遮挡
        // overflow: hidden; 
        display: flex;
        flex-wrap: wrap;
        align-content: flex-start;

        .rtLink {
          width: 273px;
          height: 160px;
          margin-right: 18px;
          margin-bottom: 40px;
          display: block; // .rtLink 本身作为容器
          transition: transform 0.3s ease;

          &:hover {
             transform: translateY(-4px);

             .img-title {
               color: #ff371d;
             }
          }

          &:nth-of-type(3n) {
            margin-right: 0;
          }

          a {
            display: flex; // a 标签作为 flex 容器
            width: 100%;
            height: 100%;
            text-decoration: none;
            color: inherit;
          }

          img {
            width: 118px;
            height: 158px;
            border-radius: 6px;
            object-fit: cover;
            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
            flex-shrink: 0;
            display: block; // 防止图片底部留白
          }

          .info {
            flex: 1;
            padding-left: 14px;
            display: flex;
            flex-direction: column;
            justify-content: space-between;
            overflow: hidden;
            height: 100%; // 确保高度撑满

            .img-title {
              font-size: 15px;
              color: #333;
              line-height: 20px; // 显式设置行高
              font-weight: 500;
              margin-bottom: 4px;
              display: -webkit-box;
              -webkit-box-orient: vertical;
              -webkit-line-clamp: 2;
              overflow: hidden;
              text-overflow: ellipsis; // 增加省略号
              transition: color 0.3s;
              max-height: 42px; // 2行 * 20px + 少量缓冲，防止溢出
            }

            .local, .showTime {
              font-size: 12px;
              color: #999;
              margin-top: 4px;
              white-space: nowrap;
              overflow: hidden;
              text-overflow: ellipsis;
              display: block;
              line-height: 1.5;
            }

            .price {
              font-size: 20px;
              color: #ff371d;
              font-weight: bold;
              margin-top: auto;
              line-height: 1;
              padding-bottom: 2px; // 微调底部位置

              .rise {
                font-size: 12px;
                color: #999;
                font-weight: normal;
                margin-left: 2px;
              }
            }
          }
        }
      }

    }
  }
}


</style>
