package study.querydsl;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.expression.spel.ast.Projection;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberDto;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.dto.QMemberDto;
import study.querydsl.dto.UserDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.QTeam;
import study.querydsl.entity.Team;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import static study.querydsl.entity.QMember.*;
import static study.querydsl.entity.QTeam.team;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

    @PersistenceContext
    EntityManager em;
    JPAQueryFactory queryFactory;

    @BeforeEach
    public void before() {
        queryFactory = new JPAQueryFactory(em);
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);
        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

//        Team teamA = new Team("teamA");
//        Team teamB = new Team("teamB");
//        em.persist(teamA);
//        em.persist(teamB);
//        Member member1 = new Member("member1", 10, teamA);
//        Member member2 = new Member("member2", 20, teamA);
//        Member member3 = new Member("member3", 30, teamB);
//        Member member4 = new Member("member4", 40, teamB);
//        em.persist(member1);
//        em.persist(member2);
//        em.persist(member3);
//        em.persist(member4);
//        Member member5 = new Member("member5", 50, null);
//        Member member6 = new Member("member6", 60, null);
//        em.persist(member5);
//        em.persist(member6);
//        Team teamC = new Team("teamC");
//        Team teamD = new Team("teamD");
//        em.persist(teamC);
//        em.persist(teamD);
    }

    @Test
    public void startJPQL() {
        String qlString = "select m from Member m " +
            "where m.username = :username";
        Member findMember = em.createQuery(qlString, Member.class)
            .setParameter("username", "member1")
            .getSingleResult();
        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void startQuerydsl() {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        QMember m = new QMember("m");

        Member findMember = queryFactory
            .select(m)
            .from(m)
            .where(m.username.eq("member1"))
            .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void search() {
        Member findMember = queryFactory
            .selectFrom(member)
            .where(member.username.eq("member1")
                .and(member.age.eq(10)))
            .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void searchOr() {
        Member findMember = queryFactory
            .selectFrom(member)
            .where(member.username.eq("member1")
                .or(member.age.eq(90)))
            .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void searchAndParam() {
        List<Member> result1 = queryFactory
            .selectFrom(member)
            .where(member.username.eq("member1"),
                member.age.eq(10))
            .fetch();
        assertThat(result1.size()).isEqualTo(1);
    }

    @Test
    void fetchTest() {
        Long totalCount = queryFactory
            .select(Wildcard.count) //select count(*)
//            .select(member.count()) //select count(member.id)
            .from(member)
            .fetchOne();

        System.out.println("totalCount = " + totalCount);
    }


    @Test
    void sort() {
        em.persist(new Member(null, 100));
        em.persist(new Member("member5", 100));
        em.persist(new Member("member6", 100));
        em.persist(new Member("member7", 110));
        List<Member> result = queryFactory.selectFrom(member)
            .orderBy(member.age.desc(), member.username.asc())
            .fetch();
        for (Member member1 : result) {
            System.out.println(member1.toString());
        }
//        Member member5 = result.get(0);
//        Member member6 = result.get(1);
//        Member memberNull = result.get(2);
//        assertThat(member5.getUsername()).isEqualTo("member5");
//        assertThat(member6.getUsername()).isEqualTo("member6");
//        assertThat(memberNull.getUsername()).isNull();
    }

    @Test
    public void paging1() {
        List<Member> fetch = queryFactory.selectFrom(member)
            .orderBy(member.username.desc())
            .offset(1)
            .limit(2)
            .fetch();

    }

    @Test
    public void paging2() {
        QueryResults<Member> queryResults = queryFactory
            .selectFrom(member)
            .orderBy(member.username.desc())
            .offset(1)
            .limit(2)
            .fetchResults();
        //내부에 해당 offset limit 처리된 결과 포함
        List<Member> results = queryResults.getResults();
        //이런 식으로 해당 select의 전체 수가 포함된 queryResults를 반환한다.
        assertThat(queryResults.getTotal()).isEqualTo(4);
        assertThat(queryResults.getLimit()).isEqualTo(2);
        assertThat(queryResults.getOffset()).isEqualTo(1);
    }

    @Test
    public void aggregation() throws Exception {
        List<Tuple> result = queryFactory
            .select(member.count(),
                member.age.sum(),
                member.age.avg(),
                member.age.max(),
                member.age.min())
            .from(member)
            .fetch();
        Tuple tuple = result.get(0);
        assertThat(tuple.get(member.count())).isEqualTo(4);
        assertThat(tuple.get(member.age.sum())).isEqualTo(100);
        assertThat(tuple.get(member.age.avg())).isEqualTo(25);
        assertThat(tuple.get(member.age.max())).isEqualTo(40);
        assertThat(tuple.get(member.age.min())).isEqualTo(10);
    }

    /**
     * 팀의 이름과 각 팀의 평균 연령을 구해라.
     */
    @Test
    @Rollback(value = false)
    public void group() throws Exception {
        List<Tuple> result = queryFactory
            .select(team.name, member.age.avg())
            .from(member)
            .join(member.team, team) //team 또한 Q멤버이다.
            .groupBy(team.name) //팀을 기준으로 멤버들의 나이 평균을 구할 수 잇다.
            .fetch();
        Tuple teamA = result.get(0);
        Tuple teamB = result.get(1);
        assertThat(teamA.get(team.name)).isEqualTo("teamA");
        assertThat(teamA.get(member.age.avg())).isEqualTo(15);
        assertThat(teamB.get(team.name)).isEqualTo("teamB");
        assertThat(teamB.get(member.age.avg())).isEqualTo(35);
    }

    @Test
    public void join() throws Exception {
        QMember member = QMember.member;
        em.persist(new Member("noTeamMember1", 15, null));
        em.persist(new Member("noTeamMember2", 25, null));
        QTeam team = QTeam.team;
        List<Member> result = queryFactory
            .selectFrom(member)
            .join(member.team, team)
            .fetch();

        for (Member member1 : result) {
            System.out.println(member1.toString());
        }
//        assertThat(result)
//            .extracting("username")
//            .containsExactly("member1", "member2");
    }

    @Test
    public void theta_join() throws Exception {
        //현재 teamA와 teamB가 저장되어 있다.
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        List<Member> result = queryFactory
            .select(member)
            .from(member, team)
            .where(member.username.eq(team.name))
            .fetch();
        assertThat(result)
            .extracting("username")
            .containsExactly("teamA", "teamB");
    }


    @Test
    @Rollback(value = false)
    void innerJoin() {
        List<Tuple> result = queryFactory
            .select(member, team)
            .from(member)
            //innerjoin() 이렇게 써도 된다.
            .join(member.team, team)
            .fetch();
        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    @Test
    @Rollback(value = false)
    void innerJoinOn() {
        List<Tuple> result = queryFactory
            .select(member, team)
            .from(member)
            //innerjoin() 이렇게 써도 된다.
            .join(member.team, team)
            .on(team.name.eq("teamA"))
            .fetch();
        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    @Test
    @Rollback(value = false)
    void innerJoinWhere() {
        List<Tuple> result = queryFactory
            .select(member, team)
            .from(member)
            //innerjoin() 이렇게 써도 된다.
            .join(member.team, team)
            .on(member.username.eq("member1"))
            .fetch();
        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    @Test
    void leftJoin() {
        List<Tuple> result = queryFactory
            .select(member, team)
            .from(member)
            .leftJoin(member.team, team)
            .fetch();
        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    @Test
    void allJoin() {
        List<Tuple> result = queryFactory
            .selectDistinct(member, team)
            .from(member, team)
            .fetch();
        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    @Test
    void rightJoin() {
        List<Tuple> result = queryFactory
            .selectDistinct(member, team)
            .from(member)
            .rightJoin(member.team, team)
            .fetch();
        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    @Test
    @Rollback(value = false)
    public void theta_join2() throws Exception {
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        List<Tuple> result = queryFactory
            .select(member, team)
            .from(member, team)
            .where(member.username.eq(team.name))
            .fetch();
        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }

    }

    @Test

    public void join_on_no_relation() throws Exception {
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        List<Tuple> result = queryFactory
            .select(member, team)
            .from(member)
            .leftJoin(team).on(member.username.eq(team.name))
            .fetch();
        for (Tuple tuple : result) {
            System.out.println("t=" + tuple);
        }
    }

    @PersistenceUnit
    EntityManagerFactory emf;

    @Test
    public void fetchJoinNo() throws Exception {
        em.flush();
        em.clear();
        Member findMember = queryFactory
            .selectFrom(member)
            .where(member.username.eq("member1"))
            .fetchOne();
        boolean loaded =
            emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded).as("페치 조인 미적용").isFalse();
    }

    @Test
    public void fetchJoinUse() throws Exception {
        em.flush();
        em.clear();
        Member findMember = queryFactory
            .selectFrom(member)
            .join(member.team, team).fetchJoin()
            .where(member.username.eq("member1"))
            .fetchOne();
        boolean loaded =
            emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded).as("페치 조인 적용").isTrue();
    }

    /**
     * 나이가 가장 많은 회원 조회
     */
    @Test
    public void subQuery() throws Exception {
        QMember memberSub = new QMember("memberSub");
        List<Member> result = queryFactory
            .selectFrom(member)
            .where(member.age.eq(
                JPAExpressions
                    .select(memberSub.age.max())
                    .from(memberSub)
            ))
            .fetch();
        assertThat(result).extracting("age")
            .containsExactly(40);
    }

    @Test
    public void subQueryIn() throws Exception {
        QMember memberSub = new QMember("memberSub");
        List<Member> result = queryFactory
            .selectFrom(member)
            .where(member.age.in(
                JPAExpressions
                    .select(memberSub.age)
                    .from(memberSub)
                    .where(memberSub.age.gt(10))
            ))
            .fetch();
        assertThat(result).extracting("age")
            .containsExactly(20, 30, 40);
    }

    @Test
    public void subQuerySelect() throws Exception {
        QMember memberSub = new QMember("memberSub");
        List<Tuple> fetch = queryFactory
            .select(member.username,
                JPAExpressions
                    .select(memberSub.age.avg())
                    .from(memberSub)
            ).from(member)
            .fetch();
        for (Tuple tuple : fetch) {
            System.out.println("username = " + tuple.get(member.username));
            System.out.println("age = " +
                tuple.get(JPAExpressions.select(memberSub.age.avg())
                    .from(memberSub)));
        }

    }

    @Test
    void caseTest() {
        List<String> result = queryFactory
            .select(member.username.concat("_").concat(member.age.stringValue()))
            .from(member)
            .fetch();
        for (String string : result) {
            System.out.println(string);
        }
    }

    @Test
    void property() {
        List<MemberDto> result = queryFactory
            .select(Projections.bean(MemberDto.class,
                member.username,
                member.age))
            .from(member)
            .fetch();
        for (MemberDto memberDto : result) {
            System.out.println(memberDto);
        }
    }

    @Test
    void field() {
        QMember memberSub = new QMember("memberSub");
        List<UserDto> result = queryFactory
            .select(Projections.fields(UserDto.class,
                    member.username.as("name"),
                    ExpressionUtils.as(
                        JPAExpressions
                            .select(memberSub.age.max())
                            .from(memberSub), "age")
                )
            ).from(member)
            .fetch();
        for (UserDto memberDto : result) {
            System.out.println(memberDto);
        }
    }

    @Test
    void constructor() {
        List<MemberDto> result = queryFactory
            .select(Projections.constructor(MemberDto.class,
                member.username,
                member.age))
            .from(member)
            .fetch();
        for (MemberDto memberDto : result) {
            System.out.println(memberDto);
        }
    }


    @Test
    void queryProjection() {
        List<MemberDto> result = queryFactory
            .select(new QMemberDto(member.username, member.age))
            .from(member)
            .fetch();
        for (MemberDto memberDto : result) {
            System.out.println(memberDto);
        }
    }


    @Test
    public void 동적쿼리_BooleanBuilder() throws Exception {
        String usernameParam = "member1";
        Integer ageParam = 10;

        List<Member> result = searchMember1(null, ageParam);
        for (Member member1 : result) {
            System.out.println(member1);
        }

    }

    private List<Member> searchMember1(String usernameCond, Integer ageCond) {
        BooleanBuilder builder = new BooleanBuilder();
        if (usernameCond != null) {
            builder.and(member.username.eq(usernameCond));
        }
        if (ageCond != null) {
            builder.and(member.age.gt(ageCond));
        }
        return queryFactory
            .selectFrom(member)
            .where(builder)
            .fetch();
    }
    @Test
    public void 동적쿼리_WhereParam() throws Exception {
        String usernameParam = "member1";
        Integer ageParam = 10;
        List<Member> result = searchMember2(usernameParam, ageParam);
        Assertions.assertThat(result.size()).isEqualTo(1);
    }
    private List<Member> searchMember2(String usernameCond, Integer ageCond) {
        return queryFactory
            .selectFrom(member)
            .where(usernameEq(usernameCond), ageEq(ageCond))
            .fetch();
    }
    private BooleanExpression usernameEq(String usernameCond) {
        return usernameCond != null ? member.username.eq(usernameCond) : null;
    }
    private BooleanExpression ageEq(Integer ageCond) {
        return ageCond != null ? member.age.eq(ageCond) : null;
    }

    @Test
    void test22() {
        Pageable pageable = PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC,
            "username"));
        QueryResults<Member> results = queryFactory.select(member).from(member)
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize()).fetchResults();
        List<Member> content = results.getResults();
        for (Member member1 : content) {
            System.out.println(member1);
        }
        long total = results.getTotal();
        PageImpl page = new PageImpl(content, pageable, total);
    }

    @Test
    void test223() {
        Pageable pageable = PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC,
            "username"));
        List<Member> content = queryFactory
            .select(member)
            .from(member)
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            //fetch로 content만 조회
            .fetch();

        long total = queryFactory
            .select(member)
            .from(member)
            //count 따로 죄회하기
            .fetchCount();

        for (Member member1 : content) {
            System.out.println(member1);
        }

        PageImpl page = new PageImpl(content, pageable, total);
    }
}
