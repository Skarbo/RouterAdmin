package com.skarbo.routeradmin.fragment;

import java.util.Date;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import com.skarbo.routeradmin.R;
import com.skarbo.routeradmin.RouterAdminActivity;
import com.skarbo.routeradmin.container.RestartToolsContainer;
import com.skarbo.routeradmin.handler.RouterHandler;
import com.skarbo.routeradmin.listener.RestartToolsListener;
import com.skarbo.routeradmin.listener.RouterHandlerListener;

public class RestartToolsFragment extends Fragment implements RouterHandlerListener, RestartToolsListener {

	private static final String TAG = RestartToolsFragment.class.getSimpleName();

	private RouterHandler routerHandler;

	private Button restartToggleButton;
	private ProgressBar restartProgressBar;
	private CountDownTimer countDownTimer;

	// ... ON

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Log.d(TAG, "On activity created");

		this.routerHandler = ((RouterAdminActivity) getActivity()).getRouterHandler();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_tools_restart, container, false);

		restartToggleButton = (Button) view.findViewById(R.id.tools_router_restart_toggle_button);
		restartProgressBar = (ProgressBar) view.findViewById(R.id.tools_router_restart_progress_bar);
		restartToggleButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				doRestart();
			}
		});

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.d(TAG, "OnResume");
		if (this.routerHandler != null)
			this.routerHandler.addListener(TAG, this);

		doUpdateView();
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.d(TAG, "OnPause");
		if (this.routerHandler != null)
			this.routerHandler.removeListener(TAG);
	}

	// ... ... ROUTER

	@Override
	public void onUpdating() {
		restartToggleButton.setEnabled(false);
	}

	@Override
	public void onUpdated() {
		restartToggleButton.setEnabled(true);
	}

	@Override
	public void onRefresh() {

	}

	@Override
	public void onRestarting() {
		Log.d(TAG, "OnRestarting");
		doUpdateView();
	}

	@Override
	public void onRestarted() {
		Log.d(TAG, "OnRestarted");
		doUpdateView();
	}

	// ... ... /ROUTER

	// ... /ON

	// ... DO

	private void doRestart() {
		Log.d(TAG, "DoRestart");
		if (routerHandler != null)
			routerHandler.getControlHandler().doToolsRestart();
	}

	private void doUpdateView() {
		boolean enabled = !this.routerHandler.getControlHandler().isQueueHandling();
		restartToggleButton.setEnabled(enabled);

		// RESTARTING

		final RestartToolsContainer restartToolsContainer = this.routerHandler.getControlHandler().getContainers()
				.getRestartToolsContainer();
		if (countDownTimer != null)
			countDownTimer.cancel();
		if (restartToolsContainer.restarting) {
			Log.d(TAG, "Do update view progress bar");
			final int totalDelay = (int) ((restartToolsContainer.delay * 1000) - ((new Date()).getTime() - restartToolsContainer.time));
			restartProgressBar.setEnabled(true);
			restartProgressBar.setMax(totalDelay);
			countDownTimer = new CountDownTimer(totalDelay, 1000) {
				public void onTick(long millisUntilFinished) {
					int progres = (int) (totalDelay - millisUntilFinished);
					Log.d(TAG, "Do update view countdown tick: " + progres);
					restartProgressBar.setProgress(progres);
				}

				public void onFinish() {
					Log.d(TAG, "Do update view countdown finish");
					restartProgressBar.setProgress(0);
				}
			};
			countDownTimer.start();
		} else {
			restartProgressBar.setProgress(0);
			restartProgressBar.setEnabled(false);
		}

		// /RESTARTING
	}

	// ... /DO

}
