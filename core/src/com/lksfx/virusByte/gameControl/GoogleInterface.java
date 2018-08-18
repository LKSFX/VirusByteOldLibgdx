package com.lksfx.virusByte.gameControl;

public interface GoogleInterface {
	
	/** return if user is signed **/
	public boolean getSignedInGPGS();
	
	/** try login user on google play **/
	public void loginGPGS();
	
	/** send score to the respective highscore id **/
	public void submitScoreGPGS(int score, String highscoreId);
	
	/** unlock the respective achievement **/
	public void unlockAchievementGPGS(String achievementId);
	
	/** get all leaderboards **/
	public void getLeaderboardGPGS();
	
	/** get a specific leaderboard **/
	public void getLeaderboardGPGS(String highscoreId);
	
	/** get achievements **/
	public void getAchievementsGPGS();
}
