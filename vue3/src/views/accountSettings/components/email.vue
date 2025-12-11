<template>
  <Header></Header>
  <div class="email-binding-page">
    <div class="binding-card">
      <div class="card-left">
        <img src="https://img.ixintu.com/upload/jpg/20210526/03877612b791e3d356f79c8a20ef9d96_62474_800_800.jpg!con" alt="Security" class="cover-image" />
        <div class="image-overlay">
          <h3 class="overlay-title">账户安全</h3>
          <p class="overlay-text">绑定邮箱，及时接收票务提醒与安全通知</p>
        </div>
      </div>
      <div class="card-right">
        <div class="form-header">
          <h2 class="title">绑定邮箱</h2>
          <p class="subtitle">为了您的账号安全，请输入真实的邮箱地址</p>
        </div>
        <el-form ref="editEmailRef" :model="editEmailForm" :rules="editEmailRules" class="login-form" size="large">
          <template v-if="currentEmail">
            <div class="step-title">第一步：验证当前邮箱</div>
            <el-form-item>
              <el-input
                v-model="currentEmail"
                class="custom-input"
                disabled
                placeholder="当前已绑定邮箱"
                :prefix-icon="Message"
              ></el-input>
            </el-form-item>
            <el-form-item>
              <div class="sms-input-group">
                <el-input 
                  v-model="currentEmailCode" 
                  placeholder="请输入验证码" 
                  class="custom-input code-input"
                  :disabled="currentEmailVerified"
                >
                  <template #prepend>
                    <el-icon :size="20">
                      <Message />
                    </el-icon>
                  </template>
                </el-input>
                <el-button 
                  class="send-code-btn" 
                  :disabled="currentEmailCountdown > 0 || currentEmailVerified"
                  @click="handleSendCurrentEmailCode"
                >
                  {{ currentEmailCountdown > 0 ? `${currentEmailCountdown}s` : '获取验证码' }}
                </el-button>
              </div>
            </el-form-item>
            
            <el-form-item>
              <el-button 
                type="primary" 
                class="verify-btn"
                :class="{ 'is-verified': currentEmailVerified }"
                :disabled="!currentEmailCode || currentEmailVerified"
                @click="handleVerifyCurrentEmailCode"
              >
                {{ currentEmailVerified ? '当前邮箱验证通过' : '验证当前邮箱' }}
              </el-button>
            </el-form-item>
            
            <div class="step-divider"></div>
            <div class="step-title" :class="{ 'disabled': !currentEmailVerified }">第二步：绑定新邮箱</div>
          </template>

          <el-form-item prop="email">
            <el-input
              v-model="editEmailForm.email"
              class="custom-input"
              :disabled="currentEmail && !currentEmailVerified"
              placeholder="请输入新的邮箱地址"
              :prefix-icon="Message"
            ></el-input>
          </el-form-item>
          
          <el-form-item>
            <el-button
              type="primary"
              class="submit-btn"
              :loading="loading"
              :disabled="currentEmail && !currentEmailVerified"
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
import { getEditPsd } from '@/api/accountSettings'
import { getPersonInfoId } from '@/api/personInfo'
import { ElMessage } from "element-plus"
import { getUserIdKey } from "../../../utils/auth"
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import useUserStore from '@/store/modules/user'
import { getEditEmail } from "../../../api/accountSettings";
import { Message } from '@element-plus/icons-vue'
import { sendEmailCode, emailCodeLogin } from '@/api/login'

const router = useRouter();
const userStore = useUserStore()
const loading = ref(false)
const editEmailForm = ref({
  email: '',
  id: getUserIdKey()
})

const currentEmail = ref('')
const currentEmailCode = ref('')
const currentEmailVerified = ref(false)
const currentEmailCountdown = ref(0)
let currentEmailCountdownTimer = null

onMounted(() => {
  const id = getUserIdKey()
  if (id) {
    getPersonInfoId({id: id}).then(response => {
      if(response.code == '0' && response.data) {
        currentEmail.value = response.data.email
        currentEmailVerified.value = !currentEmail.value
      }
    })
  }
})

const editEmailRules = reactive({
  email: [
    { required: true, message: '请填写邮箱', trigger: ['blur', 'change'] },
    { type: 'email', message: '邮箱格式不正确', trigger: ['blur', 'change'] }
  ]
})

const editEmailRef = ref(null)

function savePsd() {
  if (!editEmailRef.value) return
  
  editEmailRef.value.validate((valid) => {
    if (valid) {
      if (currentEmail.value && editEmailForm.value.email === currentEmail.value) {
        ElMessage({
          message: '新邮箱不能与当前绑定的邮箱一致',
          type: 'warning',
        })
        return
      }
      if (currentEmail.value && !currentEmailVerified.value) {
        ElMessage({
          message: '请先完成当前邮箱验证码验证',
          type: 'warning',
        })
        return
      }
      loading.value = true
      getEditEmail(editEmailForm.value).then(response => {
        loading.value = false
        if (response.code == '0') {
          ElMessage({
            message: '邮箱绑定成功',
            type: 'success',
            duration: 2000
          })
          // 绑定成功后返回上一页
          setTimeout(() => {
            router.go(-1)
          }, 1500)
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

function handleSendCurrentEmailCode() {
  if (!currentEmail.value) {
    ElMessage({
      message: '当前未绑定邮箱，无需验证',
      type: 'warning',
    })
    return
  }
  sendEmailCode(currentEmail.value, 'login').then(response => {
    if (response.code == '0' || response.code == 0) {
      ElMessage({
        message: '验证码已发送,请注意查收',
        type: 'success',
      })
      currentEmailCountdown.value = 60
      if (currentEmailCountdownTimer) {
        clearInterval(currentEmailCountdownTimer)
        currentEmailCountdownTimer = null
      }
      currentEmailCountdownTimer = setInterval(() => {
        currentEmailCountdown.value--
        if (currentEmailCountdown.value <= 0) {
          clearInterval(currentEmailCountdownTimer)
          currentEmailCountdownTimer = null
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

function handleVerifyCurrentEmailCode() {
  if (!currentEmail.value) return
  if (!currentEmailCode.value) {
    ElMessage({
      message: '请输入验证码',
      type: 'warning',
    })
    return
  }
  emailCodeLogin(currentEmail.value, currentEmailCode.value, '0001')
    .then(response => {
      if (response.code == '0' || response.code == 0) {
        currentEmailVerified.value = true
        ElMessage({
          message: '当前邮箱验证通过',
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
.email-binding-page {
  min-height: calc(100vh - 200px); /* Adjust based on header/footer height */
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding: 40px 20px;
}

.binding-card {
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
  display: none; // Hidden on very small screens if needed, but flex: 1 typically wraps or shrinks
  
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
  background: linear-gradient(90deg, #ff4b2b 0%, #ff416c 100%); /* Damai-like reddish gradient */
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
  margin-top: -5px; // 微调与上方输入框的间距

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
</style>
