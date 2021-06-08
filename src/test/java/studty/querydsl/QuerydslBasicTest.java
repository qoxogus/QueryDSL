package studty.querydsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import studty.querydsl.entity.Member;
import studty.querydsl.entity.QMember;
import studty.querydsl.entity.Team;

import javax.persistence.EntityManager;

import static org.assertj.core.api.Assertions.*;
import static studty.querydsl.entity.QMember.*;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

    @Autowired
    EntityManager em;

    JPAQueryFactory queryFactory; //필드 레벨로 가져가도 괜찮다
     // 멀티스레드에 문제 없게 설계되어있다(em) 여러 멀티스레드에서 들어와도 현재 내 트랜젝션이 어디 걸려있는지에 따라서 트랜젝션에 바인딩 되도록 분배해준다
    //멀티스레드 환경에서 동시성문제없이 동작된다.

    @BeforeEach
    public void before(){
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
    }

    @Test
    public void startJPQL() {
        //member1
        String qlString =  "select m from Member m " +
                            "where m.username = :username";
        Member findMember = em.createQuery(qlString, Member.class)
                .setParameter("username", "member1")
                .getSingleResult();

        assertThat(findMember.getUsername()).isEqualTo("member1");

    }
    
    @Test
    public void startQuerydsl() {
//        QMember m = new QMember("m"); //m 이름에 따라 select m from Member m 쿼리문이 이런식으로 나간다 (같은테이블을 조인해서 쓸 경우에 이렇게 직접 정의해준다 아닐 땐 static import를 해서 쓴다.
//        QMember m = QMember.member;

        Member findMember = queryFactory
//                .select(m)
//                .from(m)
//                .where(m.username.eq("member1")) //파라미터 바인딩 처리
                .select(member) //static import (QMember.member) -> (member)
                .from(member)
                .where(member.username.eq("member1")) //파라미터 바인딩 처리
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

        //member.username.eq  / username = 'member1'
        //member.username.ne  / username != 'member1'
        //member.username.eq("member1").not()  / username != 'member1'

        //member.username.isNotNull()   // 이름이 isNotNull

        //member.age.in(10, 20)   /  age in (10, 20)
        //member.age.notIn(10, 20) / age not in (10, 20)
        //member.age.between(10, 30) / between 10, 30

        //member.age.goe(30)  /  age >= 30
        //member.age.gt(30)  /  age > 30
        //member.age.loe(30)  /  age <= 30
        //member.age.lt(30)  /  age < 30

        //member.username.like("member%")  /  like 검색
        //member.username.contains("member")  /  like '%member%' 검색
        //member.username.startsWith("member")  /  like 'member%' 검색
    }

    @Test
    public void searchAndParam() {
        Member findMember = queryFactory
                .selectFrom(member)
                .where(
                        member.username.eq("member1"), //and로 연결한 것과 같다 (중간에 null이 들어가면 null을 무시한다 {동적쿼리 만들 때 이를 활용하여 기가막히게 코드를 깔끔하게 짤 수 있는 방법이 있다 (이 방법을 선호한다)}
                        member.age.eq(10)
                )
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }
}
