package server.loop.domain.post.entity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import server.loop.domain.post.entity.PostImage;

public interface PostImageRepository extends JpaRepository<PostImage, Long> {
}