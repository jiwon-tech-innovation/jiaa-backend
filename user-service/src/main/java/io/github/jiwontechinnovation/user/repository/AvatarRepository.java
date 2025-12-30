package io.github.jiwontechinnovation.user.repository;

import io.github.jiwontechinnovation.user.entity.Avatar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AvatarRepository extends JpaRepository<Avatar, String> {
}
