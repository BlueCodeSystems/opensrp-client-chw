package org.smartregister.brac.hnpp.presenter;

import android.util.Pair;

import org.smartregister.brac.hnpp.model.HnppChildRegisterModel;
import org.smartregister.chw.core.contract.CoreChildProfileContract;
import org.smartregister.chw.core.presenter.CoreChildProfilePresenter;
import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.interactor.HfChildProfileInteractor;
import org.smartregister.brac.hnpp.interactor.HnppFamilyProfileInteractor;
import org.smartregister.clientandeventmodel.Client;
import org.smartregister.clientandeventmodel.Event;

import java.lang.ref.WeakReference;

public class HfChildProfilePresenter extends CoreChildProfilePresenter {
    String houseHoldId = "";

    public HfChildProfilePresenter(CoreChildProfileContract.View childView, CoreChildProfileContract.Model model,String houseHoldId, String childBaseEntityId) {
        this.houseHoldId = houseHoldId;
        setView(new WeakReference<>(childView));
        setInteractor(new HfChildProfileInteractor());
        setModel(model);
        setChildBaseEntityId(childBaseEntityId);
    }

    @Override
    public void verifyHasPhone() {
        new HnppFamilyProfileInteractor().verifyHasPhone(familyID, this);
    }

    @Override
    public void updateChildProfile(String jsonString) {
        getView().showProgressDialog(R.string.updating);
        Pair<Client, Event> pair = new HnppChildRegisterModel(houseHoldId,familyID).processRegistration(jsonString);
        if (pair == null) {
            return;
        }

        getInteractor().saveRegistration(pair, jsonString, true, this);
    }
}
