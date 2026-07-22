package com.bob.pay.infrastructure.persistence;

import com.bob.pay.domain.model.user.AppUser;
import com.bob.pay.domain.model.user.AppUserRepository;
import com.bob.pay.domain.model.user.UserRole;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InMemoryAppUserRepository implements AppUserRepository {

    private final List<AppUser> users = new ArrayList<>(List.of(
            new AppUser("admin", "admin", "系统管理员", "18069906336", UserRole.ADMIN, null, "{noop}admin123"),
            new AppUser("client-demo", "client", "客户用户", "15058666378", UserRole.CLIENT, null, "{noop}client123"),
            new AppUser("tech-sha", "tech", "沙鑫鑫", "13800000001", UserRole.TECHNICIAN, "sha", "{noop}tech123")
    ));

    @Override
    public List<AppUser> findAll() {
        return List.copyOf(users);
    }

    @Override
    public Optional<AppUser> findById(String id) {
        return users.stream().filter(user -> user.id().equals(id)).findFirst();
    }

    @Override
    public Optional<AppUser> findByUsername(String username) {
        return users.stream().filter(user -> user.username().equals(username)).findFirst();
    }

    @Override
    public Optional<AppUser> findByWechatOpenId(String wechatOpenId) {
        return users.stream().filter(user -> wechatOpenId.equals(user.wechatOpenId())).findFirst();
    }

    @Override
    public AppUser save(AppUser user) {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).id().equals(user.id())) {
                users.set(i, user);
                return user;
            }
        }
        users.add(user);
        return user;
    }
}
