package com.briehman.leadtimeanalyzer.repository;

import com.briehman.leadtimeanalyzer.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {

    @Override
    List<User> findAll();

    Optional<User> findByUsername(@Param("username") String username);
}
