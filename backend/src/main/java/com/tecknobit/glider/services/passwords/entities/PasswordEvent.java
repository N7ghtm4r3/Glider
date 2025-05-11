package com.tecknobit.glider.services.passwords.entities;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tecknobit.equinoxbackend.annotations.EmptyConstructor;
import com.tecknobit.equinoxbackend.environment.services.builtin.entity.EquinoxItem;
import com.tecknobit.glidercore.enums.PasswordEventType;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;

import static com.tecknobit.glidercore.ConstantsKt.EVENT_DATE_KEY;
import static com.tecknobit.glidercore.ConstantsKt.PASSWORD_EVENTS_KEY;
import static jakarta.persistence.EnumType.STRING;
import static org.hibernate.annotations.OnDeleteAction.CASCADE;

/**
 * The {@code PasswordEvent} class is used to represent the event related to the {@link Password}'s lifecycle
 *
 * @author N7ghtm4r3 - Tecknobit
 * @see EquinoxItem
 */
@Entity
@Table(name = PASSWORD_EVENTS_KEY)
public class PasswordEvent extends EquinoxItem {

    /**
     * {@code eventDate} when the event occurred
     */
    @Column(name = EVENT_DATE_KEY)
    private final long eventDate;

    /**
     * {@code type} the type of the event
     */
    @Enumerated(value = STRING)
    private final PasswordEventType type;

    /**
     * {@code password} the password owner of the event
     */
    @ManyToOne(
            cascade = CascadeType.ALL
    )
    @OnDelete(action = CASCADE)
    @JsonIgnoreProperties(PASSWORD_EVENTS_KEY)
    private final Password password;

    /**
     * Constructor to init the {@link PasswordEvent} class
     *
     * @apiNote empty constructor required
     */
    @EmptyConstructor
    public PasswordEvent() {
        this(null, -1, null, null);
    }

    /**
     * Constructor to init the {@link PasswordEvent} class
     *
     * @param id        The identifier of the event
     * @param eventDate When the event occurred
     * @param type      The type of the event
     * @param password  The password owner of the event
     */
    public PasswordEvent(String id, long eventDate, PasswordEventType type, Password password) {
        super(id);
        this.eventDate = eventDate;
        this.type = type;
        this.password = password;
    }

    /**
     * Method used to get the {@link #eventDate} instance
     *
     * @return the {@link #eventDate} instance as {@code long}
     */
    @JsonGetter(EVENT_DATE_KEY)
    public long getEventDate() {
        return eventDate;
    }

    /**
     * Method used to get the {@link #type} instance
     *
     * @return the {@link #type} instance as {@link  PasswordEventType}
     */
    public PasswordEventType getType() {
        return type;
    }

    /**
     * Method used to get the {@link #password} instance
     *
     * @return the {@link #password} instance as {@link  Password}
     */
    public Password getPassword() {
        return password;
    }

}
