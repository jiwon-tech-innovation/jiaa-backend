package io.github.jiwontechinnovation.user.config;

import io.github.jiwontechinnovation.user.entity.Avatar;
import io.github.jiwontechinnovation.user.repository.AvatarRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AvatarInitializer {

    @Bean
    public CommandLineRunner initAvatars(AvatarRepository avatarRepository) {
        return args -> {
            saveOrUpdateAvatar(avatarRepository, "Hiyori", "Hiyori",
                    "https://project-jiaa-images.s3.ap-northeast-2.amazonaws.com/Hiyori.zip");
            saveOrUpdateAvatar(avatarRepository, "뉵", "뉵",
                    "https://project-jiaa-images.s3.ap-northeast-2.amazonaws.com/뉵.zip");
            saveOrUpdateAvatar(avatarRepository, "Angel MaidFix1", "Angel Maid",
                    "https://project-jiaa-images.s3.ap-northeast-2.amazonaws.com/Angel+Maid.zip");
        };
    }

    private void saveOrUpdateAvatar(AvatarRepository repository, String id, String name, String url) {
        repository.findById(id).ifPresentOrElse(
                avatar -> {
                    avatar.setName(name);
                    avatar.setS3Url(url);
                    repository.save(avatar);
                },
                () -> repository.save(new Avatar(id, name, url)));
    }
}
