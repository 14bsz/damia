<template>
  <!--详情-->
  <Header></Header>
  <div class="app-container">
    <div class="goods">共 <span>{{ goods }}</span>个商品</div>
    <div class="box-main">
      <div class="box-main-left">
        <div class="box-tLeft">
          <div class="box-type">
            <el-collapse v-model="activeNames" @change="handleChange">
              <el-collapse-item name="1">
                <template #title>
                  <span class="title">城市：</span>
                  <span>当前选中城市：</span><span class="active">{{ currentCity }}</span>
                  <el-icon class="city-toggle-arrow" size = " 27" role="button" tabindex="0" @click.stop="toggleCityExpand" @keydown.enter="toggleCityExpand" :style="{ transform: isShow ? 'rotate(90deg)' : 'rotate(0deg)'}">
                    <CaretBottom/>
                  </el-icon>
                </template>
                <div class="city">
                  <ul style="margin-left: 70px">
                    <li v-show="isShow" v-for="(item,index) in cityArr.slice(0,22)" :key="item.id"
                        @click="cityClick(item,index)"
                        :class="{active: item.name == currentCity}"
                    >
                      <span>{{ item.name }}</span>
                    </li>
                    <li v-show="!isShow" v-for="(item,index) in cityArr" :key="item.id"
                        @click="cityClick(item,index)" :class="{active: activeCityIndex == index}">
                      <span>{{ item.name }}</span>
                    </li>

                  </ul>
                </div>
                <div class="btn" v-show="cityArr.length>22">
                  <span v-show="isShow" @click="isShow=false">更多</span>
                  <span v-show="!isShow" @click="isShow=true">收起</span>
                </div>
              </el-collapse-item>
              <el-collapse-item name="2">
                <template #title>
                  <span class="title">分类:</span>
                </template>
                <div>
                  <ul>
                    <li v-for="(item,ind) in categoryArr" :key="item.id"
                        @click="categoryClick(item,ind)"
                        :class="{active: ($route.query.name==item.name) || (item.name==='全部' ? isActive : activeIndex == ind)}"
                    >
                      <span>{{ item.name }}</span>
                    </li>
                  </ul>
                </div>
              </el-collapse-item>
              <el-collapse-item name="3" v-if="isShowChildren">
                <template #title>
                  <span class="title">子类:</span>
                </template>
                <div>
                  <ul>
                    <li v-for="(item,index) in childrenArr" :key="item.id"
                        @click="childrenClick(item,index)" :class="{active: activeChildrenIndex == index}">
                      {{ item.name }}
                    </li>

                  </ul>
                </div>
              </el-collapse-item>

              <el-collapse-item name="4">
                <template #title>
                  <span class="title">时间:</span>
                </template>
                <div>
                  <ul>
                    <li v-for="(item,index) in timeArr" :key="item.id"
                        @click="timeClick(item,index)" :class="{active: activeTimeIndex == index}">
                      <span>{{ item.name }}</span>
                    </li>
                    <li class="liDate" v-if="isShowDate">
                      <el-date-picker
                          v-model="value1"
                          type="daterange"
                          start-placeholder="开始时间"
                          end-placeholder="结束时间"
                          @change="handleChangeDate"
                      />
                    </li>
                  </ul>
                </div>

              </el-collapse-item>

            </el-collapse>
          </div>
          <div class="box-sort">
            <el-tabs type="border-card" class="box-tabs" @tab-click="handleClickTab">
              <el-tab-pane label="Config">
                <template #label>相关度排序</template>
                <ul>
                  <li v-for="item in cardArr">
                    <router-link :to="{name:'detial',params:{id:item.id}}" class="link">
                      <img :src="item.itemPicture" alt="">
                      <div class="img-tag" v-if="item.parentProgramCategoryName">{{ item.parentProgramCategoryName }}</div>
                    </router-link>
                    <div class="item-txt">
                      <div class="item-title">
                        <span>【{{ item.areaName }}】</span>
                        <router-link :to="{name:'detial', params:{id:item.id}}" class="link-detial" v-if="titleIsShow">
                          {{ item.title }}
                        </router-link>
                        <router-link :to="{name:'detial', params:{id:item.id}}" class="link-detial" v-if="!titleIsShow"
                                     v-html="item.title"></router-link>

                      </div>
                      <div class="item-content" v-if="titleIsShow">艺人：{{ item.actor }}</div>
                      <div class="item-content" v-if="!titleIsShow"><span>艺人：</span><span  v-html="item.actor"></span> </div>

                      <div class="item-content"> {{ item.areaName }} | {{ item.place }}</div>
                      <div class="item-content">{{ formatDateWithWeekday(item.showTime, item.showWeekTime) }}</div>
                      <div class="item-seat" v-if="item.permitChooseSeat == 1 || item.permitChooseSeat == '1'">可选座</div>

                      <div class="item-tag"></div>
                      <div class="item-price">
                        <span class="price">{{ item.minPrice }}-{{ item.maxPrice }}元</span>
                        <span class="item-content">售票中</span></div>
                    </div>
                  </li>
                </ul>
                <pagination
                    v-show="total > 0"
                    :total="total"
                    v-model:page="queryParams.pageNum"
                    v-model:limit="queryParams.pageSize"
                    @pagination="getList"
                />
              </el-tab-pane>
              <el-tab-pane label="Config">
                <template #label>推荐排序</template>
                <ul>
                  <li v-for="item in cardArr">
                    <router-link :to="{name:'detial',params:{id:item.id}}" class="link">
                      <img :src="item.itemPicture" alt="">
                      <div class="img-tag" v-if="item.parentProgramCategoryName">{{ item.parentProgramCategoryName }}</div>
                    </router-link>
                    <div class="item-txt">
                      <div class="item-title">
                        <span>【{{ item.areaName }}】</span>
                        <router-link :to="{name:'detial', params:{id:item.id}}" class="link-detial" v-if="titleIsShow">
                          {{ item.title }}
                        </router-link>
                        <router-link :to="{name:'detial', params:{id:item.id}}" class="link-detial" v-if="!titleIsShow"
                                     v-html="item.title"></router-link>

                      </div>
                      <div class="item-content" v-if="titleIsShow">艺人：{{ item.actor }}</div>
                      <div class="item-content" v-if="!titleIsShow"><span>艺人：</span><span  v-html="item.actor"></span> </div>
                      <div class="item-content"> {{ item.areaName }} | {{ item.place }}</div>
                      <div class="item-content">{{ formatDateWithWeekday(item.showTime, item.showWeekTime) }}</div>
                      <div class="item-seat" v-if="item.permitChooseSeat == 1 || item.permitChooseSeat == '1'">可选座</div>

                      <div class="item-tag"></div>
                      <div class="item-price">
                        <span class="price">{{ item.minPrice }}-{{ item.maxPrice }}元</span>
                        <span class="item-content">售票中</span></div>
                    </div>
                  </li>
                </ul>
                <pagination
                    v-show="total > 0"
                    :total="total"
                    v-model:page="queryParams.pageNum"
                    v-model:limit="queryParams.pageSize"
                    @pagination="getList"
                />
              </el-tab-pane>
              <el-tab-pane label="Role">
                <template #label>最近开场</template>
                <ul>
                  <li v-for="item in cardArr">
                    <router-link :to="{name:'detial',params:{id:item.id}}" class="link">
                      <img :src="item.itemPicture" alt="">
                      <div class="img-tag" v-if="item.parentProgramCategoryName">{{ item.parentProgramCategoryName }}</div>
                    </router-link>
                    <div class="item-txt">
                      <div class="item-title">
                        <span>【{{ item.areaName }}】</span>
                        <router-link :to="{name:'detial', params:{id:item.id}}" class="link-detial" v-if="titleIsShow">
                          {{ item.title }}
                        </router-link>
                        <router-link :to="{name:'detial', params:{id:item.id}}" class="link-detial" v-if="!titleIsShow"
                                     v-html="item.title"></router-link>

                      </div>
                      <div class="item-content" v-if="titleIsShow">艺人：{{ item.actor }}</div>
                      <div class="item-content" v-if="!titleIsShow"><span>艺人：</span><span  v-html="item.actor"></span> </div>
                      <div class="item-content"> {{ item.areaName }} | {{ item.place }}</div>
                      <div class="item-content">{{ formatDateWithWeekday(item.showTime, item.showWeekTime) }}</div>
                      <div class="item-seat" v-if="item.permitChooseSeat == 1 || item.permitChooseSeat == '1'">可选座</div>

                      <div class="item-tag"></div>
                      <div class="item-price">
                        <span class="price">{{ item.minPrice }}-{{ item.maxPrice }}元</span>
                        <span class="item-content">售票中</span></div>
                    </div>
                  </li>
                </ul>
                <pagination
                    v-show="total > 0"
                    :total="total"
                    v-model:page="queryParams.pageNum"
                    v-model:limit="queryParams.pageSize"
                    @pagination="getList"
                />
              </el-tab-pane>
              <el-tab-pane label="Task">
                <template #label>最新上架</template>
                <ul>
                  <li v-for="item in cardArr">
                    <router-link :to="{name:'detial',params:{id:item.id}}" class="link">
                      <img :src="item.itemPicture" alt="">
                      <div class="img-tag" v-if="item.parentProgramCategoryName">{{ item.parentProgramCategoryName }}</div>
                    </router-link>
                    <div class="item-txt">
                      <div class="item-title">
                        <span>【{{ item.areaName }}】</span>
                        <router-link :to="{name:'detial', params:{id:item.id}}" class="link-detial" v-if="titleIsShow">
                          {{ item.title }}
                        </router-link>
                        <router-link :to="{name:'detial', params:{id:item.id}}" class="link-detial" v-if="!titleIsShow"
                                     v-html="item.title"></router-link>

                      </div>
                      <div class="item-content" v-if="titleIsShow">艺人：{{ item.actor }}</div>
                      <div class="item-content" v-if="!titleIsShow"><span>艺人：</span><span  v-html="item.actor"></span> </div>
                      <div class="item-content"> {{ item.areaName }} | {{ item.place }}</div>
                      <div class="item-content">{{ formatDateWithWeekday(item.showTime, item.showWeekTime) }}</div>
                      <div class="item-seat" v-if="item.permitChooseSeat == 1 || item.permitChooseSeat == '1'">可选座</div>

                      <div class="item-tag"></div>
                      <div class="item-price">
                        <span class="price">{{ item.minPrice }}-{{ item.maxPrice }}元</span>
                        <span class="item-content">售票中</span></div>
                    </div>
                  </li>
                </ul>
                <pagination
                    v-show="total > 0"
                    :total="total"
                    v-model:page="queryParams.pageNum"
                    v-model:limit="queryParams.pageSize"
                    @pagination="getList"
                />
              </el-tab-pane>
            </el-tabs>

          </div>
        </div>
      </div>
      <div class="box-main-right">
        <div class="box-like">
          您可能还喜欢
        </div>
        <ul class="search__box" v-if="recommendList && recommendList.length">
          <li class="search__item" v-for="item in recommendList" :key="item.id || item.title">
            <router-link v-if="item.id" :to="{name:'detial',params:{id:item.id}}" class="link">
              <img :src="item.itemPicture" alt="">
            </router-link>
            <img v-else :src="item.itemPicture" alt="">
            <div class="search_item_info">
              <router-link v-if="item.id" :to="{name:'detial',params:{id:item.id}}" class="link__title">
                {{ item.title }}
              </router-link>
              <div v-else class="link__title">{{ item.title }}</div>
              <div class="search__item__info__venue">{{ item.place }}</div>
              <div class="search__item__info__venue">{{ formatDateWithWeekday(item.showTime, item.showWeekTime) }}</div>
              <div class="search__item__info__price"><strong>{{ item.minPrice }} 起</strong></div>
            </div>
          </li>
        </ul>
        <ul class="search__box" v-else>
          <li class="search__item">
            <img :src="'https://picsum.photos/seed/like1/98/132'" alt="">
            <div class="search_item_info">
              <div class="link__title">为你精选</div>
              <div class="search__item__info__venue">近期热门活动</div>
              <div class="search__item__info__price"><strong>99 起</strong></div>
            </div>
          </li>
          <li class="search__item">
            <img :src="'https://picsum.photos/seed/like2/98/132'" alt="">
            <div class="search_item_info">
              <div class="link__title">热门演出</div>
              <div class="search__item__info__venue">覆盖全国热门城市</div>
              <div class="search__item__info__price"><strong>199 起</strong></div>
            </div>
          </li>
          <li class="search__item">
            <img :src="'https://picsum.photos/seed/like3/98/132'" alt="">
            <div class="search_item_info">
              <div class="link__title">专题推荐</div>
              <div class="search__item__info__venue">精彩不容错过</div>
              <div class="search__item__info__price"><strong>299 起</strong></div>
            </div>
          </li>
        </ul>
      </div>
    </div>
    <Footer></Footer>
  </div>
</template>

<script setup>
//引入reactive
import {getCurrentInstance, onMounted, reactive, ref, watch} from 'vue'
import {getCurrentCity, getOtherCity} from '@/api/area'
import {getcategoryType, getMainCategory} from "@/api/index";
import {getCurrentDate, useMitt,formatDateWithWeekday} from "@/utils/index";
import {getChildrenType, getProgramPageType, getProgramSearch} from "@/api/allType";
import {getProgramRecommendList} from "@/api/recommendlist.js"
//引入路由器
import {useRouter} from 'vue-router'

const router = useRouter()

const emitter = useMitt();

const goods = ref(0)

let count = 0;

const cityArr = ref([])
const categoryArr = ref([])
const childrenArr = ref([])
const currentCity = ref('')
const parentProgramCategoryId = ref('')
const isShow = ref(true)
const activeIndex = ref('')
const isShowClass = ref('')
const activeCityIndex = ref('')
const activeChildrenIndex = ref('')
const activeTimeIndex = ref('')
const isShowChildren = ref(false)
const queryParams = ref({pageNum: 1, pageSize: 10})
const total = ref(0)
const isShowDate = ref(false)
const value1 = ref([])
const timeType = ref(0)
const cardArr = ref([])
const titleIsShow = ref(true)
const rawHtml = ref('')
const preferSecondPageOnFirst = ref(false)
//推荐列表数据
const recommendList = ref([])
const isActive = ref(false)
const pageParams = ref({
  areaId: undefined,
  endDateTime: undefined,
  pageNumber: undefined,
  pageSize: undefined,
  parentProgramCategoryId: undefined,
  programCategoryId: undefined,
  startDateTime: undefined,
  timeType: undefined,
  type: 1//1:相关度排序(默认) 2:推荐排序 3:最近开场 4:最新上架
})
//推荐节目列表入参
const recommendParams = reactive({
  areaId: undefined,
  parentProgramCategoryId: 1,
  programId: undefined
})
const {proxy} = getCurrentInstance();

//手风琴面板
const activeNames = ref(['1', '2', '3', '4'])
const handleChange = (val) => {
  activeNames.value = ['1', '2', '3', '4'];//始终让四个面板显示，不可关闭
}

const toggleCityExpand = () => {
  isShow.value = !isShow.value
}


//获取城市数据
const getcityList = () => {
  getOtherCity().then(response => {
    cityArr.value = response.data
    cityArr.value.unshift({name: '全部', id: ''})
  })
}
getcityList()


//当前城市
const getCurrent = () => {
  getCurrentCity().then(response => {
    let {name, parentId, id, type} = response.data
    currentCity.value = name

  })

}
getCurrent()

//获取分类
const getTypeList = () => {
  getcategoryType({type: 1}).then(response => {
    categoryArr.value = response.data
    categoryArr.value.unshift({name: '全部', id: ''})
  })
}

getTypeList()
//获取子类
const getChildrenTypeList = () => {
  getChildrenType({parentProgramCategoryId: parentProgramCategoryId.value}).then(response => {
    childrenArr.value = response.data
    childrenArr.value.unshift({name: '全部', id: ''})
    if (childrenArr.value == '') {
      isShowChildren.value = false
    }
  })
}

//分类

//点击分类每一项
const categoryClick = (item, ind) => {
  proxy.$route.query.name = ''
  activeIndex.value = ind
  queryParams.value.pageNum = 1
  pageParams.value.programCategoryId = undefined
  if (item.name === '全部') {
    isActive.value = true
    isShowChildren.value = false
    parentProgramCategoryId.value = ''
    pageParams.value.parentProgramCategoryId = undefined
    recommendParams.parentProgramCategoryId = undefined
    activeChildrenIndex.value = ''
    getList()
    getRecommendList()
    return
  }
  isActive.value = false
  isShowChildren.value = true
  parentProgramCategoryId.value = item.id
  pageParams.value.parentProgramCategoryId = item.id
  recommendParams.parentProgramCategoryId = item.id
  activeChildrenIndex.value = ''
  getChildrenTypeList()
  getList()
  getRecommendList()
}
//点击城市
const cityClick = (item, index) => {
  activeCityIndex.value = index
  currentCity.value = item.name
  queryParams.value.pageNum = 1
  pageParams.value.areaId = item.id
  //推荐节目列表入参中的区域字段
  recommendParams.areaId = item.id
  getList()
  getRecommendList()
}
//点击子类
const childrenClick = (item, index) => {
  activeChildrenIndex.value = index
  queryParams.value.pageNum = 1
  if (item.name === '全部') {
    pageParams.value.programCategoryId = undefined
    pageParams.value.parentProgramCategoryId = parentProgramCategoryId.value
  } else {
    pageParams.value.programCategoryId = item.id
    pageParams.value.parentProgramCategoryId = undefined
  }
  getList()
}
//点击时间
const timeClick = (item, index) => {
  activeTimeIndex.value = index
  timeType.value = item.id
  queryParams.value.pageNum = 1
  pageParams.value.timeType = item.id
  if (item.id == 5) {
    isShowDate.value = true
    pageParams.value.timeType = 5
  } else {
    isShowDate.value = false
    pageParams.value.startDateTime = undefined
    pageParams.value.endDateTime = undefined
    getList()
  }

}
const handleChangeDate = (selection) => {
  queryParams.value.pageNum = 1
  pageParams.value.startDateTime = getCurrentDate(selection[0])
  pageParams.value.endDateTime = getCurrentDate(selection[1])
  getList()
}

//时间数组
const timeArr = ref(
    [{
      name: '全部',
      id: 0
    }, {
      name: '今天',
      id: 1
    },
      {
        name: '明天',
        id: 2
      },
      {
        name: '本周末',
        id: 3
      },
      {
        name: '一个月内',
        id: 4
      }, {
      name: '按日历',
      id: 5
    },
    ])


const getList = () => {
  pageParams.value.timeType = timeType.value
  pageParams.value.pageNumber = queryParams.value.pageNum
  pageParams.value.pageSize = queryParams.value.pageSize
  getProgramPageType(pageParams.value).then(response => {
    cardArr.value = response.data.list
    total.value = Number(response.data.totalSize)
    goods.value = Number(response.data.totalSize) || (Array.isArray(response.data.list) ? response.data.list.length : 0)
  })
}

//节目推荐列表
const getRecommendList = () => {
  //如果没有选择父类型，则默认为演唱会
  if (recommendParams.parentProgramCategoryId == '') {
    recommendParams.parentProgramCategoryId = 1
  }
  const maxRightItems = 17
  getProgramRecommendList(recommendParams).then(response => {
    const data = response.data || []
    if (data.length) {
      recommendList.value = data.slice(0, maxRightItems)
    } else {
      const homeParams = { areaId: recommendParams.areaId, parentProgramCategoryIds: [] }
      if (recommendParams.parentProgramCategoryId) {
        homeParams.parentProgramCategoryIds = [recommendParams.parentProgramCategoryId]
      }
      getMainCategory(homeParams).then(res => {
        const list = []
        ;(res.data || []).forEach(c => {
          if (c.programListVoList) list.push(...c.programListVoList)
        })
        if (list.length) {
          recommendList.value = list.slice(0, maxRightItems)
        } else {
          recommendList.value = [
            { itemPicture: 'https://picsum.photos/seed/like1/98/132', title: '为你精选', place: '近期热门活动', showTime: '', showWeekTime: '', minPrice: 99 },
            { itemPicture: 'https://picsum.photos/seed/like2/98/132', title: '热门演出', place: '覆盖全国热门城市', showTime: '', showWeekTime: '', minPrice: 199 },
            { itemPicture: 'https://picsum.photos/seed/like3/98/132', title: '专题推荐', place: '精彩不容错过', showTime: '', showWeekTime: '', minPrice: 299 }
          ]
        }
      })
    }
    const minCount = 20
    if ((recommendList.value || []).length < minCount) {
      const extraParams = {
        areaId: pageParams.value.areaId,
        parentProgramCategoryId: pageParams.value.parentProgramCategoryId,
        programCategoryId: pageParams.value.programCategoryId,
        timeType: pageParams.value.timeType,
        pageNumber: 1,
        pageSize: 50
      }
      getProgramPageType(extraParams).then(r => {
        const extras = (r.data && r.data.list) ? r.data.list : []
        const seen = new Set((recommendList.value || []).map(i => i.id))
        const merged = [...recommendList.value]
        extras.forEach(it => {
          if (!seen.has(it.id)) {
            seen.add(it.id)
            merged.push(it)
          }
        })
        recommendList.value = merged.slice(0, maxRightItems)
      })
    }
  })
}

onMounted(() => {
  pageParams.value.pageNumber = 1
  pageParams.value.pageSize = 10
  pageParams.value.areaId = currentCity.value
  pageParams.value.timeType = timeType.value
  pageParams.value.parentProgramCategoryId = proxy.$route.query.id
  const q = proxy?.$route?.query?.q
  if (q && String(q).trim().length) {
    doSearchFromRoute(String(q))
    getRecommendList()
  } else {
    getList()
    getRecommendList()
  }
  emitter.on('searchList', (data) => {
    cardArr.value = data.list
    total.value = Number(data.totalSize)
    titleIsShow.value = false
    goods.value = Number(data.totalSize) || (Array.isArray(data.list) ? data.list.length : 0)
  })
})

function doSearchFromRoute(content) {
  const params = { content, pageNumber: 1, pageSize: 10, timeType: 0 }
  getProgramSearch(params).then(response => {
    const data = response.data || { list: [], totalSize: 0 }
    cardArr.value = data.list
    total.value = Number(data.totalSize)
    titleIsShow.value = false
    goods.value = Number(data.totalSize) || (Array.isArray(data.list) ? data.list.length : 0)
  })
}

watch(() => proxy.$route.query.q, (newQ) => {
  if (typeof newQ === 'string' && newQ.trim().length) {
    doSearchFromRoute(newQ.trim())
  }
})

function handleClickTab(tab, event) {
  pageParams.value.type = Number(tab.index) + 1
  getList()
}

function removeTag(str, tag) {
  const regex = new RegExp(`<${tag}[^>]*>|</${tag}>`, 'gi');
  return str.replace(regex, '');
}


</script>

<style scoped lang="scss">
.app-container {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;

  .goods {
    display: flex;
    align-items: center;
    gap: 8px;
    line-height: 50px;
    color: #666;
    font-size: 15px;
    font-weight: 500;

    span {
      color: rgba(255, 55, 29, 0.85);
      display: inline-flex;
      align-items: center;
      justify-content: center;
      min-width: 36px;
      height: 26px;
      padding: 0 12px;
      border: 1px solid rgba(255, 55, 29, 0.25);
      border-radius: 999px;
      background: linear-gradient(180deg, rgba(255, 55, 29, 0.08) 0%, rgba(255, 55, 29, 0.06) 100%);
      font-weight: 600;
      font-variant-numeric: tabular-nums lining-nums;
      box-shadow: inset 0 1px 2px rgba(0,0,0,0.03);
    }
  }

  .box-main {
    display: flex;

    .box-main-left {


      .box-tLeft {
        width: 928px;

        .box-type {
          padding: 0 24px;
          border: 1px solid #e9e9e9;
          min-height: 300px;
          border-radius: 12px;
          background: #fff;

          div.city {
            display: inline-block;
            width: calc(100% - 40px);
          }

          div {
            ul {
              margin: 0;
              padding: 10px;

              li {
                //width: 42px;
                list-style: none;
                display: inline-block;
                height: 26px;
                line-height: 26px;
                padding: 0 12px;
                margin-right: 12px;
                margin-bottom: 10px;
                color: #333;
                white-space: nowrap;
                cursor: pointer;
                border: 1px solid #e9e9e9;
                border-radius: 999px;
                transition: all .2s ease;
                user-select: none;
              }
              li:hover { border-color: rgba(255, 55, 29, 0.85); transform: translateY(-1px); }
              li.active { border-color: rgba(255, 55, 29, 0.45); background: rgba(255, 55, 29, 0.05); color: rgba(255, 55, 29, 0.85); font-weight: 600; }
              .active { border: 1px solid rgba(255, 55, 29, 0.25); border-radius: 12px; background: transparent !important; color: rgba(255, 55, 29, 0.85) !important; font-weight: 600; padding: 0 8px; }

              .liDate {
                width: 320px;
                padding: 0px !important;
                height: 26px;
                line-height: 26px;
                float: right;
              }

            }

          }

          .btn {
            display: inline-block;
            width: 40px;
            height: 100px;
            line-height: 100px;
            cursor: pointer;
          }
        }

        .box-sort {
          .box-tabs {
            :deep(.el-tabs__item) { font-weight: 600; padding: 0 18px; transition: color .2s ease; }
            :deep(.el-tabs__item.is-active) { color: rgba(255, 55, 29, 0.85); }
            ul {
              margin: 0;
              padding: 0;

              li {
                list-style-type: none;
                position: relative;
                display: flex;
                align-items: flex-start;
                padding: 16px 16px 18px;
                border-bottom: none;
                margin: 12px 10px;
                height: 250px;
                background: #fff;
                border: 1px solid #eaeaea;
                border-radius: 12px;
                box-shadow: 0 4px 12px rgba(0,0,0,0.06);
                transition: transform .2s ease, box-shadow .2s ease;
                &:hover { transform: translateY(-2px); box-shadow: 0 8px 20px rgba(0,0,0,0.08); }

              .link {
                position: relative;
                display: block;
                width: 153px;
                height: 206px;
                overflow: hidden;
                margin-right: 20px;
                flex: 0 0 153px;

                  img {
                    width: 100%;
                    height: 100%;
                    object-fit: cover;
                    border-radius: 8px;
                  }

                  .img-tag {
                    position: absolute;
                    top: 8px;
                    left: 8px;
                    background: rgba(255, 55, 29, 0.85);
                    color: #fff;
                    font-size: 12px;
                    padding: 2px 8px;
                    border-radius: 6px;
                  }

              }

                .item-txt {

                  flex: 1;
                  line-height: 24px;
                  position: relative;
                  display: grid;
                  grid-auto-rows: min-content;
                  row-gap: 6px;
                  padding-bottom: 44px;

                  .item-title {
                    margin-bottom: 8px;
                    font-size: 18px;
                    font-weight: 600;

                    span { margin-right: 6px; }

                    .link-detial {
                      color: #333;
                      text-decoration: none;
                      outline: 0;
                      transition: color .2s ease;


                    }
                  }

                  .item-content {
                    margin-bottom: 10px;
                    overflow: hidden;
                    white-space: nowrap;
                    text-overflow: ellipsis;
                    color: #999;
                  }

                  .item-tag {
                  }

                  .item-price {
                    position: absolute;
                    bottom: 12px;
                    color: #999;
                    overflow: hidden;
                    display: flex;
                    align-items: center;
                    gap: 8px;

                    .price {
                      color: rgba(255, 55, 29, 0.85);
                      font-weight: 700;
                      font-size: 16px;
                      margin-right: 20px;
                      font-style: normal;
                      font-weight: bold;
                    }

                  }

                  .item-seat {
                    display: inline-block;
                    font-size: 12px;
                    color: rgba(255, 55, 29, 0.85);
                    border: 1px solid rgba(255, 55, 29, 0.25);
                    border-radius: 12px;
                    padding: 0 8px;
                    margin-top: 6px;
                  }
                }
              }
            }

            .page {
              margin: 12px 12px 20px 0;
              float: right;
            }
          }
        }
      }

    }

    .box-main-right {
      width: 258px;
      border: 1px solid #eaeaea;
      margin-left: 10px;
      max-height: 514px;
      float: right;

      .box-like {
        height: 37px;
        line-height: 37px;
        background-color: #f5f5f5;
        border-bottom: 1px solid #eaeaea;
        font-size: 14px;
        font-family: Microsoft YaHei;
        color: #000;
        padding: 0 15px;
      }

      .search__box {
        margin: 0;
        padding: 0;

        .search__item {
          display: flex;
          background-color: #fff;
          border: none;
          padding: 10px 10px;
          margin-bottom: 8px;
          box-shadow: none;

          img {
            width: 98px;
            height: 132px;
            float: left;
          }

          .search_item_info {
            .link__title {
              font-size: 14px;
            }

            .search__item__info__venue {
              font-size: 12px;
            }

            .search__item__info__price {
              font-size: 12px;
            }
          }
        }

      }
    }
  }


}

.active {
  background-color: transparent;
  color: rgba(255, 55, 29, 0.85) !important;
}

:deep(.el-collapse-item__header .el-collapse-item__arrow) {
  display: none !important;
}

:deep(.el-collapse-item__header .title) {
  width: 50px;
  height: 26px;
  line-height: 26px;
  display: inline-block;
  color: #968788;
  text-align: center;
}

:deep(.el-collapse-item__header .active) {
  display: inline-flex;
  align-items: center;
  height: 26px;
  line-height: 26px;
  padding: 0 10px;
  border: 1px solid rgba(255, 55, 29, 0.25);
  border-radius: 999px;
  background: rgba(255, 55, 29, 0.05);
  color: rgba(255, 55, 29, 0.85) !important;
  font-weight: 600;
}

.city-toggle-arrow {
  margin-left: auto;
  padding-left: 8px;
  transition: transform .2s ease;
  cursor: pointer;
  vertical-align: middle;
  display: inline-flex;
  align-items: center;
  pointer-events: auto;
}


:deep(.el-collapse-item) {
  display: flex;
  flex-direction: row;
  border-bottom: 1px dotted #dfdfdf;
}

:deep(.el-collapse-item:first-child) {
  display: block;


}

:deep(.el-collapse-item__content) {
  padding-bottom: 0;
}

:deep(.el-collapse-item__wrap) {
  border: none;
}

/* 添加CSS来隐藏日历控件的输入框 */
:deep(.el-date-editor.el-input, .el-date-editor.el-input__wrapper) {
  display: none;
}

.search__item {
  background-color: #fff; /* 白色背景 */
  border: 1px solid #e0e0e0; /* 边框 */
  padding: 15px; /* 内边距 */
  margin-bottom: 10px; /* 每个列表项之间的外边距 */
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1); /* 阴影效果 */
}

.link {
  color: #007bff; /* 链接颜色 */
  text-decoration: none; /* 去除下划线 */
}

.link__title {

  text-decoration: none; /* 去除下划线 */
}

.search__item img {
  width: 100px; /* 图片宽度 */
  height: auto; /* 图片高度自适应 */
  margin-right: 15px; /* 图片与文字的间距 */
}

.search_item_info {
  display: inline-block; /* 文字信息与图片同行显示 */
  vertical-align: top; /* 顶部对齐 */
}

.search__item__info__venue,
.search__item__info__price {
  font-size: 14px; /* 字体大小 */
  color: #666; /* 字体颜色 */
}

.search__item__info__price strong {
  color: rgba(255, 55, 29, 0.85); /* 价格字体颜色为红色 */
}
:deep(em){
 font-weight: bolder;
  color: rgba(255, 55, 29, 0.85);

}
</style>
