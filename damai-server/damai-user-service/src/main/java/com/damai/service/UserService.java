package com.damai.service;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baidu.fsg.uid.UidGenerator;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.damai.client.BaseDataClient;
import com.damai.common.ApiResponse;
import com.damai.core.RedisKeyManage;
import com.damai.dto.GetChannelDataByCodeDto;
import com.damai.dto.UserAuthenticationDto;
import com.damai.dto.UserExistDto;
import com.damai.dto.UserGetAndTicketUserListDto;
import com.damai.dto.UserIdDto;
import com.damai.dto.UserLoginDto;
import com.damai.dto.UserLogoutDto;
import com.damai.dto.UserMobileDto;
import com.damai.dto.UserRegisterDto;
import com.damai.dto.UserUpdateDto;
import com.damai.dto.UserUpdateEmailDto;
import com.damai.dto.UserUpdateMobileDto;
import com.damai.dto.UserUpdatePasswordDto;
import com.damai.dto.SendSmsCodeDto;
import com.damai.dto.UserSmsLoginDto;
import com.damai.entity.TicketUser;
import com.damai.entity.User;
import com.damai.entity.UserEmail;
import com.damai.entity.UserMobile;
import com.damai.enums.BaseCode;
import com.damai.enums.BusinessStatus;
import com.damai.enums.CompositeCheckType;
import com.damai.exception.DaMaiFrameException;
import com.damai.handler.BloomFilterHandler;
import com.damai.initialize.impl.composite.CompositeContainer;
import com.damai.jwt.TokenUtil;
import com.damai.mapper.TicketUserMapper;
import com.damai.mapper.UserEmailMapper;
import com.damai.mapper.UserMapper;
import com.damai.mapper.UserMobileMapper;
import com.damai.redis.RedisCache;
import com.damai.redis.RedisKeyBuild;
import com.damai.servicelock.LockType;
import com.damai.servicelock.annotion.ServiceLock;
import com.damai.util.StringUtil;
import com.damai.vo.GetChannelDataVo;
import com.damai.vo.TicketUserVo;
import com.damai.vo.UserGetAndTicketUserListVo;
import com.damai.vo.UserLoginVo;
import com.damai.vo.UserVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.damai.core.DistributedLockConstants.REGISTER_USER_LOCK;

/**
 * @program: 极度真实还原票务平台网高并发实战项目。 添加 阿星不是程序员 微信，添加时备注 票务平台 来获取项目的完整资料
 * @description: 用户 service
 **/
@Slf4j
@Service
public class UserService extends ServiceImpl<UserMapper, User> {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserMobileMapper userMobileMapper;

    @Autowired
    private UserEmailMapper userEmailMapper;

    @Autowired
    private UidGenerator uidGenerator;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private TicketUserMapper ticketUserMapper;

    @Autowired
    private BloomFilterHandler bloomFilterHandler;

    @Autowired
    private CompositeContainer compositeContainer;

    @Autowired
    private BaseDataClient baseDataClient;

    @Autowired
    private SmsService smsService;

    @Autowired
    private EmailService emailService;

    @Value("${token.expire.time:40}")
    private Long tokenExpireTime;

    private static final Integer ERROR_COUNT_THRESHOLD = 5;

    @Transactional(rollbackFor = Exception.class)
    @ServiceLock(lockType = LockType.Write, name = REGISTER_USER_LOCK, keys = { "#userRegisterDto.mobile" })
    public Boolean register(UserRegisterDto userRegisterDto) {
        compositeContainer.execute(CompositeCheckType.USER_REGISTER_CHECK.getValue(), userRegisterDto);
        log.info("注册手机号:{}", userRegisterDto.getMobile());
        // 用户表添加
        User user = new User();
        BeanUtils.copyProperties(userRegisterDto, user);
        user.setId(uidGenerator.getUid());
        userMapper.insert(user);
        // 用户手机表添加
        UserMobile userMobile = new UserMobile();
        userMobile.setId(uidGenerator.getUid());
        userMobile.setUserId(user.getId());
        userMobile.setMobile(userRegisterDto.getMobile());
        userMobileMapper.insert(userMobile);
        bloomFilterHandler.add(userMobile.getMobile());
        return true;
    }

    @ServiceLock(lockType = LockType.Read, name = REGISTER_USER_LOCK, keys = { "#mobile" })
    public void exist(UserExistDto userExistDto) {
        doExist(userExistDto.getMobile());
    }

    public void doExist(String mobile) {
        boolean contains = bloomFilterHandler.contains(mobile);
        if (contains) {
            LambdaQueryWrapper<UserMobile> queryWrapper = Wrappers.lambdaQuery(UserMobile.class)
                    .eq(UserMobile::getMobile, mobile)
                    .eq(UserMobile::getStatus, 1);
            UserMobile userMobile = userMobileMapper.selectOne(queryWrapper);
            if (Objects.nonNull(userMobile)) {
                throw new DaMaiFrameException(BaseCode.USER_EXIST);
            }
        }
    }

    /**
     * 登录
     * 
     * @param userLoginDto 登录入参
     * @return 用户信息
     */
    public UserLoginVo login(UserLoginDto userLoginDto) {
        UserLoginVo userLoginVo = new UserLoginVo();
        String code = userLoginDto.getCode();
        String mobile = userLoginDto.getMobile();
        String email = userLoginDto.getEmail();
        String password = userLoginDto.getPassword();
        if (StringUtil.isEmpty(mobile) && StringUtil.isEmpty(email)) {
            throw new DaMaiFrameException(BaseCode.USER_MOBILE_AND_EMAIL_NOT_EXIST);
        }
        Long userId;
        if (StringUtil.isNotEmpty(mobile)) {
            // 1. 检查错误次数是否超限（防暴力）
            String errorCountStr = redisCache
                    .get(RedisKeyBuild.createRedisKey(RedisKeyManage.LOGIN_USER_MOBILE_ERROR, mobile), String.class);
            if (StringUtil.isNotEmpty(errorCountStr) && Integer.parseInt(errorCountStr) >= ERROR_COUNT_THRESHOLD) {
                throw new DaMaiFrameException(BaseCode.MOBILE_ERROR_COUNT_TOO_MANY);
            }
            LambdaQueryWrapper<UserMobile> queryWrapper = Wrappers.lambdaQuery(UserMobile.class)
                    .eq(UserMobile::getMobile, mobile)
                    .eq(UserMobile::getStatus, 1)
                    .last("LIMIT 1");
            // 2. 查手机号是否存在
            UserMobile userMobile = userMobileMapper.selectOne(queryWrapper);
            if (Objects.isNull(userMobile)) {
                // 错误次数+1，缓存1分钟
                redisCache.incrBy(RedisKeyBuild.createRedisKey(RedisKeyManage.LOGIN_USER_MOBILE_ERROR, mobile), 1);
                redisCache.expire(RedisKeyBuild.createRedisKey(RedisKeyManage.LOGIN_USER_MOBILE_ERROR, mobile), 1,
                        TimeUnit.MINUTES);
                throw new DaMaiFrameException(BaseCode.USER_MOBILE_EMPTY);
            }
            userId = userMobile.getUserId();
        } else {
            String errorCountStr = redisCache
                    .get(RedisKeyBuild.createRedisKey(RedisKeyManage.LOGIN_USER_EMAIL_ERROR, email), String.class);
            if (StringUtil.isNotEmpty(errorCountStr) && Integer.parseInt(errorCountStr) >= ERROR_COUNT_THRESHOLD) {
                throw new DaMaiFrameException(BaseCode.EMAIL_ERROR_COUNT_TOO_MANY);
            }
            LambdaQueryWrapper<User> queryUserByEmail = Wrappers.lambdaQuery(User.class)
                    .eq(User::getEmail, email)
                    .eq(User::getStatus, 1);
            User userByEmail = userMapper.selectOne(queryUserByEmail);
            if (Objects.isNull(userByEmail)) {
                redisCache.incrBy(RedisKeyBuild.createRedisKey(RedisKeyManage.LOGIN_USER_EMAIL_ERROR, email), 1);
                redisCache.expire(RedisKeyBuild.createRedisKey(RedisKeyManage.LOGIN_USER_EMAIL_ERROR, email), 1,
                        TimeUnit.MINUTES);
                throw new DaMaiFrameException(BaseCode.USER_EMAIL_NOT_EXIST);
            }
            userId = userByEmail.getId();
        }
        LambdaQueryWrapper<User> queryUserWrapper = Wrappers.lambdaQuery(User.class)
                .eq(User::getId, userId)
                .eq(User::getPassword, password);
        if (StringUtil.isNotEmpty(mobile)) {
            queryUserWrapper.eq(User::getMobile, mobile);
        }
        User user = userMapper.selectOne(queryUserWrapper);
        if (Objects.isNull(user)) {
            throw new DaMaiFrameException(BaseCode.NAME_PASSWORD_ERROR);
        }
        // 5. 登录成功，签发 Token 并缓存
        redisCache.set(RedisKeyBuild.createRedisKey(RedisKeyManage.USER_LOGIN, code, user.getId()), user,
                tokenExpireTime, TimeUnit.MINUTES);
        userLoginVo.setUserId(userId);
        userLoginVo.setToken(createToken(user.getId(), getChannelDataByCode(code).getTokenSecret()));
        return userLoginVo;
    }

    private GetChannelDataVo getChannelDataByRedis(String code) {
        return redisCache.get(RedisKeyBuild.createRedisKey(RedisKeyManage.CHANNEL_DATA, code), GetChannelDataVo.class);
    }

    private GetChannelDataVo getChannelDataByClient(String code) {
        GetChannelDataByCodeDto getChannelDataByCodeDto = new GetChannelDataByCodeDto();
        getChannelDataByCodeDto.setCode(code);
        ApiResponse<GetChannelDataVo> getChannelDataApiResponse = baseDataClient.getByCode(getChannelDataByCodeDto);
        if (Objects.equals(getChannelDataApiResponse.getCode(), BaseCode.SUCCESS.getCode())) {
            return getChannelDataApiResponse.getData();
        }
        throw new DaMaiFrameException("没有找到ChannelData");
    }

    public String createToken(Long userId, String tokenSecret) {
        Map<String, Object> map = new HashMap<>(4);
        map.put("userId", userId);
        return TokenUtil.createToken(String.valueOf(uidGenerator.getUid()), JSON.toJSONString(map),
                tokenExpireTime * 60 * 1000, tokenSecret);
    }

    public Boolean logout(UserLogoutDto userLogoutDto) {
        String userStr = TokenUtil.parseToken(userLogoutDto.getToken(),
                getChannelDataByCode(userLogoutDto.getCode()).getTokenSecret());
        if (StringUtil.isEmpty(userStr)) {
            throw new DaMaiFrameException(BaseCode.USER_EMPTY);
        }
        String userId = JSONObject.parseObject(userStr).getString("userId");
        redisCache.del(RedisKeyBuild.createRedisKey(RedisKeyManage.USER_LOGIN, userLogoutDto.getCode(), userId));
        return true;
    }

    public GetChannelDataVo getChannelDataByCode(String code) {
        GetChannelDataVo channelDataVo = getChannelDataByRedis(code);
        if (Objects.isNull(channelDataVo)) {
            channelDataVo = getChannelDataByClient(code);
        }
        return channelDataVo;
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(UserUpdateDto userUpdateDto) {
        User user = userMapper.selectById(userUpdateDto.getId());
        if (Objects.isNull(user)) {
            throw new DaMaiFrameException(BaseCode.USER_EMPTY);
        }
        User updateUser = new User();
        BeanUtil.copyProperties(userUpdateDto, updateUser);
        userMapper.updateById(updateUser);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(UserUpdatePasswordDto userUpdatePasswordDto) {
        User user = userMapper.selectById(userUpdatePasswordDto.getId());
        if (Objects.isNull(user)) {
            throw new DaMaiFrameException(BaseCode.USER_EMPTY);
        }
        User updateUser = new User();
        BeanUtil.copyProperties(userUpdatePasswordDto, updateUser);
        userMapper.updateById(updateUser);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateEmail(UserUpdateEmailDto userUpdateEmailDto) {
        User user = userMapper.selectById(userUpdateEmailDto.getId());
        if (Objects.isNull(user)) {
            throw new DaMaiFrameException(BaseCode.USER_EMPTY);
        }
        // 重复校验：新邮箱是否已被其他用户占用
        LambdaQueryWrapper<User> duplicateCheck = Wrappers.lambdaQuery(User.class)
                .eq(User::getEmail, userUpdateEmailDto.getEmail())
                .ne(User::getId, user.getId())
                .eq(User::getStatus, 1);
        User otherUser = userMapper.selectOne(duplicateCheck);
        if (Objects.nonNull(otherUser)) {
            throw new DaMaiFrameException(BaseCode.USER_EXIST);
        }

        User updateUser = new User();
        BeanUtil.copyProperties(userUpdateEmailDto, updateUser);
        updateUser.setEmailStatus(BusinessStatus.YES.getCode());
        userMapper.updateById(updateUser);

        String oldEmail = user.getEmail();
        if (StringUtil.isNotEmpty(oldEmail)) {
            UserEmail oldRow = userEmailMapper.selectOne(Wrappers.lambdaQuery(UserEmail.class)
                    .eq(UserEmail::getUserId, user.getId())
                    .eq(UserEmail::getEmail, oldEmail)
                    .eq(UserEmail::getStatus, 1)
                    .last("LIMIT 1"));
            if (Objects.nonNull(oldRow)) {
                LambdaUpdateWrapper<UserEmail> disableOldWrapper = Wrappers.lambdaUpdate(UserEmail.class)
                        .eq(UserEmail::getId, oldRow.getId())
                        .eq(UserEmail::getStatus, 1)
                        .set(UserEmail::getStatus, 0);
                userEmailMapper.update(new UserEmail(), disableOldWrapper);
            }
        } else {
            LambdaUpdateWrapper<UserEmail> disableActivesWrapper = Wrappers.lambdaUpdate(UserEmail.class)
                    .eq(UserEmail::getUserId, user.getId())
                    .eq(UserEmail::getStatus, 1)
                    .set(UserEmail::getStatus, 0);
            userEmailMapper.update(new UserEmail(), disableActivesWrapper);
        }

        LambdaQueryWrapper<UserEmail> newEmailQuery = Wrappers.lambdaQuery(UserEmail.class)
                .eq(UserEmail::getEmail, userUpdateEmailDto.getEmail())
                .eq(UserEmail::getUserId, user.getId())
                .last("LIMIT 1");
        UserEmail existNewEmail = userEmailMapper.selectOne(newEmailQuery);
        if (Objects.nonNull(existNewEmail)) {
            Integer status = existNewEmail.getStatus();
            if (Objects.nonNull(status) && status == 0) {
                LambdaUpdateWrapper<UserEmail> activateWrapper = Wrappers.lambdaUpdate(UserEmail.class)
                        .eq(UserEmail::getId, existNewEmail.getId())
                        .set(UserEmail::getStatus, 1);
                userEmailMapper.update(new UserEmail(), activateWrapper);
            }
        } else {
            UserEmail userEmail = new UserEmail();
            userEmail.setId(uidGenerator.getUid());
            userEmail.setUserId(user.getId());
            userEmail.setEmail(userUpdateEmailDto.getEmail());
            userEmail.setStatus(1);
            userEmailMapper.insert(userEmail);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateMobile(UserUpdateMobileDto userUpdateMobileDto) {
        User user = userMapper.selectById(userUpdateMobileDto.getId());
        if (Objects.isNull(user)) {
            throw new DaMaiFrameException(BaseCode.USER_EMPTY);
        }
        LambdaQueryWrapper<User> duplicateCheck = Wrappers.lambdaQuery(User.class)
                .eq(User::getMobile, userUpdateMobileDto.getMobile())
                .ne(User::getId, user.getId())
                .eq(User::getStatus, 1);
        User otherUser = userMapper.selectOne(duplicateCheck);
        if (Objects.nonNull(otherUser)) {
            throw new DaMaiFrameException(BaseCode.USER_EXIST);
        }

        User updateUser = new User();
        BeanUtil.copyProperties(userUpdateMobileDto, updateUser);
        userMapper.updateById(updateUser);

        LambdaUpdateWrapper<UserMobile> disableOldMobiles = Wrappers.lambdaUpdate(UserMobile.class)
                .eq(UserMobile::getUserId, user.getId())
                .eq(UserMobile::getStatus, 1)
                .set(UserMobile::getStatus, 0);
        userMobileMapper.update(new UserMobile(), disableOldMobiles);

        LambdaQueryWrapper<UserMobile> newMobileQuery = Wrappers.lambdaQuery(UserMobile.class)
                .eq(UserMobile::getMobile, userUpdateMobileDto.getMobile())
                .eq(UserMobile::getUserId, user.getId())
                .last("LIMIT 1");
        UserMobile existNew = userMobileMapper.selectOne(newMobileQuery);
        if (Objects.nonNull(existNew)) {
            Integer status = existNew.getStatus();
            if (Objects.nonNull(status) && status == 0) {
                UserMobile activate = new UserMobile();
                activate.setId(existNew.getId());
                activate.setStatus(1);
                userMobileMapper.updateById(activate);
            }
        } else {
            UserMobile userMobile = new UserMobile();
            userMobile.setId(uidGenerator.getUid());
            userMobile.setUserId(user.getId());
            userMobile.setMobile(userUpdateMobileDto.getMobile());
            userMobile.setStatus(1);
            userMobileMapper.insert(userMobile);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void authentication(UserAuthenticationDto userAuthenticationDto) {
        User user = userMapper.selectById(userAuthenticationDto.getId());
        if (Objects.isNull(user)) {
            throw new DaMaiFrameException(BaseCode.USER_EMPTY);
        }
        if (Objects.equals(user.getRelAuthenticationStatus(), BusinessStatus.YES.getCode())) {
            throw new DaMaiFrameException(BaseCode.USER_AUTHENTICATION);
        }
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setRelName(userAuthenticationDto.getRelName());
        updateUser.setIdNumber(userAuthenticationDto.getIdNumber());
        updateUser.setRelAuthenticationStatus(BusinessStatus.YES.getCode());
        userMapper.updateById(updateUser);
    }

    public UserVo getByMobile(UserMobileDto userMobileDto) {
        LambdaQueryWrapper<UserMobile> queryWrapper = Wrappers.lambdaQuery(UserMobile.class)
                .eq(UserMobile::getMobile, userMobileDto.getMobile())
                .eq(UserMobile::getStatus, 1)
                .last("LIMIT 1");
        UserMobile userMobile = userMobileMapper.selectOne(queryWrapper);
        if (Objects.isNull(userMobile)) {
            throw new DaMaiFrameException(BaseCode.USER_MOBILE_EMPTY);
        }
        User user = userMapper.selectById(userMobile.getUserId());
        if (Objects.isNull(user)) {
            throw new DaMaiFrameException(BaseCode.USER_EMPTY);
        }
        UserVo userVo = new UserVo();
        BeanUtil.copyProperties(user, userVo);
        userVo.setMobile(userMobile.getMobile());
        return userVo;
    }

    public UserVo getById(UserIdDto userIdDto) {
        User user = userMapper.selectById(userIdDto.getId());
        if (Objects.isNull(user)) {
            throw new DaMaiFrameException(BaseCode.USER_EMPTY);
        }
        UserVo userVo = new UserVo();
        BeanUtil.copyProperties(user, userVo);
        return userVo;
    }

    public UserGetAndTicketUserListVo getUserAndTicketUserList(
            final UserGetAndTicketUserListDto userGetAndTicketUserListDto) {
        UserIdDto userIdDto = new UserIdDto();
        userIdDto.setId(userGetAndTicketUserListDto.getUserId());
        UserVo userVo = getById(userIdDto);

        LambdaQueryWrapper<TicketUser> ticketUserLambdaQueryWrapper = Wrappers.lambdaQuery(TicketUser.class)
                .eq(TicketUser::getUserId, userGetAndTicketUserListDto.getUserId());
        List<TicketUser> ticketUserList = ticketUserMapper.selectList(ticketUserLambdaQueryWrapper);
        List<TicketUserVo> ticketUserVoList = BeanUtil.copyToList(ticketUserList, TicketUserVo.class);

        UserGetAndTicketUserListVo userGetAndTicketUserListVo = new UserGetAndTicketUserListVo();
        userGetAndTicketUserListVo.setUserVo(userVo);
        userGetAndTicketUserListVo.setTicketUserVoList(ticketUserVoList);
        return userGetAndTicketUserListVo;
    }

    public List<String> getAllMobile() {
        QueryWrapper<User> lambdaQueryWrapper = Wrappers.emptyWrapper();
        List<User> users = userMapper.selectList(lambdaQueryWrapper);
        return users.stream().map(User::getMobile).collect(Collectors.toList());
    }

    /**
     * 发送短信验证码
     * 
     * @param sendSmsCodeDto 发送短信验证码入参
     * @return 是否发送成功
     */
    public Boolean sendSmsCode(SendSmsCodeDto sendSmsCodeDto) {
        String mobile = sendSmsCodeDto.getMobile();
        String type = sendSmsCodeDto.getType();

        // 验证类型是否合法
        if (!type.equals("login") && !type.equals("register")) {
            throw new DaMaiFrameException(BaseCode.SMS_CODE_TYPE_ERROR);
        }

        // 检查发送频率限制(60秒内只能发送一次) - 按手机号+类型维度
        RedisKeyBuild limitKey = RedisKeyBuild.createRedisKey(RedisKeyManage.SMS_CODE_SEND_LIMIT, mobile + ":" + type);
        String limitCount = redisCache.get(limitKey, String.class);
        if (StringUtil.isNotEmpty(limitCount)) {
            throw new DaMaiFrameException(BaseCode.SMS_CODE_SEND_FREQUENT);
        }

        // 生成6位随机验证码
        String smsCode = String.valueOf((int) ((Math.random() * 9 + 1) * 100000));

        // 存储验证码到Redis,有效期1分钟
        RedisKeyBuild codeKey = RedisKeyBuild.createRedisKey(RedisKeyManage.SMS_CODE_LOGIN, mobile);
        redisCache.set(codeKey, smsCode, 1, TimeUnit.MINUTES);

        // 设置发送频率限制,60秒 - 按手机号+类型维度
        redisCache.set(limitKey, "1", 60, TimeUnit.SECONDS);

        // 调用Spug推送发送短信验证码
        log.info("发送短信验证码到手机号: {}, 验证码: {}, 类型: {}", mobile, smsCode, type);
        boolean sendResult = smsService.sendSmsCode(mobile, smsCode);

        if (!sendResult) {
            log.warn("短信发送失败,但验证码已存储到Redis,可以继续使用 - 手机号: {}, 验证码: {}", mobile, smsCode);
            // 即使短信发送失败,也不抛出异常,因为验证码已经存储到Redis
            // 用户可以通过日志获取验证码进行测试
        }

        return true;
    }

    public Boolean sendEmailCode(com.damai.dto.SendEmailCodeDto sendEmailCodeDto) {
        String email = sendEmailCodeDto.getEmail();
        String type = sendEmailCodeDto.getType();
        if (!type.equals("login") && !type.equals("register")) {
            throw new DaMaiFrameException(BaseCode.SMS_CODE_TYPE_ERROR);
        }
        RedisKeyBuild limitKey = RedisKeyBuild.createRedisKey(RedisKeyManage.EMAIL_CODE_SEND_LIMIT, email + ":" + type);
        String limitCount = redisCache.get(limitKey, String.class);
        if (StringUtil.isNotEmpty(limitCount)) {
            throw new DaMaiFrameException(BaseCode.EMAIL_CODE_SEND_FREQUENT);
        }
        String emailCode = String.valueOf((int) ((Math.random() * 9 + 1) * 100000));
        RedisKeyBuild codeKey = RedisKeyBuild.createRedisKey(RedisKeyManage.EMAIL_CODE_LOGIN, email);
        redisCache.set(codeKey, emailCode, 5, TimeUnit.MINUTES);
        redisCache.set(limitKey, "1", 60, TimeUnit.SECONDS);
        log.info("邮箱验证码生成成功 - 邮箱: {}, 验证码: {}", email, emailCode);
        emailService.sendEmailCodeAsync(email, emailCode);
        return true;
    }

    /**
     * 短信验证码登录
     * 
     * @param userSmsLoginDto 短信验证码登录入参
     * @return 用户信息
     */
    @Transactional(rollbackFor = Exception.class)
    public UserLoginVo smsLogin(UserSmsLoginDto userSmsLoginDto) {
        String code = userSmsLoginDto.getCode();
        String mobile = userSmsLoginDto.getMobile();
        String smsCode = userSmsLoginDto.getSmsCode();

        // 1. 验证验证码
        RedisKeyBuild codeKey = RedisKeyBuild.createRedisKey(RedisKeyManage.SMS_CODE_LOGIN, mobile);
        String storedCode = redisCache.get(codeKey, String.class);

        if (StringUtil.isEmpty(storedCode)) {
            throw new DaMaiFrameException(BaseCode.SMS_CODE_EXPIRED);
        }

        if (!storedCode.equals(smsCode)) {
            throw new DaMaiFrameException(BaseCode.SMS_CODE_ERROR);
        }

        // 2. 验证码正确,删除验证码
        redisCache.del(codeKey);

        List<UserMobile> userMobileList = userMobileMapper.selectList(
                Wrappers.lambdaQuery(UserMobile.class)
                        .eq(UserMobile::getMobile, mobile)
                        .eq(UserMobile::getStatus, 1)
                        .orderByDesc(UserMobile::getId));
        if (userMobileList == null || userMobileList.isEmpty()) {
            log.info("短信验证码登录失败,手机号未注册: {}", mobile);
            throw new DaMaiFrameException(BaseCode.USER_MOBILE_NOT_REGISTERED);
        }
        UserMobile userMobile = userMobileList.get(0);

        User user = userMapper.selectById(userMobile.getUserId());
        if (Objects.isNull(user)) {
            throw new DaMaiFrameException(BaseCode.USER_EMPTY);
        }
        if (!Objects.equals(user.getMobile(), mobile)) {
            throw new DaMaiFrameException(BaseCode.USER_MOBILE_NOT_REGISTERED);
        }
        log.info("短信验证码登录,用户已存在,手机号: {}, 用户ID: {}", mobile, user.getId());

        // 6. 登录成功,签发 Token 并缓存
        redisCache.set(RedisKeyBuild.createRedisKey(RedisKeyManage.USER_LOGIN, code, user.getId()), user,
                tokenExpireTime, TimeUnit.MINUTES);

        UserLoginVo userLoginVo = new UserLoginVo();
        userLoginVo.setUserId(user.getId());
        userLoginVo.setToken(createToken(user.getId(), getChannelDataByCode(code).getTokenSecret()));

        return userLoginVo;
    }

    @Transactional(rollbackFor = Exception.class)
    public UserLoginVo emailCodeLogin(com.damai.dto.UserEmailCodeLoginDto userEmailCodeLoginDto) {
        String code = userEmailCodeLoginDto.getCode();
        String email = userEmailCodeLoginDto.getEmail();
        String emailCode = userEmailCodeLoginDto.getEmailCode();
        RedisKeyBuild codeKey = RedisKeyBuild.createRedisKey(RedisKeyManage.EMAIL_CODE_LOGIN, email);
        String storedCode = redisCache.get(codeKey, String.class);
        if (StringUtil.isEmpty(storedCode)) {
            throw new DaMaiFrameException(BaseCode.EMAIL_CODE_INVALID);
        }
        if (!storedCode.equals(emailCode)) {
            log.warn("邮箱验证码校验失败 - 邮箱: {}, Redis中验证码: {}, 用户输入: {}", email, storedCode, emailCode);
            throw new DaMaiFrameException(BaseCode.EMAIL_CODE_INVALID);
        }
        redisCache.del(codeKey);
        LambdaQueryWrapper<User> queryUserByEmail = Wrappers.lambdaQuery(User.class)
                .eq(User::getEmail, email)
                .eq(User::getStatus, 1);
        User user = userMapper.selectOne(queryUserByEmail);
        if (Objects.isNull(user)) {
            throw new DaMaiFrameException(BaseCode.USER_EMPTY);
        }
        redisCache.set(RedisKeyBuild.createRedisKey(RedisKeyManage.USER_LOGIN, code, user.getId()), user, tokenExpireTime, TimeUnit.MINUTES);
        UserLoginVo userLoginVo = new UserLoginVo();
        userLoginVo.setUserId(user.getId());
        userLoginVo.setToken(createToken(user.getId(), getChannelDataByCode(code).getTokenSecret()));
        return userLoginVo;
    }
}
