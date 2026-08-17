package library;

import java.sql.Date;

public class Member {
    private int memberId;
    private String name;
    private String email;
    private Date joinDate;

    public Member(int memberId, String name, String email, Date joinDate) {
        this.memberId = memberId;
        this.name = name;
        this.email = email;
        this.joinDate = joinDate;
    }

    public int getMemberId() { return memberId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public Date getJoinDate() { return joinDate; }
}