package com.dat.dateca.controller;

import com.dat.dateca.domain.friendship.FriendDTO;
import com.dat.dateca.domain.friendship.FriendshipRequestDTO;
import com.dat.dateca.domain.friendship.FriendshipResponseDTO;
import com.dat.dateca.domain.friendship.FriendshipService;
import com.dat.dateca.domain.friendship.UserSearchDTO;
import com.dat.dateca.domain.student.Student;
import com.dat.dateca.domain.student.StudentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class FriendshipController {

    @Autowired
    private FriendshipService friendshipService;

    @Autowired
    private StudentService studentService;

    @PostMapping("/friendships")
    public ResponseEntity<FriendshipResponseDTO> sendRequest(@RequestBody @Valid FriendshipRequestDTO dto) {
        Student current = studentService.getAuthenticatedStudent();
        return ResponseEntity.status(HttpStatus.CREATED).body(friendshipService.sendRequest(current, dto.receiverId()));
    }

    @PatchMapping("/friendships/{id}/accept")
    public ResponseEntity<FriendshipResponseDTO> accept(@PathVariable UUID id) {
        Student current = studentService.getAuthenticatedStudent();
        return ResponseEntity.ok(friendshipService.accept(current, id));
    }

    @PatchMapping("/friendships/{id}/decline")
    public ResponseEntity<FriendshipResponseDTO> decline(@PathVariable UUID id) {
        Student current = studentService.getAuthenticatedStudent();
        return ResponseEntity.ok(friendshipService.decline(current, id));
    }

    @PatchMapping("/friendships/{id}/cancel")
    public ResponseEntity<FriendshipResponseDTO> cancel(@PathVariable UUID id) {
        Student current = studentService.getAuthenticatedStudent();
        return ResponseEntity.ok(friendshipService.cancel(current, id));
    }

    @DeleteMapping("/friendships/{id}")
    public ResponseEntity<Void> remove(@PathVariable UUID id) {
        Student current = studentService.getAuthenticatedStudent();
        friendshipService.remove(current, id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/friendships/{id}/block")
    public ResponseEntity<FriendshipResponseDTO> block(@PathVariable UUID id) {
        Student current = studentService.getAuthenticatedStudent();
        return ResponseEntity.ok(friendshipService.block(current, id));
    }

    @PatchMapping("/friendships/{id}/unblock")
    public ResponseEntity<FriendshipResponseDTO> unblock(@PathVariable UUID id) {
        Student current = studentService.getAuthenticatedStudent();
        return ResponseEntity.ok(friendshipService.unblock(current, id));
    }

    @GetMapping("/me/friends")
    public ResponseEntity<Page<FriendDTO>> listFriends(@PageableDefault(size = 20) Pageable pageable) {
        Student current = studentService.getAuthenticatedStudent();
        return ResponseEntity.ok(friendshipService.listFriends(current, pageable));
    }

    @GetMapping("/me/friends/count")
    public ResponseEntity<Long> countFriends() {
        Student current = studentService.getAuthenticatedStudent();
        return ResponseEntity.ok(friendshipService.countFriends(current));
    }

    @GetMapping("/me/friendships/received")
    public ResponseEntity<Page<FriendshipResponseDTO>> listReceived(@PageableDefault(size = 20) Pageable pageable) {
        Student current = studentService.getAuthenticatedStudent();
        return ResponseEntity.ok(friendshipService.listReceived(current, pageable));
    }

    @GetMapping("/me/friendships/sent")
    public ResponseEntity<Page<FriendshipResponseDTO>> listSent(@PageableDefault(size = 20) Pageable pageable) {
        Student current = studentService.getAuthenticatedStudent();
        return ResponseEntity.ok(friendshipService.listSent(current, pageable));
    }

    @GetMapping("/users/search")
    public ResponseEntity<Page<UserSearchDTO>> search(@RequestParam String q, @PageableDefault(size = 20) Pageable pageable) {
        Student current = studentService.getAuthenticatedStudent();
        return ResponseEntity.ok(friendshipService.searchUsers(current, q, pageable));
    }
}
