package com.dat.dateca.domain.friendship;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FriendshipRepository extends JpaRepository<Friendship, UUID> {

    @Query("SELECT f FROM Friendship f WHERE (f.requesterId = :a AND f.receiverId = :b) OR (f.requesterId = :b AND f.receiverId = :a)")
    Optional<Friendship> findBetween(@Param("a") UUID a, @Param("b") UUID b);

    Page<Friendship> findAllByReceiverIdAndStatus(UUID receiverId, FriendshipStatus status, Pageable pageable);

    Page<Friendship> findAllByRequesterIdAndStatus(UUID requesterId, FriendshipStatus status, Pageable pageable);

    @Query("SELECT f FROM Friendship f WHERE f.status = :status AND (f.requesterId = :studentId OR f.receiverId = :studentId)")
    Page<Friendship> findAllByStatusInvolving(@Param("status") FriendshipStatus status, @Param("studentId") UUID studentId, Pageable pageable);

    @Query("SELECT COUNT(f) FROM Friendship f WHERE f.status = :status AND (f.requesterId = :studentId OR f.receiverId = :studentId)")
    long countByStatusInvolving(@Param("status") FriendshipStatus status, @Param("studentId") UUID studentId);

    @Query("SELECT CASE WHEN f.requesterId = :studentId THEN f.receiverId ELSE f.requesterId END " +
            "FROM Friendship f WHERE f.status = :status AND (f.requesterId = :studentId OR f.receiverId = :studentId)")
    List<UUID> findFriendIds(@Param("studentId") UUID studentId, @Param("status") FriendshipStatus status);

    @Query("SELECT f FROM Friendship f WHERE (f.requesterId = :currentId AND f.receiverId IN :ids) OR (f.receiverId = :currentId AND f.requesterId IN :ids)")
    List<Friendship> findRelevantBetween(@Param("currentId") UUID currentId, @Param("ids") Collection<UUID> ids);

    @Query("SELECT f.requesterId AS id, COUNT(f) AS count FROM Friendship f WHERE f.status = :status AND f.requesterId IN :ids GROUP BY f.requesterId")
    List<IdCount> countGroupedByRequester(@Param("status") FriendshipStatus status, @Param("ids") Collection<UUID> ids);

    @Query("SELECT f.receiverId AS id, COUNT(f) AS count FROM Friendship f WHERE f.status = :status AND f.receiverId IN :ids GROUP BY f.receiverId")
    List<IdCount> countGroupedByReceiver(@Param("status") FriendshipStatus status, @Param("ids") Collection<UUID> ids);

    interface IdCount {
        UUID getId();
        long getCount();
    }
}
