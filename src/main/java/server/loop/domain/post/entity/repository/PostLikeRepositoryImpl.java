package server.loop.domain.post.entity.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import server.loop.domain.post.entity.Post;
import server.loop.domain.user.entity.User;

import java.time.LocalDateTime;
import java.util.List;

import static server.loop.domain.post.entity.QPost.post;
import static server.loop.domain.post.entity.QPostLike.postLike;
import static server.loop.domain.user.entity.QUser.user;

@RequiredArgsConstructor
public class PostLikeRepositoryImpl implements PostLikeRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Slice<Post> findActivePostsLikedByUser(User targetUser, Pageable pageable) {
        List<Post> content = queryFactory
                .select(postLike.post)
                .from(postLike)
                .join(postLike.post, post).fetchJoin() // 게시글 정보 로딩
                .leftJoin(post.author, user).fetchJoin() // 게시글 작성자 정보 로딩 (N+1 방지)
                .where(
                        postLike.user.eq(targetUser),
                        post.isDeleted.isFalse() // 삭제된 게시글 제외
                )
                .orderBy(post.createdAt.desc()) // 게시글 최신순
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        boolean hasNext = false;
        if (content.size() > pageable.getPageSize()) {
            content.remove(pageable.getPageSize());
            hasNext = true;
        }

        return new SliceImpl<>(content, pageable, hasNext);
    }

    @Override
    public List<Post> findTopPostsCreatedInPeriodOrderByLikes(LocalDateTime start, LocalDateTime end) {
        return queryFactory
                .select(post)
                .from(post)
                .leftJoin(post.likes, postLike)
                .leftJoin(post.author, user).fetchJoin() // 작성자 정보 미리 로딩 (성능 최적화)
                .where(
                        post.createdAt.between(start, end),
                        post.isDeleted.isFalse()
                )
                .groupBy(post.id) // 게시글 ID로 그룹화
                .orderBy(
                        postLike.count().desc(), // 좋아요 개수 내림차순
                        post.createdAt.desc()    // 좋아요 같으면 최신순
                )
                .limit(5)
                .fetch();
    }
}