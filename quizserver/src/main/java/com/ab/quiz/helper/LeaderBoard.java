package com.ab.quiz.helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ab.quiz.pojo.PlayerSummary;
import com.ab.quiz.pojo.PrizeDetail;

public class LeaderBoard {
	
	private List<PlayerSummary> playerSummaryList;
	private List<PrizeDetail> prizeDetails;
	
	public LeaderBoard(List<PlayerSummary> playerSummaryList, List<PrizeDetail> prizeDetails) {
		this.playerSummaryList = playerSummaryList;
		this.prizeDetails = prizeDetails;
	}
	
	public List<PlayerSummary> fillRankAndWinningMoney() {
		
		Collections.sort(playerSummaryList, new Comparator<PlayerSummary>() {
			public int compare(final PlayerSummary summary1, final PlayerSummary summary2) {
				Integer correctQuestionCount1 = new Integer(summary1.getCorrectCount());
				Integer correctQuestionCount2 = new Integer(summary2.getCorrectCount());
				int c = correctQuestionCount1.compareTo(correctQuestionCount2);
				c = c * -1;
				if (c == 0) {
					Long totalTimeTake1 = new Long(summary1.getTotalTime());
					Long totalTimeTake2 = new Long(summary2.getTotalTime());
					c = totalTimeTake1.compareTo(totalTimeTake2);
				}
				return c;
			}
		});
		
		int rank = 1;
		for (PlayerSummary obj : playerSummaryList) {
			if (obj.getCorrectCount() == 0) {
				break;
			}
			obj.setRank(rank++);
		}
		
		boolean groupingPresent = false;
		HashMap<String, List<PlayerSummary>> groupByCorrectQuestionCountAndTime = new HashMap<>();
		for (PlayerSummary p : playerSummaryList) {
			if (p.getCorrectCount() == 0) {
				continue;
			}
			String key = p.getCorrectCount() + ":" + p.getTotalTime();
			List<PlayerSummary> groupedList = groupByCorrectQuestionCountAndTime.get(key);
			if (groupedList == null) {
				groupedList = new ArrayList<>();
			}
			groupedList.add(p);
			if (groupedList.size() > 1) {
				groupingPresent = true;
			}
			groupByCorrectQuestionCountAndTime.put(key, groupedList);
		}
		
		HashMap<Integer, Integer> rankVsPrizeMoney = new HashMap<>();
		
		for (PrizeDetail pd : prizeDetails) {
			rankVsPrizeMoney.put(pd.getRank(), pd.getPrizeMoney());
		}
		for (PlayerSummary ps : playerSummaryList) {
			Integer prizeMoney = rankVsPrizeMoney.get(ps.getRank());
			if (prizeMoney != null) {
				ps.setAmountWon(prizeMoney);
			}
		}
		
		if (!groupingPresent) {
			return playerSummaryList;
		}
		
		int maxWinCount = prizeDetails.size();
		Map.Entry<String, List<PlayerSummary>> updatedMaxMapEntry = 
				sameRankHoldersCount(groupByCorrectQuestionCountAndTime, maxWinCount);
		int updatedMaxWinCount = maxWinCount;
		if (updatedMaxMapEntry != null) {
			int lastRankWinners = updatedMaxMapEntry.getValue().size();
			if (lastRankWinners > 1) {
				updatedMaxWinCount = maxWinCount + lastRankWinners - 1;
			}
		}
		int currentRank = 1;
		while (currentRank <= updatedMaxWinCount) {
			updatedMaxMapEntry = sameRankHoldersCount(groupByCorrectQuestionCountAndTime, currentRank);
			if (updatedMaxMapEntry == null) {
				currentRank++;
				continue;
			}
			int groupedCount = updatedMaxMapEntry.getValue().size();
			List<PlayerSummary> groupedPlayerSummary = updatedMaxMapEntry.getValue();
			if (groupedCount > 1) {
				double groupedAmt = 0;
				for (int index = currentRank; index <= (currentRank + groupedCount - 1); index ++) {
					if (rankVsPrizeMoney.get(index) == null) {
						break;
					}
					groupedAmt = groupedAmt + rankVsPrizeMoney.get(index); 
				}
				double dividedShare = groupedAmt/(double)groupedCount;
				int floorVal = (int) Math.floor(dividedShare);
				for (int index = 0; index < groupedPlayerSummary.size(); index++) {
					PlayerSummary groupedSumary = groupedPlayerSummary.get(index);
					groupedSumary.setRank(currentRank);
					groupedSumary.setAmountWon(floorVal);
				}
				currentRank = currentRank + groupedCount - 1;
			}
			currentRank++;
		}
		return playerSummaryList;
	}
	
	private Map.Entry<String, List<PlayerSummary>> sameRankHoldersCount(HashMap<String, List<PlayerSummary>> groupedMap, int rank) {
		
		Set<Map.Entry<String, List<PlayerSummary>>> setValues = groupedMap.entrySet();
		List<PlayerSummary> playerSummaryList = null;
		for (Map.Entry<String, List<PlayerSummary>> eachEntry : setValues) {
			playerSummaryList = eachEntry.getValue();
			for (PlayerSummary summary : playerSummaryList) {
				if (summary.getRank() == rank) {
					return eachEntry;
				}
			}
		}
		return null;
	}
	
	public static void main(String[] args) {
		List<PlayerSummary> playerSumaryList = new ArrayList<>();
		
		PlayerSummary playerSumary1 = new PlayerSummary();
		playerSumary1.setUserProfileId(12);
		playerSumary1.setUserName("Rajasekhar");
		playerSumary1.setTotalTime(90000);
		playerSumary1.setCorrectCount(2);
		playerSumaryList.add(playerSumary1);
		
		PlayerSummary playerSumary2 = new PlayerSummary();
		playerSumary2.setUserProfileId(6);
		playerSumary2.setUserName("User2");
		playerSumary2.setTotalTime(90000);
		playerSumary2.setCorrectCount(0);
		playerSumaryList.add(playerSumary2);
		
		PlayerSummary playerSumary3 = new PlayerSummary();
		playerSumary3.setUserProfileId(2);
		playerSumary3.setUserName("User3");
		playerSumary3.setTotalTime(90000);
		playerSumary3.setCorrectCount(0);
		playerSumaryList.add(playerSumary3);
		
		PlayerSummary playerSumary4 = new PlayerSummary();
		playerSumary4.setUserProfileId(3);
		playerSumary4.setUserName("User4");
		playerSumary4.setTotalTime(90000);
		playerSumary4.setCorrectCount(0);
		playerSumaryList.add(playerSumary4);
		
		PlayerSummary playerSumary5 = new PlayerSummary();
		playerSumary5.setUserProfileId(7);
		playerSumary5.setUserName("User5");
		playerSumary5.setTotalTime(90 * 1000);
		playerSumary5.setCorrectCount(0);
		playerSumaryList.add(playerSumary5);
		
		PlayerSummary playerSumary6 = new PlayerSummary();
		playerSumary6.setUserProfileId(4);
		playerSumary6.setUserName("User6");
		playerSumary6.setTotalTime(90 * 1000);
		playerSumary6.setCorrectCount(0);
		playerSumaryList.add(playerSumary6);
		
		PlayerSummary playerSumary7 = new PlayerSummary();
		playerSumary7.setUserProfileId(5);
		playerSumary7.setUserName("User7");
		playerSumary7.setTotalTime(90 * 1000);
		playerSumary7.setCorrectCount(0);
		playerSumaryList.add(playerSumary7);
		
		PlayerSummary playerSumary8 = new PlayerSummary();
		playerSumary8.setUserProfileId(1);
		playerSumary8.setUserName("User8");
		playerSumary8.setTotalTime(90 * 1000);
		playerSumary8.setCorrectCount(1);
		playerSumaryList.add(playerSumary8);
		
		/*PlayerSummary playerSumary9 = new PlayerSummary();
		playerSumary9.setUserProfileId(9);
		playerSumary9.setUserName("User9");
		playerSumary9.setTotalTime(400 * 1000);
		playerSumary9.setCorrectCount(5);
		playerSumaryList.add(playerSumary9);
		
		PlayerSummary playerSumary10 = new PlayerSummary();
		playerSumary10.setUserProfileId(10);
		playerSumary10.setUserName("User10");
		playerSumary10.setTotalTime(400 * 1000);
		playerSumary10.setCorrectCount(5);
		playerSumaryList.add(playerSumary10);*/
		
		List<PrizeDetail> prizeDetail = Utils.getPrizeDetails(100, playerSumaryList.size());
		System.out.println(prizeDetail);
		LeaderBoard board = new LeaderBoard(playerSumaryList, prizeDetail);
		List<PlayerSummary> finalList = board.fillRankAndWinningMoney();
		System.out.println(finalList);
		
	}
}
