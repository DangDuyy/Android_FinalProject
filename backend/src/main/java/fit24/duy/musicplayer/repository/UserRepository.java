package fit24.duy.musicplayer.repository;

import fit24.duy.musicplayer.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
   Optional<User> findByEmailAndPassword(String email, String password);
   Optional<User> findByEmail(String email);
   User findUserByEmail(String email);
}
