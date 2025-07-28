package server.loop.domain.post.dto.post.res;

import lombok.Getter;
import org.springframework.data.domain.Slice;

import java.util.List;

@Getter
public class SliceResponseDto<T> {
    private final List<T> content;
    private final boolean hasNext;
    private final int currentPage;
    private final int size;

    public SliceResponseDto(Slice<T> slice) {
        this.content = slice.getContent();
        this.hasNext = slice.hasNext();
        this.currentPage = slice.getNumber();
        this.size = slice.getSize();
    }
}