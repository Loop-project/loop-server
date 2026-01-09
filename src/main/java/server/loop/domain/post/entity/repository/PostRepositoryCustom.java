package server.loop.domain.post.entity.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import server.loop.domain.post.entity.Category;
import server.loop.domain.post.entity.Post;
import server.loop.domain.user.entity.User;

import java.util.Optional;

public interface PostRepositoryCustom {
    Slice<Post> searchActivePosts(String q, Pageable pageable);

    Slice<Post> findAllActivePosts(Pageable pageable);

    Slice<Post> findAllActivePostsByCategory(Category category, Pageable pageable);

    Optional<Post> findActivePostWithCommentsById(Long id);

    Slice<Post> findActivePostsByAuthor(User author, Pageable pageable);
}
