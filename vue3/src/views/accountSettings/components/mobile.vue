<template>
  <Header></Header>
  <div class="mobile-binding-page">
    <div class="binding-card">
      <div class="card-left">
        <img src="https://p3-flow-imagex-sign.byteimg.com/tos-cn-i-a9rns2rl98/rc_gen_image/2e9e21437b93429193495e163f5a6123.jpeg~tplv-a9rns2rl98-image_raw_b.png?rcl=20251211175251B069ABB7A61497877AF0&rk3s=8e244e95&rrcfp=827586d3&x-expires=2080806786&x-signature=icpe%2FnmTS0GR%2BsBvpId%2FLe77ldg%3D"
         alt="Security" class="cover-image" />
        <div class="image-overlay">
          <h3 class="overlay-title">账户安全</h3>
          <p class="overlay-text">绑定手机号，提升账号安全性，享受便捷服务</p>
        </div>
      </div>
      <div class="card-right">
        <div class="form-header">
          <h2 class="title">绑定手机</h2>
          <p class="subtitle">请输入您要绑定的新手机号码</p>
        </div>
        <el-form ref="editMobileRef" :model="editMobileForm" :rules="editMobileRules" class="login-form" size="large">
          <template v-if="currentMobile">
            <div class="step-title">第一步：验证当前手机号</div>
            <el-form-item>
              <el-input
                v-model="currentMobile"
                class="custom-input"
                disabled
                placeholder="当前已绑定手机号"
                :prefix-icon="Iphone"
              ></el-input>
            </el-form-item>
            <el-form-item v-if="!realMobile">
              <el-input
                v-model="manualMobile"
                class="custom-input"
                placeholder="请输入当前手机号以进行验证"
                :prefix-icon="Iphone"
              ></el-input>
            </el-form-item>
            <el-form-item>
              <div class="sms-input-group">
                <el-input 
                  v-model="currentMobileCode" 
                  placeholder="请输入验证码" 
                  class="custom-input code-input"
                  :disabled="currentMobileVerified"
                >
                  <template #prepend>
                    <el-icon :size="20">
                      <Message />
                    </el-icon>
                  </template>
                </el-input>
                <el-button 
                  class="send-code-btn" 
                  :disabled="currentMobileCountdown > 0 || currentMobileVerified"
                  @click="handleSendCurrentMobileCode"
                >
                  {{ currentMobileCountdown > 0 ? `${currentMobileCountdown}s` : '获取验证码' }}
                </el-button>
              </div>
            </el-form-item>
            <el-form-item>
              <el-button 
                type="primary" 
                class="verify-btn"
                :class="{ 'is-verified': currentMobileVerified }"
                :disabled="!currentMobileCode || currentMobileVerified"
                @click="handleVerifyCurrentMobileCode"
              >
                {{ currentMobileVerified ? '当前手机号验证通过' : '验证当前手机号' }}
              </el-button>
            </el-form-item>
            <div class="step-divider"></div>
            <div class="step-title" :class="{ 'disabled': !currentMobileVerified }">第二步：绑定新手机号</div>
          </template>

          <el-form-item prop="mobile">
            <el-input
                v-model="editMobileForm.mobile"
                class="custom-input"
                :disabled="currentMobile && !currentMobileVerified"
                placeholder="请输入新的手机号"
                :prefix-icon="Iphone"
            ></el-input>
          </el-form-item>
          <el-form-item>
            <el-button
                type="primary"
                class="submit-btn"
                :loading="loading"
                :disabled="currentMobile && !currentMobileVerified"
                @click.prevent="savePsd"
            >
              立即绑定
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
import {getUserIdKey, getName} from "../../../utils/auth"
import {ref, reactive, onMounted} from 'vue'
import {useRouter} from 'vue-router'
import useUserStore from '@/store/modules/user'
import {getEditMobile} from "../../../api/accountSettings";
import { Iphone, Message } from '@element-plus/icons-vue'
import { getPersonInfoId } from '@/api/personInfo'
import { sendSmsCode, smsLogin } from '@/api/login'

const router = useRouter();
const userStore = useUserStore()
const loading = ref(false)
const editMobileForm = ref({
  mobile: '',
  id: getUserIdKey()
})

const currentMobile = ref('')
const currentMobileCode = ref('')
const currentMobileVerified = ref(false)
const currentMobileCountdown = ref(0)
let currentMobileCountdownTimer = null
const realMobile = ref('')
const manualMobile = ref('')

onMounted(() => {
  const id = getUserIdKey()
  if (id) {
    getPersonInfoId({id: id}).then(response => {
      if(response.code == '0' || response.code == 0) {
        currentMobile.value = response.data?.mobile || ''
        currentMobileVerified.value = !currentMobile.value
      }
    })
  }
  const name = getName()
  const reg = /^1[3-9]\d{9}$/
  if (name && reg.test(name)) {
    realMobile.value = name
  }
})

const validatePhone = (rule, value, callback) => {
  const reg = /^1[3-9]\d{9}$/;
  if (!value) {
    return callback(new Error('手机号码不能为空'));
  } else if (!reg.test(value)) {
    return callback(new Error('请输入正确的手机号码'));
  } else {
    callback();
  }
};
const editMobileRules = reactive({
      mobile: [{required: true, trigger: "blur", validator: validatePhone}]
    }
)

function savePsd() {
  const refForm = editMobileRef.value
  if (!refForm) return
  refForm.validate((valid) => {
    if (valid) {
      if (realMobile.value && editMobileForm.value.mobile === realMobile.value) {
        ElMessage({
          message: '新手机号不能与当前绑定的手机号一致',
          type: 'warning',
        })
        return
      }
      if (currentMobile.value && !currentMobileVerified.value) {
        ElMessage({
          message: '请先完成当前手机号验证码验证',
          type: 'warning',
        })
        return
      }
      loading.value = true
      getEditMobile(editMobileForm.value).then(response => {
        loading.value = false
        if (response.code == '0' || response.code == 0) {
          ElMessage({
            message: '绑定成功',
            type: 'success',
            duration: 2000
          })
          userStore.logOut().then(() => {
            location.href = '../../login';
          })
        } else {
          ElMessage({
            message: response.message || '绑定失败，请重试',
            type: 'error',
          })
        }
      }).catch(() => {
        loading.value = false
      })
    }
  })
}

const editMobileRef = ref(null)

function handleSendCurrentMobileCode() {
  const target = realMobile.value || manualMobile.value
  const reg = /^1[3-9]\d{9}$/
  if (!target || !reg.test(target)) {
    ElMessage({
      message: '无法识别当前手机号，请手动输入正确的手机号',
      type: 'warning',
    })
    return
  }
  sendSmsCode(target, 'login').then(response => {
    if (response.code == '0' || response.code == 0) {
      ElMessage({
        message: '验证码已发送,请注意查收',
        type: 'success',
      })
      currentMobileCountdown.value = 60
      if (currentMobileCountdownTimer) {
        clearInterval(currentMobileCountdownTimer)
        currentMobileCountdownTimer = null
      }
      currentMobileCountdownTimer = setInterval(() => {
        currentMobileCountdown.value--
        if (currentMobileCountdown.value <= 0) {
          clearInterval(currentMobileCountdownTimer)
          currentMobileCountdownTimer = null
        }
      }, 1000)
    } else {
      ElMessage({
        message: response.message || '验证码发送失败',
        type: 'error',
      })
    }
  }).catch((error) => {
    ElMessage({
      message: error?.response?.data?.message || error.message || '验证码发送失败',
      type: 'error',
    })
  })
}

function handleVerifyCurrentMobileCode() {
  const target = realMobile.value || manualMobile.value
  const reg = /^1[3-9]\d{9}$/
  if (!target || !reg.test(target)) {
    ElMessage({
      message: '请输入当前正确的手机号',
      type: 'warning',
    })
    return
  }
  if (!currentMobileCode.value) {
    ElMessage({
      message: '请输入验证码',
      type: 'warning',
    })
    return
  }
  smsLogin(target, currentMobileCode.value, '0001')
    .then(response => {
      if (response.code == '0' || response.code == 0) {
        currentMobileVerified.value = true
        ElMessage({
          message: '当前手机号验证通过',
          type: 'success',
        })
      } else {
        ElMessage({
          message: response.message || '验证码错误或已失效',
          type: 'error',
        })
      }
    })
    .catch((error) => {
      ElMessage({
        message: error?.response?.data?.message || error.message || '验证失败',
        type: 'error',
      })
    })
}
</script>

<style scoped lang="scss">
.mobile-binding-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 300px);
  background-color: #f5f7fa;
  padding: 40px 0;
}

.binding-card {
  display: flex;
  width: 900px;
  background: #ffffff;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease, box-shadow 0.3s ease;

  &:hover {
    transform: translateY(-5px);
    box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15);
  }
}

.card-left {
  flex: 0 0 400px;
  position: relative;
  overflow: hidden;

  .cover-image {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.5s ease;
  }
  
  &:hover .cover-image {
    transform: scale(1.05);
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
  background: linear-gradient(90deg, #ff4b2b 0%, #ff416c 100%);
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

.step-title {
  font-size: 14px;
  color: #333;
  margin: 15px 0 15px 0;
  font-weight: 600;
  padding-left: 8px;
  border-left: 3px solid #ff4b2b;
  line-height: 1.2;
  
  &.disabled {
    color: #999;
    border-left-color: #ccc;
  }
}

.step-divider {
  height: 1px;
  background-color: #f0f0f0;
  margin: 10px 0 20px;
}

.sms-input-group {
  display: flex;
  width: 100%;
  gap: 12px;
  align-items: center;
  
  .code-input {
    flex: 1;
  }
  
  .send-code-btn {
    width: 120px;
    height: 48px;
    border-radius: 8px;
    background-color: #fff;
    color: #ff4b2b;
    border: 1px solid #ff4b2b;
    font-size: 14px;
    transition: all 0.3s;
    
    &:hover:not(:disabled) {
      background-color: #fff5f5;
      opacity: 0.9;
    }
    
    &:disabled {
      background-color: #f5f7fa;
      color: #999;
      border-color: #dcdfe6;
    }
  }
}

.verify-btn {
  width: 100%;
  height: 48px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 1px;
  background: linear-gradient(90deg, #4facfe 0%, #00f2fe 100%);
  border: none;
  transition: all 0.3s;
  margin-top: -5px;

  &:hover:not(:disabled) {
    opacity: 0.9;
    transform: scale(1.02);
    box-shadow: 0 4px 12px rgba(79, 172, 254, 0.3);
  }
  
  &:active {
    transform: scale(0.98);
  }
  
  &.is-verified {
    background: linear-gradient(90deg, #67c23a 0%, #85ce61 100%);
    cursor: default;
    opacity: 0.8;
  }
  
  &:disabled:not(.is-verified) {
    background: #a0cfff;
    opacity: 0.7;
  }
}

/* Response for mobile */
@media (max-width: 768px) {
  .binding-card {
    flex-direction: column;
    width: 100%;
  }
  
  .card-left {
    height: 200px;
  }
  
  .card-right {
    padding: 40px 20px;
  }
}
</style>
