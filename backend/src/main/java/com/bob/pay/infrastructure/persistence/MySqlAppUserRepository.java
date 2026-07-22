package com.bob.pay.infrastructure.persistence;

import com.bob.pay.domain.model.user.AppUser;
import com.bob.pay.domain.model.user.AppUserRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Primary
@Repository
public class MySqlAppUserRepository implements AppUserRepository {

    private final SpringDataAppUserJpaRepository repository;

    public MySqlAppUserRepository(SpringDataAppUserJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<AppUser> findAll() {
        return repository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<AppUser> findById(String id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<AppUser> findByUsername(String username) {
        return repository.findByUsername(username).map(this::toDomain);
    }

    @Override
    public Optional<AppUser> findByWechatOpenId(String wechatOpenId) {
        return repository.findByWechatOpenId(wechatOpenId).map(this::toDomain);
    }

    @Override
    public AppUser save(AppUser user) {
        return toDomain(repository.save(toEntity(user)));
    }

    private AppUser toDomain(JpaAppUserEntity entity) {
        return new AppUser(
                entity.id(),
                entity.username(),
                entity.name(),
                entity.phone(),
                entity.role(),
                entity.technicianId(),
                entity.wechatOpenId(),
                entity.wechatUnionId(),
                entity.passwordHash()
        );
    }

    private JpaAppUserEntity toEntity(AppUser user) {
        return new JpaAppUserEntity(
                user.id(),
                user.username(),
                user.name(),
                user.phone(),
                user.role(),
                user.role() == com.bob.pay.domain.model.user.UserRole.TECHNICIAN ? user.technicianId() : null,
                user.wechatOpenId(),
                user.wechatUnionId(),
                user.passwordHash()
        );
    }
}
