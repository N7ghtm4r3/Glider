package com.tecknobit.glider.services.passwords.entities;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.tecknobit.equinoxbackend.environment.services.builtin.entity.EquinoxItem;
import com.tecknobit.glider.services.users.entities.GliderUser;
import com.tecknobit.glidercore.enums.PasswordType;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;

import java.util.List;

import static com.tecknobit.equinoxcore.helpers.InputsValidator.PASSWORD_MAX_LENGTH;
import static com.tecknobit.glidercore.ConstantsKt.*;
import static com.tecknobit.glidercore.helpers.GliderInputsValidator.SCOPES_MAX_LENGTH;
import static com.tecknobit.glidercore.helpers.GliderInputsValidator.TAIL_MAX_LENGTH;
import static jakarta.persistence.EnumType.STRING;
import static org.hibernate.annotations.OnDeleteAction.CASCADE;

@Entity
@Table(name = PASSWODS_KEY)
public class Password extends EquinoxItem {

    @Column(name = CREATION_DATE_KEY)
    private final long creationDate;

    @Column(
            length = TAIL_MAX_LENGTH
    )
    private final String tail;

    @Column(
            length = PASSWORD_MAX_LENGTH
    )
    private final String password;

    @Column(
            length = SCOPES_MAX_LENGTH
    )
    private final String scopes;

    @Enumerated(value = STRING)
    private final PasswordType type;

    @OneToMany(
            mappedBy = PASSWOD_KEY,
            cascade = CascadeType.ALL
    )
    private final List<PasswordEvent> events;

    @ManyToOne
    @OnDelete(action = CASCADE)
    private GliderUser user;

    public Password() {
        this(null, -1, null, null, null, null, List.of());
    }

    public Password(String id, long creationDate, String tail, String password, String scopes, PasswordType type,
                    List<PasswordEvent> events) {
        super(id);
        this.creationDate = creationDate;
        this.tail = tail;
        this.password = password;
        this.scopes = scopes;
        this.type = type;
        this.events = events;
    }

    @JsonGetter(CREATION_DATE_KEY)
    public long getCreationDate() {
        return creationDate;
    }

    public String getTail() {
        return tail;
    }

    public String getPassword() {
        return password;
    }

    public String getScopes() {
        return scopes;
    }

    public PasswordType getType() {
        return type;
    }

    public List<PasswordEvent> getEvents() {
        return events;
    }

}
