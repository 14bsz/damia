<template>
  <!--点击进入单独界面详情-->
  <div class="app-container">
    <Header></Header>
    <div class="app-container">
      <div class="wrapper">
        <div class="box-left">
          <div class="box-detail">
            <div class="count">
              <div class="box-img">
                <img :src="detailList.itemPicture || 'https://picsum.photos/270/360'" alt="节目海报" loading="lazy">
              </div>
              <div class="order">
                <div class="title">
                  <span class="tips" v-if="detailList.electronicDeliveryTicket=='1'">电子票</span>
                  <span class="tips" v-else-if="detailList.electronicDeliveryTicket=='2'">快递票</span>
                  <h1 class="text-title">{{ detailList.title }}</h1>
                </div>
                
                <div class="info-section">
                  <div class="info-row">
                    <span class="label">时间：</span>
                    <span class="value">{{ formatDateWithWeekday(detailList.showTime, detailList.showWeekTime) }}</span>
                  </div>
                  <div class="info-row">
                    <span class="label">场馆：</span>
                    <span class="value">{{ detailList.areaName }} | {{ detailList.place }}</span>
                  </div>
                </div>

                <!-- 预售/提示 -->
                <div class="notice-card" v-show="detailList.preSell=='1'">
                  <div class="notice-header">
                    <span class="tag-presell" v-if="detailList.preSell=='1'">预售</span>
                    <span class="notice-text">{{ detailList.preSellInstruction }}</span>
                  </div>
                  <div class="notice-body" :class="{ expanded: noticeExpanded }" @click="toggleNotice">
                     {{ detailList.importantNotice }}
                     <i class="arrow-icon" :class="{ up: noticeExpanded }"></i>
                  </div>
                </div>

                <!-- 选座/选票区域 -->
                <div class="selection-area">
                  <!-- 城市 -->
                  <div class="selection-row" v-if="detailList.areaName">
                    <div class="row-label">城市</div>
                    <div class="row-content">
                      <div class="city-tag active">{{ detailList.areaName }}</div>
                    </div>
                  </div>

                  <!-- 场次 -->
                  <div class="selection-row">
                    <div class="row-label">场次</div>
                    <div class="row-content">
                      <div class="time-tag active">
                        {{ formatDateWithWeekday(detailList.showTime, detailList.showWeekTime) }}
                      </div>
                      <div class="time-note">场次时间均为演出当地时间</div>
                    </div>
                  </div>

                  <!-- 票档 -->
                  <div class="selection-row">
                    <div class="row-label">票档</div>
                    <div class="row-content">
                      <div class="ticket-list">
                        <div 
                          class="ticket-item" 
                          v-for="(item, index) in ticketCategoryVoList"
                          :key="item.id"
                          :class="{ active: actvieIndex == index }"
                          @click="ticketClick(item, index)"
                        >
                          <span class="ticket-price">{{ item.introduce }}</span>
                          <span class="ticket-desc" v-if="item.price != item.introduce">¥{{ item.price }}</span>
                        </div>
                      </div>
                    </div>
                  </div>

                  <!-- 数量 -->
                  <div class="selection-row" v-if="detailList.permitChooseSeat !== '1'">
                    <div class="row-label">数量</div>
                    <div class="row-content flex-center">
                      <el-input-number v-model="num" :min="1" :max="6" @change="handleChange" size="large" />
                      <span class="limit-note">每笔订单限购6张</span>
                    </div>
                  </div>
                </div>

                <!-- 底部合计与购买 -->
                <div class="footer-action">
                  <div class="total-price">
                    <span class="label">合计</span>
                    <span class="price">￥{{ allPrice || countPrice }}</span>
                  </div>
                  <div class="action-buttons">
                    <button class="btn-buy" @click="nowBuy">
                      {{ detailList.permitChooseSeat === '1' ? '选座购买' : '立即购买' }}
                    </button>
                  </div>
                </div>

              </div>
            </div>
          </div>
          <div class="box-item">
            <div class="box-menu">
              <router-link class="menu-children" to="#projectDetial" @click="detialClick('#projectDetial',1)"
                           :class="{menuActive: menuActive == 1}">项目详情
              </router-link>
              <router-link class="menu-children" to="#ticketNeed" @click="detialClick('#ticketNeed',2)"
                           :class="{menuActive: menuActive == 2}">购票须知
              </router-link>
              <router-link class="menu-children" to="#watchNeed" @click="detialClick('#watchNeed',3)"
                           :class="{menuActive: menuActive == 3}">观演须知
              </router-link>
            </div>
            <div id="projectDetial">
              <div class="proDetial">活动介绍</div>
              <div class="rich-content" v-html="formattedDetail"></div>
            </div>
            <div id="ticketNeed">
              <div class="proDetial">购票须知</div>
              <ul>
                <li v-for="item in ticketNeedInfo">
                  <span>{{ item.name }}</span>
                  <div>{{ item.value }}</div>
                </li>

              </ul>
            </div>
            <div id="watchNeed">
              <div class="proDetial">观演须知</div>
              <ul>
                <li v-for="item in watchNeedInfo">
                  <span v-if="item.value!=''">{{ item.name }}</span>
                  <div v-if="item.value!=''">{{ item.value }}</div>
                </li>

              </ul>
            </div>
          </div>
        </div>
        <div class="box-right">
          <div class="service">

            <div class="sit" v-show="detailList.permitChooseSeat=='1'" @click="goSeatMap" aria-label="查看座位图">查看座位图</div>
            <div class="service-note">
              <!-- 退票 -->
              <div class="service-item" v-if="detailList.permitRefund!=''">
                <div class="service-name">
                  <template v-if="detailList.permitRefund=='0'">
                    <i class="icon-no"></i><span>不支持退</span>
                  </template>
                  <template v-else-if="detailList.permitRefund=='1'">
                    <i class="icon-yes"></i><span>条件退</span>
                  </template>
                  <template v-else-if="detailList.permitRefund=='2'">
                    <i class="icon-yes"></i><span>全部退</span>
                  </template>
                </div>
                <div class="service-desc" v-if="detailList.refundExplain">{{ detailList.refundExplain }}</div>
              </div>

              <!-- 实名制 -->
              <div class="service-item" v-if="detailList.relNameTicketEntrance!=''">
                <div class="service-name">
                  <template v-if="detailList.relNameTicketEntrance=='0'">
                    <i class="icon-no"></i><span>不实名购票和入场</span>
                  </template>
                  <template v-else-if="detailList.relNameTicketEntrance=='1'">
                    <i class="icon-yes"></i><span>实名购票和入场</span>
                  </template>
                </div>
                <div class="service-desc" v-if="detailList.relNameTicketEntranceExplain">{{ detailList.relNameTicketEntranceExplain }}</div>
              </div>

              <!-- 选座 -->
              <div class="service-item" v-if="detailList.permitChooseSeat!=''">
                <div class="service-name">
                  <template v-if="detailList.permitChooseSeat=='0'">
                    <i class="icon-no"></i><span>不支持选座</span>
                  </template>
                  <template v-else-if="detailList.permitChooseSeat=='1'">
                    <i class="icon-yes"></i><span>支持选座</span>
                  </template>
                </div>
                <div class="service-desc" v-if="detailList.chooseSeatExplain">{{ detailList.chooseSeatExplain }}</div>
              </div>

              <!-- 配送 -->
              <div class="service-item" v-if="detailList.electronicDeliveryTicket!=''">
                <div class="service-name">
                  <template v-if="detailList.electronicDeliveryTicket=='0'">
                    <i class="icon-no"></i><span>无票</span>
                  </template>
                  <template v-else-if="detailList.electronicDeliveryTicket=='1'">
                    <i class="icon-yes"></i><span>电子票</span>
                  </template>
                  <template v-else-if="detailList.electronicDeliveryTicket=='2'">
                    <i class="icon-yes"></i><span>快递票</span>
                  </template>
                </div>
                <div class="service-desc" v-if="detailList.electronicDeliveryTicketExplain">{{ detailList.electronicDeliveryTicketExplain }}</div>
              </div>

              <!-- 发票 -->
              <div class="service-item" v-if="detailList.electronicInvoice!=''">
                <div class="service-name">
                  <template v-if="detailList.electronicInvoice=='0'">
                    <i class="icon-no"></i><span>纸质发票</span>
                  </template>
                  <template v-else-if="detailList.electronicInvoice=='1'">
                    <i class="icon-yes"></i><span>电子发票</span>
                  </template>
                </div>
                <div class="service-desc" v-if="detailList.electronicInvoiceExplain">{{ detailList.electronicInvoiceExplain }}</div>
              </div>
            </div>

          </div>
          <div class="box-like">
            为你推荐
          </div>
          <ul class="search__box" v-if="recommendList && recommendList.length">
            <li class="search__item" v-for="item in recommendList" :key="item.id || item.title">
                <router-link v-if="item.id" :to="{name:'detial',params:{id:item.id}}" class="link" >
                  <img :src="item.itemPicture" alt="">
                </router-link>
                <img v-else :src="item.itemPicture" alt="">

              <div class="search_item_info">
                  <router-link v-if="item.id" :to="{name:'detial',params:{id:item.id}}"  class="link__title" >
                    {{ item.title }}
                  </router-link>
                  <div v-else class="link__title">{{ item.title }}</div>
                <div class="search__item__info__venue">{{ item.place }}</div>
                <div class="search__item__info__venue">{{ formatDateWithWeekday(item.showTime, item.showWeekTime) }}</div>
                <div class="search__item__info__price">￥<strong>{{ item.minPrice }}</strong> 起</div>
              </div>

            </li>
          </ul>
          <ul class="search__box" v-else>
            <li class="search__item">
              <img :src="'https://picsum.photos/seed/rec1/98/132'" alt="">
              <div class="search_item_info">
                <div class="link__title">精选演出推荐</div>
                <div class="search__item__info__venue">敬请期待更多精彩</div>
                <div class="search__item__info__price"><strong>¥99 起</strong></div>
              </div>
            </li>
            <li class="search__item">
              <img :src="'https://picsum.photos/seed/rec2/98/132'" alt="">
              <div class="search_item_info">
                <div class="link__title">热门活动推荐</div>
                <div class="search__item__info__venue">覆盖全国热门城市</div>
                <div class="search__item__info__price"><strong>¥199 起</strong></div>
              </div>
            </li>
            <li class="search__item">
              <img :src="'https://picsum.photos/seed/rec3/98/132'" alt="">
              <div class="search_item_info">
                <div class="link__title">为你精选</div>
                <div class="search__item__info__venue">近期上新不容错过</div>
                <div class="search__item__info__price"><strong>¥299 起</strong></div>
              </div>
            </li>
          </ul>
        </div>
      </div>
    </div>

   <Footer></Footer>

  </div>

</template>

<script setup name="detial">
import Header from '@/components/header/index'
import Footer from '@/components/footer/index'
import {formatDateWithWeekday } from '@/utils/index'
import {useRoute, useRouter} from 'vue-router'
import {getProgramDetials} from '@/api/contentDetail'
import {ref, computed, watch} from 'vue'
import {   useMitt } from "@/utils/index";
import {getProgramRecommendList} from "@/api/recommendlist.js"
import {getMainCategory} from "@/api/index"
const emitter = useMitt();
//引入reactive
import {reactive} from 'vue'
const route = useRoute();
const router = useRouter();
const paramId = ref(Number(route.params.id))
const detailList = ref([])
const formattedDetail = computed(() => {
  const v = detailList.value && detailList.value.detail ? String(detailList.value.detail).trim() : ''
  if (!v) {
    return '<img src="https://picsum.photos/800/600" style="max-width:100%;height:auto;display:block;margin:0 auto;" />'
  }
  if (v.startsWith('<')) {
    let html = v
      .replace(/src="\/\/img\.alicdn\.com\//g, 'src="https://img.alicdn.com/')
      .replace(/\swidth="[^"]*"/g, '')
      .replace(/\sheight="[^"]*"/g, '')
    html = html.replace(/<img\b(?![^>]*style=)/g, '<img style="max-width:100%;height:auto;display:block;margin:0 auto;"')
    html = html.replace(/<img\b([^>]*?)style="([^"]*?)"/g, (m, pre, style) => {
      return `<img${pre}style="${style};max-width:100%;height:auto;display:block;margin:0 auto;"`
    })
    return html
  }
  const urls = v.split(/[\n\r\s]+/).filter(Boolean)
  const parts = urls.map(u => {
    const url = u.startsWith('//') ? 'https:' + u : u
    if (/\.(png|jpg|jpeg|gif|webp|bmp|svg)(\?.*)?$/i.test(url)) {
      return `<p><img src="${url}" style="max-width:100%;height:auto;display:block;margin:0 auto;" /></p>`
    }
    return `<p><a href="${url}" target="_blank" rel="noopener">${url}</a></p>`
  })
  return parts.join('')
})
const ticketCategoryVoList = ref([])
const actvieIndex = ref('')
const menuActive = ref('')
const ticketNeedInfo = ref([])
const watchNeedInfo = ref([])
const num = ref(1)
const countPrice = ref('')
const allPrice = ref('')
//票档id
const ticketCategoryId = ref('')
//推荐节目列表入参
const recommendParams = reactive({
  areaId: undefined,
  parentProgramCategoryId: undefined,
  programId: undefined
})
recommendParams.programId = paramId.value;
//推荐列表数据
const recommendList = ref([])
getProgramDetialsList()

const noticeExpanded = ref(false)
const toggleNotice = () => {
  noticeExpanded.value = !noticeExpanded.value
}

function getProgramDetialsList() {
  getProgramDetials({id: paramId.value}).then(response => {
    detailList.value = response.data
    if (String(detailList.value.permitChooseSeat) === '1') {
      // 获取后端返回的真实票档数据
      const realTickets = Array.isArray(detailList.value.ticketCategoryVoList) ? [...detailList.value.ticketCategoryVoList] : []
      
      // 定义五档价格配置
      const priceConfig = [2008, 1688, 1288, 888, 288]
      
      // 创建价格到真实票档ID的映射
      const ticketIdMap = {}
      priceConfig.forEach((price, idx) => {
        // 优先从真实票档中找到匹配价格的
        const matchTicket = realTickets.find(t => Number(t.price) === price)
        if (matchTicket) {
          ticketIdMap[price] = matchTicket.id
        } else if (realTickets[idx]) {
          // 如果没有匹配价格,使用索引对应的票档
          ticketIdMap[price] = realTickets[idx].id
        } else {
          // 如果都没有,使用索引+1作为临时ID
          ticketIdMap[price] = idx + 1
        }
      })
      
      // 保存映射关系到详情数据中,供选座页面使用
      detailList.value.seatTicketIdMap = ticketIdMap
      
      // 显示用的票档列表(保持五档价格,但使用真实ID)
      ticketCategoryVoList.value = priceConfig.map((price, idx) => ({
        id: ticketIdMap[price], // 使用真实的票档ID
        introduce: `￥${price}`,
        price: price
      }))
    } else {
      const cats = Array.isArray(detailList.value.ticketCategoryVoList) ? [...detailList.value.ticketCategoryVoList] : []
      const hasEarlyBird = cats.some(x => String(x.introduce || '').includes('早鸟'))
      if (!hasEarlyBird && String(detailList.value.preSell) === '1' && cats.length) {
        const earlyBird = {...cats[0], introduce: '早鸟票'}
        ticketCategoryVoList.value = [earlyBird, ...cats]
      } else {
        ticketCategoryVoList.value = cats
      }
    }
    countPrice.value = ticketCategoryVoList.value[0] ? ticketCategoryVoList.value[0].price : ''
    ticketCategoryId.value = ticketCategoryVoList.value[0] ? ticketCategoryVoList.value[0].id : ''
    allPrice.value = ''
    ticketNeedInfo.value = [{
      name: '限购规则',
      value: detailList.value.purchaseLimitRule,
    }, {
      name: '退票/换票规则',
      value: detailList.value.refundTicketRule,
    }, {
      name: '入场规则',
      value: detailList.value.entryRule,
    }, {
      name: '儿童购票',
      value: detailList.value.childPurchase,
    }, {
      name: '发票说明',
      value: detailList.value.invoiceSpecification,
    }, {
      name: '实名购票规则',
      value: detailList.value.realTicketPurchaseRule,
    }, {
      name: '异常排单说明',
      value: detailList.value.abnormalOrderDescription,
    }]
    watchNeedInfo.value = [{
      name: '演出时长',
      value: detailList.value.performanceDuration
    }, {
      name: '入场时间',
      value: detailList.value.entryTime
    }, {
      name: '最低演出曲目',
      value: detailList.value.minPerformanceCount
    }, {
      name: '主要演员',
      value: detailList.value.mainActor
    }, {
      name: '最低演出时长',
      value: detailList.value.minPerformanceDuration
    }, {
      name: '禁止携带物品',
      value: detailList.value.prohibitedItem
    }, {
      name: '寄存说明',
      value: detailList.value.depositSpecification
    }]
    recommendParams.areaId = detailList.value.areaId
    recommendParams.parentProgramCategoryId = detailList.value.parentProgramCategoryId || detailList.value.programCategoryId
    getRecommendList()
  })
}

const ticketClick = (item, index) => {
  actvieIndex.value = index
  countPrice.value = item.price
  allPrice.value = item.price * Number(num.value || 1)
  ticketCategoryId.value = item.id
}
const detialClick = (url, index) => {
  menuActive.value = index


}
const handleChange = (value) => {
  const priceEach = Number(countPrice.value || 0)
  const qty = Number(value || 1)
  allPrice.value = priceEach * qty
}
const nowBuy=()=>{
  if (String(detailList.value.permitChooseSeat) === '1') {
    router.replace({
      path: `/seat/map/${detailList.value.id}`,
      state: {
        seatTicketIdMap: detailList.value.seatTicketIdMap || {}
      }
    })
  } else {
    const cats = Array.isArray(ticketCategoryVoList.value) ? ticketCategoryVoList.value : []
    const idx = Number(actvieIndex.value)
    const picked = (!isNaN(idx) && cats[idx]) ? cats[idx] : cats.find(x => Number(x.id) === Number(ticketCategoryId.value))
    const label = picked && picked.introduce ? String(picked.introduce) : ''
    router.replace({path:'/order/index',state:
          {'detailList':JSON.stringify(detailList.value),'allPrice':allPrice.value,
            'countPrice':countPrice.value,'num':num.value,'ticketCategoryId':ticketCategoryId.value,'ticketLabel':label}})
  }
}

const goSeatMap = () => {
  if (String(detailList.value.permitChooseSeat) === '1') {
    router.replace({
      path: `/seat/map/${detailList.value.id}`,
      state: {
        seatTicketIdMap: detailList.value.seatTicketIdMap || {}
      }
    })
  }
}



//节目推荐列表
function getRecommendList(){
  getProgramRecommendList(recommendParams).then(response => {
    let data = response.data || []
    // 过滤掉当前详情页的节目
    data = data.filter(item => Number(item.id) !== Number(paramId.value))
    
    if (data.length) {
      recommendList.value = data.slice(0,6)
    } else {
      const homeParams = { areaId: recommendParams.areaId, parentProgramCategoryIds: [] }
      if (recommendParams.parentProgramCategoryId) {
        homeParams.parentProgramCategoryIds = [recommendParams.parentProgramCategoryId]
      }
      getMainCategory(homeParams).then(res => {
        let list = []
        ;(res.data || []).forEach(c => {
          if (c.programListVoList) list.push(...c.programListVoList)
        })
        // 过滤掉当前详情页的节目
        list = list.filter(item => Number(item.id) !== Number(paramId.value))
        
        if (list.length) {
          recommendList.value = list.slice(0,6)
        } else {
          recommendList.value = [
            { itemPicture: 'https://picsum.photos/seed/rec1/98/132', title: '精选演出推荐', place: '敬请期待更多精彩', showTime: '', showWeekTime: '', minPrice: 99 },
            { itemPicture: 'https://picsum.photos/seed/rec2/98/132', title: '热门活动推荐', place: '覆盖全国热门城市', showTime: '', showWeekTime: '', minPrice: 199 },
            { itemPicture: 'https://picsum.photos/seed/rec3/98/132', title: '为你精选', place: '近期上新不容错过', showTime: '', showWeekTime: '', minPrice: 299 }
          ]
        }
      })
    }
  })
}







</script>

<style scoped lang="scss">
.app-container {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  overflow: auto;

  .wrapper {
    display: flex;
    justify-content: space-between;

    .box-left {
      flex: 1;
      min-width: 0;

      .box-detail {
        position: relative;
        padding: 30px;
        background: #fff;
        border-radius: 12px;
        box-shadow: 0 4px 24px rgba(0,0,0,0.04);
        margin-bottom: 20px;

        .count {
          display: flex;
          gap: 30px;
          
          .box-img {
            flex: 0 0 270px;
            width: 270px;
            height: 360px;
            border-radius: 8px;
            overflow: hidden;
            box-shadow: 0 8px 20px rgba(0,0,0,0.15);
            
            img {
              width: 100%;
              height: 100%;
              object-fit: cover;
              transition: transform 0.3s ease;
              
              &:hover {
                transform: scale(1.02);
              }
            }
          }

          .order {
            flex: 1;
            min-width: 0;
            display: flex;
            flex-direction: column;

            .title {
              margin-bottom: 16px;
              
              .tips {
                display: inline-block;
                padding: 2px 8px;
                background: linear-gradient(135deg, #FF2D55, #FF5593);
                color: #fff;
                font-size: 12px;
                border-radius: 4px;
                margin-right: 8px;
                vertical-align: middle;
              }

              .text-title {
                font-size: 22px;
                font-weight: 700;
                color: #111;
                line-height: 1.4;
                vertical-align: middle;
                display: inline;
              }
            }

            .info-section {
              margin-bottom: 20px;
              
              .info-row {
                margin-bottom: 8px;
                font-size: 14px;
                
                .label {
                  color: #999;
                }
                .value {
                  color: #333;
                  margin-left: 8px;
                }
              }
            }

            .notice-card {
              background: #FFF5F7;
              border-radius: 8px;
              padding: 12px 16px;
              margin-bottom: 24px;
              border: 1px solid rgba(255, 45, 85, 0.1);

              .notice-header {
                display: flex;
                align-items: center;
                margin-bottom: 8px;
                
                .tag-presell {
                  color: #FF2D55;
                  border: 1px solid #FF2D55;
                  font-size: 12px;
                  padding: 1px 6px;
                  border-radius: 4px;
                  margin-right: 8px;
                }
                .notice-text {
                  color: #FF2D55;
                  font-size: 14px;
                  font-weight: 500;
                }
              }

              .notice-body {
                font-size: 13px;
                color: #666;
                line-height: 1.6;
                cursor: pointer;
                position: relative;
                padding-right: 20px;
                
                display: -webkit-box;
                -webkit-line-clamp: 2;
                -webkit-box-orient: vertical;
                overflow: hidden;
                transition: all 0.3s;

                &.expanded {
                  -webkit-line-clamp: unset;
                  overflow: visible;
                }
                
                .arrow-icon {
                  position: absolute;
                  right: 0;
                  top: 4px;
                  width: 8px;
                  height: 8px;
                  border-right: 1px solid #999;
                  border-bottom: 1px solid #999;
                  transform: rotate(45deg);
                  transition: transform 0.3s;
                  
                  &.up {
                    transform: rotate(-135deg);
                    top: 8px;
                  }
                }
              }
            }

            .selection-area {
              flex: 1;
              
              .selection-row {
                display: flex;
                margin-bottom: 20px;
                
                .row-label {
                  width: 50px;
                  color: #999;
                  font-size: 14px;
                  padding-top: 8px; /* Align with button text */
                  flex-shrink: 0;
                }
                
                .row-content {
                  flex: 1;
                  
                  &.flex-center {
                    display: flex;
                    align-items: center;
                  }

                  .city-tag {
                    display: inline-block;
                    padding: 0 20px;
                    height: 36px;
                    line-height: 34px;
                    border: 1px solid #ddd;
                    border-radius: 4px;
                    color: #333;
                    font-size: 14px;
                    cursor: pointer;
                    margin-right: 12px;
                    
                    &.active {
                      border-color: #FF2D55;
                      color: #FF2D55;
                      background: #FFF5F7;
                    }
                  }
                  
                  .more-link {
                    font-size: 12px;
                    color: #666;
                    cursor: pointer;
                    &:hover { color: #FF2D55; }
                  }

                  .time-tag {
                    display: inline-block;
                    padding: 0 20px;
                    height: 40px;
                    line-height: 38px;
                    border: 1px solid #ddd;
                    border-radius: 4px;
                    color: #333;
                    font-size: 14px;
                    cursor: pointer;
                    margin-bottom: 8px;
                    
                    &.active {
                      border-color: #FF2D55;
                      color: #FF2D55;
                      background: #FFF5F7;
                    }
                  }
                  
                  .time-note {
                    font-size: 12px;
                    color: #999;
                    margin-top: 4px;
                  }

                  .ticket-list {
                    display: flex;
                    flex-wrap: wrap;
                    gap: 12px;
                    
                    .ticket-item {
                      min-width: 100px;
                      padding: 10px 20px;
                      border: 1px solid #ddd;
                      border-radius: 4px;
                      text-align: center;
                      cursor: pointer;
                      transition: all 0.2s;
                      
                      &:hover {
                        border-color: #FF2D55;
                      }
                      
                      &.active {
                        border-color: #FF2D55;
                        color: #FF2D55;
                        background: #FFF5F7;
                      }

                      .ticket-price {
                        display: block;
                        font-size: 14px;
                      }
                      .ticket-desc {
                        display: block;
                        font-size: 12px;
                        opacity: 0.8;
                      }
                    }
                  }
                  
                  .limit-note {
                    margin-left: 12px;
                    font-size: 12px;
                    color: #999;
                  }
                }
              }
            }

            .footer-action {
              margin-top: 20px;
              padding-top: 20px;
              border-top: 1px dashed #eee;
              display: flex;
              justify-content: space-between;
              align-items: center;
              
              .total-price {
                .label {
                  font-size: 14px;
                  color: #333;
                  margin-right: 12px;
                }
                .price {
                  font-size: 24px;
                  color: #FF2D55;
                  font-weight: 700;
                }
              }
              
              .btn-buy {
                background: linear-gradient(90deg, #FF2D55, #FF5593);
                color: #fff;
                border: none;
                padding: 0 40px;
                height: 48px;
                border-radius: 24px;
                font-size: 18px;
                font-weight: 600;
                cursor: pointer;
                box-shadow: 0 6px 16px rgba(255, 45, 85, 0.25);
                transition: transform 0.1s;
                
                &:hover {
                  filter: brightness(1.05);
                  transform: translateY(-1px);
                }
                &:active {
                  transform: translateY(1px);
                }
              }
            }
          }
        }
      }

          .box-item {
        width: 100%;
        height: 800px;
        
        .box-menu {
          height: 54px;
          line-height: 54px;
          padding-left: 30px;
          border-top: 1px solid #e2e2e2;
          border-bottom: 1px solid #e2e2e2;
          //padding: 60px 30px 0;

          .menu-children {
            font-size: 16px;
            color: #9c9ca5;
            margin-right: 60px;
            cursor: pointer;
          }

        }

          #projectDetial {
          //width: 100%;
          //height:100%;
          padding: 60px 30px 0;

          .proDetial {
            padding-bottom: 14px;
            margin-bottom: 23px;
            font-size: 20px;
            color: #000;
            border-bottom: 1px solid #e2e2e2;
            .rich-content {
              img {
                max-width: 100%;
                height: auto;
                display: block;
                margin: 0 auto;
              }
            }
          }

          img {
            width: 100%;
            height:100%;
            display: block;
            padding-bottom: 50px;
          }
        }

        #ticketNeed {
          width: 100%;
          height: 600px;
          padding: 60px 30px 0;

          .proDetial {
            padding-bottom: 14px;
            margin-bottom: 23px;
            font-size: 20px;
            color: #000;
            border-bottom: 1px solid #e2e2e2;
          }

          ul {
            margin: 0;
            padding: 0;

            li {
              list-style: none;

              span {
                width: 100%;
                height: 20px;
                line-height: 20px;
                display: block;
                color: #999;
                font-size: 13px;
              }

              div {
                line-height: 26px;
                padding-bottom: 15px;
                font-size: 16px;
                color: #4a4a4a;
              }
            }
          }
        }

        #watchNeed {
          width: 100%;
          height: 500px;
          padding: 60px 30px 0;
          //padding-bottom: 14px;
          //margin-bottom: 23px;
          //font-size: 20px;
          //color: #000;
          //border-bottom: 1px solid #e2e2e2;
          .proDetial {
            padding-bottom: 14px;
            margin-bottom: 23px;
            font-size: 20px;
            color: #000;
            border-bottom: 1px solid #e2e2e2;
          }

          ul {
            margin: 0;
            padding: 0;

            li {
              list-style: none;

              span {
                width: 100%;
                height: 20px;
                line-height: 20px;
                display: block;
                color: #999;
                font-size: 13px;
              }

              div {
                line-height: 26px;
                padding-bottom: 15px;
                font-size: 16px;
                color: #4a4a4a;
              }
            }
          }
        }
      }
    }

    .box-right {
      box-sizing: border-box;
      width: 320px;
      border-left: 1px solid #ebebeb;
      padding: 40px 18px 0;
      flex: 0 0 320px;

      .service {
        padding: 24px 15px;
        background: #fafafa;
        border: 1px solid #ebebeb;

        .sit {
          display: block;
          margin-bottom: 24px;
          height: 35px;
          line-height: 35px;
          font-size: 12px;
          text-align: center;
          color: #fff;
          cursor: pointer;
          background-color: #FF2D55;
          border-radius: 36px;
        }

        .service-note {
          margin-bottom: 18px;

          .service-item {
            margin-bottom: 16px;
            &:last-child {
              margin-bottom: 0;
            }

            .service-name {
              font-size: 14px;
              color: #333;
              margin-bottom: 6px;
              display: flex;
              align-items: center;
              font-weight: 500;

              i {
                margin-right: 8px;
                flex-shrink: 0;
              }
            }

            .service-desc {
              font-size: 12px;
              color: #999;
              line-height: 1.6;
              padding-left: 20px;
              text-align: justify;
            }
          }
        }
      }
      .box-like{
        margin-top: 24px;
        margin-bottom: 24px;
        font-size: 20px;
        color: #000;
        line-height: 28px;
      }
      .search__box{
        list-style: none;
        margin: 0;
        padding: 0;
        .search__item{
          width: 100%;
          height: 160px;
          margin-bottom: 30px;
          .link{
            width: 120px;
            height: 100%;
            display: inline-block;
            img{
              float: left;
              width: 120px;
              height: 100%;
            }
          }
          .search_item_info{
            width: 157px;
            float: right;
            height: 160px;
            .link__title{
              display: -webkit-box;
              -webkit-box-orient: vertical;
              -webkit-line-clamp: 2;
              line-clamp: 2;
              overflow: hidden;
              font-size: 14px;
              color: #4a4a4a;
              padding-left: 17px;
            }
            .search__item__info__venue{
              margin-top: 12px;
              color: #9b9b9b;
              padding-left: 20px;
              font-size: 12px;

            }
            .search__item__info__price{
              font-size: 16px;
              color: rgba(255, 55, 29, 0.85);
              margin-top: 39px;
              padding-left: 20px;
              font-weight: bold;
            }
          }
        }
      }
    }
  }


}

.active {
  border-color: rgba(255, 55, 29, 0.85);
  color: rgba(255, 55, 29, 0.85);
  background: #fff;
}

.activeCity {
  color: rgba(255, 55, 29, 0.85) !important;
  border: 1px solid rgba(255, 55, 29, 0.85) !important;
}

.ticket {
  color: rgba(255, 55, 29, 0.85) !important;
  border: 1px solid rgba(255, 55, 29, 0.85) !important;
}

.menuActive {
  //position: relative;
  font-size: 20px;
  color: #000;
  border-bottom: 2px solid rgba(255, 55, 29, 0.85);
}

.icon-no {
  display: inline-block;
  width: 12px;
  height: 12px;
  background-repeat: no-repeat;
  background-size: 12px 12px;
  background: url('@/assets/section/no.png')
}

.icon-yes {
  display: inline-block;
  width: 12px;
  height: 12px;
  background-repeat: no-repeat;
  background-size: 12px 12px;
  background: url('@/assets/section/yes.png')
}
</style>
<style scoped lang="scss">
.text-title{font-size:24px;line-height:32px;font-weight:700;color:#000}
.text-subtitle{font-size:17px;line-height:24px;font-weight:400;color:#4a4a4a}
.citys .text-subtitle{margin-right:12px}
.order-time .order-name{margin-right:12px;font-weight:400}
.order-price .num{margin-right:12px;font-weight:400}
.btn{display:inline-flex;align-items:center;justify-content:center;min-height:44px;padding:10px 16px;border-radius:8px;transition:all .2s ease}
.btn--primary{background-color:rgba(255,55,29,0.85);color:#fff;box-shadow:0 6px 16px rgba(0,0,0,0.06)}
.btn--primary:hover{filter:brightness(1.05)}
.btn--primary:active{transform:translateY(1px)}
.btn--secondary{background:#fafafa;border:1px solid #ebebeb;color:#4a4a4a}
.btn--secondary:hover{filter:brightness(1.03)}
.btn--link{background:transparent;color:#4a4a4a}
.btn--lg{min-width:160px}
.btn--block{width:100%}
.btn:focus{outline:2px solid rgba(255,55,29,0.35);outline-offset:2px}
.buy .buy-link-now{width:160px;height:44px;line-height:44px;font-size:14px;border-radius:8px}
.box-right .sit{height:44px;line-height:44px;font-size:14px;border-radius:8px;box-shadow:0 6px 16px rgba(0,0,0,0.06)}
@media (max-width: 992px){
  .app-container .wrapper{flex-direction: column}
  .box-right{width:100%;flex:0 0 auto;border-left:none;border-top:1px solid #ebebeb;padding:24px 0 0}
  .buy .buy-link-now{width:100%}
}
@media (max-width: 768px){
  .text-title{font-size:20px;line-height:28px}
  .text-subtitle{font-size:15px;line-height:22px}
  .order-time .order-name{font-size:15px;height:40px}
  .select-list .select-list-item{min-height:40px;padding:8px 14px}
}
</style>
watch(() => route.params.id, (newId) => {
  paramId.value = Number(newId)
  recommendParams.programId = paramId.value
  getProgramDetialsList()
})
