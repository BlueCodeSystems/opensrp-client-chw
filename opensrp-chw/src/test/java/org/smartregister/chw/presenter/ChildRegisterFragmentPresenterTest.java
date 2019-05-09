package org.smartregister.chw.presenter;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.smartregister.chw.BaseUnitTest;
import org.smartregister.chw.contract.ChildRegisterFragmentContract;

public class ChildRegisterFragmentPresenterTest extends BaseUnitTest {

    private ChildRegisterFragmentPresenter presenter;

    @Mock
    private ChildRegisterFragmentContract.View view;

    @Mock
    private ChildRegisterFragmentContract.Model model;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        presenter = new ChildRegisterFragmentPresenter(view, model, "");
    }

    @Test
    public void testMainCondition() {
        Assert.assertEquals(" date_removed is null AND  (( strftime('%Y','now') - strftime('%Y',dob))<5)", presenter.getMainCondition());

    }

    @Test
    public void testMainConditionWithTableName() {
        String tableName = "table_a";
        Assert.assertEquals(" table_a.date_removed is null AND  (( strftime('%Y','now') - strftime('%Y',table_a.dob))<5)", presenter.getMainCondition(tableName));

    }

    @Test
    public void testDefaultSortQuery() {
        Assert.assertEquals("last_interacted_with DESC ", presenter.getDefaultSortQuery());

    }

    @Test
    public void testDueAndFilterCondition() {
        Assert.assertEquals(" date_removed is null AND  (( strftime('%Y','now') - strftime('%Y',dob))<5) AND (( IFNULL(STRFTIME('%Y%m%d%H%M%S', datetime((last_home_visit)/1000,'unixepoch')),0) < STRFTIME('%Y%m%d%H%M%S', datetime('now','start of month')) AND IFNULL(STRFTIME('%Y%m%d%H%M%S', datetime((visit_not_done)/1000,'unixepoch')),0) < STRFTIME('%Y%m%d%H%M%S', datetime('now','start of month'))  ))", presenter.getDueFilterCondition());

    }
}