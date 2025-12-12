<template>
  <Header></Header>
  <div class="auth-page">
    <div class="auth-card">
      <div class="card-left">
        <img src="https://p3-flow-imagex-sign.byteimg.com/tos-cn-i-a9rns2rl98/rc_gen_image/106ded7fd76040b9bed568084a7299e0.jpeg~tplv-a9rns2rl98-image_raw_b.png?rcl=20251212093655AF5DEEA8A3A3D1911628&rk3s=8e244e95&rrcfp=827586d3&x-expires=2080863432&x-signature=VtXcLnGa0eCIgW9xUNxbi8g7QKk%3D" alt="Security" class="cover-image" />
        <div class="image-overlay">
          <h3 class="overlay-title">账户安全</h3>
          <p class="overlay-text">实名认证提升账号安全等级</p>
        </div>
      </div>
      <div class="card-right">
        <div class="form-header">
          <h2 class="title">实名认证</h2>
          <p class="subtitle">请输入真实姓名与身份证号码</p>
        </div>
        <el-form ref="authRef" :model="authForm" :rules="authRules" class="login-form" size="large" label-position="top">
          <el-form-item label="请输入真实姓名:" prop="relName" class="input-item">
            <el-input
              v-model="authForm.relName"
              class="custom-input"
              placeholder="请输入真实姓名"
              type="text"
            ></el-input>
          </el-form-item>
          <el-form-item label="请输入身份证号码:" prop="idNumber" class="input-item">
            <el-input
              v-model="authForm.idNumber"
              class="custom-input"
              placeholder="请输入身份证号码"
              type="text"
            ></el-input>
          </el-form-item>
          <el-form-item class="btn-item">
            <el-button size="large" type="primary" class="submit-btn" @click.prevent="savePsd">
              <span>保存</span>
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
  <Footer class="foot"></Footer>
</template>

<script setup>

import Header from '../../../components/header/index'
import Footer from '../../../components/footer/index'
import {ElMessage} from "element-plus"
import {getUserIdKey} from "../../../utils/auth"
import {ref, reactive} from 'vue'
import {useRouter} from 'vue-router'
import useUserStore from '@/store/modules/user'
import {getAuthentication} from "../../../api/accountSettings";


const router = useRouter();
const userStore = useUserStore()
const authForm = ref({
  idNumber: '',
  relName: '',
  id: getUserIdKey()
})


const validateIdNumber = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入身份证号码'))
    return
  }
  const reg = /^[1-9]\d{5}(19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])\d{3}([0-9Xx])$/
  if (!reg.test(value)) {
    callback(new Error('身份证格式不正确'))
    return
  }
  const weights = [7,9,10,5,8,4,2,1,6,3,7,9,10,5,8,4,2]
  const codes = ['1','0','X','9','8','7','6','5','4','3','2']
  const sum = value.substring(0,17).split('').reduce((acc,cur,idx)=>acc + parseInt(cur,10) * weights[idx], 0)
  const code = codes[sum % 11]
  if (code !== value[17].toUpperCase()) {
    callback(new Error('身份证校验码错误'))
    return
  }
  callback()
}

const validateRelName = (rule, value, callback) => {
  const v = (value || '').trim()
  if (!v) {
    callback(new Error('请输入真实姓名'))
    return
  }
  const reg = /^[\u4e00-\u9fa5·]{2,20}$/
  if (!reg.test(v)) {
    callback(new Error('姓名需为2位以上中文'))
    return
  }
  callback()
}

const authRules = reactive({
  idNumber: [
    { validator: validateIdNumber, trigger: 'blur' }
  ],
  relName: [
    { validator: validateRelName, trigger: 'blur' }
  ]
})


function savePsd() {
  getAuthentication(authForm.value).then(response => {
    if (response.code == '0') {
      ElMessage({
        message: '保存成功',
        type: 'success',
      })
      router.push({ path: '/accountSettings/index' })

    } else {
      ElMessage({
        message: response.message,
        type: 'error',
      })
    }
  })
}
</script>

<style scoped lang="scss">
.auth-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 200px);
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding: 40px 20px;
}

.auth-card {
  display: flex;
  width: 900px;
  max-width: 100%;
  background: #ffffff;
  border-radius: 20px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  transition: transform 0.3s ease;

  &:hover {
    transform: translateY(-5px);
    box-shadow: 0 25px 50px rgba(0, 0, 0, 0.15);
  }
}

.card-left {
  flex: 1;
  position: relative;
  display: none;

  @media (min-width: 768px) {
    display: block;
  }

  .cover-image {
    width: 100%;
    height: 100%;
    object-fit: cover;
    display: block;
  }

  .image-overlay {
    position: absolute;
    bottom: 0;
    left: 0;
    width: 100%;
    padding: 30px;
    background: linear-gradient(to top, rgba(0,0,0,0.8) 0%, rgba(0,0,0,0) 100%);
    color: #ffffff;

    .overlay-title {
      font-size: 24px;
      margin-bottom: 10px;
      font-weight: 600;
    }

    .overlay-text {
      font-size: 14px;
      opacity: 0.9;
      line-height: 1.5;
    }
  }
}

.card-right {
  flex: 1;
  padding: 60px 50px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  background: #ffffff;
}

.form-header {
  margin-bottom: 40px;
  text-align: center;

  .title {
    font-size: 28px;
    color: #333;
    margin-bottom: 10px;
    font-weight: 700;
  }

  .subtitle {
    font-size: 14px;
    color: #999;
  }
}

.login-form {
  width: 100%;
}

:deep(.el-form-item__label) {
  text-align: left;
  padding-left: 0;
}

.custom-input {
  :deep(.el-input__wrapper) {
    box-shadow: 0 0 0 1px #e0e0e0 inset;
    border-radius: 8px;
    padding: 0 15px;
    background-color: #f9f9f9;
    transition: all 0.3s;

    &:hover {
      box-shadow: 0 0 0 1px #b0b0b0 inset;
    }

    &.is-focus {
      box-shadow: 0 0 0 2px var(--el-color-primary) inset !important;
      background-color: #fff;
    }
  }

  :deep(.el-input__inner) {
    height: 48px;
  }
}

.submit-btn {
  width: 100%;
  height: 48px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 1px;
  margin-top: 10px;
  background: linear-gradient(90deg, #4facfe 0%, #00f2fe 100%);
  border: none;
  transition: all 0.3s;

  &:hover {
    opacity: 0.9;
    transform: scale(1.02);
  }

  &:active {
    transform: scale(0.98);
  }
}

.input-item {
  margin-bottom: 32px;
}

.btn-item {
  margin-bottom: 0;
}

:deep(.el-form-item__error) {
  padding-top: 6px;
  line-height: 1.2;
}

@media (max-width: 768px) {
  .auth-card {
    flex-direction: column;
    width: 100%;
  }

  .card-left {
    height: 200px;
    display: block;
  }

  .card-right {
    padding: 40px 20px;
  }
}

.foot {
  margin-top: 0;
}
</style>
