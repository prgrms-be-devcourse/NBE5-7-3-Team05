package io.powerrangers.backend.service;

import io.powerrangers.backend.dto.TaskScope;
import io.powerrangers.backend.dto.TaskStatus;
import io.powerrangers.backend.dto.CommentCreateRequestDto;
import io.powerrangers.backend.dto.CommentResponseDto;
import io.powerrangers.backend.dto.CommentUpdateRequestDto;
import io.powerrangers.backend.entity.Comment;
import io.powerrangers.backend.entity.Task;
import io.powerrangers.backend.entity.User;
import io.powerrangers.backend.dao.CommentRepository;
import io.powerrangers.backend.dao.TaskRepository;
import io.powerrangers.backend.dao.UserRepository;
import io.powerrangers.backend.exception.CustomException;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@DisplayName("댓글기능 테스트")
@Transactional
public class CommentServiceTests {

    @Autowired
    CommentService commentService;

    @Autowired
    CommentRepository commentRepository;

    @Autowired
    TaskRepository taskRepository;

    @Autowired
    UserRepository userRepository;

    private User testUser;
    private Task testTask;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .nickname("tester")
                .email("tester@naver.com")
                .provider("naver")
                .providerId("naver123")
                .profileImage("http://image.url")
                .build();

        userRepository.save(testUser);

        testTask = Task.builder()
                .category("공부")
                .content("스프링 공부하기")
                .dueDate(LocalDateTime.now().plusDays(3))
                .status(TaskStatus.COMPLETE)
                .taskImage("http://task.img")
                .scope(TaskScope.PUBLIC)
                .user(testUser)
                .build();

        taskRepository.save(testTask);
    }

    @Test
    @DisplayName("최상위 부모 댓글 작성")
    void 최상위댓글() {
        // given
        CommentCreateRequestDto dto = new CommentCreateRequestDto(testTask.getId(), testUser.getId(), null, "최상위 댓글입니다.");

        // when
        commentService.createComment(dto.getUserId(), dto);

        // then
        List<Comment> all = commentRepository.findAll();
        assertThat(all).hasSize(1);
        Comment comment = all.get(0);
        assertThat(comment.getContent()).isEqualTo("최상위 댓글입니다.");
        assertThat(comment.getParent()).isNull();
    }

    @Test
    @DisplayName("기존 부모 댓글이 있는 상태에서 대댓글을 제대로 작성할 수 있는지 테스트")
    void 대댓글() {
        // given
        Comment parent = new Comment(testTask, testUser, null, "부모 댓글입니다.");
        commentRepository.save(parent);

        CommentCreateRequestDto dto = new CommentCreateRequestDto(testTask.getId(), testUser.getId(), parent.getId(), "대댓글입니다");

        // when
        commentService.createComment(dto.getUserId(), dto);

        // then
        List<Comment> comments = commentRepository.findAll();
        assertThat(comments).hasSize(2);

        // 대댓글 찾기
        Comment reply = comments.stream()
                .filter(c -> c.getParent() != null)
                .findFirst()
                .orElseThrow();

        assertThat(reply.getContent()).isEqualTo("대댓글입니다");
        assertThat(reply.getParent().getId()).isEqualTo(parent.getId());
    }

    @Test
    @DisplayName("부모 댓글 있는 상태에서 대댓글2 작성 검증")
    void 대댓글2() {
        //given
        Comment parent = new Comment(testTask, testUser, null, "부모 댓글입니다.");
        commentRepository.save(parent);
        Comment child = new Comment(testTask, testUser, parent, "대댓글입니다");
        commentRepository.save(child);

        CommentCreateRequestDto dto = new CommentCreateRequestDto(testTask.getId(), testUser.getId(), parent.getId(), "대댓글2입니다");

        //when
        commentService.createComment(dto.getUserId(), dto);

        //then
        List<Comment> comments = commentRepository.findAll();
        assertThat(comments).hasSize(3);

        // 대댓글 찾기
        Comment reply = comments.stream()
                .filter(c -> "대댓글2입니다".equals(c.getContent()))
                .findFirst()
                .orElseThrow();

        assertThat(reply.getContent()).isEqualTo("대댓글2입니다");
        assertThat(reply.getParent().getId()).isEqualTo(parent.getId());

        assertThat(child.getParent().getId()).isEqualTo(reply.getParent().getId());
    }

    @Test
    @DisplayName("조회 검증")
    void 조회() {
        Comment parent = new Comment(testTask, testUser, null, "부모 댓글입니다.");
        commentRepository.save(parent);

        Comment child1 = new Comment(testTask, testUser, parent, "대댓글입니다");
        commentRepository.save(child1);

        Comment child2 = new Comment(testTask, testUser, parent, "불라불라");
        commentRepository.save(child2);

        List<CommentResponseDto> result = commentService.getComments(testTask.getId());

        assertThat(result).hasSize(1); // 부모 댓글 하나
        CommentResponseDto parentDto = result.get(0);
        assertThat(parentDto.getContent()).isEqualTo("부모 댓글입니다.");

        assertThat(parentDto.getChildren()).hasSize(2);
        assertThat(parentDto.getChildren())
                .extracting(CommentResponseDto::getContent)
                .containsExactlyInAnyOrder("대댓글입니다", "불라불라");
    }

    @Test
    @DisplayName("댓글 수정 테스트")
    void 댓글수정() {
        Comment parent = new Comment(testTask, testUser, null, "부모댓글");
        commentRepository.save(parent);
        Comment child1 = new Comment(testTask, testUser, parent, "자식댓글");
        commentRepository.save(child1);

        String newContent = "아싸 수정됐다.";
        CommentUpdateRequestDto dto = CommentUpdateRequestDto.builder()
                .content(newContent)
                .build();
        commentService.updateComment(parent.getId(), dto);

        Comment updatedParent = commentRepository.findById(parent.getId())
                .orElseThrow(() -> new RuntimeException("댓글 수정 실패"));

        assertThat(updatedParent.getContent()).isEqualTo(newContent);
    }

    @Test
    @DisplayName("댓글 수정 본인 댓글이 아닌경우")
    void 댓글수정2() {
        /*
         * TODO: 사용자 검증으로 본인의 댓글인지 판단하고 예외를 터트리는 테스트를 만들자 (아직 기능 미구현)
         *
         * */
    }

    @Test
    @DisplayName("댓글 삭제")
    void 댓글삭제() {
        Comment parent = new Comment(testTask, testUser, null, "삭제할 댓글입니다.");
        commentRepository.save(parent);

        commentService.deleteComment(parent.getId());

        boolean exists = commentRepository.existsById(parent.getId());
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("댓글 삭제 본인이 아닌 경우")
    void 댓글삭제권한X() {
        Comment parent = new Comment(testTask, testUser, null, "삭제할 댓글입니다.");
        commentRepository.save(parent);

        User attacker = User.builder()
                .nickname("hacker")
                .email("bad@naver.com")
                .provider("naver")
                .providerId("hacker123")
                .profileImage("http://image.url")
                .build();
        userRepository.save(attacker);

        //Todo: 예외부 구현
    }

    @Test
    @DisplayName("댓글 삭제 존재하지 않는 댓글 예외")
    void 삭제시댓글없음() {
        Long nonexistentId = 999L;

        assertThatThrownBy(() -> commentService.deleteComment(nonexistentId))
                .isInstanceOf(CustomException.class);
    }
}