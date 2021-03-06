package kr.co.rwm.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import kr.co.rwm.dto.RanksDto;
import kr.co.rwm.dto.UserDto;
import kr.co.rwm.entity.Ranks;
import kr.co.rwm.entity.Record;
import kr.co.rwm.entity.Running;
import kr.co.rwm.entity.RunningUser;
import kr.co.rwm.entity.User;
import kr.co.rwm.repo.RanksRepository;
import kr.co.rwm.repo.RecordRepository;
import kr.co.rwm.repo.RunningRepository;
import kr.co.rwm.repo.RunningUserRepository;
import kr.co.rwm.repo.UserRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class RanksServiceImpl implements RanksService{
	private final RanksRepository rankRepository;
	private final RecordRepository recordRepository;
	private final RunningRepository runningRepository;
	private final RunningUserRepository runningUserRepository;
	private final UserRepository userRepository;
	
	private final int DEFAULT_EXP = 0;
	private final int DONATE_EXP = 100;
	
	// 회원가입시 활용 및 매달 스코어 초기화시 사용
	@Override
	public void join(User user) {
		rankRepository.save(Ranks.builder().userId(user).raceExp(DEFAULT_EXP).donateExp(DEFAULT_EXP).totalExp(DEFAULT_EXP).tier(DEFAULT_EXP).build());
	}

	@Override
	public void getRaceExp(int userId, int runningId) {
		// 해당 레코드아이디를 받아오고
		Optional<Running> runnings = runningRepository.findByRunningId(runningId);
		if(runnings.isPresent()) {
			Running running = runnings.get();
			List<Record> list = recordRepository.findAllRecordByRunningId(running);
			
			Comparator<Record> comparator = new Comparator<Record>() {
				@Override
				public int compare(Record o1, Record o2) {
					return Double.compare(o1.getAccDistance(), o2.getAccDistance());
				}
	        };
	        Collections.sort(list,comparator);
			
			double sum = 0.0;
			double distance = 0.0;
			double mul = 1;
			double add = 0.5;
			// 사용자 경험치 부여
			for(int i=0;i<list.size();i++) {
				Record temp = list.get(i);
				double realDistance = temp.getAccDistance();
				sum += ((realDistance - distance)+1) * (temp.getSpeed()*mul);
				distance += realDistance;
				mul +=add;
			}
			Optional<User> members = userRepository.findByUserId(userId);
			if(members.isPresent()) {
				User member = members.get();
				Optional<Ranks> user = rankRepository.findByUserId(member);
				if(user.isPresent()) {
					Ranks temp = user.get(); 
					
					final double total = sum + temp.getTotalExp();
					sum += temp.getRaceExp();
					final double result = sum;
					final int tier = (int)total / 100;
					user.ifPresent(selectUser->{
						selectUser.setRankId(temp.getRankId());
						selectUser.setUserId(temp.getUserId());
						selectUser.setDonateExp(temp.getDonateExp());
						selectUser.setTotalExp(Math.round(total*10)/10.0);
						selectUser.setRaceExp(Math.round(result*10)/10.0);
						selectUser.setTier(tier);
						rankRepository.save(selectUser);
					});
				}
			}
		}
	}

	@Override
	public void getDonateExp(List<User> users) {
		int len = users.size();
		for(int i=0;i<len;i++) {
			User member= users.get(i);
			Optional<Ranks> user = rankRepository.findByUserId(member);
			if(user.isPresent()) {
				Ranks temp = user.get();
				
				final double total = DONATE_EXP + temp.getTotalExp();
				double sum = DONATE_EXP + temp.getDonateExp();
				final double result = sum;
				final int tier = (int)total / 100;
				user.ifPresent(selectUser->{
					selectUser.setRankId(temp.getRankId());
					selectUser.setUserId(temp.getUserId());
					selectUser.setDonateExp(result);
					selectUser.setTotalExp(total);
					selectUser.setRaceExp(temp.getRaceExp());
					selectUser.setTier(tier);
					rankRepository.save(selectUser);
				});
			}
		}
	}

	@Override
	public List<RanksDto> raceTop() {
		// running 정보를 같이 추가해서 전송 - RunningUser
		List<Ranks> ranks = rankRepository.findTop10ByOrderByRaceExpDesc();
		List<RanksDto> result = new ArrayList<RanksDto>();
		RanksDto temp = null;
		
		for(Ranks rank : ranks) {
			RunningUser runningUser = runningUserRepository.findByUserId(rank.getUserId());
			temp = RanksDto.builder()
							.rankId(rank.getRankId())
							.userId(rank.getUserId())
							.raceExp(rank.getRaceExp())
							.donateExp(rank.getDonateExp())
							.totalExp(rank.getTotalExp())
							.tier(rank.getTier())
							.running(runningUser)
							.build();
			result.add(temp);
		}
		
		return result;
		
	}

	@Override
	public List<Ranks> donateTop() {
		return rankRepository.findTop10ByOrderByDonateExpDesc();
	}

	@Override
	public List<Ranks> totalTop() {
		return rankRepository.findTop10ByOrderByTotalExpDesc();
	}

	// 지역별 랭킹
	@Override
	public List<Ranks> totalTopByRegion(int regionId) {
		List<Ranks> list = rankRepository.findAll();
		List<Ranks> region = new ArrayList<Ranks>();
		int len = list.size();
		for(int i=0;i<len;i++) {
			Ranks temp = list.get(i);
			if(temp.getUserId().getGugunId().getGugunId() == regionId) {
				region.add(temp);
			}
		}
		Collections.sort(region,new Comparator<Ranks>(){ 
			@Override
			public int compare(Ranks o1, Ranks o2) {
				return Double.compare(o2.getTotalExp(), o1.getTotalExp());
			}
		});
		if(region.size()<10) {
			return region;
		}else {
			return region.subList(0, 10);
		}
	}

	@Override
	public List<Ranks> totalRank() {
		return rankRepository.findAllByOrderByTotalExpDesc();
	}

	// 사용자 정보 조회 ( 이메일 or 이름 or userid 모두 가능)
	@Override
	public List<Ranks> findByUserId(UserDto userId) {
		User user;
		List<Ranks> list = new ArrayList<Ranks>();
		if(userId.getUserId()!=null) {
			user = userRepository.findByUserId(userId.getUserId());
			Optional<Ranks> rank = rankRepository.findByUserId(user);
			if(rank.isPresent()) {
				list.add(rank.get());
			}
		}else if(userId.getUserEmail() != null) {
			Optional<User> users = userRepository.findByUserEmail(userId.getUserEmail());
			if(users.isPresent()) {
				user = users.get();
				Optional<Ranks> rank = rankRepository.findByUserId(user);
				if(rank.isPresent()) {
					list.add(rank.get());
				}
			}
		}else if(userId.getUsername() != null) {
			List<User> users = userRepository.findByUserName(userId.getUsername());
			for(int i=0;i<users.size();i++) {
				Optional<Ranks> rank = rankRepository.findByUserId(users.get(i));
				if(rank.isPresent()) {
					list.add(rank.get());
				}
			}
		}
		return list;
	}
	
	@Override
	@Transactional
	public void deleteByUserId(User userId) {
		runningUserRepository.deleteByUserId(userId);
		rankRepository.deleteByUserId(userId);
	}

}
