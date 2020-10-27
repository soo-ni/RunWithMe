package kr.co.rwm.service;

import java.time.LocalDateTime;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import kr.co.rwm.entity.Challenge;
import kr.co.rwm.repo.ChallengeRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ChallengeServiceImpl implements ChallengeService {

	private final ChallengeRepository challengeRepository;
	
	@Override
	public void saveChallenge(Challenge challenge) {
		challengeRepository.save(challenge);
	}

	@Override
	public List<Challenge> findAllChallenge() {
		return challengeRepository.findAll();
	}

	@Override
	public Challenge findChallengeByChallengeId(int challengeId) {
		return challengeRepository.findByChallengeId(challengeId)
				.orElseThrow(() -> new IllegalArgumentException("해당 챌린지가 없습니다."));
	}

	@Override
	public Challenge updateChallenge(int challengeId, Challenge challenge) {
		Challenge updateChallenge = challengeRepository.findByChallengeId(challengeId)
				.orElseThrow(() -> new IllegalArgumentException("해당 챌린지가 없습니다."));
		updateChallenge.setTitle(challenge.getTitle());
		updateChallenge.setDistanceGoal(challenge.getDistanceGoal());
		updateChallenge.setDonateGoal(challenge.getDonateGoal());
		updateChallenge.setStartTime(challenge.getStartTime());
		updateChallenge.setEndTime(challenge.getEndTime());
		challengeRepository.save(updateChallenge);
		
		return updateChallenge;
	}

	@Transactional
	@Override
	public void deleteChallenge(int challengeId) {
		Challenge challenge = challengeRepository.findByChallengeId(challengeId)
					.orElseThrow(() -> new IllegalArgumentException("해당 챌린지가 없습니다."));
		challengeRepository.delete(challenge);
	}

	@Override
	public Challenge updateChallengeDistance(int challengeId, int userId, double distance) {
		Challenge challenge = challengeRepository.findByChallengeId(challengeId)
				.orElseThrow(() -> new IllegalArgumentException("해당 챌린지가 없습니다."));
		challenge.setDistanceCurrent(challenge.getDistanceCurrent() + distance);
		challengeRepository.save(challenge);
		
		return null;
	}

	@Override
	public Challenge updateChallengeDonate(int challengeId, long donate) {
		Challenge challenge = challengeRepository.findByChallengeId(challengeId)
				.orElseThrow(() -> new IllegalArgumentException("해당 챌린지가 없습니다."));
		challenge.setDonateCurrent(challenge.getDonateCurrent() + donate);
		challengeRepository.save(challenge);
		
		return null;
	}

	@Override
	public List<Challenge> findAllChallengeGraterThanEndTime(LocalDateTime endTime) {
		List<Challenge> challenges = challengeRepository.findAllByEndTimeGreaterThanEqual(endTime);
		return challenges;
	}

	@Override
	public List<Challenge> findAllChallengeLessThanEndTime(LocalDateTime endTime) {
		List<Challenge> challenges = challengeRepository.findAllByEndTimeLessThanEqual(endTime);
		return challenges;
	}

}
