// Copyright (C) 2008 The Android Open Source Project
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.gerrit.client.changes;

import com.google.gerrit.client.Gerrit;
import com.google.gerrit.client.changes.ChangeTable.ApprovalViewType;
import com.google.gerrit.client.rpc.GerritCallback;
import com.google.gerrit.client.rpc.ScreenLoadCallback;
import com.google.gerrit.client.ui.Hyperlink;
import com.google.gerrit.client.ui.Screen;
import com.google.gerrit.common.data.ChangeDetail;
import com.google.gerrit.common.data.ChangeInfo;
import com.google.gerrit.common.data.SingleListChangeInfo;
import com.google.gerrit.reviewdb.AccountGeneralPreferences;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwtexpui.globalkey.client.KeyCommand;

import java.util.List;


public abstract class ChangeSetListScreen extends Screen {
  protected static final String MIN_SORTKEY = "";
  protected static final String MAX_SORTKEY = "z";

  protected final int pageSize;
  private ChangeTable table;
  private ChangeTable.Section section;
  protected Hyperlink prev;
  protected Hyperlink next;
  protected List<ChangeInfo> changes;

  protected final String anchorPrefix;
  protected boolean useLoadPrev;
  protected String pos;

  private TopicInfoBlock descriptionBlock;
  private CommitMessageBlock messageBlock;
  private ApprovalTable approvals;
  protected ChangeDetail detail;
  protected SingleListChangeInfo listChangeInfo;

  protected ChangeSetListScreen(final String anchorToken,
      final String positionToken) {
    anchorPrefix = anchorToken;
    useLoadPrev = positionToken.startsWith("p,");
    pos = positionToken.substring(2);
    detail = null;
    listChangeInfo = null;

    if (Gerrit.isSignedIn()) {
      final AccountGeneralPreferences p =
          Gerrit.getUserAccount().getGeneralPreferences();
      final short m = p.getMaximumPageSize();
      pageSize = 0 < m ? m : AccountGeneralPreferences.DEFAULT_PAGESIZE;
    } else {
      pageSize = AccountGeneralPreferences.DEFAULT_PAGESIZE;
    }
  }

  @Override
  protected void onInitUI() {
    super.onInitUI();
    // Need to properly handle the previous messages
    prev = new Hyperlink(Util.C.pagedChangeListPrev(), true, "");
    prev.setVisible(false);

    next = new Hyperlink(Util.C.pagedChangeListNext(), true, "");
    next.setVisible(false);

    table = new ChangeTable(true) {
      {
        keysNavigation.add(new DoLinkCommand(0, 'p', Util.C
            .changeTablePagePrev(), prev));
        keysNavigation.add(new DoLinkCommand(0, 'n', Util.C
            .changeTablePageNext(), next));
      }
    };
    section = new ChangeTable.Section(null, ApprovalViewType.STRONGEST, null);

    // TODO
    // Probably, we need to put it together as the ChangeDescriptionBlock
    final HorizontalPanel hp = new HorizontalPanel();
    descriptionBlock = new TopicInfoBlock();
    hp.add(descriptionBlock);

    messageBlock = new CommitMessageBlock();
    hp.add(messageBlock);
    add(hp);

    approvals = new ApprovalTable();
    add(approvals);

    table.addSection(section);
    table.setSavePointerId(anchorPrefix);
    add(table);

    final HorizontalPanel buttons = new HorizontalPanel();
    buttons.setStyleName(Gerrit.RESOURCES.css().changeTablePrevNextLinks());
    buttons.add(prev);
    buttons.add(next);
    add(buttons);
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    if (useLoadPrev) {
      loadPrev();
    } else {
      loadNext();
    }
  }

  @Override
  public void registerKeys() {
    super.registerKeys();
    table.setRegisterKeys(true);
  }

  protected abstract void loadPrev();

  protected abstract void loadNext();

  protected abstract void getDetails();

  protected AsyncCallback<SingleListChangeInfo> loadCallback() {
    return new ScreenLoadCallback<SingleListChangeInfo>(this) {
      @Override
      protected void preDisplay(final SingleListChangeInfo result) {
        display(result);
      }
    };
  }

  protected void display(final SingleListChangeInfo result) {
    changes = result.getChanges();
    getDetails();

    if (!changes.isEmpty()) {
      final ChangeInfo f = changes.get(0);
      final ChangeInfo l = changes.get(changes.size() - 1);

      prev.setTargetHistoryToken(anchorPrefix + ",p," + f.getSortKey());
      next.setTargetHistoryToken(anchorPrefix + ",n," + l.getSortKey());

      if (useLoadPrev) {
        prev.setVisible(!result.isAtEnd());
        next.setVisible(!MIN_SORTKEY.equals(pos));
      } else {
        prev.setVisible(!MAX_SORTKEY.equals(pos));
        next.setVisible(!result.isAtEnd());
      }
    }

    table.setAccountInfoCache(result.getAccounts());
    section.display(result.getChanges());
    table.finishDisplay();
  }

  protected AsyncCallback<ChangeDetail> saveDetails() {
    return new GerritCallback<ChangeDetail>() {
      public final void onSuccess(ChangeDetail cDetail) {
        detail = cDetail;
        descriptionBlock.display(detail.getChange(), detail.getAccounts());
        messageBlock.display("Test message to show on the message block");
        approvals.setAccountInfoCache(detail.getAccounts());
        approvals.display(detail.getChange(), detail.getMissingApprovals(), detail
            .getApprovals());
        populateReviewAction();
      }
    };
  }

  private static final class DoLinkCommand extends KeyCommand {
    private final Hyperlink link;

    private DoLinkCommand(int mask, char key, String help, Hyperlink l) {
      super(mask, key, help);
      link = l;
    }

    @Override
    public void onKeyPress(final KeyPressEvent event) {
      if (link.isVisible()) {
        History.newItem(link.getTargetHistoryToken());
      }
    }
  }

  private void populateReviewAction() {
    final Button b = new Button(Util.C.buttonReview());
    b.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(final ClickEvent event) {
        Gerrit.display("changeset,publish," + detail.getCurrentPatchSet().getId().toString(),
            new PublishTopicCommentScreen(detail.getCurrentPatchSet().getId()));
      }
    });
    add(b);
  }
}
