package team.nine.booknutsbackend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import team.nine.booknutsbackend.domain.Board;
import team.nine.booknutsbackend.domain.User;
import team.nine.booknutsbackend.dto.request.BoardRequest;
import team.nine.booknutsbackend.dto.response.BoardResponse;
import team.nine.booknutsbackend.exception.board.NoAccessException;
import team.nine.booknutsbackend.service.BoardService;
import team.nine.booknutsbackend.service.UserService;

import javax.validation.Valid;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/board")
public class BoardController {

    private final BoardService boardService;
    private final UserService userService;

    //게시글 작성
    @PostMapping("/write")
    public ResponseEntity<BoardResponse> writePost(@RequestBody @Valid BoardRequest board, Principal principal) {
        User user = userService.findUserByEmail(principal.getName());
        Board newBoard = boardService.writePost(BoardRequest.newBoard(board, user));
        return new ResponseEntity<>(BoardResponse.boardResponse(newBoard, user), HttpStatus.CREATED);
    }

    //게시글 목록 조회
    //나의 구독 = 0, 오늘 추천 = 1, 독립 출판 = 2
    @GetMapping("/list/{type}")
    public ResponseEntity<List<BoardResponse>> getBoard(@PathVariable int type, Principal principal) {
        User user = userService.findUserByEmail(principal.getName());
        return new ResponseEntity<>(boardService.getBoard(user, type), HttpStatus.OK);
    }

    //내가 작성한 게시글 목록
    @GetMapping("/mypost")
    public ResponseEntity<List<BoardResponse>> getMyBoard(Principal principal){
        User user = userService.findUserByEmail(principal.getName());
        return new ResponseEntity<>(boardService.getMyBoard(user), HttpStatus.OK);
    }

    //특정 게시글 조회
    @GetMapping("/{boardId}")
    public ResponseEntity<BoardResponse> getPost(@PathVariable Long boardId, Principal principal) {
        User user = userService.findUserByEmail(principal.getName());
        return new ResponseEntity<>(BoardResponse.boardResponse(boardService.getPost(boardId), user), HttpStatus.OK);
    }

    //게시글 수정
    @PatchMapping("/{boardId}")
    public ResponseEntity<BoardResponse> updatePost(@PathVariable Long boardId, @RequestBody BoardRequest board, Principal principal) throws NoAccessException {
        Board originBoard = boardService.getPost(boardId);
        User user = userService.findUserByEmail(principal.getName());

        if (board.getTitle() != null) originBoard.setTitle(board.getTitle());
        if (board.getContent() != null) originBoard.setContent(board.getContent());

        Board updateBoard = boardService.updatePost(originBoard, user);
        return new ResponseEntity<>(BoardResponse.boardResponse(updateBoard, user), HttpStatus.OK);
    }

    //게시글 삭제
    @DeleteMapping("/{boardId}")
    public ResponseEntity<Object> deletePost(@PathVariable Long boardId, Principal principal) throws NoAccessException {
        User user = userService.findUserByEmail(principal.getName());
        boardService.deletePost(boardId, user);

        Map<String, String> map = new HashMap<>();
        map.put("result", "삭제 완료");
        return new ResponseEntity<>(map, HttpStatus.OK);
    }

}
