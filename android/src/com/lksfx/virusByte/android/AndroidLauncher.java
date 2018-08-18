package com.lksfx.virusByte.android;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.games.Games;
import com.immersion.uhl.Launcher;
import com.lksfx.virusByte.VirusByteGame;
import com.lksfx.virusByte.gameControl.AdControl;
import com.lksfx.virusByte.gameControl.GoogleInterface;
import com.google.example.games.basegameutils.GameHelper;
import com.google.example.games.basegameutils.GameHelper.GameHelperListener;


public class AndroidLauncher extends AndroidApplication implements GameHelperListener, AdControl, GoogleInterface {
	
	public GameHelper gameHelper;
	
	public Launcher m_launcher = null;
	
	private static final String AD_UNIT_ID_BANNER = "ca-app-pub-7318392903545148/2285299886";
	private static final String AD_UNIT_ID_INTERSTITIAL  = "ca-app-pub-7318392903545148/6062914286";
	protected AdView adView;
	protected View gameView;
	private InterstitialAd interstitialAd;
	private RelativeLayout mainLayout;
	
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// try create immersion launcher instance
		try {
			m_launcher = new Launcher(this);
		} catch(RuntimeException ex) {
			Gdx.app.log("VirusByte: ", "Immersion Runtime exception");
			ex.printStackTrace();
		}
		
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		config.useAccelerometer = false;
		config.useCompass = false;
		
		//Do the stuff that initialize() would do for you
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
		
		mainLayout = new RelativeLayout(this);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
		mainLayout.setLayoutParams(params);
		
		/*AdView admobView = */createAdView();
		//layout.addView(admobView);
		View gameView = createGameView(config);
		mainLayout.addView(gameView);
		
		setContentView(mainLayout);
		//startAdvertising(admobView);
		
		interstitialAd = new InterstitialAd(this);
		interstitialAd.setAdUnitId(AD_UNIT_ID_INTERSTITIAL);
		interstitialAd.setAdListener(new AdListener() {
			@Override
			public void onAdLoaded() {
				Toast.makeText(getApplicationContext(), "Finished Loading Interstitial", Toast.LENGTH_SHORT).show();
			}
			@Override
			public void onAdClosed() {
				Toast.makeText(getApplicationContext(), "Closed Interstitial", Toast.LENGTH_SHORT).show();
			}
			@Override
			public void onAdFailedToLoad(int errorCode) {
				String errorMsg = "unknow error occurs on load interstitial";;
				switch (errorCode) {
				case AdRequest.ERROR_CODE_INTERNAL_ERROR:
					errorMsg = "internal error on load interstitial";
					break;
				case AdRequest.ERROR_CODE_INVALID_REQUEST:
					errorMsg = "invalid interstitial code request";
					break;
				case AdRequest.ERROR_CODE_NETWORK_ERROR:
					errorMsg = "network error on load interstitial";
					break;
				case AdRequest.ERROR_CODE_NO_FILL:
					errorMsg = "interstitial code no fill";
					break;
				}
				Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_SHORT).show();
			}
		});
		//create game helper instance
		if ( gameHelper == null ) {
			gameHelper = new GameHelper(this, GameHelper.CLIENT_GAMES);
			gameHelper.enableDebugLog(true);
		}
		gameHelper.setConnectOnStart( VirusByteGame.GOOGLE_SERVICES );
		gameHelper.setup(this);
	}
	
	private AdView createAdView() {
		adView = new AdView(this);
		adView.setAdSize(AdSize.SMART_BANNER);
		adView.setAdUnitId(AD_UNIT_ID_BANNER);
		adView.setId(12345); //this is an arbitrary id, allows for relative positioning in createGameView()
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
		params.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
		adView.setLayoutParams(params);
		adView.setBackgroundColor(Color.TRANSPARENT);
		return adView;
	}
	
	private View createGameView(AndroidApplicationConfiguration config) {
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		ServicesAndroid services = new ServicesAndroid(m_launcher, this, this);
		gameView = initializeForView(new VirusByteGame( services ), config);
		gameView.setId(11111);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
		params.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
		//params.addRule(RelativeLayout.BELOW, adView.getId());
		gameView.setLayoutParams(params);
		return gameView;
	}
	
	private void startAdvertising(AdView adView) {
		AdRequest adRequest = new AdRequest.Builder() 
		.addTestDevice(AdRequest.DEVICE_ID_EMULATOR) // Emulator
		.addTestDevice("BBA75383A040A0B652AB35876DA94AAF") // My telephone
		.build();
		adView.loadAd(adRequest);
	}
	
	@Override
	public void onStart() {
		super.onStart();
		gameHelper.onStart(this);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if (adView != null) adView.resume();
	}
	
	@Override
	public void onPause() {
		if (adView != null) adView.pause();
		super.onPause();
	}
	
	@Override
	public void onStop() {
		super.onStop();
		gameHelper.onStop();
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data ) {
		super.onActivityResult(requestCode, resultCode, data);
		gameHelper.onActivityResult(requestCode, resultCode, data);
	}
	
	@Override
	public void onDestroy() {
		if (adView != null) adView.destroy();
		super.onDestroy();
	}
	
	// ================================================= //
	// ==============   AdControl   =============== //
	// ================================================= //
	
	@Override
	public void showInterstitial() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (interstitialAd.isLoaded()) {
					interstitialAd.show();
					Toast.makeText(getApplicationContext(), "Showing Interstitial", Toast.LENGTH_SHORT).show();
				}
			}
		});
	}
	
	@Override
	public void loadInterstitial() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if ( interstitialAd.isLoaded() ) return;
				try {
					AdRequest InterstitialRequest = new AdRequest.Builder()
					.addTestDevice(AdRequest.DEVICE_ID_EMULATOR) // Emulator
					.addTestDevice("BBA75383A040A0B652AB35876DA94AAF") // My telephone
					.build();
					interstitialAd.loadAd(InterstitialRequest);
					Toast.makeText(getApplicationContext(), "Loading Interstitial", Toast.LENGTH_SHORT).show();
				} catch (Exception ex) {}
			}
		});
	}

	@Override
	public void showBanner() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				AdView admobView = createAdView();
				/*RelativeLayout.LayoutParams adParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
				adParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
				adParams.addRule(RelativeLayout.ALIGN_RIGHT);
				adParams.addRule(RelativeLayout.ABOVE, gameView.getId());*/
				mainLayout.addView(admobView);
				startAdvertising(admobView);
				adView.setVisibility(View.VISIBLE);
			}
		});
	}

	@Override
	public void hideBanner() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				adView.setVisibility(View.GONE);
				adView.destroy();
			}
		});
	}

	@Override
	public boolean isInterstitialLoaded() {
		return (interstitialAd != null && interstitialAd.isLoaded());
	}

	@Override
	public boolean isBannerActive() {
		return ( adView != null );
	}

	@Override
	public boolean isBannerVisible() {
		return ( (adView != null) && (adView.getVisibility() == View.VISIBLE) );
	}
	
	// ================================================= //
	// ==============   GoogleServices   =============== //
	// ================================================= //
	/** request codes */
	private final int 
	REQUEST_LEADERBOARD = 100,
	REQUEST_LEADERBOARDS = 200,
	REQUEST_ACHIEVEMENTS = 200;
	
	@Override
	public boolean getSignedInGPGS() {
		return gameHelper.isSignedIn();
	}

	@Override
	public void loginGPGS() {
		try {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					gameHelper.beginUserInitiatedSignIn();
				}
			});
		} catch (final Exception ex) {}
	}

	@Override
	public void submitScoreGPGS(int score, String highscoreId) {
		Games.Leaderboards.submitScore(gameHelper.getApiClient(), highscoreId, score);
	}

	@Override
	public void unlockAchievementGPGS(String achievementId) {
		Games.Achievements.unlock(gameHelper.getApiClient(), achievementId);
	}
	
	@Override
	public void getLeaderboardGPGS() {
		if ( gameHelper.isSignedIn() ) {
			startActivityForResult( Games.Leaderboards.getAllLeaderboardsIntent( gameHelper.getApiClient() ), REQUEST_LEADERBOARDS);
		} else if ( !gameHelper.isConnecting() ) {
			loginGPGS();
		} 
	}
	
	@Override
	public void getLeaderboardGPGS(String highscoreId) {
		if ( gameHelper.isSignedIn() ) {
			startActivityForResult( Games.Leaderboards.getLeaderboardIntent(gameHelper.getApiClient(), highscoreId), REQUEST_LEADERBOARD);
		} else if ( !gameHelper.isConnecting() ) {
			loginGPGS();
		}
	}

	@Override
	public void getAchievementsGPGS() {
		if ( gameHelper.isSignedIn() ) {
			startActivityForResult(Games.Achievements.getAchievementsIntent(gameHelper.getApiClient()), REQUEST_ACHIEVEMENTS);
		} else if ( !gameHelper.isConnecting() ) {
			loginGPGS();
		}
	}
	
	// ================================================= //
	// ============== GameHelperListener =============== //
	// ================================================= //
	
	@Override
	public void onSignInFailed() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSignInSucceeded() {
		// TODO Auto-generated method stub
		
	}

}
