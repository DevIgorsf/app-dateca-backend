package com.dat.dateca.domain.ranking;

import com.dat.dateca.domain.friendship.FriendshipRepository;
import com.dat.dateca.domain.friendship.FriendshipStatus;
import com.dat.dateca.domain.student.Student;
import com.dat.dateca.domain.student.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class RankingService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private FriendshipRepository friendshipRepository;

    public RankingResponseDTO getFriendsRanking(Student current, Pageable pageable) {
        List<UUID> participantIds = new ArrayList<>(
                friendshipRepository.findFriendIds(current.getId(), FriendshipStatus.ACCEPTED));
        participantIds.add(current.getId());

        Page<StudentRepository.RankingProjection> page = studentRepository.findRankingByIds(participantIds, pageable);

        long offset = pageable.getOffset();
        List<StudentRepository.RankingProjection> projections = page.getContent();
        List<RankingItemDTO> items = new ArrayList<>(projections.size());
        for (int i = 0; i < projections.size(); i++) {
            StudentRepository.RankingProjection projection = projections.get(i);
            items.add(new RankingItemDTO((int) (offset + i + 1), projection.getId(), projection.getName(), projection.getPoints()));
        }
        Page<RankingItemDTO> rankingPage = new PageImpl<>(items, pageable, page.getTotalElements());

        long higherRanked = studentRepository.countHigherRankedAmong(
                participantIds, current.getPoints(), current.getRegistrationNumber());
        CurrentUserRankingDTO currentUserRanking = new CurrentUserRankingDTO(
                current.getId(), (int) (higherRanked + 1), current.getPoints());

        return new RankingResponseDTO(currentUserRanking, rankingPage);
    }
}
