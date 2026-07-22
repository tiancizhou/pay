package com.bob.pay.application;

import com.bob.pay.domain.model.technician.TechnicianRepository;
import com.bob.pay.domain.model.user.AppUser;
import com.bob.pay.domain.model.user.AppUserRepository;
import com.bob.pay.domain.model.user.UserRole;
import jakarta.annotation.PostConstruct;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserApplicationService {

    public static final String DEFAULT_USER_ID = "client-demo";

    private final AppUserRepository userRepository;
    private final TechnicianRepository technicianRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserApplicationService(
            AppUserRepository userRepository,
            TechnicianRepository technicianRepository
    ) {
        this.userRepository = userRepository;
        this.technicianRepository = technicianRepository;
    }

    @PostConstruct
    void seedDefaultAccounts() {
        seedAccount("admin", "admin", "系统管理员", "18069906336", UserRole.ADMIN, null, "admin123");
        seedAccount("client-demo", "client", "客户用户", "15058666378", UserRole.CLIENT, null, "client123");
        seedAccount("tech-sha", "tech", "沙鑫鑫", "13800000001", UserRole.TECHNICIAN, "sha", "tech123");
    }

    private void seedAccount(
            String id,
            String username,
            String name,
            String phone,
            UserRole role,
            String technicianId,
            String password
    ) {
        if (userRepository.findByUsername(username).isPresent()) {
            return;
        }
        userRepository.save(new AppUser(
                id,
                username,
                name,
                phone,
                role,
                technicianId,
                passwordEncoder.encode(password)
        ));
    }

    public AppUser currentUser(String userId) {
        var resolvedUserId = userId == null || userId.isBlank() ? DEFAULT_USER_ID : userId;
        return userRepository.findById(resolvedUserId)
                .orElseGet(() -> userRepository.findById(DEFAULT_USER_ID).orElseThrow())
                .withoutPassword();
    }

    public synchronized AppUser findOrCreateWechatUser(String openId, String unionId) {
        if (openId == null || openId.isBlank()) {
            throw new IllegalArgumentException("微信用户标识不能为空");
        }
        var existing = userRepository.findByWechatOpenId(openId);
        if (existing.isPresent()) {
            var user = existing.get();
            if (unionId != null && !unionId.isBlank() && !unionId.equals(user.wechatUnionId())) {
                user = userRepository.save(new AppUser(
                        user.id(),
                        user.username(),
                        user.name(),
                        user.phone(),
                        user.role(),
                        user.technicianId(),
                        user.wechatOpenId(),
                        unionId,
                        user.passwordHash()
                ));
            }
            return user.withoutPassword();
        }

        var suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        return userRepository.save(new AppUser(
                "wx-user-" + suffix,
                "wx_" + suffix,
                "微信用户",
                "",
                UserRole.CLIENT,
                null,
                openId,
                unionId == null || unionId.isBlank() ? null : unionId,
                passwordEncoder.encode(UUID.randomUUID().toString())
        )).withoutPassword();
    }

    public List<AppUser> listUsers() {
        return userRepository.findAll().stream().map(AppUser::withoutPassword).toList();
    }

    public AppUser login(LoginCommand command) {
        var username = clean(command.username());
        if (username.isBlank() || command.password() == null || command.password().isBlank()) {
            throw new IllegalArgumentException("账号或密码错误");
        }
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("账号或密码错误"));
        if (!passwordEncoder.matches(command.password(), user.passwordHash())) {
            throw new IllegalArgumentException("账号或密码错误");
        }
        if ("ADMIN".equalsIgnoreCase(clean(command.scope())) && user.role() != UserRole.ADMIN) {
            throw new IllegalArgumentException("当前账号不是管理员");
        }
        if ("PORTAL".equalsIgnoreCase(clean(command.scope())) && user.role() == UserRole.ADMIN) {
            throw new IllegalArgumentException("管理员请从管理端入口登录");
        }
        return user.withoutPassword();
    }

    public AppUser createUser(CreateUserCommand command) {
        var username = clean(command.username());
        if (username.isBlank()) {
            throw new IllegalArgumentException("账号不能为空");
        }
        if (command.password() == null || command.password().length() < 6) {
            throw new IllegalArgumentException("密码至少 6 位");
        }
        if (command.role() == null) {
            throw new IllegalArgumentException("请选择账号角色");
        }
        userRepository.findByUsername(username).ifPresent(existing -> {
            throw new IllegalArgumentException("账号已存在");
        });
        validateRoleBinding(command.role(), command.technicianId());
        var id = "user-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return userRepository.save(new AppUser(
                id,
                username,
                clean(command.name()).isBlank() ? username : clean(command.name()),
                clean(command.phone()),
                command.role(),
                command.role() == UserRole.TECHNICIAN ? command.technicianId() : null,
                passwordEncoder.encode(command.password())
        )).withoutPassword();
    }

    public AppUser updateRole(String userId, UpdateUserRoleCommand command) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        validateRoleBinding(command.role(), command.technicianId());
        return userRepository.save(user.withRole(command.role(), command.technicianId())).withoutPassword();
    }

    public AppUser resetPassword(String userId, ResetPasswordCommand command) {
        if (command.password() == null || command.password().length() < 6) {
            throw new IllegalArgumentException("密码至少 6 位");
        }
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        return userRepository.save(new AppUser(
                user.id(),
                user.username(),
                user.name(),
                user.phone(),
                user.role(),
                user.technicianId(),
                user.wechatOpenId(),
                user.wechatUnionId(),
                passwordEncoder.encode(command.password())
        )).withoutPassword();
    }

    private void validateRoleBinding(UserRole role, String technicianId) {
        if (role == null) {
            throw new IllegalArgumentException("请选择账号角色");
        }
        if (role == UserRole.TECHNICIAN) {
            if (technicianId == null || technicianId.isBlank()) {
                throw new IllegalArgumentException("技师角色必须绑定技师资料");
            }
            technicianRepository.findById(technicianId)
                    .orElseThrow(() -> new IllegalArgumentException("绑定的技师不存在"));
        }
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }

    public record LoginCommand(String username, String password, String scope) {
    }

    public record CreateUserCommand(
            String username,
            String password,
            String name,
            String phone,
            UserRole role,
            String technicianId
    ) {
    }

    public record UpdateUserRoleCommand(UserRole role, String technicianId) {
    }

    public record ResetPasswordCommand(String password) {
    }
}
