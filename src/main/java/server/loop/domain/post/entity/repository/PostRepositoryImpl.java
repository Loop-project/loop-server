package server.loop.domain.post.entity.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import server.loop.domain.post.entity.Category;
import server.loop.domain.post.entity.Post;
import server.loop.domain.user.entity.User;

import java.util.List;
import java.util.Optional;

import static server.loop.domain.post.entity.QComment.comment;
import static server.loop.domain.post.entity.QPost.post;
import static server.loop.domain.user.entity.QUser.user;

@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    // 1. 검색 (제목+내용)
    @Override
    public Slice<Post> searchActivePosts(String q, Pageable pageable) {
        return getPostSlice(containsTitleOrContent(q), pageable);
    }

    // 2. 전체 조회
    @Override
    public Slice<Post> findAllActivePosts(Pageable pageable) {
        return getPostSlice(null, pageable);
    }

    // 3. 카테고리별 조회
    @Override
    public Slice<Post> findAllActivePostsByCategory(Category category, Pageable pageable) {
        return getPostSlice(post.category.eq(category), pageable);
    }

    // 4. 작성자별 조회
    @Override
    public Slice<Post> findActivePostsByAuthor(User author, Pageable pageable) {
        return getPostSlice(post.author.eq(author), pageable);
    }

    //공통 로직
    private Slice<Post> getPostSlice(BooleanExpression condition, Pageable pageable) {
        List<Post> content = queryFactory
                .selectFrom(post)
                .leftJoin(post.author, user).fetchJoin()
                .where(
                        post.isDeleted.isFalse(),
                        condition
                )
                .orderBy(post.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        return createSlice(content, pageable);
    }

    // 5. 상세 조회
    @Override
    public Optional<Post> findActivePostWithCommentsById(Long id) {
        return Optional.ofNullable(
                queryFactory
                        .selectFrom(post)
                        .leftJoin(post.author, user).fetchJoin()
                        .leftJoin(post.comments, comment).fetchJoin()
                        .leftJoin(comment.author).fetchJoin()
                        .where(
                                post.id.eq(id),
                                post.isDeleted.isFalse()
                        )
                        .fetchOne()
        );
    }

    private BooleanExpression containsTitleOrContent(String q) {
        if (q == null || q.isBlank()) {
            return null;
        }
        return post.title.containsIgnoreCase(q)
                .or(post.content.containsIgnoreCase(q));
    }

    private Slice<Post> createSlice(List<Post> content, Pageable pageable) {
        boolean hasNext = false;
        if (content.size() > pageable.getPageSize()) {
            content.remove(pageable.getPageSize());
            hasNext = true;
        }
        return new SliceImpl<>(content, pageable, hasNext);
    }
}