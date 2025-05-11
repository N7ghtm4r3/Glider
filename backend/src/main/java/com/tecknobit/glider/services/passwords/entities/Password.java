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
import static com.tecknobit.equinoxcore.helpers.CommonKeysKt.CREATION_DATE_KEY;
import static com.tecknobit.equinoxcore.helpers.CommonKeysKt.PASSWORD_KEY;
import static com.tecknobit.glidercore.ConstantsKt.EVENT_DATE_KEY;
import static com.tecknobit.glidercore.ConstantsKt.PASSWORDS_KEY;
import static jakarta.persistence.EnumType.STRING;
import static org.hibernate.annotations.OnDeleteAction.CASCADE;

/**
 * The {@code Password} class is used to represent a password stored in the Glider's system
 *
 * @author N7ghtm4r3 - Tecknobit
 * @see EquinoxItem
 */
@Entity
@Table(name = PASSWORDS_KEY)
public class Password extends EquinoxItem {

    /**
     * {@code creationDate} the date when the password has been created
     */
    @Column(name = CREATION_DATE_KEY)
    private final long creationDate;

    /**
     * {@code tail} the tail of the password
     */
    @Column(unique = true)
    private String tail;

    /**
     * {@code password} the password value
     */
    @Column
    private String password;

    /**
     * {@code scopes} the scopes of the password
     */
    @Column
    private String scopes;

    /**
     * {@code type} the type of the password
     */
    @Enumerated(value = STRING)
    private final PasswordType type;

    /**
     * {@code events} the events related to the lifecycle of the password
     */
    @OneToMany(
            mappedBy = PASSWORD_KEY
    )
    @OrderBy(EVENT_DATE_KEY + " DESC")
    @JsonIgnoreProperties(PASSWORD_KEY)
    private final List<PasswordEvent> events;

    /**
     * {@code user} the user owner of the password
     */
    @ManyToOne
    @OnDelete(action = CASCADE)
    private final GliderUser user;

    /**
     * {@code configuration} the configuration used to generate the password
     */
    @OneToOne(
            mappedBy = PASSWORD_KEY,
            cascade = CascadeType.ALL
    )
    private final PasswordConfiguration configuration;

    /**
     * Constructor to init the {@link Password} class
     *
     * @apiNote empty constructor required
     */
    public Password() {
        this(null, -1, null, null, null, null, List.of(), null, null);
    }

    /**
     * Constructor to init the {@link Password} class
     *
     * @param id            The identifier of the password
     * @param creationDate  The date when the password has been created
     * @param tail          The tail of the password
     * @param password      The value of the password
     * @param scopes        The scopes of the password
     * @param type          The type of the password
     * @param configuration The configuration used to generate the password
     * @param user          The user owner of the password
     */
    public Password(String id, long creationDate, String tail, String password, String scopes, PasswordType type,
                    PasswordConfiguration configuration, GliderUser user) {
        this(id, creationDate, tail, password, scopes, type, List.of(), configuration, user);
    }

    /**
     * Constructor to init the {@link Password} class
     *
     * @param id The identifier of the password
     * @param creationDate The date when the password has been created
     * @param tail The tail of the password
     * @param password The value of the password
     * @param scopes The scopes of the password
     * @param type The type of the password
     * @param events The events related to the lifecycle of the password
     * @param configuration The configuration used to generate the password
     * @param user The user owner of the password
     */
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

    /**
     * Method used to get the {@link #creationDate} instance
     *
     * @return the {@link #creationDate} instance as {@code long}
     */
    @JsonGetter(CREATION_DATE_KEY)
    public long getCreationDate() {
        return creationDate;
    }

    /**
     * Method used to get the {@link #tail} instance
     *
     * @return the {@link #tail} instance as {@link String}
     */
    public String getTail() {
        return tail;
    }

    /**
     * Method used to get the {@link #password} instance
     *
     * @return the {@link #password} instance as {@link String}
     */
    public String getPassword() {
        return password;
    }

    /**
     * Method used to get the {@link #scopes} instance
     *
     * @return the {@link #scopes} instance as {@link String}
     */
    public String getScopes() {
        return scopes;
    }

    /**
     * Method used to set the decrypted data of the password
     *
     * @param decryptedData The decrypted data to set
     */
    @JsonIgnore
    public void setDecryptedData(Triple<String, String, String> decryptedData) {
        tail = decryptedData.getFirst();
        scopes = decryptedData.getSecond();
        password = decryptedData.getThird();
    }

    /**
     * Method used to get the {@link #type} instance
     *
     * @return the {@link #type} instance as {@link PasswordType}
     */
    public PasswordType getType() {
        return type;
    }

    /**
     * Method used to get the {@link #events} instance
     *
     * @return the {@link #events} instance as {@link List} of {@link PasswordEvent}
     */
    public List<PasswordEvent> getEvents() {
        return events;
    }

    /**
     * Method used to get the {@link #configuration} instance
     *
     * @return the {@link #configuration} instance as {@link PasswordConfiguration}
     */
    @JsonIgnore
    public PasswordConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * Method used to get the {@link #user} instance
     *
     * @return the {@link #user} instance as {@link GliderUser}
     */
    @JsonIgnore
    public GliderUser getUser() {
        return user;
    }

    /**
     * Method used to check whether the filter keywords match with any {@link #scopes} of the password
     *
     * @param keywords The keywords filter to compare
     *
     * @return whether the filter keywords match with any {@link #scopes} of the password as {@code boolean}
     */
    @JsonIgnore
    public boolean scopesMatch(Set<String> keywords) {
        Set<String> scopesSet = new HashSet<>(List.of(scopes.toLowerCase().split(COMMA)));
        for (String scope : scopesSet)
            for (String keyword : keywords)
                if (scope.contains(keyword))
                    return true;
        return false;
    }

}