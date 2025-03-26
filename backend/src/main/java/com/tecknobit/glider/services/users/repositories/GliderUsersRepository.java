package com.tecknobit.glider.services.users.repositories;

import com.tecknobit.equinoxbackend.environment.services.users.repository.EquinoxUsersRepository;
import com.tecknobit.glider.services.users.entities.GliderUser;
import org.springframework.stereotype.Repository;

@Repository
public interface GliderUsersRepository extends EquinoxUsersRepository<GliderUser> {
}
