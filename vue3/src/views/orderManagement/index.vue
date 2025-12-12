<template>
  <!--订单管理-->
  <Header></Header>
  <div class="red-line"></div>
  <div class="section">
    <MenuSideBar class="sidebarMenu" activeIndex="5"></MenuSideBar>
    <div class="right-section">
      <div class="list-header">
        <div class="col item">项目名称</div>
        <div class="col count">票品张数</div>
        <div class="col price">订单金额</div>
        <div class="col status">交易状态</div>
        <div class="col action">交易操作</div>
      </div>
      
      <div class="orderBox" v-for="(order, index) in orderList" :key="index">
        <div class="num">
          <span class="label">订单号:</span> 
          <span class="val">{{ order.orderNumber }}</span>
        </div>
        <div class="order-content">
          <div class="col item">
            <img :src="order.programItemPicture" alt="" class="poster">
            <div class="project">
              <div class="title" :title="order.programTitle">{{ order.programTitle }}</div>
              <div class="info-row"><el-icon><Clock /></el-icon> {{ order.programShowTime }}</div>
              <div class="info-row"><el-icon><Location /></el-icon> {{ order.programPlace }}</div>
            </div>
          </div>
          <div class="col count">{{ order.ticketCount }}</div>
          <div class="col price">
            <div class="amount">￥{{ order.orderPrice }}</div>
            <div class="shipping">(含运费￥0.00)</div>
          </div>
          <div class="col status">
            <div class="status-badge" :class="statusClassName(order.orderStatus)">{{ getOrderStatus(order.orderStatus) }}</div>
            <div class="countdown" v-if="order.orderStatus == 1 && remainText(order.orderNumber)">
               剩余 {{ remainText(order.orderNumber) }}
            </div>
          </div>
          <div class="col action">
              <template v-if="order.orderStatus == 1 && remainText(order.orderNumber)">
                <el-button type="primary" class="action-btn" @click="goPay(order.orderNumber)">立即支付</el-button>
                <el-button link type="info" class="cancel-btn" @click="openCancelDialog(order.orderNumber)">取消订单</el-button>
              </template>
              <template v-else>
                <el-button link type="primary" class="detail-btn" @click="router.push({name:'orderDetail', params:{orderNumber:order.orderNumber}})">
                  订单详情
                </el-button>
              </template>
          </div>
        </div>
      </div>

      <el-dialog
          v-model="cancelDialogVisible"
          title="提示"
          width="420px"
          class="dm-cancel-dialog"
          center
          :show-close="false"
      >
        <div class="dialog-content">
          <el-icon class="warn-icon"><WarningFilled /></el-icon>
          <div class="warn-text">确定要取消此订单吗？</div>
          <div class="sub-text">取消后将无法恢复，需要重新下单。</div>
        </div>
        <template #footer>
          <div class="dialog-footer">
            <el-button class="dialog-btn cancel" @click="closeCancelDialog">暂不取消</el-button>
            <el-button class="dialog-btn confirm" type="primary" @click="confirmCancel">确认取消</el-button>
          </div>
        </template>
      </el-dialog>

    </div>
  </div>
  <Footer class="foot"></Footer>


</template>

<script setup name="OrderManagement">
import {ref, onMounted, getCurrentInstance, nextTick, reactive, watch, onBeforeUnmount} from 'vue'
import MenuSideBar from '../../components/menuSidebar/index'
import Header from '../../components/header/index'
import Footer from '../../components/footer/index'
import {useRoute, useRouter} from 'vue-router'
import {cancelOrderApi, getOrderListApi, payCheckApi} from '@/api/order.js'
import {ElMessage} from "element-plus";
//获取用户信息
import useUserStore from "../../store/modules/user";
import { Clock, Location, WarningFilled } from '@element-plus/icons-vue'

//订单列表数据
const orderList = ref([])
const useUser = useUserStore()
//订单列表入参
const orderListParams = reactive({
  userId: useUser.userId
})

//订单列表方法
const getOrderList = () => {
  getOrderListApi(orderListParams).then(response => {
    orderList.value = response.data;
    subscribePendingOrders()
    // 立即检查一次待支付订单的最新状态
    checkPendingOrdersOnce()
  })
}

function getOrderStatus(orderStatus) {
  if (orderStatus == 1) {
    return '未支付';
  }
  if (orderStatus == 2) {
    return '交易关闭';
  }
  if (orderStatus == 3) {
    return '已支付';
  }
  if (orderStatus == 4) {
    return '已取消';
  }
}

const cancelDialogVisible = ref(false)
const targetOrderNumber = ref('')
const targetOrderIndex = ref(-1)

function openCancelDialog(orderNumber) {
  targetOrderNumber.value = orderNumber
  targetOrderIndex.value = orderList.value?.findIndex?.(o => o.orderNumber === orderNumber) ?? -1
  cancelDialogVisible.value = true
}

function confirmCancel() {
  const orderNumberParams = {orderNumber: targetOrderNumber.value}
  cancelOrderApi(orderNumberParams).then(response => {
    if (response.code == '0') {
      ElMessage({message: '取消成功', type: 'success'})
      if (targetOrderIndex.value > -1) {
        orderList.value[targetOrderIndex.value].orderStatus = 4
      }
      cancelDialogVisible.value = false
    } else {
      ElMessage({message: response.message, type: 'error'})
    }
  })
}

function closeCancelDialog() {
  cancelDialogVisible.value = false
}

const router = useRouter();

function goPay(orderNumber) {
  // 跳转到支付页面，传递订单编号
  router.push({path: '/order/payMethod', state: {orderNumber}})
}

onMounted(() => {
  getOrderList()
})

const remainMap = reactive({})
const expireMap = reactive({})
let countdownTimer = null
let pollTimer = null
let quickPollTimer = null
const canceledSet = new Set()
const sseMap = {}

function parseCreateTime(order){
  const t = order.createOrderTime ?? order.create_order_time ?? order.createTime ?? order.create_time
  if (!t) return null
  if (typeof t === 'number') return t
  const ts = Date.parse(t)
  return isNaN(ts) ? null : ts
}

function initCountdown(){
  Object.keys(remainMap).forEach(k => delete remainMap[k])
  Object.keys(expireMap).forEach(k => delete expireMap[k])
  orderList.value?.forEach?.(o => {
    if (o.orderStatus == 1){
      const ct = parseCreateTime(o)
      if (ct){
        expireMap[o.orderNumber] = ct + 15*60*1000
      }
    }
  })
  if (countdownTimer) { clearInterval(countdownTimer); countdownTimer = null }
  if (Object.keys(expireMap).length){
    const tick = () => {
      const now = Date.now()
      for (const k in expireMap){
        const diffRaw = expireMap[k] - now
        if (diffRaw <= 0){
          remainMap[k] = ''
          delete expireMap[k]
          
          // 倒计时结束，立即更新本地状态为交易关闭
          const idx = orderList.value?.findIndex?.(o => o.orderNumber === k) ?? -1
          if (idx > -1){
            orderList.value[idx].orderStatus = 2
          }

          if (!canceledSet.has(k)){
            canceledSet.add(k)
            cancelOrderApi({orderNumber: k}).catch(() => {})
          }
        } else {
          const diff = diffRaw
          const mm = String(Math.floor(diff/60000)).padStart(2,'0')
          const ss = String(Math.floor((diff%60000)/1000)).padStart(2,'0')
          remainMap[k] = mm + ':' + ss
        }
      }
    }
    tick()
    countdownTimer = setInterval(tick, 1000)
  }
}

function remainText(orderNumber){
  return remainMap[orderNumber] || ''
}

watch(orderList, () => {
  initCountdown()
})

onBeforeUnmount(() => {
  if (countdownTimer) { clearInterval(countdownTimer) }
  if (pollTimer) { clearInterval(pollTimer) }
  if (quickPollTimer) { clearInterval(quickPollTimer) }
  document.removeEventListener('visibilitychange', handleVisibilityChange)
  window.removeEventListener('focus', handleFocus)
  Object.values(sseMap).forEach(es => { try { es.close() } catch(e){} })
})

function statusClassName(code){
  if (code == 1) return 'pending'
  if (code == 3) return 'paid'
  if (code == 2) return 'closed'
  if (code == 4) return 'canceled'
  return ''
}

function handleVisibilityChange(){
  if (!document.hidden){
    getOrderList()
  }
}

function handleFocus(){
  getOrderList()
}

function startPolling(){
  if (pollTimer) { clearInterval(pollTimer) }
  pollTimer = setInterval(() => {
    if (!document.hidden){
      getOrderList()
    }
  }, 30000)
}

function checkPendingOrdersOnce(){
  const arr = Array.isArray(orderList.value) ? orderList.value : []
  const nums = arr.filter(o => o && o.orderStatus == 1).map(o => o.orderNumber)
  if (!nums.length) return
  const numSet = new Set(nums)
  nums.forEach(num => {
    const params = { orderNumber: Number(num), payChannelType: 1 }
    payCheckApi(params).then(res => {
      if (res && String(res.code) === '0' && res.data) {
        const st = Number(res.data.orderStatus || 0)
        const idx = orderList.value?.findIndex?.(o => o.orderNumber === num) ?? -1
        if (idx > -1 && st && st !== orderList.value[idx].orderStatus) {
          orderList.value[idx].orderStatus = st
        }
      }
    }).catch(() => {})
  })
}

function startQuickPolling(){
  if (quickPollTimer) { clearInterval(quickPollTimer) }
  quickPollTimer = setInterval(() => {
    if (!document.hidden){
      checkPendingOrdersOnce()
    }
  }, 2000)
}

function subscribePendingOrders(){
  const arr = Array.isArray(orderList.value) ? orderList.value : []
  arr.forEach(o => {
    const num = o.orderNumber
    if (o.orderStatus == 1 && num && !sseMap[num]){
      const url = `/damai/order/order/status/subscribe?orderNumber=${num}`
      const es = new EventSource(url)
      es.onmessage = (e) => {
        try {
          const data = JSON.parse(e.data)
          const idx = orderList.value?.findIndex?.(x => x.orderNumber === data.orderNumber) ?? -1
          if (idx > -1 && data.orderStatus){
            orderList.value[idx].orderStatus = Number(data.orderStatus)
            if (Number(data.orderStatus) !== 1){
              es.close(); delete sseMap[num]
            }
          }
        }catch(err){}
      }
      es.addEventListener('orderStatus', (e) => {
        try {
          const data = JSON.parse(e.data)
          const idx = orderList.value?.findIndex?.(x => x.orderNumber === data.orderNumber) ?? -1
          if (idx > -1 && data.orderStatus){
            orderList.value[idx].orderStatus = Number(data.orderStatus)
            if (Number(data.orderStatus) !== 1){
              es.close(); delete sseMap[num]
            }
          }
        }catch(err){}
      })
      es.onerror = () => { try { es.close() } catch(e){}; delete sseMap[num] }
      sseMap[num] = es
    }
  })
}

onMounted(() => {
  document.addEventListener('visibilitychange', handleVisibilityChange)
  window.addEventListener('focus', handleFocus)
  startPolling()
  startQuickPolling()
})
</script>


<style scoped lang="scss">
.red-line {
  border-bottom: 5px solid rgba(255, 55, 29, 0.85);
}

.section {
  width: 1200px;
  margin: 15px auto 0;
  display: flex;
  gap: 20px;
  align-items: flex-start;

  .sidebarMenu {
    width: 200px;
    flex-shrink: 0;
  }

  .right-section {
    flex-grow: 1;
    min-height: 600px;
    
    // Header
    .list-header {
      display: flex;
      background: #f5f7fa;
      padding: 12px 20px;
      border: 1px solid #ebeef5;
      border-radius: 4px 4px 0 0;
      font-size: 14px;
      color: #606266;
      font-weight: 600;

      .col {
        &.item { flex: 1; padding-right: 20px;}
        &.count { width: 100px; text-align: center; }
        &.price { width: 140px; text-align: center; }
        &.status { width: 140px; text-align: center; }
        &.action { width: 160px; text-align: center; }
      }
    }

    // Order Item
    .orderBox {
      margin-top: 15px;
      border: 1px solid #ebeef5;
      border-radius: 4px;
      transition: all 0.3s;
      background: #fff;

      &:hover {
        box-shadow: 0 4px 16px rgba(0,0,0,0.08);
        border-color: #e4e7ed;
      }

      .num {
        padding: 10px 20px;
        background: #fbfbfb;
        border-bottom: 1px solid #ebeef5;
        font-size: 12px;
        color: #909399;
        display: flex;
        gap: 10px;
        
        .val { color: #303133; font-weight: 500;}
      }

      .order-content {
        display: flex;
        padding: 20px;
        align-items: center;

        .col {
          &.item {
            flex: 1;
            display: flex;
            gap: 15px;
            padding-right: 20px;
            
            .poster {
              width: 80px;
              height: 108px;
              object-fit: cover;
              border-radius: 4px;
              border: 1px solid #f2f2f2;
            }

            .project {
              flex: 1;
              display: flex;
              flex-direction: column;
              gap: 8px;
              justify-content: flex-start;
              overflow: hidden;

              .title {
                font-size: 16px;
                color: #303133;
                font-weight: 500;
                line-height: 1.4;
                display: -webkit-box;
                -webkit-line-clamp: 2;
                -webkit-box-orient: vertical;
                overflow: hidden;
                margin-bottom: 4px;
              }

              .info-row {
                font-size: 13px;
                color: #909399;
                display: flex;
                align-items: center;
                gap: 5px;
                line-height: 1.2;
              }
            }
          }

          &.count {
            width: 100px;
            text-align: center;
            font-size: 14px;
            color: #606266;
          }

          &.price {
            width: 140px;
            text-align: center;
            
            .amount {
              font-size: 16px;
              color: #FF2D55;
              font-weight: 600;
            }
            
            .shipping {
              font-size: 12px;
              color: #909399;
              margin-top: 4px;
            }
          }

          &.status {
            width: 140px;
            text-align: center;
            display: flex;
            flex-direction: column;
            align-items: center;
            gap: 8px;

            .status-badge {
               display: inline-flex;
               align-items: center;
               justify-content: center;
               padding: 6px 14px;
               border-radius: 14px;
               font-size: 13px;
               font-weight: 500;
               background: #f4f4f5;
               color: #909399;
               
               &.pending {
                 color: #FF2D55;
                 background: #FFF5F7;
               }
               &.paid {
                 color: #67c23a;
                 background: #f0f9eb;
               }
               &.closed {
                 color: #909399;
                 background: #f4f4f5;
               }
               &.canceled {
                 color: #909399;
                 background: #f4f4f5;
                 text-decoration: line-through;
               }
            }

            .countdown {
              font-size: 12px;
              color: #FF2D55;
            }
          }

          &.action {
            width: 160px;
            text-align: center;
            display: flex;
            flex-direction: column;
            gap: 8px;
            align-items: center;
            justify-content: center;

            .detail-btn {
              font-size: 14px;
              padding: 6px 12px;
            }
            
            .action-btn {
              width: 96px;
              height: 32px;
              border-radius: 16px;
              font-size: 13px;
              font-weight: 500;
              background: linear-gradient(90deg, #FF4B8B 0%, #FF2D55 100%);
              border: none;
              color: #fff;
              transition: all 0.3s;
              
              &:hover {
                background: linear-gradient(90deg, #ff6a9e 0%, #ff4f71 100%);
                box-shadow: 0 4px 12px rgba(255, 45, 85, 0.3);
                transform: translateY(-1px);
              }
            }

            .cancel-btn {
              font-size: 13px;
              color: #909399;
              margin-left: 0 !important;
              height: auto;
              padding: 4px 0;
              
              &:hover {
                color: #606266;
                background-color: transparent;
              }
            }
          }
        }
      }
    }
  }
}

// Dialog Customization
:deep(.dm-cancel-dialog) {
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  
  .el-dialog__header {
    padding: 24px 0 0;
    margin: 0;
    .el-dialog__title {
      font-weight: 600;
      color: #303133;
      font-size: 20px;
    }
  }
  
  .el-dialog__body {
    padding: 24px 30px 30px;
    
    .dialog-content {
      display: flex;
      flex-direction: column;
      align-items: center;
      text-align: center;
      
      .warn-icon {
        font-size: 56px;
        color: #ff9900;
        margin-bottom: 20px;
      }
      
      .warn-text {
        font-size: 18px;
        color: #303133;
        font-weight: 500;
        margin-bottom: 10px;
      }
      
      .sub-text {
        font-size: 14px;
        color: #909399;
        line-height: 1.6;
      }
    }
  }
  
  .el-dialog__footer {
    padding: 0 30px 30px;
    background: #fff;
    border: none;
  }
}

.dialog-footer {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  
  .dialog-btn {
    flex: 1;
    height: 42px;
    border-radius: 21px;
    font-size: 15px;
    margin: 0;
    
    &.cancel {
      background: #f5f7fa;
      border: none;
      color: #606266;
      &:hover {
        background: #e4e7ed;
        color: #303133;
      }
    }
    
    &.confirm {
      background: linear-gradient(90deg, #FF4B8B 0%, #FF2D55 100%);
      border: none;
      &:hover {
        background: linear-gradient(90deg, #ff6a9e 0%, #ff4f71 100%);
        box-shadow: 0 4px 12px rgba(255, 45, 85, 0.2);
        transform: translateY(-1px);
      }
    }
  }
}
</style>
