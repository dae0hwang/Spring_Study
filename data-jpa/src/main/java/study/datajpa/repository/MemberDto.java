package study.datajpa.repository;

import lombok.Data;
import study.datajpa.entity.BaseEntity;

@Data
public class MemberDto {

    private Long id;
    private String username;
    private String teamName;

    public MemberDto(Long id, String username, String teamName) {
        this.id = id;
        this.username = username;
        this.teamName = teamName;
    }
}