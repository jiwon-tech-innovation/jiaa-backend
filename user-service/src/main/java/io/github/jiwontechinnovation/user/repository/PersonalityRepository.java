package io.github.jiwontechinnovation.user.repository;

import io.github.jiwontechinnovation.user.entity.Personality;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonalityRepository extends JpaRepository<Personality, String> {
}
