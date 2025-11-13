package com.damai.servicelock.factory;

import com.damai.core.ManageLocker;
import com.damai.servicelock.LockType;
import com.damai.servicelock.ServiceLocker;
import lombok.AllArgsConstructor;

/**
 * @description: 分布式锁类型工厂
 **/
@AllArgsConstructor
public class ServiceLockFactory {
    
    private final ManageLocker manageLocker;
    
    
    public ServiceLocker getLock(LockType lockType){
        if (lockType == LockType.Fair) {
            return manageLocker.getFairLocker();
        }
        if (lockType == LockType.Write) {
            return manageLocker.getWriteLocker();
        }
        if (lockType == LockType.Read) {
            return manageLocker.getReadLocker();
        }
        return manageLocker.getReentrantLocker();
    }
}
