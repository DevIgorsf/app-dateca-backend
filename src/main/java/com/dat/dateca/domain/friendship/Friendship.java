package com.dat.dateca.domain.friendship;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "friendships", uniqueConstraints = @UniqueConstraint(columnNames = {"requester_id", "receiver_id"}))
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Friendship {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "requester_id", nullable = false)
    private UUID requesterId;

    @Column(name = "receiver_id", nullable = false)
    private UUID receiverId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FriendshipStatus status;

    @Column(name = "blocked_by")
    private UUID blockedBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Friendship(UUID requesterId, UUID receiverId) {
        this.requesterId = requesterId;
        this.receiverId = receiverId;
        this.status = FriendshipStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public boolean involves(UUID studentId) {
        return requesterId.equals(studentId) || receiverId.equals(studentId);
    }

    public UUID counterpartOf(UUID studentId) {
        return requesterId.equals(studentId) ? receiverId : requesterId;
    }

    public void resendFrom(UUID requesterId, UUID receiverId) {
        this.requesterId = requesterId;
        this.receiverId = receiverId;
        this.status = FriendshipStatus.PENDING;
        this.acceptedAt = null;
        this.blockedBy = null;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void accept() {
        this.status = FriendshipStatus.ACCEPTED;
        this.acceptedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void decline() {
        this.status = FriendshipStatus.DECLINED;
        this.updatedAt = LocalDateTime.now();
    }

    public void cancel() {
        this.status = FriendshipStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }

    public void remove() {
        this.status = FriendshipStatus.REMOVED;
        this.updatedAt = LocalDateTime.now();
    }

    public void block(UUID blockerId) {
        this.status = FriendshipStatus.BLOCKED;
        this.blockedBy = blockerId;
        this.updatedAt = LocalDateTime.now();
    }

    public void unblock() {
        this.status = FriendshipStatus.REMOVED;
        this.blockedBy = null;
        this.updatedAt = LocalDateTime.now();
    }
}
