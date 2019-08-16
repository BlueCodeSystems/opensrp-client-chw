package org.smartregister.chw.interactor;

import android.content.Context;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.Months;
import org.joda.time.format.DateTimeFormat;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.R;
import org.smartregister.chw.actionhelper.DewormingAction;
import org.smartregister.chw.actionhelper.ExclusiveBreastFeedingAction;
import org.smartregister.chw.actionhelper.ImmunizationActionHelper;
import org.smartregister.chw.actionhelper.ObservationAction;
import org.smartregister.chw.actionhelper.SleepingUnderLLITNAction;
import org.smartregister.chw.actionhelper.VitaminaAction;
import org.smartregister.chw.anc.AncLibrary;
import org.smartregister.chw.anc.actionhelper.HomeVisitActionHelper;
import org.smartregister.chw.anc.contract.BaseAncHomeVisitContract;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.domain.VisitDetail;
import org.smartregister.chw.anc.fragment.BaseAncHomeVisitFragment;
import org.smartregister.chw.anc.fragment.BaseHomeVisitImmunizationFragment;
import org.smartregister.chw.anc.model.BaseAncHomeVisitAction;
import org.smartregister.chw.anc.util.VisitUtils;
import org.smartregister.chw.application.ChwApplication;
import org.smartregister.chw.core.utils.RecurringServiceUtil;
import org.smartregister.chw.util.Constants;
import org.smartregister.chw.util.VaccineScheduleUtil;
import org.smartregister.domain.Alert;
import org.smartregister.immunization.domain.ServiceWrapper;
import org.smartregister.immunization.domain.VaccineWrapper;
import org.smartregister.immunization.domain.jsonmapping.VaccineGroup;

import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import timber.log.Timber;

import static org.smartregister.chw.core.utils.Utils.dd_MMM_yyyy;
import static org.smartregister.chw.util.JsonFormUtils.getValue;

public abstract class DefaultChwChildHomeVisitInteractor implements ChwChildHomeVisitInteractor.Flavor {
    protected LinkedHashMap<String, BaseAncHomeVisitAction> actionList;
    protected Context context;
    protected Map<String, List<VisitDetail>> details = null;
    protected MemberObject memberObject;
    protected BaseAncHomeVisitContract.View view;
    protected Date dob;
    protected Boolean vaccineCardReceived = false;
    protected Boolean hasBirthCert = false;


    //TODO get vaccineCardReceived FROM DB
    @Override
    public LinkedHashMap<String, BaseAncHomeVisitAction> calculateActions(BaseAncHomeVisitContract.View view, MemberObject memberObject, BaseAncHomeVisitContract.InteractorCallBack callBack) throws BaseAncHomeVisitAction.ValidationException {
        actionList = new LinkedHashMap<>();
        context = view.getContext();
        this.memberObject = memberObject;
        try {
            this.dob = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(memberObject.getDob());
        } catch (ParseException e) {
            Timber.e(e);
        }
        this.view = view;
        // get the preloaded data
        if (view.getEditMode()) {
            Visit lastVisit = AncLibrary.getInstance().visitRepository().getLatestVisit(memberObject.getBaseEntityId(), Constants.EventType.CHILD_HOME_VISIT);
            if (lastVisit != null) {
                details = VisitUtils.getVisitGroups(AncLibrary.getInstance().visitDetailsRepository().getVisits(lastVisit.getVisitId()));
            }
        }

        Map<String, ServiceWrapper> serviceWrapperMap =
                RecurringServiceUtil.getRecurringServices(
                        memberObject.getBaseEntityId(),
                        new DateTime(dob),
                        Constants.SERVICE_GROUPS.CHILD
                );

        try {
            Constants.JSON_FORM.setLocaleAndAssetManager(ChwApplication.getCurrentLocale(), ChwApplication.getInstance().getApplicationContext().getAssets());
            evaluateChildVaccineCard();
            //evaluateImmunization();
            evaluateExclusiveBreastFeeding(serviceWrapperMap);
            evaluateVitaminA(serviceWrapperMap);
            evaluateDeworming(serviceWrapperMap);
            evaluateMNP();
            evaluateMUAC();
            evaluateLLITN();
            evaluateECD();
            evaluateBirthCertForm();
            evaluateObsAndIllness();
        } catch (BaseAncHomeVisitAction.ValidationException e) {
            throw (e);
        } catch (Exception e) {
            Timber.e(e);
        }
        return actionList;
    }

    protected void evaluateChildVaccineCard() throws Exception {
        class ChildVaccineCardHelper extends HomeVisitActionHelper {
            private String child_vaccine_card;
            private LocalDate birthDate;

            public ChildVaccineCardHelper(Date birthDate) {
                this.birthDate = new LocalDate(birthDate);
            }

            private boolean isOverDue() {
                return new LocalDate().isAfter(birthDate.plusMonths(12));
            }

            @Override
            public void onPayloadReceived(String jsonPayload) {
                try {
                    JSONObject jsonObject = new JSONObject(jsonPayload);
                    child_vaccine_card = getValue(jsonObject, "child_vaccine_card");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public String getPreProcessedSubTitle() {
                return MessageFormat.format("{0} {1}",
                        context.getString(isOverDue() ? org.smartregister.chw.core.R.string.overdue : org.smartregister.chw.core.R.string.due),
                        dd_MMM_yyyy.format(birthDate.toDate())
                );
            }

            @Override
            public BaseAncHomeVisitAction.ScheduleStatus getPreProcessedStatus() {
                return isOverDue() ?
                        BaseAncHomeVisitAction.ScheduleStatus.OVERDUE : BaseAncHomeVisitAction.ScheduleStatus.DUE;
            }

            @Override
            public String evaluateSubTitle() {
                if (StringUtils.isBlank(child_vaccine_card))
                    return null;

                return child_vaccine_card.equalsIgnoreCase("Yes") ? context.getString(R.string.yes) : context.getString(R.string.no);
            }

            @Override
            public BaseAncHomeVisitAction.Status evaluateStatusOnPayload() {
                if (StringUtils.isBlank(child_vaccine_card))
                    return BaseAncHomeVisitAction.Status.PENDING;

                if (child_vaccine_card.equalsIgnoreCase("Yes")) {
                    return BaseAncHomeVisitAction.Status.COMPLETED;
                } else if (child_vaccine_card.equalsIgnoreCase("No")) {
                    return BaseAncHomeVisitAction.Status.PARTIALLY_COMPLETED;
                } else {
                    return BaseAncHomeVisitAction.Status.PENDING;
                }
            }
        }

        // expires after 24 months. verify that vaccine card is not received
        if (!new LocalDate().isAfter(new LocalDate(dob).plusMonths(24)) && !vaccineCardReceived) {
            BaseAncHomeVisitAction vaccine_card = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.vaccine_card_title))
                    .withOptional(false)
                    .withDetails(details)
                    .withHelper(new ChildVaccineCardHelper(dob))
                    .withDestinationFragment(BaseAncHomeVisitFragment.getInstance(view, Constants.JSON_FORM.CHILD_HOME_VISIT.getVaccineCard(), null, details, null))
                    .build();

            actionList.put(context.getString(R.string.vaccine_card_title), vaccine_card);
        }
    }

    protected void evaluateImmunization() throws Exception {
        List<VaccineGroup> groups = VaccineScheduleUtil.getVaccineGroups(ChwApplication.getInstance().getApplicationContext(), "child");
        int x = 0;

        for (VaccineGroup group : groups) {

            List<VaccineWrapper> wrappers = VaccineScheduleUtil.getChildDueVaccines(memberObject.getBaseEntityId(), dob, x);

            BaseAncHomeVisitAction action = new BaseAncHomeVisitAction.Builder(context, group.name)
                    .withOptional(false)
                    .withDetails(details)
                    .withProcessingMode(BaseAncHomeVisitAction.ProcessingMode.DETACHED)
                    .withVaccineWrapper(wrappers)
                    .withDestinationFragment(BaseHomeVisitImmunizationFragment.getInstance(view, memberObject.getBaseEntityId(), dob, details, wrappers))
                    .withHelper(new ImmunizationActionHelper(context, wrappers))
                    .build();
            actionList.put(group.name, action);

            x++;
        }
    }

    protected void evaluateBirthCertForm() throws Exception {
        class BirthCertHelper extends HomeVisitActionHelper {
            private String birth_cert;
            private String birth_cert_issue_date;
            private String birth_cert_num;
            private LocalDate birthDate;

            public BirthCertHelper(Date birthDate) {
                this.birthDate = new LocalDate(birthDate);
            }

            private boolean isOverDue() {
                return new LocalDate().isAfter(birthDate.plusMonths(12));
            }

            @Override
            public void onPayloadReceived(String jsonPayload) {
                try {
                    JSONObject jsonObject = new JSONObject(jsonPayload);
                    birth_cert = getValue(jsonObject, "birth_cert");
                    birth_cert_issue_date = getValue(jsonObject, "birth_cert_issue_date");
                    birth_cert_num = getValue(jsonObject, "birth_cert_num");
                } catch (JSONException e) {
                    Timber.e(e);
                }
            }

            @Override
            public String getPreProcessedSubTitle() {
                return MessageFormat.format("{0} {1}",
                        context.getString(isOverDue() ? org.smartregister.chw.core.R.string.overdue : org.smartregister.chw.core.R.string.due),
                        dd_MMM_yyyy.format(birthDate.toDate())
                );
            }

            @Override
            public BaseAncHomeVisitAction.ScheduleStatus getPreProcessedStatus() {
                return isOverDue() ?
                        BaseAncHomeVisitAction.ScheduleStatus.OVERDUE : BaseAncHomeVisitAction.ScheduleStatus.DUE;
            }

            @Override
            public String evaluateSubTitle() {
                if (StringUtils.isBlank(birth_cert))
                    return null;

                String certDate;
                try {
                    Date date = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(birth_cert_issue_date);
                    certDate = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(date);
                } catch (Exception e) {
                    certDate = birth_cert_issue_date;
                }

                return birth_cert.equalsIgnoreCase("Yes") ?
                        MessageFormat.format("{0} {1} (#{2}) ", context.getString(R.string.issued), certDate, birth_cert_num) :
                        context.getString(org.smartregister.chw.core.R.string.not_done);
            }

            @Override
            public BaseAncHomeVisitAction.Status evaluateStatusOnPayload() {
                if (StringUtils.isBlank(birth_cert))
                    return BaseAncHomeVisitAction.Status.PENDING;

                if ("Yes".equalsIgnoreCase(birth_cert)) {
                    return BaseAncHomeVisitAction.Status.COMPLETED;
                } else if (birth_cert.equalsIgnoreCase("No")) {
                    return BaseAncHomeVisitAction.Status.PARTIALLY_COMPLETED;
                } else {
                    return BaseAncHomeVisitAction.Status.PENDING;
                }
            }
        }

        if (!hasBirthCert) {
            BaseAncHomeVisitAction action = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.birth_certification))
                    .withOptional(false)
                    .withDetails(details)
                    .withHelper(new BirthCertHelper(dob))
                    .withFormName(Constants.JSON_FORM.getBirthCertification())
                    .build();

            actionList.put(context.getString(R.string.birth_certification), action);
        }
    }

    protected void evaluateExclusiveBreastFeeding(Map<String, ServiceWrapper> serviceWrapperMap) throws Exception {
        ServiceWrapper serviceWrapper = serviceWrapperMap.get("Exclusive breastfeeding");
        if (serviceWrapper == null) {
            return;
        }

        Alert alert = serviceWrapper.getAlert();
        final String serviceIteration = serviceWrapper.getName().substring(serviceWrapper.getName().length() - 1);

        String title = context.getString(R.string.exclusive_breastfeeding_months, serviceIteration);

        // alert if overdue after 14 days
        boolean isOverdue = new LocalDate().isAfter(new LocalDate(alert.startDate()).plusDays(14));
        String dueState = !isOverdue ? context.getString(R.string.due) : context.getString(R.string.overdue);

        ExclusiveBreastFeedingAction helper = new ExclusiveBreastFeedingAction(context, alert);
        JSONObject jsonObject = org.smartregister.chw.util.JsonFormUtils.getJson(Constants.JSON_FORM.PNC_HOME_VISIT.getExclusiveBreastFeeding(), memberObject.getBaseEntityId());

        BaseAncHomeVisitAction action = new BaseAncHomeVisitAction.Builder(context, title)
                .withHelper(helper)
                .withDetails(details)
                .withOptional(false)
                .withDestinationFragment(BaseAncHomeVisitFragment.getInstance(view, null, jsonObject, details, serviceIteration))
                .withServiceWrapper(serviceWrapper)
                .withScheduleStatus(!isOverdue ? BaseAncHomeVisitAction.ScheduleStatus.DUE : BaseAncHomeVisitAction.ScheduleStatus.OVERDUE)
                .withSubtitle(MessageFormat.format("{0}{1}", dueState, DateTimeFormat.forPattern("dd MMM yyyy").print(new DateTime(serviceWrapper.getVaccineDate()))))
                .build();

        // don't show if its after now
        if (!serviceWrapper.getVaccineDate().isAfterNow()) {
            actionList.put(title, action);
        }

    }

    protected void evaluateVitaminA(Map<String, ServiceWrapper> serviceWrapperMap) throws Exception {
        ServiceWrapper serviceWrapper = serviceWrapperMap.get("Vitamin A");
        if (serviceWrapper == null) {
            return;
        }

        Alert alert = serviceWrapper.getAlert();
        final String serviceIteration = serviceWrapper.getName().substring(serviceWrapper.getName().length() - 1);

        String title = MessageFormat.format(context.getString(R.string.visit_vitamin_a_dose), serviceIteration);

        // alert if overdue after 14 days
        boolean isOverdue = new LocalDate().isAfter(new LocalDate(alert.startDate()).plusDays(14));
        String dueState = !isOverdue ? context.getString(R.string.due) : context.getString(R.string.overdue);

        VitaminaAction helper = new VitaminaAction(context, serviceIteration, alert);
        JSONObject jsonObject = org.smartregister.chw.util.JsonFormUtils.getJson(Constants.JSON_FORM.CHILD_HOME_VISIT.getVitaminA(), memberObject.getBaseEntityId());
        JSONObject preProcessObject = helper.preProcess(jsonObject, serviceIteration);

        BaseAncHomeVisitAction action = new BaseAncHomeVisitAction.Builder(context, title)
                .withHelper(helper)
                .withDetails(details)
                .withOptional(false)
                .withDestinationFragment(BaseAncHomeVisitFragment.getInstance(view, null, preProcessObject, details, serviceIteration))
                .withServiceWrapper(serviceWrapper)
                .withScheduleStatus(!isOverdue ? BaseAncHomeVisitAction.ScheduleStatus.DUE : BaseAncHomeVisitAction.ScheduleStatus.OVERDUE)
                .withSubtitle(MessageFormat.format("{0} {1}", dueState, DateTimeFormat.forPattern("dd MMM yyyy").print(new DateTime(serviceWrapper.getVaccineDate()))))
                .build();

        // don't show if its after now
        if (!serviceWrapper.getVaccineDate().isAfterNow()) {
            actionList.put(title, action);
        }
    }

    protected void evaluateDeworming(Map<String, ServiceWrapper> serviceWrapperMap) throws Exception {
        ServiceWrapper serviceWrapper = serviceWrapperMap.get("Deworming");
        if (serviceWrapper == null) {
            return;
        }

        Alert alert = serviceWrapper.getAlert();
        final String serviceIteration = serviceWrapper.getName().substring(serviceWrapper.getName().length() - 1);

        String title = MessageFormat.format(context.getString(R.string.deworming), serviceIteration);

        // alert if overdue after 14 days
        boolean isOverdue = new LocalDate().isAfter(new LocalDate(alert.startDate()).plusDays(14));
        String dueState = !isOverdue ? context.getString(R.string.due) : context.getString(R.string.overdue);

        DewormingAction helper = new DewormingAction(context, serviceIteration, alert);
        JSONObject jsonObject = org.smartregister.chw.util.JsonFormUtils.getJson(Constants.JSON_FORM.CHILD_HOME_VISIT.getDEWORMING(), memberObject.getBaseEntityId());
        JSONObject preProcessObject = helper.preProcess(jsonObject, serviceIteration);

        BaseAncHomeVisitAction action = new BaseAncHomeVisitAction.Builder(context, title)
                .withHelper(helper)
                .withDetails(details)
                .withOptional(false)
                .withDestinationFragment(BaseAncHomeVisitFragment.getInstance(view, null, preProcessObject, details, serviceIteration))
                .withServiceWrapper(serviceWrapper)
                .withScheduleStatus(!isOverdue ? BaseAncHomeVisitAction.ScheduleStatus.DUE : BaseAncHomeVisitAction.ScheduleStatus.OVERDUE)
                .withSubtitle(MessageFormat.format("{0} {1}", dueState, DateTimeFormat.forPattern("dd MMM yyyy").print(new DateTime(serviceWrapper.getVaccineDate()))))
                .build();

        // don't show if its after now
        if (!serviceWrapper.getVaccineDate().isAfterNow()) {
            actionList.put(title, action);
        }
    }

    protected void evaluateMNP() throws Exception {
    }

    protected void evaluateMUAC() throws Exception {
    }

    protected void evaluateLLITN() throws Exception {
        if (getAgeInMonths() < 60) {
            BaseAncHomeVisitAction sleeping = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.anc_home_visit_sleeping_under_llitn_net))
                    .withOptional(false)
                    .withDetails(details)
                    .withHelper(new SleepingUnderLLITNAction())
                    .withDestinationFragment(BaseAncHomeVisitFragment.getInstance(view, Constants.JSON_FORM.ANC_HOME_VISIT.getSleepingUnderLlitn(), null, details, null))
                    .build();

            actionList.put(context.getString(R.string.anc_home_visit_sleeping_under_llitn_net), sleeping);
        }
    }

    protected void evaluateECD() throws Exception {
    }

    protected void evaluateObsAndIllness() throws Exception {
        BaseAncHomeVisitAction observation = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.anc_home_visit_observations_n_illnes))
                .withOptional(true)
                .withDetails(details)
                .withHelper(new ObservationAction())
                .withFormName(Constants.JSON_FORM.ANC_HOME_VISIT.getObservationAndIllness())
                .build();

        actionList.put(context.getString(R.string.anc_home_visit_observations_n_illnes), observation);
    }

    protected int getAgeInMonths() {
        return Months.monthsBetween(new LocalDate(dob), new LocalDate()).getMonths();
    }
}