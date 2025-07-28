package server.loop.domain.post.entity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import server.loop.domain.post.entity.Post;

public interface PostRepository extends JpaRepository<Post, Long> {
    // 일단은 기본적인 CRUD 기능만으로 충분합니다.
    // 나중에 특정 카테고리의 게시글만 조회하는 등의 기능이 필요하면 여기에 메소드를 추가할 수 있습니다.
    // 예: List<Post> findByCategory(Category category);
}