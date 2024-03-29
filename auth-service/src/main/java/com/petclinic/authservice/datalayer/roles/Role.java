/**
 * Created by IntelliJ IDEA.
 * <p>
 * User: @Fube
 * Date: 21/09/21
 * Ticket: feat(AUTH-CPC-95)
 */
package com.petclinic.authservice.datalayer.roles;

import jakarta.persistence.*;
import lombok.*;


@Table(name = "roles")
@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Role {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String name;

}
