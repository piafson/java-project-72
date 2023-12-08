package hexlet.code.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@AllArgsConstructor
public final class Url {

    private long id;
    private String name;
    private Timestamp createdAt;

    public Url(String name) {
        this.name = name;
    }
}
