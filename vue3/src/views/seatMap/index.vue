<template>
  <div class="app-container">
    <Header />
    <div class="wrapper">
      <div class="seat-header">
        <div class="title">座位选择</div>
        <div class="hint">请选择座位后，点击“选好，去下单”进入确认订单</div>
      </div>
      
      <!-- Legend -->
      <div class="legend">
        <div class="legend-item" v-for="(p,idx) in priceList" :key="p">
          <span class="dot" :class="'price-'+idx"></span>
          <span class="label">￥{{ p }}</span>
        </div>
        <div class="legend-item">
          <span class="dot sold"></span>
          <span class="label">已售</span>
        </div>
        <div class="legend-item">
          <span class="dot picked"></span>
          <span class="label">已选</span>
        </div>
      </div>
      
      <!-- Main Map Area -->
      <div class="seat-map-container" ref="mapContainer">
        <!-- Stage -->
        <div class="stage-area">
          <div class="stage-shape">
            <div class="stage-text">舞台 STAGE</div>
          </div>
          <div class="control-center">控台</div>
        </div>
        
        <!-- Seats Layer -->
        <div class="seats-layer">
          <!-- Render Zones for visual grouping (Optional, mostly for debugging or labels) -->
          <div v-for="z in zones" :key="z.id" class="zone-label" :style="zoneLabelStyle(z)">
            {{ z.name }}
          </div>

          <!-- Render Individual Seats -->
          <div
            v-for="s in displaySeats"
            :key="s.id"
            class="seat-item"
            :class="seatClass(s)"
            :style="seatStyle(s)"
            @click="toggleSeat(s)"
            :title="`${s.zoneName} ${s.rowCode}排${s.colCode}座 ￥${s.price}`"
          >
          </div>
        </div>
      </div>

      <!-- Footer Actions -->
      <div class="actions">
        <div class="selected-info">
          <div class="selected-count">已选 {{ selectedSeats.length }} 个座位</div>
          <div class="selected-list" v-if="selectedSeats.length">
            <span v-for="s in selectedSeats" :key="s.id" class="sel-tag">
              {{ s.zoneName }} {{ s.rowCode }}排{{ s.colCode }}座
              <span class="price-tag">￥{{ s.price }}</span>
              <i class="close-btn" @click.stop="toggleSeat(s)">×</i>
            </span>
          </div>
        </div>
        <el-button type="primary" size="large" class="go-btn" :disabled="selectedSeats.length===0" @click="goOrder">
          选好，去下单
        </el-button>
      </div>
    </div>
    <Footer />
  </div>
</template>

<script setup>
import Header from '@/components/header/index'
import Footer from '@/components/footer/index'
import {useRoute, useRouter} from 'vue-router'
import {ref, computed, onMounted, onBeforeUnmount} from 'vue'
import {getSeatRelateInfo} from '@/api/seat'
import {getSoldSeatListApi} from '@/api/order'
import {getProgramDetials} from '@/api/contentDetail'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const programId = Number(route.params.id)

const relate = ref({ priceList: [], seatVoMap: {} })
const detail = ref(null)
const selectedSeats = ref([])
const seatTicketIdMap = ref({})
const realStatusMap = ref({})
const soldStatusMap = ref({})
let soldPollingTimer = null
let seatSse = null

// Configuration
const TOTAL_SEATS_TARGET = 500
const priceList = computed(() => [2008, 1688, 1288, 888, 288])

// Zone Definitions (Based on Reference Image)
// Coordinates: Center X = 450, Stage Y = 50
// Zone Definitions (Optimized for spacing)
// Coordinates: Center X = 450, Stage Y = 50
const zones = [
  // Inner Field (Red/Orange) - Center
  // Compacted slightly to make room
  { id: 'inner1', name: '2008票档', type: 'rect', x: 455, y: 220, rows: 7, cols: 12, priceIdx: 0, gap: 4, rowOffsetY: { 7: -24 } },
 { id: 'inner3', name: '内场3区', type: 'rect', x: 390, y: 404, rows: 6, cols: 6, priceIdx: 2, gap: 4 },
  { id: 'inner4', name: '内场4区', type: 'rect', x: 510, y: 404, rows: 6, cols: 6, priceIdx: 2, gap: 4 },
  
  // Side Stands (Purple) - Pushed out further
  { id: 'standL1', name: 'A区', type: 'rect', x: 180, y: 300, rows: 14, cols: 4, priceIdx: 1, rotate: -15, gap: 6 },
  { id: 'standR1', name: 'C区', type: 'rect', x: 720, y: 300, rows: 14, cols: 4, priceIdx: 1, rotate: 15, gap: 6 },
  
  // Back Corners (Green) - Adjusted for better curve
  { id: 'standL2', name: 'B区', type: 'rect', x: 220, y: 580, rows: 5, cols: 8, priceIdx: 3, rotate: -25, gap: 6 },
  { id: 'standR2', name: 'D区', type: 'rect', x: 680, y: 580, rows: 5, cols: 8, priceIdx: 3, rotate: 25, gap: 6 },
  
  // Back Center (Pink) - Lowered and centered
  { id: 'standBack', name: 'E区', type: 'rect', x: 460, y: 650, rows: 4, cols: 10, priceIdx: 4, rotate: 0, gap: 6 },
]

const displaySeats = computed(() => {
  const realMap = relate.value.seatVoMap || {}
  const soldDeps = Object.keys(soldStatusMap.value).length
  const list = Object.values(realMap).flat().map(s => ({...s}))
  buildRealStatusIndex(list)
  return generateLayoutSeats(list)
})

function generateLayoutSeats(realSeats) {
  let result = []
  let realSeatIdx = 0
  
  zones.forEach(zone => {
    const startX = zone.x - (zone.cols * (20 + zone.gap)) / 2
    const startY = zone.y - (zone.rows * (20 + zone.gap)) / 2
    
    for (let r = 1; r <= zone.rows; r++) {
      if (zone.priceIdx === 0 && r === 6) {
        continue
      }
      if (zone.priceIdx === 1 && r === 1) {
        continue
      }
      if (zone.priceIdx === 2 && r === 1) {
        continue
      }
      for (let c = 1; c <= zone.cols; c++) {
        // Calculate local position
        const lx = startX + (c - 1) * (20 + zone.gap)
        const ly = startY + (r - 1) * (20 + zone.gap)
        const lyAdj = ly + (zone.rowOffsetY && typeof zone.rowOffsetY[r] === 'number' ? zone.rowOffsetY[r] : 0)
        
        // Rotate around zone center
        const rad = (zone.rotate || 0) * Math.PI / 180
        const cx = zone.x
        const cy = zone.y
        
        const rx = cx + (lx - cx) * Math.cos(rad) - (lyAdj - cy) * Math.sin(rad)
        const ry = cy + (lx - cx) * Math.sin(rad) + (lyAdj - cy) * Math.cos(rad)
        
        // Create seat object
        const seatId = 100000 + result.length
        const price = priceList.value[zone.priceIdx] || 100
        
        const realTicketId = resolveTicketCategoryId(price)
        // 获取真实状态，如果为undefined则默认为1（可售）
        let sellStatus = (realTicketId ? getRealSellStatus(realTicketId, zone.name, r, c) : 1) || 1
        
        // 模拟真实场景：随机将1/3的座位设为已售
        // 使用确定性算法避免重绘时闪烁：基于坐标的简单哈希
        if (sellStatus === 1) {
          const zoneHash = zone.name.split('').reduce((acc, char) => acc + char.charCodeAt(0), 0)
          // 约33%的概率
          if ((r * 17 + c * 31 + zoneHash) % 100 < 33) {
            sellStatus = 3
          }
        }

        result.push({
          id: seatId,
          programId,
          ticketCategoryId: realTicketId,
          priceIdx: zone.priceIdx,
          rowCode: r,
          colCode: c,
          zoneName: zone.name,
          seatType: 1,
          price: price,
          sellStatus: sellStatus || 1,
          styleX: rx,
          styleY: ry,
          styleRotate: zone.rotate || 0
        })
      }
    }
  })
  
  return result
}

function buildRealStatusIndex(realSeats){
  const idx = {}
  ;(realSeats || []).forEach(s => {
    const k = `${String(s.ticketCategoryId)}|${s.zoneName || ''}|${Number(s.rowCode)}|${Number(s.colCode)}`
    const st = Number(s.sellStatus || 1)
    if (!idx[k] || st === 3 || st === 2) idx[k] = st
  })
  realStatusMap.value = Object.assign({}, idx, soldStatusMap.value)
}

function getRealSellStatus(ticketCategoryId, zoneName, rowCode, colCode){
  const k = `${String(ticketCategoryId)}|${zoneName || ''}|${Number(rowCode)}|${Number(colCode)}`
  return realStatusMap.value[k]
}

function buildSoldStatusIndex(list){
  const idx = {}
  ;(list || []).forEach(it => {
    if (it && it.ticketCategoryId != null && it.rowCode != null && it.colCode != null){
      const k = `${String(it.ticketCategoryId)}|${it.zoneName || ''}|${Number(it.rowCode)}|${Number(it.colCode)}`
      idx[k] = 3
    }
  })
  soldStatusMap.value = idx
}

function resolveTicketCategoryId(price) {
  // Hardcode for programId 2
  if (programId === 2) {
    if (Number(price) === 2008) return 201
    if (Number(price) === 1688) return 202
    if (Number(price) === 1288) return 203
    if (Number(price) === 888) return 204
    if (Number(price) === 288) return 205
  }

  const map = seatTicketIdMap.value || {}
  if (map && map[price]) return map[price]
  const cats = Array.isArray(detail.value?.ticketCategoryVoList) ? detail.value.ticketCategoryVoList : []
  const m = cats.find(x => Number(x.price) === Number(price))
  if (m) return m.id
  if (cats.length) return cats[0].id
  return undefined
}

function seatStyle(s) {
  return {
    left: s.styleX + 'px',
    top: s.styleY + 'px',
    transform: `rotate(${s.styleRotate}deg)`
  }
}

function zoneLabelStyle(z) {
  return {
    left: z.x + 'px',
    top: z.y + 'px',
    transform: `translate(-50%, -50%) rotate(${z.rotate || 0}deg)`
  }
}

function seatClass(s) {
  if (Number(s.sellStatus) === 2) return 'lock'
  if (Number(s.sellStatus) === 3) return 'sold'
  
  const isSelected = selectedSeats.value.find(x => x.id === s.id)
  if (isSelected) return 'picked'
  
  const pIdx = typeof s.priceIdx === 'number' ? s.priceIdx : priceList.value.indexOf(s.price)
  return `available price-${pIdx >= 0 ? pIdx : 0}`
}

function toggleSeat(s) {
  if (Number(s.sellStatus) !== 1) return
  
  const idx = selectedSeats.value.findIndex(x => x.id === s.id)
  if (idx >= 0) {
    selectedSeats.value.splice(idx, 1)
  } else {
    if (selectedSeats.value.length >= 6) {
      ElMessage.warning('最多只能选择6个座位')
      return
    }
    selectedSeats.value.push(s)
  }
}

function goOrder() {
  const hasMap = Object.keys(seatTicketIdMap.value || {}).length > 0
  const cats = Array.isArray(detail.value?.ticketCategoryVoList) ? detail.value.ticketCategoryVoList : []
  if (!hasMap && !cats.length) {
    ElMessage.error('票档未加载，请稍后再试')
    return
  }
  const seatDtoList = selectedSeats.value.map(s => ({
    id: s.id > 90000 ? -1 : s.id,
    ticketCategoryId: resolveTicketCategoryId(s.price),
    rowCode: s.rowCode,
    colCode: s.colCode,
    seatType: s.seatType,
    price: s.price,
    zoneName: s.zoneName
  }))
  if (seatDtoList.some(x => !x.ticketCategoryId)) {
    ElMessage.error('票档匹配失败，请稍后再试')
    return
  }
  
  router.replace({
    path: '/order/index',
    state: {
      detailList: JSON.stringify(detail.value || {}),
      allPrice: '',
      countPrice: '',
      num: seatDtoList.length,
      ticketCategoryId: '',
      seatDtoList: seatDtoList
    }
  })
}

onMounted(() => {
  // 接收从详情页传递的票档映射
  if (history.state && history.state.seatTicketIdMap) {
    seatTicketIdMap.value = history.state.seatTicketIdMap
  }
  
  getSeatRelateInfo({ programId }).then(res => {
    relate.value = res.data || { priceList: [], seatVoMap: {} }
  })
  getProgramDetials({ id: programId }).then(res => {
    detail.value = res.data || null
    // 如果详情中有映射且当前没有,使用详情中的
    if (detail.value && detail.value.seatTicketIdMap && !Object.keys(seatTicketIdMap.value).length) {
      seatTicketIdMap.value = detail.value.seatTicketIdMap
    }
    getSoldSeatListApi({ programId }).then(r => {
      buildSoldStatusIndex(r.data || [])
    })
    if (!seatSse) {
      const url = `/damai/order/order/seat/status/subscribe?programId=${programId}`
      seatSse = new EventSource(url)
      seatSse.addEventListener('seatStatus', (e) => {
        try {
          const data = JSON.parse(e.data)
          const items = Array.isArray(data.items) ? data.items : []
          items.forEach(it => {
            const k = `${String(it.ticketCategoryId)}|${it.zoneName || ''}|${Number(it.rowCode)}|${Number(it.colCode)}`
            const st = Number(it.sellStatus || 1)
            if (st === 3) {
              soldStatusMap.value[k] = 3
            } else if (st === 1) {
              if (soldStatusMap.value[k]) delete soldStatusMap.value[k]
            }
          })
        } catch (err) {}
      })
      seatSse.onerror = () => { try { seatSse.close() } catch(e){}; seatSse = null }
    }
  })

onBeforeUnmount(() => {
  if (soldPollingTimer) { clearInterval(soldPollingTimer); soldPollingTimer = null }
  if (seatSse) { try { seatSse.close() } catch(e){}; seatSse = null }
})
})
</script>

<style scoped>
.app-container {
  max-width: 100%;
  background: #111122; /* Dark Theme Background */
  min-height: 100vh;
  color: #fff;
  padding-bottom: 40px;
}

.wrapper {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.seat-header {
  text-align: center;
  margin-bottom: 30px;
}

.title {
  font-size: 28px;
  font-weight: 700;
  color: #fff;
  margin-bottom: 10px;
  text-shadow: 0 2px 10px rgba(0,0,0,0.5);
}

.hint {
  color: #888;
  font-size: 14px;
}

.legend {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-bottom: 30px;
  background: rgba(255,255,255,0.05);
  padding: 15px;
  border-radius: 50px;
  backdrop-filter: blur(10px);
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #ccc;
}

.dot {
  width: 12px;
  height: 12px;
  border-radius: 3px;
}

/* Reference Colors */
.price-0 { background: #FF4B4B; box-shadow: 0 0 10px #FF4B4B; }
.price-1 { background: #FF9F1C; box-shadow: 0 0 10px #FF9F1C; } /* Orange - Inner Back */
.price-2 { background: #9B5DE5; box-shadow: 0 0 10px #9B5DE5; } /* Purple - Side Stands */
.price-3 { background: #00F5D4; box-shadow: 0 0 10px #00F5D4; } /* Green - Back Corners */
.price-4 { background: #3B82F6; box-shadow: 0 0 10px #3B82F6; }

.picked { background: #fff; box-shadow: 0 0 15px #fff; border: 1px solid #fff; }
.sold { background: #444; opacity: 0.5; }

.seat-map-container {
  position: relative;
  width: 900px;
  height: 800px;
  margin: 0 auto;
  background: #1a1a2e;
  border-radius: 20px;
  box-shadow: 0 20px 50px rgba(0,0,0,0.5);
  overflow: hidden;
  border: 1px solid #333;
}

.stage-area {
  position: absolute;
  top: 40px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 10;
  display: flex;
  flex-direction: column;
  align-items: center;
  pointer-events: none;
}

.stage-shape {
  width: 300px;
  height: 80px;
  background: #333;
  clip-path: polygon(10% 0, 90% 0, 100% 100%, 0% 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 10px 30px rgba(0,0,0,0.5);
}

.stage-text {
  color: #fff;
  font-weight: 800;
  letter-spacing: 4px;
  font-size: 20px;
  text-transform: uppercase;
}

.control-center {
  margin-top: 165px;
  width: 60px;
  height: 60px;
  background: #222;
  border: 1px solid #444;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #666;
  font-size: 12px;
  border-radius: 4px;
}

.seats-layer {
  position: absolute;
  inset: 0;
}

.zone-label {
  position: absolute;
  color: rgba(255,255,255,0.1);
  font-size: 40px;
  font-weight: 900;
  pointer-events: none;
  z-index: 0;
}

.seat-item {
  position: absolute;
  width: 18px;
  height: 18px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
  z-index: 5;
}

.seat-item:hover {
  transform: scale(1.4) !important;
  z-index: 100;
  box-shadow: 0 0 15px currentColor;
}

.actions {
  margin-top: 40px;
  background: #232336;
  padding: 20px;
  border-radius: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border: 1px solid #333;
}

.selected-info {
  flex: 1;
}

.selected-count {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 10px;
  color: #fff;
}

.selected-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.sel-tag {
  background: #333;
  border: 1px solid #555;
  padding: 6px 12px;
  border-radius: 6px;
  font-size: 13px;
  color: #fff;
  display: flex;
  align-items: center;
  gap: 8px;
}

.price-tag {
  color: #FF9F1C;
  font-weight: bold;
}

.close-btn {
  cursor: pointer;
  color: #888;
  transition: color 0.2s;
}

.close-btn:hover {
  color: #ff4d4f;
}

.go-btn {
  width: 200px;
  height: 56px;
  font-size: 18px;
  border-radius: 28px;
  background: linear-gradient(90deg, #FF4B4B, #FF9F1C);
  border: none;
  font-weight: 700;
  box-shadow: 0 4px 20px rgba(255, 75, 75, 0.4);
}

.go-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(255, 75, 75, 0.5);
}

.go-btn:disabled {
  background: #444;
  box-shadow: none;
  transform: none;
}
:deep(.app-header) {
  background: linear-gradient(180deg, rgba(17,17,34,0.92), rgba(17,17,34,0.6));
  box-shadow: 0 8px 24px rgba(0,0,0,0.35);
}

:deep(.app-header .header .link img) {
  filter: brightness(0.85) contrast(0.95) saturate(0.9);
}

:deep(.app-header .header .localHeader .city-location) {
  color: #e8e8e8;
}

:deep(.app-header .header .recommendHeader .routeHome),
:deep(.app-header .header .recommendHeader .routeType) {
  color: #e6e6e6;
}

:deep(.app-header .header .recommendHeader .routeHome.router-link-active),
:deep(.app-header .header .recommendHeader .routeType.router-link-active) {
  color: #FF2D55;
}

:deep(.app-header .header .searchHeader .input-with-search) {
  background-color: rgba(255,255,255,0.08);
}

:deep(.app-header .header .searchHeader .input-with-search .el-input__wrapper) {
  background-color: transparent !important;
}

:deep(.app-header .header .searchHeader .el-input__inner) {
  color: #ddd;
}

:deep(.app-header .header .searchHeader .searchBtn) {
  background: linear-gradient(90deg, #FF4B4B, #FF9F1C);
  border: none;
}

:deep(.app-header .header .rightHeader .box-left img) {
  filter: brightness(0.9) contrast(0.95);
}

:deep(.app-header .header .rightHeader .box-left .log) {
  color: #e6e6e6;
}
</style>
