package server.loop.domain.post.entity.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import server.loop.domain.post.entity.Post;
import server.loop.domain.user.entity.User;

import java.util.List;

import static server.loop.domain.post.entity.QComment.comment;
import static server.loop.domain.post.entity.QPost.post;
import static server.loop.domain.user.entity.QUser.user;

@RequiredArgsConstructor
public class CommentRepositoryImpl implements CommentRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    // 내가 댓글 단 게시글 찾기 (중복 제거)
    @Override
    public Slice<Post> findActivePostsCommentedByUser(User author, Pageable pageable) {
        List<Post> content = queryFactory
                .selectDistinct(comment.post) // 게시글 기준 DISTINCT
                .from(comment)
                .join(comment.post, post).fetchJoin()       // 게시글 로딩
                .leftJoin(post.author, user).fetchJoin()    // 게시글 작성자도 미리 로딩
                .where(
                        comment.author.eq(author),
                        post.isDeleted.isFalse() // 삭제된 게시글 제외
                )
                .orderBy(comment.createdAt.max().desc()) // 최신 게시글 순 max를 넣은 이유는 가장 최신을 가져오기 위함.
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
}
