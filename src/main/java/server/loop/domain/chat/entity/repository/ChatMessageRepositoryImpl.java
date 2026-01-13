package server.loop.domain.chat.entity.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import server.loop.domain.chat.entity.ChatMessage;

import java.util.List;

import static server.loop.domain.chat.entity.QChatMessage.chatMessage;
import static server.loop.domain.user.entity.QUser.user;

@RequiredArgsConstructor
public class ChatMessageRepositoryImpl implements ChatMessageRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<ChatMessage> findMessages(String roomId, Long lastMessageId, Pageable pageable) {
        return queryFactory
                .selectFrom(chatMessage)
                .leftJoin(chatMessage.sender, user).fetchJoin() // 발신자 정보 미리 로딩
                .where(
                        chatMessage.room.id.eq(roomId),
                        ltMessageId(lastMessageId) // 커서(동적 조건)
                )
                .orderBy(chatMessage.id.desc())
                .limit(pageable.getPageSize())
                .fetch();
    }

    private BooleanExpression ltMessageId(Long lastMessageId) {
        if (lastMessageId == null) {
            return null; // 첫 페이지면 조건 없음
        }
        return chatMessage.id.lt(lastMessageId); // 지정된 ID보다 작은 메시지들만
    }
}
