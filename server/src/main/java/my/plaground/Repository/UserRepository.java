package my.plaground.Repository;

import my.plaground.Domain.Entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends  JpaRepository<UserEntity, Integer> {
    public UserEntity findByUsername(String username);
}
