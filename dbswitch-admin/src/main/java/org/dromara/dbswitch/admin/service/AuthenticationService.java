// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.dbswitch.admin.service;

import org.dromara.dbswitch.admin.common.response.Result;
import org.dromara.dbswitch.admin.common.response.ResultCode;
import org.dromara.dbswitch.admin.model.response.AccessTokenResponse;
import org.dromara.dbswitch.admin.util.CacheUtils;
import org.dromara.dbswitch.admin.util.PasswordUtils;
import org.dromara.dbswitch.admin.util.ServletUtils;
import org.dromara.dbswitch.admin.util.TokenUtils;
import org.dromara.dbswitch.admin.entity.SystemUserEntity;
import com.mchange.v2.lang.StringUtils;
import java.util.Objects;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuthenticationService {

  @Resource
  private SystemUserService systemUserService;

  public Result<AccessTokenResponse> login(String username, String password) {
    SystemUserEntity user = systemUserService.findByUsername(username);
    if (Objects.isNull(user)) {
      return Result.failed(ResultCode.ERROR_USER_NOT_EXISTS, username);
    }

    String encryptPassword = PasswordUtils.encryptPassword(password, user.getSalt());
    if (!encryptPassword.equals(user.getPassword())) {
      return Result.failed(ResultCode.ERROR_USER_PASSWORD_WRONG, username);
    }

    String token = TokenUtils.generateValue();
    CacheUtils.put(token, user);
    AccessTokenResponse accessTokenWrapper = new AccessTokenResponse(user.getRealName(), token,
        CacheUtils.CACHE_DURATION_SECONDS);
    return Result.success(accessTokenWrapper);
  }

  public Result logout() {
    String token = TokenUtils.getRequestToken(ServletUtils.getHttpServletRequest());
    if (StringUtils.nonEmptyString(token)) {
      CacheUtils.remove(token);
    }

    log.info("logout with token:{}", token);
    return Result.success();
  }

}
