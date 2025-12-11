import request from '@/utils/request'
 /**
 *登录接口

 * @param password 密码
 * @param code 渠道code 0001(pc网站)
 * @param email   用户邮箱(手机号任选其一)
 * @param mobile    用户手机号(邮箱任选其一)
 * @returns {*}
 */
export function login(email, mobile, password, code) {
    const data = {
        email,
        mobile,
        password,
        code
    }
    return request({
        url: '/damai/user/user/login',
        method: 'post',
        data: data
    })
}

 /**
  * 退出接口
  * @param code
  * @param token
  * @returns {*}
  */
 export function logout(code,token) {
     const data = {
         token,
         code
     }
     return request({
         url: '/damai/user/user/logout',
         method: 'post',
         data:data
     })
 }

 /**
  * 检查是否需要验证码
  * @returns {*}
  */
 export function isCaptcha(){
     return request({
         url: '/damai/user/user/captcha/check/need',
         method: 'post'
     })
 }



 export function register(data){
     return request({
         url: '/damai/user/user/register',
         method: 'post',
         data:data
     })
 }

/**
 * 发送短信验证码
 * @param mobile 手机号
 * @param type 验证码类型 login-登录, register-注册
 * @returns {*}
 */
export function sendSmsCode(mobile, type) {
    const data = {
        mobile,
        type
    }
    return request({
        url: '/damai/user/user/send/sms/code',
        method: 'post',
        data: data
    })
}

/**
 * 短信验证码登录
 * @param mobile 手机号
 * @param smsCode 短信验证码
 * @param code 渠道code 0001(pc网站)
 * @returns {*}
 */
export function smsLogin(mobile, smsCode, code) {
    const data = {
        mobile,
        smsCode,
        code
    }
    return request({
        url: '/damai/user/user/sms/login',
        method: 'post',
        data: data
    })
}

/**
 * 发送邮箱验证码
 * @param email 邮箱地址
 * @returns {*}
 */
export function sendEmailCode(email, type = 'login') {
    const data = {
        email,
        type
    }
    return request({
        url: '/damai/user/api/auth/sendEmailCode',
        method: 'post',
        data: data
    })
}

/**
 * 邮箱验证码登录
 * @param email 邮箱地址
 * @param emailCode 邮箱验证码
 * @param code 渠道code 0001(pc网站)
 * @returns {*}
 */
export function emailCodeLogin(email, emailCode, code) {
    const data = {
        email,
        emailCode,
        code
    }
    return request({
        url: '/damai/user/api/auth/loginByEmailCode',
        method: 'post',
        data: data
    })
}
