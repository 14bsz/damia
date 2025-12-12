<template>
  <Header></Header>
  <div class="password-update-page">
    <div class="update-card">
      <div class="card-left">
        <img src="https://p3-flow-imagex-sign.byteimg.com/tos-cn-i-a9rns2rl98/rc_gen_image/22890516845743c59843b94269bf4da5.jpeg~tplv-a9rns2rl98-image_raw_b.png?rcl=20251212090637BB6728625E8DF106A4A0&rk3s=8e244e95&rrcfp=827586d3&x-expires=2080861612&x-signature=LpbrcM0tgqO7oeXy%2B5zaXha1%2FK8%3D" 
             alt="Security" class="cover-image" />
        <div class="image-overlay">
          <h3 class="overlay-title">安全中心</h3>
          <p class="overlay-text">定期修改密码，有效保护您的账号安全与隐私</p>
        </div>
      </div>
      <div class="card-right">
        <div class="form-header">
          <h2 class="title">修改密码</h2>
          <p class="subtitle">建议使用字母、数字和符号的组合，提高密码强度</p>
        </div>
        <el-form ref="editPsdRef" :model="editPsdForm" :rules="editPsdRules" class="login-form" size="large">
          <el-form-item prop="oldPassword" class="input-item">
            <el-input
                v-model="editPsdForm.oldPassword"
                class="custom-input"
                type="password"
                show-password
                placeholder="请输入旧密码"
                :prefix-icon="Lock"
            > </el-input>
          </el-form-item>
          <el-form-item prop="password" class="input-item">
            <el-input
                v-model="editPsdForm.password"
                class="custom-input"
                type="password"
                show-password
                placeholder="请输入新密码"
                :prefix-icon="Lock"
            > </el-input>
          </el-form-item>
          <el-form-item prop="confirmPassword" class="input-item">
            <el-input
                v-model="editPsdForm.confirmPassword"
                class="custom-input"
                type="password"
                show-password
                placeholder="请再次输入新密码"
                :prefix-icon="Lock"
            > </el-input>
          </el-form-item>
          
          <el-form-item class="btn-item">
            <el-button
                type="primary"
                class="submit-btn"
                @click.prevent="savePsd"
            >
              确认修改
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
import {getEditPsd} from '@/api/accountSettings'
import {ElMessage} from "element-plus"
import {getUserIdKey} from "../../../utils/auth"
import {ref, reactive, getCurrentInstance} from 'vue'
import {useRouter} from 'vue-router'
import useUserStore from '@/store/modules/user'
import { Lock } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()
const router = useRouter();
const userStore = useUserStore()
const editPsdForm = ref({
  oldPassword: '',
  password: '',
  confirmPassword: '',
  id: getUserIdKey()
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'));
  } else if (value !== editPsdForm.value.password) {
    callback(new Error('两次输入密码不一致!'));
  } else {
    callback();
  }
};

const validateOldNewPassword = (rule, value, callback) => {
  if (value && value === editPsdForm.value.oldPassword) {
    callback(new Error('新密码不能与旧密码一致!'));
  } else {
    callback();
  }
};

const editPsdRules = reactive({
  oldPassword: [
    { required: true, message: '请输入旧密码', trigger: 'blur' }
  ],
  password: [
    {
      required: true,
      pattern: /^(?![\d]+$)(?![a-zA-Z]+$)(?![^\da-zA-Z]+$)([^\u4e00-\u9fa5\s]){6,20}$/,
      message: '6-20位英文字母、数字或者符号（除空格），且字母、数字和标点符号至少包含两种',
      trigger: ['blur', 'focus']
    },
    { validator: validateOldNewPassword, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ]
})


function savePsd() {
  proxy.$refs.editPsdRef.validate((valid) => {
    if (valid) {
      getEditPsd(editPsdForm.value).then(response=>{
        if(response.code == '0'){
          ElMessage({
            message: '保存成功',
            type: 'success',
          })

          userStore.logOut().then(() => {
            location.href = '../../login';
          })

        }else{
          ElMessage({
            message: response.message,
            type: 'error',
          })
        }
      })
    }
  })
}
</script>

<style scoped lang="scss">
.password-update-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 200px);
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding: 40px 20px;
}

.update-card {
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

.input-item {
  margin-bottom: 32px; /* 预留错误提示空间 */
}

.btn-item {
  margin-bottom: 0;
}

:deep(.el-form-item__error) {
  padding-top: 6px;
  line-height: 1.2;
}

/* Response for mobile */
@media (max-width: 768px) {
  .update-card {
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
</style>
