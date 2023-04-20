package study.querydsl.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import study.querydsl.entity.Member;
import java.util.List;
public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {
    List<Member> findByUsername(String username);

    List<UsernameOnly> findProjectionsByUsername(String username);
    List<UsernameOnlyDto> findProjectionsByUsername2(String username);
}