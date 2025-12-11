<template>
  <div class="app-container">
    <Header></Header>
    <div class="main">
      <div class="login">
        <div class="main-left">
          <img :src="bg" alt="">
        </div>
        <div class="main-right">
          <el-tabs
              v-model="activeName"
              type="card"
              class="demo-tabs"
              @tab-click="handleClick"
          >
            <el-tab-pane label="密码登录" name="first">
              <el-form ref="loginRef" :model="loginForm" :rules="loginRules" class="login-form" @submit.prevent="handleLogin">
                <div class="error-tips" v-if="isTips">
                  <WarningFilled style="width: 1em; height: 1em; margin-left: 8px;"/>
                  {{ tipsContent }}</div>
                <el-input v-model="userName" placeholder="请输入手机号或邮箱" prop="userName">
                  <template #prepend>
                    <el-icon :size="20">
                      <User/>
                    </el-icon>
                  </template>
                </el-input>
                <el-input type="password" show-password v-model="loginForm.password" placeholder="请输入密码"
                          prop="password">
                  <template #prepend>
                    <el-icon :size="20">
                      <Lock/>
                    </el-icon>
                  </template>
                </el-input>
                <el-button
                    :loading="loading"
                    size="large"
                    type="primary"
                    style="width:100%;"
                    class="btn"
                    native-type="submit"
                    @click.prevent="handleLogin"
                >
                  <span v-if="!loading">登 录</span>
                  <span v-else>登 录 中...</span>
                </el-button>
                <div v-show="experienceAccountFlag != 1" v-if="register" class="register">
                  <router-link class="link-type" :to="'/register'">立即注册</router-link>
                </div>
                <div v-show="experienceAccountFlag == 1" v-if="register" class="experienceAccount">
                  <a class="link-type" @click="getExperienceAccount">点击获取体验账号</a>
                </div>
              </el-form>
            </el-tab-pane>
            
            <el-tab-pane label="验证码登录" name="second">
              <el-form ref="smsLoginRef" :model="smsLoginForm" :rules="smsLoginRules" class="login-form" @submit.prevent="handleSmsLogin">
                <div class="error-tips" v-if="isSmsTips">
                  <WarningFilled style="width: 1em; height: 1em; margin-left: 8px;"/>
                  {{ smsTipsContent }}</div>
                <el-input v-model="smsLoginForm.mobile" placeholder="请输入手机号" prop="mobile">
                  <template #prepend>
                    <el-icon :size="20">
                      <Iphone/>
                    </el-icon>
                  </template>
                </el-input>
                <div class="sms-code-input">
                  <el-input v-model="smsLoginForm.smsCode" placeholder="请输入验证码" prop="smsCode">
                    <template #prepend>
                      <el-icon :size="20">
                        <Message/>
                      </el-icon>
                    </template>
                  </el-input>
                  <el-button 
                      class="sms-code-btn" 
                      :disabled="countdown > 0"
                      @click="handleSendSmsCode"
                  >
                    {{ countdown > 0 ? `${countdown}秒后重试` : '获取验证码' }}
                  </el-button>
                </div>
                <el-button
                    :loading="smsLoading"
                    size="large"
                    type="primary"
                    style="width:100%;"
                    class="btn"
                    native-type="submit"
                    @click.prevent="handleSmsLogin"
                >
                  <span v-if="!smsLoading">登 录</span>
                  <span v-else>登 录 中...</span>
                </el-button>
                <div v-show="experienceAccountFlag != 1" v-if="register" class="register">
                  <router-link class="link-type" :to="'/register'">立即注册</router-link>
                </div>
                <div v-show="experienceAccountFlag == 1" v-if="register" class="experienceAccount">
                  <a class="link-type" @click="getExperienceAccount">点击获取体验账号</a>
                </div>
              </el-form>
            </el-tab-pane>
            
            <el-tab-pane label="邮箱登录" name="third">
              <el-form ref="emailLoginRef" :model="emailLoginForm" :rules="emailLoginRules" class="login-form" @submit.prevent="handleEmailLogin">
                <div class="error-tips" v-if="isEmailTips">
                  <WarningFilled style="width: 1em; height: 1em; margin-left: 8px;"/>
                  {{ emailTipsContent }}</div>
                <el-input v-model="emailLoginForm.email" placeholder="请输入邮箱" prop="email">
                  <template #prepend>
                    <el-icon :size="20">
                      <Message/>
                    </el-icon>
                  </template>
                </el-input>
                <div class="sms-code-input">
                  <el-input v-model="emailLoginForm.emailCode" placeholder="请输入验证码" prop="emailCode">
                    <template #prepend>
                      <el-icon :size="20">
                        <Message/>
                      </el-icon>
                    </template>
                  </el-input>
                  <el-button 
                      class="sms-code-btn" 
                      :disabled="emailCountdown > 0"
                      @click="handleSendEmailCode"
                  >
                    {{ emailCountdown > 0 ? `${emailCountdown}秒后重试` : '获取验证码' }}
                  </el-button>
                </div>
                <el-button
                    :loading="emailLoading"
                    size="large"
                    type="primary"
                    style="width:100%;"
                    class="btn"
                    native-type="submit"
                    @click.prevent="handleEmailLogin"
                >
                  <span v-if="!emailLoading">登 录</span>
                  <span v-else>登 录 中...</span>
                </el-button>
                <div v-show="experienceAccountFlag != 1" v-if="register" class="register">
                  <router-link class="link-type" :to="'/register'">立即注册</router-link>
                </div>
                <div v-show="experienceAccountFlag == 1" v-if="register" class="experienceAccount">
                  <a class="link-type" @click="getExperienceAccount">点击获取体验账号</a>
                </div>
              </el-form>
            </el-tab-pane>
            
          </el-tabs>
        </div>
      </div>
    </div>
    <Footer></Footer>
    <el-dialog
        v-model="stateOpen"
        title="体验账号"
        width="500"

    >
      <div class="wrapper">
        <h2 class="tip-text">
          {{
            `扫码二维码关注后回复: 票务平台     获取体验账号`
          }}
        </h2>
        <img
            class="qrcode-image"
            :src="wechatOfficialAccount"
            alt="微信公众号"
        />
        <div class="dialog-footer">
          <el-button class="experienceAccountConfirm" @click="stateOpen = false">确定</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import wechatOfficialAccount from '@/assets/section/wechatOfficialAccount.jpg'
import bg from '@/assets/section/javaup1.jpg'
import Header from '@/components/header/index'
import Footer from '@/components/footer/index'
import {isPhoneNumber, isEmailAddress} from '@/utils/index'
import {ref, getCurrentInstance, inject} from 'vue'
import useUserStore from '@/store/modules/user'
import {useRouter} from 'vue-router'
import {sendSmsCode, smsLogin, sendEmailCode, emailCodeLogin} from '@/api/login'
import {ElMessage, ElMessageBox} from 'element-plus'
import {setToken, setUserIdKey, setName} from '@/utils/auth'

//体验账号标识
const experienceAccountFlag = ref(import.meta.env.VITE_EXPERIENCE_ACCOUNT_FLAG);
//获取体验账号弹出框
const stateOpen = ref(false)
const userStore = useUserStore()
const router = useRouter();
const loading = ref(false);
const activeName = ref('first')
// 注册开关
const register = ref(true);
const isTips = ref(false)
const tipsContent = ref('')
const {proxy} = getCurrentInstance();

const userName = ref('');
const loginForm = ref({
  email: '',
  mobile: '13212345678',
  password: '',
  code: '0001'//pc网站
})

const loginRules = ref({});

// 短信验证码登录相关
const smsLoading = ref(false);
const isSmsTips = ref(false);
const smsTipsContent = ref('');
const countdown = ref(0);
let countdownTimer = null;

const smsLoginForm = ref({
  mobile: '',
  smsCode: '',
  code: '0001'//pc网站
})

// 手机号验证规则
const validateMobile = (rule, value, callback) => {
  const reg = /^1[3-9]\d{9}$/;
  if (!value) {
    return callback(new Error('手机号不能为空'));
  } else if (!reg.test(value)) {
    return callback(new Error('请输入正确的手机号'));
  } else {
    callback();
  }
};

const smsLoginRules = ref({
  mobile: [{required: true, trigger: "blur", validator: validateMobile}],
  smsCode: [{required: true, message: '请输入验证码', trigger: 'blur'}]
});



// 邮箱验证码登录相关
const emailLoading = ref(false);
const isEmailTips = ref(false);
const emailTipsContent = ref('');
const emailCountdown = ref(0);
let emailCountdownTimer = null;

const emailLoginForm = ref({
  email: '',
  emailCode: '',
  code: '0001'//pc网站
})

const emailLoginRules = ref({
  email: [{required: true, message: '请输入邮箱', trigger: 'blur'}],
  emailCode: [{required: true, message: '请输入验证码', trigger: 'blur'}]
});
const handleClick = (tab, event) => {
  console.log(tab, event)
}
const handleLogin = () => {
  proxy.$refs.loginRef.validate(valid => {
    if (valid) {
      if (userName.value == '') {
        isTips.value = true
        tipsContent.value = '请输入邮箱或者手机号'
      } else if (loginForm.value.password == '') {
        tipsContent.value = '请输入密码'
        isTips.value = true
      }else{
        isTips.value = false
        //正则匹配(手机号还是邮箱涉及到传参)
        identifyType(userName.value)
        // 调用action的登录方法
        userStore.login(loginForm.value).then(() => {
          router.push({path: "/"});
        }).catch(() => {
          loading.value = false;
        });
      }

    }
  });
}


function identifyType(value) {
  if (isPhoneNumber(value)) {
    loginForm.value.mobile = value
    return loginForm.value.mobile;
  } else if (isEmailAddress(value)) {
    loginForm.value.email = value
    return loginForm.value.email;
  }
}

function getExperienceAccount(){
  console.log('getExperienceAccount')
  stateOpen.value = true
}

// 发送短信验证码
function handleSendSmsCode() {
  // 验证手机号
  if (!smsLoginForm.value.mobile) {
    isSmsTips.value = true;
    smsTipsContent.value = '请输入手机号';
    return;
  }
  
  const reg = /^1[3-9]\d{9}$/;
  if (!reg.test(smsLoginForm.value.mobile)) {
    isSmsTips.value = true;
    smsTipsContent.value = '请输入正确的手机号';
    return;
  }
  
  isSmsTips.value = false;
  
  // 发送验证码
  sendSmsCode(smsLoginForm.value.mobile, 'login').then(response => {
    if (response.code == '0') {
      ElMessage({
        message: '验证码已发送,请注意查收',
        type: 'success',
      });
      
      // 开始倒计时
      countdown.value = 60;
      countdownTimer = setInterval(() => {
        countdown.value--;
        if (countdown.value <= 0) {
          clearInterval(countdownTimer);
          countdownTimer = null;
        }
      }, 1000);
    }
  }).catch((error) => {
    isSmsTips.value = true;
    smsTipsContent.value = error.message || '验证码发送失败';
  });
}

// 短信验证码登录
function handleSmsLogin() {
  proxy.$refs.smsLoginRef.validate(valid => {
    if (valid) {
      if (!smsLoginForm.value.mobile) {
        isSmsTips.value = true;
        smsTipsContent.value = '请输入手机号';
        return;
      }
      
      if (!smsLoginForm.value.smsCode) {
        isSmsTips.value = true;
        smsTipsContent.value = '请输入验证码';
        return;
      }
      
      isSmsTips.value = false;
      smsLoading.value = true;
      
      smsLogin(smsLoginForm.value.mobile, smsLoginForm.value.smsCode, smsLoginForm.value.code)
        .then(response => {
          if (response.code == '0') {
            // 保存用户信息
            const userInfo = response.data;
            setToken(userInfo.token);
            setUserIdKey(userInfo.userId);
            setName(smsLoginForm.value.mobile);
            userStore.token = userInfo.token;
            userStore.userId = userInfo.userId;
            
            ElMessage({
              message: '登录成功',
              type: 'success',
            });
            
            router.push({path: "/"});
          } else {
            const errCode = response.code;
            const errMsg = response.message || '登录失败';
            if (errCode == 70009) {
              ElMessageBox.confirm('该手机号尚未注册，请先注册后再登录', '提示', {
                confirmButtonText: '去注册',
                cancelButtonText: '取消',
                type: 'warning',
                center: true,
                showClose: true,
                closeOnClickModal: false,
                customClass: 'unregistered-dialog'
              }).then(() => {
                router.push({path: "/register"});
              }).catch(() => {});
            } else {
              isSmsTips.value = true;
              smsTipsContent.value = errMsg;
            }
          }
        })
        .catch((error) => {
          const errMsg = error?.response?.data?.message || error.message || '登录失败';
          isSmsTips.value = true;
          smsTipsContent.value = errMsg;
        })
        .finally(() => {
          smsLoading.value = false;
        });
    }
  });
}

// 发送邮箱验证码
function handleSendEmailCode() {
  if (!emailLoginForm.value.email) {
    isEmailTips.value = true;
    emailTipsContent.value = '请输入邮箱';
    return;
  }
  isEmailTips.value = false;
  sendEmailCode(emailLoginForm.value.email, 'login').then(response => {
    if (response.code == '0' || response.code == 0) {
      ElMessage({
        message: '验证码已发送,请注意查收',
        type: 'success',
      });
      emailCountdown.value = 60;
      emailCountdownTimer = setInterval(() => {
        emailCountdown.value--;
        if (emailCountdown.value <= 0) {
          clearInterval(emailCountdownTimer);
          emailCountdownTimer = null;
        }
      }, 1000);
    }
  }).catch((error) => {
    isEmailTips.value = true;
    emailTipsContent.value = error.message || '验证码发送失败';
  });
}

// 邮箱验证码登录
function handleEmailLogin() {
  proxy.$refs.emailLoginRef.validate(valid => {
    if (valid) {
      if (!emailLoginForm.value.email) {
        isEmailTips.value = true;
        emailTipsContent.value = '请输入邮箱';
        return;
      }
      if (!emailLoginForm.value.emailCode) {
        isEmailTips.value = true;
        emailTipsContent.value = '请输入验证码';
        return;
      }
      isEmailTips.value = false;
      emailLoading.value = true;
      emailCodeLogin(emailLoginForm.value.email, emailLoginForm.value.emailCode, emailLoginForm.value.code)
        .then(response => {
          if (response.code == '0' || response.code == 0) {
            const userInfo = response.data;
            setToken(userInfo.token);
            setUserIdKey(userInfo.userId);
            setName(emailLoginForm.value.email);
            userStore.token = userInfo.token;
            userStore.userId = userInfo.userId;
            ElMessage({
              message: '登录成功',
              type: 'success',
            });
            router.push({path: "/"});
          } else {
            const errMsg = response.message || '登录失败';
            isEmailTips.value = true;
            emailTipsContent.value = errMsg;
          }
        })
        .catch((error) => {
          const errMsg = error?.response?.data?.message || error.message || '登录失败';
          isEmailTips.value = true;
          emailTipsContent.value = errMsg;
        })
        .finally(() => {
          emailLoading.value = false;
        });
    }
  });
}

</script>

<style scoped lang="scss">
.app-container {
  width: 100%;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f0f2f5;

  .main {
    flex: 1;
    width: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
    background: linear-gradient(135deg, #17073d 0%, #361066 100%);
    position: relative;
    overflow: hidden;

    // 添加一些装饰性背景元素
    &::before {
      content: '';
      position: absolute;
      top: -100px;
      left: -100px;
      width: 400px;
      height: 400px;
      background: radial-gradient(circle, rgba(255,255,255,0.05) 0%, rgba(255,255,255,0) 70%);
      border-radius: 50%;
    }

    &::after {
      content: '';
      position: absolute;
      bottom: -50px;
      right: -50px;
      width: 300px;
      height: 300px;
      background: radial-gradient(circle, rgba(255,255,255,0.05) 0%, rgba(255,255,255,0) 70%);
      border-radius: 50%;
    }

    .login {
      width: 1200px;
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 0 20px;
      z-index: 1;

      .main-left {
        flex: 1;
        display: flex;
        justify-content: center;
        align-items: center;
        padding-right: 70px;
        
        img {
          max-width: 100%;
          height: auto;
          object-fit: contain;
          filter: drop-shadow(0 10px 20px rgba(0,0,0,0.3));
          animation: float 6s ease-in-out infinite;
        }
      }

      .main-right {
        width: 420px;
        background: rgba(255, 255, 255, 0.98);
        border-radius: 16px;
        box-shadow: 0 16px 40px rgba(0, 0, 0, 0.25);
        padding: 40px;
        box-sizing: border-box;
        transition: transform 0.3s ease;

        &:hover {
          transform: translateY(-5px);
        }
      }
    }
  }
}

@keyframes float {
  0% { transform: translateY(0px); }
  50% { transform: translateY(-15px); }
  100% { transform: translateY(0px); }
}

.register {
  margin-top: 15px;
  text-align: right;
  
  a {
    font-size: 14px;
    color: #ff371d;
    text-decoration: none;
    transition: color 0.3s;
    
    &:hover {
      color: #d62d16;
      text-decoration: underline;
    }
  }
}

.experienceAccount {
  margin-top: 15px;
  text-align: right;
  
  a {
    font-size: 14px;
    color: #ff371d;
    text-decoration: none;
    cursor: pointer;
    
    &:hover {
      text-decoration: underline;
    }
  }
}

:deep(.demo-tabs > .el-tabs__content) {
  padding: 20px 0 0;
}

:deep(.el-tabs__nav-scroll) {
  width: 100%;
  display: flex;
  justify-content: center;
}

:deep(.el-tabs__nav) {
  width: 100%;
  display: flex;
  justify-content: space-around;
}

:deep(.el-tabs__item) {
  font-size: 18px;
  color: #666;
  height: 50px;
  line-height: 50px;
  font-weight: 500;
  
  &.is-active {
    color: #ff371d;
    font-weight: 600;
  }
  
  &:hover {
    color: #ff371d;
  }
}

:deep(.el-tabs__active-bar) {
  background-color: #ff371d;
  height: 3px;
  border-radius: 3px;
}

// 隐藏原有card模式的边框，改用默认样式或自定义
:deep(.el-tabs--card > .el-tabs__header) {
  border-bottom: 1px solid #eee;
  margin-bottom: 20px;
}

:deep(.el-tabs--card > .el-tabs__header .el-tabs__nav) {
  border: none;
}

:deep(.el-tabs--card > .el-tabs__header .el-tabs__item) {
  border: none;
  background: transparent;
}

:deep(.el-input-group__prepend) {
  background-color: #f5f7fa;
  padding: 0 15px;
  
  .el-icon {
    color: #909399;
  }
}

.el-input {
  margin-bottom: 24px;
  
  :deep(.el-input__wrapper) {
    box-shadow: 0 0 0 1px #dcdfe6 inset;
    padding: 1px 11px;
    background-color: #fff;
    
    &.is-focus {
      box-shadow: 0 0 0 1px #ff371d inset !important;
    }
  }
  
  :deep(.el-input__inner) {
    height: 44px;
  }
}

.sms-code-input {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
  
  .el-input {
    flex: 1;
    margin-bottom: 0;
  }
  
  .sms-code-btn {
    width: 130px;
    height: 46px;
    border-radius: 23px;
    font-size: 14px;
    font-weight: 500;
    background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
    border: none;
    color: #fff;
    transition: all 0.3s;
    white-space: nowrap;
    
    &:hover:not(:disabled) {
      transform: translateY(-2px);
      box-shadow: 0 6px 16px rgba(102, 126, 234, 0.4);
      background: linear-gradient(90deg, #5568d3 0%, #6a3f8f 100%);
    }
    
    &:disabled {
      background: #e0e0e0;
      color: #999;
      cursor: not-allowed;
    }
  }
}

.btn {
  background: linear-gradient(90deg, #ff371d 0%, #ff6b5a 100%);
  border: none;
  border-radius: 25px;
  font-size: 18px;
  height: 48px;
  font-weight: 500;
  letter-spacing: 2px;
  transition: all 0.3s;
  box-shadow: 0 6px 16px rgba(255, 55, 29, 0.25);
  margin-top: 10px;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 20px rgba(255, 55, 29, 0.35);
    background: linear-gradient(90deg, #ff2a0e 0%, #ff5e4d 100%);
  }
  
  &:active {
    transform: translateY(0);
  }
}



.error-tips {
  border-radius: 4px;
  background: #fef0f0;
  color: #f56c6c;
  border: 1px solid #fde2e2;
  margin-bottom: 20px;
  padding: 10px 15px;
  display: flex;
  align-items: center;
  font-size: 13px;
  
  .el-icon {
    margin-right: 8px;
    margin-left: 0 !important;
  }
}

.wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 30px;
  
  .tip-text {
    font-size: 18px;
    color: #303133;
    margin-bottom: 20px;
    font-weight: 500;
  }
  
  .qrcode-image {
    width: 200px;
    height: 200px;
    border-radius: 8px;
    box-shadow: 0 4px 12px rgba(0,0,0,0.1);
    margin-bottom: 30px;
  }
  
  .experienceAccountConfirm {
    width: 120px;
    height: 40px;
    font-size: 16px;
    border-radius: 20px;
  }
}
</style>

<style lang="scss">
.unregistered-dialog {
  border-radius: 16px !important;
  padding-bottom: 20px !important;
  border: none !important;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.3) !important;
  
  .el-message-box__header {
    padding-top: 20px;
  }

  .el-message-box__title {
    font-size: 20px;
    font-weight: 600;
    color: #1f2d3d;
  }

  .el-message-box__content {
    padding: 20px 20px;
    font-size: 16px;
    color: #5e6d82;
    line-height: 1.6;
  }

  .el-message-box__btns {
    padding-top: 10px;
    display: flex;
    justify-content: center;
    gap: 15px; 
    
    .el-button {
      margin-left: 0; 
    }
  }

  // Primary Button (Go Register)
  .el-button--primary {
    background: linear-gradient(135deg, #ff371d 0%, #ff6b5a 100%);
    border: none;
    border-radius: 24px;
    font-weight: 600;
    font-size: 15px;
    padding: 12px 32px;
    height: auto;
    box-shadow: 0 8px 16px rgba(255, 55, 29, 0.25);
    transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
    
    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 12px 20px rgba(255, 55, 29, 0.35);
      background: linear-gradient(135deg, #ff2a0e 0%, #ff5e4d 100%);
    }
    
    &:active {
      transform: scale(0.98);
    }
  }

  // Cancel Button
  .el-button:not(.el-button--primary) {
    background: #f2f3f5;
    color: #606266;
    border: 1px solid transparent;
    border-radius: 24px;
    font-weight: 500;
    font-size: 15px;
    padding: 12px 32px;
    height: auto;
    transition: all 0.3s;
    
    &:hover {
      background: #e6e8eb;
      color: #303133;
      transform: translateY(-1px);
    }
    
    &:active {
      transform: scale(0.98);
    }
  }
}
</style>
