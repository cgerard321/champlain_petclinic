/**
 * Created by IntelliJ IDEA.
 * <p>
 * User: @Fube
 * Date: 21/09/21
 * Ticket: feat(AUTH-CPC-95)
 */
package com.auth.authservice.datalayer.roles;

import jakarta.persistence.*;
import lombok.*;


@Table(schema = "auth", name = "roles")
@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Role {

    public Role(long id, String name) {
        this.id = id;
        this.name = name;
        this.parent = null;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String name;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Role parent;
}
