package org.example.operatormanagementsystem.repository;

import org.example.operatormanagementsystem.enumeration.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.example.operatormanagementsystem.entity.Users;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface UserRepository extends JpaRepository<Users, Integer> {
    Optional<Users> findByUsername(String username);
    Optional<Users> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    // Đã sửa: Phương thức này cần trả về List<Users> vì nó tìm kiếm nhiều người dùng theo trạng thái
    List<Users> findByStatus(UserStatus status); // <-- Đã đổi từ Optional<Users> sang List<Users>

    // Phương thức này là lý tưởng cho getUsersNeedingManagerAction, nhưng bạn muốn dùng findByStatus 2 lần.
    // Dù sao, nó vẫn nên tồn tại trong repository nếu bạn muốn linh hoạt trong tương lai.
    List<Users> findByStatusIn(List<UserStatus> statuses);

}