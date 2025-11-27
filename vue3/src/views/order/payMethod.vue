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
        <div class="selected-list" v-if="resolvedSeats && resolvedSeats.length">
          <span class="sel-tag" v-for="(s,i) in resolvedSeats" :key="i">{{ s.name }} <span class="price-tag">￥{{ s.price }}</span></span>
        </div>
      </div>
      <div class="seat-info" v-else-if="ticketCount > 0">
        <div class="label">座位明细</div>
        <ul class="seat-list">
          <li class="seat-item">票档（￥{{ unitPrice }}）×{{ ticketCount }}</li>
        </ul>
      </div>
      <el-button type="primary" class="payContinue" :disabled="isPaying" @click="continuePay">继续浏览器付款</el-button>
    </div>
  </div>
</template>

<script setup name="PayMethod">
import pay from "@/assets/section/pay.png"
import {ref,onMounted,computed} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {getOrderDetailApi,orderPayApi} from "@/api/order.js";
import { ElMessage } from 'element-plus'
import { ArrowLeftBold } from '@element-plus/icons-vue'
//订单编号
const orderNumber = ref('')
//订单详情数据
const orderDetailData = ref('');
const remainText = ref('');
const route = useRoute();
const router = useRouter();
import {useMitt} from "@/utils/index";

const emitter = useMitt();

const isPaying = ref(false);


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
    const body = response.data;
    if (typeof body === 'string') {
      const html = String(body);
      if (html.includes('<form') || html.includes('<html')) {
        if (!submitAlipayHtml(html)) {
          submitAlipayForm(html);
        }
        return;
      }
      if (/^https?:\/\//i.test(html)) {
        if (/excashier-sandbox\.dl\.alipaydev\.com\/standard\/auth\.htm/i.test(html)) {
          window.location.replace(html);
          return;
        }
        postAlipayUrl(html);
        return;
      }
    }
    ElMessage.error('支付链接获取失败，请稍后重试');
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

function submitAlipayForm(html){
  try{
    const parser = new DOMParser();
    const doc = parser.parseFromString(html, 'text/html');
    const originForm = doc.querySelector('form');
    if(!originForm){
      ElMessage.error('支付表单解析失败');
      return;
    }
    const method = (originForm.getAttribute('method') || 'post').toLowerCase();
    const action = originForm.getAttribute('action') || '';
    const form = document.createElement('form');
    form.method = method === 'get' ? 'get' : 'post';
    form.action = action;
    const inputs = originForm.querySelectorAll('input[name]');
    inputs.forEach((inp) => {
      const input = document.createElement('input');
      input.type = 'hidden';
      input.name = inp.getAttribute('name') || '';
      input.value = inp.getAttribute('value') || '';
      form.appendChild(input);
    });
    document.body.appendChild(form);
    form.submit();
  }catch(err){
    ElMessage.error('支付表单提交失败');
  }
}

function submitAlipayHtml(html){
  try{
    const w = window.open('', '_self');
    w.document.open('text/html', 'replace');
    w.document.write(html);
    w.document.close();
    return true;
  }catch(err){
    try{
      document.open('text/html', 'replace');
      document.write(html);
      document.close();
      return true;
    }catch(e){
      return false;
    }
  }
}

function postAlipayUrl(payUrl){
  try{
    const u = new URL(payUrl);
    const action = u.origin + u.pathname;
    const raw = u.search.startsWith('?') ? u.search.slice(1) : u.search;
    const form = document.createElement('form');
    form.method = 'post';
    form.action = action;
    form.acceptCharset = 'UTF-8';
    form.enctype = 'application/x-www-form-urlencoded';
  raw.split('&').forEach(pair => {
      if (!pair) return;
      const eq = pair.indexOf('=');
      const k = eq >= 0 ? pair.slice(0, eq) : pair;
      const vRaw = eq >= 0 ? pair.slice(eq + 1) : '';
      const input = document.createElement('input');
      input.type = 'hidden';
      input.name = decodeURIComponent(k || '');
      const needsSpace = input.name !== 'sign';
      const norm = needsSpace ? vRaw.replace(/\+/g, '%20') : vRaw;
      input.value = decodeURIComponent(norm || '');
      form.appendChild(input);
    });
    document.body.appendChild(form);
    form.submit();
  }catch(e){
    window.location.href = payUrl;
  }
}

const resolvedSeats = computed(() => {
  const result = []
  const arr = orderDetailData.value?.orderTicketInfoVoList || []
  arr.forEach(it => {
    const names = String(it.seatInfo || '').split(',').map(s => s.trim()).filter(Boolean)
    names.forEach(n => result.push({ name: n, price: Number(it.price || 0) }))
  })
  return result
})

const ticketCount = computed(() => {
  const arr = orderDetailData.value?.userAndTicketUserInfoVo?.ticketUserInfoVoList || []
  return Array.isArray(arr) ? arr.length : 0
})

const unitPrice = computed(() => {
  const price = Number(orderDetailData.value?.orderPrice || 0)
  const c = Number(ticketCount.value || 0)
  if (c <= 0) return 0
  return Math.round((price / c) * 100) / 100
})

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
        left: 38%;
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
  max-width: 880px;
  margin: 24px auto;
  padding: 24px 24px 16px;
  background: #fff;
  border: 1px solid #eee;
  border-radius: 12px;
  box-shadow: 0 6px 18px rgba(0,0,0,0.04);

  .order-brief{ display:flex; align-items:center; justify-content:space-between; gap:16px; padding-bottom: 16px; border-bottom: 1px dashed #e6e6e6; }
  .order-brief .title{ font-size: 20px; font-weight: 700; color:#333; flex:1; }
  .order-brief .price{ font-size: 20px; font-weight: 700; color: rgba(255, 55, 29, 0.85); }
  .order-brief .countdown{ font-size: 14px; color:#ff4d4f; background: #fff1f0; border: 1px solid #ffccc7; border-radius: 999px; padding: 6px 10px; }

  .seat-info{ padding-top: 16px; }
  .seat-info .label{ font-size: 16px; font-weight: 600; margin-bottom: 8px; color:#333; }
  .seat-list{ list-style:none; padding:0; margin:0; }
  .seat-item{ padding: 6px 0; font-size: 14px; color:#333; }

  .selected-list{ margin-top: 8px; display:flex; flex-wrap:wrap; gap:8px; }
  .sel-tag{ display:inline-flex; align-items:center; gap:6px; background:#f7f8fa; border:1px solid #e6e6e6; border-radius:6px; padding:6px 10px; color:#333; }
  .price-tag{ color: rgba(255, 55, 29, 0.85); font-weight:700; }

  .payContinue{
    width: 100%;
    height: 48px;
    margin-top: 16px;
    font-size: 18px;
    margin-left: 0;
    border-radius: 8px;
  }
}
}

</style>
