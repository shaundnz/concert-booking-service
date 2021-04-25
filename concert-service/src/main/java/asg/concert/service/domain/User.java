package asg.concert.service.domain;

import javax.persistence.*;

@Entity
@Table(name = "Users")
public class User {

    @Id
    private Long id;
    private String username;
    private String password;
    private Long version;

    public User (Long id, String username, String password, Long version) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.version = version;
    }

    public User () {
        
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    } 

    public void setPassword(String password) {
        this.password = password;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
