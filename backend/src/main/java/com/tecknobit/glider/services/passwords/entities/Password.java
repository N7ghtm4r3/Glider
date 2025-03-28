package com.tecknobit.glider.services.passwords.entities;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tecknobit.equinoxbackend.environment.services.builtin.entity.EquinoxItem;
import com.tecknobit.glider.services.users.entities.GliderUser;
import com.tecknobit.glidercore.enums.PasswordType;
import jakarta.persistence.*;
import kotlin.Triple;
import org.hibernate.annotations.OnDelete;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.tecknobit.equinoxbackend.environment.services.builtin.service.EquinoxItemsHelper.COMMA;
import static com.tecknobit.equinoxcore.helpers.CommonKeysKt.PASSWORD_KEY;
import static com.tecknobit.glidercore.ConstantsKt.*;
import static jakarta.persistence.EnumType.STRING;
import static org.hibernate.annotations.OnDeleteAction.CASCADE;

@Entity
@Table(name = PASSWORDS_KEY)
public class Password extends EquinoxItem {

    @Column(name = CREATION_DATE_KEY)
    private final long creationDate;

    @Column(unique = true)
    private String tail;

    @Column
    private String password;

    @Column
    private String scopes;

    @Enumerated(value = STRING)
    private final PasswordType type;

    @OneToMany(
            mappedBy = PASSWORD_KEY
    )
    @OrderBy(EVENT_DATE_KEY + " DESC")
    @JsonIgnoreProperties(PASSWORD_KEY)
    private final List<PasswordEvent> events;

    @ManyToOne
    @OnDelete(action = CASCADE)
    private final GliderUser user;

    @OneToOne(
            mappedBy = PASSWORD_KEY,
            cascade = CascadeType.ALL
    )
    private final PasswordConfiguration configuration;

    public Password() {
        this(null, -1, null, null, null, null, List.of(), null, null);
    }

    public Password(String id, long creationDate, String tail, String password, String scopes, PasswordType type,
                    PasswordConfiguration configuration, GliderUser user) {
        this(id, creationDate, tail, password, scopes, type, List.of(), configuration, user);
    }

    public Password(String id, long creationDate, String tail, String password, String scopes, PasswordType type,
                    List<PasswordEvent> events, PasswordConfiguration configuration, GliderUser user) {
        super(id);
        this.creationDate = creationDate;
        this.tail = tail;
        this.password = password;
        this.scopes = scopes;
        this.type = type;
        this.events = events;
        this.configuration = configuration;
        this.user = user;
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

    @JsonIgnore
    public void setDecryptedData(Triple<String, String, String> encryptedData) {
        tail = encryptedData.getFirst();
        scopes = encryptedData.getSecond();
        password = encryptedData.getThird();
    }

    public PasswordType getType() {
        return type;
    }

    public List<PasswordEvent> getEvents() {
        return events;
    }

    @JsonIgnore
    public PasswordConfiguration getConfiguration() {
        return configuration;
    }

    @JsonIgnore
    public GliderUser getUser() {
        return user;
    }

    public boolean scopesMatch(Set<String> keywords) {
        Set<String> scopesSet = new HashSet<>(List.of(scopes.toLowerCase().split(COMMA)));
        for (String scope : scopesSet)
            for (String keyword : keywords)
                if (scope.contains(keyword))
                    return true;
        return false;
    }

}