package server.loop.domain.post.entity.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import server.loop.domain.post.entity.Post;
import server.loop.domain.post.entity.PostReport;
import server.loop.domain.user.entity.User;


public interface PostReportRepository extends JpaRepository<PostReport, Long> {
    boolean existsByUserAndPost(User user, Post post);

}