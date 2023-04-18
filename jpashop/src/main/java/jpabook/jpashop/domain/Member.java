package jpabook.jpashop.domain;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class Member {

    @Id
    @GeneratedValue
    @Column(name = "member_id")
    private Long id;
    private String name;
    //임베디드 값 타입 필드에 넣기
    @Embedded
    private Address address;
    //다대일 양방향 읽기 전용 필드 생성
    //연관관계 주인 mappedBy 지정하기
    @OneToMany(mappedBy = "member")
    private List<Order> orders = new ArrayList<>();
}
