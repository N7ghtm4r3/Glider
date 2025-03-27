package com.tecknobit.glider.services.passwords.entities;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.tecknobit.equinoxbackend.environment.services.builtin.entity.EquinoxItem;
import com.tecknobit.glidercore.enums.PasswordEventType;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;

import static com.tecknobit.glidercore.ConstantsKt.EVENT_DATE_KEY;
import static com.tecknobit.glidercore.ConstantsKt.PASSWORD_EVENTS_KEY;
import static jakarta.persistence.EnumType.STRING;
import static org.hibernate.annotations.OnDeleteAction.CASCADE;

@Entity
@Table(name = PASSWORD_EVENTS_KEY)
public class PasswordEvent extends EquinoxItem {

    @Column(name = EVENT_DATE_KEY)
    private final long eventDate;

    @Enumerated(value = STRING)
    private final PasswordEventType type;

    @ManyToOne(
            cascade = CascadeType.ALL
    )
    @OnDelete(action = CASCADE)
    private Password password;

    public PasswordEvent() {
        this(null, -1, null);
    }

    public PasswordEvent(String id, long eventDate, PasswordEventType type) {
        super(id);
        this.eventDate = eventDate;
        this.type = type;
    }

    @JsonGetter(EVENT_DATE_KEY)
    public long getEventDate() {
        return eventDate;
    }

    public PasswordEventType getType() {
        return type;
    }

}
