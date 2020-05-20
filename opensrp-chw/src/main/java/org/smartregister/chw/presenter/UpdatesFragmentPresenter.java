package org.smartregister.chw.presenter;

import android.app.Activity;

import org.smartregister.chw.activity.UpdateRegisterDetailsActivity;
import org.smartregister.chw.core.contract.BaseChwNotificationFragmentContract;
import org.smartregister.chw.core.presenter.BaseChwNotificationFragmentPresenter;
import org.smartregister.chw.fragment.UpdatesRegisterFragment;
import org.smartregister.chw.model.UpdatesRegisterModel;

public class UpdatesFragmentPresenter extends BaseChwNotificationFragmentPresenter {

    public UpdatesFragmentPresenter(BaseChwNotificationFragmentContract.View view) {
        super(view, new UpdatesRegisterModel());
    }

    @Override
    public void displayDetailsActivity(String notificationId, String notificationType) {
        Activity activity = ((UpdatesRegisterFragment) getView()).getActivity();
        UpdateRegisterDetailsActivity.startActivity(activity, notificationId, notificationType);
    }
}
