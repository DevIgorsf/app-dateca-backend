package com.dat.dateca.domain.friendship;

import com.dat.dateca.domain.student.Student;
import com.dat.dateca.domain.student.StudentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FriendshipService {

    @Autowired
    private FriendshipRepository friendshipRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Transactional
    public FriendshipResponseDTO sendRequest(Student requester, UUID receiverId) {
        if (requester.getId().equals(receiverId)) {
            throw new CannotFriendYourselfException();
        }

        Student receiver = studentRepository.findById(receiverId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado: " + receiverId));

        Friendship friendship = friendshipRepository.findBetween(requester.getId(), receiverId).orElse(null);

        if (friendship == null) {
            friendship = new Friendship(requester.getId(), receiverId);
        } else {
            switch (friendship.getStatus()) {
                case PENDING -> throw new DuplicateFriendshipException("Já existe uma solicitação pendente entre esses usuários");
                case ACCEPTED -> throw new DuplicateFriendshipException("Vocês já são amigos");
                case BLOCKED -> throw new BlockedUserException();
                case DECLINED, CANCELLED, REMOVED -> friendship.resendFrom(requester.getId(), receiverId);
            }
        }

        friendshipRepository.save(friendship);
        return toResponseDTO(friendship, requester, receiver);
    }

    @Transactional
    public FriendshipResponseDTO accept(Student current, UUID friendshipId) {
        Friendship friendship = getOwned(friendshipId, current.getId());
        if (!friendship.getReceiverId().equals(current.getId())) {
            throw new ForbiddenFriendshipActionException("Somente quem recebeu a solicitação pode aceitá-la");
        }
        requireStatus(friendship, FriendshipStatus.PENDING);
        friendship.accept();
        friendshipRepository.save(friendship);
        return toResponseDTO(friendship);
    }

    @Transactional
    public FriendshipResponseDTO decline(Student current, UUID friendshipId) {
        Friendship friendship = getOwned(friendshipId, current.getId());
        if (!friendship.getReceiverId().equals(current.getId())) {
            throw new ForbiddenFriendshipActionException("Somente quem recebeu a solicitação pode recusá-la");
        }
        requireStatus(friendship, FriendshipStatus.PENDING);
        friendship.decline();
        friendshipRepository.save(friendship);
        return toResponseDTO(friendship);
    }

    @Transactional
    public FriendshipResponseDTO cancel(Student current, UUID friendshipId) {
        Friendship friendship = getOwned(friendshipId, current.getId());
        if (!friendship.getRequesterId().equals(current.getId())) {
            throw new ForbiddenFriendshipActionException("Somente quem enviou a solicitação pode cancelá-la");
        }
        requireStatus(friendship, FriendshipStatus.PENDING);
        friendship.cancel();
        friendshipRepository.save(friendship);
        return toResponseDTO(friendship);
    }

    @Transactional
    public void remove(Student current, UUID friendshipId) {
        Friendship friendship = getOwned(friendshipId, current.getId());
        requireStatus(friendship, FriendshipStatus.ACCEPTED);
        friendship.remove();
        friendshipRepository.save(friendship);
    }

    @Transactional
    public FriendshipResponseDTO block(Student current, UUID friendshipId) {
        Friendship friendship = getOwned(friendshipId, current.getId());
        if (friendship.getStatus() == FriendshipStatus.BLOCKED) {
            throw new IllegalFriendshipStateException("Este usuário já está bloqueado");
        }
        friendship.block(current.getId());
        friendshipRepository.save(friendship);
        return toResponseDTO(friendship);
    }

    @Transactional
    public FriendshipResponseDTO unblock(Student current, UUID friendshipId) {
        Friendship friendship = getOwned(friendshipId, current.getId());
        requireStatus(friendship, FriendshipStatus.BLOCKED);
        if (!current.getId().equals(friendship.getBlockedBy())) {
            throw new ForbiddenFriendshipActionException("Somente quem bloqueou pode desbloquear");
        }
        friendship.unblock();
        friendshipRepository.save(friendship);
        return toResponseDTO(friendship);
    }

    public Page<FriendshipResponseDTO> listReceived(Student current, Pageable pageable) {
        Page<Friendship> page = friendshipRepository.findAllByReceiverIdAndStatus(current.getId(), FriendshipStatus.PENDING, pageable);
        return enrich(page);
    }

    public Page<FriendshipResponseDTO> listSent(Student current, Pageable pageable) {
        Page<Friendship> page = friendshipRepository.findAllByRequesterIdAndStatus(current.getId(), FriendshipStatus.PENDING, pageable);
        return enrich(page);
    }

    public Page<FriendDTO> listFriends(Student current, Pageable pageable) {
        Page<Friendship> page = friendshipRepository.findAllByStatusInvolving(FriendshipStatus.ACCEPTED, current.getId(), pageable);
        Set<UUID> counterpartIds = page.getContent().stream()
                .map(f -> f.counterpartOf(current.getId()))
                .collect(Collectors.toSet());
        Map<UUID, Student> students = studentRepository.findAllById(counterpartIds).stream()
                .collect(Collectors.toMap(Student::getId, s -> s));
        return page.map(f -> new FriendDTO(students.get(f.counterpartOf(current.getId())), f.getAcceptedAt()));
    }

    public long countFriends(Student current) {
        return friendshipRepository.countByStatusInvolving(FriendshipStatus.ACCEPTED, current.getId());
    }

    public Page<UserSearchDTO> searchUsers(Student current, String query, Pageable pageable) {
        Page<Student> page = studentRepository.search(query, pageable);
        List<UUID> ids = page.getContent().stream().map(Student::getId).collect(Collectors.toList());

        Map<UUID, RelationshipStatus> relationshipStatuses = new HashMap<>();
        Map<UUID, Long> friendCounts = new HashMap<>();
        if (!ids.isEmpty()) {
            friendshipRepository.findRelevantBetween(current.getId(), ids)
                    .forEach(f -> relationshipStatuses.put(f.counterpartOf(current.getId()), resolveStatus(f, current.getId())));

            friendshipRepository.countGroupedByRequester(FriendshipStatus.ACCEPTED, ids)
                    .forEach(ic -> friendCounts.merge(ic.getId(), ic.getCount(), Long::sum));
            friendshipRepository.countGroupedByReceiver(FriendshipStatus.ACCEPTED, ids)
                    .forEach(ic -> friendCounts.merge(ic.getId(), ic.getCount(), Long::sum));
        }

        return page.map(student -> {
            RelationshipStatus status = student.getId().equals(current.getId())
                    ? RelationshipStatus.SELF
                    : relationshipStatuses.getOrDefault(student.getId(), RelationshipStatus.NONE);
            long friendsCount = friendCounts.getOrDefault(student.getId(), 0L);
            return new UserSearchDTO(student, friendsCount, status);
        });
    }

    private RelationshipStatus resolveStatus(Friendship friendship, UUID currentId) {
        return switch (friendship.getStatus()) {
            case ACCEPTED -> RelationshipStatus.FRIEND;
            case BLOCKED -> RelationshipStatus.BLOCKED;
            case PENDING -> friendship.getRequesterId().equals(currentId)
                    ? RelationshipStatus.PENDING_SENT
                    : RelationshipStatus.PENDING_RECEIVED;
            case DECLINED, CANCELLED, REMOVED -> RelationshipStatus.NONE;
        };
    }

    private Friendship getOwned(UUID friendshipId, UUID currentId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new FriendshipNotFoundException(friendshipId));
        if (!friendship.involves(currentId)) {
            throw new ForbiddenFriendshipActionException("Você não participa desta amizade");
        }
        return friendship;
    }

    private void requireStatus(Friendship friendship, FriendshipStatus expected) {
        if (friendship.getStatus() != expected) {
            throw new IllegalFriendshipStateException("Ação inválida para o status atual: " + friendship.getStatus());
        }
    }

    private FriendshipResponseDTO toResponseDTO(Friendship friendship) {
        Student requester = studentRepository.findById(friendship.getRequesterId()).orElse(null);
        Student receiver = studentRepository.findById(friendship.getReceiverId()).orElse(null);
        return toResponseDTO(friendship, requester, receiver);
    }

    private FriendshipResponseDTO toResponseDTO(Friendship friendship, Student requester, Student receiver) {
        return new FriendshipResponseDTO(
                friendship,
                requester != null ? requester.getName() : null,
                receiver != null ? receiver.getName() : null
        );
    }

    private Page<FriendshipResponseDTO> enrich(Page<Friendship> page) {
        Set<UUID> ids = new HashSet<>();
        page.getContent().forEach(f -> {
            ids.add(f.getRequesterId());
            ids.add(f.getReceiverId());
        });
        Map<UUID, Student> students = studentRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Student::getId, s -> s));
        return page.map(f -> new FriendshipResponseDTO(
                f,
                students.containsKey(f.getRequesterId()) ? students.get(f.getRequesterId()).getName() : null,
                students.containsKey(f.getReceiverId()) ? students.get(f.getReceiverId()).getName() : null
        ));
    }
}
