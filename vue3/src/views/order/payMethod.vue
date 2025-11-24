<template>
  <div class="app-container">
    <div class="pay-header">
      <div class="back" role="button" aria-label="返回上一页" tabindex="0" @click="goBack" @keydown.enter="goBack" @keydown.space.prevent="goBack" @touchstart.prevent="goBack"><el-icon><ArrowLeftBold /></el-icon></div>
      <div class="content"><img :src="pay" alt=""><span>支付宝付款</span></div>
    </div>
    <div class="pay-section">
      <div class="order-brief" v-if="orderDetailData">
        <div class="title">{{ orderDetailData.programTitle }}</div>
        <div class="price">￥{{ orderDetailData.orderPrice }}</div>
        <div class="countdown" v-if="remainText">剩余支付时间：{{ remainText }}</div>
      </div>
      <div class="seat-info" v-if="orderDetailData && orderDetailData.orderTicketInfoVoList && orderDetailData.orderTicketInfoVoList.length">
        <div class="label">座位明细</div>
        <ul class="seat-list">
          <li class="seat-item" v-for="(it,idx) in orderDetailData.orderTicketInfoVoList" :key="idx">{{ it.seatInfo }}（￥{{ it.price }}）×{{ it.quantity }}</li>
        </ul>
      </div>
      <el-button type="primary" class="payContinue" @click="continuePay">继续浏览器付款</el-button>
    </div>
  </div>
</template>

<script setup name="PayMethod">
import pay from "@/assets/section/pay.png"
import {ref,onMounted} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {getOrderDetailApi,orderPayApi} from "@/api/order.js";
//订单编号
const orderNumber = ref('')
//订单详情数据
const orderDetailData = ref('');
const remainText = ref('');
const route = useRoute();
const router = useRouter();
import {useMitt} from "@/utils/index";

const emitter = useMitt();


function continuePay() {
  //支付前，要调取订单详情
  if (orderDetailData.value == '' || orderDetailData.value == null) {
    getOrderDetail()
  }

  const orderPayParams = {
    'platform':3,
    'orderNumber':orderNumber.value,
    'subject':orderDetailData.value.programTitle,
    'price':orderDetailData.value.orderPrice,
    'channel':'alipay',
    'payBillType':1,
    'seatInfo': buildSeatInfoJson()
  }
  console.log('支付参数:', orderPayParams);
  orderPayApi(orderPayParams).then(response => {
    console.log('支付接口返回:', response);
    // 优先判断后端是否返回错误码或 message
    if (response.code && response.code !== '0') {
      ElMessage.error(response.message || '支付接口异常');
      return;
    }
    if (typeof response.data === 'string' && response.data.startsWith('<form')) {
      document.write(response.data);
    } else if (typeof response.data === 'string' && response.data.startsWith('http')) {
      window.location.href = response.data;
    } else {
      ElMessage.error('支付链接获取失败，请稍后重试');
    }
  }).catch(err => {
    console.error('支付接口异常:', err);
    ElMessage.error('支付接口异常，请稍后重试');
  });
}

//跳转后的接收值
onMounted(() => {
  getOrderDetail()
})
//订单详情方法
function getOrderDetail() {
  // 优先从history.state获取，其次从localStorage，再其次从route.query
  let tempOrderNumber = '';
  if (history.state && history.state.orderNumber) {
    tempOrderNumber = history.state.orderNumber;
  } else if (localStorage.getItem('orderNumber')) {
    tempOrderNumber = localStorage.getItem('orderNumber');
  } else if (route.query && route.query.orderNumber) {
    tempOrderNumber = route.query.orderNumber;
  }
  orderNumber.value = tempOrderNumber;
  const orderDetailParams = {'orderNumber': orderNumber.value}
  //传值-订单号
  localStorage.setItem('orderNumber',orderNumber.value)
  getOrderDetailApi(orderDetailParams).then(response => {
    orderDetailData.value = response.data;
    updateRemainText()
  })
}

function buildSeatInfoJson(){
  try{
    const list = (orderDetailData.value?.orderTicketInfoVoList || []).map(it => ({ seatInfo: it.seatInfo, price: it.price, quantity: it.quantity }))
    return JSON.stringify({ orderNumber: orderNumber.value, seats: list })
  }catch(e){
    return JSON.stringify({ orderNumber: orderNumber.value, seats: [] })
  }
}

function updateRemainText(){
  try{
    const create = new Date(orderDetailData.value.createOrderTime).getTime()
    const expire = create + 15*60*1000
    const tick = () => {
      const now = Date.now()
      const diff = Math.max(0, expire - now)
      const mm = String(Math.floor(diff/60000)).padStart(2,'0')
      const ss = String(Math.floor((diff%60000)/1000)).padStart(2,'0')
      remainText.value = mm+':'+ss
    }
    tick()
    setInterval(tick,1000)
  }catch(e){
    remainText.value = ''
  }
}

function goBack(){
  try{
    if (window.history.length > 1){
      router.back()
    } else {
      router.push('/index')
    }
  }catch(e){
    console.error('返回上一页失败:', e)
  }
}

</script>

<style scoped lang="scss">
.app-container {
  .pay-header {
    display: flex;
    -webkit-box-pack: justify;
    -webkit-justify-content: space-between;
    justify-content: space-between;
    -webkit-box-align: center;
    -webkit-align-items: center;
    flex-direction: row;
    align-items: center;
    height: 100%;
    padding: 0 55px;
    background-color: #fff;

    .back {
      width: 40px;
      cursor: pointer;
      user-select: none;
      transition: transform .15s ease, opacity .15s ease;
      &:hover{ opacity: .85 }
      &:active{ transform: translateY(1px) }
      .el-icon{
        font-size: 40px;
      }
    }

    .content {
      width: calc(100% - 40px);
      text-align: center;
      position: relative;
      img {
        width: 60px;
        height: 60px;
        position: absolute;
        top: 4px;
        left: 40%;
      }

      span {
        font-size: 40px;
        font-weight: 700;
        color: #333;
        height: 70px;
        display: inline-block;
        line-height: 70px;
        width: 200px;
        margin-left: 20px;
      }
    }
  }
.pay-section{
  .payContinue{
    width: 95%;
    height: 123px;
    margin-top: 300px;
    font-size: 60px;
    margin-left: 40px;
  }
  .order-brief{ padding: 24px 40px; display:flex; align-items:center; gap:24px; }
  .order-brief .title{ font-size: 20px; font-weight: 600; }
  .order-brief .price{ font-size: 18px; color:#333; }
  .order-brief .countdown{ font-size: 16px; color:#ff4d4f; }
  .seat-info{ padding: 0 40px; }
  .seat-info .label{ font-size: 16px; font-weight: 600; margin-bottom: 8px; }
  .seat-list{ list-style:none; padding:0; margin:0; }
  .seat-item{ padding: 4px 0; font-size: 14px; color:#333; }
}
}

</style>
