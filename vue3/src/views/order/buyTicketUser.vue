<template>
<div class="app-container">
  <div class="form-container">
    <div class="page-title">新增观演人</div>
    <el-form ref="formTicket" :model="form" :rules="rules" class="ticket-form" label-position="top">
      <el-form-item label="姓名" prop="relName">
        <el-input
            v-model="form.relName"
            type="text"
            placeholder="请填写观演人姓名"
            class="custom-input"
        ></el-input>
      </el-form-item>
      
      <el-form-item label="证件类型" prop="idType">
        <el-select v-model="form.idType" class="custom-select" placeholder="请选择证件类型">
          <el-option v-for="item in idType"
                     :key="item.value"
                     :value="item.value"
                     :label="item.name">{{item.name}}</el-option>
        </el-select>
      </el-form-item>
      
      <el-form-item label="证件号码" prop="idNumber">
        <el-input
            v-model="form.idNumber"
            type="text"
            placeholder="请填写证件号码"
            class="custom-input"
        ></el-input>
      </el-form-item>
      
      <div class="tips">
        <el-icon class="icon-warning"><Warning /></el-icon>
        <span>点击确定表示您已阅读并同意 <span class="link">《实名须知》</span></span>
      </div>
      
      <div class="form-actions">
        <el-button type="primary" class="submit-btn" @click="submit">确定</el-button>
      </div>
    </el-form>
  </div>
</div>
</template>

<script setup name="BuyTicket">
import {getCurrentInstance, ref,onMounted} from 'vue'
import {saveTicketUser} from "@/api/buyTicketUser";
import { getUserIdKey} from "@/utils/auth";
import {useRoute, useRouter} from 'vue-router'
import { ElMessage } from 'element-plus'
import { Warning } from '@element-plus/icons-vue'

const route = useRoute();
const router = useRouter();

const {proxy} = getCurrentInstance();
const form=ref({})
const rules = ref({})
form.value.idType = '1' // Default to ID Card
const detailList = ref([])
const allPrice = ref('')
const countPrice = ref('')
const num = ref('')

const idType = ref([{
  name:'身份证',
  value:'1'
},{
  name:'港澳台居民居住证',
  value:'2'
},{
  name:'港澳台居民来往内地通行证',
  value:'3'
},{
  name:'台湾居民来往内地通行证',
  value:'4'
},{
  name:'护照',
  value:'5'
},{
  name:'外国人永久居住证',
  value:'6'
}])

onMounted(()=>{
  if (history.state) {
      detailList.value  = history.state.detailList ? JSON.parse(history.state.detailList) : []
      allPrice.value  = history.state.allPrice
      countPrice.value  =history.state.countPrice
      num.value  =history.state.num
  }
})
const submit =()=>{
  proxy.$refs.formTicket.validate(valid => {
    if (valid) {
      if(!form.value.relName){
        ElMessage({
          message: '请填写观演姓名',
          type: 'error',
        })
      }else if(!form.value.idNumber){
        ElMessage({
          message: '请填写证件号码',
          type: 'error',
        })
      }else{
        form.value.userId=getUserIdKey()
        saveTicketUser(form.value).then(response=>{
          if(response.code==0){
            router.replace({path:'/order/index', state: history.state})
          }
        })
      }
    }
  });
}
</script>

<style scoped lang="scss">
.app-container {
  width: 100%;
  min-height: 100vh;
  background: linear-gradient(135deg, #fdfbfb 0%, #ebedee 100%); // Subtle gradient background
  padding: 40px 20px;
  box-sizing: border-box;
  display: flex;
  justify-content: center;
  align-items: flex-start;
}

.form-container {
  width: 100%;
  max-width: 820px;
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.08);
  overflow: hidden; // Ensure header background doesn't overflow
  margin-top: 20px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #fff;
  background: linear-gradient(90deg, #ff2869 0%, #ff3299 100%);
  padding: 24px;
  text-align: center;
  margin-bottom: 30px;
  letter-spacing: 1px;
}

.ticket-form {
  padding: 0 40px 40px; // Move padding here to exclude header
  
  :deep(.el-form-item__label) {
    font-size: 16px;
    color: #333;
    font-weight: 500;
    padding-bottom: 8px;
    line-height: 1.5;
  }

  :deep(.el-input__wrapper), :deep(.el-select__wrapper) {
    box-shadow: 0 0 0 1px #e4e7ed inset;
    padding: 8px 12px;
    border-radius: 8px;
    background-color: #f8f9fa; // Light background for inputs
    transition: all 0.3s;
    
    &.is-focus {
      background-color: #fff;
      box-shadow: 0 0 0 1px #ff2869 inset !important;
    }
    
    &:hover {
      background-color: #fff;
    }
  }
  
  :deep(.el-input__inner) {
    height: 40px;
    font-size: 16px;
    color: #333;
  }

  .custom-input, .custom-select {
    width: 100%;
  }
}

.tips {
  display: flex;
  align-items: center;
  margin: 12px 0 16px;
  font-size: 14px;
  color: #999;
  
  .icon-warning {
    margin-right: 6px;
    font-size: 16px;
    color: #ff2869;
  }
  
  .link {
    color: #3b99fc;
    cursor: pointer;
    &:hover {
      text-decoration: underline;
    }
  }
}

.form-actions {
  display: flex;
  justify-content: center;
  
  .submit-btn {
    width: 100%;
    height: 48px;
    font-size: 18px;
    border-radius: 24px;
    background: linear-gradient(90deg, #ff2869 0%, #ff3299 100%);
    border: none;
    
    &:hover {
      opacity: 0.9;
    }
    
    &:active {
      opacity: 1;
      transform: scale(0.99);
    }
  }
}
</style>
