package com.bob.pay.domain.model.user;

import java.util.List;
import java.util.Optional;

public interface AppUserRepository {

    List<AppUser> findAll();

    Optional<AppUser> findById(String id);

    Optional<AppUser> findByUsername(String username);

    Optional<AppUser> findByWechatOpenId(String wechatOpenId);

    AppUser save(AppUser user);
}
