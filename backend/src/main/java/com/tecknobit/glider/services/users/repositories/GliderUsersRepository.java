package com.tecknobit.glider.services.users.repositories;

import com.tecknobit.equinoxbackend.environment.services.users.entity.EquinoxUser;
import com.tecknobit.equinoxbackend.environment.services.users.repository.EquinoxUsersRepository;
import com.tecknobit.glider.services.users.entities.GliderUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * The {@code GliderUsersRepository} interface is useful to manage the queries for the users operations
 *
 * @author N7ghtm4r3 - Tecknobit
 * @see JpaRepository
 * @see EquinoxUser
 * @see EquinoxUsersRepository
 */
@Repository
public interface GliderUsersRepository extends EquinoxUsersRepository<GliderUser> {
}
