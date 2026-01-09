package server.loop.domain.post.entity.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import server.loop.domain.post.entity.Comment;
import server.loop.domain.post.entity.Post;
import server.loop.domain.user.entity.User;

import java.util.List;

import static server.loop.domain.post.entity.QComment.comment;
import static server.loop.domain.post.entity.QPost.post;
import static server.loop.domain.user.entity.QUser.user;

@RequiredArgsConstructor
public class CommentRepositoryImpl implements CommentRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Comment> findAllByPost(Post targetPost) {
        return queryFactory
                .selectFrom(comment)
                .leftJoin(comment.author, user).fetchJoin() // 작성자 N+1 방지
                .where(
                        comment.post.eq(targetPost),
                        comment.isDeleted.isFalse() // 삭제된 댓글 제외
                )
                .orderBy(comment.createdAt.asc())
                .fetch();
    }

    @Override
    public Slice<Post> findActivePostsCommentedByUser(User author, Pageable pageable) {
        List<Post> content = queryFactory
                .select(comment.post)
                .from(comment)
                .join(comment.post, post).fetchJoin()
                .leftJoin(post.author, user).fetchJoin()
                .where(
                        comment.author.eq(author),
                        post.isDeleted.isFalse()
                )
                .groupBy(post.id)
                .orderBy(comment.createdAt.max().desc()) // 가장 최근 댓글 순 정렬 (에러 방지용 max)
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